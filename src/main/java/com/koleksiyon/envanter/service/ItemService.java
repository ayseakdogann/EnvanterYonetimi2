package com.koleksiyon.envanter.service;

import com.koleksiyon.envanter.entity.Bid;
import com.koleksiyon.envanter.entity.Item;
import com.koleksiyon.envanter.entity.SystemVault;
import com.koleksiyon.envanter.repository.BidRepository;
import com.koleksiyon.envanter.repository.ItemRepository;
import com.koleksiyon.envanter.repository.SystemVaultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.koleksiyon.envanter.entity.User;
import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor // Lombok ile constructor injection işlemini otomatik yapar
public class ItemService {

    private final ItemRepository itemRepository;
    private final SystemVaultRepository systemVaultRepository;
    private final BidRepository bidRepository;
    private final com.koleksiyon.envanter.repository.UserRepository userRepository;

    // Tüm parçaları listeleme (Read)
    public List<Item> getAllItems() {
        return itemRepository.findAll();
    }

    // Yeni parça ekleme (Create)
    public void saveItem(Item item, MultipartFile file) throws IOException {
        // Eğer kullanıcı bir dosya yüklediyse ve dosya boş değilse
        if (file != null && !file.isEmpty()) {
            // MultipartFile'ı byte array'e (BLOB) çevirip entity'e set ediyoruz
            item.setImage(file.getBytes());
        }
        itemRepository.save(item);
    }

    // ID'ye göre tek bir parça bulma
    public Item getItemById(Long id) {
        return itemRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Geçersiz Parça ID:" + id));
    }

    // Parça silme (Delete)
    @Transactional
    public void deleteItem(Long id) {
        //Önce bu ürüne yapılmış tüm teklifleri bul ve veritabanından temizle
        List<Bid> bids = bidRepository.findByItemIdOrderByAmountDesc(id);
        if (bids != null && !bids.isEmpty()) {
            bidRepository.deleteAll(bids);
        }

        //Bağlı teklifler silindikten sonra artık ürünü güvenle silebiliriz
        itemRepository.deleteById(id);
    }

    // Dinamik arama işlemi
    public List<Item> searchItems(String keyword) {
        if (keyword != null && !keyword.isEmpty()) {
            return itemRepository.findByNameContainingIgnoreCase(keyword);
        }
        return itemRepository.findAll();
    }

    // SATIŞI TAMAMLAMA VE KOMİSYON KESME METODU
    @Transactional
    public void closeAuction(Long itemId, String loggedInUsername) {
        Item item = getItemById(itemId);

        if (!item.getOwner().getUsername().equals(loggedInUsername)) {
            throw new IllegalStateException("Bu işlemi sadece ürünün sahibi yapabilir!");
        }
        if (item.isSold()) {
            throw new IllegalStateException("Bu ürün zaten satıldı.");
        }
        if (item.getCurrentHighestBid() == null) {
            throw new IllegalStateException("Ürüne henüz teklif verilmemiş.");
        }

        // Ürünü satıldı olarak işaretle
        item.setSold(true);
        itemRepository.save(item);

        // Parayı Paylaştır (%10 Kasa, %90 Satıcı)
        Double finalPrice = item.getCurrentHighestBid();
        Double commissionAmount = finalPrice * 0.10;
        Double sellerEarnings = finalPrice - commissionAmount;

        // Yüzde 10 Komisyonu Kasaya Ekle
        SystemVault vault = systemVaultRepository.findById(1L).orElse(new SystemVault());
        vault.setId(1L);
        vault.setTotalCommission(vault.getTotalCommission() + commissionAmount);
        systemVaultRepository.save(vault);

        // Kalan %90'ı Satıcının Cüzdanına Ekle
        User owner = item.getOwner();
        if (owner.getBalance() == null) owner.setBalance(0.0);
        owner.setBalance(owner.getBalance() + sellerEarnings);
        userRepository.save(owner);
    }

    // Teklif Verme Metodu
    @Transactional
    public void placeBid(Long itemId, Double amount, User bidder) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Ürün bulunamadı"));

        // Mevcut fiyat kontrolü
        Double currentPrice = item.getCurrentHighestBid() != null ?
                item.getCurrentHighestBid() : item.getStartingPrice();

        if (amount <= currentPrice) {
            throw new IllegalArgumentException("Teklifiniz mevcut fiyattan yüksek olmalıdır!");
        }

        // Teklif nesnesini oluştur ve kaydet
        Bid bid = new Bid();
        bid.setItem(item);
        bid.setBidder(bidder);
        bid.setAmount(amount);
        bidRepository.save(bid);

        // Ürünün en yüksek teklif bilgisini güncelle
        item.setCurrentHighestBid(amount);
        itemRepository.save(item);
    }

    // Sadece satışta olanları getiren temel metot
    public List<Item> getItemsForSale(String sort, String category) {
        if (category != null && !category.isEmpty()) {
            return itemRepository.findByForSaleTrueAndTypeContainingIgnoreCase(category);
        }

        if (sort != null) {
            switch (sort) {
                case "newest": return itemRepository.findByForSaleTrueOrderByCreatedAtDesc();
                case "price_asc": return itemRepository.findByForSaleTrueOrderByStartingPriceAsc();
                // Varsayılan olarak satıştakileri getir
                default: return itemRepository.findByForSaleTrue();
            }
        }
        return itemRepository.findByForSaleTrue();
    }
    @Transactional
    public void deleteBidsByUserId(Long userId) {
        // BidRepository üzerinden kullanıcıya ait teklifleri siler
        bidRepository.deleteByUserId(userId);
    }
    // Kullanıcının koleksiyonundaki (satılık olsun olmasın) her şeyi getirmek için
    @Transactional(readOnly = true)
    public List<Item> getItemsByOwner(User owner) {
        return itemRepository.findByOwner(owner);
    }
}
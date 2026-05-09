package com.koleksiyon.envanter.repository;

import com.koleksiyon.envanter.entity.Bid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BidRepository extends JpaRepository<Bid, Long> {
    // Bir ürüne gelen tüm teklifleri en yüksekten en düşüğe sıralamak için
    List<Bid> findByItemIdOrderByAmountDesc(Long itemId);
}
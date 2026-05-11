package com.koleksiyon.envanter.repository;

import com.koleksiyon.envanter.entity.Bid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface BidRepository extends JpaRepository<Bid, Long> {

    // Bir ürüne gelen tüm teklifleri en yüksekten en düşüğe sıralar
    List<Bid> findByItemIdOrderByAmountDesc(Long itemId);

    //Kullanıcı silinirken ona ait tüm teklifleri silmek için kullanılır
    @Modifying
    @Transactional
    @Query("DELETE FROM Bid b WHERE b.bidder.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}
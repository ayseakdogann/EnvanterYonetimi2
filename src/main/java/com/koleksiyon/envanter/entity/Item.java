package com.koleksiyon.envanter.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import java.sql.Types;

@Entity
@Table(name = "items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String type;

    @Column(length = 5000)
    private String description;

    @Lob //büyük boyutlu resimlerin saklanacagını belirtir
    @JdbcTypeCode(Types.VARBINARY) // 'lob stream' hatasını önlemek için veriyi doğrudan binary olarak saklar
    @Column(name = "image")
    private byte[] image;

    private Double startingPrice; // Başlangıç Fiyatı

    private Double currentHighestBid; // Şu anki en yüksek teklif

    private boolean isSold = false; // Ürün satıldı mı?

    @ManyToOne(fetch = FetchType.LAZY) // her ürün bir kullanıcıya bağlı
    @JoinColumn(name = "owner_id")
    private User owner; // Ürünü satışa koyan kişi (Sahibi)

    private boolean forSale = false; // Varsayılan olarak ürün sadece koleksiyondadır, satılık değildir.

    private java.time.LocalDateTime createdAt = java.time.LocalDateTime.now(); // eklenme tarihi
}
package com.koleksiyon.envanter.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode; // Yeni eklendi
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true) // sadece id üzerinden karşılaştırma yapılarak sonsuz döngüler engelendi
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    private String email;
    private String role;

    @Column(length = 500)
    private String bio;

    private String city;
    private LocalDate birthDate;

    @Lob
    @org.hibernate.annotations.JdbcTypeCode(java.sql.Types.VARBINARY)
    private byte[] profileImage;

    // Takip Sistemi
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "user_follows",
            joinColumns = @JoinColumn(name = "follower_id"),
            inverseJoinColumns = @JoinColumn(name = "following_id"))
    private Set<User> following = new HashSet<>();

    @ManyToMany(mappedBy = "following", fetch = FetchType.LAZY)
    private Set<User> followers = new HashSet<>();

    // Kullanıcının sistemdeki parası
    private Double balance = 0.0;
}
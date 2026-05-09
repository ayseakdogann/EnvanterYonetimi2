package com.koleksiyon.envanter.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "system_vault")
@Data
public class SystemVault {

    @Id
    private Long id = 1L; // Sistemde sadece 1 adet kasa olacak

    private Double totalCommission = 0.0; // Biriken toplam para
}
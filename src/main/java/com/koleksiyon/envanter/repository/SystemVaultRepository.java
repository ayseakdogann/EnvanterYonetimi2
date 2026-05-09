package com.koleksiyon.envanter.repository;

import com.koleksiyon.envanter.entity.SystemVault;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SystemVaultRepository extends JpaRepository<SystemVault, Long> {
}
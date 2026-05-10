package com.koleksiyon.envanter.repository;

import com.koleksiyon.envanter.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    @EntityGraph(attributePaths = {"followers", "following"})
    Optional<User> findByUsername(String username);
}
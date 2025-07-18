package org.hotiver.repo;

import org.hotiver.domain.Entity.Seller;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SellerRepo extends JpaRepository<Seller, Long> {

    boolean existsByNickname(String requestedNickname);

    @Query("SELECT s FROM Seller s WHERE s.user.email = :email")
    Seller findByEmail(String email);

    @Query("SELECT s FROM Seller s WHERE s.nickname = :username")
    Optional<Seller> findByUsername(String username);
}

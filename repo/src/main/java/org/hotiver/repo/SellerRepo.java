package org.hotiver.repo;

import org.hotiver.domain.Entity.Seller;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface SellerRepo extends JpaRepository<Seller, Long> {

    boolean existsByNickname(String requestedNickname);

    @Query("SELECT s FROM Seller s WHERE s.user.email = :email")
    Optional<Seller> findByEmail(String email);

    @Query("SELECT s FROM Seller s WHERE s.nickname = :nickname")
    Optional<Seller> findByNickname(String nickname);
}

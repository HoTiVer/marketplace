package org.hotiver.repo;

import org.hotiver.domain.Entity.Seller;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SellerRepo extends JpaRepository<Seller, Long> {

    boolean existsByNickname(String requestedNickname);
}

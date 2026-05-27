package org.hotiver.repo.core;

import org.hotiver.common.Enum.SellerRegisterRequestStatus;
import org.hotiver.domain.Entity.SellerRegister;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;

public interface SellerRegisterRepo extends JpaRepository<SellerRegister, Long> {

    @Query("""
            SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END
            FROM SellerRegister s
            WHERE s.userId = :userId AND s.requestDate = :today""")
    boolean existsByUserIdAndRequestDate(Long userId, Date today);

    @Query("""
            SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END
            FROM SellerRegister s
            WHERE s.userId = :userId AND s.status = :sellerRegisterRequestStatus""")
    boolean existsByUserIdAndStatus(Long userId,
                                    SellerRegisterRequestStatus sellerRegisterRequestStatus);
}

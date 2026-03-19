package org.hotiver.repo;

import org.hotiver.common.Enum.SellerRegisterRequestStatus;
import org.hotiver.domain.Entity.SellerRegister;
import org.hotiver.dto.admin.SellerRegisterResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

public interface SellerRegisterRepo extends JpaRepository<SellerRegister, Long> {
    boolean existsByUserId(Long userId);

    @Query("""
            SELECT
                s.id as id,
                s.requestedNickname as requestedNickname,
                s.displayName as displayName,
                s.profileDescription as profileDescription
            FROM SellerRegister s WHERE s.status = :status""")
    List<SellerRegisterResponse> findByStatus(@Param("status")SellerRegisterRequestStatus status);

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

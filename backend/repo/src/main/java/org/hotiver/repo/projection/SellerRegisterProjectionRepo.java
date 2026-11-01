package org.hotiver.repo.projection;

import org.hotiver.common.Enum.SellerRegisterRequestStatus;
import org.hotiver.domain.Entity.SellerRegister;
import org.hotiver.dto.admin.SellerRegisterResponse;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SellerRegisterProjectionRepo extends Repository<SellerRegister, Long> {

    @Query("""
            SELECT
                s.id as id,
                s.requestedNickname as requestedNickname,
                s.displayName as displayName,
                s.profileDescription as profileDescription
            FROM SellerRegister s WHERE s.status = :status""")
    List<SellerRegisterResponse> findByStatus(@Param("status") SellerRegisterRequestStatus status);
}

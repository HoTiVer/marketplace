package org.hotiver.repo;

import org.hotiver.domain.Entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepo extends JpaRepository<Role, Long> {

    @Query("SELECT r FROM Role r WHERE r.id = :id")
    Optional<Role> findById(Long id);

}

package org.hotiver.repo;

import org.hotiver.domain.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepo extends JpaRepository<User, Long> {

    @Query("select u from User u where u.email = :username")
    Optional<User> findByEmail(String username);

    boolean existsUserByEmail(String email);
}

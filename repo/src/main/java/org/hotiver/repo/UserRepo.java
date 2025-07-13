package org.hotiver.repo;

import org.hotiver.domain.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepo extends JpaRepository<User, Long> {

    User findByEmail(String username);

    boolean existsUserByEmail(String email);
}

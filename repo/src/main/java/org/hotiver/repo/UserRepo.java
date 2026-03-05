package org.hotiver.repo;

import org.hotiver.domain.Entity.User;
import org.hotiver.dto.product.ListProductDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepo extends JpaRepository<User, Long> {

    @Query("SELECT u FROM User u WHERE u.email = :username")
    Optional<User> findByEmail(String username);

    boolean existsUserByEmail(String email);

    @Query("""
            SELECT new org.hotiver.dto.product.ListProductDto(
            p.id,
            p.name,
            p.price,
            null
        )
        FROM User u
        JOIN u.wishlist p
        WHERE u.email = :email
    """)
    List<ListProductDto> findUserProductWishList(String email);
}

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

    @Query(value = """
        SELECT
            p.id as productId,
            p.name as productName,
            p.price as price,
            pi.url as mainImageUrl
        FROM public."user" u
        JOIN user_wishes uw ON uw.user_id = u.id
        JOIN product p ON p.id = uw.product_id
        LEFT JOIN product_image pi ON pi.product_id = p.id AND pi.is_main = true
        WHERE u.email = :email
    """, nativeQuery = true)
    List<ListProductDto> findUserProductWishList(String email);
}

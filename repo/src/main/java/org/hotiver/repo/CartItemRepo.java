package org.hotiver.repo;

import org.hotiver.domain.Entity.CartItem;
import org.hotiver.domain.keys.CartItemId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepo extends JpaRepository<CartItem, CartItemId> {

}

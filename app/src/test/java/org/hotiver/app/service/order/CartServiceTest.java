package org.hotiver.app.service.order;

import jakarta.persistence.EntityNotFoundException;
import org.hotiver.common.Enum.RoleType;
import org.hotiver.domain.Entity.CartItem;
import org.hotiver.domain.Entity.Product;
import org.hotiver.domain.Entity.Role;
import org.hotiver.domain.Entity.User;
import org.hotiver.domain.keys.CartItemId;
import org.hotiver.domain.security.SecurityUser;
import org.hotiver.dto.cart.CartItemDto;
import org.hotiver.repo.core.CartItemRepo;
import org.hotiver.repo.core.ProductRepo;
import org.hotiver.repo.core.UserRepo;
import org.hotiver.repo.projection.CartItemProjectionRepo;
import org.hotiver.service.common.CurrentUserService;
import org.hotiver.service.order.CartService;
import org.hotiver.service.product.ProductImageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class CartServiceTest {

    @Mock
    private UserRepo userRepo;

    @Mock
    private ProductRepo productRepo;

    @Mock
    private CartItemRepo cartItemRepo;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private ProductImageService productImageService;

    @Mock
    private CartItemProjectionRepo cartItemProjectionRepo;

    @InjectMocks
    private CartService cartService;

    private User user;
    private Product product;

    @BeforeEach
    public void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("test@test.com");
        user.setRoles(List.of(new Role(1L, RoleType.USER)));

        product = new Product();
        product.setId(1L);
        product.setName("test product");
    }

    @Nested
    class GetUserCart {

        private List<CartItemDto> userCart;

        @BeforeEach
        public void setUp() {
            userCart = new ArrayList<>();

            userCart.add(
                    new CartItemDto(
                           1L,
                           "test",
                            BigDecimal.valueOf(52),
                            1,
                            "/url"
                    )
            );
        }

        @Test
        void shouldReturnUserCart() {
            when(currentUserService.getUserPrincipal())
                    .thenReturn(new SecurityUser(user));

            when(cartItemProjectionRepo.findByUserId(user.getId()))
                    .thenReturn(userCart);

            List<CartItemDto> result = cartService.getUserCart();

            assertEquals(userCart.size(), result.size());
            assertEquals(userCart.getFirst().getProductId(),
                    result.getFirst().getProductId());
        }

        @Test
        void shouldReturnEmptyUserCart() {
            when(currentUserService.getUserPrincipal())
                    .thenReturn(new SecurityUser(user));

            when(cartItemProjectionRepo.findByUserId(user.getId()))
                    .thenReturn(Collections.emptyList());

            List<CartItemDto> result = cartService.getUserCart();

            assertEquals(0, result.size());
        }

    }

    @Nested
    class AddProductToCart {

        @Test
        void shouldAddProductToCart() {
            when(currentUserService.getCurrentUser())
                    .thenReturn(user);

            when(productRepo.findById(product.getId()))
                    .thenReturn(Optional.of(product));

            CartItemId cartItemId = new CartItemId(user.getId(), product.getId());
            when(cartItemRepo.findById(cartItemId))
                    .thenReturn(Optional.empty());

            cartService.addProductToCart(product.getId());

            verify(userRepo).save(user);
        }

        @Test
        void shouldThrowException_whenProductNotFound() {
            when(currentUserService.getCurrentUser())
                    .thenReturn(user);

            when(productRepo.findById(product.getId()))
                    .thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    ()-> cartService.addProductToCart(product.getId())
            );


            verify(userRepo, never()).save(user);
        }

    }

    @Nested
    class DeleteProductFromCart {

        @Test
        void shouldDeleteProductFromCart() {
            SecurityUser securityUser = new SecurityUser(user);
            when(currentUserService.getUserPrincipal())
                    .thenReturn(securityUser);

            when(productRepo.findById(product.getId()))
                    .thenReturn(Optional.of(product));

            CartItemId cartItemId = new CartItemId(user.getId(), product.getId());

            cartService.deleteProductFromCart(product.getId());

            verify(cartItemRepo).deleteById(cartItemId);
        }

        @Test
        void shouldThrowExceptionWhenProductNotFound() {
            when(currentUserService.getUserPrincipal())
                    .thenReturn(new SecurityUser(user));

            when(productRepo.findById(product.getId()))
                    .thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    ()-> cartService.deleteProductFromCart(product.getId())
            );
        }
    }

    @Nested
    class UpdateProductCount {

        private SecurityUser securityUser;

        @BeforeEach
        public void setUp() {
            securityUser = new SecurityUser(user);
        }

        @Test
        void shouldUpdateProductCount() {
            CartItem cartItem = new CartItem();
            cartItem.setId(new CartItemId(user.getId(), product.getId()));

            when(currentUserService.getUserPrincipal())
                    .thenReturn(securityUser);

            when(productRepo.findById(product.getId()))
                    .thenReturn(Optional.of(product));

            when(cartItemRepo.findById(cartItem.getId()))
                    .thenReturn(Optional.of(cartItem));

            cartService.updateProductCount(product.getId(), 1);

            verify(cartItemRepo).save(cartItem);
        }

        @Test
        void shouldThrowExceptionWhenProductNotFound() {
            CartItem cartItem = new CartItem();

            when(currentUserService.getUserPrincipal())
                    .thenReturn(securityUser);

            assertThrows(EntityNotFoundException.class,
                    () -> cartService.updateProductCount(product.getId(), 1)
            );
        }

        @Test
        void shouldThrowExceptionWhenProductNotFoundInCart() {
            when(currentUserService.getUserPrincipal())
                    .thenReturn(securityUser);

            when(productRepo.findById(product.getId()))
                    .thenReturn(Optional.of(product));

            when(cartItemRepo.findById(new CartItemId(user.getId(), product.getId())))
                    .thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> cartService.updateProductCount(product.getId(), 1)
            );
        }

    }
}

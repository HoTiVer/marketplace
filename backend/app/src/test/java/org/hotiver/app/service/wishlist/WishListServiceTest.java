package org.hotiver.app.service.wishlist;

import jakarta.persistence.EntityNotFoundException;
import org.hotiver.common.Enum.RoleType;
import org.hotiver.domain.Entity.Product;
import org.hotiver.domain.Entity.Role;
import org.hotiver.domain.Entity.Seller;
import org.hotiver.domain.Entity.User;
import org.hotiver.domain.security.SecurityUser;
import org.hotiver.dto.product.ListProductDto;
import org.hotiver.repo.core.ProductRepo;
import org.hotiver.repo.core.UserRepo;
import org.hotiver.repo.projection.ProductProjectionRepo;
import org.hotiver.service.common.CurrentUserService;
import org.hotiver.service.product.ProductImageService;
import org.hotiver.service.wishlist.WishListService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class WishListServiceTest {

    @Mock
    private UserRepo userRepo;

    @Mock
    private ProductRepo productRepo;

    @Mock
    private ProductImageService productImageService;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private ProductProjectionRepo productProjectionRepo;

    @InjectMocks
    private WishListService wishListService;

    private User currentUser;

    @BeforeEach
    public void setUp() {
        currentUser = new User();
        currentUser.setId(1L);
        currentUser.setWishlist(new HashSet<>());
        currentUser.setRoles(List.of(new Role(1L, RoleType.USER)));

    }

    @Nested
    class GetUserWishList {

        private List<ListProductDto> userWishList;

        @BeforeEach
        public void setUp() {
            userWishList = new ArrayList<>();

            userWishList.add(
                    new ListProductDto(
                            1L,
                            "test1",
                            BigDecimal.valueOf(52),
                            "/url/1",
                            0
                    )
            );

            userWishList.add(
                    new ListProductDto(
                            2L,
                            "test2",
                            BigDecimal.valueOf(52),
                            "/url/2",
                            0
                    )
            );
        }

        @Test
        void shouldReturnWishList() {
            SecurityUser user = new SecurityUser(currentUser);
            when(currentUserService.getUserPrincipal()).thenReturn(user);

            when(productProjectionRepo.findUserProductWishListByUserId(user.getId()))
                    .thenReturn(userWishList);

            List<ListProductDto> result = wishListService.getUserWishList();

            assertEquals(userWishList.size(),result.size());
            assertEquals(userWishList.getFirst().getProductName(),
                    result.getFirst().getProductName());

            verify(productImageService).addHostToImage(userWishList);
        }

        @Test
        void shouldReturnEmptyWishList() {
            SecurityUser user = new SecurityUser(currentUser);
            when(currentUserService.getUserPrincipal()).thenReturn(user);

            when(productProjectionRepo.findUserProductWishListByUserId(user.getId()))
                    .thenReturn(Collections.emptyList());

            List<ListProductDto> result = wishListService.getUserWishList();

            assertEquals(0, result.size());

            verify(productImageService).addHostToImage(Collections.emptyList());
        }

    }

    @Nested
    class RemoveProductFromWishList {

        private Long productId;
        private Product product;

        @BeforeEach
        public void setUp() {
            productId = 1L;
            product = new Product();
            product.setId(1L);
            product.setName("test product");

            currentUser.setWishlist(new HashSet<>(Set.of(product)));
        }

        @Test
        void shouldRemoveProductFromWishList() {
            when(currentUserService.getCurrentUser()).thenReturn(currentUser);

            when(productRepo.findById(productId)).thenReturn(Optional.of(product));

            wishListService.removeProductFromWishList(productId);

            assertEquals(Collections.EMPTY_SET, currentUser.getWishlist());
            verify(userRepo).save(currentUser);
        }

        @Test
        void shouldThrowException_whenRemovedProductNotExists() {
            when(currentUserService.getCurrentUser()).thenReturn(currentUser);

            when(productRepo.findById(productId)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    ()->wishListService.removeProductFromWishList(productId)
            );

            verify(userRepo, never()).save(currentUser);
        }
    }

    @Nested
    class AddProductToWishList {

        private Long productId;
        private Product product;

        @BeforeEach
        public void setUp() {
            productId = 1L;
            product = new Product();
            product.setId(1L);
            product.setName("test product");
        }

        @Test
        void shouldAddProductToWishList() {
            Seller seller = new Seller();
            seller.setId(999L);

            product.setSeller(seller);

            when(currentUserService.getCurrentUser()).thenReturn(currentUser);

            when(productRepo.findById(productId)).thenReturn(Optional.of(product));

            wishListService.addProductToWishList(productId);

            verify(userRepo).save(currentUser);
        }

        @Test
        void shouldThrowException_whenAddedProductNotExists() {
            when(currentUserService.getCurrentUser()).thenReturn(currentUser);

            when(productRepo.findById(productId)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    ()->wishListService.addProductToWishList(productId)
            );

            verify(userRepo, never()).save(currentUser);
        }

        @Test
        void shouldDoNothing_whenProductAlreadyInWishList() {
            currentUser.setWishlist(new HashSet<>(Set.of(product)));

            when(currentUserService.getCurrentUser()).thenReturn(currentUser);

            when(productRepo.findById(productId)).thenReturn(Optional.of(product));

            wishListService.addProductToWishList(productId);

            verify(userRepo,never()).save(currentUser);
        }

        @Test
        void shouldDoNothing_whenProductBelongsToUser() {
            Seller seller = new Seller();
            seller.setId(currentUser.getId());

            product.setSeller(seller);

            when(currentUserService.getCurrentUser()).thenReturn(currentUser);

            when(productRepo.findById(productId)).thenReturn(Optional.of(product));

            wishListService.addProductToWishList(productId);

            verify(userRepo,never()).save(currentUser);
        }
    }
}

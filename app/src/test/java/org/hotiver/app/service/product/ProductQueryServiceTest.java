package org.hotiver.app.service.product;

import jakarta.persistence.EntityNotFoundException;
import org.hotiver.common.Enum.RoleType;
import org.hotiver.common.Exception.auth.ForbiddenOperationException;
import org.hotiver.common.Exception.seller.SellerNotFoundException;
import org.hotiver.domain.Entity.*;
import org.hotiver.domain.security.SecurityUser;
import org.hotiver.dto.product.CurrentSellerProductDto;
import org.hotiver.dto.product.ProductGetDto;
import org.hotiver.dto.product.SellerInventoryProductDto;
import org.hotiver.repo.core.ProductRepo;
import org.hotiver.repo.core.SellerRepo;
import org.hotiver.repo.projection.ProductProjectionRepo;
import org.hotiver.service.common.CurrentUserService;
import org.hotiver.service.mapper.ProductMapper;
import org.hotiver.service.product.ProductImageService;
import org.hotiver.service.product.ProductQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductQueryServiceTest {

    @Mock
    private ProductRepo productRepo;

    @Mock
    private ProductProjectionRepo productProjectionRepo;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private ProductImageService productImageService;

    @Mock
    private SellerRepo sellerRepo;

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private ProductQueryService productQueryService;

    private Product product;

    private Seller seller;

    private ProductGetDto productGetDto;

    private SecurityUser user;

    @BeforeEach
    public void setUp()  {
        seller = new Seller();
        seller.setId(1L);

        user = new SecurityUser(createUser(1L));

        product = createProduct();

        productGetDto = new ProductGetDto(
                1L,
                "test",
                BigDecimal.valueOf(1.5),
                "test",
                "test",
                new HashMap<String, Object>(),
                "test",
                "test",
                null
        );
    }

    private User createUser(Long userId) {
        User user = new User();
        user.setId(userId);
        user.setRoles(new ArrayList<>(List.of(
                new Role(1L, RoleType.USER)
        )));

        return user;
    }

    private Product createProduct() {
        return new Product(
                1L,
                "test",
                BigDecimal.valueOf(1.5),
                "test",
                new Category(1L, "test"),
                new HashMap<String, Object>(),
                seller,
                1,
                1,
                Date.valueOf(LocalDate.now()),
                BigDecimal.valueOf(3),
                true,
                new ArrayList<ProductImage>()
        );
    }

    @Nested
    class GetProductById {

        @Test
        public void shouldReturnProduct_whenProductExists() {
            Long productId = 1L;

            when(productRepo.findById(productId))
                    .thenReturn(Optional.of(product));

            when(productMapper.entityToProductGetDto(product))
                    .thenReturn(productGetDto);

            ProductGetDto productGetDto = productQueryService.getProductById(productId);

            assertEquals(productId, productGetDto.getId());
            assertEquals("test", productGetDto.getName());
            verify(productRepo, times(1)).findById(productId);
            verify(productMapper, times(1)).entityToProductGetDto(product);
        }

        @Test
        public void shouldThrowsException_whenProductDoesNotExist() {
            Long productId = 1L;

            when(productRepo.findById(productId))
                    .thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> productQueryService.getProductById(productId));

            verify(productRepo, times(1)).findById(productId);
            verify(productMapper, never()).entityToProductGetDto(product);
        }
    }

    @Nested
    public class GetCurrentSellerProducts {
        @Test
        void shouldReturnListOfProducts_whenSellerExists() {
            Long sellerId = 1L;

            List<SellerInventoryProductDto> list = new ArrayList<>();

            when(sellerRepo.findByEmail("test")).thenReturn(Optional.of(seller));

            when(productProjectionRepo.getCurrentSellerProducts(sellerId))
                    .thenReturn(list);

            productQueryService.getCurrentSellerProducts("test");

            verify(sellerRepo, times(1)).findByEmail("test");
            verify(productProjectionRepo, times(1))
                    .getCurrentSellerProducts(sellerId);
        }

        @Test
        void shouldThrowsException_whenSellerNotExists() {
            Long sellerId = 1L;

            when(sellerRepo.findByEmail("test")).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> productQueryService.getCurrentSellerProducts("test"));

            verify(sellerRepo, times(1)).findByEmail("test");
            verify(productProjectionRepo, never()).getCurrentSellerProducts(sellerId);
        }
    }

    @Nested
    class GetCurrentSellerProductById {

        @Test
        void shouldReturnProductForSeller_whenProductExistsAndSellerOwnIt() {
            Long productId = 1L;

            CurrentSellerProductDto dto = new CurrentSellerProductDto();
            dto.setId(1L);
            dto.setName("test");

            when(productRepo.findById(productId)).thenReturn(Optional.of(product));
            when(productMapper.entityToCurrentSellerProductDto(product))
                    .thenReturn(dto);

            when(currentUserService.getUserPrincipal())
                    .thenReturn(user);

            CurrentSellerProductDto currentSellerProductDto =
                    productQueryService.getCurrentSellerProductById(productId);

            assertEquals(productId, currentSellerProductDto.getId());
            assertEquals("test", currentSellerProductDto.getName());

            verify(productRepo, times(1)).findById(productId);
            verify(productMapper, times(1))
                    .entityToCurrentSellerProductDto(product);
        }

        @Test
        void shouldThrowsException_whenProductDoesNotExist() {
            Long productId = 1L;
            when(productRepo.findById(productId)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> productQueryService.getCurrentSellerProductById(productId));

            verify(productRepo, times(1)).findById(productId);
            verify(productMapper, never()).entityToCurrentSellerProductDto(product);
        }

        @Test
        void shouldThrowsException_whenSellerDoesNotOwnIt() {
            Long productId = 1L;

            User user = new User();
            user.setId(52L);
            user.setRoles(List.of(new Role(1L, RoleType.USER)));
            SecurityUser securityUser = new SecurityUser(user);

            when(productRepo.findById(productId)).thenReturn(Optional.of(product));
            when(currentUserService.getUserPrincipal())
                    .thenReturn(securityUser);

            assertThrows(ForbiddenOperationException.class,
                    () -> productQueryService.getCurrentSellerProductById(productId));

            verify(productRepo, times(1)).findById(productId);
            verify(productMapper, never()).entityToCurrentSellerProductDto(product);
        }
    }

    @Nested
    public class GetSellerVisibleProducts {

        @Test
        void shouldReturnSellerVisibleProduct_whenSellerExists() {
            when(sellerRepo.findByNickname("test")).thenReturn(Optional.of(seller));

            productQueryService.getSellerVisibleProducts("test");

            verify(sellerRepo, times(1)).findByNickname("test");
            verify(productProjectionRepo, times(1))
                    .findAllVisibleBySellerId(seller.getId());
        }

        @Test
        void shouldThrowsException_whenSellerNotExists() {
            when(sellerRepo.findByNickname("test")).thenReturn(Optional.empty());

            assertThrows(SellerNotFoundException.class,
                    () -> productQueryService.getSellerVisibleProducts("test"));

            verify(sellerRepo, times(1)).findByNickname("test");
            verify(productProjectionRepo, never()).findAllVisibleBySellerId(seller.getId());
        }
    }
}

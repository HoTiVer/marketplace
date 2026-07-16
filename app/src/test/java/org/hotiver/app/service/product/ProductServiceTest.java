package org.hotiver.app.service.product;

import jakarta.persistence.EntityNotFoundException;
import org.hotiver.common.Enum.RoleType;
import org.hotiver.domain.Entity.*;
import org.hotiver.domain.security.SecurityUser;
import org.hotiver.dto.product.ProductAddDto;
import org.hotiver.repo.core.CategoryRepo;
import org.hotiver.repo.core.ProductRepo;
import org.hotiver.repo.core.SellerRepo;
import org.hotiver.service.chat.ChatService;
import org.hotiver.service.common.CurrentUserService;
import org.hotiver.service.mapper.ProductMapper;
import org.hotiver.service.product.ProductImageService;
import org.hotiver.service.product.ProductPriceHistoryService;
import org.hotiver.service.product.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    @Mock
    private ChatService chatService;

    @Mock
    private ProductImageService productImageService;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private SellerRepo sellerRepo;

    @Mock
    private ProductRepo productRepo;

    @Mock
    private CategoryRepo categoryRepo;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private ProductPriceHistoryService productPriceHistoryService;

    @InjectMocks
    private ProductService productService;

    private User user;
    private Product product;

    @BeforeEach
    public void setup() {
        user = new User();
        user.setId(1L);
        user.setEmail("test");
        user.setRoles(new ArrayList<>(List.of(
                new Role(1L, RoleType.USER)
        )));

        product = createProduct();
    }

    private Product createProduct() {
        Map<String, Object> characteristics = new HashMap<>();
        characteristics.put("color", "black");
        characteristics.put("weight", 1.5);

        Seller seller = new Seller();
        seller.setId(1L);

        return Product.builder()
                .id(1L)
                .name("test product")
                .price(BigDecimal.valueOf(99.99))
                .description("test description")
                .category(new Category(1L, "electronics"))
                .characteristic(characteristics)
                .seller(seller)
                .stockQuantity(10)
                .salesCount(5)
                .publishingDate(Date.valueOf(LocalDate.now()))
                .rating(BigDecimal.valueOf(4.5))
                .isVisible(true)
                .images(new ArrayList<>())
                .build();
    }

    @Nested
    class AddProduct {

        private ProductAddDto productAddDto;
        private Seller seller;
        private Category category;

        @BeforeEach
        void setUp() {
            Map<String, Object> characteristics = new HashMap<>();
            characteristics.put("color", "black");
            characteristics.put("weight", 1.2);

            productAddDto = new ProductAddDto(
                    "test product",
                    BigDecimal.valueOf(99.99),
                    "test description",
                    "electronics",
                    characteristics,
                    10
            );

            seller = new Seller();
            seller.setId(1L);

            category = new Category();
            category.setId(1L);
            category.setName("test category");
        }

        @Test
        void shouldAddProduct() {
            when(currentUserService.getUserPrincipal()).thenReturn(new SecurityUser(user));
            when(sellerRepo.findByEmail(user.getEmail())).thenReturn(Optional.of(seller));
            when(categoryRepo.findByName(productAddDto.getCategoryName()))
                    .thenReturn(Optional.of(category));

            when(productMapper.productAddDtoToEntity(productAddDto, category, seller))
                    .thenReturn(product);

            when(productRepo.save(product)).thenReturn(product);

            productService.addProduct(productAddDto, null);

            verify(productRepo, times(2)).save(product);
            verify(productMapper).productAddDtoToEntity(productAddDto, category, seller);
            verify(productImageService).addImageToProduct(product, null);
        }

        @Test
        void shouldThrowsException_whenCategoryNotFound() {
            when(currentUserService.getUserPrincipal()).thenReturn(new SecurityUser(user));
            when(sellerRepo.findByEmail(user.getEmail())).thenReturn(Optional.of(seller));
            when(categoryRepo.findByName(productAddDto.getCategoryName()))
                    .thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    ()-> productService.addProduct(productAddDto, null));

            verify(productRepo, never()).save(product);
            verify(productMapper, never()).productAddDtoToEntity(productAddDto, category, seller);
            verify(productImageService, never()).addImageToProduct(product, null);
        }

    }

    @Nested
    class DeleteProductById {

        @Test
        void shouldDeleteProductById() {
            when(currentUserService.getUserPrincipal()).thenReturn(new SecurityUser(user));
            when(productRepo.findById(product.getId())).thenReturn(Optional.of(product));

            productService.deleteProductById(product.getId());

            verify(productRepo).deleteById(product.getId());
            verify(productImageService).deleteAllImages(product.getId());
        }

        @Test
        void shouldThrowsException_whenProductNotFound() {
            when(currentUserService.getUserPrincipal()).thenReturn(new SecurityUser(user));
            when(productRepo.findById(product.getId())).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class, () -> {
                productService.deleteProductById(product.getId());
            });

            verify(productRepo, never()).deleteById(product.getId());
            verify(productImageService, never()).deleteAllImages(product.getId());
        }
    }

    @Nested
    class UpdateProductById {

        private ProductAddDto productAddDto;
        private Seller seller;
        private Category category;

        @BeforeEach
        void setUp() {
            Map<String, Object> characteristics = new HashMap<>();
            characteristics.put("color", "black");
            characteristics.put("weight", 1.2);

            productAddDto = new ProductAddDto(
                    "test product",
                    BigDecimal.valueOf(99.99),
                    "test description",
                    "test category",
                    characteristics,
                    10
            );

            seller = new Seller();
            seller.setId(1L);

            category = new Category();
            category.setId(1L);
            category.setName("test category");
        }

        @Test
        void shouldUpdateProductById() {
            when(currentUserService.getUserPrincipal()).thenReturn(new SecurityUser(user));
            when(productRepo.findById(product.getId())).thenReturn(Optional.of(product));
            when(categoryRepo.findByName(productAddDto.getCategoryName()))
                    .thenReturn(Optional.of(category));

            productService.updateProductById(product.getId(), productAddDto, null);

            verify(productImageService).addImageToProduct(product, null);
            verify(productRepo).save(product);
        }

        @Test
        void shouldThrowsException_whenProductNotFound() {
            when(currentUserService.getUserPrincipal()).thenReturn(new SecurityUser(user));
            when(productRepo.findById(product.getId())).thenReturn(Optional.empty());


            assertThrows(EntityNotFoundException.class, () -> {
                productService.updateProductById(product.getId(), productAddDto, null);
            });

            verify(productImageService, never()).addImageToProduct(product, null);
            verify(productRepo, never()).save(product);
        }

        @Test
        void shouldThrowsException_whenCategoryNotFound() {
            when(currentUserService.getUserPrincipal()).thenReturn(new SecurityUser(user));
            when(productRepo.findById(product.getId())).thenReturn(Optional.of(product));
            when(categoryRepo.findByName(productAddDto.getCategoryName()))
                    .thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class, () -> {
                productService.updateProductById(product.getId(), productAddDto, null);
            });

            verify(productImageService, never()).addImageToProduct(product, null);
            verify(productRepo, never()).save(product);
        }

        @Test
        void shouldThrowsException_whenSellerDoNotOwnProduct() {
            user.setId(52L);
            when(currentUserService.getUserPrincipal()).thenReturn(new SecurityUser(user));
            when(productRepo.findById(product.getId())).thenReturn(Optional.of(product));

            assertThrows(AccessDeniedException.class, () -> {
                productService.updateProductById(product.getId(), productAddDto, null);
            });

            verify(productImageService, never()).addImageToProduct(product, null);
            verify(productRepo, never()).save(product);
        }
    }

}

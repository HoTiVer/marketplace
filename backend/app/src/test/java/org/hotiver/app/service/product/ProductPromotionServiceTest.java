package org.hotiver.app.service.product;

import jakarta.persistence.EntityNotFoundException;
import org.hotiver.common.Exception.auth.ForbiddenOperationException;
import org.hotiver.domain.Entity.Product;
import org.hotiver.domain.Entity.ProductPromotion;
import org.hotiver.domain.Entity.Seller;
import org.hotiver.domain.Entity.User;
import org.hotiver.dto.product.ProductPromotionDto;
import org.hotiver.dto.product.ProductPromotionRequest;
import org.hotiver.dto.product.SellerProductPromotionDto;
import org.hotiver.repo.core.ProductPromotionRepo;
import org.hotiver.repo.core.ProductRepo;
import org.hotiver.service.common.CurrentUserService;
import org.hotiver.service.mapper.ProductPromotionMapper;
import org.hotiver.service.product.ProductPromotionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Optional;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductPromotionServiceTest {

    @Mock
    private ProductPromotionRepo productPromotionRepo;

    @Mock
    private ProductRepo productRepo;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private ProductPromotionMapper productPromotionMapper;

    @InjectMocks
    private ProductPromotionService productPromotionService;

    private ProductPromotion productPromotion;
    private Product product;
    private User currentUser;

    @BeforeEach
    public void set_up() {
        product = new Product();
        product.setId(1L);
        product.setName("Test Product");
        product.setPrice(BigDecimal.valueOf(100));

        productPromotion = new ProductPromotion();
        productPromotion.setId(1L);
        productPromotion.setProduct(product);
        productPromotion.setDiscountPercent(10);
        productPromotion.setEndTime(Instant.now().plus(5, ChronoUnit.DAYS));
        productPromotion.setStartTime(Instant.now().plus(1, ChronoUnit.DAYS));
        productPromotion.setShowEndDate(true);

        currentUser = new User();
        currentUser.setId(1L);

        Seller seller = new Seller();
        seller.setId(currentUser.getId());
        seller.setUser(currentUser);

        product.setSeller(seller);
    }

    @Nested
    class GetProductPromotion {

        @Test
        public void shouldReturnProductPromotionWithPromotionAndEndDate() {
            when(productPromotionRepo.findActiveProductPromotion(any(), any()))
                    .thenReturn(Optional.of(productPromotion));

            ProductPromotionDto result =
                    productPromotionService.getProductPromotion(product);

            assertTrue(result.isPromotion());
            assertEquals(result.defaultPrice(), product.getPrice());
            assertNotEquals(result.promotionPrice(), product.getPrice());
            assertNotNull(result.promotionEndTime());

            verify(productPromotionRepo).findActiveProductPromotion(any(), any());
        }

        @Test
        public void shouldReturnProductPromotionWithPromotionWithoutEndDate() {
            productPromotion.setShowEndDate(false);

            when(productPromotionRepo.findActiveProductPromotion(any(), any()))
                    .thenReturn(Optional.of(productPromotion));

            ProductPromotionDto result =
                    productPromotionService.getProductPromotion(product);

            assertTrue(result.isPromotion());
            assertEquals(result.defaultPrice(), product.getPrice());
            assertNotEquals(result.promotionPrice(), product.getPrice());
            assertNull(result.promotionEndTime());

            verify(productPromotionRepo).findActiveProductPromotion(any(), any());
        }

        @Test
        public void shouldReturnProductPromotionWithoutPromotion() {
            when(productPromotionRepo.findActiveProductPromotion(any(), any()))
                    .thenReturn(Optional.empty());

            ProductPromotionDto result =
                    productPromotionService.getProductPromotion(product);

            assertFalse(result.isPromotion());
            assertNull(result.promotionEndTime());
            assertEquals(result.defaultPrice(), product.getPrice());
            assertEquals(result.promotionPrice(), result.defaultPrice());
        }
    }

    @Nested
    class GetProductPromotions {

        @Test
        public void shouldReturnNotEmptyListOfProductPromotions() {
            SellerProductPromotionDto dto =
                    new SellerProductPromotionDto(
                            1L,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null
                    );

            when(productRepo.findById(product.getId())).thenReturn(Optional.of(product));
            when(currentUserService.getCurrentUser()).thenReturn(currentUser);

            when(productPromotionRepo.findProductPromotions(product.getId()))
                    .thenReturn(List.of(productPromotion));

            when(productPromotionMapper.entityToSellerProductPromotionDto(productPromotion))
                    .thenReturn(dto);

            List<SellerProductPromotionDto> result =
                    productPromotionService.getProductPromotions(product.getId());

            assertEquals(1, result.size());
            assertNotNull(result.getFirst());
        }

        @Test
        public void shouldReturnEmptyListOfProductPromotions() {
            when(productRepo.findById(product.getId())).thenReturn(Optional.of(product));
            when(currentUserService.getCurrentUser()).thenReturn(currentUser);

            when(productPromotionRepo.findProductPromotions(product.getId()))
                    .thenReturn(Collections.emptyList());

            List<SellerProductPromotionDto> result =
                    productPromotionService.getProductPromotions(product.getId());

            assertEquals(0, result.size());
        }

        @Test
        public void shouldThrowNotFoundException_whenProductNotExists() {
            when(productRepo.findById(product.getId())).thenReturn(Optional.empty());

            assertThrows(ForbiddenOperationException.class,
                    () -> productPromotionService.getProductPromotions(product.getId()));
        }
    }

    @Nested
    class CreatePercentPromotion {

        private ProductPromotionRequest productPromotionRequest;

        @BeforeEach
        public void set_up() {
            productPromotionRequest = new ProductPromotionRequest(
                    15,
                    "sale",
                    OffsetDateTime.now().plusDays(1),
                    OffsetDateTime.now().plusDays(5),
                    true,
                    true
            );
        }

        @Test
        public void shouldCreatePercentPromotion() {
            when(productRepo.findById(product.getId()))
                    .thenReturn(Optional.of(product));

            when(currentUserService.getCurrentUser()).thenReturn(currentUser);

            when(productPromotionRepo.existsOverlappingPromotion(
                    product.getId(),
                    productPromotionRequest.startTime().toInstant(),
                    productPromotionRequest.endTime().toInstant()
            )).thenReturn(false);

            productPromotionService
                    .createPercentPromotion(product.getId(), productPromotionRequest);


            verify(productPromotionRepo).save(any(ProductPromotion.class));
        }

        @Test
        public void shouldThrowIllegalArgumentException_whenIncorrectTimePeriod() {
            productPromotionRequest = new ProductPromotionRequest(
                    15,
                    "sale",
                    OffsetDateTime.now().plusDays(6),
                    OffsetDateTime.now().plusDays(1),
                    true,
                    true
            );

            when(productRepo.findById(product.getId()))
                    .thenReturn(Optional.of(product));

            when(currentUserService.getCurrentUser()).thenReturn(currentUser);


            assertThrows(IllegalArgumentException.class, () ->
                        productPromotionService
                                .createPercentPromotion(product.getId(), productPromotionRequest)
            );

            verify(productPromotionRepo, never()).save(any(ProductPromotion.class));
        }

        @Test
        public void shouldThrowForbiddenOperationException_whenSellerNotOwnsProduct() {
            currentUser.setId(2L);

            when(productRepo.findById(product.getId()))
                    .thenReturn(Optional.of(product));

            when(currentUserService.getCurrentUser()).thenReturn(currentUser);


            assertThrows(ForbiddenOperationException.class, () ->
                productPromotionService
                        .createPercentPromotion(product.getId(), productPromotionRequest)
            );

            verify(productPromotionRepo, never()).save(any(ProductPromotion.class));
        }

        @Test
        public void shouldThrowNotFoundException_whenProductNotExists() {
            when(productRepo.findById(product.getId()))
                    .thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class, () ->
                productPromotionService
                        .createPercentPromotion(product.getId(), productPromotionRequest)
            );

            verify(productPromotionRepo, never()).save(any(ProductPromotion.class));
        }
    }

    @Nested
    class DeleteProductPromotion {

        @Test
        public void shouldDeleteProductPromotion() {
            when(productPromotionRepo.findById(anyLong()))
                    .thenReturn(Optional.of(productPromotion));

            when(currentUserService.getCurrentUser()).thenReturn(currentUser);

            productPromotionService.deleteProductPromotion(product.getId());

            verify(productPromotionRepo).deleteById(anyLong());
        }

        @Test
        public void shouldThrowForbiddenOperationException_whenSellerNotOwnsProduct() {
            currentUser.setId(2L);

            when(productPromotionRepo.findById(anyLong()))
                    .thenReturn(Optional.of(productPromotion));

            when(currentUserService.getCurrentUser()).thenReturn(currentUser);

            assertThrows(ForbiddenOperationException.class, () ->
                productPromotionService.deleteProductPromotion(product.getId())
            );

            verify(productPromotionRepo, never()).deleteById(anyLong());
        }

        @Test
        public void shouldThrowNotFoundException_whenProductPromotionNotExists() {
            when(productPromotionRepo.findById(anyLong())).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class, () ->
                productPromotionService.deleteProductPromotion(product.getId())
            );

            verify(productPromotionRepo, never()).deleteById(anyLong());
        }
    }

    @Nested
    class UpdateProductPromotion {

        private ProductPromotionRequest productPromotionRequest;
        private Long productPromotionId;

        @BeforeEach
        public void set_up() {
            productPromotionRequest = new ProductPromotionRequest(
                    15,
                    "sale",
                    OffsetDateTime.now().plusDays(1),
                    OffsetDateTime.now().plusDays(5),
                    true,
                    true
            );

            productPromotionId = productPromotion.getId();
        }

        @Test
        public void shouldUpdateProductPromotion() {
            when(productPromotionRepo.findById(productPromotionId))
                    .thenReturn(Optional.of(productPromotion));

            when(currentUserService.getCurrentUser()).thenReturn(currentUser);

            productPromotionService.updateProductPromotion(
                    productPromotionId,
                    productPromotionRequest
            );

            verify(productPromotionRepo).save(productPromotion);
        }

        @Test
        public void shouldThrowForbiddenOperationException_whenSellerNotOwnsProduct() {
            currentUser.setId(2L);

            when(productPromotionRepo.findById(productPromotionId))
                    .thenReturn(Optional.of(productPromotion));

            when(currentUserService.getCurrentUser()).thenReturn(currentUser);

            assertThrows(ForbiddenOperationException.class, () ->
                productPromotionService.updateProductPromotion(
                        productPromotionId,
                        productPromotionRequest
                )
            );

            verify(productPromotionRepo, never()).save(productPromotion);
        }

        @Test
        public void shouldThrowNotFoundException_whenProductPromotionNotExists() {
            when(productPromotionRepo.findById(productPromotionId))
                    .thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class, () ->
                productPromotionService.updateProductPromotion(
                        productPromotionId,
                        productPromotionRequest
                )
            );

            verify(productPromotionRepo, never()).save(productPromotion);
        }
    }
}

package org.hotiver.app.service.product;

import jakarta.persistence.EntityNotFoundException;
import org.hotiver.common.Enum.OrderStatus;
import org.hotiver.common.Exception.auth.ForbiddenOperationException;
import org.hotiver.domain.Entity.Product;
import org.hotiver.domain.Entity.Review;
import org.hotiver.domain.Entity.Seller;
import org.hotiver.domain.Entity.User;
import org.hotiver.dto.review.ProductReviewDto;
import org.hotiver.dto.review.ProductReviewPageDto;
import org.hotiver.dto.review.ReviewDto;
import org.hotiver.repo.OrderRepo;
import org.hotiver.repo.ProductRepo;
import org.hotiver.repo.ReviewRepo;
import org.hotiver.repo.SellerRepo;
import org.hotiver.service.common.CurrentUserService;
import org.hotiver.service.product.ProductReviewService;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductReviewServiceTest {

    @Mock
    private ReviewRepo reviewRepo;

    @Mock
    private ProductRepo productRepo;

    @Mock
    private OrderRepo orderRepo;

    @Mock
    private SellerRepo sellerRepo;

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private ProductReviewService productReviewService;

    private final Long productId = 1L;
    private Product product;

    @BeforeEach
    public void setUp() {
        product = new Product();
        product.setId(productId);
        product.setName("test");
    }

    @Nested
    class AddReviewToProduct {

        private ReviewDto reviewDto;
        private User user;

        @BeforeEach
        public void setUp() {
            reviewDto = new ReviewDto();
            reviewDto.setComment("test");
            reviewDto.setRating(5);

            user = new User();
            user.setId(1L);

            Seller seller = new Seller();
            seller.setId(user.getId());

            product.setSeller(seller);
        }

        @Test
        void shouldAddProductReview() {
            when(currentUserService.getCurrentUser()).thenReturn(user);
            when(productRepo.findById(productId)).thenReturn(Optional.of(product));

            when(orderRepo.isUserBoughtProduct(user.getId(), product.getId(),
                    OrderStatus.COMPLETED.toString())).thenReturn(true);

            when(reviewRepo.findReviewByUserIdAndProductId(user.getId(), productId))
                    .thenReturn(null);

            productReviewService.addReviewToProduct(reviewDto, productId);

            verify(productRepo, times(2)).findById(productId);
            verify(orderRepo).isUserBoughtProduct(user.getId(), product.getId(),
                    OrderStatus.COMPLETED.toString());
            verify(reviewRepo).findReviewByUserIdAndProductId(user.getId(), productId);
            verify(reviewRepo).save(any(Review.class));
        }

        @Test
        void shouldThrowException_whenProductNotFound() {
            when(currentUserService.getCurrentUser()).thenReturn(user);
            when(productRepo.findById(productId)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    ()-> productReviewService.addReviewToProduct(reviewDto, productId));

            verify(productRepo).findById(productId);
            verify(orderRepo, never()).isUserBoughtProduct(user.getId(), product.getId(),
                    OrderStatus.COMPLETED.toString());
            verify(reviewRepo, never()).findReviewByUserIdAndProductId(user.getId(), productId);
            verify(reviewRepo, never()).save(any(Review.class));
        }

        @Test
        void shouldThrowException_whenUserDidNotBuyProduct() {
            when(currentUserService.getCurrentUser()).thenReturn(user);
            when(productRepo.findById(productId)).thenReturn(Optional.of(product));

            when(orderRepo.isUserBoughtProduct(user.getId(), product.getId(),
                    OrderStatus.COMPLETED.toString())).thenReturn(false);

            assertThrows(ForbiddenOperationException.class,
                    ()-> productReviewService.addReviewToProduct(reviewDto, productId));

            verify(productRepo).findById(productId);
            verify(orderRepo).isUserBoughtProduct(user.getId(), product.getId(),
                    OrderStatus.COMPLETED.toString());
            verify(reviewRepo, never()).findReviewByUserIdAndProductId(user.getId(), productId);
            verify(reviewRepo, never()).save(any(Review.class));
        }

    }

    @Nested
    class GetProductReviews {

        private List<ProductReviewDto> productReviewDtoList;

        @BeforeEach
        public void setUp() {
            productReviewDtoList = new ArrayList<>();

            productReviewDtoList.add(
                    new ProductReviewDto(
                            1L,
                            1L,
                            "test",
                            "test",
                            5,
                            Date.valueOf(LocalDate.now())
                    )
            );

            productReviewDtoList.add(
                    new ProductReviewDto(
                            2L,
                            2L,
                            "test",
                            "test",
                            4,
                            Date.valueOf(LocalDate.now())
                    )
            );

        }

        @Test
        void shouldGetProductReviews() {
            when(productRepo.findById(productId)).thenReturn(Optional.of(product));

            when(reviewRepo.getProductReview(productId)).thenReturn(productReviewDtoList);

            when(reviewRepo.calculateProductRating(productId))
                    .thenReturn(Optional.of(BigDecimal.valueOf(4.5)));

            ProductReviewPageDto review = productReviewService
                    .getProductReviews(productId);

            assertEquals(2, review.getProductReviews().size());
            assertEquals(BigDecimal.valueOf(4.5), review.getRating());
            assertEquals(1L, review.getProductId());
            assertEquals("test", review.getProductName());

            verify(productRepo).findById(productId);
            verify(reviewRepo).getProductReview(productId);
            verify(reviewRepo).calculateProductRating(productId);
        }

        @Test
        void shouldGetEmptyProductReviews() {
            productReviewDtoList = new ArrayList<>();

            when(productRepo.findById(productId)).thenReturn(Optional.of(product));

            when(reviewRepo.getProductReview(productId)).thenReturn(productReviewDtoList);

            when(reviewRepo.calculateProductRating(productId))
                    .thenReturn(Optional.of(BigDecimal.valueOf(4.5)));

            ProductReviewPageDto review = productReviewService
                    .getProductReviews(productId);

            assertTrue(review.getProductReviews().isEmpty());
            assertEquals(BigDecimal.valueOf(4.5), review.getRating());
            assertEquals(1L, review.getProductId());
            assertEquals("test", review.getProductName());

            verify(productRepo).findById(productId);
            verify(reviewRepo).getProductReview(productId);
            verify(reviewRepo).calculateProductRating(productId);
        }

        @Test
        void shouldThrowException_whenProductNotExists() {
            when(productRepo.findById(productId)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> productReviewService.getProductReviews(productId)
            );

            verify(productRepo).findById(productId);
            verify(reviewRepo, never()).getProductReview(productId);
            verify(reviewRepo, never()).calculateProductRating(productId);
        }
    }
}

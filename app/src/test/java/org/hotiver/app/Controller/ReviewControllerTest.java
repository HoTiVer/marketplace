package org.hotiver.app.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.hotiver.api.Controller.ReviewController;
import org.hotiver.config.filter.JwtFilter;
import org.hotiver.dto.review.ProductReviewDto;
import org.hotiver.dto.review.ReviewDto;
import org.hotiver.dto.review.ProductReviewPageDto;
import org.hotiver.service.auth.JwtService;
import org.hotiver.service.product.ProductReviewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReviewController.class)
@AutoConfigureMockMvc(addFilters = false)
public class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProductReviewService productReviewService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private JwtFilter jwtFilter;

    @Nested
    class AddReviewToProduct {

        private ReviewDto reviewDto;

        @BeforeEach
        public void setup() {
            reviewDto = new ReviewDto();
            reviewDto.setRating(3);
            reviewDto.setComment("This is a comment");
        }

        @Test
        public void shouldReturnCreated_whenReviewSuccessfullyAdded() throws Exception {

            doNothing().when(productReviewService).addReviewToProduct(any(), anyLong());

            mockMvc.perform(post("/api/v1/product/{productId}/review", 1)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(reviewDto)))
                    .andExpect(status().isCreated());

            verify(productReviewService, times(1))
                    .addReviewToProduct(any(), anyLong());
        }

        @Test
        public void shouldReturnCreated_whenReviewCommentIsNull() throws Exception {
            reviewDto.setComment(null);

            doNothing().when(productReviewService).addReviewToProduct(any(), anyLong());

            mockMvc.perform(post("/api/v1/product/{productId}/review", 1)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(reviewDto)))
                    .andExpect(status().isCreated());

            verify(productReviewService, times(1))
                    .addReviewToProduct(any(), anyLong());
        }

        @Test
        public void shouldReturnUnprocessableEntity_whenReviewRatingIsNull() throws Exception {
            reviewDto.setRating(null);

            mockMvc.perform(post("/api/v1/product/{productId}/review", 1)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(reviewDto)))
                    .andExpect(status().isUnprocessableEntity());

        }

        @Test
        public void shouldReturnUnprocessableEntity_whenReviewRatingIsNegative() throws Exception {
            reviewDto.setRating(-1);

            mockMvc.perform(post("/api/v1/product/{productId}/review", 1)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(reviewDto)))
                    .andExpect(status().isUnprocessableEntity());
        }

        @Test
        public void shouldReturnUnprocessableEntity_whenReviewRatingIsOutOfBoundsHigher() throws Exception {
            reviewDto.setRating(6);

            mockMvc.perform(post("/api/v1/product/{productId}/review", 1)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(reviewDto)))
                    .andExpect(status().isUnprocessableEntity());
        }

        @Test
        public void shouldReturnUnprocessableEntity_whenCommentIsOutOfBounds() throws Exception {
            reviewDto.setComment("a".repeat(1000));

            mockMvc.perform(post("/api/v1/product/{productId}/review", 1)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(reviewDto)))
                    .andExpect(status().isUnprocessableEntity());
        }
    }

    @Nested
    class GetProductReviews {
        @Test
        public void shouldReturnAllProductReviews() throws Exception {
            ProductReviewPageDto review = new ProductReviewPageDto(1L, "test",
                    BigDecimal.valueOf(4),
                    List.of(new ProductReviewDto(1L, 1L,
                            "lol", "test",
                            5, Date.valueOf(LocalDate.now()))));

            when(productReviewService.getProductReviews(anyLong())).thenReturn(review);

            mockMvc.perform(get("/api/v1/product/{productId}/review", 1)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.productId").value(1))
                    .andExpect(jsonPath("$.productReviews.size()").value(1));
        }

        @Test
        public void shouldReturnEmptyProductReviews() throws Exception {
            ProductReviewPageDto review = new ProductReviewPageDto(1L, null,
                    null, null);

            when(productReviewService.getProductReviews(anyLong())).thenReturn(review);

            mockMvc.perform(get("/api/v1/product/{productId}/review", 1)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        @Test
        public void shouldReturnNotFound_whenProductIsNotExists() throws Exception {
            when(productReviewService.getProductReviews(anyLong()))
                    .thenThrow(new EntityNotFoundException("Not found"));

            mockMvc.perform(get("/api/v1/product/{productId}/review", 1)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Not found"));
        }
    }
}

package org.hotiver.app.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dockerjava.api.exception.NotFoundException;
import jakarta.persistence.EntityNotFoundException;
import org.hotiver.api.Controller.ReviewController;
import org.hotiver.config.filter.JwtFilter;
import org.hotiver.dto.ResponseDto;
import org.hotiver.dto.review.ProductReviewDto;
import org.hotiver.dto.review.ReviewDto;
import org.hotiver.dto.review.ReviewPageDto;
import org.hotiver.service.JwtService;
import org.hotiver.service.ReviewService;
import org.junit.jupiter.api.BeforeEach;
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
import java.util.ArrayList;
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
    private ReviewService reviewService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private JwtFilter jwtFilter;

    private ReviewDto reviewDto;

    @BeforeEach
    public void setup() {
        reviewDto = new ReviewDto();
        reviewDto.setRating(3);
        reviewDto.setComment("This is a comment");
    }

    @Test
    public void add_review_success() throws Exception {

        when(reviewService.addReviewToProduct(any(), anyLong()))
                .thenReturn(new ResponseDto("success"));

        mockMvc.perform(post("/api/v1/product/{productId}/review", 1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reviewDto)))
                .andExpect(status().isOk());
    }

    @Test
    public void add_review_success_comment_null() throws Exception {
        reviewDto.setComment(null);

        when(reviewService.addReviewToProduct(any(), anyLong()))
                .thenReturn(new ResponseDto("success"));

        mockMvc.perform(post("/api/v1/product/{productId}/review", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reviewDto)))
                .andExpect(status().isOk());
    }

    @Test
    public void add_review_null_rating() throws Exception {
        reviewDto.setRating(null);

        mockMvc.perform(post("/api/v1/product/{productId}/review", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reviewDto)))
                .andExpect(status().isUnprocessableEntity());

    }

    @Test
    public void add_review_negative_rating() throws Exception {
        reviewDto.setRating(-1);

        mockMvc.perform(post("/api/v1/product/{productId}/review", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reviewDto)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    public void add_review_rating_out_of_bounds_higher() throws Exception {
        reviewDto.setRating(6);

        mockMvc.perform(post("/api/v1/product/{productId}/review", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reviewDto)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    public void add_review_comment_out_of_bounds() throws Exception {
        StringBuilder sb = new StringBuilder(1000);

        for (int i = 0; i < 1000; i++) {
            sb.append('a');
        }


        reviewDto.setComment(sb.toString());

        mockMvc.perform(post("/api/v1/product/{productId}/review", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reviewDto)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    public void get_all_product_reviews_not_empty() throws Exception {
        ReviewPageDto review = new ReviewPageDto(1L, "test",
                        BigDecimal.valueOf(4),
                List.of(new ProductReviewDto(1L, 1L,
                                        "lol", "test",
                                        5, Date.valueOf(LocalDate.now()))));

        when(reviewService.getProductReviews(anyLong())).thenReturn(review);

        mockMvc.perform(get("/api/v1/product/{productId}/review", 1)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(1))
            .andExpect(jsonPath("$.productReviews.size()").value(1));
    }

    @Test
    public void get_all_product_reviews_empty() throws Exception {
        ReviewPageDto review = new ReviewPageDto(1L, null,
                null, null);

        when(reviewService.getProductReviews(anyLong())).thenReturn(review);

        mockMvc.perform(get("/api/v1/product/{productId}/review", 1)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void get_all_product_reviews_product_not_exists() throws Exception {
        when(reviewService.getProductReviews(anyLong()))
                .thenThrow(new EntityNotFoundException("Not found"));

        mockMvc.perform(get("/api/v1/product/{productId}/review", 1)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Not found"));
    }

}

package org.hotiver.app.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hotiver.api.Controller.product.ProductPromotionController;
import org.hotiver.common.Exception.auth.ForbiddenOperationException;
import org.hotiver.common.Exception.base.InvalidStateException;
import org.hotiver.config.filter.JwtFilter;
import org.hotiver.domain.Entity.ProductPromotion;
import org.hotiver.dto.product.ProductPromotionRequest;
import org.hotiver.dto.product.SellerProductPromotionDto;
import org.hotiver.service.auth.JwtService;
import org.hotiver.service.product.ProductPromotionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductPromotionController.class)
@AutoConfigureMockMvc(addFilters = false)
public class ProductPromotionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProductPromotionService productPromotionService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private JwtFilter jwtFilter;


    @Nested
    class AddProductPromotion {

        private ProductPromotionRequest productPromotionRequest;

        @BeforeEach
        void setUp() {
            productPromotionRequest = new ProductPromotionRequest(
                    15,
                    "sale",
                    OffsetDateTime.now(),
                    OffsetDateTime.now().plusDays(7),
                    true,
                    true
            );
        }

        @Test
        void shouldReturnCreated_whenPromotionSuccessfullyCreated() throws Exception {
            when(productPromotionService.createPercentPromotion(anyLong(), any()))
                    .thenReturn(new ProductPromotion());

            ResultActions result = mockMvc
                    .perform(post("/api/v1/products/{productId}/promotions",
                            1)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(productPromotionRequest)));

            result.andExpect(status().isCreated());

            verify(productPromotionService).createPercentPromotion(anyLong(), any());
        }

        @Test
        void shouldReturnConflict_whenPromotionPeriodOverlaps() throws Exception {
            when(productPromotionService.createPercentPromotion(anyLong(), any()))
                    .thenThrow(new InvalidStateException("Your dates must not overlap."));

            ResultActions result = mockMvc.perform(post("/api/v1/products/{productId}/promotions",
                            1)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(productPromotionRequest)));

            result.andExpect(status().isConflict());

            verify(productPromotionService).createPercentPromotion(anyLong(), any());
        }

        @Test
        void shouldReturnForbidden_whenUserIsNotProductOwner() throws Exception {
            when(productPromotionService.createPercentPromotion(anyLong(), any()))
                    .thenThrow(new ForbiddenOperationException(""));

            ResultActions result = mockMvc.perform(post("/api/v1/products/{productId}/promotions",
                    1)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(productPromotionRequest)));

            result.andExpect(status().isForbidden());

            verify(productPromotionService).createPercentPromotion(anyLong(), any());
        }

        @Test
        void shouldReturnUnprocessableEntity_whenDiscountIsIncorrect() throws Exception {
            productPromotionRequest = new ProductPromotionRequest(
                    104,
                    "sale",
                    OffsetDateTime.now(),
                    OffsetDateTime.now().plusDays(7),
                    true,
                    true
            );

            ResultActions result = mockMvc.perform(post("/api/v1/products/{productId}/promotions",
                    1)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(productPromotionRequest)));

            result.andExpect(status().isUnprocessableEntity());

            verify(productPromotionService, never()).createPercentPromotion(anyLong(), any());
        }
    }

    @Nested
    class GetProductPromotions {

        private final List<SellerProductPromotionDto> result = new ArrayList<>();

        @BeforeEach
        void setUp() {
            result.add(
                    new SellerProductPromotionDto(
                            1L,
                            1L,
                            "name",
                            "title",
                            20,
                            OffsetDateTime.now(),
                            OffsetDateTime.now().plusDays(7),
                            true,
                            true
                    )
            );
        }

        @Test
        void shouldReturnProductPromotions_whenProductExists() throws Exception {
            when(productPromotionService.getProductPromotions(anyLong()))
                    .thenReturn(result);

            ResultActions result = mockMvc
                    .perform(get("/api/v1/products/{productId}/promotions",
                            1)
                    .contentType(MediaType.APPLICATION_JSON));

            result.andExpect(status().isOk());
            result.andExpect(jsonPath("$.size()").value(1));
            result.andExpect(jsonPath("$[0].promotionId").value(1L));
            result.andExpect(jsonPath("$[0].productId").value(1L));
        }

        @Test
        void shouldReturnEmptyList_whenProductHasNoPromotions() throws Exception {
            when(productPromotionService.getProductPromotions(anyLong()))
                    .thenReturn(new ArrayList<>());

            ResultActions result = mockMvc
                    .perform(get("/api/v1/products/{productId}/promotions",
                            1)
                            .contentType(MediaType.APPLICATION_JSON));

            result.andExpect(status().isOk());
            result.andExpect(jsonPath("$.size()").value(0));
        }

        @Test
        void shouldReturnForbidden_whenUserIsNotProductOwner() throws Exception {
            when(productPromotionService.getProductPromotions(anyLong()))
                    .thenThrow(new ForbiddenOperationException(""));

            ResultActions result = mockMvc
                    .perform(get("/api/v1/products/{productId}/promotions",
                            1)
                            .contentType(MediaType.APPLICATION_JSON));

            result.andExpect(status().isForbidden());
        }

        @Test
        void shouldReturnForbidden_whenProductDoesNotExist() throws Exception {
            when(productPromotionService.getProductPromotions(anyLong()))
                    .thenThrow(new ForbiddenOperationException(""));

            ResultActions result = mockMvc
                    .perform(get("/api/v1/products/{productId}/promotions",
                            1)
                            .contentType(MediaType.APPLICATION_JSON));

            result.andExpect(status().isForbidden());
        }

    }

    @Nested
    class UpdateProductPromotion {

        private ProductPromotionRequest productPromotionRequest;

        @BeforeEach
        void setUp() {
            productPromotionRequest = new ProductPromotionRequest(
                    15,
                    "sale",
                    OffsetDateTime.now(),
                    OffsetDateTime.now().plusDays(7),
                    true,
                    true
            );
        }

        @Test
        void shouldReturnNoContent_whenPromotionSuccessfullyUpdated() throws Exception {
            doNothing().when(productPromotionService)
                    .updateProductPromotion(anyLong(), any());

            ResultActions result = mockMvc
                    .perform(put("/api/v1/products/promotions/{promotionId}",
                            1)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(productPromotionRequest)));

            result.andExpect(status().isNoContent());

            verify(productPromotionService).updateProductPromotion(anyLong(), any());
        }

        @Test
        void shouldReturnForbidden_whenPromotionDoesNotExist() throws Exception {
            doThrow(ForbiddenOperationException.class).when(productPromotionService)
                    .updateProductPromotion(anyLong(), any());

            ResultActions result = mockMvc
                    .perform(put("/api/v1/products/promotions/{promotionId}",
                            1)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(productPromotionRequest)));

            result.andExpect(status().isForbidden());

            verify(productPromotionService).updateProductPromotion(anyLong(), any());
        }

        @Test
        void shouldReturnForbidden_whenUserIsNotPromotionOwner() throws Exception {
            doThrow(ForbiddenOperationException.class).when(productPromotionService)
                    .updateProductPromotion(anyLong(), any());

            ResultActions result = mockMvc
                    .perform(put("/api/v1/products/promotions/{promotionId}",
                            1)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(productPromotionRequest)));

            result.andExpect(status().isForbidden());

            verify(productPromotionService).updateProductPromotion(anyLong(), any());
        }

        @Test
        void shouldReturnConflict_whenPromotionPeriodOverlaps() throws Exception {
            doThrow(InvalidStateException.class).when(productPromotionService)
                    .updateProductPromotion(anyLong(), any());

            ResultActions result = mockMvc
                    .perform(put("/api/v1/products/promotions/{promotionId}",
                            1)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(productPromotionRequest)));

            result.andExpect(status().isConflict());

            verify(productPromotionService).updateProductPromotion(anyLong(), any());
        }
    }

    @Nested
    class DeleteProductPromotion {

        @Test
        void shouldReturnNoContent_whenPromotionSuccessfullyDeleted() throws Exception {
            doNothing().when(productPromotionService).deleteProductPromotion(anyLong());

            ResultActions result = mockMvc
                    .perform(delete("/api/v1/products/promotions/{promotionId}",
                    1)
                    .contentType(MediaType.APPLICATION_JSON));

            result.andExpect(status().isNoContent());
        }

        @Test
        void shouldReturnForbidden_whenPromotionDoesNotExist() throws Exception {
            doThrow(ForbiddenOperationException.class)
                    .when(productPromotionService).deleteProductPromotion(anyLong());

            ResultActions result = mockMvc
                    .perform(delete("/api/v1/products/promotions/{promotionId}",
                            1)
                            .contentType(MediaType.APPLICATION_JSON));

            result.andExpect(status().isForbidden());
        }

        @Test
        void shouldReturnForbidden_whenUserIsNotPromotionOwner() throws Exception {
            doThrow(ForbiddenOperationException.class)
                    .when(productPromotionService).deleteProductPromotion(anyLong());

            ResultActions result = mockMvc
                    .perform(delete("/api/v1/products/promotions/{promotionId}",
                            1)
                            .contentType(MediaType.APPLICATION_JSON));

            result.andExpect(status().isForbidden());
        }
    }

}

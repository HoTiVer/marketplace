package org.hotiver.app.Controller;

import org.hotiver.api.Controller.product.ProductPromotionController;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

//@WebMvcTest(ProductPromotionController.class)
//@AutoConfigureMockMvc(addFilters = false)
public class ProductPromotionControllerTest {

    @Nested
    class AddProductPromotion {

        @Test
        void shouldReturnCreated_whenPromotionSuccessfullyCreated() {

        }

        @Test
        void shouldReturnConflict_whenPromotionPeriodOverlaps() {

        }

        @Test
        void shouldReturnForbidden_whenUserIsNotProductOwner() {

        }
    }

    @Nested
    class GetProductPromotions {

        @Test
        void shouldReturnProductPromotions_whenProductExists() {

        }

        @Test
        void shouldReturnEmptyList_whenProductHasNoPromotions() {

        }

        @Test
        void shouldReturnForbidden_whenUserIsNotProductOwner() {

        }

        @Test
        void shouldReturnForbidden_whenProductDoesNotExist() {

        }

    }

    @Nested
    class UpdateProductPromotion {
        @Test
        void shouldReturnNoContent_whenPromotionSuccessfullyUpdated() {}

        @Test
        void shouldReturnNotFound_whenPromotionDoesNotExist() {}

        @Test
        void shouldReturnForbidden_whenUserIsNotPromotionOwner() {}

        @Test
        void shouldReturnConflict_whenPromotionPeriodOverlaps() {}
    }

    @Nested
    class DeleteProductPromotion {
        @Test
        void shouldReturnNoContent_whenPromotionSuccessfullyDeleted() {}

        @Test
        void shouldReturnNotFound_whenPromotionDoesNotExist() {}

        @Test
        void shouldReturnForbidden_whenUserIsNotPromotionOwner() {}
    }

}

package org.hotiver.app.Controller;

import org.hotiver.api.Controller.WishListController;
import org.hotiver.common.Exception.ResourceNotFoundException;
import org.hotiver.config.filter.JwtFilter;
import org.hotiver.dto.product.ListProductDto;
import org.hotiver.service.JwtService;
import org.hotiver.service.WishListService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(WishListController.class)
@AutoConfigureMockMvc(addFilters = false)
public class WishListControllerTest {

    @Autowired
    private MockMvc mockMvc;


    @MockitoBean
    private WishListService wishListService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private JwtFilter jwtFilter;

    @Test
    public void get_user_wishlist_success() throws Exception {
        List<ListProductDto> wishlist = List.of(
                new ListProductDto(1L, "test1",
                        BigDecimal.valueOf(52.0), null),
                new ListProductDto(2L, "test2",
                        BigDecimal.valueOf(100.0), null));

        when(wishListService.getUserWishList()).thenReturn(wishlist);

        mockMvc.perform(get("/api/v1/wishlist")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$.[0].productId").value(1));

        verify(wishListService, times(1)).getUserWishList();
    }

    @Test
    public void get_user_empty_wishlist() throws Exception {
        List<ListProductDto> wishlist = new ArrayList<>();

        given(wishListService.getUserWishList()).willReturn(wishlist);

        mockMvc.perform(get("/api/v1/wishlist")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(0));

        verify(wishListService, times(1)).getUserWishList();
    }

    @Test
    public void delete_existing_product_from_wishlist() throws Exception {
        mockMvc.perform(delete("/api/v1/wishlist/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());


        verify(wishListService, times(1))
                .removeProductFromWishList(anyLong());
    }

    @Test
    public void delete_not_existing_product_from_wishlist() throws Exception {
        mockMvc.perform(delete("/api/v1/wishlist/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());


        verify(wishListService, times(1))
                .removeProductFromWishList(anyLong());
    }

    @Test
    public void add_existing_product_in_wishlist() throws Exception {
        mockMvc.perform(post("/api/v1/wishlist/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());


        verify(wishListService, times(1)).addProductInWishList(anyLong());
    }

    @Test
    public void add_not_existing_product_in_wishlist() throws Exception {
        doThrow(ResourceNotFoundException.class)
                .when(wishListService)
                .addProductInWishList(anyLong());

        mockMvc.perform(post("/api/v1/wishlist/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(wishListService, times(1)).addProductInWishList(anyLong());
    }

}

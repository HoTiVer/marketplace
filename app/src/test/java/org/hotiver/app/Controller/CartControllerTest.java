package org.hotiver.app.Controller;

import org.hotiver.api.Controller.CartController;
import org.hotiver.common.Exception.base.ResourceNotFoundException;
import org.hotiver.config.filter.JwtFilter;
import org.hotiver.dto.cart.CartItemDto;
import org.hotiver.service.order.CartService;
import org.hotiver.service.auth.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CartController.class)
@AutoConfigureMockMvc(addFilters = false)
public class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CartService cartService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private JwtFilter jwtFilter;

    @Test
    public void get_cart_not_empty() throws Exception {
        List<CartItemDto> list = List.of(
            new CartItemDto(1L, "test", 0.0, 3)
        );

        when(cartService.getUserCart()).thenReturn(list);

        mockMvc.perform(get("/api/v1/cart")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1));

        verify(cartService, times(1)).getUserCart();
    }

    @Test
    public void get_cart_empty() throws Exception {
        List<CartItemDto> list = new ArrayList<>();

        when(cartService.getUserCart()).thenReturn(list);

        mockMvc.perform(get("/api/v1/cart")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(0));

        verify(cartService, times(1)).getUserCart();
    }

    @Test
    public void add_product_to_cart() throws Exception {

        doNothing().when(cartService).addProductToCart(anyLong());

        mockMvc.perform(post("/api/v1/cart/2")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(cartService, times(1)).addProductToCart(anyLong());
    }

    @Test
    public void add_not_existing_product_to_cart() throws Exception {
        doThrow(ResourceNotFoundException.class).when(cartService).addProductToCart(anyLong());

        mockMvc.perform(post("/api/v1/cart/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(cartService, times(1)).addProductToCart(anyLong());
    }

    @Test
    public void delete_existing_product_from_cart() throws Exception {

        mockMvc.perform(delete("/api/v1/cart/1"))
                .andExpect(status().isOk());

        verify(cartService, times(1)).deleteProductFromCart(anyLong());
    }

    @Test
    public void delete_not_existing_product_from_cart() throws Exception {
        mockMvc.perform(delete("/api/v1/cart/2"))
                .andExpect(status().isOk());

        verify(cartService, times(1)).deleteProductFromCart(anyLong());
    }

    @Test
    public void update_product_count() throws Exception {
        mockMvc.perform(patch("/api/v1/cart/2")
                        .param("count", "1"))
                .andExpect(status().isOk());

        verify(cartService, times(1))
                .updateProductCount(anyLong(), anyInt());
    }
}

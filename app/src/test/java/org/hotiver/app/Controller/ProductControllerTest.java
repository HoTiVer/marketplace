package org.hotiver.app.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.hotiver.api.Controller.ProductController;
import org.hotiver.config.filter.JwtFilter;
import org.hotiver.dto.product.ProductAddDto;
import org.hotiver.dto.product.ProductGetDto;
import org.hotiver.service.auth.JwtService;
import org.hotiver.service.product.ProductImageService;
import org.hotiver.service.product.ProductQueryService;
import org.hotiver.service.product.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;


import java.math.BigDecimal;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
@AutoConfigureMockMvc(addFilters = false)
public class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private ProductQueryService productQueryService;

    @MockitoBean
    private ProductImageService productImageService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private JwtFilter jwtFilter;

    private ProductAddDto productAddDto;

    @BeforeEach
    public void setup() {
        productAddDto = new ProductAddDto();
        productAddDto.setName("Test Product");
        productAddDto.setDescription("Test Description");
        productAddDto.setPrice(BigDecimal.valueOf(1.0));
        productAddDto.setCategoryName("Test Category");
        productAddDto.setQuantity(1);
        productAddDto.setCharacteristics(Map.of("key1", "value1",
                "key2", "value2"));
    }

    @Test
    public void get_product_by_id() throws Exception {
        ProductGetDto productGetDto = ProductGetDto.builder()
                .id(1L)
                .name("Product 1")
                .price(BigDecimal.valueOf(1.0))
                .build();

        when(productQueryService.getProductById(any())).thenReturn(productGetDto);

        mockMvc.perform(get("/api/v1/product/{id}", 1)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(productGetDto.getName()));
    }

    @Test
    public void get_product_by_id_product_not_exist() throws Exception {
        when(productQueryService.getProductById(any())).thenThrow(new EntityNotFoundException());

        mockMvc.perform(get("/api/v1/product/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void add_product_success() throws Exception {
        MockMultipartFile data = new MockMultipartFile(
                "data",
                "",
                "application/json",
                objectMapper.writeValueAsBytes(productAddDto)
        );

        ResultActions response = mockMvc.perform(
                multipart("/api/v1/product")
                        .file(data)
        );

        response.andExpect(status().isOk());
    }

    @Test
    public void add_product_negative_price() throws Exception {
        productAddDto.setPrice(BigDecimal.valueOf(-1.0));

        MockMultipartFile data = new MockMultipartFile(
                "data",
                "",
                "application/json",
                objectMapper.writeValueAsBytes(productAddDto)
        );

        ResultActions response = mockMvc.perform(
                multipart("/api/v1/product")
                        .file(data)
        );

        response.andExpect(status().isUnprocessableEntity());
    }

    @Test
    public void add_product_null_name() throws Exception {
        productAddDto.setName(null);

        MockMultipartFile data = new MockMultipartFile(
                "data",
                "",
                "application/json",
                objectMapper.writeValueAsBytes(productAddDto)
        );

        ResultActions response = mockMvc.perform(
                multipart("/api/v1/product")
                        .file(data)
        );

        response.andExpect(status().isUnprocessableEntity());
    }

    @Test
    public void add_product_empty_name() throws Exception {
        productAddDto.setName("");

        MockMultipartFile data = new MockMultipartFile(
                "data",
                "",
                "application/json",
                objectMapper.writeValueAsBytes(productAddDto)
        );

        ResultActions response = mockMvc.perform(
                multipart("/api/v1/product")
                        .file(data)
        );

        response.andExpect(status().isUnprocessableEntity());
    }

    @Test
    public void add_product_category_not_exist() throws Exception {

        doThrow(new EntityNotFoundException("Category not found"))
                .when(productService)
                .addProduct(any(), any());

        MockMultipartFile data = new MockMultipartFile(
                "data",
                "",
                "application/json",
                objectMapper.writeValueAsBytes(productAddDto)
        );

        ResultActions response = mockMvc.perform(
                multipart("/api/v1/product")
                        .file(data)
        );

        response.andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message")
                    .value("Category not found"));
    }


    @Test
    public void delete_product_success() throws Exception {
        mockMvc.perform(delete("/api/v1/product/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    public void delete_product_is_not_exist() throws Exception {

        doThrow(new EntityNotFoundException("Product not found"))
                .when(productService)
                .deleteProductById(anyLong());

        mockMvc.perform(delete("/api/v1/product/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void update_product_success() throws Exception {
        MockMultipartFile data = new MockMultipartFile(
                "data",
                "",
                "application/json",
                objectMapper.writeValueAsBytes(productAddDto)
        );

        ResultActions response = mockMvc.perform(
                multipart("/api/v1/product/1")
                        .file(data)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        })
        );

        response.andExpect(status().isOk());
    }

    @Test
    public void update_product_name_null() throws Exception {
        productAddDto.setName(null);

        MockMultipartFile data = new MockMultipartFile(
                "data",
                "",
                "application/json",
                objectMapper.writeValueAsBytes(productAddDto)
        );

        ResultActions response = mockMvc.perform(
                multipart("/api/v1/product/1")
                        .file(data)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        })
        );

        response.andExpect(status().isUnprocessableEntity());
    }
}

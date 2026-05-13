package org.hotiver.app.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hotiver.api.Controller.ProductSearchController;
import org.hotiver.config.filter.JwtFilter;
import org.hotiver.dto.product.ListProductDto;
import org.hotiver.service.auth.JwtService;
import org.hotiver.service.product.ProductSearchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductSearchController.class)
@AutoConfigureMockMvc(addFilters = false)
public class ProductSearchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private JwtFilter jwtFilter;

    @MockitoBean
    private ProductSearchService productSearchService;

    private Page<ListProductDto> productDtoPage;

    @BeforeEach
    public void setup() {
        productDtoPage = new PageImpl<ListProductDto>(List.of(
                new ListProductDto(1L, "test1",
                        BigDecimal.valueOf(1), "url1"),

                new ListProductDto(2L, "test2",
                        BigDecimal.valueOf(2), "url2")
        ));
    }

    @Test
    public void find_product_by_keyword() throws Exception {
        when(productSearchService.productSearchByKeyWords(anyString(), anyInt(), anyInt()))
                .thenReturn(productDtoPage);

        ResultActions response = mockMvc.perform(get("/api/v1/search/product")
                .contentType(MediaType.APPLICATION_JSON)
                .param("searchTerm", "test")
                .param("page", "0")
                .param("size", "10"));

        response.andExpect(status().isOk());
        response.andExpect(jsonPath("$.content.[0].productId").value(1));

        verify(productSearchService, times(1))
                .productSearchByKeyWords(anyString(), anyInt(), anyInt());
    }

    @Test
    public void find_product_by_category() throws Exception {
        when(productSearchService.productSearchByCategory(anyString(), anyInt(), anyInt()))
                .thenReturn(productDtoPage);

        ResultActions response = mockMvc.perform(
                get("/api/v1/search/product/category/{category}", "test")
                .contentType(MediaType.APPLICATION_JSON)
                .param("page", "0")
                .param("size", "10"));

        response.andExpect(status().isOk());
        response.andExpect(jsonPath("$.content.[0].productId").value(1));

        verify(productSearchService, times(1))
                .productSearchByCategory(anyString(), anyInt(), anyInt());
    }

    @Test
    void should_return_empty_page_when_nothing_found() throws Exception {
        when(productSearchService.productSearchByKeyWords("none", 0, 10))
                .thenReturn(Page.empty());

        mockMvc.perform(get("/api/v1/search/product")
                        .param("searchTerm", "none")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());
    }
}

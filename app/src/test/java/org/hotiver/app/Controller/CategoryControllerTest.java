package org.hotiver.app.Controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.hotiver.api.Controller.CategoryController;
import org.hotiver.common.Exception.EntityAlreadyExistsException;
import org.hotiver.config.filter.JwtFilter;
import org.hotiver.dto.category.CategoryDto;
import org.hotiver.service.CategoryService;
import org.hotiver.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CategoryController.class)
@AutoConfigureMockMvc(addFilters = false)
public class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CategoryService categoryService;

    @MockitoBean
    private JwtFilter jwtFilter;

    @MockitoBean
    private JwtService jwtService;

    private List<CategoryDto> categoryDtoList;

    @BeforeEach
    public void setUp() {
        categoryDtoList = new ArrayList<>();
        categoryDtoList.add(new CategoryDto(1L, "test"));
    }

    @Test
    public void get_all_categories_success() throws Exception {
        given(categoryService.getCategories())
                .willReturn(categoryDtoList);

        ResultActions result = mockMvc
                .perform(get("/api/v1/category")
                        .contentType(MediaType.APPLICATION_JSON));


        result
                .andDo(print())
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.size()").value(categoryDtoList.size()))
                .andExpect(jsonPath("$.[0].id")
                .value(categoryDtoList.getFirst().getId()));

        verify(categoryService, times(1)).getCategories();
    }

    @Test
    public void add_category_success() throws Exception {
        ResultActions resultActions = mockMvc
                .perform(post("/api/v1/category")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryDtoList.getFirst())));

        resultActions.andExpect(status().isCreated());
        verify(categoryService, times(1)).addCategory(any());
    }

    @Test
    public void add_category_category_name_exists() throws Exception {
        String message = "Category already exists";

        doThrow(new  EntityAlreadyExistsException(message))
                .when(categoryService)
                .addCategory(any());

        ResultActions resultActions = mockMvc
                .perform(post("/api/v1/category")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryDtoList.getFirst())));

        resultActions.andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(message));

        verify(categoryService, times(1)).addCategory(any());
    }

    @Test
    public void add_category_category_name_is_null() throws Exception {
        CategoryDto emptyFieldDto = new CategoryDto();

        ResultActions resultActions = mockMvc
                .perform(post("/api/v1/category")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emptyFieldDto)));

        resultActions.andExpect(status().isUnprocessableEntity());
        verify(categoryService, times(0)).addCategory(any());
    }

    @Test
    public void delete_category_success() throws Exception {
        ResultActions resultActions = mockMvc
                .perform(delete("/api/v1/category/{id}", categoryDtoList.get(0).getId())
                        .contentType(MediaType.APPLICATION_JSON));

        resultActions.andExpect(status().isNoContent());
        verify(categoryService, times(1)).deleteCategory(anyLong());
    }

    @Test
    public void edit_category_success() throws Exception {
        CategoryDto categoryDto = new CategoryDto();
        categoryDto.setName("updated test");

        ResultActions resultActions = mockMvc
                .perform(put("/api/v1/category/{id}", categoryDtoList.get(0).getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryDto)));

        resultActions.andExpect(status().isOk());
        verify(categoryService, times(1)).editCategory(anyLong(), any());
    }

    @Test
    public void edit_category_category_name_is_null() throws Exception {
        CategoryDto categoryDto = new CategoryDto();

        ResultActions resultActions = mockMvc
                .perform(put("/api/v1/category/{id}", categoryDtoList.get(0).getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryDto)));

        resultActions.andExpect(status().isUnprocessableEntity());

        verify(categoryService, times(0)).editCategory(anyLong(), any());
    }

    @Test
    public void edit_category_category_does_not_exists() throws Exception {
        CategoryDto categoryDto = new CategoryDto();
        categoryDto.setName("updated test");

        doThrow(new EntityNotFoundException("not found"))
            .when(categoryService)
                .editCategory(anyLong(), any(CategoryDto.class));

        ResultActions resultActions = mockMvc
                .perform(put("/api/v1/category/{id}", categoryDtoList.get(0).getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryDto)));

        resultActions.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                    .value("not found"));

        verify(categoryService, times(1)).editCategory(anyLong(), any());
    }
}

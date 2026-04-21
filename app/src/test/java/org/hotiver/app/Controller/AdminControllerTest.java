package org.hotiver.app.Controller;


import jakarta.persistence.EntityNotFoundException;
import org.hotiver.api.Controller.AdminController;
import org.hotiver.common.Exception.base.EntityAlreadyExistsException;
import org.hotiver.config.filter.JwtFilter;
import org.hotiver.dto.admin.SellerRegisterResponse;
import org.hotiver.service.admin.AdminService;
import org.hotiver.service.auth.JwtService;
import org.hotiver.service.user.SellerRegisterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminService adminService;

    @MockitoBean
    private SellerRegisterService sellerRegisterService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private JwtFilter jwtFilter;

    private List<SellerRegisterResponse> sellerRegisters;

    @BeforeEach
    public void setup() {
        sellerRegisters = new ArrayList<SellerRegisterResponse>();
        sellerRegisters.add(
                new SellerRegisterResponse(
                        1L,
                        "requestedNickname",
                        "displayName",
                        "profileDescription"
                )
        );
    }

    @Test
    public void get_seller_register_requests_not_empty() throws Exception {
        doReturn(sellerRegisters)
                .when(sellerRegisterService)
                .getSellerRegisterRequests();

        mockMvc.perform(get("/api/v1/admin/request/seller-register")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(sellerRegisters.size()));
    }

    @Test
    public void get_seller_register_requests_empty() throws Exception {
        sellerRegisters = new ArrayList<>();

        doReturn(sellerRegisters)
                .when(sellerRegisterService)
                .getSellerRegisterRequests();

        mockMvc.perform(get("/api/v1/admin/request/seller-register")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(sellerRegisters.size()));
    }

    @Test
    public void accept_seller_register_request() throws Exception {

        mockMvc.perform(post("/api/v1/admin/request/seller-register/{id}", 1)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void accept_seller_register_request_not_exist() throws Exception {
        doThrow(new EntityNotFoundException("SellerRegister not found"))
                .when(sellerRegisterService)
                .acceptSellerRegisterRequest(anyLong());

        mockMvc.perform(post("/api/v1/admin/request/seller-register/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void accept_seller_register_request_seller_already_exist() throws Exception{
        doThrow(new EntityAlreadyExistsException("Seller already exists"))
                .when(sellerRegisterService)
                .acceptSellerRegisterRequest(anyLong());

        mockMvc.perform(post("/api/v1/admin/request/seller-register/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());
    }

    @Test
    public void decline_seller_register_request() throws Exception{
        mockMvc.perform(delete("/api/v1/admin/request/seller-register/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void decline_seller_register_request_not_exist() throws Exception{
        doThrow(new EntityNotFoundException("SellerRegister not found"))
                .when(sellerRegisterService)
                .acceptSellerRegisterRequest(anyLong());

        mockMvc.perform(post("/api/v1/admin/request/seller-register/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}

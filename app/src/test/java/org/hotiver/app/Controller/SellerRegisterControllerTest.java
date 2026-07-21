package org.hotiver.app.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.hotiver.api.Controller.SellerRegisterController;
import org.hotiver.common.Exception.base.EntityAlreadyExistsException;
import org.hotiver.config.filter.JwtFilter;
import org.hotiver.dto.admin.SellerRegisterResponse;
import org.hotiver.dto.seller.SellerRegisterDto;
import org.hotiver.service.auth.JwtService;
import org.hotiver.service.user.SellerRegisterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
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
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SellerRegisterController.class)
@AutoConfigureMockMvc(addFilters = false)
public class SellerRegisterControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SellerRegisterService sellerRegisterService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private JwtFilter jwtFilter;

    @Nested
    class SendSellerRegisterRequest {

        @Test
        public void shouldReturnCreated_whenSellerRegisterRequestSent() throws Exception {
            SellerRegisterDto sellerRegisterDto = new SellerRegisterDto(
                    "test",
                    "test",
                    "test"
            );
            sellerRegisterDto.setRequestedNickname("test");
            sellerRegisterDto.setDisplayName("test");

            doNothing().when(sellerRegisterService)
                    .sendSellerRegisterRequest(any(SellerRegisterDto.class));

            mockMvc.perform(post("/api/v1/seller-requests")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sellerRegisterDto)))
                    .andExpect(status().isCreated());
        }

        @Test
        public void shouldReturnUnprocessableEntity_whensDisplayNameIsBlank() throws Exception {
            SellerRegisterDto sellerRegisterDto = new SellerRegisterDto();

            doNothing().when(sellerRegisterService)
                    .sendSellerRegisterRequest(any(SellerRegisterDto.class));

            mockMvc.perform(post("/api/v1/seller-requests")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sellerRegisterDto)))
                    .andExpect(status().isUnprocessableEntity());
        }

        @Test
        public void shouldReturnUnprocessableEntity_whensNicknameIsBlank() throws Exception {
            SellerRegisterDto sellerRegisterDto = new SellerRegisterDto();

            doNothing().when(sellerRegisterService)
                    .sendSellerRegisterRequest(any(SellerRegisterDto.class));

            mockMvc.perform(post("/api/v1/seller-requests")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sellerRegisterDto)))
                    .andExpect(status().isUnprocessableEntity());
        }
    }

    @Nested
    class GetSellerRegisterRequests {

        private List<SellerRegisterResponse> sellerRegisters;

        @BeforeEach
        public void setup() {
            sellerRegisters = new ArrayList<>();
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
        public void shouldReturnSellerRegisterRequests() throws Exception {
            doReturn(sellerRegisters)
                    .when(sellerRegisterService)
                    .getSellerRegisterRequests();

            mockMvc.perform(get("/api/v1/seller-requests")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.size()").value(sellerRegisters.size()));
        }

        @Test
        public void shouldReturnEmptySellerRegisterRequests() throws Exception {
            sellerRegisters = new ArrayList<>();

            doReturn(sellerRegisters)
                    .when(sellerRegisterService)
                    .getSellerRegisterRequests();

            mockMvc.perform(get("/api/v1/seller-requests")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.size()").value(sellerRegisters.size()));
        }
    }

    @Nested
    class AcceptSellerRegisterRequest {

        @Test
        public void shouldReturnNoContent_whenSellerRegisterRequestAccepted() throws Exception {

            mockMvc.perform(post("/api/v1/seller-requests/{id}/approve", 1)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNoContent());
        }

        @Test
        public void shouldReturnNotFound_whenSellerRegisterRequestNotExists() throws Exception {
            doThrow(new EntityNotFoundException("SellerRegister not found"))
                    .when(sellerRegisterService)
                    .acceptSellerRegisterRequest(anyLong());

            mockMvc.perform(post("/api/v1/seller-requests/{id}/approve", 1)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }

        @Test
        public void shouldReturnConflict_whenSellerAlreadyExist() throws Exception {
            doThrow(new EntityAlreadyExistsException("Seller already exists"))
                    .when(sellerRegisterService)
                    .acceptSellerRegisterRequest(anyLong());

            mockMvc.perform(post("/api/v1/seller-requests/{id}/approve", 1)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isConflict());
        }
    }

    @Nested
    class DeclineSellerRegisterRequest {
        @Test
        public void shouldReturnNoContent_whenDeclineSellerRegisterRequest() throws Exception{
            mockMvc.perform(delete("/api/v1/seller-requests/{id}", 1)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNoContent());
        }

        @Test
        public void shouldReturnNoContent_whenSellerRegisterRequestNotExists() throws Exception{
            mockMvc.perform(delete("/api/v1/seller-requests/{id}", 1)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNoContent());
        }
    }
}

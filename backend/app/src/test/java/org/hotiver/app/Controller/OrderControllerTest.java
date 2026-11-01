package org.hotiver.app.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.hotiver.api.Controller.OrderController;
import org.hotiver.common.Exception.auth.ForbiddenOperationException;
import org.hotiver.common.Exception.base.InvalidStateException;
import org.hotiver.common.Exception.base.ResourceNotFoundException;
import org.hotiver.common.Exception.order.CannotBuyOwnProductException;
import org.hotiver.config.filter.JwtFilter;
import org.hotiver.dto.order.CreateOrderDto;
import org.hotiver.dto.order.SellerOrdersResponse;
import org.hotiver.dto.order.UpdateStatusDto;
import org.hotiver.dto.order.UserOrderDto;
import org.hotiver.service.auth.JwtService;
import org.hotiver.service.order.OrderService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
@AutoConfigureMockMvc(addFilters = false)
public class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderService productService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private JwtFilter jwtFilter;
    @Autowired
    private OrderService orderService;

    @Nested
    class CreateOrder {

        @Test
        public void shouldReturnCreated_whenOrderCreatedSuccessfully() throws Exception {
            CreateOrderDto createOrderDto = new CreateOrderDto("Test",
                    "Test",
                    "Test", "8888888888");

            mockMvc.perform(post("/api/v1/order")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createOrderDto)))
                    .andExpect(status().isCreated());

            verify(orderService, times(1)).createOrder(any());
        }

        @Test
        public void shouldReturnUnprocessableEntity_whenPhoneNumberIsIncorrect() throws Exception {
            CreateOrderDto createOrderDto = new CreateOrderDto("Test",
                    "Test",
                    "Test", "no");

            mockMvc.perform(post("/api/v1/order")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createOrderDto)))
                    .andExpect(status().isUnprocessableEntity());

            verify(orderService, times(0)).createOrder(any());
        }

        @Test
        public void shouldReturnUnprocessableEntity_incorrectFieldsInDto() throws Exception {
            CreateOrderDto createOrderDto = new CreateOrderDto(null,
                    "",
                    "Test", "8888888888");

            mockMvc.perform(post("/api/v1/order")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createOrderDto)))
                    .andExpect(status().isUnprocessableEntity());

            verify(orderService, times(0)).createOrder(any());
        }

        @Test
        public void shouldReturnNotFound_whenUserCartIsEmpty() throws Exception {
            CreateOrderDto createOrderDto = new CreateOrderDto("Test",
                    "Test",
                    "Test", "8888888888");

            doThrow(new EntityNotFoundException("Cart is empty"))
                    .when(orderService).createOrder(any());

            mockMvc.perform(post("/api/v1/order")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createOrderDto)))
                    .andExpect(status().isNotFound());

            verify(orderService, times(1)).createOrder(any());
        }

        @Test
        public void shouldReturnNotFound_whenProductIsNotFound() throws Exception {
            CreateOrderDto createOrderDto = new CreateOrderDto("Test",
                    "Test",
                    "Test", "8888888888");

            doThrow(EntityNotFoundException.class)
                    .when(orderService).createOrder(any());

            mockMvc.perform(post("/api/v1/order")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createOrderDto)))
                    .andExpect(status().isNotFound());

            verify(orderService, times(1)).createOrder(any());
        }

        @Test
        public void shouldReturnForbidden_whenSellerWantsToByHisOwnProduct() throws Exception {
            CreateOrderDto createOrderDto = new CreateOrderDto("Test",
                    "Test",
                    "Test", "8888888888");

            doThrow(new CannotBuyOwnProductException("You cannot buy your own product"))
                    .when(orderService).createOrder(any());

            mockMvc.perform(post("/api/v1/order")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createOrderDto)))
                    .andExpect(status().isForbidden());

            verify(orderService, times(1)).createOrder(any());
        }

        @Test
        public void shouldReturnNotFound_whenProductQuantityIsNotEnough() throws Exception {
            CreateOrderDto createOrderDto = new CreateOrderDto("Test",
                    "Test",
                    "Test", "8888888888");

            doThrow(new InvalidStateException("Quantity is greater than stock quantity"))
                    .when(orderService).createOrder(any());

            mockMvc.perform(post("/api/v1/order")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createOrderDto)))
                    .andExpect(status().isConflict());

            verify(orderService, times(1)).createOrder(any());
        }
    }

    @Nested
    class GetSellerOrders {
        @Test
        public void shouldReturnSellerOrders() throws Exception {
            SellerOrdersResponse response = new SellerOrdersResponse(
                    null,
                    null
            );

            when(orderService.getSellerOrders(anyInt(), anyInt())).thenReturn(response);

            mockMvc.perform(get("/api/v1/order/seller/manage-orders")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            verify(orderService, times(1))
                    .getSellerOrders(anyInt(), anyInt());
        }
    }

    @Nested
    class CancelOrder {
        @Test
        public void shouldReturnNoContent_whenOrderCancelled() throws Exception {

            mockMvc.perform(patch("/api/v1/order/orders/{orderId}/cancel",
                            anyLong())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNoContent());

            verify(orderService, times(1))
                    .cancelUserOrder(anyLong());
        }

        @Test
        public void shouldReturnNotFound_whenOrderNotFound() throws Exception {
            doThrow(new EntityNotFoundException("Order not found"))
                    .when(orderService).cancelUserOrder(anyLong());

            mockMvc.perform(patch("/api/v1/order/orders/{orderId}/cancel",
                            anyLong())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());

            verify(orderService, times(1))
                    .cancelUserOrder(anyLong());
        }

        @Test
        public void shouldReturnForbidden_whenUserIsNotOwner() throws Exception {
            doThrow(ForbiddenOperationException.class)
                    .when(orderService).cancelUserOrder(anyLong());

            mockMvc.perform(patch("/api/v1/order/orders/{orderId}/cancel",
                            anyLong())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());

            verify(orderService, times(1))
                    .cancelUserOrder(anyLong());
        }
    }

    @Nested
    class ChangeOrderStatus {
        @Test
        public void shouldReturnNoContent_whenStatusStatusUpdated() throws Exception {
            UpdateStatusDto updateStatusDto = new UpdateStatusDto();
            updateStatusDto.setStatus("newStatus");

            mockMvc.perform(patch("/api/v1/order/seller/manage-orders/{orderId}",
                            1)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateStatusDto)))
                    .andExpect(status().isNoContent());

            verify(orderService, times(1))
                    .changeOrderStatus(anyLong(), any());

        }

        @Test
        public void shouldReturnNotFound_whenOrderStatusNotExists() throws Exception {
            UpdateStatusDto updateStatusDto = new UpdateStatusDto();
            updateStatusDto.setStatus("newStatus");

            doThrow(ResourceNotFoundException.class)
                    .when(orderService).changeOrderStatus(anyLong(), any());

            mockMvc.perform(patch("/api/v1/order/seller/manage-orders/{orderId}",
                            1)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateStatusDto)))
                    .andExpect(status().isNotFound());

            verify(orderService, times(1))
                    .changeOrderStatus(anyLong(), any());
        }

        @Test
        public void shouldReturnNotFound_whenOrderNotExists() throws Exception {
            UpdateStatusDto updateStatusDto = new UpdateStatusDto();
            updateStatusDto.setStatus("newStatus");

            doThrow(EntityNotFoundException.class)
                    .when(orderService).changeOrderStatus(anyLong(), any());

            mockMvc.perform(patch("/api/v1/order/seller/manage-orders/{orderId}",
                            1)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateStatusDto)))
                    .andExpect(status().isNotFound());

            verify(orderService, times(1))
                    .changeOrderStatus(anyLong(), any());
        }

        @Test
        public void shouldReturnForbidden_whenSellerIsNotOwnerOfOrder() throws Exception {
            UpdateStatusDto updateStatusDto = new UpdateStatusDto();
            updateStatusDto.setStatus("newStatus");

            doThrow(ForbiddenOperationException.class)
                    .when(orderService).changeOrderStatus(anyLong(), any());

            mockMvc.perform(patch("/api/v1/order/seller/manage-orders/{orderId}",
                            1)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateStatusDto)))
                    .andExpect(status().isForbidden());

            verify(orderService, times(1))
                    .changeOrderStatus(anyLong(), any());
        }

        @Test
        public void shouldReturnConflict_whenStatusCannotBeChangedToNew() throws Exception {
            UpdateStatusDto updateStatusDto = new UpdateStatusDto();
            updateStatusDto.setStatus("newStatus");

            doThrow(InvalidStateException.class)
                    .when(orderService).changeOrderStatus(anyLong(), any());

            mockMvc.perform(patch("/api/v1/order/seller/manage-orders/{orderId}",
                            1)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateStatusDto)))
                    .andExpect(status().isConflict());

            verify(orderService, times(1))
                    .changeOrderStatus(anyLong(), any());
        }
    }

    @Nested
    class getUserOrdersHistory {
        @Test
        public void shouldReturnUserOrdersHistory() throws Exception {
            Page<UserOrderDto> userOrderDtoPage = null;

            when(orderService.getUserOrders(anyInt(), anyInt()))
                    .thenReturn(userOrderDtoPage);

            mockMvc.perform(get("/api/v1/order/orders",
                            1)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            verify(orderService, times(1))
                    .getUserOrders(anyInt(), anyInt());
        }
    }
}

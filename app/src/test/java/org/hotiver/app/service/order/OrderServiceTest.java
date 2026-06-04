package org.hotiver.app.service.order;

import jakarta.persistence.EntityNotFoundException;
import org.hotiver.common.Enum.OrderStatus;
import org.hotiver.common.Enum.RoleType;
import org.hotiver.common.Exception.auth.ForbiddenOperationException;
import org.hotiver.common.Exception.base.InvalidStateException;
import org.hotiver.common.Exception.order.CannotBuyOwnProductException;
import org.hotiver.domain.Entity.*;
import org.hotiver.domain.security.SecurityUser;
import org.hotiver.dto.order.CreateOrderDto;
import org.hotiver.dto.order.SellerOrderDto;
import org.hotiver.dto.order.SellerOrdersResponse;
import org.hotiver.dto.order.UserOrderDto;
import org.hotiver.repo.core.CartItemRepo;
import org.hotiver.repo.core.OrderRepo;
import org.hotiver.repo.core.ProductRepo;
import org.hotiver.repo.core.SellerRepo;
import org.hotiver.repo.projection.OrderProjectionRepo;
import org.hotiver.service.common.CurrentUserService;
import org.hotiver.service.order.OrderService;
import org.hotiver.service.redis.RedisOutboxService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.testcontainers.shaded.org.apache.commons.lang3.builder.ToStringExclude;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    private ProductRepo productRepo;

    @Mock
    private OrderRepo orderRepo;

    @Mock
    private OrderProjectionRepo orderProjectionRepo;

    @Mock
    private CartItemRepo cartItemRepo;

    @Mock
    private SellerRepo sellerRepo;

    @Mock
    private RedisOutboxService redisOutboxService;

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private OrderService orderService;

    private User user;
    private Product product;
    private Seller seller;
    private Order order;

    @BeforeEach
    public void init() {
        user = new User();
        user.setId(1L);
        user.setEmail("test@test.com");
        user.setRoles(new ArrayList<>(
                List.of(
                        new Role(1L, RoleType.USER)
                )
        ));

        product = new Product();
        product.setId(1L);
        product.setName("test");
        product.setPrice(BigDecimal.valueOf(52));
        product.setStockQuantity(10);
        product.setSalesCount(10);

        seller = new Seller();
        seller.setId(2L);

        product.setSeller(seller);

        order = new Order();
        order.setId(1L);
        order.setProduct(product);
        order.setQuantity(1);
    }

    @Nested
    class CreateOrder {

        private CreateOrderDto createOrderDto;
        private CartItem cartItem;

        @BeforeEach
        public void setUp() {
            cartItem = new CartItem();
            cartItem.setProduct(product);

            user.setCart(new HashSet<>(Set.of(
                    cartItem
            )));

            createOrderDto = new CreateOrderDto(
                    "deliveryAddress",
                    "deliveryCity",
                    "receiverName",
                    "receiverPhone"
            );
        }

        @Test
        void shouldCreateOrder() {
            when(currentUserService.getCurrentUser()).thenReturn(user);


            orderService.createOrder(createOrderDto);

            assertTrue(user.getCart().isEmpty());

            verify(productRepo).save(product);
            verify(cartItemRepo).delete(cartItem);
        }

        @Test
        void shouldThrowException_whenUserCartIsEmpty() {
            user.setCart(new HashSet<>());

            when(currentUserService.getCurrentUser()).thenReturn(user);

            assertThrows(EntityNotFoundException.class,
                    () -> orderService.createOrder(createOrderDto)
            );

        }

        @Test
        void shouldThrowException_whenUserWantsByOwnProduct() {
            seller.setId(1L);

            when(currentUserService.getCurrentUser()).thenReturn(user);

            assertThrows(CannotBuyOwnProductException.class,
                    () -> orderService.createOrder(createOrderDto)
            );
        }

    }

    @Nested
    class GetUserOrders {

        private Page<UserOrderDto> userOrders;

        @Test
        void shouldGetUserOrders() {
            int page = 0;
            int size = 10;

            Pageable pageable = PageRequest.of(page, size);

            UserOrderDto dto = new UserOrderDto(
                    1L,
                    10L,
                    "Product",
                    "Seller",
                    2,
                    new Date(1),
                    null,
                    "CREATED",
                    BigDecimal.valueOf(100),
                    "Kyiv"
            );

            Page<UserOrderDto> expected =
                    new PageImpl<>(List.of(dto), pageable, 1);

            when(currentUserService.getUserPrincipal())
                    .thenReturn(new SecurityUser(user));

            when(orderProjectionRepo.findUserOrders(1L, pageable))
                    .thenReturn(expected);

            Page<UserOrderDto> result =
                    orderService.getUserOrders(page, size);

            assertEquals(1, result.getTotalElements());
            assertEquals("Product", result.getContent().get(0).getProductName());

            verify(orderProjectionRepo).findUserOrders(1L, pageable);
        }

        @Test
        void shouldGetEmptyUserOrders() {
            int page = 0;
            int size = 10;

            Pageable pageable = PageRequest.of(page, size);

            Page<UserOrderDto> emptyPage =
                    new PageImpl<>(List.of(), pageable, 0);

            when(currentUserService.getUserPrincipal())
                    .thenReturn(new SecurityUser(user));

            when(orderProjectionRepo.findUserOrders(1L, pageable))
                    .thenReturn(emptyPage);

            Page<UserOrderDto> result =
                    orderService.getUserOrders(page, size);

            assertTrue(result.isEmpty());

            verify(orderProjectionRepo).findUserOrders(1L, pageable);
        }

    }

    @Nested
    class CancelUserOrder {

        private Long orderId;

        @Test
        void shouldCancelUserOrder() {
            order.setUser(user);
            order.setStatus(OrderStatus.CREATED);

            when(currentUserService.getUserPrincipal()).thenReturn(new SecurityUser(user));

            when(orderRepo.findById(orderId))
                    .thenReturn(Optional.of(order));

            when(productRepo.findById(order.getProduct().getId()))
                    .thenReturn(Optional.of(product));


            orderService.cancelUserOrder(orderId);

            verify(orderRepo).save(order);
            verify(productRepo).save(product);
        }

        @Test
        void shouldThrowException_whenOrderNotFound() {
            order.setUser(user);
            order.setStatus(OrderStatus.CREATED);

            when(currentUserService.getUserPrincipal()).thenReturn(new SecurityUser(user));

            when(orderRepo.findById(orderId))
                    .thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> orderService.cancelUserOrder(orderId)
            );
        }

        @Test
        void shouldThrowException_whenProductNotFound() {
            order.setUser(user);
            order.setStatus(OrderStatus.CREATED);

            when(currentUserService.getUserPrincipal()).thenReturn(new SecurityUser(user));

            when(orderRepo.findById(orderId))
                    .thenReturn(Optional.of(order));

            when(productRepo.findById(order.getProduct().getId()))
                    .thenReturn(Optional.empty());


            assertThrows(EntityNotFoundException.class,
                    () -> orderService.cancelUserOrder(orderId)
            );
        }

        @Test
        void shouldThrowException_whenUserDoNotOwnThisOrder() {
            User newUser = new User();
            newUser.setId(52L);

            order.setUser(newUser);
            order.setStatus(OrderStatus.CREATED);

            when(currentUserService.getUserPrincipal()).thenReturn(new SecurityUser(user));

            when(orderRepo.findById(orderId))
                    .thenReturn(Optional.of(order));

            assertThrows(ForbiddenOperationException.class,
                    () -> orderService.cancelUserOrder(orderId)
            );
        }
    }

    @Nested
    class GetSellerOrders {

        private final int page = 0;
        private final int size = 10;

        private Pageable pageable;

        @BeforeEach
        void setUp() {
            pageable = PageRequest.of(page, size);
        }

        @Test
        void shouldGetSellerOrders() {

            SecurityUser securityUser = new SecurityUser(user);

            Page<SellerOrderDto> ordersPage =
                    new PageImpl<>(List.of(new SellerOrderDto()), pageable, 1);

            when(currentUserService.getUserPrincipal())
                    .thenReturn(securityUser);

            when(sellerRepo.findByEmail(user.getEmail()))
                    .thenReturn(Optional.of(seller));

            when(orderProjectionRepo.findSellerOrders(seller.getId(), pageable))
                    .thenReturn(ordersPage);

            SellerOrdersResponse result =
                    orderService.getSellerOrders(page, size);

            assertNotNull(result);
            assertEquals(1, result.orders().getTotalElements());
            assertEquals(OrderStatus.values().length, result.statuses().size());

            verify(orderProjectionRepo)
                    .findSellerOrders(seller.getId(), pageable);

        }

        @Test
        void shouldGetEmptySellerOrders() {
            SecurityUser securityUser = new SecurityUser(user);

            Page<SellerOrderDto> emptyPage =
                    new PageImpl<>(List.of(), pageable, 0);

            when(currentUserService.getUserPrincipal())
                    .thenReturn(securityUser);

            when(sellerRepo.findByEmail(user.getEmail()))
                    .thenReturn(Optional.of(seller));

            when(orderProjectionRepo.findSellerOrders(seller.getId(), pageable))
                    .thenReturn(emptyPage);

            SellerOrdersResponse result =
                    orderService.getSellerOrders(page, size);

            assertTrue(result.orders().isEmpty());
            assertEquals(OrderStatus.values().length, result.statuses().size());
        }

        @Test
        void shouldThrowException_whenSellerNotFound() {
            SecurityUser securityUser = new SecurityUser(user);

            when(currentUserService.getUserPrincipal())
                    .thenReturn(securityUser);

            when(sellerRepo.findByEmail(user.getEmail()))
                    .thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> orderService.getSellerOrders(page, size)
            );
        }

    }

    @Nested
    class ChangeOrderStatus {

        private Category category;

        @BeforeEach
        void setUp() {
            category = new Category();
            category.setId(1L);

            product.setCategory(category);
        }

        @ParameterizedTest
        @CsvSource({
                "CREATED,CONFIRMED",
                "CREATED,CANCELLED",
                "CONFIRMED,IN_TRANSIT",
                "CONFIRMED,CANCELLED",
                "IN_TRANSIT,DELIVERED",
                "IN_TRANSIT,RETURNED",
                "DELIVERED,COMPLETED",
                "DELIVERED,RETURNED"
        })
        void shouldChangeOrderStatusSuccessfully(
                OrderStatus currentStatus,
                OrderStatus newStatus
        ) {
            order.setStatus(currentStatus);
            order.setSeller(seller);

            when(currentUserService.getUserPrincipal())
                    .thenReturn(new SecurityUser(user));

            when(sellerRepo.findByEmail(user.getEmail()))
                    .thenReturn(Optional.of(seller));

            when(orderRepo.findById(order.getId()))
                    .thenReturn(Optional.of(order));

            orderService.changeOrderStatus(
                    order.getId(),
                    newStatus.name()
            );

            assertEquals(newStatus, order.getStatus());

            verify(orderRepo).save(order);
        }

        @Test
        void shouldThrowException_whenSellerNotFound() {
            String newStatus = OrderStatus.CANCELLED.toString();

            when(currentUserService.getUserPrincipal()).thenReturn(new SecurityUser(user));

            when(sellerRepo.findByEmail(user.getEmail())).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    ()-> orderService.changeOrderStatus(order.getId(), newStatus)
            );
        }

        @Test
        void shouldThrowException_whenOrderNotFound() {
            String newStatus = OrderStatus.CANCELLED.toString();

            when(currentUserService.getUserPrincipal()).thenReturn(new SecurityUser(user));

            when(sellerRepo.findByEmail(user.getEmail())).thenReturn(Optional.of(seller));

            when(orderRepo.findById(order.getId())).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    ()-> orderService.changeOrderStatus(order.getId(), newStatus)
            );
        }

        @Test
        void shouldThrowException_whenSellerDoNotOwnsOrder() {
            order.setStatus(OrderStatus.CREATED);

            Seller newSeller = new Seller();
            newSeller.setId(52L);
            order.setSeller(newSeller);

            String newStatus = OrderStatus.CANCELLED.toString();

            when(currentUserService.getUserPrincipal()).thenReturn(new SecurityUser(user));

            when(sellerRepo.findByEmail(user.getEmail())).thenReturn(Optional.of(seller));

            when(orderRepo.findById(order.getId())).thenReturn(Optional.of(order));

            assertThrows(ForbiddenOperationException.class,
                    () -> orderService.changeOrderStatus(order.getId(), newStatus)
            );
        }

        @ParameterizedTest
        @CsvSource({
                "CREATED,DELIVERED",
                "CREATED,COMPLETED",
                "CONFIRMED,COMPLETED",
                "CANCELLED,CREATED",
                "RETURNED,CONFIRMED",
                "COMPLETED,CANCELLED"
        })
        void shouldThrowException_whenTransitionIsInvalid(
                OrderStatus currentStatus,
                OrderStatus newStatus
        ) {
            order.setStatus(currentStatus);
            order.setSeller(seller);

            when(currentUserService.getUserPrincipal())
                    .thenReturn(new SecurityUser(user));

            when(sellerRepo.findByEmail(user.getEmail()))
                    .thenReturn(Optional.of(seller));

            when(orderRepo.findById(order.getId()))
                    .thenReturn(Optional.of(order));

            assertThrows(
                    InvalidStateException.class,
                    () -> orderService.changeOrderStatus(
                            order.getId(),
                            newStatus.name()
                    )
            );

            verify(orderRepo, never()).save(any());
        }

    }
}

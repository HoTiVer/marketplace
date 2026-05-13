package org.hotiver.app.service.user;

import org.hotiver.common.Enum.RoleType;
import org.hotiver.common.Enum.SellerRegisterRequestStatus;
import org.hotiver.common.Exception.base.EntityAlreadyExistsException;
import org.hotiver.domain.Entity.Role;
import org.hotiver.domain.Entity.SellerRegister;
import org.hotiver.domain.Entity.User;
import org.hotiver.domain.security.SecurityUser;
import org.hotiver.dto.admin.SellerRegisterResponse;
import org.hotiver.dto.seller.SellerRegisterDto;
import org.hotiver.repo.RoleRepo;
import org.hotiver.repo.SellerRegisterRepo;
import org.hotiver.repo.SellerRepo;
import org.hotiver.repo.UserRepo;
import org.hotiver.service.chat.ChatService;
import org.hotiver.service.common.CurrentUserService;
import org.hotiver.service.email.EmailService;
import org.hotiver.service.user.SellerRegisterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.*;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SellerRegisterServiceTest {

    @Mock
    private SellerRegisterRepo sellerRegisterRepo;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private SellerRepo sellerRepo;

    @Mock
    private UserRepo userRepo;

    @Mock
    private RoleRepo roleRepo;

    @Mock
    private ChatService chatService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private SellerRegisterService sellerRegisterService;

    private List<SellerRegisterResponse> sellerRegisterResponseList;

    private SellerRegisterDto sellerRegisterDto;

    private User user;

    @BeforeEach
    public void setUp() {
        sellerRegisterResponseList = new ArrayList<>();
        sellerRegisterResponseList.add(
                new SellerRegisterResponse(1L, "test1",
                        "test1", "test1")
        );
        sellerRegisterResponseList.add(
                new SellerRegisterResponse(2L, "test2",
                        "test2", "test2")
        );

        sellerRegisterDto = new SellerRegisterDto(
                "test",
                "test",
                "test");

        user = new User();
        user.setId(1L);
        user.setRoles(new ArrayList<>(List.of(
                new Role(1L, RoleType.USER)
        )));
    }

    @Test
    public void get_seller_register_requests() {
        when(sellerRegisterRepo.findByStatus(SellerRegisterRequestStatus.ACTIVE))
                .thenReturn(sellerRegisterResponseList);

        var response = sellerRegisterService.getSellerRegisterRequests();

        assertEquals(sellerRegisterResponseList.size(), response.size());
    }

    @Test
    public void get_seller_register_requests_empty() {
        when(sellerRegisterRepo.findByStatus(SellerRegisterRequestStatus.ACTIVE))
                .thenReturn(Collections.emptyList());

        var response = sellerRegisterService.getSellerRegisterRequests();

        assertEquals(0, response.size());
    }

    @Test
    public void send_seller_register_request() {
        when(currentUserService.getUserPrincipal())
                .thenReturn(new SecurityUser(user));

        sellerRegisterService.sendSellerRegisterRequest(sellerRegisterDto);

        verify(sellerRegisterRepo, times(1))
                .save(any(SellerRegister.class));
    }

    @Test
    public void send_seller_register_request_invalid_nickname() {
        sellerRegisterDto.setRequestedNickname("!!!");

        assertThrows(IllegalArgumentException.class, ()-> {
            sellerRegisterService.sendSellerRegisterRequest(sellerRegisterDto);
        });

        verify(sellerRegisterRepo, never()).save(any());
    }

    @Test
    public void send_seller_register_request_seller_with_nickname_exists() {
        when(sellerRepo.existsByNickname(anyString())).thenReturn(true);

        assertThrows(EntityAlreadyExistsException.class, ()-> {
            sellerRegisterService.sendSellerRegisterRequest(sellerRegisterDto);
        });

        verify(sellerRegisterRepo, never()).save(any());
    }

    @Test
    public void send_seller_register_request_already_seller() {
        when(currentUserService.getUserPrincipal())
                .thenReturn(new SecurityUser(user));

        when(sellerRepo.existsById(anyLong())).thenReturn(true);

        assertThrows(EntityAlreadyExistsException.class, ()-> {
            sellerRegisterService.sendSellerRegisterRequest(sellerRegisterDto);
        });

        verify(sellerRegisterRepo, never()).save(any());
    }

    @Test
    public void send_seller_register_request_already_sent_request_today() {
        when(currentUserService.getUserPrincipal())
                .thenReturn(new SecurityUser(user));

        when(sellerRegisterRepo.existsByUserIdAndRequestDate(eq(1L), any(Date.class)))
                .thenReturn(true);

        assertThrows(EntityAlreadyExistsException.class, ()-> {
            sellerRegisterService.sendSellerRegisterRequest(sellerRegisterDto);
        });

        verify(sellerRegisterRepo, never()).save(any());
    }

    @Test
    public void send_seller_register_request_already_sent_request() {
        when(currentUserService.getUserPrincipal())
                .thenReturn(new SecurityUser(user));

        when(sellerRegisterRepo.existsByUserIdAndStatus(1L, SellerRegisterRequestStatus.ACTIVE))
                .thenReturn(true);

        assertThrows(EntityAlreadyExistsException.class, ()-> {
            sellerRegisterService.sendSellerRegisterRequest(sellerRegisterDto);
        });

        verify(sellerRegisterRepo, never()).save(any());
    }

    @Test
    public void accept_seller_register_request() {
        SellerRegister sellerRegister = new SellerRegister();
        sellerRegister.setUserId(1L);
        sellerRegister.setRequestedNickname("test");

        when(sellerRegisterRepo.findById(1L)).thenReturn(Optional.of(sellerRegister));
        when(userRepo.findById(1L)).thenReturn(Optional.of(user));
        when(roleRepo.findById(3L)).thenReturn(Optional.of(new Role(3L, RoleType.SELLER)));
        when(sellerRepo.existsByNickname("test")).thenReturn(false);

        sellerRegisterService.acceptSellerRegisterRequest(1L);

        verify(sellerRepo, times(1)).save(any());
        verify(sellerRegisterRepo, times(1)).save(any());
    }

    @Test
    public void accept_seller_register_request_nickname_already_exists() {
        SellerRegister sellerRegister = new SellerRegister();
        sellerRegister.setUserId(1L);
        sellerRegister.setRequestedNickname("test");

        when(sellerRegisterRepo.findById(1L)).thenReturn(Optional.of(sellerRegister));
        when(userRepo.findById(1L)).thenReturn(Optional.of(user));
        when(sellerRepo.existsByNickname("test")).thenReturn(true);

        assertThrows(EntityAlreadyExistsException.class, ()-> {
            sellerRegisterService.acceptSellerRegisterRequest(1L);
        });

        verify(sellerRepo, never()).save(any());
        verify(sellerRegisterRepo, never()).save(any());
    }

    @Test
    public void decline_seller_register_request() {
        SellerRegister sellerRegister = new SellerRegister();
        sellerRegister.setUserId(1L);
        sellerRegister.setRequestedNickname("test");

        when(sellerRegisterRepo.findById(1L)).thenReturn(Optional.of(sellerRegister));
        when(userRepo.findById(1L)).thenReturn(Optional.of(user));

        sellerRegisterService.declineSellerRegisterRequest(1L);

        verify(sellerRegisterRepo, times(1)).save(any());
    }

}

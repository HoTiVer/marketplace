package org.hotiver.app.service.chat;

import jakarta.persistence.EntityNotFoundException;
import org.hotiver.common.Enum.MessageStatus;
import org.hotiver.common.Enum.RoleType;
import org.hotiver.common.Exception.base.InvalidStateException;
import org.hotiver.domain.Entity.Chat;
import org.hotiver.domain.Entity.Message;
import org.hotiver.domain.Entity.Role;
import org.hotiver.domain.Entity.User;
import org.hotiver.domain.security.SecurityUser;
import org.hotiver.dto.chat.ChatDto;
import org.hotiver.dto.chat.SendMessageDto;
import org.hotiver.dto.chat.UpdateChatEvent;
import org.hotiver.dto.user.UserChatsDto;
import org.hotiver.repo.core.ChatRepo;
import org.hotiver.repo.core.MessageRepo;
import org.hotiver.repo.core.UserRepo;
import org.hotiver.repo.projection.ChatProjectionRepo;
import org.hotiver.repo.projection.MessageProjectionRepo;
import org.hotiver.service.chat.ChatService;
import org.hotiver.service.chat.ChatWsSender;
import org.hotiver.service.common.CurrentUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class ChatServiceTest {

    @Mock
    private UserRepo userRepo;

    @Mock
    private ChatRepo chatRepo;

    @Mock
    private ChatProjectionRepo chatProjectionRepo;

    @Mock
    private MessageRepo messageRepo;

    @Mock
    private MessageProjectionRepo messageProjectionRepo;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private ChatWsSender chatWsSender;

    @InjectMocks
    private ChatService chatService;

    private User currentUser;

    @BeforeEach
    public void setUp() {
        currentUser = new User();
        currentUser.setId(1L);
        currentUser.setRoles(new ArrayList<>(List.of(new Role(1L, RoleType.USER))));

    }

    @Nested
    class GetUserChats {

        List<UserChatsDto> userChats;

        @BeforeEach
        public void setUp() {
            userChats = new ArrayList<>();
            userChats.add(
                    new UserChatsDto(
                            1L,
                            "testChat",
                            "testMessage",
                            LocalDateTime.now()
                    )
            );
        }

        @Test
        void shouldReturnUserChats() {
            when(currentUserService.getUserPrincipal())
                    .thenReturn(new SecurityUser(currentUser));

            when(chatProjectionRepo.findUserChatsDtoByUserId(currentUser.getId()))
                .thenReturn(userChats);

            List<UserChatsDto> result = chatService.getUserChats();

            assertEquals(userChats.size(), result.size());
            assertEquals(userChats.get(0).chatId(), result.get(0).chatId());
        }

        @Test
        void shouldReturnEmptyUserChats() {
            when(currentUserService.getUserPrincipal())
                    .thenReturn(new SecurityUser(currentUser));

            when(chatProjectionRepo.findUserChatsDtoByUserId(currentUser.getId()))
                    .thenReturn(Collections.emptyList());

            List<UserChatsDto> result = chatService.getUserChats();

            assertEquals(0, result.size());
        }
    }

    @Nested
    class GetChat {

        private Long chatId;
        private ChatDto chat;

        @BeforeEach
        public void setUp() {
            chatId = 1L;

            chat = new ChatDto(
                    chatId,
                    "test",
                    2L,
                    false,
                    null,
                    Collections.emptyList()
            );
        }

        @Test
        void shouldReturnChat() {
            when(currentUserService.getUserPrincipal())
                    .thenReturn(new SecurityUser(currentUser));

            when(chatProjectionRepo.findChatDtoByChatId(chatId, currentUser.getId()))
                    .thenReturn(Optional.of(chat));

            when(messageProjectionRepo.findAllByChatIdOrderBySentAtAsc(chatId))
                    .thenReturn(Collections.emptyList());

            ChatDto result = chatService.getChat(chatId);

            assertEquals(chat.getChatId(), result.getChatId());
            assertEquals(chat.getChatName(), result.getChatName());
        }

        @Test
        void shouldThrowsException_whenChatNotFound() {
            when(currentUserService.getUserPrincipal())
                    .thenReturn(new SecurityUser(currentUser));

            when(chatProjectionRepo.findChatDtoByChatId(chatId, currentUser.getId()))
                    .thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> chatService.getChat(chatId)
            );
        }

    }

    @Nested
    class SendMessage {

        private SendMessageDto sendMessage;
        private SecurityUser senderPrincipal;
        private User receiver;

        @BeforeEach
        public void setUp() {
            sendMessage = new SendMessageDto(2L, "test message");

            senderPrincipal = new SecurityUser(currentUser);

            receiver = new User();
            receiver.setId(2L);
        }

        @Test
        void shouldSendMessage() {
            Chat chat = new Chat();
            chat.setId(1L);
            UpdateChatEvent updateChatEvent = new UpdateChatEvent(
                    chat.getId(),
                    999L,
                    1L,
                    "test",
                    MessageStatus.SENT,
                    LocalDateTime.now()
            );

            when(userRepo.findByEmail(senderPrincipal.getUsername()))
                    .thenReturn(Optional.of(currentUser));

            when(userRepo.findById(receiver.getId())).thenReturn(Optional.of(receiver));

            when(chatRepo.findChatByUsersIds(currentUser.getId(), receiver.getId()))
                    .thenReturn(Optional.of(chat));


            chatService.sendMessage(sendMessage, senderPrincipal);

            verify(messageRepo).save(any(Message.class));
            verify(chatRepo).save(chat);

            verify(chatWsSender).sendMessagesToUsers(
                    any(),
                    any(),
                    any()
            );
        }

        @Test
        void shouldThrowsException_whenYouTextToYourself() {
            sendMessage = new SendMessageDto(1L, "test message");

            assertThrows(InvalidStateException.class,
                    () -> chatService.sendMessage(sendMessage, senderPrincipal)
            );
        }

        @Test
        void shouldThrowsException_whenUserIdIsNotValid() {
            sendMessage = new SendMessageDto(-1L, "test message");

            assertThrows(InvalidStateException.class,
                    () -> chatService.sendMessage(sendMessage, senderPrincipal)
            );
        }

        @Test
        void shouldThrowsException_whenSenderNotFound() {
            when(userRepo.findByEmail(senderPrincipal.getUsername()))
                    .thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> chatService.sendMessage(sendMessage, senderPrincipal)
            );
        }

        @Test
        void shouldThrowsException_whenReceiverNotFound() {
            when(userRepo.findByEmail(senderPrincipal.getUsername()))
                    .thenReturn(Optional.of(currentUser));

            when(userRepo.findById(receiver.getId())).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> chatService.sendMessage(sendMessage, senderPrincipal)
            );
        }

    }

}

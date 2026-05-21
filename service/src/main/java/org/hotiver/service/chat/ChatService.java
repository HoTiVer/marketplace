package org.hotiver.service.chat;

import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.hotiver.common.Enum.MessageStatus;
import org.hotiver.common.Exception.base.InvalidStateException;
import org.hotiver.domain.Entity.Chat;
import org.hotiver.domain.Entity.Message;
import org.hotiver.domain.Entity.User;
import org.hotiver.domain.security.SecurityUser;
import org.hotiver.dto.chat.ChatDto;
import org.hotiver.dto.chat.ChatMessageProjection;
import org.hotiver.dto.chat.SendMessageDto;
import org.hotiver.dto.chat.UpdateChatEvent;
import org.hotiver.dto.user.UserChatsDto;
import org.hotiver.repo.ChatRepo;
import org.hotiver.repo.MessageRepo;
import org.hotiver.repo.SellerRepo;
import org.hotiver.repo.UserRepo;
import org.hotiver.service.common.CurrentUserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;


@Service
@AllArgsConstructor
public class ChatService {

    private final UserRepo userRepo;
    private final ChatRepo chatRepo;
    private final MessageRepo messageRepo;
    private final SellerRepo sellerRepo;
    private final CurrentUserService currentUserService;
    private final ChatWsSender chatWsSender;

    public List<UserChatsDto> getUserChats() {
        SecurityUser user = currentUserService.getUserPrincipal();

        return chatRepo.findUserChatsDtoByUserId(user.getId());
    }

    public ChatDto getChat(Long id) {
        SecurityUser user = currentUserService.getUserPrincipal();

        ChatDto chat = chatRepo.findChatDtoByChatId(id, user.getId())
                .orElseThrow(() -> new EntityNotFoundException("Chat not found"));

        List<ChatMessageProjection> messages = messageRepo.findAllByChatIdOrderBySentAtAsc(id);

        chat.setMessages(messages);
        return chat;
    }

    @Transactional
    public void sendMessage(Long senderId, Long receiverId, String message) {
        SendMessageDto sendMessageDto = new SendMessageDto(
                receiverId,
                message
        );
        User sender = userRepo.findById(senderId)
                .orElseThrow(()-> new EntityNotFoundException("Sender is not found"));

        SecurityUser user = new SecurityUser(sender);

        sendMessage(sendMessageDto, user);
    }

    @Transactional
    public void sendMessage(SendMessageDto sendMessageDto, SecurityUser senderPrincipal) {
        if (senderPrincipal.getId().equals(sendMessageDto.receiverId()))
            throw new InvalidStateException("You cannot write message to yourself");
        if (sendMessageDto.receiverId() < 1)
            throw new InvalidStateException("You cannot write message to this user");

        User sender = userRepo.findByEmail(senderPrincipal.getUsername())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        User receiver = userRepo.findById(sendMessageDto.receiverId())
                .orElseThrow(()-> new EntityNotFoundException("Receiver is not found"));
        Chat chat = chatRepo.findChatByUsersIds(sender.getId(), receiver.getId())
                .orElse(null);

        if (chat == null) {
            chat = createChat(sender, receiver);
            chatRepo.save(chat);
        }
        Message message = createMessage(chat, sendMessageDto.content(), sender);
        messageRepo.save(message);
        updateChatLastMessage(chat, message.getContent());
        chatRepo.save(chat);

        UpdateChatEvent updateChatEvent = createUpdateChatEvent(message, sender);
        chatWsSender.sendMessagesToUsers(updateChatEvent, sender.getEmail(), receiver.getEmail());
    }

    private UpdateChatEvent createUpdateChatEvent(Message message, User sender) {
        return new UpdateChatEvent(
                message.getChat().getId(),
                message.getId(),
                sender.getId(),
                message.getContent(),
                message.getStatus(),
                message.getSentAt()
        );
    }

    private Message createMessage(Chat chat, String messageContent, User sender) {
        return Message.builder()
                .chat(chat)
                .content(messageContent)
                .sender(sender)
                .sentAt(LocalDateTime.now())
                .status(MessageStatus.SENT)
                .build();
    }

    private Chat createChat(User sender, User receiver) {
        Chat chat = new Chat();

        chat.setUser1(sender);
        chat.setUser2(receiver);
        updateChatLastMessage(chat, "");

        return chat;
    }

    private void updateChatLastMessage(Chat chat, String content) {
        chat.setLastMessage(content);
        chat.setUpdatedAt(LocalDateTime.now());
    }
}

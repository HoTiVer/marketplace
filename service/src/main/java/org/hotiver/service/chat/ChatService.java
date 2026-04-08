package org.hotiver.service.chat;

import jakarta.persistence.EntityNotFoundException;
import org.hotiver.domain.Entity.Chat;
import org.hotiver.domain.Entity.Message;
import org.hotiver.domain.Entity.Seller;
import org.hotiver.domain.Entity.User;
import org.hotiver.dto.chat.ChatDto;
import org.hotiver.dto.chat.ChatMessageProjection;
import org.hotiver.dto.chat.SendMessageDto;
import org.hotiver.dto.user.UserChatsDto;
import org.hotiver.repo.ChatRepo;
import org.hotiver.repo.MessageRepo;
import org.hotiver.repo.SellerRepo;
import org.hotiver.repo.UserRepo;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;


@Service
public class ChatService {

    private final UserRepo userRepo;
    private final ChatRepo chatRepo;
    private final MessageRepo messageRepo;
    private final SellerRepo sellerRepo;

    public ChatService(UserRepo userRepo, ChatRepo chatRepo, MessageRepo messageRepo, SellerRepo sellerRepo) {
        this.userRepo = userRepo;
        this.chatRepo = chatRepo;
        this.messageRepo = messageRepo;
        this.sellerRepo = sellerRepo;
    }

    public List<UserChatsDto> getUserChats() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        return chatRepo.findUserChatsDtoByUserId(user.getId());
    }

    public ChatDto getChat(Long id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        ChatDto chat = chatRepo.findChatDtoByChatId(id, user.getId())
                .orElseThrow(() -> new EntityNotFoundException("Chat not found"));

        List<ChatMessageProjection> messages = messageRepo.findAllByChatIdOrderBySentAtAsc(id);

        chat.setMessages(messages);
        return chat;
    }

    public void sendMessage(Long chatId, SendMessageDto sendMessageDto) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        Chat chat = chatRepo.findById(chatId)
                .orElseThrow(()-> new EntityNotFoundException("Chat is not found"));

        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (chat.getUser1().equals(user) || chat.getUser2().equals(user)) {

            Message message = Message.builder()
                    .chat(chat)
                    .sender(user)
                    .sentAt(LocalDateTime.now())
                    .content(sendMessageDto.getContent())
                    .build();

            messageRepo.save(message);
        }
    }

    public void sendMessage(Long senderId, Long receiverId, String message) {
        if (Objects.equals(receiverId, senderId) || message == null){
            return;
        }
        Chat chat = chatRepo.findChatByUsersIds(senderId, receiverId);
        User sender = userRepo.findById(senderId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (chat == null){
            chat = new Chat();
            User receiver = userRepo.findById(receiverId)
                    .orElseThrow(() -> new EntityNotFoundException("User not found"));
            chat.setUser1(sender);
            chat.setUser2(receiver);
            chatRepo.save(chat);
        }

        Message messageToSave = Message.builder()
                .chat(chat)
                .content(message)
                .sender(sender)
                .sentAt(LocalDateTime.now())
                .build();

        messageRepo.save(messageToSave);
    }

    public void sendMessageToSeller(String sellerNickName, SendMessageDto message) {
        String senderEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        User sender = userRepo.findByEmail(senderEmail)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Seller seller = sellerRepo.findByNickname(sellerNickName)
                .orElseThrow(() -> new EntityNotFoundException("Seller not found"));

        if (sender.getId().equals(seller.getId())) {
            return;
        }

        Chat chat = chatRepo.findChatByUsersIds(sender.getId(), seller.getId());

        if (chat == null){
            chat = new Chat();
            User receiver = userRepo.findById(seller.getId())
                    .orElseThrow(() -> new EntityNotFoundException("User not found"));

            chat.setUser1(sender);
            chat.setUser2(receiver);
            chatRepo.save(chat);
        }

        Message messageToSave = Message.builder()
                .chat(chat)
                .content(message.getContent())
                .sender(sender)
                .sentAt(LocalDateTime.now())
                .build();

        messageRepo.save(messageToSave);
    }
}

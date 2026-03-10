package org.hotiver.service;

import jakarta.persistence.EntityNotFoundException;
import org.hotiver.common.Exception.SellerNotFoundException;
import org.hotiver.domain.Entity.Chat;
import org.hotiver.domain.Entity.Message;
import org.hotiver.domain.Entity.Seller;
import org.hotiver.domain.Entity.User;
import org.hotiver.dto.chat.ChatDto;
import org.hotiver.dto.chat.ChatMessageDto;
import org.hotiver.dto.chat.SendMessageDto;
import org.hotiver.dto.user.UserChatsDto;
import org.hotiver.repo.ChatRepo;
import org.hotiver.repo.MessageRepo;
import org.hotiver.repo.SellerRepo;
import org.hotiver.repo.UserRepo;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

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

        List<Chat> userChats = chatRepo.findChatsByUserId(user.getId());
        return getUserChatsDto(userChats, user.getId());
    }

    //TODO OPTIMIZE
    private List<UserChatsDto> getUserChatsDto(List<Chat> userChats, Long userId) {
        List<UserChatsDto> returnedChats = new ArrayList<>();

        for (var chat : userChats) {
            UserChatsDto userChatsDto = new UserChatsDto();
            userChatsDto.setChatId(chat.getId());
            if (Objects.equals(chat.getUser1().getId(), userId)) {
                userChatsDto.setName(chat.getUser2().getDisplayName());
            }
            else {
                userChatsDto.setName(chat.getUser1().getDisplayName());
            }
            returnedChats.add((userChatsDto));
        }
        return returnedChats;
    }

    public ChatDto getChat(Long id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Chat chat = chatRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Chat not found"));

        List<Message> messages = messageRepo.findAllByChatOrderBySentAtAsc(chat);

        String chatName;
        boolean isSeller;
        Long chatNameUserId;

        if (chat.getUser1().getId().equals(user.getId())) {
            chatNameUserId = chat.getUser2().getId();
            isSeller = sellerRepo.existsById(chatNameUserId);
            if (isSeller) {
                Seller seller = sellerRepo.findById(chatNameUserId)
                        .orElseThrow(() -> new SellerNotFoundException("Seller not found"));
                chatName = seller.getNickname();
            }
            else
                chatName = chat.getUser2().getDisplayName();
        }
        else {
            chatNameUserId = chat.getUser1().getId();
            isSeller = sellerRepo.existsById(chatNameUserId);
            if (isSeller) {
                Seller seller = sellerRepo.findById(chatNameUserId)
                        .orElseThrow(() -> new SellerNotFoundException("Seller not found"));
                chatName = seller.getNickname();
            }
            else
                chatName = chat.getUser1().getDisplayName();
        }

        List<ChatMessageDto> messagesDto = messages.stream().map(m -> ChatMessageDto.builder()
                .id(m.getId())
                .senderId(m.getSender().getId())
                .senderName(m.getSender().getDisplayName())
                .content(m.getContent())
                .sentAt(m.getSentAt())
                .build()
        ).toList();

        return ChatDto.builder()
                .chatId(chat.getId())
                .chatName(chatName)
                .isSeller(isSeller)
                .messages(messagesDto)
                .build();
    }

    public void sendMessage(Long chatId, SendMessageDto sendMessageDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        Chat chat = chatRepo.findById(chatId)
                .orElseThrow(()-> new EntityNotFoundException("Chat is not found"));

        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (chat.getUser1().equals(user) || chat.getUser2().equals(user)){

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

        Message messageObj = Message.builder()
                .chat(chat)
                .content(message)
                .sender(sender)
                .sentAt(LocalDateTime.now())
                .build();

        messageRepo.save(messageObj);
    }

    public void sendMessageToSeller(String sellerNickName, SendMessageDto message) {
        String senderEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        User sender = userRepo.findByEmail(senderEmail)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        Optional<Seller> seller = sellerRepo.findByNickname(sellerNickName);

        if (seller.isEmpty()) {
            throw new EntityNotFoundException("Seller is not found");
        }

        if (sender.getId().equals(seller.get().getId())) {
            throw new RuntimeException();
        }

        Chat chat = chatRepo.findChatByUsersIds(sender.getId(), seller.get().getId());

        if (chat == null){
            chat = new Chat();
            User receiver = userRepo.findById(seller.get().getId())
                    .orElseThrow(() -> new EntityNotFoundException("User not found"));

            chat.setUser1(sender);
            chat.setUser2(receiver);
            chatRepo.save(chat);
        }

        Message messageObj = Message.builder()
                .chat(chat)
                .content(message.getContent())
                .sender(sender)
                .sentAt(LocalDateTime.now())
                .build();

        messageRepo.save(messageObj);
    }
}

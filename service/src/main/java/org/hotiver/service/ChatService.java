package org.hotiver.service;

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
import org.springframework.http.ResponseEntity;
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
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User user = userRepo.findByEmail(email).get();
        Long userId = user.getId();

        List<Chat> userChats = chatRepo.findChatsByUserId(userId);
        return getUserChatsDto(userChats, userId);
    }

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

    public ResponseEntity<ChatDto> getChat(Long id) {
        Optional<Chat> chatOptional = chatRepo.findById(id);
        if (chatOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Chat chat = chatOptional.get();

        List<Message> messages = messageRepo.findAllByChatOrderBySentAtAsc(chat);

        List<ChatMessageDto> messageDtos = messages.stream().map(m -> ChatMessageDto.builder()
                .id(m.getId())
                .senderId(m.getSender().getId())
                .senderName(m.getSender().getDisplayName())
                .content(m.getContent())
                .sentAt(m.getSentAt())
                .build()
        ).toList();

        ChatDto chatDto = ChatDto.builder()
                .chatId(chat.getId())
                .participantIds(List.of(chat.getUser1().getId(), chat.getUser2().getId()))
                .messages(messageDtos)
                .build();

        return ResponseEntity.ok(chatDto);
    }

    public ResponseEntity<?> sendMessage(Long chatId, SendMessageDto sendMessageDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        Optional<Chat> optionalChat = chatRepo.findById(chatId);

        if (optionalChat.isEmpty()){
            return ResponseEntity.notFound().build();
        }

        User user = userRepo.findByEmail(email).get();
        Chat chat = optionalChat.get();
        if (chat.getUser1().equals(user) || chat.getUser2().equals(user)){

            Message message = Message.builder()
                    .chat(chat)
                    .sender(user)
                    .sentAt(LocalDateTime.now())
                    .content(sendMessageDto.getContent())
                    .build();

            messageRepo.save(message);

            return ResponseEntity.ok().build();
        }

        return ResponseEntity.badRequest().build();
    }

    public void sendMessage(Long senderId, Long receiverId, String message) {
        if (Objects.equals(receiverId, senderId) || message == null){
            return;
        }
        Chat chat = chatRepo.findChatByUsersIds(senderId, receiverId);
        Optional<User> sender = userRepo.findById(senderId);

        if (chat == null){
            chat = new Chat();
            var receiver = userRepo.findById(receiverId);
            chat.setUser1(sender.get());
            chat.setUser2(receiver.get());
            chatRepo.save(chat);
        }

        Message messageObj = Message.builder()
                .chat(chat)
                .content(message)
                .sender(sender.get())
                .sentAt(LocalDateTime.now())
                .build();

        messageRepo.save(messageObj);
    }

    public ResponseEntity<?> sendMessageToSeller(String sellerNickName, SendMessageDto message) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        String senderEmail = auth.getName();
        Optional<User> sender = userRepo.findByEmail(senderEmail);
        Optional<Seller> seller = sellerRepo.findByNickname(sellerNickName);

        if (seller.isEmpty() || sender.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        if (sender.get().getId().equals(seller.get().getId())) {
            return ResponseEntity.badRequest().build();
        }

        Chat chat = chatRepo.findChatByUsersIds(sender.get().getId(), seller.get().getId());

        if (chat == null){
            chat = new Chat();
            var receiver = userRepo.findById(seller.get().getId());
            chat.setUser1(sender.get());
            chat.setUser2(receiver.get());
            chatRepo.save(chat);
        }

        Message messageObj = Message.builder()
                .chat(chat)
                .content(message.getContent())
                .sender(sender.get())
                .sentAt(LocalDateTime.now())
                .build();

        messageRepo.save(messageObj);

        return ResponseEntity.ok().build();
    }
}

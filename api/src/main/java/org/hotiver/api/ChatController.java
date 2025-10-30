package org.hotiver.api;

import org.hotiver.dto.chat.ChatDto;
import org.hotiver.dto.chat.SendMessageDto;
import org.hotiver.dto.user.UserChatsDto;
import org.hotiver.service.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/cabinet/message")
    public List<UserChatsDto> getUserChats(){
        return chatService.getUserChats();
    }

    @GetMapping("/cabinet/message/{id}")
    public ResponseEntity<ChatDto> getChat(@PathVariable Long id){
        return chatService.getChat(id);
    }

    @PostMapping("/cabinet/message/{id}")
    public ResponseEntity<?> sendMessage(@PathVariable Long id, @RequestBody SendMessageDto sendMessageDto) {
        return chatService.sendMessage(id, sendMessageDto);
    }

}

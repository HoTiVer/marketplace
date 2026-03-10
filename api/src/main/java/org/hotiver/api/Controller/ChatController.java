package org.hotiver.api.Controller;

import jakarta.validation.Valid;
import org.hotiver.dto.chat.ChatDto;
import org.hotiver.dto.chat.SendMessageDto;
import org.hotiver.dto.user.UserChatsDto;
import org.hotiver.service.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/message")
    public List<UserChatsDto> getUserChats(){
        return chatService.getUserChats();
    }

    @GetMapping("/message/{id}")
    public ResponseEntity<ChatDto> getChat(@PathVariable Long id){
        ChatDto chatDto = chatService.getChat(id);
        return ResponseEntity.ok(chatDto);
    }

    @PostMapping("/message/{id}")
    public ResponseEntity<Void> sendMessage(@PathVariable Long id,
                                            @RequestBody @Valid SendMessageDto sendMessageDto) {
        chatService.sendMessage(id, sendMessageDto);
        return ResponseEntity.ok().build();
    }

}

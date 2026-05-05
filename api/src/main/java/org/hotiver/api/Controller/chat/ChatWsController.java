package org.hotiver.api.Controller.chat;

import jakarta.validation.Valid;
import org.hotiver.dto.chat.SendMessageDto;
import org.hotiver.service.chat.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
public class ChatWsController {

    private final ChatService chatService;
    private final SimpMessagingTemplate simpMessagingTemplate;

    public ChatWsController(ChatService chatService, SimpMessagingTemplate simpMessagingTemplate) {
        this.chatService = chatService;
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    @MessageMapping("/chat.sent")
    public void sendMessage(@Payload SendMessageDto sendMessageDto){
        
    }

//    @PostMapping("/{id}/message")
//    public ResponseEntity<Void> sendMessage(@PathVariable Long id,
//                                            @RequestBody @Valid SendMessageDto sendMessageDto) {
//        MessageDto = chatService.sendMessage(id, sendMessageDto);
//        return ResponseEntity.ok().build();
//    }

//    @PostMapping("/seller/message/{username}")
//    public ResponseEntity<Void> sendMessageToSeller(@PathVariable String username,
//                                                    @RequestBody @Valid SendMessageDto message) {
//        chatService.sendMessageToSeller(username, message);
//        return ResponseEntity.ok().build();
//    }

}

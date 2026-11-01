package org.hotiver.api.Controller.chat;

import org.hotiver.domain.security.SecurityUser;
import org.hotiver.dto.chat.SendMessageDto;
import org.hotiver.service.chat.ChatService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class ChatWsController {
    private final ChatService chatService;

    public ChatWsController(ChatService chatService) {
        this.chatService = chatService;
    }

    @MessageMapping("/chat.sent")
    public void sendMessage(@Payload SendMessageDto sendMessageDto,
                            Principal principal) {
        Authentication auth = (Authentication) principal;
        SecurityUser sender = (SecurityUser) auth.getPrincipal();
        chatService.sendMessage(sendMessageDto, sender);
    }
}

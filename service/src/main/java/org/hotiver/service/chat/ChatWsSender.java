package org.hotiver.service.chat;

import lombok.AllArgsConstructor;
import org.hotiver.dto.chat.UpdateChatEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Service
@Profile("!test")
@AllArgsConstructor
public class ChatWsSender {

    private final SimpMessagingTemplate simpMessagingTemplate;

    public void sendMessagesToUsers(UpdateChatEvent updateChatEvent,
                                    String senderEmail, String receiverEmail) {
        simpMessagingTemplate.convertAndSendToUser(
                senderEmail,
                "/queue/chats",
                updateChatEvent);

        simpMessagingTemplate.convertAndSendToUser(
                receiverEmail,
                "/queue/chats",
                updateChatEvent);
    }
}

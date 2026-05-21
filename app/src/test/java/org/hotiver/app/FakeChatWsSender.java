package org.hotiver.app;

import org.hotiver.dto.chat.UpdateChatEvent;
import org.hotiver.service.chat.ChatWsSender;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("test")
public class FakeChatWsSender extends ChatWsSender {

    public FakeChatWsSender() {
        super(null);
    }

    @Override
    public void sendMessagesToUsers(UpdateChatEvent updateChatEvent,
                                    String senderEmail, String receiverEmail) {

    }
}

package org.hotiver.dto.chat;

import java.time.LocalDateTime;

public interface ChatMessageProjection {

    Long getId();

    Long getSenderId();

    String getSenderName();

    String getContent();

    LocalDateTime getSentAt();
}

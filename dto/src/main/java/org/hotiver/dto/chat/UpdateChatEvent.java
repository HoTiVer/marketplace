package org.hotiver.dto.chat;

import org.hotiver.common.Enum.MessageStatus;

import java.time.LocalDateTime;

public record UpdateChatEvent (
    Long chatId,
    Long messageId,
    Long senderId,
    String content,
    MessageStatus status,
    LocalDateTime sentAt
) {}

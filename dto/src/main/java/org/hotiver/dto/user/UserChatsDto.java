package org.hotiver.dto.user;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

public record UserChatsDto(
        Long chatId,
        String name,
        String lastMessage,
        LocalDateTime updatedAt
) { }

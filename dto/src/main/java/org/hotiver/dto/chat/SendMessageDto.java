package org.hotiver.dto.chat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SendMessageDto(
        @NotNull
        Long receiverId,
        @NotBlank(message = "Message must contains something")
        String content
) {}

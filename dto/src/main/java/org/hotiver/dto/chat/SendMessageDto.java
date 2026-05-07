package org.hotiver.dto.chat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SendMessageDto {
    @NotNull
    private Long receiverId;
    @NotBlank(message = "Message must contains something")
    private String content;
}

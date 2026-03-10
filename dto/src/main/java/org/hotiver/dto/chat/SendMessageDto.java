package org.hotiver.dto.chat;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SendMessageDto {
    @NotBlank(message = "Message must contains something")
    String content;
}

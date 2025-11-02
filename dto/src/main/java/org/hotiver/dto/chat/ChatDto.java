package org.hotiver.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatDto {
    private Long chatId;
    private String chatName;
    private Boolean isSeller;
    //private List<Long> participantIds;
    private List<ChatMessageDto> messages;
}


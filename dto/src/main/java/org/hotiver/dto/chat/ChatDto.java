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
    private Long receiverId;
    private Boolean isSeller;
    private String sellerUsername;
    private List<ChatMessageProjection> messages;

    public ChatDto(Long chatId, String chatName, Long receiverId,
                   Boolean isSeller, String sellerUsername) {
        this.chatId = chatId;
        this.chatName = chatName;
        this.receiverId = receiverId;
        this.isSeller = isSeller;
        this.sellerUsername = sellerUsername;
    }
}


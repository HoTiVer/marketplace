package org.hotiver.repo;

import org.hotiver.domain.Entity.Message;
import org.hotiver.domain.Entity.Chat;
import org.hotiver.dto.chat.ChatMessageProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MessageRepo extends JpaRepository<Message, Long> {

    List<Message> findAllByChatOrderBySentAtAsc(Chat chat);

    @Query(value =
    """
    SELECT
        m.id as id,
        m.sender.id as senderId,
        m.sender.displayName as senderName,
        m.content as content,
        m.sentAt as sentAt
    FROM Message m
    WHERE m.chat.id = :chatId
    ORDER BY m.sentAt
    """)
    List<ChatMessageProjection> findAllByChatIdOrderBySentAtAsc(Long chatId);

}

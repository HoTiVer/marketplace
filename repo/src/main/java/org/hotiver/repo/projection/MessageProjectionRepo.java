package org.hotiver.repo.projection;

import org.hotiver.domain.Entity.Message;
import org.hotiver.dto.chat.ChatMessageProjection;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.List;

public interface MessageProjectionRepo extends Repository<Message, Long> {

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

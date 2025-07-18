package org.hotiver.repo;

import org.hotiver.domain.Entity.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ChatRepo extends JpaRepository<Chat, Long> {

    @Query("SELECT c FROM Chat c WHERE c.user1.id = :id or c.user2.id = :id")
    List<Chat> findChatsByUserId(Long id);

    @Query("""
    SELECT c FROM Chat c
    WHERE (c.user1.id = :senderId AND c.user2.id = :receiverId)
       OR (c.user1.id = :receiverId AND c.user2.id = :senderId)
    """)
    Chat findChatByUsersIds(Long senderId, Long receiverId);
}

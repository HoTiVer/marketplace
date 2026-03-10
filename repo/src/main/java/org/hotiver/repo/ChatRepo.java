package org.hotiver.repo;

import org.hotiver.domain.Entity.Chat;
import org.hotiver.dto.user.UserChatsDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ChatRepo extends JpaRepository<Chat, Long> {

    @Query(
        value = """
        SELECT
            c.id as chatId,
            CASE
                WHEN c.user1_id = :id THEN u2.display_name
                ELSE u1.display_name
                    END as name
        FROM Chat c
        JOIN public.User u1 on c.user1_id = u1.id
        JOIN public.User u2 on c.user2_id = u2.id
        WHERE c.user1_id = :id or c.user2_id = :id
    """, nativeQuery = true)
    List<UserChatsDto> findUserChatsDtoByUserId(Long id);

    @Query("""
    SELECT c FROM Chat c
    WHERE (c.user1.id = :senderId AND c.user2.id = :receiverId)
       OR (c.user1.id = :receiverId AND c.user2.id = :senderId)
    """)
    Chat findChatByUsersIds(Long senderId, Long receiverId);
}

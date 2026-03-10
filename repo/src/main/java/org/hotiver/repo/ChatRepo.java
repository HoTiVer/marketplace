package org.hotiver.repo;

import org.hotiver.domain.Entity.Chat;
import org.hotiver.dto.chat.ChatDto;
import org.hotiver.dto.user.UserChatsDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ChatRepo extends JpaRepository<Chat, Long> {

    @Query(
        value = """
        SELECT
            c.id as chatId,
            CASE
                WHEN c.user1_id = :userId THEN u2.display_name
                ELSE u1.display_name
                    END as name
        FROM Chat c
        JOIN public.User u1 on c.user1_id = u1.id
        JOIN public.User u2 on c.user2_id = u2.id
        WHERE c.user1_id = :userId or c.user2_id = :userId
    """, nativeQuery = true)
    List<UserChatsDto> findUserChatsDtoByUserId(Long userId);

    @Query(value = """
    SELECT
        c.id,
        CASE
            WHEN c.user1_id = :userId THEN u2.display_name
            ELSE u1.display_name
            END as chatName,
        CASE
            WHEN c.user1_id = :userId THEN s2 IS NOT NULL
            ELSE s1 IS NOT NULL
            END as isSeller,
        CASE
            WHEN c.user1_id = :userId THEN s2.nickname
            ELSE s1.nickname
            END as sellerUserName
    FROM chat c
    JOIN public.user u1 on c.user1_id = u1.id
    JOIN public.user u2 on c.user2_id = u2.id
    LEFT JOIN seller s1 ON s1.id = u1.id
    LEFT JOIN seller s2 ON s2.id = u2.id
    WHERE c.id = :chatId
    """, nativeQuery = true)
    Optional<ChatDto> findChatDtoByChatId(Long chatId, Long userId);

    @Query("""
    SELECT c FROM Chat c
    WHERE (c.user1.id = :senderId AND c.user2.id = :receiverId)
       OR (c.user1.id = :receiverId AND c.user2.id = :senderId)
    """)
    Chat findChatByUsersIds(Long senderId, Long receiverId);
}

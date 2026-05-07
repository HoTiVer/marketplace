package org.hotiver.repo;

import org.hotiver.domain.Entity.Chat;
import org.hotiver.dto.chat.ChatDto;
import org.hotiver.dto.user.UserChatsDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ChatRepo extends JpaRepository<Chat, Long> {

    @Query("""
    SELECT
        c.id,
        CASE WHEN c.user1.id = :userId THEN u2.displayName ELSE u1.displayName END,
        c.lastMessage,
        c.updatedAt
    FROM Chat c
        JOIN c.user1 u1
        JOIN c.user2 u2
    WHERE c.user1.id = :userId OR c.user2.id = :userId
    ORDER BY c.updatedAt DESC
""")
    List<UserChatsDto> findUserChatsDtoByUserId(Long userId);

    @Query(value = """
    SELECT
        c.id,
        CASE
            WHEN c.user1_id = :userId THEN u2.display_name
            ELSE u1.display_name
            END as chatName,
        CASE
            WHEN c.user1_id = :userId THEN u2.id
            ELSE u1.id
            END as receiverId,
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
    Optional<Chat> findChatByUsersIds(Long senderId, Long receiverId);
}

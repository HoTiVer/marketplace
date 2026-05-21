package org.hotiver.domain.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "chat")
public class Chat {
    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "sequence-chat"
    )
    @SequenceGenerator(
            name = "sequence-chat",
            sequenceName = "sequence_chat",
            allocationSize = 5
    )
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user1;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user2;

    private String lastMessage;

    private LocalDateTime updatedAt;
}

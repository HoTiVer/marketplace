package org.hotiver.domain.Entity;


import jakarta.persistence.*;
import lombok.*;
import org.hotiver.common.Enum.MessageStatus;

import java.time.LocalDateTime;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "message")
public class Message {
    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "sequence-message"
    )
    @SequenceGenerator(
            name = "sequence-message",
            sequenceName = "sequence_message",
            allocationSize = 5
    )
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Chat chat;

    @ManyToOne(fetch = FetchType.LAZY)
    private User sender;

    private String content;

    private LocalDateTime sentAt;

    @Enumerated(EnumType.STRING)
    private MessageStatus status;
}

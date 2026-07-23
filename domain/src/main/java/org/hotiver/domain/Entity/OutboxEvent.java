package org.hotiver.domain.Entity;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;
import org.hotiver.common.Enum.OutboxEventType;
import org.hotiver.common.Enum.OutboxStatus;

import java.time.Instant;
import java.util.UUID;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "outbox_event")
public class OutboxEvent {

    @Id
    private UUID id;
    
    private String aggregateType;

    private Long aggregateId;

    @Enumerated(EnumType.STRING)
    private OutboxEventType eventType;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private String payload;

    @Enumerated(EnumType.STRING)
    private OutboxStatus status;

    private Instant createdAt;

    private Instant publishedAt;
}

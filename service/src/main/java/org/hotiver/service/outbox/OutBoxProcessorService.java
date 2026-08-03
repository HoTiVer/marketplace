package org.hotiver.service.outbox;

import jakarta.transaction.Transactional;
import org.hotiver.common.Enum.OutboxEventType;
import org.hotiver.common.Enum.OutboxStatus;
import org.hotiver.domain.Entity.OutboxEvent;
import org.hotiver.repo.core.OutboxEventRepo;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@ConditionalOnProperty(
        name = "outbox.processor.enabled",
        havingValue = "true"
)
public class OutBoxProcessorService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final OutboxEventRepo outboxEventRepo;

    public OutBoxProcessorService(KafkaTemplate<String, String> kafkaTemplate,
                                  OutboxEventRepo outboxEventRepo) {
        this.kafkaTemplate = kafkaTemplate;
        this.outboxEventRepo = outboxEventRepo;
    }

    @Scheduled(fixedDelay = 2000, initialDelay = 5000)
    @Transactional
    public void process() {
        List<OutboxEvent> events =
                outboxEventRepo.findTop100ByStatusOrderByCreatedAtAsc(
                        OutboxStatus.NEW);

        for (OutboxEvent event : events) {
            try {
                kafkaTemplate.send(
                        getTopicName(event.getEventType()),
                        event.getAggregateId().toString(),
                        event.getPayload()
                );
                event.setStatus(OutboxStatus.PUBLISHED);
                event.setPublishedAt(Instant.now());
            } catch (Exception ex) {
                event.setStatus(OutboxStatus.FAILED);
            }
        }
    }

    private String getTopicName(OutboxEventType type) {
        return switch (type) {
            case ORDER_ACCEPTED -> "order-accepted-event";
        };
    }
}

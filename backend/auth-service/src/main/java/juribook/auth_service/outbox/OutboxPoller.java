package juribook.auth_service.outbox;

import juribook.auth_service.events.KafkaEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Drain l'outbox vers Kafka.
 *
 * Pattern : fixed delay (pas fixed rate). Le tour N+1 démarre 2s APRÈS la
 * fin du tour N, ce qui évite l'empilement si un batch prend du temps.
 *
 * Garanties :
 *   - At-least-once : si Kafka acquitte, on UPDATE published_at. Si crash
 *     entre les deux, l'event sera re-publié au tour suivant → idempotence
 *     côté consumer requise (via eventId).
 *   - Pas de perte : tant qu'un event n'est pas marqué published_at, il
 *     reste candidat à la prochaine itération.
 *   - Pas de double publication concurrente : un seul scheduler thread.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxPoller {

    private final OutboxRepository outboxRepository;
    private final KafkaEventPublisher kafkaEventPublisher;
    private final OutboxProperties outboxProperties;

    @Scheduled(fixedDelayString = "${juribook.outbox.poll-interval-ms:2000}")
    @Transactional
    public void drainOutbox() {
        List<OutboxEvent> batch = outboxRepository.findUnpublishedBatch(
            PageRequest.of(0, outboxProperties.batchSize())
        );

        if (batch.isEmpty()) {
            return;  // pas de log, sinon spam toutes les 2s
        }

        log.info("Poller: traitement de {} event(s) outbox", batch.size());

        for (OutboxEvent event : batch) {
            try {
                publishAndMark(event);
            } catch (Exception e) {
                handleFailure(event, e);
            }
        }
    }

    private void publishAndMark(OutboxEvent event) {
        // Publication synchrone : on attend la confirmation Kafka avant de
        // marquer l'event comme publié. Évite la course "marqué publié mais
        // Kafka a fini par échouer".
        SendResult<String, Object> result = kafkaEventPublisher
            .publishLawyerRegistered(event.getPayload(), event.getAggregateId().toString())
            .join();  // bloquant — c'est ok dans un @Scheduled thread dédié

        event.setPublishedAt(OffsetDateTime.now());
        event.setLastError(null);
        outboxRepository.save(event);

        log.debug("Event {} publié sur Kafka (offset={})",
                  event.getId(),
                  result.getRecordMetadata().offset());
    }

    private void handleFailure(OutboxEvent event, Exception e) {
        event.setRetryCount(event.getRetryCount() + 1);
        event.setLastError(truncateError(e.getMessage()));
        outboxRepository.save(event);

        if (event.getRetryCount() >= outboxProperties.maxRetries()) {
            log.error("Event {} a atteint {} retries, marqué comme poison. " +
                      "Investigation manuelle requise. Cause: {}",
                      event.getId(), event.getRetryCount(), e.getMessage());
        } else {
            log.warn("Event {} échoué (retry {}/{}). Cause: {}",
                     event.getId(), event.getRetryCount(),
                     outboxProperties.maxRetries(), e.getMessage());
        }
    }

    private String truncateError(String msg) {
        if (msg == null) return "Unknown error";
        return msg.length() > 1000 ? msg.substring(0, 1000) : msg;
    }
}
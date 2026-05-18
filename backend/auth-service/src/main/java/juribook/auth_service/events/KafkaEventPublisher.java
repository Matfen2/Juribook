package juribook.auth_service.events;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import juribook.events.lawyer.LawyerRegisteredV1;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

/**
 * Publie les events sur Kafka via l'Apicurio Avro serializer.
 *
 * Pourquoi on ne réutilise pas le bytestream stocké en BDD :
 *   - En BDD : Avro brut (juste les champs sérialisés)
 *   - Sur Kafka : format Apicurio = [magic byte][globalId 8 bytes][Avro brut]
 * Donc on désérialise depuis BDD vers SpecificRecord, et Apicurio re-sérialise
 * en ajoutant son préfixe de routage vers Schema Registry.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaEventPublisher {

    private static final String LAWYER_EVENTS_TOPIC = "lawyer-events";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * Publie un event lawyer.registered. Retourne un Future pour permettre
     * au caller (poller) de réagir au succès/échec sans bloquer son thread.
     */
    public CompletableFuture<SendResult<String, Object>> publishLawyerRegistered(
            byte[] avroBytes,
            String partitionKey
    ) {
        try {
            // Désérialise les bytes Avro bruts vers le SpecificRecord typé.
            LawyerRegisteredV1 event = deserialize(avroBytes);

            // KafkaTemplate déclenche l'Apicurio serializer configuré dans
            // application.yaml : il ajoute magic byte + globalId au bytestream
            // et publie sur le topic. La clé de partitionnement = aggregateId.
            return kafkaTemplate.send(LAWYER_EVENTS_TOPIC, partitionKey, event);

        } catch (IOException e) {
            CompletableFuture<SendResult<String, Object>> failed = new CompletableFuture<>();
            failed.completeExceptionally(e);
            return failed;
        }
    }

    private LawyerRegisteredV1 deserialize(byte[] bytes) throws IOException {
        SpecificDatumReader<LawyerRegisteredV1> reader =
            new SpecificDatumReader<>(LawyerRegisteredV1.class);
        BinaryDecoder decoder = DecoderFactory.get().binaryDecoder(bytes, null);
        return reader.read(null, decoder);
    }
}
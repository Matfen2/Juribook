package juribook.auth_service.events;

import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.specific.SpecificRecord;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * Sérialise n'importe quel SpecificRecord Avro en bytes binaires.
 * Réutilisable pour tous les events (lawyer.registered, booking.created, etc.)
 * — évite de dupliquer le code EncoderFactory + DatumWriter à chaque event.
 */
@Component
public class AvroSerializer {

    public <T extends SpecificRecord> byte[] toBytes(T record) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            SpecificDatumWriter<T> writer =
                new SpecificDatumWriter<>(record.getSchema());
            BinaryEncoder encoder = EncoderFactory.get().binaryEncoder(out, null);
            writer.write(record, encoder);
            encoder.flush();
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException("Avro serialization failed", e);
        }
    }
}
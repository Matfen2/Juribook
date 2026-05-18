package juribook.lawyer_service.repository;

import juribook.lawyer_service.model.Lawyer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface LawyerRepository extends JpaRepository<Lawyer, UUID> {
    boolean existsByEmail(String email);
    boolean existsByBarNumber(String barNumber);
}
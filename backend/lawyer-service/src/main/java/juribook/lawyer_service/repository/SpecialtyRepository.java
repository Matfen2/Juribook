package juribook.lawyer_service.repository;

import juribook.lawyer_service.model.Specialty;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SpecialtyRepository extends JpaRepository<Specialty, Long> {
    Optional<Specialty> findByCode(String code);
}
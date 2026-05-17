package juribook.auth_service.repository;

import juribook.auth_service.model.entity.Specialty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SpecialtyRepository extends JpaRepository<Specialty, Long> {

    Optional<Specialty> findByCode(String code);
}
package juribook.auth_service.repository;

import juribook.auth_service.model.entity.Lawyer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface LawyerRepository extends JpaRepository<Lawyer, UUID> {

    boolean existsByBarNumber(String barNumber);
}
package juribook.lawyer_service.repository;

import juribook.lawyer_service.entity.Lawyer;
import juribook.lawyer_service.entity.Speciality;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository Spring Data JPA pour l'entité Lawyer.
 *
 * interface + extends JpaRepository<Lawyer, Long> : Spring Data génère
 * automatiquement l'implémentation (CRUD de base : save, findById, findAll...).
 * Les méthodes ci-dessous sont dérivées de leur nom (Query Derivation) :
 * aucune requête SQL/JPQL à écrire manuellement.
 */
public interface LawyerRepository extends JpaRepository<Lawyer, Long> {

    // Utilisé par LawyerRegistrationConsumer pour éviter de créer un doublon
    // si l'événement Kafka est consommé plusieurs fois (idempotence).
    boolean existsByUserId(Long userId);

    // Utilisé par LawyerRegistrationConsumer pour garantir l'unicité
    // du numéro de barreau avant insertion.
    boolean existsByBarNumber(Long barNumber);

    // Permet de retrouver le profil avocat à partir de l'id auth-service.
    Optional<Lawyer> findByUserId(Long userId);

    // ─── Recherches anticipées pour le Sprint 2 (2.3 - recherche par filtres) ──
    List<Lawyer> findByCityIgnoreCase(String city);

    List<Lawyer> findBySpeciality(Speciality speciality);

    List<Lawyer> findByCityIgnoreCaseAndSpeciality(String city, Speciality speciality);
}
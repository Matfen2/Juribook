package juribook.lawyer_service.service;

import juribook.lawyer_service.dto.response.LawyerResponse;
import juribook.lawyer_service.entity.Lawyer;
import juribook.lawyer_service.exception.LawyerNotFoundException;
import juribook.lawyer_service.mapper.LawyerMapper;
import juribook.lawyer_service.repository.LawyerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service métier pour la consultation des profils avocats.
 *
 * Service en lecture uniquement pour l'instant (Sprint 1) :
 * la création est déléguée à LawyerRegistrationConsumer (Kafka),
 * la modification (CRUD profil) est prévue au Sprint 2.2,
 * et la validation/refus admin au Sprint 2.6/7.3.
 *
 * @Transactional(readOnly = true) : optimisation Hibernate pour les
 * transactions qui ne modifient pas de données.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LawyerService {

    private final LawyerRepository lawyerRepository;
    private final LawyerMapper lawyerMapper;

    /**
     * Récupère un profil avocat par son identifiant technique (lawyer.id,
     * pas userId). Lève LawyerNotFoundException (-> 404) si absent.
     */
    public LawyerResponse getById(Long id) {
        Lawyer lawyer = lawyerRepository.findById(id)
                .orElseThrow(() -> new LawyerNotFoundException("Aucun avocat trouvé avec l'id " + id));
        return lawyerMapper.toResponse(lawyer);
    }

    /**
     * Récupère tous les profils avocats.
     * Sera enrichi au Sprint 2.3 avec des filtres (ville, spécialité, pagination).
     */
    public List<LawyerResponse> getAll() {
        return lawyerRepository.findAll().stream()
                .map(lawyerMapper::toResponse)
                .toList();
    }
}
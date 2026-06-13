package juribook.lawyer_service.entity;

/**
 * Statut de validation d'un profil avocat.
 *
 * Cycle de vie :
 * - PENDING_VALIDATION : valeur par défaut à la création (cf. LawyerRegistrationConsumer).
 *   Le profil existe mais n'est pas encore visible/exploitable publiquement.
 * - VALIDATED : profil validé par un administrateur, l'avocat est consultable
 *   et peut recevoir des réservations (Sprint 2.6 / 7.3 - non implémenté ici).
 * - REJECTED : profil refusé par un administrateur (Sprint 7.3 - non implémenté ici).
 *
 * Stocké en base via @Enumerated(EnumType.STRING) (colonne "status").
 */
public enum LawyerStatus {
    PENDING_VALIDATION,
    VALIDATED,
    REJECTED
}
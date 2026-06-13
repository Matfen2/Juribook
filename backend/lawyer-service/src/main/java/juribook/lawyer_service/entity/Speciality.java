package juribook.lawyer_service.entity;

/**
 * Spécialités juridiques proposées sur la plateforme JuriBook.
 *
 * Stocké en base via @Enumerated(EnumType.STRING) (cf. Lawyer.java) :
 * les valeurs sont écrites en texte ("DROIT_DU_TRAVAIL", ...) dans la
 * colonne "speciality", ce qui les rend lisibles directement en SQL
 * et insensibles à un réordonnancement de l'enum (contrairement à ORDINAL).
 *
 * Cette liste est dupliquée côté auth-service dans CreateLawyerRequest
 * (via une contrainte @Pattern) afin de valider la valeur dès l'inscription,
 * avant la publication de l'événement Kafka.
 */
public enum Speciality {
    DROIT_DU_TRAVAIL,           // Licenciement, contrats de travail, prud'hommes
    DROIT_DE_LA_DEFENSE,        // Pénal, défense pénale
    DROIT_DE_LA_FAMILLE,        // Divorce, garde d'enfants, succession
    DROIT_IMMOBILIER,           // Litiges locatifs, copropriété, achat/vente
    DROIT_DES_AFFAIRES,         // Création d'entreprise, contrats commerciaux
    DROIT_FISCAL,               // Contentieux fiscal, optimisation
    DROIT_DE_LA_CONSOMMATION,   // Litiges consommateur/vendeur
    DROIT_DES_ETRANGERS,        // Immigration, titres de séjour, naturalisation
    DROIT_DE_LA_SANTE,          // Responsabilité médicale, litiges patients
    DROIT_DES_ASSURANCES        // Litiges avec assureurs, indemnisation
}
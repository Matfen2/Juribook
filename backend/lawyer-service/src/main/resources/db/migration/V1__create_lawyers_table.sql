-- ═══════════════════════════════════════════════════════════
--  V1 : Création de la table lawyers (lawyer-service, base lawyerdb)
--  Profils avocats : spécialité, n° barreau, ville, statut de validation.
--  Volontairement sans email/password/name/phone_number/role : ces
--  informations restent dans authdb.users (auth-service), reliées
--  ici via user_id.
-- ═══════════════════════════════════════════════════════════
CREATE TABLE lawyers (
    id           BIGSERIAL PRIMARY KEY,

    -- UNIQUE : un utilisateur ne peut avoir qu'un seul profil avocat.
    user_id      BIGINT NOT NULL UNIQUE,

    -- Numéro de barreau : également UNIQUE (un numéro = un avocat).
    bar_number   BIGINT NOT NULL UNIQUE,

    speciality   VARCHAR(50) NOT NULL,
    city         VARCHAR(255) NOT NULL,

    -- Statut de validation, "PENDING_VALIDATION" par défaut à la création
    status       VARCHAR(30) NOT NULL DEFAULT 'PENDING_VALIDATION',

    -- Contraintes CHECK : garantissent au niveau base que seules les
    -- valeurs définies par les enums Speciality.java / LawyerStatus.java
    -- peuvent être insérées, même en cas de bug applicatif.
    CONSTRAINT chk_lawyers_speciality CHECK (speciality IN (
        'DROIT_DU_TRAVAIL', 'DROIT_DE_LA_DEFENSE', 'DROIT_DE_LA_FAMILLE',
        'DROIT_IMMOBILIER', 'DROIT_DES_AFFAIRES', 'DROIT_FISCAL',
        'DROIT_DE_LA_CONSOMMATION', 'DROIT_DES_ETRANGERS',
        'DROIT_DE_LA_SANTE', 'DROIT_DES_ASSURANCES'
    )),
    CONSTRAINT chk_lawyers_status CHECK (status IN ('PENDING_VALIDATION', 'VALIDATED', 'REJECTED'))
);
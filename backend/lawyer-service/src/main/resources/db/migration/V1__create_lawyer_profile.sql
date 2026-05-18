-- ─── Spécialités (seed identique au Sprint 1, ici autorité locale) ─────
CREATE TABLE specialties (
    id   BIGSERIAL    PRIMARY KEY,
    code VARCHAR(50)  NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL UNIQUE
);

INSERT INTO specialties (code, name) VALUES
    ('DROIT_TRAVAIL',       'Droit du travail'),
    ('DROIT_FAMILLE',       'Droit de la famille'),
    ('DROIT_PENAL',         'Droit pénal'),
    ('DROIT_AFFAIRES',      'Droit des affaires'),
    ('DROIT_IMMOBILIER',    'Droit immobilier'),
    ('DROIT_FISCAL',        'Droit fiscal'),
    ('DROIT_SOCIAL',        'Droit social'),
    ('DROIT_COMMERCIAL',    'Droit commercial'),
    ('DROIT_CIVIL',         'Droit civil'),
    ('DROIT_ADMINISTRATIF', 'Droit administratif');

-- ─── Profil avocat ──────────────────────────────────────────────────────
CREATE TABLE lawyers (
    id                  UUID         PRIMARY KEY,

    -- Projection depuis auth-service (alimentée via Kafka au Sprint 2.2)
    email               VARCHAR(254) NOT NULL UNIQUE,
    first_name          VARCHAR(100) NOT NULL,
    last_name           VARCHAR(100) NOT NULL,
    phone               VARCHAR(20),

    -- Identité professionnelle
    bar_number          VARCHAR(50)  NOT NULL UNIQUE,
    specialty_id        BIGINT       NOT NULL REFERENCES specialties (id) ON DELETE RESTRICT,

    -- Adresse (Embeddable côté Java, colonnes flat ici)
    address_street      VARCHAR(255),
    address_postal_code VARCHAR(10),
    address_city        VARCHAR(100) NOT NULL,
    address_country     VARCHAR(2)   NOT NULL DEFAULT 'FR',

    -- Profil étendu (édité par l'avocat au Sprint 2.2)
    bio                 TEXT,
    photo_url           VARCHAR(500),
    consultation_fee    NUMERIC(8,2),

    -- Cycle de vie
    status              VARCHAR(30)  NOT NULL DEFAULT 'PENDING_VALIDATION',
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_lawyers_status CHECK (status IN ('PENDING_VALIDATION', 'ACTIVE', 'SUSPENDED', 'DEACTIVATED'))
);

CREATE INDEX idx_lawyers_specialty_id ON lawyers (specialty_id);
CREATE INDEX idx_lawyers_city         ON lawyers (address_city);
CREATE INDEX idx_lawyers_status       ON lawyers (status);
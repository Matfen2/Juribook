-- ─── Table de référentiel des spécialités ────────────────────────────
CREATE TABLE specialties (
    id   BIGSERIAL PRIMARY KEY,
    code VARCHAR(50)  NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL UNIQUE
);

-- Seed des spécialités juridiques les plus courantes en France
INSERT INTO specialties (code, name) VALUES
    ('DROIT_TRAVAIL',     'Droit du travail'),
    ('DROIT_FAMILLE',     'Droit de la famille'),
    ('DROIT_PENAL',       'Droit pénal'),
    ('DROIT_AFFAIRES',    'Droit des affaires'),
    ('DROIT_IMMOBILIER',  'Droit immobilier'),
    ('DROIT_FISCAL',      'Droit fiscal'),
    ('DROIT_SOCIAL',      'Droit social'),
    ('DROIT_COMMERCIAL',  'Droit commercial'),
    ('DROIT_CIVIL',       'Droit civil'),
    ('DROIT_ADMINISTRATIF', 'Droit administratif');

-- ─── Table des avocats (extension de users) ──────────────────────────
CREATE TABLE lawyers (
    user_id      UUID PRIMARY KEY REFERENCES users (id) ON DELETE CASCADE,
    bar_number   VARCHAR(50)  NOT NULL UNIQUE,
    specialty_id BIGINT       NOT NULL REFERENCES specialties (id) ON DELETE RESTRICT,
    city         VARCHAR(100) NOT NULL,
    created_at   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_lawyers_specialty_id ON lawyers (specialty_id);
CREATE INDEX idx_lawyers_city         ON lawyers (city);
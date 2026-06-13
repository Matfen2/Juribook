-- ═══════════════════════════════════════════════════════════
--  V1 : Création de la table users (auth-service)
--  Profils utilisateur : email, numéro de téléphone, nom, mot de passe
-- ═══════════════════════════════════════════════════════════
CREATE TABLE users (
    id           BIGSERIAL PRIMARY KEY,
    email        VARCHAR(255) NOT NULL UNIQUE,
    password     VARCHAR(255) NOT NULL,
    name         VARCHAR(255) NOT NULL,
    phone_number VARCHAR(20),
    role         VARCHAR(20)  NOT NULL,

    CONSTRAINT chk_users_role CHECK (role IN ('CLIENT', 'LAWYER', 'ADMIN'))
);

CREATE INDEX idx_users_role ON users(role);
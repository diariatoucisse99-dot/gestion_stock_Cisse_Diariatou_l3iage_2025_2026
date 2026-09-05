--- ==============================================================================================
--SCRIPT D'INITIALISATION DE LA BASE DE DONNEES GESTIONSTOCKIAGE
--SGBD: MYSQL
--- ==============================================================================================

CREATE DATABASE IF NOT EXISTS gestion_stock_iage
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE gestion_stock_iage;

-- Table categories
CREATE TABLE IF NOT EXISTS categories(
    id  INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    description TEXT
);

-- Table fournisseurs
CREATE TABLE IF NOT EXISTS fournisseurs(
    id  INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(150) NOT NULL,
    email VARCHAR(150),
    tel VARCHAR(20)
);

-- Table produits
CREATE TABLE IF NOT EXISTS produits(
    id  INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(150) NOT NULL,
    prix DECIMAL(12, 2) NOT NULL,
    quantite_stock INT NOT NULL DEFAULT 0,
    quantite_min INT NOT NULL DEFAULT 5,
    categorie_id INT,
    fournisseur_id INT,
    FOREIGN KEY (categorie_id) REFERENCES categories(id),
    FOREIGN KEY (fournisseur_id) REFERENCES fournisseurs(id)
);

-- Table mouvements de stock
CREATE TABLE IF NOT EXISTS mouvements(
    id  INT AUTO_INCREMENT PRIMARY KEY,
    type ENUM('ENTRE', 'SORTIE') NOT NULL,
    quantite INT NOT NULL,
    date_mouvement DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    motif VARCHAR(255),
    produit_id INT,
    FOREIGN KEY (produit_id) REFERENCES produits(id)
);

--- ==============================================================================================
--DONNEES DE TEST
--- ==============================================================================================

INSERT INTO categories(nom, description) VALUES
 ('Informatique', 'Materiel et accessoires informatiques'),
 ('Mobilier', 'Bureau, chaises et rangements'),
 ('Fournitures', 'Papeterie et consommables');


INSERT INTO fournisseurs(nom, email, tel) VALUES
  ('TechPro SARL', 'contact@techpro.sn', '+221 77 100 00 01'),
  ('MeubleAfrik', 'contact@meubleafrik.sn', '+221 77 200 00 01');

INSERT INTO produits(nom, prix, quantite_stock, quantite_min, categorie_id, fournisseur_id) VALUES
    ("Ordinateur Portable", 550000.0, 15, 3, 1, 1),
    ("Bureau en bois", 87000.0, 8, 2, 2, 2);

-- ==============================================================================================
-- L'EXAMEN FINAL : prixPromo, utilisateurs, tracabilite des mouvements dont j'ai ajouté
-- ==============================================================================================

-- Colonne prix_promo sur produits (optionnelle, correspond a Double prixPromo en Java)
ALTER TABLE produits ADD COLUMN prix_promo DECIMAL(12, 2) NULL;

-- Table des utilisateurs (authentification)
CREATE TABLE IF NOT EXISTS utilisateurs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(150) NOT NULL UNIQUE,
    nom VARCHAR(100) NOT NULL,
    mot_de_passe_hash VARCHAR(100) NOT NULL,
    role ENUM('ADMIN', 'GESTIONNAIRE') NOT NULL,
    date_creation DATE DEFAULT (CURRENT_DATE),
    actif BOOLEAN NOT NULL DEFAULT TRUE
    );

-- Colonne de traçabilité sur mouvements (qui a fait le mouvement)
ALTER TABLE mouvements ADD COLUMN utilisateur_id BIGINT NULL;
ALTER TABLE mouvements ADD CONSTRAINT fk_mouvement_utilisateur
    FOREIGN KEY (utilisateur_id) REFERENCES utilisateurs(id);

-- Utilisateurs de test (mêmes comptes que sur PostgreSQL)
-- admin@gestionstock.sn / mot de passe : admin123
-- gestionnaire@gestionstock.sn / mot de passe : gestion123
INSERT INTO utilisateurs (email, nom, mot_de_passe_hash, role) VALUES
    ('admin@gestionstock.sn', 'Admin Principal', '$2a$12$PeLywNUe30p68bcFG8VMP.F9f4kSoQZQ0cUDeejhJMa.Tm4IKzYe.', 'ADMIN'),
    ('gestionnaire@gestionstock.sn', 'Nadia Gestionnaire', '$2a$12$NV9lnb9gkRY733WXrF.33uuBx/bEpd15Rr7.cs82QiaGif1DkK2.6', 'GESTIONNAIRE');

-- Mettre à jour un produit avec un prix promo pour tester l'affichage
UPDATE produits SET prix_promo = 480000.0 WHERE nom = 'Ordinateur Portable';
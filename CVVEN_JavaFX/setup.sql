-- Ajouter le prix par nuit aux types de chambres
ALTER TABLE Type_Chambre ADD COLUMN IF NOT EXISTS type_prix DECIMAL(10,2) NOT NULL DEFAULT 0.00;

-- Table des clients
CREATE TABLE IF NOT EXISTS Client (
    client_id VARCHAR(20) PRIMARY KEY,
    client_nom VARCHAR(100) NOT NULL,
    client_prenom VARCHAR(100) NOT NULL,
    client_email VARCHAR(150),
    client_telephone VARCHAR(20),
    client_adresse VARCHAR(255)
);

-- Table des réservations
CREATE TABLE IF NOT EXISTS Reservation (
    resa_id VARCHAR(20) PRIMARY KEY,
    client_id VARCHAR(20) NOT NULL,
    chamb_id VARCHAR(20) NOT NULL,
    resa_date_debut DATE NOT NULL,
    resa_date_fin DATE NOT NULL,
    resa_statut ENUM('en_attente','confirmée','annulée','terminée') NOT NULL DEFAULT 'en_attente',
    FOREIGN KEY (client_id) REFERENCES Client(client_id) ON DELETE RESTRICT,
    FOREIGN KEY (chamb_id) REFERENCES Chambre(chamb_id) ON DELETE RESTRICT
);

-- Table des factures (une par réservation)
CREATE TABLE IF NOT EXISTS Facture (
    fact_id VARCHAR(20) PRIMARY KEY,
    resa_id VARCHAR(20) NOT NULL UNIQUE,
    fact_date DATE NOT NULL,
    fact_montant DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (resa_id) REFERENCES Reservation(resa_id) ON DELETE CASCADE
);

# 🏨 CVVEN - Application de Gestion (JavaFX)

![Java](https://img.shields.io/badge/Java-21-orange.svg)
![JavaFX](https://img.shields.io/badge/JavaFX-22-blue.svg)
![MySQL](https://img.shields.io/badge/MySQL-Database-lightgrey.svg)
![Architecture](https://img.shields.io/badge/Architecture-MVC-green.svg)

## 📝 Présentation du projet
**CVVEN** est une application de bureau (client lourd) développée pour informatiser la gestion d'un centre de vacances. Ce projet a été réalisé dans le cadre du BTS SIO. 
Il permet aux employés (gestionnaires) d'administrer les clients, de gérer les hébergements, d'effectuer des réservations et de générer des factures.

## ✨ Fonctionnalités Principales
- **🔐 Authentification :** Connexion sécurisée au système pour les membres du personnel.
- **👥 Gestion des Clients :** Ajout, modification, consultation et suppression des clients.
- **🛏️ Gestion des Chambres :** Catalogue des hébergements et de leurs spécificités (capacité, PMR, etc.).
- **📅 Réservations :** Création de séjours avec calcul automatique des nuits et vérification des disponibilités.
- **🧾 Facturation :** Génération de factures PDF / Texte et suivi des paiements.

## 🛠️ Architecture Technique
L'application repose sur le modèle de conception **MVC (Modèle-Vue-Contrôleur)** :
- **Modèle (`src/model` & `src/Metier`) :** Logique métier, requêtes SQL (JDBC) et objets (Client, Chambre, Reservation...).
- **Vue (`src/view`) :** Interfaces graphiques conçues avec SceneBuilder (fichiers `.fxml`) et stylisées avec CSS.
- **Contrôleur (`src/controller`) :** Liaison entre l'interface et la base de données (JavaFX Controllers).
- **Base de données :** MySQL / MariaDB gérée avec une classe de connexion Singleton (`SingletonConnection.java`).

## 🚀 Installation & Exécution

### Prérequis
- Java Development Kit (JDK) 17 ou supérieur.
- JavaFX SDK.
- Serveur de base de données (WAMP, XAMPP ou serveur MySQL dédié).
- Pilote JDBC MySQL (`mysql-connector-java.jar`).

### Déploiement de la base de données
1. Ouvrez PhpMyAdmin ou votre console MySQL.
2. Créez une base de données nommée `CVVEN`.
3. Importez le fichier SQL fourni dans le projet pour générer la structure (tables : `Utilisateur`, `Chambre`, `Type_Chambre`, `Reserve`, `Facture`).

### Configuration
Modifiez le fichier `src/dbConnection/SingletonConnection.java` pour y insérer vos identifiants locaux :
```java
String url = "jdbc:mysql://localhost:3306/CVVEN";
String user = "root"; // Votre utilisateur MySQL
String password = ""; // Votre mot de passe MySQL

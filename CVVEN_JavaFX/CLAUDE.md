# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

CVVEN is a JavaFX 21 desktop application for hotel/room management (gestion des chambres). The UI and code are in French. It uses MySQL for persistence with direct JDBC (no ORM).

## Build System

This is an **Eclipse IDE project** — there is no Maven, Gradle, or Ant build script. The project is compiled directly through Eclipse using the configuration in `.classpath` and `.project`.

To run outside Eclipse, you must manually compile with `javac` and provide the JavaFX SDK and MySQL Connector/J on the module/classpath. Library paths in `.classpath` are absolute and machine-specific (they will need updating on a new machine).

**Dependencies (referenced in `.classpath`):**
- JavaFX SDK 21.0.9 (base, controls, fxml, graphics, media, swing, web modules)
- MySQL Connector/J 9.2.0

**Compiled output:** `bin/` directory

## Architecture

The project follows MVC:

| Layer | Package | Role |
|-------|---------|------|
| View | `src/view/*.fxml` + CSS | FXML UI definitions, styled via `application.css` and `src/view/style.css` |
| Controller | `src/controller/` | FXML event handlers (`LoginController`, `HomeController`, `ChambreController`) |
| Model | `src/model/` | SQL query logic (`LoginModel`, `listeChambres`) |
| Domain | `src/Metier/` | Business entities with JavaFX properties (`Chambre`, `type_Chambre`, `UserSession`) |
| DB | `src/dbConnection/SingletonConnection.java` | Single static JDBC connection to MySQL |
| Entry point | `src/application/MainCVVEN.java` | Extends `javafx.application.Application`, loads `FXMLLogin.fxml` |

### Key Patterns

- **Singleton DB connection** — `SingletonConnection.getConnection()` returns one static `Connection` instance initialized at class load. No pooling.
- **Session singleton** — `UserSession` stores logged-in user's nom, prenom, and role after authentication. Cleared on logout.
- **JavaFX Properties on entities** — `Chambre` uses `StringProperty`/`IntegerProperty` fields to support direct `TableView` data binding via `ObservableList`.
- **Screen navigation** — Each screen opens a new `Stage`; the previous stage is closed. Screens are loaded with `FXMLLoader`.
- **Role-based access** — `UserSession.role` is either `"administrateur"` or `"personnel"`; controllers check this to show/hide admin features.

## Database

```
URL:      jdbc:mysql://localhost:3306/CVVEN
Driver:   com.mysql.cj.jdbc.Driver
User:     root
Password: (empty)
```

Key tables: `Utilisateur` (auth), `Chambre` (rooms), `Type_Chambre` (room categories).

Credentials are hardcoded in `SingletonConnection.java`. There is no config file.

## Known Issues

- `HomeController.java` — `btnDeconnexion` click handler is empty (logout not implemented).
- `listeChambres.modifierChambre()` — parameter 4 is missing a `setString`/`setInt` type call (SQL type mismatch bug).
- No tests exist — there is no test framework or test directory.
- Absolute JAR paths in `.classpath` are not portable across machines.

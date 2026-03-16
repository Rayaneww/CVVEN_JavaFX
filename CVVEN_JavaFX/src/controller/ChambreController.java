package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import model.listeChambres;
import Metier.Chambre;
import dbConnection.SingletonConnection;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ChambreController implements Initializable {

	// --- FXML IDs ---
	@FXML
	private TableView<Chambre> tableChambres;

	// CORRECTION 1 : Types des colonnes (Integer pour les nombres)
	@FXML
	private TableColumn<Chambre, String> colId; // Ajout de l'ID (utile même si caché)
	@FXML
	private TableColumn<Chambre, Integer> colNumero;
	@FXML
	private TableColumn<Chambre, String> colEmplacement; // Remplace "Etage"
	@FXML
	private TableColumn<Chambre, String> colRemarque; // Remplace "Description"
	@FXML
	private TableColumn<Chambre, Integer> colType; // Affiche l'ID du type pour l'instant

	// Note : colPrix et colStatut ont été retirés car absents de ta classe Chambre
	// actuelle

	@FXML
	private TextField txtRecherche;
	@FXML
	private ComboBox<String> comboType;
	@FXML
	private ComboBox<String> comboStatut; // A garder seulement si tu ajoutes le champ statut en BDD plus tard
	@FXML
	private Button btnAjouter, btnModifier, btnSupprimer, btnDeconnexion, btnAcceuil;

	// --- Données ---
	private listeChambres gestionnaireChambres;
	private FilteredList<Chambre> listeFiltree;

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		// 1. Initialiser le lien avec la BDD
		gestionnaireChambres = new listeChambres();

		// 2. Configurer les colonnes
		// CORRECTION 2 : Les noms doivent correspondre aux "Property" de la classe
		// Chambre
		// Exemple : chambNumeroProperty() -> on met "chambNumero"
		colId.setCellValueFactory(new PropertyValueFactory<>("chambId"));
		colNumero.setCellValueFactory(new PropertyValueFactory<>("chambNumero"));
		colEmplacement.setCellValueFactory(new PropertyValueFactory<>("chambEmplacement"));
		colRemarque.setCellValueFactory(new PropertyValueFactory<>("chambRemarque"));
		// Dans ta classe Chambre, la méthode s'appelle typeLibelleProperty(), donc on
		// met "typeLibelle"
		colType.setCellValueFactory(new PropertyValueFactory<>("typeLibelle"));

		// 3. Filtres
		setupFilters();

		// 4. Gestion Sélection
		tableChambres.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
			boolean selected = (newV != null);
			btnModifier.setDisable(!selected);
			btnSupprimer.setDisable(!selected);
		});
	}

	public void goAcceuil() {
		System.out.println("Clic sur accueil");
		// --- SUCCÈS ---
		try {
			// Fermer la fenêtre de login actuelle
			Stage currentStage = (Stage) btnAcceuil.getScene().getWindow();
			currentStage.close();

			// Ouvrir la nouvelle fenêtre (Page d'accueil)
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/FXMLAcceuil.fxml"));
			Parent root = loader.load();

			Stage newStage = new Stage();
			newStage.setTitle("Page d'Acceuil");
			newStage.setScene(new Scene(root));
			newStage.show();

		} catch (IOException e) {
			e.printStackTrace();
			showAlert("Erreur Critique", "Impossible de charger la page d'accueil.", Alert.AlertType.ERROR);
		}
		;
	}

	private void setupFilters() {
		// Remplissage des combos (A adapter selon tes vrais types en BDD)
		comboType.getItems().addAll("Tous", "Simple", "Double", "Suite");
		comboType.getSelectionModel().selectFirst();

		// On initialise la liste filtrée
		listeFiltree = new FilteredList<>(gestionnaireChambres.getChambres(), p -> true);

		// Listeners
		txtRecherche.textProperty().addListener((obs, o, n) -> filtrer());
		comboType.valueProperty().addListener((obs, o, n) -> filtrer());

		SortedList<Chambre> sortedData = new SortedList<>(listeFiltree);
		sortedData.comparatorProperty().bind(tableChambres.comparatorProperty());
		tableChambres.setItems(sortedData);
	}

	private void filtrer() {
		String search = txtRecherche.getText().toLowerCase();

		// CORRECTION 3 : Adaptation du filtre aux types de données (int vs String)
		listeFiltree.setPredicate(c -> {
			// Filtre Recherche Texte
			// On convertit le numéro (int) en String pour utiliser .contains
			boolean matchText = search.isEmpty() || String.valueOf(c.getChambNumero()).contains(search)
					|| (c.getChambRemarque() != null && c.getChambRemarque().toLowerCase().contains(search))
					|| (c.getChambEmplacement() != null && c.getChambEmplacement().toLowerCase().contains(search));

			// Note : Le filtre par Type via ComboBox est complexe ici car ta BDD stocke un
			// ID (int)
			// et ta ComboBox affiche du texte ("Simple"). Il faudrait une map de
			// correspondance.
			// Pour l'instant, on laisse passer tout le monde sur le type.
			return matchText;
		});
	}

	// --- ACTIONS ---

	@FXML
	public void ajtChambre(ActionEvent event) {
		// CORRECTION 4 : Le constructeur doit correspondre : (String id, String
		// emplacement, int numero, String remarque, int type)
		// Génération d'un ID unique fictif pour le test
		String newId = "CH-" + System.currentTimeMillis();
		int newNum = (int) (Math.random() * 1000);

		Chambre nouvelle = new Chambre(newId, "Etage 1", // Emplacement
				newNum, // Numéro
				"Nouvelle chambre test" // Remarque
		// Type ID (1 par défaut)
		);

		if (gestionnaireChambres.ajouterChambre(nouvelle)) {
			System.out.println("Chambre ajoutée !");
		} else {
			System.err.println("Erreur d'ajout");
		}
	}

	@FXML
	public void suppChambre(ActionEvent event) {
		Chambre selected = tableChambres.getSelectionModel().getSelectedItem();
		if (selected != null) {
			Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
					"Supprimer la chambre n°" + selected.getChambNumero() + " ?", ButtonType.YES, ButtonType.NO);
			alert.showAndWait().ifPresent(response -> {
				if (response == ButtonType.YES) {
					if (gestionnaireChambres.supprimerChambre(selected)) {
						System.out.println("Suppression réussie !");
					}
				}
			});
		}
	}

	@FXML
	public void modifChambre(ActionEvent event) {
		System.out.println("Fonctionnalité à implémenter : Ouvrir fenêtre de modif");
	}

	@FXML
	public void deconnexion(ActionEvent event) {
		// Code pour revenir au login
	}

	// Méthode utilitaire pour afficher les alertes proprement
	private void showAlert(String titre, String message, Alert.AlertType type) {
		Alert alert = new Alert(type);
		alert.setTitle(titre);
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
	}
}
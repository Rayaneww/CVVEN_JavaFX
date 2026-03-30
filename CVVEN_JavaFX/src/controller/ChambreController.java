package controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import model.listeChambres;
import Metier.Chambre;
import Metier.type_Chambre;
import Metier.UserSession;
import util.NavigationUtils;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class ChambreController implements Initializable {

	@FXML private BorderPane mainContainer;
	@FXML private TableView<Chambre> tableChambres;
	@FXML private TableColumn<Chambre, String> colId;
	@FXML private TableColumn<Chambre, Integer> colNumero;
	@FXML private TableColumn<Chambre, String> colEmplacement;
	@FXML private TableColumn<Chambre, String> colRemarque;
	@FXML private TableColumn<Chambre, String> colType;

	@FXML private TextField txtRecherche;
	@FXML private ComboBox<String> comboType;
	@FXML private ComboBox<String> comboStatut;
	@FXML private Button btnAjouter, btnModifier, btnSupprimer, btnDeconnexion, btnAcceuil;
	@FXML private Label lblUtilisateur;

	private listeChambres gestionnaireChambres;
	private FilteredList<Chambre> listeFiltree;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		gestionnaireChambres = new listeChambres();

		colId.setCellValueFactory(new PropertyValueFactory<>("chambId"));
		colNumero.setCellValueFactory(new PropertyValueFactory<>("chambNumero"));
		colEmplacement.setCellValueFactory(new PropertyValueFactory<>("chambEmplacement"));
		colRemarque.setCellValueFactory(new PropertyValueFactory<>("chambRemarque"));
		colType.setCellValueFactory(cellData -> {
			type_Chambre type = cellData.getValue().getType_chambre();
			return new javafx.beans.property.SimpleStringProperty(type != null ? type.getType_libelle() : "N/A");
		});

		UserSession session = UserSession.getInstance();
		if (session != null && lblUtilisateur != null) {
			lblUtilisateur.setText(session.toString());
		}

		setupFilters();

		tableChambres.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
			boolean selected = (newV != null);
			btnModifier.setDisable(!selected);
			btnSupprimer.setDisable(!selected);
		});
	}

	private void setupFilters() {
		comboType.getItems().add("Tous");
		gestionnaireChambres.getTypes().forEach(t -> comboType.getItems().add(t.getType_libelle()));
		comboType.getSelectionModel().selectFirst();

		listeFiltree = new FilteredList<>(gestionnaireChambres.getChambres(), p -> true);

		txtRecherche.textProperty().addListener((obs, o, n) -> filtrer());
		comboType.valueProperty().addListener((obs, o, n) -> filtrer());

		SortedList<Chambre> sortedData = new SortedList<>(listeFiltree);
		sortedData.comparatorProperty().bind(tableChambres.comparatorProperty());
		tableChambres.setItems(sortedData);
	}

	private void filtrer() {
		String search = txtRecherche.getText().toLowerCase();
		String typeFilter = comboType.getValue();

		listeFiltree.setPredicate(c -> {
			boolean matchText = search.isEmpty()
					|| String.valueOf(c.getChambNumero()).contains(search)
					|| (c.getChambRemarque() != null && c.getChambRemarque().toLowerCase().contains(search))
					|| (c.getChambEmplacement() != null && c.getChambEmplacement().toLowerCase().contains(search));

			boolean matchType = "Tous".equals(typeFilter)
					|| (c.getType_chambre() != null && typeFilter.equals(c.getType_chambre().getType_libelle()));

			return matchText && matchType;
		});
	}

	private void nav(String fxml, String title) {
		NavigationUtils.goTo(fxml, title, true, (Stage) mainContainer.getScene().getWindow());
	}

	@FXML public void goAcceuil()      { nav("/view/FXMLAcceuil.fxml",  "CVVEN - Accueil"); }
	@FXML public void goChambres()     { nav("/view/Chambres.fxml",      "Gestion des Chambres"); }
	@FXML public void goClients()      { nav("/view/Clients.fxml",       "Gestion des Clients"); }
	@FXML public void goReservations() { nav("/view/Reservations.fxml",  "Gestion des Réservations"); }
	@FXML public void goPersonnel()    { nav("/view/Personnel.fxml",     "Gestion du Personnel"); }
	@FXML public void goFacturation()  { nav("/view/Facturation.fxml",   "Facturation"); }

	@FXML
	public void deconnexion(ActionEvent event) {
		UserSession.cleanSession();
		NavigationUtils.goTo("/view/FXMLLogin.fxml", "CVVEN - Authentification", false,
				(Stage) mainContainer.getScene().getWindow());
	}

	@FXML
	public void ajtChambre(ActionEvent event) {
		showChambreDialog(null).ifPresent(c -> {
			if (gestionnaireChambres.ajouterChambre(c)) {
				showAlert("Succès", "Chambre ajoutée avec succès.", Alert.AlertType.INFORMATION);
			} else {
				showAlert("Erreur", "Impossible d'ajouter la chambre. Vérifiez les données.", Alert.AlertType.ERROR);
			}
		});
	}

	@FXML
	public void modifChambre(ActionEvent event) {
		Chambre selected = tableChambres.getSelectionModel().getSelectedItem();
		if (selected == null) return;

		showChambreDialog(selected).ifPresent(c -> {
			if (gestionnaireChambres.modifierChambre(c)) {
				tableChambres.refresh();
				showAlert("Succès", "Chambre modifiée avec succès.", Alert.AlertType.INFORMATION);
			} else {
				showAlert("Erreur", "Impossible de modifier la chambre.", Alert.AlertType.ERROR);
			}
		});
	}

	@FXML
	public void suppChambre(ActionEvent event) {
		Chambre selected = tableChambres.getSelectionModel().getSelectedItem();
		if (selected == null) return;

		Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
				"Supprimer la chambre n°" + selected.getChambNumero() + " ?",
				ButtonType.YES, ButtonType.NO);
		confirm.setTitle("Confirmation");
		confirm.setHeaderText(null);
		confirm.showAndWait().ifPresent(response -> {
			if (response == ButtonType.YES) {
				if (!gestionnaireChambres.supprimerChambre(selected)) {
					showAlert("Erreur", "Impossible de supprimer la chambre.", Alert.AlertType.ERROR);
				}
			}
		});
	}

	private Optional<Chambre> showChambreDialog(Chambre existing) {
		Dialog<Chambre> dialog = new Dialog<>();
		dialog.setTitle(existing == null ? "Nouvelle Chambre" : "Modifier la Chambre");
		dialog.setHeaderText(null);

		ButtonType saveBtn = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
		dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

		TextField fieldNumero = new TextField();
		fieldNumero.setPromptText("Ex: 101");
		TextField fieldEmplacement = new TextField();
		fieldEmplacement.setPromptText("Ex: Bâtiment A - Étage 1");
		TextField fieldRemarque = new TextField();
		fieldRemarque.setPromptText("Facultatif");

		ComboBox<type_Chambre> comboTypeDialog = new ComboBox<>();
		comboTypeDialog.setItems(gestionnaireChambres.getTypes());
		comboTypeDialog.setConverter(new StringConverter<type_Chambre>() {
			@Override
			public String toString(type_Chambre t) {
				return t == null ? "" : t.getType_libelle();
			}
			@Override
			public type_Chambre fromString(String s) {
				return null;
			}
		});
		comboTypeDialog.setMaxWidth(Double.MAX_VALUE);

		if (existing != null) {
			fieldNumero.setText(String.valueOf(existing.getChambNumero()));
			fieldEmplacement.setText(existing.getChambEmplacement());
			fieldRemarque.setText(existing.getChambRemarque());
			if (existing.getType_chambre() != null) {
				int currentTypeId = existing.getType_chambre().getType_id();
				comboTypeDialog.getItems().stream()
						.filter(t -> t.getType_id() == currentTypeId)
						.findFirst()
						.ifPresent(comboTypeDialog::setValue);
			}
		}

		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(12);
		grid.setPadding(new Insets(20, 30, 10, 10));
		grid.add(new Label("Numéro :"), 0, 0);
		grid.add(fieldNumero, 1, 0);
		grid.add(new Label("Emplacement :"), 0, 1);
		grid.add(fieldEmplacement, 1, 1);
		grid.add(new Label("Remarque :"), 0, 2);
		grid.add(fieldRemarque, 1, 2);
		grid.add(new Label("Type :"), 0, 3);
		grid.add(comboTypeDialog, 1, 3);
		dialog.getDialogPane().setContent(grid);

		Node saveButton = dialog.getDialogPane().lookupButton(saveBtn);
		saveButton.setDisable(true);

		Runnable validate = () -> {
			boolean invalid = fieldNumero.getText().trim().isEmpty()
					|| fieldEmplacement.getText().trim().isEmpty()
					|| comboTypeDialog.getValue() == null;
			saveButton.setDisable(invalid);
		};
		fieldNumero.textProperty().addListener((o, ov, nv) -> validate.run());
		fieldEmplacement.textProperty().addListener((o, ov, nv) -> validate.run());
		comboTypeDialog.valueProperty().addListener((o, ov, nv) -> validate.run());

		dialog.setResultConverter(btn -> {
			if (btn != saveBtn) return null;
			try {
				int numero = Integer.parseInt(fieldNumero.getText().trim());
				String emplacement = fieldEmplacement.getText().trim();
				String remarque = fieldRemarque.getText().trim().isEmpty()
						? "Pas de remarque" : fieldRemarque.getText().trim();
				type_Chambre type = comboTypeDialog.getValue();

				if (existing != null) {
					existing.setChambNumero(numero);
					existing.setChambEmplacement(emplacement);
					existing.setChambRemarque(remarque);
					existing.setType_chambre(type);
					return existing;
				} else {
					String id = "CH-" + System.currentTimeMillis();
					return new Chambre(id, emplacement, numero, remarque, type);
				}
			} catch (NumberFormatException e) {
				showAlert("Erreur de saisie", "Le numéro doit être un entier valide.", Alert.AlertType.WARNING);
				return null;
			}
		});

		return dialog.showAndWait();
	}

	private void showAlert(String titre, String message, Alert.AlertType type) {
		Alert alert = new Alert(type);
		alert.setTitle(titre);
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
	}
}

package controller; // Assurez-vous que c'est bien votre package

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import Metier.UserSession;
import util.NavigationUtils;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class HomeController implements Initializable {

	@FXML private BorderPane mainContainer;
	@FXML private Button btnChambres, btnReservations, btnClients, btnPersonnel, btnFacturation, btnDeconnexion, btnAcceuil;
	@FXML private Label lblDate;

	@FXML
	private Label lblUtilisateur;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		if (lblDate != null) {
			lblDate.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
		}

		UserSession session = UserSession.getInstance();
		if (session != null && lblUtilisateur != null) {
			lblUtilisateur.setText(session.toString());
		}

		btnDeconnexion.setOnAction(this::handleDeconnexion);
		btnChambres.setOnAction(e -> goChambre());
		btnReservations.setOnAction(e -> goReservations());
		btnClients.setOnAction(e -> goClients());
		btnPersonnel.setOnAction(e -> goPersonnel());
		btnFacturation.setOnAction(e -> goFacturation());
	}

	private void nav(String fxml, String title) {
		NavigationUtils.goTo(fxml, title, true, (Stage) mainContainer.getScene().getWindow());
	}

	public void goChambre()        { nav("/view/Chambres.fxml",     "Gestion des Chambres"); }
	public void goAcceuil()        { nav("/view/FXMLAcceuil.fxml",  "CVVEN - Accueil"); }
	public void goReservations()   { nav("/view/Reservations.fxml", "Gestion des Réservations"); }
	public void goClients()        { nav("/view/Clients.fxml",      "Gestion des Clients"); }
	public void goPersonnel()      { nav("/view/Personnel.fxml",    "Gestion du Personnel"); }
	public void goFacturation()    { nav("/view/Facturation.fxml",  "Facturation"); }

	@FXML
	private void handleDeconnexion(ActionEvent event) {
		UserSession.cleanSession();
		NavigationUtils.goTo("/view/FXMLLogin.fxml", "CVVEN - Authentification", false,
				(Stage) mainContainer.getScene().getWindow());
	}

	private void showAlert(String titre, String message, Alert.AlertType type) {
		Alert alert = new Alert(type);
		alert.setTitle(titre);
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
	}
}
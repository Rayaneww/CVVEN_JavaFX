package controller;

import model.LoginModel;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

	// ATTENTION : Ces noms doivent correspondre aux fx:id de votre FXML
	@FXML
	private TextField txtlogin; // Dans votre code précédent c'était fieldLogin
	@FXML
	private PasswordField txtpassword; // Dans votre code précédent c'était fieldPsw
	@FXML
	private Button btnconn;
	@FXML
	private HBox rootPane;

	private LoginModel loginModel;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		this.loginModel = new LoginModel();

		btnconn.setOnAction(event -> handleConnexion());

		// --- 1. Activer la touche "ENTRÉE" ---
		// Cela signifie que si on appuie sur Entrée n'importe où, ça clique sur le
		// bouton
		btnconn.setDefaultButton(true);

		// --- 2. Enlever le curseur du champ Login au démarrage ---
		// On utilise Platform.runLater car on doit attendre que la fenêtre soit
		// affichée
		Platform.runLater(() -> {
			rootPane.requestFocus();
		});
	}

	private void handleConnexion() {
		String login = txtlogin.getText().trim();
		String password = txtpassword.getText().trim();

		// 1. Validation basique
		if (login.isEmpty() || password.isEmpty()) {
			showAlert("Erreur", "Veuillez saisir le login et le mot de passe.", Alert.AlertType.WARNING);
			return;
		}

		// 2. Appel au Modèle (Base de données)
		if (loginModel.isLoginValidDatabase(login, password)) {

			// --- SUCCÈS ---
			try {
				// Fermer la fenêtre de login actuelle
				Stage currentStage = (Stage) btnconn.getScene().getWindow();
				currentStage.close();

				// Ouvrir la nouvelle fenêtre (Page d'accueil)
				FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/FXMLAcceuil.fxml"));
				Parent root = loader.load();

				Stage newStage = new Stage();
				newStage.setTitle("Page d'accueil");
				newStage.setScene(new Scene(root));
				newStage.show();

			} catch (IOException e) {
				e.printStackTrace();
				showAlert("Erreur Critique", "Impossible de charger la page d'accueil.", Alert.AlertType.ERROR);
			}

		} else {
			// --- ÉCHEC ---
			showAlert("Erreur d'authentification", "Login ou mot de passe incorrect !", Alert.AlertType.ERROR);
		}
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
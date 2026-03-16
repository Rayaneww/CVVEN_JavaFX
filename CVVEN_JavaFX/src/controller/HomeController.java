package controller; // Assurez-vous que c'est bien votre package

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.event.ActionEvent;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class HomeController implements Initializable {

    @FXML
    private Button btnChambres;

    @FXML
    private Button btnReservations;

    @FXML
    private Button btnClients;

    @FXML
    private Button btnPersonnel;

    @FXML
    private Button btnFacturation;

    @FXML
    private Button btnDeconnexion;
    
    @FXML
    private Button btnAcceuil;

    @FXML
    private Label lblDate;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialisation de la date du jour
        if (lblDate != null) {
            lblDate.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        }
        
        // Configuration des actions des boutons
        btnDeconnexion.setOnAction(this::handleDeconnexion);
        btnChambres.setOnAction(event -> goChambre());
        btnReservations.setOnAction(event -> System.out.println("Clic sur Réservations"));
        // Ajoutez les autres actions ici...
    }
    
    public void goChambre() {
    	System.out.println("Clic sur Chambres");
            // --- SUCCÈS ---
            try {
                // Fermer la fenêtre de login actuelle
                Stage currentStage = (Stage) btnChambres.getScene().getWindow();
                currentStage.close();

                // Ouvrir la nouvelle fenêtre (Page d'accueil)
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/Chambres.fxml"));
                Parent root = loader.load();
                
                Stage newStage = new Stage();
                newStage.setTitle("Gestion des Chambres");
                newStage.setScene(new Scene(root));
                newStage.show();
                
            } catch (IOException e) {
                e.printStackTrace();
                showAlert("Erreur Critique", "Impossible de charger la page de gestion des chambre.", Alert.AlertType.ERROR);
            }
    		;
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


    private void handleDeconnexion(ActionEvent event) {
        System.out.println("Déconnexion en cours...");
        // Ici, insérez la logique pour fermer la fenêtre ou revenir au Login
        // Ex: ((Node)event.getSource()).getScene().getWindow().hide();
    }
    private void showAlert(String titre, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
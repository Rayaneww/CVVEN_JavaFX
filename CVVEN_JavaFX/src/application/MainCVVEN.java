package application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class MainCVVEN extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // 1. Chargement du fichier FXML
            // Utiliser 'Parent' est plus sûr : cela accepte HBox, AnchorPane, VBox, etc.
            Parent root = FXMLLoader.load(getClass().getResource("/view/FXMLLogin.fxml"));

            // 2. Création de la scène
            Scene scene = new Scene(root);
            
            // (Optionnel) Ajout du fichier CSS global s'il n'est pas déjà lié dans le FXML
            // scene.getStylesheets().add(getClass().getResource("/view/style.css").toExternalForm());

            // 3. Configuration de la fenêtre
            primaryStage.setTitle("CVVEN - Authentification");
            primaryStage.setScene(scene);
            
            // Empêcher le redimensionnement de la fenêtre de login (plus propre)
            primaryStage.setResizable(false);
            
            // (Optionnel) Ajouter une icône à la fenêtre (mettez une image 'logo.png' dans src/view)
            // primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/view/logo.png")));

            primaryStage.show();

        } catch(Exception e) {
            e.printStackTrace();
            System.err.println("ERREUR CRITIQUE : Impossible de lancer l'application.");
            System.err.println("Vérifiez que le fichier '/view/FXMLLogin.fxml' existe bien.");
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
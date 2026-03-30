package controller;

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
import model.listeClients;
import Metier.Client;
import Metier.UserSession;
import util.NavigationUtils;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class ClientController implements Initializable {

    @FXML private BorderPane mainContainer;
    @FXML private TableView<Client> tableClients;
    @FXML private TableColumn<Client, String> colId, colNom, colPrenom, colEmail, colTelephone, colAdresse;
    @FXML private TextField txtRecherche;
    @FXML private Button btnAjouter, btnModifier, btnSupprimer, btnDeconnexion;
    @FXML private Label lblUtilisateur;

    private listeClients gestionnaire;
    private FilteredList<Client> listeFiltree;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        gestionnaire = new listeClients();

        colId.setCellValueFactory(new PropertyValueFactory<>("clientId"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("clientNom"));
        colPrenom.setCellValueFactory(new PropertyValueFactory<>("clientPrenom"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("clientEmail"));
        colTelephone.setCellValueFactory(new PropertyValueFactory<>("clientTelephone"));

        UserSession session = UserSession.getInstance();
        if (session != null && lblUtilisateur != null)
            lblUtilisateur.setText(session.toString());

        listeFiltree = new FilteredList<>(gestionnaire.getClients(), p -> true);
        txtRecherche.textProperty().addListener((obs, o, n) -> {
            String s = n.toLowerCase();
            listeFiltree.setPredicate(c ->
                s.isEmpty()
                || c.getClientNom().toLowerCase().contains(s)
                || c.getClientPrenom().toLowerCase().contains(s)
                || c.getClientEmail().toLowerCase().contains(s)
                || c.getClientTelephone().contains(s));
        });
        SortedList<Client> sorted = new SortedList<>(listeFiltree);
        sorted.comparatorProperty().bind(tableClients.comparatorProperty());
        tableClients.setItems(sorted);

        tableClients.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            btnModifier.setDisable(n == null);
            btnSupprimer.setDisable(n == null);
        });
    }

    // --- Navigation ---
    private void nav(String fxml, String title) {
        NavigationUtils.goTo(fxml, title, true, (Stage) mainContainer.getScene().getWindow());
    }
    @FXML public void goAcceuil()      { nav("/view/FXMLAcceuil.fxml",  "CVVEN - Accueil"); }
    @FXML public void goChambres()     { nav("/view/Chambres.fxml",      "Gestion des Chambres"); }
    @FXML public void goClients()      { nav("/view/Clients.fxml",       "Gestion des Clients"); }
    @FXML public void goReservations() { nav("/view/Reservations.fxml",  "Gestion des Réservations"); }
    @FXML public void goPersonnel()    { nav("/view/Personnel.fxml",     "Gestion du Personnel"); }
    @FXML public void goFacturation()  { nav("/view/Facturation.fxml",   "Facturation"); }
    @FXML public void deconnexion(ActionEvent e) {
        UserSession.cleanSession();
        NavigationUtils.goTo("/view/FXMLLogin.fxml", "CVVEN - Authentification", false,
                (Stage) mainContainer.getScene().getWindow());
    }

    // --- CRUD ---
    @FXML public void ajtClient(ActionEvent e) {
        showDialog(null).ifPresent(c -> {
            if (!gestionnaire.ajouter(c))
                showAlert("Erreur", "Impossible d'ajouter le client (ID déjà utilisé?).", Alert.AlertType.ERROR);
        });
    }

    @FXML public void modifClient(ActionEvent e) {
        Client selected = tableClients.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        showDialog(selected).ifPresent(c -> {
            if (!gestionnaire.modifier(c))
                showAlert("Erreur", "Impossible de modifier le client.", Alert.AlertType.ERROR);
            else tableClients.refresh();
        });
    }

    @FXML public void suppClient(ActionEvent e) {
        Client selected = tableClients.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer le client " + selected + " ?", ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(r -> {
            if (r == ButtonType.YES && !gestionnaire.supprimer(selected))
                showAlert("Erreur", "Impossible de supprimer ce client (réservations existantes?).", Alert.AlertType.ERROR);
        });
    }

    private Optional<Client> showDialog(Client existing) {
        Dialog<Client> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Nouveau Client" : "Modifier le Client");
        dialog.setHeaderText(null);
        ButtonType saveBtn = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        TextField fNom       = new TextField();
        TextField fPrenom    = new TextField();
        TextField fEmail     = new TextField();
        TextField fTel       = new TextField();
        TextField fAdresse   = new TextField();

        fNom.setPromptText("Nom *");
        fPrenom.setPromptText("Prénom *");
        fEmail.setPromptText("email@exemple.com");
        fTel.setPromptText("0600000000");
        fAdresse.setPromptText("Adresse");

        if (existing != null) {
            fNom.setText(existing.getClientNom());
            fPrenom.setText(existing.getClientPrenom());
            fEmail.setText(existing.getClientEmail());
            fTel.setText(existing.getClientTelephone());
        }

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(12);
        grid.setPadding(new Insets(20, 30, 10, 10));
        grid.add(new Label("Nom :"),       0, 0); grid.add(fNom,     1, 0);
        grid.add(new Label("Prénom :"),    0, 1); grid.add(fPrenom,  1, 1);
        grid.add(new Label("Email :"),     0, 2); grid.add(fEmail,   1, 2);
        grid.add(new Label("Téléphone :"), 0, 3); grid.add(fTel,     1, 3);
        grid.add(new Label("Adresse :"),   0, 4); grid.add(fAdresse, 1, 4);
        dialog.getDialogPane().setContent(grid);

        Node save = dialog.getDialogPane().lookupButton(saveBtn);
        save.setDisable(true);
        Runnable validate = () -> save.setDisable(
                fNom.getText().trim().isEmpty() || fPrenom.getText().trim().isEmpty());
        fNom.textProperty().addListener((o, ov, nv) -> validate.run());
        fPrenom.textProperty().addListener((o, ov, nv) -> validate.run());

        dialog.setResultConverter(btn -> {
            if (btn != saveBtn) return null;
            if (existing != null) {
                existing.setClientNom(fNom.getText().trim());
                existing.setClientPrenom(fPrenom.getText().trim());
                existing.setClientEmail(fEmail.getText().trim());
                existing.setClientTelephone(fTel.getText().trim());
                return existing;
            }
            return new Client("CLI-" + System.currentTimeMillis(),
                    fNom.getText().trim(), fPrenom.getText().trim(),
                    fEmail.getText().trim(), fTel.getText().trim());
        });
        return dialog.showAndWait();
    }

    private void showAlert(String titre, String msg, Alert.AlertType type) {
        Alert a = new Alert(type); a.setTitle(titre); a.setHeaderText(null);
        a.setContentText(msg); a.showAndWait();
    }
}

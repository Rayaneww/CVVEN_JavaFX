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
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import model.listeChambres;
import model.listeClients;
import model.listeReservations;
import Metier.*;
import util.NavigationUtils; // Si vous utilisez cette classe utilitaire, sinon retirez cet import

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.ResourceBundle;

public class ReservationController implements Initializable {

    @FXML private BorderPane mainContainer;
    @FXML private TableView<Reservation> tableReservations;
    
    // J'ai retiré colId et colStatut
    @FXML private TableColumn<Reservation, String> colClient, colChambre, colDateDebut, colDateFin, colNuits;
    @FXML private TextField txtRecherche;
    
    // J'ai retiré le comboStatut
    @FXML private Button btnAjouter, btnModifier, btnSupprimer, btnDeconnexion;
    @FXML private Label lblUtilisateur;

    private listeReservations gestionnaire;
    private listeClients gestionnaireClients;
    private listeChambres gestionnaireChambres;
    private FilteredList<Reservation> listeFiltree;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        gestionnaire         = new listeReservations();
        gestionnaireClients  = new listeClients();
        gestionnaireChambres = new listeChambres();

        // Plus de setCellValueFactory pour l'ID et le statut
        colClient.setCellValueFactory(cd -> cd.getValue().clientNomProperty());
        colChambre.setCellValueFactory(cd -> cd.getValue().chambreInfoProperty());
        
        colDateDebut.setCellValueFactory(cd ->
            new SimpleStringProperty(cd.getValue().getResaDateDebut() != null
                ? cd.getValue().getResaDateDebut().format(FMT) : ""));
                
        colDateFin.setCellValueFactory(cd ->
            new SimpleStringProperty(cd.getValue().getResaDateFin() != null
                ? cd.getValue().getResaDateFin().format(FMT) : ""));
                
        colNuits.setCellValueFactory(cd ->
            new SimpleStringProperty(String.valueOf(cd.getValue().getNbNuits())));

        // Optionnel : Gestion de session utilisateur (si vous l'avez gardée)
        // UserSession session = UserSession.getInstance();
        // if (session != null && lblUtilisateur != null)
        //     lblUtilisateur.setText(session.toString());

        listeFiltree = new FilteredList<>(gestionnaire.getReservations(), p -> true);
        txtRecherche.textProperty().addListener((obs, o, n) -> filtrer());

        SortedList<Reservation> sorted = new SortedList<>(listeFiltree);
        sorted.comparatorProperty().bind(tableReservations.comparatorProperty());
        tableReservations.setItems(sorted);

        tableReservations.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            btnModifier.setDisable(n == null);
            btnSupprimer.setDisable(n == null);
        });
    }

    private void filtrer() {
        String s = txtRecherche.getText().toLowerCase();
        
        listeFiltree.setPredicate(r -> {
            if (s.isEmpty()) return true;
            
            return r.getClient().toString().toLowerCase().contains(s)
                || (r.getChambre() != null && r.getChambre().getChambEmplacement().toLowerCase().contains(s))
                || (r.getChambre() != null && String.valueOf(r.getChambre().getChambNumero()).contains(s));
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
        // UserSession.cleanSession();
        NavigationUtils.goTo("/view/FXMLLogin.fxml", "CVVEN - Authentification", false,
                (Stage) mainContainer.getScene().getWindow());
    }

    // --- CRUD ---
    @FXML public void ajtReservation(ActionEvent e) {
        showDialog(null).ifPresent(r -> {
            if (!gestionnaire.ajouter(r))
                showAlert("Erreur", "Impossible d'ajouter la réservation.", Alert.AlertType.ERROR);
        });
    }

    @FXML public void modifReservation(ActionEvent e) {
        Reservation selected = tableReservations.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        showDialog(selected).ifPresent(r -> {
            if (!gestionnaire.modifier(r)) showAlert("Erreur", "Impossible de modifier.", Alert.AlertType.ERROR);
            else tableReservations.refresh();
        });
    }

    @FXML public void suppReservation(ActionEvent e) {
        Reservation selected = tableReservations.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        
        // J'ai modifié le texte de l'alerte pour ne plus afficher l'ID
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer la réservation de " + selected.getClient().toString() + " ?", ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(r -> {
            if (r == ButtonType.YES && !gestionnaire.supprimer(selected))
                showAlert("Erreur", "Impossible de supprimer cette réservation.", Alert.AlertType.ERROR);
        });
    }

    private Optional<Reservation> showDialog(Reservation existing) {
        Dialog<Reservation> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Nouvelle Réservation" : "Modifier la Réservation");
        dialog.setHeaderText(null);
        ButtonType saveBtn = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        ComboBox<Client>  comboClient  = new ComboBox<>();
        ComboBox<Chambre> comboChambre = new ComboBox<>();
        DatePicker dpDebut = new DatePicker();
        DatePicker dpFin   = new DatePicker();

        comboClient.setItems(gestionnaireClients.getClients());
        comboClient.setConverter(new StringConverter<Client>() {
            @Override public String toString(Client c) { return c == null ? "" : c.toString(); }
            @Override public Client fromString(String s) { return null; }
        });
        
        comboChambre.setItems(gestionnaireChambres.getChambres());
        comboChambre.setConverter(new StringConverter<Chambre>() {
            @Override public String toString(Chambre c) {
                return c == null ? "" : "N°" + c.getChambNumero() + " - " + c.getChambEmplacement();
            }
            @Override public Chambre fromString(String s) { return null; }
        });

        if (existing != null) {
            comboClient.setValue(existing.getClient());
            comboChambre.setValue(existing.getChambre());
            dpDebut.setValue(existing.getResaDateDebut());
            dpFin.setValue(existing.getResaDateFin());
        }

        comboClient.setMaxWidth(Double.MAX_VALUE);
        comboChambre.setMaxWidth(Double.MAX_VALUE);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(12);
        grid.setPadding(new Insets(20, 30, 10, 10));
        grid.add(new Label("Client :"),  0, 0); grid.add(comboClient,  1, 0);
        grid.add(new Label("Chambre :"), 0, 1); grid.add(comboChambre, 1, 1);
        grid.add(new Label("Arrivée :"), 0, 2); grid.add(dpDebut,      1, 2);
        grid.add(new Label("Départ :"),  0, 3); grid.add(dpFin,        1, 3);
        dialog.getDialogPane().setContent(grid);

        Node save = dialog.getDialogPane().lookupButton(saveBtn);
        save.setDisable(true);
        
        Runnable validate = () -> save.setDisable(
                comboClient.getValue() == null || comboChambre.getValue() == null
                || dpDebut.getValue() == null || dpFin.getValue() == null
                || (dpDebut.getValue() != null && dpFin.getValue() != null
                    && !dpFin.getValue().isAfter(dpDebut.getValue())));
                    
        comboClient.valueProperty().addListener((o, ov, nv) -> validate.run());
        comboChambre.valueProperty().addListener((o, ov, nv) -> validate.run());
        dpDebut.valueProperty().addListener((o, ov, nv) -> validate.run());
        dpFin.valueProperty().addListener((o, ov, nv) -> validate.run());

        dialog.setResultConverter(btn -> {
            if (btn != saveBtn) return null;
            
            if (existing != null) {
                existing.setClient(comboClient.getValue());
                existing.setChambre(comboChambre.getValue());
                existing.setResaDateDebut(dpDebut.getValue());
                existing.setResaDateFin(dpFin.getValue());
                return existing;
            }
            
            // J'ai mis à jour le constructeur ici pour correspondre à votre classe métier
            return new Reservation(comboClient.getValue(), comboChambre.getValue(), dpDebut.getValue(), dpFin.getValue());
        });
        
        return dialog.showAndWait();
    }

    private void showAlert(String titre, String msg, Alert.AlertType type) {
        Alert a = new Alert(type); 
        a.setTitle(titre); 
        a.setHeaderText(null);
        a.setContentText(msg); 
        a.showAndWait();
    }
}
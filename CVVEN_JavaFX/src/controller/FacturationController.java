package controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import model.listeReservations;
import Metier.Reservation;
import Metier.UserSession; // À garder si vous l'utilisez
import dbConnection.SingletonConnection;
import util.NavigationUtils;

import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

public class FacturationController implements Initializable {

    @FXML private BorderPane mainContainer;
    @FXML private TableView<Reservation> tableReservations;
    
    // J'ai retiré colId et colStatut
    @FXML private TableColumn<Reservation, String> colClient, colChambre, colDateDebut, colDateFin, colNuits, colMontant, colStatutFacture;
    @FXML private TextField txtRecherche;
    @FXML private Button btnGenerer, btnVoir, btnDeconnexion;
    @FXML private Label lblUtilisateur;

    private listeReservations gestionnaire;
    private FilteredList<Reservation> listeFiltree;
    
    /** clé composite (user_id + "_" + chamb_id) → montant facturé */
    private final Map<String, Double> facturesMap = new HashMap<>();
    /** clé composite (user_id + "_" + chamb_id) → fact_id */
    private final Map<String, String> factIdMap   = new HashMap<>();

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Méthode utilitaire pour générer une clé unique pour chaque réservation
    private String getUniqueKey(Reservation r) {
        if (r == null || r.getClient() == null || r.getChambre() == null) return "";
        return r.getClient().getClientId() + "_" + r.getChambre().getChambId();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        gestionnaire = new listeReservations();
        chargerFactures();

        colClient.setCellValueFactory(cd -> cd.getValue().clientNomProperty());
        colChambre.setCellValueFactory(cd -> cd.getValue().chambreInfoProperty());
        
        colDateDebut.setCellValueFactory(cd -> new SimpleStringProperty(
                cd.getValue().getResaDateDebut() != null ? cd.getValue().getResaDateDebut().format(FMT) : ""));
                
        colDateFin.setCellValueFactory(cd -> new SimpleStringProperty(
                cd.getValue().getResaDateFin() != null ? cd.getValue().getResaDateFin().format(FMT) : ""));
                
        colNuits.setCellValueFactory(cd -> new SimpleStringProperty(
                String.valueOf(cd.getValue().getNbNuits())));
                
        colMontant.setCellValueFactory(cd -> {
            String uniqueKey = getUniqueKey(cd.getValue());
            if (facturesMap.containsKey(uniqueKey)) {
                return new SimpleStringProperty(String.format("%.2f €", facturesMap.get(uniqueKey)));
            }
            return new SimpleStringProperty("À définir");
        });
        
        colStatutFacture.setCellValueFactory(cd -> new SimpleStringProperty(
                facturesMap.containsKey(getUniqueKey(cd.getValue())) ? "Facturée" : "Non facturée"));

        // Optionnel : Gestion de la session
        // UserSession session = UserSession.getInstance();
        // if (session != null && lblUtilisateur != null)
        //     lblUtilisateur.setText(session.toString());

        listeFiltree = new FilteredList<>(gestionnaire.getReservations(), p -> true);
        txtRecherche.textProperty().addListener((obs, o, n) -> filtrer());

        SortedList<Reservation> sorted = new SortedList<>(listeFiltree);
        sorted.comparatorProperty().bind(tableReservations.comparatorProperty());
        tableReservations.setItems(sorted);

        tableReservations.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            if (n == null) { btnGenerer.setDisable(true); btnVoir.setDisable(true); return; }
            boolean aFacture = facturesMap.containsKey(getUniqueKey(n));
            btnGenerer.setDisable(aFacture);
            btnVoir.setDisable(!aFacture);
        });
    }

    private void chargerFactures() {
        facturesMap.clear(); factIdMap.clear();
        // CORRECTION SQL : On utilise user_id et chamb_id
        String sql = "SELECT fact_id, user_id, chamb_id, fact_montant FROM Facture";
        try (PreparedStatement ps = SingletonConnection.getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String uniqueKey = rs.getString("user_id") + "_" + rs.getString("chamb_id");
                facturesMap.put(uniqueKey, rs.getDouble("fact_montant"));
                factIdMap.put(uniqueKey, rs.getString("fact_id"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
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

    @FXML public void genererFacture(ActionEvent e) {
        Reservation r = tableReservations.getSelectionModel().getSelectedItem();
        String uniqueKey = getUniqueKey(r);
        
        if (r == null || facturesMap.containsKey(uniqueKey)) return;

        double montant = 0;

        // Puisqu'il n'y a pas de prix en base de données, on demande systématiquement le montant
        TextInputDialog input = new TextInputDialog("0.00");
        input.setTitle("Montant de la facture");
        input.setHeaderText("Pour le séjour de " + r.getClient().toString());
        input.setContentText("Prix total (€) :");
        
        Optional<String> res = input.showAndWait();
        if (res.isEmpty()) return;
        
        try { 
            montant = Double.parseDouble(res.get().replace(',', '.')); 
        } catch (NumberFormatException ex) {
            showAlert("Erreur", "Montant invalide.", Alert.AlertType.WARNING); 
            return;
        }

        String factId = "FACT-" + System.currentTimeMillis();
        // CORRECTION SQL : On insère la facture avec user_id et chamb_id
        String sql = "INSERT INTO Facture (fact_id, user_id, chamb_id, fact_date, fact_montant) VALUES (?, ?, ?, ?, ?)";
        
        try (PreparedStatement ps = SingletonConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, factId);
            ps.setString(2, r.getClient().getClientId());
            ps.setString(3, r.getChambre().getChambId());
            ps.setDate(4, Date.valueOf(LocalDate.now()));
            ps.setDouble(5, montant);
            ps.executeUpdate();
            
            facturesMap.put(uniqueKey, montant);
            factIdMap.put(uniqueKey, factId);
            tableReservations.refresh();
            
            btnGenerer.setDisable(true);
            btnVoir.setDisable(false);
            
            afficherRecu(r, factId, montant, LocalDate.now());
        } catch (SQLException ex) {
            ex.printStackTrace();
            showAlert("Erreur", "Impossible de créer la facture.", Alert.AlertType.ERROR);
        }
    }

    @FXML public void voirFacture(ActionEvent e) {
        Reservation r = tableReservations.getSelectionModel().getSelectedItem();
        String uniqueKey = getUniqueKey(r);
        
        if (r == null || !facturesMap.containsKey(uniqueKey)) return;

        LocalDate factDate = LocalDate.now();
        // CORRECTION SQL : On cherche par user_id et chamb_id
        String sql = "SELECT fact_date FROM Facture WHERE user_id=? AND chamb_id=?";
        try (PreparedStatement ps = SingletonConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, r.getClient().getClientId());
            ps.setString(2, r.getChambre().getChambId());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) factDate = rs.getDate("fact_date").toLocalDate();
        } catch (SQLException ex) { ex.printStackTrace(); }

        afficherRecu(r, factIdMap.get(uniqueKey), facturesMap.get(uniqueKey), factDate);
    }

    private void afficherRecu(Reservation r, String factId, double montant, LocalDate factDate) {
        String sep  = "─".repeat(44);
        String line = String.format(
            "╔%s╗%n" +
            "║%44s║%n" +
            "║%44s║%n" +
            "╠%s╣%n" +
            "║ %-42s ║%n" +
            "║ %-42s ║%n" +
            "╠%s╣%n" +
            "║ %-42s ║%n" +
            "║ %-42s ║%n" +
            "║ %-42s ║%n" +
            "╠%s╣%n" +
            "║ %-42s ║%n" +
            "║ %-42s ║%n" +
            "║ %-42s ║%n" +
            "╠%s╣%n" +
            "║ %-42s ║%n" +
            "║ %-42s ║%n" +
            "║ %-42s ║%n" +
            "╠%s╣%n" +
            "║ %-42s ║%n" +
            "║ %-42s ║%n" +
            "║                                            ║%n" +
            "║ %-42s ║%n" +
            "╚%s╝",
            sep,
            centre("FACTURE  —  CVVEN HÔTEL", 44),
            centre("", 44),
            sep,
            "N° Facture : " + factId,
            "Date       : " + factDate.format(FMT),
            sep,
            "CLIENT",
            "Nom        : " + r.getClient().getClientPrenom() + " " + r.getClient().getClientNom(),
            "Téléphone  : " + r.getClient().getClientTelephone(),
            sep,
            "CHAMBRE",
            "Numéro     : " + r.getChambre().getChambNumero(),
            "Type       : " + (r.getChambre().getType_chambre() != null
                ? r.getChambre().getType_chambre().getType_libelle() : "N/A"),
            sep,
            "SÉJOUR",
            "Arrivée    : " + r.getResaDateDebut().format(FMT),
            "Départ     : " + r.getResaDateFin().format(FMT),
            sep,
            "Durée      : " + r.getNbNuits() + " nuit(s)",
            "Statut     : Payé",
            "TOTAL      : " + String.format("%.2f €", montant),
            sep
        );

        TextArea ta = new TextArea(line);
        ta.setEditable(false);
        ta.setFont(javafx.scene.text.Font.font("Monospaced", 13));
        ta.setPrefSize(500, 520);

        Dialog<Void> d = new Dialog<>();
        d.setTitle("Facture N° " + factId);
        d.getDialogPane().setContent(ta);
        d.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        d.showAndWait();
    }

    private String centre(String text, int width) {
        if (text.length() >= width) return text;
        int pad = (width - text.length()) / 2;
        return " ".repeat(pad) + text + " ".repeat(width - text.length() - pad);
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

    private void showAlert(String titre, String msg, Alert.AlertType type) {
        Alert a = new Alert(type); 
        a.setTitle(titre); 
        a.setHeaderText(null);
        a.setContentText(msg); 
        a.showAndWait();
    }
}
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
import model.listePersonnel;
import Metier.Personnel;
import Metier.UserSession;
import util.NavigationUtils;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class PersonnelController implements Initializable {

    @FXML private BorderPane mainContainer;
    @FXML private TableView<Personnel> tablePersonnel;
    @FXML private TableColumn<Personnel, String> colLogin, colNom, colPrenom, colMail, colTelephone, colRole;
    @FXML private TextField txtRecherche;
    @FXML private Button btnAjouter, btnModifier, btnSupprimer, btnDeconnexion;
    @FXML private Label lblUtilisateur;

    private listePersonnel gestionnaire;
    private FilteredList<Personnel> listeFiltree;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        UserSession session = UserSession.getInstance();
        if (session != null && lblUtilisateur != null)
            lblUtilisateur.setText(session.toString());

        boolean isAdmin = session != null && "administrateur".equals(session.getRole());
        if (!isAdmin) {
            btnAjouter.setDisable(true);
            showAlert("Accès restreint", "Seuls les administrateurs peuvent gérer le personnel.", Alert.AlertType.WARNING);
        }

        gestionnaire = new listePersonnel();

        colLogin.setCellValueFactory(new PropertyValueFactory<>("userLogin"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("userNom"));
        colPrenom.setCellValueFactory(new PropertyValueFactory<>("userPrenom"));
        colMail.setCellValueFactory(new PropertyValueFactory<>("userMail"));
        colTelephone.setCellValueFactory(new PropertyValueFactory<>("userTelephone"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("userRole"));

        listeFiltree = new FilteredList<>(gestionnaire.getPersonnel(), p -> true);
        txtRecherche.textProperty().addListener((obs, o, n) -> {
            String s = n.toLowerCase();
            listeFiltree.setPredicate(p ->
                s.isEmpty()
                || p.getUserNom().toLowerCase().contains(s)
                || p.getUserPrenom().toLowerCase().contains(s)
                || p.getUserLogin().toLowerCase().contains(s)
                || p.getUserRole().toLowerCase().contains(s));
        });
        SortedList<Personnel> sorted = new SortedList<>(listeFiltree);
        sorted.comparatorProperty().bind(tablePersonnel.comparatorProperty());
        tablePersonnel.setItems(sorted);

        tablePersonnel.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            boolean sel = n != null && isAdmin;
            btnModifier.setDisable(!sel);
            // Empêcher la suppression de son propre compte
            boolean canDelete = sel && (session == null || !n.getUserLogin().equals(session.getNom()));
            btnSupprimer.setDisable(!canDelete);
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
    @FXML public void ajtPersonnel(ActionEvent e) {
        showDialog(null).ifPresent(result -> {
            Personnel p = (Personnel) result[0];
            String pwd  = (String) result[1];
            if (!gestionnaire.ajouter(p, pwd))
                showAlert("Erreur", "Impossible d'ajouter (login ou email déjà utilisé?).", Alert.AlertType.ERROR);
        });
    }

    @FXML public void modifPersonnel(ActionEvent e) {
        Personnel selected = tablePersonnel.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        showDialog(selected).ifPresent(result -> {
            Personnel p = (Personnel) result[0];
            String pwd  = (String) result[1];
            if (!gestionnaire.modifier(p, pwd))
                showAlert("Erreur", "Impossible de modifier.", Alert.AlertType.ERROR);
            else tablePersonnel.refresh();
        });
    }

    @FXML public void suppPersonnel(ActionEvent e) {
        Personnel selected = tablePersonnel.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer le compte « " + selected.getUserLogin() + " » ?", ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(r -> {
            if (r == ButtonType.YES && !gestionnaire.supprimer(selected))
                showAlert("Erreur", "Impossible de supprimer ce compte.", Alert.AlertType.ERROR);
        });
    }

    // Retourne Object[] { Personnel, String password }
    private Optional<Object[]> showDialog(Personnel existing) {
        Dialog<Object[]> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Nouveau Compte" : "Modifier le Compte");
        dialog.setHeaderText(null);
        ButtonType saveBtn = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        TextField fLogin = new TextField();
        TextField fNom   = new TextField();
        TextField fPrenom = new TextField();
        TextField fMail  = new TextField();
        TextField fTel   = new TextField();
        ComboBox<String> comboRole = new ComboBox<>();
        PasswordField fPwd = new PasswordField();
        Label lblHint = new Label();

        fLogin.setPromptText("login *");
        fNom.setPromptText("Nom *");
        fPrenom.setPromptText("Prénom *");
        fMail.setPromptText("email@exemple.com *");
        fTel.setPromptText("0600000000 *");
        comboRole.getItems().addAll("administrateur", "personnel");

        if (existing != null) {
            fLogin.setText(existing.getUserLogin());
            fLogin.setDisable(true);
            fNom.setText(existing.getUserNom());
            fPrenom.setText(existing.getUserPrenom());
            fMail.setText(existing.getUserMail());
            fTel.setText(existing.getUserTelephone());
            comboRole.setValue(existing.getUserRole());
            lblHint.setText("(laisser vide pour ne pas changer le mot de passe)");
        } else {
            comboRole.setValue("personnel");
            lblHint.setText("(obligatoire)");
        }
        lblHint.setStyle("-fx-font-size:11px;-fx-text-fill:#7f8c8d;");

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(12);
        grid.setPadding(new Insets(20, 30, 10, 10));
        grid.add(new Label("Login :"),        0, 0); grid.add(fLogin,    1, 0);
        grid.add(new Label("Nom :"),          0, 1); grid.add(fNom,      1, 1);
        grid.add(new Label("Prénom :"),       0, 2); grid.add(fPrenom,   1, 2);
        grid.add(new Label("Email :"),        0, 3); grid.add(fMail,     1, 3);
        grid.add(new Label("Téléphone :"),    0, 4); grid.add(fTel,      1, 4);
        grid.add(new Label("Rôle :"),         0, 5); grid.add(comboRole, 1, 5);
        grid.add(new Label("Mot de passe :"), 0, 6); grid.add(fPwd,      1, 6);
        grid.add(lblHint,                     1, 7);
        dialog.getDialogPane().setContent(grid);

        Node save = dialog.getDialogPane().lookupButton(saveBtn);
        save.setDisable(true);
        Runnable validate = () -> {
            boolean base = !fNom.getText().trim().isEmpty()
                    && !fPrenom.getText().trim().isEmpty()
                    && !fMail.getText().trim().isEmpty()
                    && !fTel.getText().trim().isEmpty()
                    && comboRole.getValue() != null;
            boolean pwdOk = existing != null || !fPwd.getText().isEmpty();
            if (existing == null) base = base && !fLogin.getText().trim().isEmpty();
            save.setDisable(!(base && pwdOk));
        };
        fNom.textProperty().addListener((o, ov, nv) -> validate.run());
        fPrenom.textProperty().addListener((o, ov, nv) -> validate.run());
        fLogin.textProperty().addListener((o, ov, nv) -> validate.run());
        fMail.textProperty().addListener((o, ov, nv) -> validate.run());
        fTel.textProperty().addListener((o, ov, nv) -> validate.run());
        fPwd.textProperty().addListener((o, ov, nv) -> validate.run());
        comboRole.valueProperty().addListener((o, ov, nv) -> validate.run());

        dialog.setResultConverter(btn -> {
            if (btn != saveBtn) return null;
            if (existing != null) {
                existing.setUserNom(fNom.getText().trim());
                existing.setUserPrenom(fPrenom.getText().trim());
                existing.setUserMail(fMail.getText().trim());
                existing.setUserTelephone(fTel.getText().trim());
                existing.setUserRole(comboRole.getValue());
                return new Object[]{ existing, fPwd.getText() };
            }
            String newId = "USR-" + System.currentTimeMillis();
            Personnel p = new Personnel(newId, fLogin.getText().trim(),
                    fNom.getText().trim(), fPrenom.getText().trim(),
                    fMail.getText().trim(), fTel.getText().trim(),
                    comboRole.getValue());
            return new Object[]{ p, fPwd.getText() };
        });
        return dialog.showAndWait();
    }

    private void showAlert(String titre, String msg, Alert.AlertType type) {
        Alert a = new Alert(type); a.setTitle(titre); a.setHeaderText(null);
        a.setContentText(msg); a.showAndWait();
    }
}

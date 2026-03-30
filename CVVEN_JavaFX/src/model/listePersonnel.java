package model;

import java.sql.*;
import org.mindrot.jbcrypt.BCrypt;
import dbConnection.SingletonConnection;
import javafx.collections.*;
import Metier.Personnel;

public class listePersonnel {

    private final ObservableList<Personnel> lePersonnel = FXCollections.observableArrayList();
    private final Connection connection;

    public listePersonnel() {
        this.connection = SingletonConnection.getConnection();
        charger();
    }

    public ObservableList<Personnel> getPersonnel() { return lePersonnel; }

    private void charger() {
        lePersonnel.clear();
        String sql = "SELECT user_id, user_login, user_nom, user_prenom, user_mail, user_telephone, user_role " +
                     "FROM Utilisateur WHERE user_role IN ('personnel', 'administrateur') ORDER BY user_nom";
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lePersonnel.add(new Personnel(
                    rs.getString("user_id"),
                    rs.getString("user_login"),
                    rs.getString("user_nom"),
                    rs.getString("user_prenom"),
                    rs.getString("user_mail"),
                    rs.getString("user_telephone"),
                    rs.getString("user_role")));
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public boolean ajouter(Personnel p, String password) {
        String hash = "$2y$" + BCrypt.hashpw(password, BCrypt.gensalt(10)).substring(4);
        String sql = "INSERT INTO Utilisateur (user_id, user_login, user_nom, user_prenom, user_mail, user_telephone, user_mdp, user_role) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, p.getUserId());
            ps.setString(2, p.getUserLogin());
            ps.setString(3, p.getUserNom());
            ps.setString(4, p.getUserPrenom());
            ps.setString(5, p.getUserMail());
            ps.setString(6, p.getUserTelephone());
            ps.setString(7, hash);
            ps.setString(8, p.getUserRole());
            if (ps.executeUpdate() > 0) { lePersonnel.add(p); return true; }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean modifier(Personnel p, String newPassword) {
        try {
            PreparedStatement ps;
            if (newPassword != null && !newPassword.isEmpty()) {
                String hash = "$2y$" + BCrypt.hashpw(newPassword, BCrypt.gensalt(10)).substring(4);
                ps = connection.prepareStatement(
                    "UPDATE Utilisateur SET user_nom=?, user_prenom=?, user_mail=?, user_telephone=?, user_role=?, user_mdp=? " +
                    "WHERE user_id=?");
                ps.setString(1, p.getUserNom());
                ps.setString(2, p.getUserPrenom());
                ps.setString(3, p.getUserMail());
                ps.setString(4, p.getUserTelephone());
                ps.setString(5, p.getUserRole());
                ps.setString(6, hash);
                ps.setString(7, p.getUserId());
            } else {
                ps = connection.prepareStatement(
                    "UPDATE Utilisateur SET user_nom=?, user_prenom=?, user_mail=?, user_telephone=?, user_role=? " +
                    "WHERE user_id=?");
                ps.setString(1, p.getUserNom());
                ps.setString(2, p.getUserPrenom());
                ps.setString(3, p.getUserMail());
                ps.setString(4, p.getUserTelephone());
                ps.setString(5, p.getUserRole());
                ps.setString(6, p.getUserId());
            }
            boolean ok = ps.executeUpdate() > 0;
            ps.close();
            if (ok) {
                int idx = lePersonnel.indexOf(p);
                if (idx >= 0) lePersonnel.set(idx, p);
                return true;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean supprimer(Personnel p) {
        String sql = "DELETE FROM Utilisateur WHERE user_id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, p.getUserId());
            if (ps.executeUpdate() > 0) { lePersonnel.remove(p); return true; }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }
}

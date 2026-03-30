package model;

import java.sql.*;
import dbConnection.SingletonConnection;
import javafx.collections.*;
import Metier.Client;

public class listeClients {

    private final ObservableList<Client> lesClients = FXCollections.observableArrayList();
    private final Connection connection;

    public listeClients() {
        this.connection = SingletonConnection.getConnection();
        charger();
    }

    public ObservableList<Client> getClients() { return lesClients; }

    private void charger() {
        lesClients.clear();
        // CORRECTION 1 : On suppose que le rôle "Client" correspond à la valeur 1 dans user_role
        String sql = "SELECT * FROM Utilisateur WHERE user_role = 'client'";
        
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
             
            while (rs.next()) {
                lesClients.add(new Client(
                    rs.getString("user_id"),
                    rs.getString("user_nom"),
                    rs.getString("user_prenom"),
                    rs.getString("user_mail"), // CORRECTION 2 : user_mail au lieu de user_email
                    rs.getString("user_telephone")
                    // ATTENTION : J'ai retiré l'adresse car elle n'est pas dans votre table Utilisateur !
                    // Il faudra la retirer du constructeur de votre classe Client.
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean ajouter(Client c) {
        // CORRECTION 3 : On insère dans Utilisateur avec les bonnes colonnes
        // ATTENTION : user_login et user_mdp sont obligatoires (NOT NULL) dans votre BDD !
        // Je les simule ici avec l'email et un mot de passe par défaut.
        String sql = "INSERT INTO Utilisateur (user_id, user_nom, user_prenom, user_mail, user_telephone, user_login, user_mdp, user_role) VALUES (?, ?, ?, ?, ?, ?, ?, 1)";
        
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, c.getClientId());
            ps.setString(2, c.getClientNom());
            ps.setString(3, c.getClientPrenom());
            ps.setString(4, c.getClientEmail());
            ps.setString(5, c.getClientTelephone());
            
            // Paramètres obligatoires de votre table Utilisateur que la classe Client n'a pas forcément :
            ps.setString(6, c.getClientEmail()); // On utilise l'email comme login par défaut
            ps.setString(7, "client123");        // Mot de passe par défaut

            if (ps.executeUpdate() > 0) { 
                lesClients.add(c); 
                return true; 
            }
        } catch (SQLException e) { 
            e.printStackTrace(); 
        }
        return false;
    }

    public boolean modifier(Client c) {
        // CORRECTION 4 : Mise à jour de la table Utilisateur
        String sql = "UPDATE Utilisateur SET user_nom=?, user_prenom=?, user_mail=?, user_telephone=? WHERE user_id=?";
        
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, c.getClientNom());
            ps.setString(2, c.getClientPrenom());
            ps.setString(3, c.getClientEmail());
            ps.setString(4, c.getClientTelephone());
            ps.setString(5, c.getClientId());
            
            if (ps.executeUpdate() > 0) {
                int idx = lesClients.indexOf(c);
                if (idx >= 0) lesClients.set(idx, c);
                return true;
            }
        } catch (SQLException e) { 
            e.printStackTrace(); 
        }
        return false;
    }

    public boolean supprimer(Client c) {
        // CORRECTION 5 : Suppression dans Utilisateur
        String sql = "DELETE FROM Utilisateur WHERE user_id=?";
        
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, c.getClientId());
            if (ps.executeUpdate() > 0) { 
                lesClients.remove(c); 
                return true; 
            }
        } catch (SQLException e) { 
            e.printStackTrace(); 
        }
        return false;
    }
}
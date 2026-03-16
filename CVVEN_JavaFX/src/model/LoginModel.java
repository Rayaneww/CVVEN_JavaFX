package model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import dbConnection.SingletonConnection;
import Metier.UserSession;

public class LoginModel {

    public boolean isLoginValidDatabase(String user, String pass) {
        Connection conn = SingletonConnection.getConnection();
        if (conn == null) return false;

        // On récupère aussi le Nom, Prénom et Rôle
        String sql = "SELECT user_nom, user_prenom, user_role FROM Utilisateur " +
                     "WHERE user_login = ? AND user_mdp = ? " +
                     "AND user_role IN ('administrateur', 'personnel')";

        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, user);
            pst.setString(2, pass);

            ResultSet rs = pst.executeQuery();
            
            if (rs.next()) {
                // SI CONNEXION RÉUSSIE : On remplit la session
                String nom = rs.getString("user_nom");
                String prenom = rs.getString("user_prenom");
                String role = rs.getString("user_role");
                
                // Création de la session globale
                UserSession.createSession(nom, prenom, role);
                
                return true;
            }
            return false;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
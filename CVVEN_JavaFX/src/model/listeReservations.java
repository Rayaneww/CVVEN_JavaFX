package model;

import java.sql.*;
import dbConnection.SingletonConnection;
import javafx.collections.*;
import Metier.*;

public class listeReservations {

    private final ObservableList<Reservation> lesReservations = FXCollections.observableArrayList();
    private final Connection connection;

    public listeReservations() {
        this.connection = SingletonConnection.getConnection();
        charger();
    }

    public ObservableList<Reservation> getReservations() { return lesReservations; }

    private void charger() {
        lesReservations.clear();
        
        // CORRECTION 1 : Adaptation aux tables Reserve, Utilisateur et Chambre
        String sql =
            "SELECT r.reser_dateDebut, r.reser_dateFin, " +
            "  u.user_id, u.user_nom, u.user_prenom, u.user_mail, u.user_telephone, " +
            "  ch.chamb_id, ch.chamb_numero, ch.chamb_emplacement, ch.chamb_remarque, " +
            "  tc.type_id, tc.type_libelle, tc.type_desc " +
            "FROM Reserve r " +
            "JOIN Utilisateur u ON r.user_id = u.user_id " +
            "JOIN Chambre ch ON r.chamb_id = ch.chamb_id " +
            "JOIN Type_Chambre tc ON ch.type_id = tc.type_id " +
            "ORDER BY r.reser_dateDebut DESC";

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
             
            while (rs.next()) {
                // Création du Client (Basé sur la classe Client mise à jour)
                Client client = new Client(
                    rs.getString("user_id"), 
                    rs.getString("user_nom"),
                    rs.getString("user_prenom"), 
                    rs.getString("user_mail"),
                    rs.getString("user_telephone")
                );
                
                // Création du Type de Chambre (Sans le prix car n'existe pas dans la BDD)
                type_Chambre type = new type_Chambre(
                    rs.getInt("type_id"), 
                    rs.getString("type_libelle"),
                    rs.getString("type_desc")
                );
                
                // Création de la Chambre (Comme dans listeChambres)
                Chambre chambre = new Chambre(
                    rs.getString("chamb_id"), 
                    rs.getString("chamb_emplacement"),
                    rs.getInt("chamb_numero"), 
                    rs.getString("chamb_remarque")
                );
                chambre.setType_chambre(type); // On lie le type à la chambre

                // CORRECTION 2 : Création de la Réservation (Sans ID unique ni statut)
                lesReservations.add(new Reservation(
                    client, 
                    chambre,
                    rs.getDate("reser_dateDebut").toLocalDate(),
                    rs.getDate("reser_dateFin").toLocalDate()
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean ajouter(Reservation r) {
        // CORRECTION 3 : Insertion dans la table Reserve
        String sql = "INSERT INTO Reserve (user_id, chamb_id, reser_dateDebut, reser_dateFin) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, r.getClient().getClientId());
            ps.setString(2, r.getChambre().getChambId());
            ps.setDate(3, Date.valueOf(r.getResaDateDebut()));
            ps.setDate(4, Date.valueOf(r.getResaDateFin()));
            
            if (ps.executeUpdate() > 0) { 
                lesReservations.add(r); 
                return true; 
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean modifier(Reservation r) {
        // CORRECTION 4 : Mise à jour en utilisant la clé composite (user_id + chamb_id)
        String sql = "UPDATE Reserve SET reser_dateDebut=?, reser_dateFin=? WHERE user_id=? AND chamb_id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(r.getResaDateDebut()));
            ps.setDate(2, Date.valueOf(r.getResaDateFin()));
            ps.setString(3, r.getClient().getClientId());
            ps.setString(4, r.getChambre().getChambId());
            
            if (ps.executeUpdate() > 0) {
                int idx = lesReservations.indexOf(r);
                if (idx >= 0) lesReservations.set(idx, r);
                return true;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean supprimer(Reservation r) {
        // CORRECTION 5 : Suppression via la clé composite
        String sql = "DELETE FROM Reserve WHERE user_id=? AND chamb_id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, r.getClient().getClientId());
            ps.setString(2, r.getChambre().getChambId());
            
            if (ps.executeUpdate() > 0) { 
                lesReservations.remove(r); 
                return true; 
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }
}
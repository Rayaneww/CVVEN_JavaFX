package model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import dbConnection.SingletonConnection; 
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import Metier.Chambre; // Assure-toi que l'import est bon
import Metier.type_Chambre;

public class listeChambres {

	private ObservableList<Chambre> lesChambres;
	private Connection connection;

	public listeChambres() {
		this.lesChambres = FXCollections.observableArrayList();
		this.connection = SingletonConnection.getConnection();
		chargerDonneesDepuisBDD();
	}

	public ObservableList<Chambre> getChambres() {
		return lesChambres;
	}

	// --- READ (SELECT) ---
	private void chargerDonneesDepuisBDD() {
		lesChambres.clear();
		String sql = "SELECT * FROM Chambre";

		try {
			PreparedStatement ps = connection.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();

			while (rs.next()) {
				String remarque = rs.getString("chamb_remarque") != null
						? rs.getString("chamb_remarque")
								: "Pas de remarque";

				int type_id =rs.getInt("type_id");
				PreparedStatement ps2 = connection.prepareStatement("Select * from Type_Chambre where type_id = ?");
				ps2.setInt(1, type_id);
				ResultSet rs2 = ps2.executeQuery();


				// Correspondance exacte avec le constructeur de ta classe Chambre
				Chambre c = new Chambre(
						rs.getString("chamb_id"),          // ID (String)
						rs.getString("chamb_emplacement"), // Emplacement (String)
						rs.getInt("chamb_numero"),         // Numéro (int)
						remarque    // Remarque (String)
						// Type ID (int)
						);
				if(rs2.next()) {
					type_Chambre tc = new type_Chambre(
							rs.getInt("type_id"), 
							rs.getString("type_libelle"), 
							rs.getString("type_desc"));
					c.setType_chambre(tc);
				}

				lesChambres.add(c);

			}

		} catch (SQLException e) {
			e.printStackTrace();
			System.err.println("Erreur chargement SQL");
		}
	}

	// --- CREATE (INSERT) ---
	public boolean ajouterChambre(Chambre c) {
		// Adaptation aux colonnes de ta BDD
		String sql = "INSERT INTO chambre (chamb_id, chamb_numero, chamb_emplacement, chamb_remarque, type_id) VALUES (?, ?, ?, ?, ?)";

		try {
			PreparedStatement ps = connection.prepareStatement(sql);

			// Attention à l'ordre des ? dans la requête
			ps.setString(1, c.getChambId());
			ps.setInt(2, c.getChambNumero());
			ps.setString(3, c.getChambEmplacement());
			ps.setString(4, c.getChambRemarque());


			int resultat = ps.executeUpdate();

			if (resultat > 0) {
				lesChambres.add(c);
				return true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	// --- DELETE ---
	public boolean supprimerChambre(Chambre c) {
		// On supprime généralement via l'ID unique (Primary Key)
		String sql = "DELETE FROM chambre WHERE chamb_id = ?";

		try {
			PreparedStatement ps = connection.prepareStatement(sql);
			ps.setString(1, c.getChambId());

			int resultat = ps.executeUpdate();

			if (resultat > 0) {
				lesChambres.remove(c);
				return true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	// --- UPDATE ---
	public boolean modifierChambre(Chambre c) {
		// On met à jour les infos en ciblant l'ID
		String sql = "UPDATE chambre SET chamb_numero=?, chamb_emplacement=?, chamb_remarque=?, type_id=? WHERE chamb_id=?";

		try {
			PreparedStatement ps = connection.prepareStatement(sql);

			ps.setInt(1, c.getChambNumero());
			ps.setString(2, c.getChambEmplacement());
			ps.setString(3, c.getChambRemarque());


			// Le WHERE est le dernier paramètre (numéro 5)
			ps.setString(5, c.getChambId()); 

			int resultat = ps.executeUpdate();

			if (resultat > 0) {
				// Rafraichir l'élément dans la liste pour le TableView (optionnel si ObservableList gère les propriétés)
				int index = lesChambres.indexOf(c);
				if(index >= 0) {
					lesChambres.set(index, c);
				}
				return true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
}
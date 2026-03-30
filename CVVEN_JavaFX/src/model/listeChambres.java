package model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import dbConnection.SingletonConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import Metier.Chambre;
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

	public ObservableList<type_Chambre> getTypes() {
		ObservableList<type_Chambre> types = FXCollections.observableArrayList();
		String sql = "SELECT type_id, type_libelle, type_desc, type_prix FROM Type_Chambre ORDER BY type_libelle";
		try {
			PreparedStatement ps = connection.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				types.add(new type_Chambre(rs.getInt("type_id"), rs.getString("type_libelle"), rs.getString("type_desc"), rs.getDouble("type_prix")));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return types;
	}

	// --- READ (SELECT) ---
	private void chargerDonneesDepuisBDD() {
		lesChambres.clear();

		// CORRECTION 1 : On sélectionne absolument TOUT depuis les deux tables avec
		// l'étoile (*)
		String sql = "SELECT ch.chamb_id, ch.chamb_numero, ch.chamb_emplacement, ch.chamb_remarque, "
				+ "tc.type_id, tc.type_libelle, tc.type_desc, tc.type_prix "
				+ "FROM Chambre ch JOIN Type_Chambre tc ON ch.type_id = tc.type_id";

		try {
			PreparedStatement ps = connection.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();

			while (rs.next()) {
				String remarque = rs.getString("chamb_remarque") != null ? rs.getString("chamb_remarque")
						: "Pas de remarque";

				// Création de la Chambre
				Chambre c = new Chambre(rs.getString("chamb_id"), // ID (String)
						rs.getString("chamb_emplacement"), // Emplacement (String)
						rs.getInt("chamb_numero"), // Numéro (int)
						remarque // Remarque (String)
				);

				// Création du Type de Chambre directement avec les résultats de la jointure
				// (plus besoin de ps2/rs2)
				type_Chambre tc = new type_Chambre(rs.getInt("type_id"), rs.getString("type_libelle"),
						rs.getString("type_desc"), rs.getDouble("type_prix"));

				// On lie le type à la chambre
				c.setType_chambre(tc);

				lesChambres.add(c);
			}

		} catch (SQLException e) {
			e.printStackTrace();
			System.err.println("Erreur chargement SQL");
		}
	}

	// --- CREATE (INSERT) ---
	public boolean ajouterChambre(Chambre c) {
		String sql = "INSERT INTO Chambre (chamb_id, chamb_numero, chamb_emplacement, chamb_remarque, type_id) VALUES (?, ?, ?, ?, ?)";

		try {
			PreparedStatement ps = connection.prepareStatement(sql);

			ps.setString(1, c.getChambId());
			ps.setInt(2, c.getChambNumero());
			ps.setString(3, c.getChambEmplacement());
			ps.setString(4, c.getChambRemarque());

			// CORRECTION 2 : ps au lieu de pst, et on récupère l'ID via l'objet
			// type_Chambre
			// Note: Si 'getType_id()' ne s'appelle pas exactement comme ça dans votre
			// classe type_Chambre, ajustez le nom.
			ps.setInt(5, c.getType_chambre().getType_id());

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
		String sql = "DELETE FROM Chambre WHERE chamb_id = ?";

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
		String sql = "UPDATE Chambre SET chamb_numero=?, chamb_emplacement=?, chamb_remarque=?, type_id=? WHERE chamb_id=?";

		try {
			PreparedStatement ps = connection.prepareStatement(sql);

			ps.setInt(1, c.getChambNumero());
			ps.setString(2, c.getChambEmplacement());
			ps.setString(3, c.getChambRemarque());
			ps.setInt(4, c.getType_chambre().getType_id());
			ps.setString(5, c.getChambId());

			int resultat = ps.executeUpdate();

			if (resultat > 0) {
				int index = lesChambres.indexOf(c);
				if (index >= 0) {
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
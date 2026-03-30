package model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.mindrot.jbcrypt.BCrypt;
import dbConnection.SingletonConnection;
import Metier.UserSession;

public class LoginModel {

	public boolean isLoginValidDatabase(String user, String pass) {
		Connection conn = SingletonConnection.getConnection();
		if (conn == null)
			return false;

		String sql = "SELECT user_nom, user_prenom, user_role, user_mdp FROM Utilisateur "
				+ "WHERE user_login = ? AND user_role IN ('administrateur', 'personnel')";

		try (PreparedStatement pst = conn.prepareStatement(sql)) {
			pst.setString(1, user);

			ResultSet rs = pst.executeQuery();

			if (rs.next()) {
				String hash = rs.getString("user_mdp");

				// PHP stocke les hash en $2y$, Java jBCrypt attend $2a$ (algorithme identique)
				if (hash != null && hash.startsWith("$2y$")) {
					hash = "$2a$" + hash.substring(4);
				}

				if (!BCrypt.checkpw(pass, hash)) {
					return false;
				}

				UserSession.createSession(
						rs.getString("user_nom"),
						rs.getString("user_prenom"),
						rs.getString("user_role"));
				return true;
			}
			return false;

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
}
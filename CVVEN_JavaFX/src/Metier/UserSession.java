package Metier;

public class UserSession {

	// Instance unique (Singleton)
	private static UserSession instance;

	private String nom;
	private String prenom;
	private String role;

	private UserSession(String nom, String prenom, String role) {
		this.nom = nom;
		this.prenom = prenom;
		this.role = role;
	}

	// Méthode pour récupérer l'instance courante
	public static UserSession getInstance() {
		return instance;
	}

	// Méthode appelée lors de la connexion pour créer la session
	public static void createSession(String nom, String prenom, String role) {
		instance = new UserSession(nom, prenom, role);
	}

	// Méthode pour détruire la session (Déconnexion)
	public static void cleanSession() {
		instance = null;
	}

	public String getNom() {
		return nom;
	}

	public String getPrenom() {
		return prenom;
	}

	public String getRole() {
		return role;
	}

	@Override
	public String toString() {
		return prenom + " " + nom + " (" + role + ")";
	}
}
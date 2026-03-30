package Metier;

import javafx.beans.property.*;

public class Chambre {
	// Champs basés sur ton MPD [cite: 69, 70, 71, 72]
	private final StringProperty chambId;
	private final IntegerProperty chambNumero;
	private final StringProperty chambEmplacement;
	private final StringProperty chambRemarque;
	private type_Chambre type_chambre;

	public Chambre(String id, String emplacement, int numero, String remarque, type_Chambre type) {
		this.chambId = new SimpleStringProperty(id);
		this.chambNumero = new SimpleIntegerProperty(numero);
		this.chambEmplacement = new SimpleStringProperty(emplacement);
		this.chambRemarque = new SimpleStringProperty(remarque);
		this.type_chambre = type;
	}

	public Chambre(String id, String emplacement, int numero, String remarque) {
		this.chambId = new SimpleStringProperty(id);
		this.chambNumero = new SimpleIntegerProperty(numero);
		this.chambEmplacement = new SimpleStringProperty(emplacement);
		this.chambRemarque = new SimpleStringProperty(remarque);

	}

	// Getters pour les propriétés (Indispensable pour le TableView)
	public StringProperty chambIdProperty() {
		return chambId;
	}

	public IntegerProperty chambNumeroProperty() {
		return chambNumero;
	}

	public StringProperty chambEmplacementProperty() {
		return chambEmplacement;
	}

	public StringProperty chambRemarqueProperty() {
		return chambRemarque;
	}

	public type_Chambre typeLibelleProperty() {
		return type_chambre;
	}

	// Getters classiques
	public String getChambId() {
		return chambId.get();
	}

	public int getChambNumero() {
		return chambNumero.get();
	}

	public String getChambEmplacement() {
		return chambEmplacement.get();
	}

	public String getChambRemarque() {
		return chambRemarque.get();
	}

	public type_Chambre getType_chambre() {
		return type_chambre;
	}

	public void setType_chambre(type_Chambre type_chambre) {
		this.type_chambre = type_chambre;
	}

	public void setChambNumero(int numero) {
		chambNumero.set(numero);
	}

	public void setChambEmplacement(String emplacement) {
		chambEmplacement.set(emplacement);
	}

	public void setChambRemarque(String remarque) {
		chambRemarque.set(remarque);
	}

}
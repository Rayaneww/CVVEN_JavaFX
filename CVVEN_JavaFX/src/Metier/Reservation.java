package Metier;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import javafx.beans.property.*;

public class Reservation {

    private Client client;
    private Chambre chambre;
    private final ObjectProperty<LocalDate> resaDateDebut;
    private final ObjectProperty<LocalDate> resaDateFin;

    // Constructeur sans 'id' et sans 'statut'
    public Reservation(Client client, Chambre chambre, LocalDate debut, LocalDate fin) {
        this.client       = client;
        this.chambre      = chambre;
        this.resaDateDebut = new SimpleObjectProperty<>(debut);
        this.resaDateFin   = new SimpleObjectProperty<>(fin);
    }

    // Computed display properties for TableView
    public StringProperty clientNomProperty() {
        return new SimpleStringProperty(client != null ? client.toString() : "N/A");
    }

    public StringProperty chambreInfoProperty() {
        return new SimpleStringProperty(chambre != null
                ? "N°" + chambre.getChambNumero() + " - " + chambre.getChambEmplacement()
                : "N/A");
    }

    // Calcul du nombre de nuits
    public long getNbNuits() {
        if (resaDateDebut.get() == null || resaDateFin.get() == null) return 0;
        return ChronoUnit.DAYS.between(resaDateDebut.get(), resaDateFin.get());
    }

    // Properties pour les colonnes du TableView JavaFX
    public ObjectProperty<LocalDate> resaDateDebutProperty() { return resaDateDebut; }
    public ObjectProperty<LocalDate> resaDateFinProperty()   { return resaDateFin; }

    // Getters normaux
    public Client    getClient()       { return client; }
    public Chambre   getChambre()      { return chambre; }
    public LocalDate getResaDateDebut(){ return resaDateDebut.get(); }
    public LocalDate getResaDateFin()  { return resaDateFin.get(); }

    // Setters
    public void setClient(Client c)        { this.client = c; }
    public void setChambre(Chambre c)      { this.chambre = c; }
    public void setResaDateDebut(LocalDate d) { resaDateDebut.set(d); }
    public void setResaDateFin(LocalDate d)   { resaDateFin.set(d); }
}
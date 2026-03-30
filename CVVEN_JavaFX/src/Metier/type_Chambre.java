package Metier;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.*;

public class type_Chambre {

    private final IntegerProperty type_id;
    private final StringProperty  type_libelle;
    private final StringProperty  type_desc;
    private final DoubleProperty  type_prix;
    private List<Chambre> Chambres = new ArrayList<>();

    public type_Chambre(int type_id, String type_libelle, String type_desc) {
        this(type_id, type_libelle, type_desc, 0.0);
    }

    public type_Chambre(int type_id, String type_libelle, String type_desc, double prix) {
        this.type_id      = new SimpleIntegerProperty(type_id);
        this.type_libelle = new SimpleStringProperty(type_libelle);
        this.type_desc    = new SimpleStringProperty(type_desc);
        this.type_prix    = new SimpleDoubleProperty(prix);
    }

    public List<Chambre> getChambres()                  { return Chambres; }
    public void setChambres(List<Chambre> chambres)     { Chambres = chambres; }

    public int    getType_id()      { return type_id.get(); }
    public IntegerProperty type_idProperty() { return type_id; }

    public String getType_libelle() { return type_libelle.get(); }
    public StringProperty type_libelleProperty() { return type_libelle; }

    public String getType_desc()    { return type_desc.get(); }
    public StringProperty type_descProperty() { return type_desc; }

    public double getType_prix()    { return type_prix.get(); }
    public DoubleProperty type_prixProperty() { return type_prix; }
    public void setType_prix(double prix) { type_prix.set(prix); }

    @Override
    public String toString() { return type_libelle.get(); }
}

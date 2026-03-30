package Metier;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Client {

    private final StringProperty clientId;
    private final StringProperty clientNom;
    private final StringProperty clientPrenom;
    private final StringProperty clientEmail;
    private final StringProperty clientTelephone;
 

    public Client(String id, String nom, String prenom, String email, String telephone) {
        this.clientId        = new SimpleStringProperty(id);
        this.clientNom       = new SimpleStringProperty(nom);
        this.clientPrenom    = new SimpleStringProperty(prenom);
        this.clientEmail     = new SimpleStringProperty(email     != null ? email     : "");
        this.clientTelephone = new SimpleStringProperty(telephone != null ? telephone : "");

    }

    // Properties
    public StringProperty clientIdProperty()        { return clientId; }
    public StringProperty clientNomProperty()       { return clientNom; }
    public StringProperty clientPrenomProperty()    { return clientPrenom; }
    public StringProperty clientEmailProperty()     { return clientEmail; }
    public StringProperty clientTelephoneProperty() { return clientTelephone; }


    // Getters
    public String getClientId()        { return clientId.get(); }
    public String getClientNom()       { return clientNom.get(); }
    public String getClientPrenom()    { return clientPrenom.get(); }
    public String getClientEmail()     { return clientEmail.get(); }
    public String getClientTelephone() { return clientTelephone.get(); }


    // Setters
    public void setClientNom(String v)       { clientNom.set(v); }
    public void setClientPrenom(String v)    { clientPrenom.set(v); }
    public void setClientEmail(String v)     { clientEmail.set(v); }
    public void setClientTelephone(String v) { clientTelephone.set(v); }


    @Override
    public String toString() {
        return clientPrenom.get() + " " + clientNom.get();
    }
}

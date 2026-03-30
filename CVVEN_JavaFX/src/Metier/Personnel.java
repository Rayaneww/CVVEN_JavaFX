package Metier;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Personnel {

    private final StringProperty userId;
    private final StringProperty userLogin;
    private final StringProperty userNom;
    private final StringProperty userPrenom;
    private final StringProperty userMail;
    private final StringProperty userTelephone;
    private final StringProperty userRole;

    public Personnel(String id, String login, String nom, String prenom,
                     String mail, String telephone, String role) {
        this.userId      = new SimpleStringProperty(id);
        this.userLogin   = new SimpleStringProperty(login);
        this.userNom     = new SimpleStringProperty(nom);
        this.userPrenom  = new SimpleStringProperty(prenom);
        this.userMail    = new SimpleStringProperty(mail      != null ? mail      : "");
        this.userTelephone = new SimpleStringProperty(telephone != null ? telephone : "");
        this.userRole    = new SimpleStringProperty(role);
    }

    public StringProperty userIdProperty()        { return userId; }
    public StringProperty userLoginProperty()     { return userLogin; }
    public StringProperty userNomProperty()       { return userNom; }
    public StringProperty userPrenomProperty()    { return userPrenom; }
    public StringProperty userMailProperty()      { return userMail; }
    public StringProperty userTelephoneProperty() { return userTelephone; }
    public StringProperty userRoleProperty()      { return userRole; }

    public String getUserId()        { return userId.get(); }
    public String getUserLogin()     { return userLogin.get(); }
    public String getUserNom()       { return userNom.get(); }
    public String getUserPrenom()    { return userPrenom.get(); }
    public String getUserMail()      { return userMail.get(); }
    public String getUserTelephone() { return userTelephone.get(); }
    public String getUserRole()      { return userRole.get(); }

    public void setUserNom(String v)       { userNom.set(v); }
    public void setUserPrenom(String v)    { userPrenom.set(v); }
    public void setUserMail(String v)      { userMail.set(v); }
    public void setUserTelephone(String v) { userTelephone.set(v); }
    public void setUserRole(String v)      { userRole.set(v); }

    @Override
    public String toString() {
        return userPrenom.get() + " " + userNom.get() + " (" + userRole.get() + ")";
    }
}

package Metier;

import java.util.ArrayList;
import java.util.List;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class type_Chambre {
	
	    private final IntegerProperty type_id;
	    private final StringProperty type_libelle;
	    private final StringProperty type_desc;
	    private List<Chambre> Chambres = new ArrayList<Chambre>(); 
	    
	    
		public type_Chambre(int type_id, String type_libelle, String type_desc) {
			super();
			this.type_id = new SimpleIntegerProperty(type_id);
			this.type_libelle = new SimpleStringProperty(type_libelle);
			this.type_desc = new SimpleStringProperty(type_desc);
		}


		public List<Chambre> getChambres() {
			return Chambres;
		}


		public void setChambres(List<Chambre> chambres) {
			Chambres = chambres;
		}


		public IntegerProperty getType_id() {
			return type_id;
		}


		public StringProperty getType_libelle() {
			return type_libelle;
		}


		public StringProperty getType_desc() {
			return type_desc;
		}


		@Override
		public String toString() {
			return "type_Chambre [type_id=" + type_id + ", type_libelle=" + type_libelle + ", type_desc=" + type_desc
					+ ", Chambres=" + Chambres + "]";
		}
		
		
	    
	    

}

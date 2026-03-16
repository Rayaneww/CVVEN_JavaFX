package dbConnection;

import java.sql.Connection;
import java.sql.DriverManager;

public class SingletonConnection {


	private static Connection  connection ;

	static {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");

			connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/CVVEN", "root", "");

			//System.out.println("Connexion !");

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static Connection getConnection() {
		// TODO Auto-generated method stub
		return connection;
	}
}

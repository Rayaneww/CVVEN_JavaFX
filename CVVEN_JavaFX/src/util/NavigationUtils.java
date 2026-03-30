package util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class NavigationUtils {

    public static void goTo(String fxmlResource, String title, boolean resizable, Stage currentStage) {
        try {
            Parent root = FXMLLoader.load(NavigationUtils.class.getResource(fxmlResource));
            Stage newStage = new Stage();
            newStage.setTitle(title);
            newStage.setResizable(resizable);
            newStage.setScene(new Scene(root));
            newStage.show();
            if (currentStage != null) currentStage.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

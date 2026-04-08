package jdm.fx;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SceneManager {

    private static Stage stage;

    public static void setStage(Stage s) {
        stage = s;
    }

    public static void switchScene(String fxml, String title) {
        try {
            var url = SceneManager.class.getResource("/" + fxml);
            if (url == null) {
                throw new IllegalStateException("Missing FXML on classpath: " + fxml
                        + " (ensure .fxml is copied next to compiled classes).");
            }
            Parent root = FXMLLoader.load(url);
            stage.setTitle(title);
            stage.setScene(new Scene(root, 1024, 720));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
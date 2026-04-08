package jdm.fx;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import jdm.service.DataLoader;
import jdm.util.DataPaths;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) {
        String dataDir = DataPaths.resolveDataDir(getParameters().getRaw().toArray(new String[0]));
        try {
            DataLoader.load(dataDir);
            System.out.println("Data directory: " + new java.io.File(dataDir).getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setTitle("Data load error");
            a.setHeaderText("Could not load CSV dataset from: " + dataDir);
            a.setContentText(e.getMessage());
            a.showAndWait();
            Platform.exit();
            return;
        }

        SceneManager.setStage(stage);
        SceneManager.switchScene("login.fxml", "JDM Healthcare — Login");
    }

    public static void main(String[] args) {
        launch(args);
    }
}

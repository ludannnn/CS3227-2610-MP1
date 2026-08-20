package recall.gui;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * The entry point of the JavaFX application. Loads the main menu screen and
 * shows it in the primary window. Later phases will let controllers swap this
 * scene for the deck view and review screens.
 */
public class MainApp extends Application {

    @Override
    public void start(Stage stage) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(MainApp.class.getResource("/view/MainMenu.fxml"));
            Parent root = fxmlLoader.load();
            stage.setScene(new Scene(root));
            stage.setTitle("Recall");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

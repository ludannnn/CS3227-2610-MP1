package recall.gui;

import java.io.IOException;
import java.io.UncheckedIOException;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import recall.model.Deck;
import recall.storage.DeckRepository;

/**
 * Handles navigation between the application's screens. It owns the primary
 * {@link Stage}, loads each screen's FXML on demand, injects the shared
 * dependencies into the freshly created controller, and swaps the scene's root
 * so the window content changes without recreating the window.
 */
public class Navigator {

    private final Stage stage;
    private final DeckRepository repository;

    public Navigator(Stage stage, DeckRepository repository) {
        this.stage = stage;
        this.repository = repository;
    }

    /** Shows the main menu (the list of decks). */
    public void showMainMenu() {
        FXMLLoader loader = load("/view/MainMenu.fxml");
        MainMenuController controller = loader.getController();
        controller.init(this, repository);
    }

    /** Shows the contents of a single deck. */
    public void showDeckView(Deck deck) {
        FXMLLoader loader = load("/view/DeckView.fxml");
        DeckViewController controller = loader.getController();
        controller.init(this, repository, deck);
    }

    /** Starts a review session for a deck. */
    public void showReview(Deck deck) {
        FXMLLoader loader = load("/view/ReviewView.fxml");
        ReviewController controller = loader.getController();
        controller.init(this, repository, deck);
    }

    /**
     * Loads an FXML file and installs its root as the current screen. The first
     * call creates the scene; later calls swap only the root so the window keeps
     * its size and position.
     *
     * @param fxmlPath classpath location of the FXML file
     * @return the loader, so callers can retrieve the typed controller
     */
    private FXMLLoader load(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(Navigator.class.getResource(fxmlPath));
            Parent root = loader.load();
            if (stage.getScene() == null) {
                Scene scene = new Scene(root);
                scene.getStylesheets().add(Navigator.class.getResource("/view/style.css").toExternalForm());
                stage.setScene(scene);
            } else {
                stage.getScene().setRoot(root);
            }
            return loader;
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to load screen: " + fxmlPath, e);
        }
    }
}
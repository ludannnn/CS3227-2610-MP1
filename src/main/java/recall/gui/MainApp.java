package recall.gui;

import java.nio.file.Path;

import javafx.application.Application;
import javafx.stage.Stage;

import recall.storage.DeckRepository;
import recall.storage.Storage;

/**
 * The entry point of the JavaFX application. It wires up the storage and
 * repository, then hands control to the {@link Navigator}, which shows the main
 * menu.
 */
public class MainApp extends Application {

    private static final Path DATA_DIR = Path.of("data", "decks");

    @Override
    public void start(Stage stage) {
        DeckRepository repository = new DeckRepository(new Storage(DATA_DIR));
        Navigator navigator = new Navigator(stage, repository);

        stage.setTitle("Recall");
        navigator.showMainMenu();
        stage.show();
    }
}
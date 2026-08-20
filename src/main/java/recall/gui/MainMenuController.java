package recall.gui;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * Controller for the main menu screen.
 *
 * <p>At this stage it only proves that the FXML-to-controller wiring works.
 * Real deck management (listing decks, creating a deck, navigating into a deck)
 * is added in a later phase.
 */
public class MainMenuController {

    @FXML
    private Label statusLabel;

    /** Temporary handler wired to the "New Deck" button, to confirm the wiring. */
    @FXML
    private void handleNewDeck() {
        statusLabel.setText("New Deck clicked — the button is wired up correctly.");
    }
}

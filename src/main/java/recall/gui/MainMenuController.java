package recall.gui;

import java.time.LocalDate;
import java.util.Optional;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import recall.model.Deck;
import recall.storage.DeckRepository;

/**
 * Controller for the main menu: lists every deck with its due count, and lets
 * the user create, open, or delete decks.
 */
public class MainMenuController {

    @FXML
    private ListView<Deck> deckList;
    @FXML
    private TextField newDeckField;
    @FXML
    private Label messageLabel;

    private Navigator navigator;
    private DeckRepository repository;

    /** Injects dependencies and populates the deck list. Called by the navigator. */
    public void init(Navigator navigator, DeckRepository repository) {
        this.navigator = navigator;
        this.repository = repository;
        deckList.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(Deck deck, boolean empty) {
                super.updateItem(deck, empty);
                if (empty || deck == null) {
                    setText(null);
                } else {
                    int due = deck.countDue(LocalDate.now());
                    setText(deck.getName() + "   (" + due + " due / " + deck.size() + " cards)");
                }
            }
        });
        refresh();
    }

    private void refresh() {
        deckList.getItems().setAll(repository.getDecks());
    }

    @FXML
    private void handleCreate() {
        String name = newDeckField.getText() == null ? "" : newDeckField.getText().trim();
        try {
            Deck created = repository.createDeck(name);
            newDeckField.clear();
            refresh();
            deckList.getSelectionModel().select(created);
            message("Created deck \"" + created.getName() + "\".");
        } catch (IllegalArgumentException e) {
            message(e.getMessage());
        }
    }

    @FXML
    private void handleOpen() {
        Deck selected = deckList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            message("Select a deck to open.");
            return;
        }
        navigator.showDeckView(selected);
    }

    @FXML
    private void handleDelete() {
        Deck selected = deckList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            message("Select a deck to delete.");
            return;
        }
        Alert confirm = new Alert(AlertType.CONFIRMATION,
                "Delete deck \"" + selected.getName() + "\" and all its cards?",
                ButtonType.OK, ButtonType.CANCEL);
        confirm.setHeaderText(null);
        Optional<ButtonType> choice = confirm.showAndWait();
        if (choice.isPresent() && choice.get() == ButtonType.OK) {
            repository.deleteDeck(selected.getName());
            refresh();
            message("Deleted deck \"" + selected.getName() + "\".");
        }
    }

    private void message(String text) {
        messageLabel.setText(text);
    }
}
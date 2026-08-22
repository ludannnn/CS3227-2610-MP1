package recall.gui;

import java.time.LocalDate;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import recall.model.Deck;
import recall.model.Flashcard;
import recall.storage.DeckRepository;

/**
 * Controller for a single deck: lists its cards, adds and deletes cards, and
 * launches a review session.
 */
public class DeckViewController {

    @FXML
    private Label titleLabel;
    @FXML
    private ListView<Flashcard> cardList;
    @FXML
    private TextField frontField;
    @FXML
    private TextField backField;
    @FXML
    private Label messageLabel;

    private Navigator navigator;
    private DeckRepository repository;
    private Deck deck;

    /** Injects dependencies and populates the card list. Called by the navigator. */
    public void init(Navigator navigator, DeckRepository repository, Deck deck) {
        this.navigator = navigator;
        this.repository = repository;
        this.deck = deck;
        cardList.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(Flashcard card, boolean empty) {
                super.updateItem(card, empty);
                if (empty || card == null) {
                    setText(null);
                } else {
                    setText(preview(card.getFront()) + "  ->  " + preview(card.getBack()));
                }
            }
        });
        refresh();
    }

    private void refresh() {
        titleLabel.setText(deck.getName() + "   (" + deck.countDue(LocalDate.now()) + " due / "
                + deck.size() + " cards)");
        cardList.getItems().setAll(deck.getCards());
    }

    @FXML
    private void handleAdd() {
        String front = frontField.getText() == null ? "" : frontField.getText().trim();
        String back = backField.getText() == null ? "" : backField.getText().trim();
        if (front.isEmpty() || back.isEmpty()) {
            message("Both the front and back are required.");
            return;
        }
        deck.addCard(new Flashcard(front, back));
        repository.save(deck);
        frontField.clear();
        backField.clear();
        refresh();
        message("Card added.");
    }

    @FXML
    private void handleDelete() {
        int index = cardList.getSelectionModel().getSelectedIndex();
        if (index < 0) {
            message("Select a card to delete.");
            return;
        }
        deck.removeCard(index);
        repository.save(deck);
        refresh();
        message("Card deleted.");
    }

    @FXML
    private void handleReview() {
        if (deck.countDue(LocalDate.now()) == 0) {
            message("Nothing is due in this deck right now.");
            return;
        }
        navigator.showReview(deck);
    }

    @FXML
    private void handleBack() {
        navigator.showMainMenu();
    }

    private static String preview(String text) {
        String singleLine = text.replaceAll("\\s+", " ");
        return singleLine.length() > 40 ? singleLine.substring(0, 39) + "\u2026" : singleLine;
    }

    private void message(String text) {
        messageLabel.setText(text);
    }
}
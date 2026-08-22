package recall.gui;

import java.time.LocalDate;
import java.util.Optional;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

import recall.model.Deck;
import recall.model.DeckStats;
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

    @FXML
    private void handleEdit() {
        Flashcard selected = cardList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            message("Select a card to edit.");
            return;
        }

        TextField front = new TextField(selected.getFront());
        TextField back = new TextField(selected.getBack());
        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(8);
        grid.add(new Label("Front:"), 0, 0);
        grid.add(front, 1, 0);
        grid.add(new Label("Back:"), 0, 1);
        grid.add(back, 1, 1);

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit card");
        dialog.setHeaderText(null);
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Optional<ButtonType> choice = dialog.showAndWait();
        if (choice.isEmpty() || choice.get() != ButtonType.OK) {
            return;
        }
        String newFront = front.getText() == null ? "" : front.getText().trim();
        String newBack = back.getText() == null ? "" : back.getText().trim();
        if (newFront.isEmpty() || newBack.isEmpty()) {
            message("Both the front and back are required; edit cancelled.");
            return;
        }
        selected.setFront(newFront);
        selected.setBack(newBack);
        repository.save(deck);
        refresh();
        message("Card updated.");
    }

    @FXML
    private void handleStats() {
        DeckStats stats = deck.stats(LocalDate.now());
        String body = String.format(
                "Total cards: %d%n"
                + "Due now: %d%n%n"
                + "New: %d%n"
                + "Young: %d%n"
                + "Mature (interval >= %d days): %d%n%n"
                + "Average ease: %.2f",
                stats.total(), stats.due(), stats.newCards(), stats.young(),
                DeckStats.MATURE_INTERVAL_DAYS, stats.mature(), stats.averageEase());
        Alert alert = new Alert(AlertType.INFORMATION, body, ButtonType.OK);
        alert.setTitle("Statistics");
        alert.setHeaderText(deck.getName());
        alert.showAndWait();
    }

    private static String preview(String text) {
        String singleLine = text.replaceAll("\\s+", " ");
        return singleLine.length() > 40 ? singleLine.substring(0, 39) + "\u2026" : singleLine;
    }

    private void message(String text) {
        messageLabel.setText(text);
    }
}
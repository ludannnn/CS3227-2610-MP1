package recall.gui;

import java.time.LocalDate;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

import recall.model.Deck;
import recall.srs.Grade;
import recall.srs.ReviewSession;
import recall.srs.Sm2Scheduler;
import recall.storage.DeckRepository;

/**
 * Controller for the review screen. Walks through the deck's due cards one at a
 * time: shows the front, reveals the back on request, then applies the grade the
 * user picks and moves on. The card's schedule is saved after every grade.
 */
public class ReviewController {

    @FXML
    private Label progressLabel;
    @FXML
    private Label frontLabel;
    @FXML
    private Label backLabel;
    @FXML
    private Button showAnswerButton;
    @FXML
    private HBox ratingBox;

    private Navigator navigator;
    private DeckRepository repository;
    private Deck deck;
    private ReviewSession session;

    /** Injects dependencies, starts the session, and shows the first card. */
    public void init(Navigator navigator, DeckRepository repository, Deck deck) {
        this.navigator = navigator;
        this.repository = repository;
        this.deck = deck;
        this.session = new ReviewSession(deck, new Sm2Scheduler(), LocalDate.now());
        showCurrent();
    }

    private void showCurrent() {
        if (session.isFinished()) {
            progressLabel.setText("Done");
            frontLabel.setText("All caught up \u2014 " + session.reviewedCount() + " card(s) reviewed.");
            setVisible(backLabel, false);
            setVisible(showAnswerButton, false);
            setVisible(ratingBox, false);
            return;
        }
        progressLabel.setText("Card " + (session.reviewedCount() + 1) + " of " + session.total());
        frontLabel.setText(session.current().getFront());
        backLabel.setText(session.current().getBack());
        setVisible(backLabel, false);
        setVisible(showAnswerButton, true);
        setVisible(ratingBox, false);
    }

    @FXML
    private void handleShowAnswer() {
        setVisible(backLabel, true);
        setVisible(showAnswerButton, false);
        setVisible(ratingBox, true);
    }

    @FXML
    private void handleAgain() {
        grade(Grade.AGAIN);
    }

    @FXML
    private void handleHard() {
        grade(Grade.HARD);
    }

    @FXML
    private void handleGood() {
        grade(Grade.GOOD);
    }

    @FXML
    private void handleEasy() {
        grade(Grade.EASY);
    }

    private void grade(Grade grade) {
        session.grade(grade);
        repository.save(deck);
        showCurrent();
    }

    @FXML
    private void handleBack() {
        navigator.showDeckView(deck);
    }

    /** Toggles both visibility and layout management so hidden nodes take no space. */
    private static void setVisible(Node node, boolean visible) {
        node.setVisible(visible);
        node.setManaged(visible);
    }
}
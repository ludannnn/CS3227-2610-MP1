package recall.srs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import recall.model.Deck;
import recall.model.Flashcard;

public class ReviewSessionTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 8, 20);

    private final Sm2Scheduler scheduler = new Sm2Scheduler();

    private Deck deckWithTwoDueOneNotDue() {
        Deck deck = new Deck("Test");
        deck.addCard(new Flashcard("due1", "x", 2.5, 1, 1, TODAY.minusDays(1)));
        deck.addCard(new Flashcard("notdue", "y", 2.5, 10, 2, TODAY.plusDays(5)));
        deck.addCard(new Flashcard("due2", "z", 2.5, 1, 1, TODAY));
        return deck;
    }

    @Test
    public void session_queuesOnlyDueCards() {
        ReviewSession session = new ReviewSession(deckWithTwoDueOneNotDue(), scheduler, TODAY);
        assertEquals(2, session.total());
        assertEquals(2, session.remaining());
        assertFalse(session.isFinished());
    }

    @Test
    public void grading_advancesThroughTheQueue() {
        ReviewSession session = new ReviewSession(deckWithTwoDueOneNotDue(), scheduler, TODAY);
        session.grade(Grade.GOOD);
        assertEquals(1, session.reviewedCount());
        assertEquals(1, session.remaining());
        session.grade(Grade.GOOD);
        assertTrue(session.isFinished());
    }

    @Test
    public void grading_updatesTheCardSchedule() {
        Deck deck = deckWithTwoDueOneNotDue();
        ReviewSession session = new ReviewSession(deck, scheduler, TODAY);
        session.grade(Grade.GOOD);
        session.grade(Grade.GOOD);
        assertEquals(0, deck.countDue(TODAY)); // both due cards pushed into the future
    }

    @Test
    public void current_whenFinished_throws() {
        Deck empty = new Deck("Empty");
        empty.addCard(new Flashcard("future", "x", 2.5, 10, 2, TODAY.plusDays(3)));
        ReviewSession session = new ReviewSession(empty, scheduler, TODAY);
        assertTrue(session.isFinished());
        assertThrows(IllegalStateException.class, session::current);
    }

    @Test
    public void grade_whenFinished_throws() {
        ReviewSession session = new ReviewSession(new Deck("Empty"), scheduler, TODAY);
        assertThrows(IllegalStateException.class, () -> session.grade(Grade.GOOD));
    }
}
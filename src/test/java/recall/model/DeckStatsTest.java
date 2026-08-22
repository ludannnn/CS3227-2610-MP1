package recall.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

public class DeckStatsTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 8, 22);

    @Test
    public void stats_classifiesNewYoungMatureAndCountsDue() {
        Deck deck = new Deck("Mix");
        deck.addCard(new Flashcard("new", "x", 2.5, 0, 0, TODAY));                 // new, due
        deck.addCard(new Flashcard("young", "x", 2.4, 6, 2, TODAY.plusDays(3)));   // young, not due
        deck.addCard(new Flashcard("mature", "x", 2.6, 30, 5, TODAY));             // mature, due
        deck.addCard(new Flashcard("boundary", "x", 2.5, 21, 4, TODAY.plusDays(10))); // mature (==21)

        DeckStats stats = deck.stats(TODAY);
        assertEquals(4, stats.total());
        assertEquals(2, stats.due());
        assertEquals(1, stats.newCards());
        assertEquals(1, stats.young());
        assertEquals(2, stats.mature());
        assertEquals(2.5, stats.averageEase(), 1e-9);
    }

    @Test
    public void stats_emptyDeck_hasZeroAverageEase() {
        DeckStats stats = new Deck("Empty").stats(TODAY);
        assertEquals(0, stats.total());
        assertEquals(0.0, stats.averageEase(), 1e-9);
    }
}
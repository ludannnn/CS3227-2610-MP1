package recall.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

public class DeckTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 8, 20);

    private Flashcard card(String front, LocalDate due) {
        return new Flashcard(front, "answer", 2.5, 1, 1, due);
    }

    @Test
    public void addAndSize_reflectCardCount() {
        Deck deck = new Deck("Spanish");
        assertTrue(deck.isEmpty());
        deck.addCard(card("uno", TODAY));
        deck.addCard(card("dos", TODAY));
        assertEquals(2, deck.size());
    }

    @Test
    public void removeCard_removesAndReturnsCard() {
        Deck deck = new Deck("Spanish");
        Flashcard first = card("uno", TODAY);
        deck.addCard(first);
        deck.addCard(card("dos", TODAY));
        assertEquals(first, deck.removeCard(0));
        assertEquals(1, deck.size());
    }

    @Test
    public void dueCards_returnsOnlyCardsDueTodayOrEarlier() {
        Deck deck = new Deck("Spanish");
        deck.addCard(card("yesterday", TODAY.minusDays(1)));
        deck.addCard(card("today", TODAY));
        deck.addCard(card("tomorrow", TODAY.plusDays(1)));
        List<Flashcard> due = deck.dueCards(TODAY);
        assertEquals(2, due.size());
        assertEquals(2, deck.countDue(TODAY));
    }

    @Test
    public void find_matchesFrontOrBackIgnoringCase() {
        Deck deck = new Deck("Spanish");
        deck.addCard(new Flashcard("Gato", "cat", 2.5, 1, 1, TODAY));
        deck.addCard(new Flashcard("perro", "DOG", 2.5, 1, 1, TODAY));
        assertEquals(1, deck.find("GATO").size());   // front, different case
        assertEquals(1, deck.find("dog").size());     // back, different case
        assertEquals(0, deck.find("bird").size());
    }

    @Test
    public void getCards_returnsUnmodifiableView() {
        Deck deck = new Deck("Spanish");
        deck.addCard(card("uno", TODAY));
        assertThrows(UnsupportedOperationException.class, () -> deck.getCards().clear());
    }
}

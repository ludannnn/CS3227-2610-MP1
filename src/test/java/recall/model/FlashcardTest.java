package recall.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

public class FlashcardTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 8, 20);

    @Test
    public void newCard_hasFreshSm2State() {
        Flashcard card = new Flashcard("hola", "hello");
        assertEquals(Flashcard.DEFAULT_EASE, card.getEaseFactor());
        assertEquals(0, card.getInterval());
        assertEquals(0, card.getRepetitions());
    }

    @Test
    public void isDue_dueDateTodayOrPast_returnsTrue() {
        Flashcard past = new Flashcard("a", "b", 2.5, 1, 1, TODAY.minusDays(1));
        Flashcard onDay = new Flashcard("a", "b", 2.5, 1, 1, TODAY);
        assertTrue(past.isDue(TODAY));
        assertTrue(onDay.isDue(TODAY));
    }

    @Test
    public void isDue_dueDateInFuture_returnsFalse() {
        Flashcard future = new Flashcard("a", "b", 2.5, 10, 2, TODAY.plusDays(3));
        assertFalse(future.isDue(TODAY));
    }

    @Test
    public void saveLine_roundTrip_preservesAllFields() {
        Flashcard original = new Flashcard("capital of France", "Paris", 2.6, 15, 3, TODAY);
        Flashcard restored = Flashcard.fromSaveLine(original.toSaveLine());
        assertEquals(original, restored);
    }

    @Test
    public void saveLine_roundTrip_handlesDelimiterAndEscapeCharacters() {
        // Front contains the '|' delimiter and a newline; back contains a backslash.
        Flashcard original = new Flashcard("a | b\nsecond line", "path is C:\\temp", 2.5, 1, 1, TODAY);
        Flashcard restored = Flashcard.fromSaveLine(original.toSaveLine());
        assertEquals(original, restored);
        assertEquals("a | b\nsecond line", restored.getFront());
        assertEquals("path is C:\\temp", restored.getBack());
    }

    @Test
    public void fromSaveLine_wrongFieldCount_throws() {
        assertThrows(IllegalArgumentException.class, () -> Flashcard.fromSaveLine("only|three|fields"));
    }

    @Test
    public void constructor_blankFront_throws() {
        assertThrows(IllegalArgumentException.class, () -> new Flashcard("   ", "back"));
    }
}

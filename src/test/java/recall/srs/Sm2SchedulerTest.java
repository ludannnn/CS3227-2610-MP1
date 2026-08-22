package recall.srs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import recall.model.Flashcard;

public class Sm2SchedulerTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 8, 20);
    private static final double EPS = 1e-9;

    private final Sm2Scheduler scheduler = new Sm2Scheduler();

    private Flashcard card(double ease, int interval, int reps) {
        return new Flashcard("q", "a", ease, interval, reps, TODAY);
    }

    @Test
    public void firstSuccessfulReview_intervalIsOneDay() {
        Flashcard c = card(2.5, 0, 0);
        scheduler.review(c, Grade.GOOD, TODAY);
        assertEquals(1, c.getInterval());
        assertEquals(1, c.getRepetitions());
        assertEquals(TODAY.plusDays(1), c.getDueDate());
    }

    @Test
    public void secondSuccessfulReview_intervalIsSixDays() {
        Flashcard c = card(2.5, 1, 1);
        scheduler.review(c, Grade.GOOD, TODAY);
        assertEquals(6, c.getInterval());
        assertEquals(2, c.getRepetitions());
    }

    @Test
    public void thirdSuccessfulReview_intervalIsPreviousTimesEase() {
        Flashcard c = card(2.5, 6, 2);
        scheduler.review(c, Grade.GOOD, TODAY);
        assertEquals(15, c.getInterval()); // ceil(6 * 2.5)
        assertEquals(3, c.getRepetitions());
    }

    @Test
    public void good_leavesEaseUnchanged() {
        Flashcard c = card(2.5, 6, 2);
        scheduler.review(c, Grade.GOOD, TODAY);
        assertEquals(2.5, c.getEaseFactor(), EPS);
    }

    @Test
    public void easy_raisesEase() {
        Flashcard c = card(2.5, 6, 2);
        scheduler.review(c, Grade.EASY, TODAY);
        assertEquals(2.6, c.getEaseFactor(), EPS);
    }

    @Test
    public void hard_lowersEase() {
        Flashcard c = card(2.5, 6, 2);
        scheduler.review(c, Grade.HARD, TODAY);
        assertEquals(2.36, c.getEaseFactor(), EPS);
    }

    @Test
    public void again_resetsRepetitionsAndInterval() {
        Flashcard c = card(2.5, 15, 3);
        scheduler.review(c, Grade.AGAIN, TODAY);
        assertEquals(0, c.getRepetitions());
        assertEquals(1, c.getInterval());
        assertEquals(TODAY.plusDays(1), c.getDueDate());
    }

    @Test
    public void easeNeverDropsBelowFloor() {
        Flashcard c = card(1.3, 15, 3);
        scheduler.review(c, Grade.AGAIN, TODAY);
        assertTrue(c.getEaseFactor() >= Flashcard.MIN_EASE);
        assertEquals(Flashcard.MIN_EASE, c.getEaseFactor(), EPS);
    }
}
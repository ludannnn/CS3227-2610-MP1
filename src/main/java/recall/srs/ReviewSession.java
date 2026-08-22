package recall.srs;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import recall.model.Deck;
import recall.model.Flashcard;

/**
 * A single pass through the cards due in a deck on a given day.
 *
 * <p>The session snapshots the deck's due cards at construction, then hands them
 * out one at a time. Grading the current card updates its schedule via the
 * {@link SchedulingStrategy} and advances to the next. The session mutates the
 * cards but does not persist them — the caller saves the deck once the session
 * ends.
 *
 * <p>This is a deliberately simple one-pass model: a lapsed card is rescheduled
 * for the future rather than repeated again within the same session (a
 * simplification of canonical SM-2's same-day re-repetition of poorly graded
 * items).
 */
public class ReviewSession {

    private final SchedulingStrategy scheduler;
    private final LocalDate today;
    private final List<Flashcard> queue;
    private int index;

    /**
     * Starts a review session over the cards in {@code deck} that are due on
     * {@code today}.
     */
    public ReviewSession(Deck deck, SchedulingStrategy scheduler, LocalDate today) {
        Objects.requireNonNull(deck, "deck");
        this.scheduler = Objects.requireNonNull(scheduler, "scheduler");
        this.today = Objects.requireNonNull(today, "today");
        this.queue = new ArrayList<>(deck.dueCards(today));
        this.index = 0;
    }

    /** Returns whether every due card has been graded. */
    public boolean isFinished() {
        return index >= queue.size();
    }

    /** The total number of cards this session started with. */
    public int total() {
        return queue.size();
    }

    /** The number of cards not yet graded, including the current one. */
    public int remaining() {
        return queue.size() - index;
    }

    /** The number of cards graded so far. */
    public int reviewedCount() {
        return index;
    }

    /**
     * Returns the card currently up for review.
     *
     * @throws IllegalStateException if the session is already finished
     */
    public Flashcard current() {
        if (isFinished()) {
            throw new IllegalStateException("No card to review; the session is finished");
        }
        return queue.get(index);
    }

    /**
     * Applies a grade to the current card and advances to the next.
     *
     * @throws IllegalStateException if the session is already finished
     */
    public void grade(Grade grade) {
        Objects.requireNonNull(grade, "grade");
        if (isFinished()) {
            throw new IllegalStateException("Cannot grade; the session is finished");
        }
        scheduler.review(current(), grade, today);
        index++;
    }
}
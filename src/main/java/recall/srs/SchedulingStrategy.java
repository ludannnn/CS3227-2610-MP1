package recall.srs;

import java.time.LocalDate;

import recall.model.Flashcard;

/**
 * Strategy for updating a card's scheduling state after it is reviewed.
 * Abstracting this behind an interface keeps {@link ReviewSession} independent
 * of any one algorithm, so an alternative scheduler could be dropped in without
 * touching the review flow.
 */
public interface SchedulingStrategy {

    /**
     * Updates the card's ease, interval, repetition count, and due date in place
     * based on how well it was recalled.
     *
     * @param card  the reviewed card (mutated)
     * @param grade the learner's recall quality
     * @param today the date of the review, used to compute the next due date
     */
    void review(Flashcard card, Grade grade, LocalDate today);
}
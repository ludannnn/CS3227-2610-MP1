package recall.srs;

import java.time.LocalDate;
import java.util.Objects;

import recall.model.Flashcard;

/**
 * The SM-2 spaced-repetition scheduler (Woźniak, 1987).
 *
 * <p>Intervals follow the canonical schedule: the first successful review is due
 * in 1 day, the second in 6 days, and thereafter the interval is multiplied by
 * the card's ease factor (fractions rounded up). After every review the ease
 * factor is adjusted by the canonical formula and floored at
 * {@link Flashcard#MIN_EASE}.
 *
 * <p><b>Fail-case decision.</b> The canonical description contains a well-known
 * tension: it says to modify the ease factor after <em>every</em> repetition,
 * yet also says a failed card (quality &lt; 3) restarts "without changing the
 * E-Factor". This implementation follows the common reference implementations
 * and updates the ease factor on every grade, including failures, while
 * resetting the repetition count and interval. The strict alternative — leaving
 * ease untouched on a lapse — reduces "ease hell" (ease drifting permanently
 * downward) but diverges from those reference implementations. The choice is
 * isolated here so it is easy to change.
 */
public class Sm2Scheduler implements SchedulingStrategy {

    private static final int SECOND_INTERVAL = 6;

    @Override
    public void review(Flashcard card, Grade grade, LocalDate today) {
        Objects.requireNonNull(card, "card");
        Objects.requireNonNull(grade, "grade");
        Objects.requireNonNull(today, "today");

        int quality = grade.quality();
        int repetitions = card.getRepetitions();
        int interval = card.getInterval();
        double ease = card.getEaseFactor();

        if (quality >= Grade.PASSING_THRESHOLD) {
            switch (repetitions) {
            case 0:
                interval = 1;
                break;
            case 1:
                interval = SECOND_INTERVAL;
                break;
            default:
                interval = (int) Math.ceil(interval * ease);
            }
            repetitions += 1;
        } else {
            repetitions = 0;
            interval = 1;
        }

        ease = ease + (0.1 - (5 - quality) * (0.08 + (5 - quality) * 0.02));
        if (ease < Flashcard.MIN_EASE) {
            ease = Flashcard.MIN_EASE;
        }

        card.setRepetitions(repetitions);
        card.setInterval(interval);
        card.setEaseFactor(ease);
        card.setDueDate(today.plusDays(interval));
    }
}
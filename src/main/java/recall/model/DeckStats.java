package recall.model;

/**
 * An immutable snapshot of a deck's review statistics on a given day.
 *
 * <p>Card maturity follows the common Anki convention: a card whose interval is
 * at least {@value #MATURE_INTERVAL_DAYS} days is "mature", a card that has been
 * successfully reviewed at least once but has a shorter interval is "young", and
 * a card never yet reviewed (zero repetitions) is "new".
 *
 * @param total       total number of cards in the deck
 * @param due         cards due for review on the reference day
 * @param newCards    cards never successfully reviewed (0 repetitions)
 * @param young       reviewed cards with an interval below the mature threshold
 * @param mature      cards with an interval at or above the mature threshold
 * @param averageEase mean ease factor across all cards (0.0 for an empty deck)
 */
public record DeckStats(int total, int due, int newCards, int young, int mature, double averageEase) {

    /** Interval (in days) at or above which a card counts as mature. */
    public static final int MATURE_INTERVAL_DAYS = 21;
}
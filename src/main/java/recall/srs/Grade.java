package recall.srs;

/**
 * A learner's self-assessed recall quality for a card, exposed as four buttons
 * in the UI and mapped to SM-2's 0-5 quality scale.
 *
 * <p>Canonical SM-2 takes a raw 0-5 grade; the four-button model is a UI design
 * choice, so the mapping below is deliberate: AGAIN is the only failing grade
 * (quality &lt; {@value #PASSING_THRESHOLD}), while HARD/GOOD/EASY map to the
 * canonical descriptions of a correct response recalled with serious difficulty
 * (3), after a hesitation (4), and perfectly (5).
 */
public enum Grade {
    AGAIN(2),
    HARD(3),
    GOOD(4),
    EASY(5);

    /** Grades with quality at or above this count as a successful recall. */
    public static final int PASSING_THRESHOLD = 3;

    private final int quality;

    Grade(int quality) {
        this.quality = quality;
    }

    public int quality() {
        return quality;
    }

    public boolean isPass() {
        return quality >= PASSING_THRESHOLD;
    }
}
package recall.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A single flashcard: a front (prompt) and back (answer), together with the
 * scheduling state needed by the SM-2 spaced-repetition algorithm.
 *
 * <p>The scheduling fields are:
 * <ul>
 *   <li>{@code easeFactor} — a per-card multiplier controlling how fast the
 *       review interval grows (starts at {@value #DEFAULT_EASE}, never below
 *       {@value #MIN_EASE}).</li>
 *   <li>{@code interval} — days until the next review.</li>
 *   <li>{@code repetitions} — number of consecutive successful recalls.</li>
 *   <li>{@code dueDate} — the date this card should next be reviewed.</li>
 * </ul>
 *
 * <p>The SM-2 algorithm itself (which updates these fields after a review) is
 * implemented separately in the scheduler; this class only holds the state.
 */
public class Flashcard {

    /** Ease factor assigned to a brand-new card. */
    public static final double DEFAULT_EASE = 2.5;

    /** SM-2 never lets the ease factor drop below this floor. */
    public static final double MIN_EASE = 1.3;

    private static final char DELIMITER = '|';
    private static final int FIELD_COUNT = 6;

    private String front;
    private String back;
    private double easeFactor;
    private int interval;
    private int repetitions;
    private LocalDate dueDate;

    /**
     * Creates a brand-new card with fresh SM-2 state, due today so that it
     * enters the very next review session.
     *
     * @param front the prompt shown first
     * @param back  the answer revealed on the back
     */
    public Flashcard(String front, String back) {
        this(front, back, DEFAULT_EASE, 0, 0, LocalDate.now());
    }

    /**
     * Creates a card with explicit scheduling state. Used when loading a card
     * from disk and in tests that need deterministic due dates.
     */
    public Flashcard(String front, String back, double easeFactor, int interval,
                     int repetitions, LocalDate dueDate) {
        this.front = requireNonBlank(front, "front");
        this.back = requireNonBlank(back, "back");
        this.easeFactor = easeFactor;
        this.interval = interval;
        this.repetitions = repetitions;
        this.dueDate = Objects.requireNonNull(dueDate, "dueDate");
    }

    public String getFront() {
        return front;
    }

    public String getBack() {
        return back;
    }

    public double getEaseFactor() {
        return easeFactor;
    }

    public int getInterval() {
        return interval;
    }

    public int getRepetitions() {
        return repetitions;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setFront(String front) {
        this.front = requireNonBlank(front, "front");
    }

    public void setBack(String back) {
        this.back = requireNonBlank(back, "back");
    }

    public void setEaseFactor(double easeFactor) {
        this.easeFactor = easeFactor;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public void setRepetitions(int repetitions) {
        this.repetitions = repetitions;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = Objects.requireNonNull(dueDate, "dueDate");
    }

    /**
     * Returns whether the card is due for review on the given day (i.e. its due
     * date is today or in the past).
     *
     * @param today the reference date
     * @return {@code true} if the card should be reviewed on or before {@code today}
     */
    public boolean isDue(LocalDate today) {
        return !dueDate.isAfter(today);
    }

    /**
     * Serialises this card to a single line for storage. Fields are separated by
     * {@code '|'}; any {@code '|'}, backslash, or newline inside the front/back
     * text is escaped so the line can always be split back into exactly
     * {@value #FIELD_COUNT} fields.
     *
     * @return the save-line representation of this card
     */
    public String toSaveLine() {
        return String.join(String.valueOf(DELIMITER),
                escape(front),
                escape(back),
                Double.toString(easeFactor),
                Integer.toString(interval),
                Integer.toString(repetitions),
                dueDate.toString());
    }

    /**
     * Reconstructs a card from a line produced by {@link #toSaveLine()}.
     *
     * @param line the stored line
     * @return the reconstructed card
     * @throws IllegalArgumentException if the line is malformed
     */
    public static Flashcard fromSaveLine(String line) {
        Objects.requireNonNull(line, "line");
        List<String> fields = splitFields(line);
        if (fields.size() != FIELD_COUNT) {
            throw new IllegalArgumentException(
                    "Expected " + FIELD_COUNT + " fields but found " + fields.size() + ": " + line);
        }
        try {
            String front = unescape(fields.get(0));
            String back = unescape(fields.get(1));
            double ease = Double.parseDouble(fields.get(2));
            int interval = Integer.parseInt(fields.get(3));
            int reps = Integer.parseInt(fields.get(4));
            LocalDate due = LocalDate.parse(fields.get(5));
            return new Flashcard(front, back, ease, interval, reps, due);
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("Malformed card line: " + line, e);
        }
    }

    /** Escapes backslash, the delimiter, and newlines so a field stays on one line. */
    private static String escape(String s) {
        StringBuilder sb = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
            case '\\': sb.append("\\\\"); break;
            case DELIMITER: sb.append('\\').append(DELIMITER); break;
            case '\n': sb.append("\\n"); break;
            case '\r': sb.append("\\r"); break;
            default: sb.append(c);
            }
        }
        return sb.toString();
    }

    /** Reverses {@link #escape(String)}. */
    private static String unescape(String s) {
        StringBuilder sb = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\' && i + 1 < s.length()) {
                char next = s.charAt(++i);
                switch (next) {
                case 'n': sb.append('\n'); break;
                case 'r': sb.append('\r'); break;
                default: sb.append(next); // '\\' -> '\', '\|' -> '|'
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * Splits a save line on unescaped delimiters, keeping escape sequences intact
     * for {@link #unescape(String)} to resolve afterwards.
     */
    private static List<String> splitFields(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '\\' && i + 1 < line.length()) {
                // An escape sequence: keep both chars, unescape resolves them later.
                cur.append(c).append(line.charAt(++i));
            } else if (c == DELIMITER) {
                fields.add(cur.toString());
                cur.setLength(0);
            } else {
                cur.append(c);
            }
        }
        fields.add(cur.toString());
        return fields;
    }

    private static String requireNonBlank(String value, String name) {
        Objects.requireNonNull(value, name);
        if (value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Flashcard)) {
            return false;
        }
        Flashcard other = (Flashcard) o;
        return Double.compare(easeFactor, other.easeFactor) == 0
                && interval == other.interval
                && repetitions == other.repetitions
                && front.equals(other.front)
                && back.equals(other.back)
                && dueDate.equals(other.dueDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(front, back, easeFactor, interval, repetitions, dueDate);
    }

    @Override
    public String toString() {
        return "Flashcard{front='" + front + "', back='" + back + "', ease=" + easeFactor
                + ", interval=" + interval + ", reps=" + repetitions + ", due=" + dueDate + "}";
    }
}

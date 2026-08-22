package recall.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * A named collection of {@link Flashcard}s on a single topic. A deck knows how
 * to report which of its cards are due for review and how to search its cards,
 * but it does not know anything about persistence (see
 * {@link recall.storage.Storage}) or scheduling (see the scheduler).
 */
public class Deck {

    private final String name;
    private final List<Flashcard> cards;

    /**
     * Creates an empty deck.
     *
     * @param name the deck's display name (also used as its file name)
     */
    public Deck(String name) {
        this.name = requireNonBlank(name);
        this.cards = new ArrayList<>();
    }

    /**
     * Creates a deck pre-populated with the given cards. Used when loading a
     * deck from disk.
     */
    public Deck(String name, List<Flashcard> cards) {
        this.name = requireNonBlank(name);
        this.cards = new ArrayList<>(Objects.requireNonNull(cards, "cards"));
    }

    public String getName() {
        return name;
    }

    /** Adds a card to the end of the deck. */
    public void addCard(Flashcard card) {
        cards.add(Objects.requireNonNull(card, "card"));
    }

    /**
     * Removes and returns the card at the given index.
     *
     * @param index zero-based position of the card
     * @return the removed card
     * @throws IndexOutOfBoundsException if the index is out of range
     */
    public Flashcard removeCard(int index) {
        return cards.remove(index);
    }

    /**
     * Returns the card at the given index.
     *
     * @throws IndexOutOfBoundsException if the index is out of range
     */
    public Flashcard getCard(int index) {
        return cards.get(index);
    }

    /** Returns an unmodifiable view of all cards in insertion order. */
    public List<Flashcard> getCards() {
        return Collections.unmodifiableList(cards);
    }

    public int size() {
        return cards.size();
    }

    public boolean isEmpty() {
        return cards.isEmpty();
    }

    /**
     * Returns the cards due for review on the given day, in deck order.
     *
     * @param today the reference date
     * @return a new list of due cards (possibly empty)
     */
    public List<Flashcard> dueCards(LocalDate today) {
        Objects.requireNonNull(today, "today");
        List<Flashcard> due = new ArrayList<>();
        for (Flashcard card : cards) {
            if (card.isDue(today)) {
                due.add(card);
            }
        }
        return due;
    }

    /** Returns how many cards are due on the given day. */
    public int countDue(LocalDate today) {
        return dueCards(today).size();
    }

    /**
     * Returns cards whose front or back contains the given keyword, ignoring case.
     *
     * @param keyword the search term
     * @return a new list of matching cards (possibly empty)
     */
    public List<Flashcard> find(String keyword) {
        Objects.requireNonNull(keyword, "keyword");
        String needle = keyword.toLowerCase(Locale.ROOT);
        List<Flashcard> matches = new ArrayList<>();
        for (Flashcard card : cards) {
            if (card.getFront().toLowerCase(Locale.ROOT).contains(needle)
                    || card.getBack().toLowerCase(Locale.ROOT).contains(needle)) {
                matches.add(card);
            }
        }
        return matches;
    }

    /**
     * Computes a snapshot of this deck's statistics as of the given day.
     *
     * @param today the reference date used to count due cards
     * @return the deck's statistics
     */
    public DeckStats stats(LocalDate today) {
        Objects.requireNonNull(today, "today");
        int due = 0;
        int newCards = 0;
        int young = 0;
        int mature = 0;
        double easeSum = 0.0;
        for (Flashcard card : cards) {
            if (card.isDue(today)) {
                due++;
            }
            if (card.getRepetitions() == 0) {
                newCards++;
            } else if (card.getInterval() >= DeckStats.MATURE_INTERVAL_DAYS) {
                mature++;
            } else {
                young++;
            }
            easeSum += card.getEaseFactor();
        }
        double averageEase = cards.isEmpty() ? 0.0 : easeSum / cards.size();
        return new DeckStats(cards.size(), due, newCards, young, mature, averageEase);
    }

    private static String requireNonBlank(String name) {
        Objects.requireNonNull(name, "name");
        if (name.isBlank()) {
            throw new IllegalArgumentException("Deck name must not be blank");
        }
        return name;
    }

    @Override
    public String toString() {
        return "Deck{name='" + name + "', cards=" + cards.size() + "}";
    }
}

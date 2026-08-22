package recall.storage;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

import recall.model.Deck;

/**
 * The in-memory collection of all decks, backed by {@link Storage}. This is the
 * single object the user interface talks to for anything deck-related: it loads
 * every deck at start-up, keeps them in memory, and writes changes straight
 * through to disk.
 *
 * <p>Deck names are restricted to a file-system-safe character set and are
 * treated case-insensitively for duplicate detection, so two decks can never
 * collide on a case-insensitive file system.
 */
public class DeckRepository {

    /** Letters, digits, spaces, hyphens and underscores — safe as file names. */
    private static final Pattern VALID_NAME = Pattern.compile("[A-Za-z0-9 _-]+");

    private final Storage storage;
    private final List<Deck> decks;

    /**
     * Creates a repository backed by the given storage and eagerly loads all
     * existing decks from disk.
     *
     * @param storage the persistence layer to load from and save to
     */
    public DeckRepository(Storage storage) {
        this.storage = Objects.requireNonNull(storage, "storage");
        this.decks = new ArrayList<>(storage.loadDecks());
    }

    /** Returns an unmodifiable view of all decks currently loaded. */
    public List<Deck> getDecks() {
        return List.copyOf(decks);
    }

    public int deckCount() {
        return decks.size();
    }

    /**
     * Looks up a deck by name, ignoring case.
     *
     * @param name the deck name to find
     * @return the deck, or {@link Optional#empty()} if no such deck exists
     */
    public Optional<Deck> getDeck(String name) {
        Objects.requireNonNull(name, "name");
        for (Deck deck : decks) {
            if (deck.getName().equalsIgnoreCase(name)) {
                return Optional.of(deck);
            }
        }
        return Optional.empty();
    }

    /**
     * Creates a new, empty deck, persists it, and returns it.
     *
     * @param name the desired deck name
     * @return the newly created deck
     * @throws IllegalArgumentException if the name is invalid or already in use
     */
    public Deck createDeck(String name) {
        String trimmed = Objects.requireNonNull(name, "name").trim();
        if (!isValidName(trimmed)) {
            throw new IllegalArgumentException(
                    "Deck name may only contain letters, digits, spaces, '-' and '_': " + name);
        }
        if (getDeck(trimmed).isPresent()) {
            throw new IllegalArgumentException("A deck named '" + trimmed + "' already exists");
        }
        Deck deck = new Deck(trimmed);
        decks.add(deck);
        storage.saveDeck(deck);
        return deck;
    }

    /**
     * Deletes the named deck from memory and disk.
     *
     * @param name the deck to delete (case-insensitive)
     * @return {@code true} if a deck was removed, {@code false} if none matched
     */
    public boolean deleteDeck(String name) {
        Objects.requireNonNull(name, "name");
        Optional<Deck> match = getDeck(name);
        if (match.isEmpty()) {
            return false;
        }
        Deck deck = match.get();
        decks.remove(deck);
        storage.deleteDeck(deck.getName());
        return true;
    }

    /**
     * Persists the current state of a deck (e.g. after adding, editing, or
     * reviewing cards).
     *
     * @param deck the deck to save
     */
    public void save(Deck deck) {
        storage.saveDeck(deck);
    }

    /** Returns whether a name is non-blank and uses only safe characters. */
    public static boolean isValidName(String name) {
        return name != null && !name.isBlank() && VALID_NAME.matcher(name).matches();
    }
}

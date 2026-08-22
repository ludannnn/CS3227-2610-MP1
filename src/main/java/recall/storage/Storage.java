package recall.storage;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import recall.model.Deck;
import recall.model.Flashcard;

/**
 * Reads and writes decks to disk. Each deck is stored as its own plain-text
 * file ({@code <deckName>.txt}) inside a base directory, one card per line in
 * the format defined by {@link Flashcard#toSaveLine()}.
 *
 * <p>The base directory is supplied at construction time. Production code uses
 * {@code data/decks}; tests pass a temporary directory so they never touch real
 * user data.
 */
public class Storage {

    private static final String FILE_EXTENSION = ".txt";

    private final Path baseDir;

    /**
     * Creates a storage rooted at the given directory. The directory is created
     * lazily on the first save if it does not already exist.
     *
     * @param baseDir the directory that holds one file per deck
     */
    public Storage(Path baseDir) {
        this.baseDir = Objects.requireNonNull(baseDir, "baseDir");
    }

    /**
     * Loads every deck found in the base directory.
     *
     * @return a list of decks (empty if the directory does not exist or has no
     *         deck files)
     * @throws UncheckedIOException if a file cannot be read
     */
    public List<Deck> loadDecks() {
        List<Deck> decks = new ArrayList<>();
        if (!Files.isDirectory(baseDir)) {
            return decks;
        }
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(baseDir, "*" + FILE_EXTENSION)) {
            for (Path file : stream) {
                decks.add(loadDeck(file));
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to list decks in " + baseDir, e);
        }
        decks.sort((a, b) -> a.getName().compareToIgnoreCase(b.getName()));
        return decks;
    }

    private Deck loadDeck(Path file) {
        String deckName = fileNameToDeckName(file);
        List<Flashcard> cards = new ArrayList<>();
        try {
            List<String> lines = Files.readAllLines(file);
            for (String line : lines) {
                if (!line.isBlank()) {
                    cards.add(Flashcard.fromSaveLine(line));
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read deck file " + file, e);
        }
        return new Deck(deckName, cards);
    }

    /**
     * Writes a deck to disk, overwriting any existing file for that deck and
     * creating the base directory if necessary.
     *
     * @param deck the deck to persist
     * @throws UncheckedIOException if the file cannot be written
     */
    public void saveDeck(Deck deck) {
        Objects.requireNonNull(deck, "deck");
        try {
            Files.createDirectories(baseDir);
            List<String> lines = new ArrayList<>();
            for (Flashcard card : deck.getCards()) {
                lines.add(card.toSaveLine());
            }
            Files.write(deckFile(deck.getName()), lines);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to save deck " + deck.getName(), e);
        }
    }

    /**
     * Deletes the file backing the named deck, if it exists.
     *
     * @param deckName the deck to remove from disk
     * @throws UncheckedIOException if the file cannot be deleted
     */
    public void deleteDeck(String deckName) {
        Objects.requireNonNull(deckName, "deckName");
        try {
            Files.deleteIfExists(deckFile(deckName));
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to delete deck " + deckName, e);
        }
    }

    private Path deckFile(String deckName) {
        return baseDir.resolve(deckName + FILE_EXTENSION);
    }

    private static String fileNameToDeckName(Path file) {
        String fileName = file.getFileName().toString();
        return fileName.substring(0, fileName.length() - FILE_EXTENSION.length());
    }
}

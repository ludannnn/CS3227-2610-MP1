package recall.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import recall.model.Deck;
import recall.model.Flashcard;

public class StorageTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 8, 20);

    @Test
    public void loadDecks_emptyOrMissingDirectory_returnsEmptyList(@TempDir Path dir) {
        Storage storage = new Storage(dir.resolve("does-not-exist"));
        assertTrue(storage.loadDecks().isEmpty());
    }

    @Test
    public void saveThenLoad_singleDeck_roundTripsCards(@TempDir Path dir) {
        Storage storage = new Storage(dir);
        Deck deck = new Deck("Spanish");
        deck.addCard(new Flashcard("hola", "hello", 2.6, 6, 2, TODAY));
        deck.addCard(new Flashcard("adios", "goodbye"));
        storage.saveDeck(deck);

        List<Deck> loaded = storage.loadDecks();
        assertEquals(1, loaded.size());
        assertEquals("Spanish", loaded.get(0).getName());
        assertEquals(2, loaded.get(0).size());
        assertEquals("hola", loaded.get(0).getCard(0).getFront());
        assertEquals(TODAY, loaded.get(0).getCard(0).getDueDate());
    }

    @Test
    public void saveThenLoad_multipleDecks_allReturned(@TempDir Path dir) {
        Storage storage = new Storage(dir);
        storage.saveDeck(new Deck("French"));
        storage.saveDeck(new Deck("Biology"));
        assertEquals(2, storage.loadDecks().size());
    }

    @Test
    public void deleteDeck_removesFileFromDisk(@TempDir Path dir) {
        Storage storage = new Storage(dir);
        storage.saveDeck(new Deck("Temp"));
        assertEquals(1, storage.loadDecks().size());

        storage.deleteDeck("Temp");
        assertTrue(storage.loadDecks().isEmpty());
    }

    @Test
    public void deleteDeck_nonExistentDeck_doesNotThrow(@TempDir Path dir) {
        Storage storage = new Storage(dir);
        storage.deleteDeck("Nope"); // should be a no-op, no exception
        assertFalse(Path.of(dir.toString(), "Nope.txt").toFile().exists());
    }
}

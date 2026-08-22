package recall.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class DeckRepositoryTest {

    private DeckRepository repo(Path dir) {
        return new DeckRepository(new Storage(dir));
    }

    @Test
    public void createDeck_addsAndPersists(@TempDir Path dir) {
        DeckRepository repo = repo(dir);
        repo.createDeck("Spanish");
        assertEquals(1, repo.deckCount());
        // A fresh repository over the same directory should see the persisted deck.
        assertTrue(repo(dir).getDeck("Spanish").isPresent());
    }

    @Test
    public void getDeck_isCaseInsensitive(@TempDir Path dir) {
        DeckRepository repo = repo(dir);
        repo.createDeck("Spanish");
        assertTrue(repo.getDeck("spanish").isPresent());
        assertTrue(repo.getDeck("SPANISH").isPresent());
    }

    @Test
    public void createDeck_duplicateIgnoringCase_throws(@TempDir Path dir) {
        DeckRepository repo = repo(dir);
        repo.createDeck("Spanish");
        assertThrows(IllegalArgumentException.class, () -> repo.createDeck("SPANISH"));
    }

    @Test
    public void createDeck_invalidName_throws(@TempDir Path dir) {
        DeckRepository repo = repo(dir);
        assertThrows(IllegalArgumentException.class, () -> repo.createDeck("bad/name"));
        assertThrows(IllegalArgumentException.class, () -> repo.createDeck("  "));
    }

    @Test
    public void deleteDeck_removesFromMemoryAndDisk(@TempDir Path dir) {
        DeckRepository repo = repo(dir);
        repo.createDeck("Temp");
        assertTrue(repo.deleteDeck("temp")); // case-insensitive
        assertEquals(0, repo.deckCount());
        assertFalse(repo(dir).getDeck("Temp").isPresent());
    }

    @Test
    public void deleteDeck_missing_returnsFalse(@TempDir Path dir) {
        assertFalse(repo(dir).deleteDeck("Nope"));
    }

    @Test
    public void isValidName_rejectsUnsafeCharacters() {
        assertTrue(DeckRepository.isValidName("CS3227 Notes_1"));
        assertFalse(DeckRepository.isValidName("bad/name"));
        assertFalse(DeckRepository.isValidName(""));
    }
}

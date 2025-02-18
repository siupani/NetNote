package commons;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class NoteTest {

    @Test
    public void testConstructor() {
        Note note = new Note("abcdefg");
        assertNotNull(note.getContent());
        assertEquals("abcdefg", note.getContent());
    }

    @Test
    public void testDefaultConstructor() {
        Note note = new Note();
        assertNull(note.getContent());
        assertNull(note.getTitle());
    }

    @Test
    public void testEqualsNotEquals() {
        Note note1 = new Note("abcdefg");
        Note note2 = new Note("abcdefg");
        Note note3 = new Note("abcde");
        assertEquals(note1, note2);
        assertNotEquals(note1, note3);
    }

    @Test
    public void testHashCode() {
        Note note1 = new Note();
        note1.setId(1L);
        note1.setTitle("Title");
        note1.setContent("Content");

        Note note2 = new Note();
        note2.setId(1L);
        note2.setTitle("Title");
        note2.setContent("Content");

        assertEquals(note1.hashCode(), note2.hashCode(), "Hash codes should be equal for notes with the same properties");

        note2.setContent("Different Content");
        assertNotEquals(note1.hashCode(), note2.hashCode(), "Hash codes should be different for notes with different properties");
    }

    @Test
    public void testSetters() {
        Note note = new Note();
        note.setTitle("Updated Title");
        note.setContent("Updated Content");
        note.setId(1L);

        assertEquals("Updated Title", note.getTitle());
        assertEquals("Updated Content", note.getContent());
        assertEquals(1L, note.getId());
    }

    @Test
    public void testCollectionAssociation() {
        Note note = new Note();
        Collection collection = new Collection();
        note.setCollection(collection);

        assertEquals(collection, note.getCollection());
    }

}

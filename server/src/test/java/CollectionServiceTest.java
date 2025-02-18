import commons.Note;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import commons.Collection;
import server.database.CollectionRepository;
import server.service.CollectionService;

import java.util.Optional;

public class CollectionServiceTest {

    @Mock
    private CollectionRepository collectionRepository;

    @InjectMocks
    private CollectionService collectionService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this); // Initialize mocks
    }

    //test disabled, as the method does not exist anymore
    @Test
    @Disabled
    public void testNoDuplicateDefaultCollection() {
        Collection existingCollection = new Collection("Default Collection");
        when(collectionRepository.findByName("Default Collection")).thenReturn(Optional.of(existingCollection));
        Collection defaultCollection = null;
        //Collection defaultCollection = collectionService.getOrCreateDefaultCollection();
        assertNotNull(defaultCollection, "Returned collection should not be null");
        assertEquals(existingCollection, defaultCollection, "Should return the existing Default Collection");
        verify(collectionRepository, never()).save(any(Collection.class));
    }

    @Test
    void deleteCollectionByID_Exists() {
        // Test whether it deletes the collection when it exists
        long collectionId = 1L;
        
        when(collectionRepository.existsById(collectionId)).thenReturn(true);
        collectionService.deleteCollectionByID(collectionId);
        verify(collectionRepository, times(1)).deleteById(collectionId);
    }

    @Test
    void deleteCollectionByID_DoesNotExist() {
        // Test whether it throws an exception when the collection does not exist
        long collectionId = 1L;

        when(collectionRepository.existsById(collectionId)).thenReturn(false);
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
            collectionService.deleteCollectionByID(collectionId));

        assertEquals("Collection with ID" + collectionId + " does not exist", exception.getMessage());
    }

    @Test
    void addNoteToCollection_CollectionExists() {
        // Should add the note when collection exists
        long collectionId = 1L;
        Note note = new Note("some note");
        Collection collection = new Collection("some collection");

        when(collectionRepository.findById(collectionId)).thenReturn(Optional.of(collection));
        when(collectionRepository.save(collection)).thenReturn(collection);

        Collection updatedCollection = collectionService.addNoteToCollection(collectionId, note);

        verify(collectionRepository, times(1)).save(collection);
        assertTrue(updatedCollection.getNotes().contains(note));
    }

    @Test
    void addNoteToCollection_CollectionDoesNotExist() {
        // Should throw exception when collection does not exist
        long collectionId = 1L;
        Note note = new Note("some note");
        when(collectionRepository.findById(collectionId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () ->
            collectionService.addNoteToCollection(collectionId, note));

        assertEquals("Collection not found", exception.getMessage());
    }

    @Test
    void addNoteToCollection_NoteDoesNotExist() {
        // Should throw an exception when the note request is null
        long collectionId = 1L;

        Collection collection = new Collection("some collection");
        when(collectionRepository.findById(collectionId)).thenReturn(Optional.of(collection));

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
            collectionService.addNoteToCollection(collectionId, null));

        assertEquals("Note cannot be null", exception.getMessage());
        // Ensure that save was never called since the note was null
        verify(collectionRepository, never()).save(any(Collection.class));
    }

    //test disabled, as the method does not exist anymore
    @Test
    @Disabled
    void getOrCreateDefaultCollection_Get() {
        // Test whether the collection is returned
        when(collectionRepository.findByName("Default Collection")).thenReturn(Optional.empty());

        Collection collection = new Collection("Default Collection");
        when(collectionRepository.save(any(Collection.class))).thenReturn(collection);
        Collection result = null;
        //Collection result = collectionService.getOrCreateDefaultCollection();

        verify(collectionRepository, times(1)).findByName("Default Collection");
        verify(collectionRepository, times(1)).save(any(Collection.class));

        assertNotNull(result);
        assertEquals("Default Collection", result.getName());
    }

    @Test
    void updateCollection_Exists() {
        // Test when collection exists
        Long collectionId = 1L;
        Collection existingCollection = new Collection("Old Name");
        Collection updatedCollection = new Collection("Updated Name");

        when(collectionRepository.findById(collectionId)).thenReturn(Optional.of(existingCollection));
        when(collectionRepository.save(existingCollection)).thenReturn(existingCollection);

        Collection result = collectionService.updateCollection(collectionId, updatedCollection);

        assertNotNull(result);
        assertEquals("Updated Name", result.getName());
        verify(collectionRepository, times(1)).save(existingCollection);
    }

    @Test
    void updateCollection_DoesNotExist() {
        // Test when collection does not exist
        Long collectionId = 1L;
        Collection updatedCollection = new Collection("Updated Name");

        when(collectionRepository.findById(collectionId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                collectionService.updateCollection(collectionId, updatedCollection));

        assertEquals("Collection with ID " + collectionId + " does not exist", exception.getMessage());
    }

    @Test
    void getCollectionById_Exists() {
        // Test when collection exists
        Long collectionId = 1L;
        Collection collection = new Collection("Some Collection");

        when(collectionRepository.findById(collectionId)).thenReturn(Optional.of(collection));

        Collection result = collectionService.getCollectionById(collectionId);

        assertNotNull(result);
        assertEquals("Some Collection", result.getName());
    }

    @Test
    void getCollectionById_DoesNotExist() {
        // Test when collection does not exist
        Long collectionId = 1L;

        when(collectionRepository.findById(collectionId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                collectionService.getCollectionById(collectionId));

        assertEquals("Collection not found with ID: " + collectionId, exception.getMessage());
    }

    @Test
    void createDefaultCollection() {
        // Test default collection creation
        Collection defaultCollection = new Collection("Initial Collection");

        when(collectionRepository.save(any(Collection.class))).thenReturn(defaultCollection);

        Collection result = collectionService.createDefaultCollection();

        assertNotNull(result);
        assertEquals("Initial Collection", result.getName());
        verify(collectionRepository, times(1)).save(any(Collection.class));
    }
}
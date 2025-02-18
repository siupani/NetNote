package server.service;

import commons.Collection;
import commons.Note;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import server.database.CollectionRepository;
import server.database.NoteRepository;

@Service
public class CollectionService {

    private CollectionRepository collectionRepository;
    private NoteRepository noteRepository;

    @Autowired
    public CollectionService(CollectionRepository collectionRepository) {
        this.collectionRepository = collectionRepository;
    }

    public void deleteCollectionByID(Long id) {
        if (!collectionRepository.existsById(id)) {
            throw new IllegalArgumentException("Collection with ID" + id + " does not exist");
        }
        collectionRepository.deleteById(id);
    }

    public Collection addNoteToCollection(Long collectionId, Note noteRequest) {
        // Check whether the note exists
        if (noteRequest == null) {
            throw new IllegalArgumentException("Note cannot be null");
        }

        // Check whether the provided collection exists
        Collection collection = collectionRepository.findById(collectionId)
                .orElseThrow(() -> new RuntimeException("Collection not found"));

        collection.addNote(noteRequest);

        return collectionRepository.save(collection);
    }

    public Collection updateCollection(Long id, Collection updatedCollection) {
        Collection existingCollection = collectionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Collection with ID " + id + " does not exist"));
        existingCollection.setName(updatedCollection.getName());
        return collectionRepository.save(existingCollection);
    }

    public Collection getCollectionById(Long id) {
        return collectionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Collection not found with ID: " + id));
    }

    /**
     * method that creates the default collection when
     * no defaultCollectionId has been found in the config file
     * @return the newly created Default Collection
     */
    public Collection createDefaultCollection() {
        Collection defaultCollection = new Collection("Initial Collection");
        return collectionRepository.save(defaultCollection);
    }

}



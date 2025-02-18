package server.api;

import commons.Collection;
import commons.Note;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import server.database.CollectionRepository;
import server.service.CollectionService;

import java.util.List;

@RestController
@RequestMapping("/api/collections")
public class CollectionController {
    private final CollectionRepository repo;
    private final CollectionService collectionService;

    public CollectionController(CollectionRepository repo, CollectionService collectionService) {
        this.repo = repo;
        this.collectionService = collectionService;
    }

    @GetMapping(path = { "", "/" })
    public List<Collection> getAll() {
        return repo.findAll();
    }

    @GetMapping("/{collectionName}")
    public ResponseEntity<Collection> get(@PathVariable String collectionName) {
        if(repo.findByName(collectionName).isPresent()) {
            return ResponseEntity.ok(repo.findByName(collectionName).get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/{collectionId}/notes")
    public ResponseEntity<List<Note>> getNotes(@PathVariable Long collectionId) {
        if (repo.findById(collectionId).isPresent()) {
            return ResponseEntity.ok(repo.findById(collectionId).get().getNotes());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PostMapping(path = { "", "/" })
    public ResponseEntity<Collection> addCollection(@RequestBody Collection collection) {
        Collection saved = repo.save(collection);
        return ResponseEntity.ok(saved);
    }

    @PostMapping("/{collectionId}")
    public ResponseEntity<Collection> addNote(@PathVariable Long collectionId, @RequestBody Note noteRequest) {
        Collection updatedCollection = collectionService.addNoteToCollection(collectionId, noteRequest);
        return ResponseEntity.ok(updatedCollection);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCollection(@PathVariable Long id) {
        collectionService.deleteCollectionByID(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Collection> updateCollection(@PathVariable Long id, @RequestBody Collection collection) {
        try {
            Collection updatedCollection = collectionService.updateCollection(id, collection);
            return ResponseEntity.ok(updatedCollection);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @GetMapping("/by-id/{id}")
    public ResponseEntity<Collection> getCollectionById(@PathVariable Long id) {
        try {
            Collection collection = collectionService.getCollectionById(id);
            return ResponseEntity.ok(collection);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PostMapping("/default")
    public ResponseEntity<Collection> createDefaultCollection() {
        try {
            Collection defaultCollection = collectionService.createDefaultCollection();
            return ResponseEntity.ok(defaultCollection);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null); // Conflict if default collection already exists
        }
    }


}

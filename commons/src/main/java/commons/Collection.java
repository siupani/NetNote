package commons;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
public class Collection {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "generator")
    @SequenceGenerator(name = "generator", sequenceName = "my_sequence1", allocationSize = 1)
    private long id;
    private String name;

    //one-to-many relation between collection and notes
    @JsonIgnore
    @OneToMany(mappedBy = "collection", cascade = CascadeType.ALL, orphanRemoval = false)
    private List<Note> notes;

    public Collection() {
        this.notes = new ArrayList<>();
    }

    public Collection(String name) {
        this.name = name;
        this.notes = new ArrayList<>();
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void addNote(Note note) {
        notes.add(note);
        note.setCollection(this);
    }

    @Override
    public String toString() {
        return this.name;
    }

    //we could move the note back to the default collection instead of assigning a null value to the collection field
    public void removeNote(Note note) {
        notes.remove(note);
        note.setCollection(null);
    }

    public Note getNote(int index) {
        return notes.get(index);
    }

    public List<Note> getNotes() {
        return notes;
    }

    public int countNote() {
        return notes.size();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Collection that = (Collection) o;
        return id == that.id && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }

}
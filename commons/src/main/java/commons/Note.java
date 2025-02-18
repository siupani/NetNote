package commons;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
public class Note {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "generator")
    @SequenceGenerator(name = "generator", sequenceName = "my_sequence2", allocationSize = 1)
    private long id;
    private String title;
    @Column(columnDefinition = "TEXT")
    private String content;

    @ManyToOne
    @JoinColumn(name = "collection_id", nullable = true)
    private Collection  collection;

    @OneToMany(mappedBy = "note", cascade = CascadeType.PERSIST, orphanRemoval = true)
    // DO NOT CHANGE THE CASCADE TYPE
    private List<FileEntity> files;

    public Note() {
        this.files = new ArrayList<>();
    }

    public Note(String content) {
        this.content = content;
        this.title = "New Note";
        this.files = new ArrayList<>();
    }

    public String getContent() {
        return content;
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Collection getCollection() {
        return collection;
    }

    public void setCollection(Collection collection) {
        this.collection = collection;
    }

    public List<FileEntity> getFiles() {
        return files;
    }

    public void addFile(FileEntity file) {
        files.add(file);
    }

    public void removeFile(FileEntity file) {
        files.remove(file);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Note note = (Note) o;
        return id == note.id && Objects.equals(title, note.title) && Objects.equals(content, note.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, content);
    }

    public void setFile(Long fileId, FileEntity file) {
        int id = fileId.intValue();
        this.files.set(id, file);
    }
}
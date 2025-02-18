package commons;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

import java.util.Arrays;
import java.util.Objects;

@Entity
public class FileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "file_generator")
    @SequenceGenerator(name = "file_generator", sequenceName = "file_sequence", allocationSize = 1)
    private long id;

    private String name;

    @Lob
    @Column(columnDefinition = "BLOB")
    private byte[] data;

    @ManyToOne
    @JsonBackReference
    @JoinColumn(name = "note_id", nullable = true)
    private Note note;

    public FileEntity() {}

    public FileEntity(String name, byte[] data, Note note) {
        this.name = name;
        this.data = data;
        this.note = note;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public byte[] getData() {
        return data;
    }

    public Note getNote() {
        return note;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setNote(Note note) {
        this.note = note;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileEntity that = (FileEntity) o;
        return id == that.id && Objects.equals(name, that.name) && Objects.deepEquals(data, that.data) && Objects.equals(note, that.note);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, Arrays.hashCode(data), note);
    }
}

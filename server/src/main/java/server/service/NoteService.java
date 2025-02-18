package server.service;

import commons.FileEntity;
import commons.Note;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import server.database.FileRepository;
import server.database.NoteRepository;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class NoteService {

    // When updating this constant, also take into account the "spring.servlet.multipart.max-file-size" setting
    //  in the application.properties. Otherwise, the Spring server itself will still reject large payloads
    //  with a 413 Content Too Large status code.
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5 MB

    private NoteRepository noteRepository;
    private FileRepository fileRepository;

    @Autowired
    public NoteService(NoteRepository noteRepository, FileRepository fileRepository) {
        this.noteRepository = noteRepository;
        this.fileRepository = fileRepository;
    }

    public void deleteNoteById(Long id) {
        if (!noteRepository.existsById(id)) {
            throw new IllegalArgumentException("Note with ID " + id + " does not exist");
        }
        // Retrieve associated files
        List<FileEntity> associatedFiles = fileRepository.findByNoteId(id);

        // Delete associated files
        for (FileEntity file : associatedFiles) {
            try {
                // Delete file from storage
                String filePath = "path/to/your/storage/" + file.getNote().getId() + "/" + file.getName();
                File fileToDelete = new File(filePath);
                if (fileToDelete.exists()) {
                    if (!fileToDelete.delete()) {
                        System.err.println("Failed to delete file: " + filePath);
                    }
                }
                fileRepository.delete(file);
            } catch (Exception e) {
                System.err.println("Error deleting file " + file.getName() + ": " + e.getMessage());
            }
        }
        System.out.println("Note with ID " + id + " and its associated files have been deleted.");
        noteRepository.deleteById(id);
    }

    // method that updates the note both locally and in the repository
    public Note updateNote(Long id, Note updatedNote) throws RuntimeException {
        Optional<Note> retrievedNote = noteRepository.findById(id);
        if (retrievedNote.isEmpty()) {
            throw new IllegalArgumentException("Note with ID " + id + " does not exist.");
        }
        if (updatedNote == null)
            throw new IllegalArgumentException("There is no update to note (updateNote is null).");

        Note noteToSave = retrievedNote.get();
        noteToSave.setTitle(updatedNote.getTitle());
        return noteRepository.save(updatedNote);
    }

    /**
     * method that triggers the search of the keyword in the note repository
     *
     * @param keyword
     * @param collectionId the id of the currently selected collection
     * @return the list of notes containing the keyword
     */
    public List<Note> searchKeyword(String keyword, Long collectionId) {
        if (collectionId == null) {
            return noteRepository.findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(keyword, keyword);
        }
        return noteRepository.findByCollectionIdAndTitleContainingIgnoreCaseOrContentContainingIgnoreCase(
                collectionId, keyword, keyword
        );
    }

    /**
     * method that saves a file to the file repository
     *
     * @param file the file to save
     * @param note the corresponding note
     * @return the saved file
     */
    public FileEntity saveFile(MultipartFile file, Note note) {
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds the maximum allowed limit of "
                    + (MAX_FILE_SIZE / (1024 * 1024)) + " MB.");
        }

        try {
            byte[] fileData = file.getBytes(); // Convert file content to a byte array
            FileEntity fileEntity = new FileEntity(file.getOriginalFilename(), fileData, note);
            return fileRepository.save(fileEntity); // Save FileEntity to the database
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file content: " + file.getOriginalFilename(), e);
        }
    }

    /**
     * method to delete a file from repository by id
     *
     * @param fileId
     * @return a boolean indicating whether the delete operation was successful or not
     */
    public boolean deleteFileById(Long fileId) {
        Optional<FileEntity> optionalFile = fileRepository.findById(fileId);
        if (optionalFile.isEmpty()) {
            return false;
        }

        fileRepository.delete(optionalFile.get());
        return true;
    }

    public FileEntity updateFileTitle(Long id, FileEntity updatedFile) {
        Optional<FileEntity> optionalRetrievedFile = fileRepository.findById(id);
        if (optionalRetrievedFile.isEmpty()) {
            throw new IllegalArgumentException("Note with ID " + id + " does not exist.");
        }
        FileEntity retrievedFile = optionalRetrievedFile.get();
        retrievedFile.setName(updatedFile.getName());
        return fileRepository.save(retrievedFile);
    }
}
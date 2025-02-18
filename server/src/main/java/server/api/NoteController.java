/*
 * Copyright 2021 Delft University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package server.api;

import java.util.List;
import java.util.Optional;

import commons.FileEntity;
import commons.Note;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;
import server.database.FileRepository;
import server.database.NoteRepository;
import server.service.NoteService;

@RestController
@RequestMapping("/api/notes")
public class NoteController {

    private final NoteRepository noteRepo;
    private final FileRepository fileRepo;
    private final NoteService noteService;

    public NoteController(NoteRepository repo, NoteService noteService, FileRepository fileRepo) {
        this.noteRepo = repo;
        this.noteService = noteService;
        this.fileRepo = fileRepo;
    }

    @GetMapping(path = { "", "/" })
    public List<Note> getAll() {
        return noteRepo.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Note> getById(@PathVariable("id") long id) {
        if (id < 0 || !noteRepo.existsById(id)) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(noteRepo.findById(id).get());
    }

    @PostMapping(path = { "", "/" })
    public ResponseEntity<Note> add(@RequestBody Note note) {
        //System.out.println(note.getFiles().stream().map(FileEntity::getName).toList());
        Note saved = noteRepo.save(note);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNote(@PathVariable Long id) {
        noteService.deleteNoteById(id);
        return ResponseEntity.noContent().build();
    }

    //method that calls the updateNote in NoteService to update the selected note
    @PutMapping("/{id}")
    public ResponseEntity<Note> updateNote(@PathVariable Long id, @RequestBody Note updatedNote) {
        try{
            Note savedNote = noteService.updateNote(id, updatedNote);
            return ResponseEntity.ok(savedNote);
        }catch(RuntimeException e){
            return ResponseEntity.notFound().build();
        }
    }

    /**
     *     method that calls the searchKeyword method in noteService to perform the keyword search
     */
    @GetMapping("/search")
    public ResponseEntity<List<Note>> searchNotes(
            @RequestParam String keyword,
            @RequestParam(required = false) Long collectionId) {
        List<Note> filteredNotes = noteService.searchKeyword(keyword, collectionId);
        return ResponseEntity.ok(filteredNotes);
    }

    /**
     * endpoint to save a file in the file repository, linking it to a note
     * @param id the note ID
     * @param file the file to save
     * @return the saved file, if the operation was successful
     */
    @PostMapping("/{id}/files")
    public ResponseEntity<FileEntity> uploadFile(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        Optional<Note> optionalNote = noteRepo.findById(id);
        if (optionalNote.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Note note = optionalNote.get();
        FileEntity fileEntity = noteService.saveFile(file, note);
        //System.out.println(fileEntity.getNote() + fileEntity.getName() + fileEntity.getId());
        return ResponseEntity.status(201).body(fileEntity);
    }

    /**
     * endpoint for retrieving all the files linked to a specific note
     * @param id the note id
     * @return the list of files, if the operation was successful
     */
    @GetMapping("/{id}/files")
    public ResponseEntity<List<FileEntity>> getFiles(@PathVariable Long id) {
        Optional<Note> optionalNote = noteRepo.findById(id);
        if (optionalNote.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<FileEntity> files = fileRepo.findByNoteId(id);
        return ResponseEntity.ok(files);
    }

    /**
     * endpoint to delete a filed by id
     * @param fileId
     * @return a response indicating whether the delete operation was successful or not
     */
    @DeleteMapping("/files/{fileId}")
    public ResponseEntity<Void> deleteFile(@PathVariable Long fileId) {
        boolean deleted = noteService.deleteFileById(fileId);
        if (!deleted) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }

    /**
     * endpoint for retrieving a file from the repository
     * @param noteId the id of the note in which the file is
     * @param fileId
     * @return the file as an FileEntity object, if present
     */
    @GetMapping("/{noteId}/files/{fileId}")
    public ResponseEntity<byte[]> getFile(
            @PathVariable Long noteId,
            @PathVariable Long fileId) {

        Optional<FileEntity> optionalFile = fileRepo.findById(fileId);
        if (optionalFile.isEmpty() || optionalFile.get().getNote().getId() != noteId) {
            return ResponseEntity.notFound().build();
        }

        FileEntity fileEntity = optionalFile.get();
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"" + fileEntity.getName() + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(fileEntity.getData());
    }

    @PutMapping("/files/{id}")
    public ResponseEntity<FileEntity> updateFileTitle(@PathVariable Long id, @RequestBody FileEntity updatedFile) {
        try{
            FileEntity savedFile = noteService.updateFileTitle(id, updatedFile);
            return ResponseEntity.ok(savedFile);
        }catch(RuntimeException e){
            return ResponseEntity.notFound().build();
        }
    }
}
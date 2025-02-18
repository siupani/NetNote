import commons.FileEntity;
import commons.Note;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import server.database.FileRepository;
import server.database.NoteRepository;
import server.service.NoteService;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;


class NoteServiceTest {

	@Mock
	private NoteRepository noteRepository;

	@Mock
	private FileRepository fileRepository;

	@InjectMocks
	private NoteService noteService;

	@BeforeEach
	public void setUp() {
		MockitoAnnotations.openMocks(this); // Initialize mocks
	}

	@Test
	void deleteNoteById_Exists() {
		long noteId = 1L;

		when(noteRepository.existsById(noteId)).thenReturn(true);

		List<FileEntity> associatedFiles = new ArrayList<>(); // Empty list for simplicity
		when(fileRepository.findByNoteId(noteId)).thenReturn(associatedFiles);

		noteService.deleteNoteById(noteId);

		// Verify the interactions
		verify(noteRepository, times(1)).existsById(noteId);
		verify(noteRepository, times(1)).deleteById(noteId);
		verify(fileRepository, times(1)).findByNoteId(noteId);

		verify(fileRepository, never()).delete(any(FileEntity.class));
	}

	@Test
	void deleteNoteById_DoesNotExist() {
		// Test to throw exception when trying to delete note by its id when it does not exist
		long noteId = 1L;

		when(noteRepository.existsById(noteId)).thenReturn(false);
		Exception exception = assertThrows(IllegalArgumentException.class, () ->
			noteService.deleteNoteById(noteId));

		assertEquals("Note with ID " + noteId + " does not exist", exception.getMessage());
	}

	@Test
	void updateNote_Exists() {
		// Test to check whether updateNote works with an existing note
		long noteId = 1L;
		Note existingNote = new Note();
		existingNote.setTitle("Old Title");

		Note updatedNote = new Note();
		updatedNote.setTitle("New Title");

		when(noteRepository.findById(noteId)).thenReturn(Optional.of(existingNote));
		when(noteRepository.save(updatedNote)).thenReturn(updatedNote);

		Note result = noteService.updateNote(noteId, updatedNote);

		verify(noteRepository, times(1)).findById(noteId);
		verify(noteRepository, times(1)).save(updatedNote);

		assertEquals("New Title", result.getTitle());
	}

	@Test
	void updateNote_DoesNotExist() {
		// Test to check whether updateNote throws an exception when the note does not exist
		long noteId = 1L;
		Note updatedNote = new Note();

		when(noteRepository.findById(noteId)).thenReturn(Optional.empty());

		Exception exception = assertThrows(RuntimeException.class, () ->
			noteService.updateNote(noteId, updatedNote));

		verify(noteRepository, times(1)).findById(noteId);
		verify(noteRepository, never()).save(any(Note.class)); // Ensure the note was never saved

		assertEquals("Note with ID " + noteId + " does not exist.", exception.getMessage());
	}

	@Test
	void updateNote_NullUpdated() {
		// Test to check whether updateNote throws an exception when the note is updated with null
		long noteId = 1L;
		Note exisitngNote = new Note();

		when(noteRepository.findById(noteId)).thenReturn(Optional.of(exisitngNote));

		Exception exception = assertThrows(IllegalArgumentException.class, () ->
			noteService.updateNote(noteId, null)
		);

		verify(noteRepository, times(1)).findById(noteId);
		verify(noteRepository, never()).save(any(Note.class)); // Ensure the note was never saved

		assertEquals("There is no update to note (updateNote is null).", exception.getMessage());
	}

	@Test
	void deleteNoteById_Exists_WithFiles() {
		//Test to verify that the method deletes a note and its associated files.
		long noteId = 1L;

		when(noteRepository.existsById(noteId)).thenReturn(true);

		Note note = new Note();
		note.setId(noteId);

		FileEntity file1 = new FileEntity("file1.txt", "data1".getBytes(), note);
		FileEntity file2 = new FileEntity("file2.txt", "data2".getBytes(), note);

		List<FileEntity> associatedFiles = Arrays.asList(file1, file2);
		when(fileRepository.findByNoteId(noteId)).thenReturn(associatedFiles);

		File mockFile1 = mock(File.class);
		File mockFile2 = mock(File.class);

		when(mockFile1.exists()).thenReturn(true);
		when(mockFile1.delete()).thenReturn(true);
		when(mockFile2.exists()).thenReturn(true);
		when(mockFile2.delete()).thenReturn(true);

		noteService.deleteNoteById(noteId);

		verify(noteRepository, times(1)).existsById(noteId);
		verify(noteRepository, times(1)).deleteById(noteId);
		verify(fileRepository, times(1)).findByNoteId(noteId);
		verify(fileRepository, times(1)).delete(file1);
		verify(fileRepository, times(1)).delete(file2);
	}

	@Test
	void deleteNoteById_Exists_NoFiles() {
		//Test to ensure that the method works when there are no files to delete
		long noteId = 1L;

		when(noteRepository.existsById(noteId)).thenReturn(true);

		when(fileRepository.findByNoteId(noteId)).thenReturn(new ArrayList<>());

		noteService.deleteNoteById(noteId);

		verify(noteRepository, times(1)).existsById(noteId);
		verify(noteRepository, times(1)).deleteById(noteId);
		verify(fileRepository, times(1)).findByNoteId(noteId);

		verify(fileRepository, never()).delete(any(FileEntity.class));
	}

	@Test
	void deleteNoteById_FileDeletionFails() {
		// Test to ensure the method handles file deletion failures gracefully
		long noteId = 1L;

		when(noteRepository.existsById(noteId)).thenReturn(true);

		Note note = new Note();
		note.setId(noteId);

		FileEntity file1 = new FileEntity("file1.txt", "data1".getBytes(), note);

		List<FileEntity> associatedFiles = List.of(file1);
		when(fileRepository.findByNoteId(noteId)).thenReturn(associatedFiles);

		File mockFile = mock(File.class);
		when(mockFile.exists()).thenReturn(true);
		when(mockFile.delete()).thenReturn(false); // Deletion fails

		noteService.deleteNoteById(noteId);

		verify(noteRepository, times(1)).deleteById(noteId);
		verify(fileRepository, times(1)).findByNoteId(noteId);

		verify(fileRepository, times(1)).delete(file1);
	}
}
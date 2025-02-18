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
package client.utils;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import commons.Collection;
import commons.FileEntity;
import commons.Note;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.glassfish.jersey.client.ClientConfig;

import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.GenericType;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;

public class ServerUtils {

    private static final String SERVER = "http://localhost:8080/";

    public String getServerPath() {
        return SERVER;
    }

    public List<Note> getNotes() {
        return ClientBuilder.newClient(new ClientConfig())
                .target(SERVER).path("api/notes")
                .request(APPLICATION_JSON)
                .get(new GenericType<>() {
                });
    }

    public Note newEmptyNote() {
        Note emptyNote = new Note("");
        Note savedNote = this.addNote(emptyNote);
        savedNote.setTitle("New Note " + savedNote.getId());
        return this.updateNote(savedNote);
    }

    public Collection newEmptyCollection() {
        Collection emptyCollection = new Collection("New Collection");
        return this.addCollection(emptyCollection);
    }

    public Note addNote(Note note) {
        return ClientBuilder.newClient(new ClientConfig())
                .target(SERVER).path("api/notes")
                .request(APPLICATION_JSON)
                .post(Entity.entity(note, APPLICATION_JSON), Note.class);
    }

    public boolean isServerAvailable() {
        try {
            ClientBuilder.newClient(new ClientConfig())
                    .target(SERVER)
                    .request(APPLICATION_JSON)
                    .get();
        } catch (ProcessingException e) {
            if (e.getCause() instanceof ConnectException) {
                return false;
            }
        }
        return true;
    }

    public void deleteNoteFromServer(long id) throws IOException {
        URL url = new URL(SERVER + "api/notes/" + id);
        HttpURLConnection connection = null;

        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("DELETE");

            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_NO_CONTENT) {
                throw new RuntimeException("Failed to delete note. " + responseCode);
            }
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    // method that triggers the update of the note in the server
    public Note updateNote(Note selectedNote) {
        String url = SERVER + "api/notes/" + selectedNote.getId();
        return ClientBuilder.newClient(new ClientConfig())
                .target(url)
                .request(APPLICATION_JSON)
                .put(Entity.entity(selectedNote, APPLICATION_JSON), Note.class);
    }

    /**
     * Fetches all collections from the backend
     *
     * @return - all collections
     */
    public List<Collection> getCollections() {
        return ClientBuilder.newClient(new ClientConfig())
                .target(SERVER).path("api/collections")
                .request(APPLICATION_JSON)
                .get(new GenericType<>() {
                });
    }

    /**
     * Adds a collection to the backend
     *
     * @param collection - collection to add
     * @return - collection to add
     */
    public Collection addCollection(Collection collection) {
        return ClientBuilder.newClient(new ClientConfig())
                .target(SERVER).path("api/collections")
                .request(APPLICATION_JSON)
                .post(Entity.entity(collection, APPLICATION_JSON), Collection.class);
    }

    /**
     * updates the collection name on the server
     *
     * @param collection - collection to update
     * @return - updated collection
     */
    public Collection updateCollection(Collection collection) {
        return ClientBuilder.newClient(new ClientConfig())
                .target(SERVER).path("api/collections/{id}")
                .resolveTemplate("id", collection.getId()) // Use the collection's ID
                .request(APPLICATION_JSON)
                .put(Entity.entity(collection, APPLICATION_JSON), Collection.class);
    }

    public int makeRequest(String serverPath, Collection collection) {
        try {
            Response response = ClientBuilder.newClient(new ClientConfig())
                    .target(serverPath)
                    .path("api/collections")
                    .request(APPLICATION_JSON)
                    .post(Entity.entity(collection, APPLICATION_JSON));
            return response.getStatus();
        } catch (Exception e) {
            return 1000;
        }
    }

    /**
     * Deletes a collection from the backend
     *
     * @param id - id of the collection to delete
     * @throws IOException - if collection fails to be deleted
     */
    public void deleteCollection(long id) throws IOException {
        URL url = new URL(SERVER + "api/collections/" + id);
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("DELETE");

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_FORBIDDEN)
                throw new RuntimeException("Default Collection cannot be deleted. " + responseCode);
            if (responseCode != HttpURLConnection.HTTP_NO_CONTENT) {
                throw new RuntimeException("Failed to delete collection. " + responseCode);
            }
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * Associates a note to a specific collection
     *
     * @param collectionId - id of the collection
     * @param note         - note to be linked
     * @return - the updated Collection
     */
    public Collection linkNoteToCollection(long collectionId, Note note) {
        return ClientBuilder.newClient(new ClientConfig())
                .target(SERVER).path("api/collections/" + collectionId)
                .request(APPLICATION_JSON)
                .post(Entity.entity(note, APPLICATION_JSON), Collection.class);
    }

    /**
     * Get a list of all notes in a collection
     *
     * @param collectionId - id of the collection
     * @return - list of all notes
     */
    public List<Note> getNotesByCollection(long collectionId) {
        return ClientBuilder.newClient(new ClientConfig())
                .target(SERVER).path("api/collections/" + collectionId + "/notes")
                .request(APPLICATION_JSON)
                .get(new GenericType<>() {
                });
    }

    public Collection getCollectionByName(String name) {
        return ClientBuilder.newClient(new ClientConfig())
                .target(SERVER).path("api/collections/" + name)
                .request(APPLICATION_JSON)
                .get(Collection.class);
    }

    /**
     * Searches for notes based on a keyword and optionally a collection ID.
     *
     * @param keyword      - the keyword to search for.
     * @param collectionId - (optional) the ID of the collection to filter notes within.
     * @return - list of filtered notes matching the keyword and collection (if provided).
     */
    public List<Note> searchKeyword(String keyword, Long collectionId) {
        try {
            String encodedKeyword = URLEncoder.encode(keyword, StandardCharsets.UTF_8);
            String baseUrl = SERVER + "api/notes/search?keyword=" + encodedKeyword;
            if (collectionId != null) {
                baseUrl += "&collectionId=" + collectionId;
            }
            String url = baseUrl;
            return ClientBuilder.newClient(new ClientConfig())
                    .target(url)
                    .request(APPLICATION_JSON)
                    .get(new GenericType<>() {
                    });
        } catch (Exception e) {
            throw new RuntimeException("Error encoding or executing search request", e);
        }
    }

    public Collection getCollectionById(long id) {
        return ClientBuilder.newClient(new ClientConfig())
                .target(SERVER)
                .path("api/collections/by-id/" + id)
                .request(APPLICATION_JSON)
                .get(Collection.class);
    }

    public Collection createDefaultCollection() {
        return ClientBuilder.newClient(new ClientConfig())
                .target(SERVER).path("api/collections/default")
                .request(APPLICATION_JSON)
                .post(Entity.entity(null, APPLICATION_JSON), Collection.class);
    }

    public void createFile(Note note, File file) {
        MultiPart multipart = new MultiPart();
        multipart.setMediaType(MediaType.MULTIPART_FORM_DATA_TYPE);
        multipart.bodyPart(new FileDataBodyPart("file", file, MediaType.APPLICATION_OCTET_STREAM_TYPE));

        Response resp = ClientBuilder.newClient(new ClientConfig())
                .target(SERVER).path("api/notes/" + note.getId() + "/files")
                .request()
                .post(Entity.entity(multipart, multipart.getMediaType()));
        if (resp.getStatus() != 201) {
            throw new RuntimeException("Server responded with unexpected status code: " + resp.getStatus());
        }
    }

    public void deleteFile(FileEntity file) {
        Response resp = ClientBuilder.newClient(new ClientConfig())
                .target(SERVER).path("api/notes/files/" + file.getId())
                .request()
                .delete();
    }

    public FileEntity updateFileName(FileEntity file) {
        return ClientBuilder.newClient(new ClientConfig())
                .target(SERVER).path("api/notes/files/" + file.getId())
                .request(APPLICATION_JSON)
                .put(Entity.entity(file, APPLICATION_JSON), FileEntity.class);
    }

    public List<FileEntity> getFilesForNote(long noteId) {
        return ClientBuilder.newClient(new ClientConfig())
                .target(SERVER)
                .path("api/notes/" + noteId + "/files")
                .request(APPLICATION_JSON)
                .get(new GenericType<>() {
                });
    }

}
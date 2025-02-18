package client.scenes;

import client.Config;
import client.ConfigManager;
import client.elements.FileElement;
import client.utils.*;
import commons.Collection;
import commons.FileEntity;
import commons.Note;
import jakarta.inject.Inject;
import javafx.animation.FadeTransition;
import javafx.animation.RotateTransition;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.StringConverter;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class NoteEditCtrl implements Initializable {
    // Utils fields
    private final ServerUtils server;
    private final KeyStrokeUtil keyStroke;
    private final MarkdownUtil markdown;
    private final LocaleUtil localeUtil;
    private final DialogUtil dialogUtil;
    private final HashUtil hashUtil;
    // Controller fields
    private final MainCtrl mainCtrl;
    // Internationalization fields
    private ResourceBundle resourceBundle;
    private final ObjectProperty<Locale> selectedLanguage = new SimpleObjectProperty<>();
    // Config fields
    private final Config config;
    private final ConfigManager configManager;
    // Flag fields
    private boolean deleteFlag;
    private Collection currentCollection;
    private Note currentNote;

    @FXML
    private Label saveLabel;

    @FXML
    private ListView<Note> noteListView;

    @FXML
    private TextArea editingArea;

    @FXML
    private MenuBar menuBar;

    @FXML
    private WebView markdownPreview;

    @FXML
    private TextField searchField;

    @FXML
    private TextField titleField;

    @FXML
    private MenuButton collectionBox;

    @FXML
    private ComboBox<Locale> liveLanguageBox;

    @FXML
    private MenuButton currentCollectionDrop;

    @FXML
    private StackPane refreshPane; // Add a placeholder in your FXML to hold the animation

    private RotateTransition refreshAnimation;

    @FXML
    private FlowPane filesPane;

    @Inject
    public NoteEditCtrl(ServerUtils server, KeyStrokeUtil keyStroke, MarkdownUtil markdown, LocaleUtil localeUtil,
                        DialogUtil dialogUtil, HashUtil hashUtil, MainCtrl mainCtrl, Config config, ConfigManager configManager) {
        this.server = server;
        this.keyStroke = keyStroke;
        this.markdown = markdown;
        this.localeUtil = localeUtil;
        this.dialogUtil = dialogUtil;
        this.hashUtil = hashUtil;
        this.mainCtrl = mainCtrl;
        this.config = config;
        this.configManager = configManager;
        this.deleteFlag = false;
    }

    public Note getCurrentNote() {
        return currentNote;
    }

    public void setCurrentNote(Note note) {
        noteListView.getSelectionModel().select(note);
    }

    public MenuButton getCollectionBox() {
        return collectionBox;
    }

    public ObjectProperty<Locale> getSelectedLanguage() {
        return selectedLanguage;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        createRefresh();
        // Load resourceBundle
        this.resourceBundle = resourceBundle;
        // Set the "All" option as default selection
        collectionBox.setText(resourceBundle.getString("collections.all"));
        // Set for client start
        titleField.setEditable(false);
        noteListView.setEditable(true);
        // Load collections from the server + config, add the collections as menuItems
        deleteAllButtons();
        List<Collection> configCollections = config.getCollections();
        Collection defaultCollection = configManager.getDefaultCollection();
        loadConfigCollections(defaultCollection, configCollections);
        // Set Language Box with languages and flags
        liveLanguageBox.setItems(FXCollections.observableList(localeUtil.getAvailableLocales()));
        liveLanguageBox.setCellFactory(c -> new ListCell<>() {
            private final ImageView flagView = new ImageView();

            {
                flagView.setFitHeight(20);
                flagView.setPreserveRatio(true);
            }

            @Override
            protected void updateItem(Locale locale, boolean empty) {
                super.updateItem(locale, empty);
                if (empty) {
                    this.setGraphic(null);
                    this.setText(null);
                } else {
                    flagView.setImage(localeUtil.getFlagImage(locale));
                    this.setGraphic(this.flagView);
                    this.setText(locale.getDisplayName(selectedLanguage.get()));
                }
            }
        });
        liveLanguageBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Locale locale, boolean empty) {
                super.updateItem(locale, empty);
                this.setText(!empty ? locale.getDisplayName(selectedLanguage.get()) : null);
            }
        });
        liveLanguageBox.getSelectionModel().selectedItemProperty().addListener((observableValue, oldValue, newValue) -> {
            if (newValue == null) {
                return;
            }
            this.setLanguage(newValue);
            configManager.saveLanguage(newValue);
        });
        // Note title addition
        noteListView.setCellFactory(c -> new TextFieldListCell<>(new StringConverter<>() {
            @Override
            public String toString(Note note) {
                if (note == null) return "";
                return note.getTitle();
            }

            @Override
            public Note fromString(String newTitle) {
                Note selectedNote = noteListView.getSelectionModel().getSelectedItem();
                if (selectedNote.getTitle().equals(newTitle.strip())) {
                    System.out.println("Title is unchanged. No action taken.");
                    return selectedNote;
                }

                if (newTitle.isBlank()) {
                    System.out.println("Title is empty");
                    dialogUtil.showDialog(resourceBundle, Alert.AlertType.WARNING,
                            "popup.emptyTitle");
                    return selectedNote;
                }
                Optional<Note> duplicatedTitle = server.getNotesByCollection(selectedNote.getCollection().getId())
                        .stream()
                        .filter(note -> note.getTitle().equals(newTitle.strip()))
                        .findAny();

                if (duplicatedTitle.isEmpty()) {
                    selectedNote.setTitle(newTitle.strip());
                    titleField.setText(newTitle.strip());
                    server.updateNote(selectedNote);
                    handleNoteSelect(selectedNote);
                } else {
                    System.out.println("Title already exists");
                    dialogUtil.showDialog(resourceBundle, Alert.AlertType.WARNING,
                            "popup.duplicateTitle");
                }
                return selectedNote;
            }
        }));
        // Listener for search bar
        searchField.textProperty().addListener((observableValue, oldValue, newValue) -> this.filterNotes());
        // Double-click triggers note title editing
        noteListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                int selectedNoteIndex = noteListView.getSelectionModel().getSelectedIndex();
                if (selectedNoteIndex != -1)
                    noteListView.edit(selectedNoteIndex);
            }
        });
        // Listener for note saving logic
        noteListView.getSelectionModel().selectedItemProperty()
                .addListener((observableValue, old, current) -> listenerSaveChangeNote(old, current));
        // Listener for Markdown
        editingArea.textProperty().addListener((observableValue, oldText, newText) ->
                markdown.renderMarkdownInWebView(newText, markdownPreview));
        // Used for autosave on keystrokes
        editingArea.setOnKeyTyped(c -> {
            keyStroke.increaseCounter();
            if (keyStroke.getCounter() == keyStroke.getTrigger() && !editingArea.getText().isEmpty()) {
                autoSave();
                keyStroke.counterReset();
            }
        });
        editingArea.textProperty().addListener((observableValue, oldText, newText) -> renderMarkdown(newText));
        // Until the user has selected a note to edit, display an informative message & do not allow the user to type.
        this.handleNoteSelect(null);
        this.keyShortcuts();

    }

    // EXTRACTED METHODS FOR INITIALIZE

    private void loadConfigCollections(Collection defaultCollection, List<Collection> configCollections) {
        if (!server.getCollections().contains(defaultCollection)) {
            server.addCollection(defaultCollection);
        }
        configManager.addCollection(defaultCollection);
        for (Collection collection : configCollections) {
            if (!server.getCollections().contains(collection)) {
                server.addCollection(collection);
            }
        }
        List<Collection> serverCollections = server.getCollections();
        for (Collection collection : serverCollections) {
            addCollectionToMenuButton(collection, collection.equals(defaultCollection));
        }
    }

    private void listenerSaveChangeNote(Note old, Note current) {
        if (deleteFlag) {
            this.deleteFlag = false;
            if (current != null) {
                titleField.setText(current.getTitle());
            }
            return;
        }
        if (current == null || current.getTitle().isEmpty()) {
            return;
        } else {
            titleField.setText(current.getTitle());
        }
        this.saveChanges(old);
        this.handleNoteSelect(current);
    }

    // MARKDOWN RENDER LOGIC

    private void renderMarkdown(String markdownContent) {
        URL cssFileUrl = MarkdownUtil.class.getResource("/css/markdown-style.css");
        if (cssFileUrl != null) {
            markdown.renderMarkdownInWebView(markdownContent, markdownPreview);
        } else {
            markdown.renderMarkdown(markdownContent, markdownPreview);
        }
    }

    // NOTE FILTERING
    public void focusSearch(MouseEvent mouseEvent) {
        searchField.requestFocus();
    }
    /**
     * called when the user clicks the "Search" button
     * This method displays all the notes in the current collection that contain the given keyword.
     */
    public void filterNotes() {
        String query = searchField.getText();
        String currentSelection = collectionBox.getText();
        if (query == null || query.isEmpty()) {
            if (currentSelection.equals(resourceBundle.getString("collections.all"))) {
                handleAllCollectionsSelected();
            } else if (currentSelection.equals(resourceBundle.getString("collections.defaultCollection"))) {
                handleDefaultCollection();
            } else {
                handleSpecificCollectionSelected(server.getCollectionByName(currentSelection));
            }
            return;
        }
        List<Note> filteredNotes;
        if (currentSelection.equals(resourceBundle.getString("collections.all"))) {
            filteredNotes = server.searchKeyword(query, null);
        } else {
            filteredNotes = server.searchKeyword(query, currentCollection.getId());
        }
        noteListView.setItems(FXCollections.observableList(filteredNotes));
    }

    // NOTE HANDLING

    // Called whenever the user clicks the "New Note" button.
    public void createNewNote() {
        Note note = server.newEmptyNote();
        titleField.setText(note.getTitle());
        Collection defaultCollection = configManager.getDefaultCollection();
        if (currentCollection == null) {
            note.setCollection(defaultCollection);
            server.linkNoteToCollection(defaultCollection.getId(), note);
            currentCollectionDrop.setText(defaultCollection.getName());
        } else {
            note.setCollection(currentCollection);
            server.linkNoteToCollection(currentCollection.getId(), note);
        }
        noteListView.getItems().add(note);
        // Updates the location of the editing area on the note currently created
        noteListView.getSelectionModel().select(note);
        editingArea.setEditable(true);
    }

    public void changeNoteTitle() {
        Note selectedNote = noteListView.getSelectionModel().getSelectedItem();
        String newTitle = titleField.getText();
        if (newTitle.isBlank()) {
            System.err.println("Title title must not be empty.");
            dialogUtil.showDialog(this.resourceBundle, Alert.AlertType.WARNING,
                    "popup.emptyTitle");
            return;
        }
        if (server.getNotesByCollection(selectedNote.getCollection().getId())
                .stream()
                .anyMatch(note -> note.getTitle().equals(newTitle))) {
            System.err.println("Note title must be unique.");
            dialogUtil.showDialog(this.resourceBundle, Alert.AlertType.WARNING,
                    "popup.duplicateTitle");
            return;
        }

        selectedNote.setTitle(newTitle);
        server.updateNote(selectedNote);
        editingArea.requestFocus();

        System.out.println("Note title changed to " + newTitle);
        this.refresh();
    }

    // Called whenever the user clicks on one of the notes in the sidebar.
    private void handleNoteSelect(Note note) {
        this.refreshFilesPane(note);
        if (note == null) {
//            If no note is selected, disable editing and show a default message
            this.clearFields();
            return;
        }
        // If a note is selected, enable editing and display its content
        currentNote = note;
        editingArea.setEditable(true);
        editingArea.setText(note.getContent());
        titleField.setEditable(true);
        currentCollectionDrop.setVisible(true);
        currentCollectionDrop.setText(server.getNotes().stream().filter(n
                -> n.getId() == note.getId()).findAny().get().getCollection().getName());
    }

    public void refreshFilesPane(Note note) {
        filesPane.getChildren().clear();
        if (note == null)
            return;

        for (FileEntity file : server.getFilesForNote(note.getId())) {
            FileElement element = new FileElement(mainCtrl, this, server, dialogUtil, resourceBundle, file);
            FlowPane.setMargin(element, new Insets(0, 10, 0, 0));
            filesPane.getChildren().add(element);
        }
    }

    /**
     * Called whenever the user clicks the "Delete" button.
     */
    public void deleteButton() throws IOException {
        Note selectedNote = noteListView.getSelectionModel().getSelectedItem();
        if (selectedNote == null) {
            editingArea.setEditable(false);
            titleField.setEditable(false);
            editingArea.setText(resourceBundle.getString("deleteText"));
            titleField.setText(resourceBundle.getString("deleteText"));
            return;
        }
        boolean deleteConfirmed = confirmationDelete(selectedNote);
        if (!deleteConfirmed) {
            return;
        }
        try {
            deleteFlag = true;
            server.deleteNoteFromServer(selectedNote.getId());
            noteListView.getItems().remove(selectedNote);
            currentCollectionDrop.setVisible(false);
            this.currentNote = null;
            this.clearFields();
            this.refresh();
        } catch (Exception e) {
            e.printStackTrace();
            ButtonType cancelButton = new ButtonType(resourceBundle.getString("popup.savingFailed.cancel"));
            ButtonType retryButton = new ButtonType(resourceBundle.getString("popup.savingFailed.retry"));

            Alert alert = new Alert(Alert.AlertType.ERROR, resourceBundle.getString("deleteText.fail"),
                    cancelButton, retryButton);
            alert.setContentText(resourceBundle.getString("deleteText.fail")
                    .replace("%id%", String.valueOf(selectedNote.getId())));
            Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
            alertStage.getIcons().add(new Image("appIcon/NoteIcon.jpg"));
            Optional<ButtonType> response = alert.showAndWait();
            if (response.isPresent() && response.get() == retryButton) {
                // Start the process again
                this.deleteButton();
            }
        }
    }

    private boolean confirmationDelete(Note selectedNote) {
        Optional<ButtonType> response = dialogUtil.showDialog(resourceBundle, Alert.AlertType.CONFIRMATION, "popup.note.confirmDelete");
        return response.isPresent() && response.get() == ButtonType.OK;
    }

    // NOTE SAVING

    public void changeNoteSavingSettings(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);

        alert.setTitle(resourceBundle.getString("popup.autosave.title"));
        alert.setHeaderText(resourceBundle.getString("popup.autosave.text")
                .replace("%num%", String.valueOf(keyStroke.getTrigger())));
        alert.getDialogPane().getScene().getWindow().setWidth(400);
        alert.getDialogPane().getScene().getWindow().setHeight(250);

        TextField textField = new TextField();
        textField.setPromptText(resourceBundle.getString("popup.autosave.prompt"));
        // Add the TextField to a layout (VBox)
        VBox content = new VBox();
        content.setSpacing(10);
        content.getChildren().add(textField);

        // Set the custom content to the Alert
        alert.getDialogPane().setContent(content);
        Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
        alertStage.getIcons().add(new Image("appIcon/NoteIcon.jpg"));

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK && !textField.getText().isEmpty()) {
                try {
                    int numberOfKeys = Integer.parseInt(textField.getText());
                    if (numberOfKeys < 10) {
                        dialogUtil.showDialog(resourceBundle, Alert.AlertType.WARNING, "popup.autosave.less");
                        return;
                    } else if (numberOfKeys > 10000) {
                        dialogUtil.showDialog(resourceBundle, Alert.AlertType.WARNING, "popup.autosave.more");
                        return;
                    }
                    keyStroke.setTriggerCount(numberOfKeys);
                } catch (NumberFormatException e) {
                    dialogUtil.showDialog(resourceBundle, Alert.AlertType.ERROR, "popup.autosave.invalid");
                }
            } else if (response == ButtonType.CANCEL) {
                alert.close();
            }
        });
    }

    private void saveLabelTransition() {
        FadeTransition fadeIn = new FadeTransition(Duration.millis(250), saveLabel);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.setOnFinished(e -> {
            // Hold the label visible for 1 seconds
            FadeTransition fadeOut = new FadeTransition(Duration.millis(250), saveLabel);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeOut.setDelay(Duration.seconds(1)); // Wait 1 second before fading out
            fadeOut.play();
        });
        fadeIn.play();
    }

    public void autoSave() {
        Note note = noteListView.getSelectionModel().getSelectedItem();
        note.setContent(editingArea.getText());
        server.addNote(note);
        this.saveLabelTransition();
        System.out.println("Changes saved");// This line is just for debugging purpose
    }

    /**
     * Called on exiting the app
     */
    public void saveChanges() {
        this.saveChanges(noteListView.getSelectionModel().getSelectedItem());
    }

    /**
     * Called when switching notes in ListView and when exiting the app
     */
    public void saveChanges(Note note) {
        if (note == null)
            return;
        note.setContent(editingArea.getText());
        try {
            //System.out.println(note.getFiles());
            server.addNote(note);
            this.saveLabelTransition();
            System.out.println("Changes were saved to note " + note.getId());
        } catch (Exception ex) {
            System.err.println("Saving changes to note " + note.getId() + " failed:");
            ex.printStackTrace();

            ButtonType cancelButton = new ButtonType(resourceBundle.getString("popup.savingFailed.cancel"));
            ButtonType retryButton = new ButtonType(resourceBundle.getString("popup.savingFailed.retry"));

            Alert alert = new Alert(Alert.AlertType.ERROR, resourceBundle.getString("popup.savingFailed.title"),
                    cancelButton, retryButton);
            alert.setContentText(resourceBundle.getString("popup.savingFailed.text")
                    .replace("%id%", String.valueOf(note.getId())));
            Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
            alertStage.getIcons().add(new Image("appIcon/NoteIcon.jpg"));
            Optional<ButtonType> response = alert.showAndWait();
            if (response.isPresent() && response.get() == retryButton) {
                // Start the process again
                this.saveChanges(note);
            }
        }
    }

    // COLLECTION RELATED

    /**
     * A method to add the buttons for changing a collection and moving a note of a specific collection
     *
     * @param collection - collection to add
     */
    public void addCollectionToMenuButton(Collection collection, boolean defaultCollection) {
        //System.out.println("Collection button added");
        MenuItem newCollectionItem;
        MenuItem newCollectionChangeItem;
        if (defaultCollection) {
            newCollectionItem = new MenuItem(collection.getName() + "(Default)");
            newCollectionChangeItem = new MenuItem(collection.getName() + "(Default)");
        } else {
            newCollectionItem = new MenuItem(collection.getName());
            newCollectionChangeItem = new MenuItem(collection.getName());
        }

        //Handle the added buttons
        newCollectionItem.setOnAction(a -> this.handleSpecificCollectionSelected(collection));
        newCollectionChangeItem.setOnAction(a -> this.moveNoteToCollection(currentNote, collection.getName()));
        // Add the new MenuItem to the MenuButton
        MenuItem editCollections = collectionBox.getItems().getLast();
        collectionBox.getItems().removeLast();
        collectionBox.getItems().add(newCollectionItem);
        collectionBox.getItems().add(editCollections);
        currentCollectionDrop.getItems().add(newCollectionChangeItem);
    }


    /**
     * A method to update the buttons for changing a collection and moving a note
     *
     * @param modifiedCollection - collection that was modified
     * @param newTitle           - String with the Title that the buttons are updated
     */
    public void updateButtons(Collection modifiedCollection, String newTitle) {
        System.out.println("Collection button updated");
        collectionBox.getItems().stream()
                .filter(mI -> mI.getText().equals(modifiedCollection.getName()) || mI.getText().equals(modifiedCollection.getName() + "(Default)"))
                .findFirst().get()
                .setText(newTitle);
        currentCollectionDrop.getItems().stream()
                .filter(mI -> mI.getText().equals(modifiedCollection.getName()) || mI.getText().equals(modifiedCollection.getName() + "(Default)"))
                .findFirst().get()
                .setText(newTitle);
    }

    public void deleteAllButtons() {
        for (MenuItem item : currentCollectionDrop.getItems().stream().toList()) {
            if (item.getText().equals(resourceBundle.getString("collections.defaultCollection"))) {
                continue;
            }
            currentCollectionDrop.getItems().remove(item);
        }
        for (MenuItem item : collectionBox.getItems().stream().toList()) {
            if (item.getText().equals(resourceBundle.getString("collections.all")) ||
                    item.getText().equals(resourceBundle.getString("collections.edit")) ||
                    item.getText().equals(resourceBundle.getString("collections.defaultCollection"))) {
                continue;
            }
            collectionBox.getItems().remove(item);
        }
    }

    /**
     * Handler for "All" option
     */
    public void handleAllCollectionsSelected() {
        System.out.println("All button pressed");

        collectionBox.setText(resourceBundle.getString("collections.all"));
        currentCollection = null;

        List<Note> notes = server.getNotes();
        currentCollection = configManager.getDefaultCollection();

        // Clear the current list
        noteListView.getItems().clear();

        // Add the notes to the ListView
        noteListView.getItems().addAll(notes);

        this.clearFields();
    }

    public void handleDefaultCollection() {
        System.out.println("Default collection handled"); //for debugging purposes
        Collection defaultCollection = configManager.getDefaultCollection();
        currentCollection = defaultCollection;
        currentNote = null;
        collectionBox.setText("Default Collection");

        // Clear the current list
        noteListView.getItems().clear();

        // Add the notes to the ListView
        noteListView.getItems().addAll(server.getNotesByCollection(server.getCollectionByName(defaultCollection.getName()).getId()));

        this.clearFields();
        currentCollectionDrop.setVisible(false);
    }

    /**
     * Handler for "Edit collections"
     */
    public void handleEditCollections() {
        System.out.println("Edit button pressed");
        mainCtrl.showCollectionEdit(this.resourceBundle);
    }

    /**
     * Displaying a given list of notes (from a collection) in the listview
     *
     * @param selectedItem - collection
     */
    private void handleSpecificCollectionSelected(Collection selectedItem) {
        System.out.println("Collection handled"); //for debugging purposes
        List<Note> notes = server.getNotesByCollection(selectedItem.getId());
        currentCollection = selectedItem;
        currentNote = null;
        collectionBox.setText(selectedItem.getName()); //set the name of the collection to show in the MenuButton

        // Clear the current list
        noteListView.getItems().clear();

        // Add the notes to the ListView
        noteListView.getItems().addAll(notes);

        this.clearFields();
        currentCollectionDrop.setVisible(false);
    }

    /**
     * Method to move the selected Note into a new Collection
     *
     * @param currentNote          - note to be moved
     * @param collectionChangeItem - the MenuItem corresponding to the new Collection
     */
    public void moveNoteToCollection(Note currentNote, String collectionChangeItem) {
        System.out.println("Collection trying to be moved");
        if (currentNote == null) {
            dialogUtil.showDialog(this.resourceBundle, Alert.AlertType.ERROR, "popup.moveNote.noneSelected");
            return;
        }
        try {
            Collection newCollection = server.getCollectionByName(collectionChangeItem);
            if (currentNote.getCollection().equals(newCollection)) {
                dialogUtil.showDialog(this.resourceBundle, Alert.AlertType.WARNING,
                        "popup.moveNote.sameCollection");
                return;
            }
            List<String> noteTitles = server.getNotesByCollection(newCollection.getId()).stream().map(Note::getTitle).toList();
            if (noteTitles.contains(currentNote.getTitle())) {
                dialogUtil.showDialog(this.resourceBundle, Alert.AlertType.WARNING, "popup.moveNote.sameTitleInCollection");
                return;
            }
            currentNote.setCollection(newCollection);
            server.updateNote(currentNote);

            dialogUtil.showDialog(this.resourceBundle, Alert.AlertType.INFORMATION,
                    "popup.moveNote.success", Map.of("%collection%", newCollection.getName()));
            currentCollectionDrop.setText(collectionChangeItem);
            if (collectionBox.getText().equals(resourceBundle.getString("collections.all"))) {
                return;
            }
            this.currentNote = null;
            this.refresh();
            this.clearFields();
            currentCollectionDrop.setVisible(false);
        } catch (Exception ex) {
            dialogUtil.showDialog(this.resourceBundle, Alert.AlertType.ERROR, "popup.moveNote.error");
            ex.printStackTrace();
        }
    }

    public void changeToDefaultCollection() {
        moveNoteToCollection(noteListView.getSelectionModel().getSelectedItem(), configManager.getDefaultCollection().getName());
    }

    public void setCollectionLabelText(String name) {
        collectionBox.setText(name);
    }

    /**
     * method used when the title of a collection is changed
     *
     * @param oldTitle old title to be changed
     * @param newTitle new title to change the old one
     */
    public void updateCurrentCollectionDropText(String oldTitle, String newTitle) {
        if (oldTitle == null) {
            System.out.println("Cannot change a null title in dropbox.");
            return;
        }
        if (newTitle == null) {
            System.out.println("Cannot change to a null title in dropbox.");
            return;
        }
        if (this.currentCollectionDrop.getText().equals(oldTitle)) {
            this.currentCollectionDrop.setText(newTitle);
        }
    }

    // LANGUAGE RELATED

    /**
     * Sets the application language to the specified Locale.
     *
     * @param locale The Locale object representing the language to set.
     */
    public void setLanguage(Locale locale) {
        this.selectedLanguage.setValue(locale);
        liveLanguageBox.setValue(locale);
    }

    // REFRESH CLEAR FIELDS

    /**
     * Called whenever the user clicks the "Refresh" button.
     */
    public void refresh() {
        List<Note> notes;
        if (currentCollection == null || collectionBox.getText().equals(resourceBundle.getString("collections.all"))) {
            notes = server.getNotes(); // if no note selected then do not refresh the file pane
        } else {
            notes = server.getNotesByCollection(currentCollection.getId());
        }
        noteListView.setItems(FXCollections.observableList(notes));
        refreshAnimation.play();
        refreshPane.setVisible(true);
        new Thread(() -> {
            try {
                Thread.sleep(700);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                javafx.application.Platform.runLater(() -> {
                    refreshPane.setVisible(false);
                    refreshAnimation.stop();
                });
            }
        }).start();
    }

    public void refreshButton() {
        this.refresh();
        //logic for refreshing everything related to collections
        this.deleteAllButtons();
        Collection defaultCollection = configManager.getDefaultCollection();
        server.getCollections().forEach(c -> addCollectionToMenuButton(c, c.equals(defaultCollection)));

        if (!(collectionBox.getText().equals(resourceBundle.getString("collections.all")) ||
                collectionBox.getText().equals(resourceBundle.getString("collections.defaultCollection")))) {
            this.setCollectionLabelText(server.getCollectionById(currentCollection.getId()).getName());
        }

        if (currentNote == null) {
            return; // check if note is null if so return since the small refresh is enough
        }

        Optional<Note> noteOptional = server.getNotes().stream().filter(n -> n.getId() == currentNote.getId()).findFirst();

        if (noteOptional.isPresent()) {
            Note note = noteOptional.get();
            //System.out.println(note.getId() + " " + note.getContent());
            editingArea.setText(note.getContent());
            editingArea.setScrollTop(Double.MAX_VALUE);
            titleField.setText(note.getTitle());
            this.refreshFilesPane(note);
        } else {
            this.clearFields();
        }
    }

    private void createRefresh() {
        Arc refreshArc = new Arc();
        refreshArc.setRadiusX(10);
        refreshArc.setRadiusY(10);
        refreshArc.setStartAngle(45);
        refreshArc.setLength(250);
        refreshArc.setType(ArcType.OPEN);
        refreshArc.setStroke(Color.WHITE);
        refreshArc.setStrokeWidth(3);
        refreshArc.setFill(null);

        // Add the refresh circle to the StackPane
        refreshPane.getChildren().add(refreshArc);

        // Set up the animation
        refreshAnimation = new RotateTransition(Duration.seconds(1.5), refreshArc);
        refreshAnimation.setByAngle(360);
        refreshAnimation.setCycleCount(RotateTransition.INDEFINITE);
        refreshAnimation.setInterpolator(javafx.animation.Interpolator.LINEAR);
    }

    private void clearFields() {
        noteListView.getSelectionModel().clearSelection();
        editingArea.setEditable(false);
        editingArea.setText(resourceBundle.getString("initialText"));
        titleField.setText(resourceBundle.getString("initialText"));
        currentCollectionDrop.setVisible(false);
        filesPane.getChildren().clear();
    }


    // KEYBINDINGS

    private void keyShortcuts() {
        noteListView.sceneProperty().addListener((observableValue, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.setOnKeyPressed(event -> {
                    Map<KeyCombination, Runnable> keyActions = keyCodeCombinations();
                    keyActions.entrySet().stream()
                            .filter(entry -> entry.getKey().match(event))
                            .findFirst()
                            .ifPresent(entry -> entry.getValue().run());
                });
            }
        });
    }

    /**
     * Defines keyboard shortcuts for the application's main functions.
     * Maps key combinations to their corresponding actions using KeyCodeCombination.
     *
     * @return A map containing key combinations and their associated actions
     */
    private Map<KeyCombination, Runnable> keyCodeCombinations() {

        Map<KeyCombination, Runnable> combinations = new HashMap<>();

        combinations.put(new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN), this::createNewNote);
        combinations.put(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN), this::saveChanges);
        combinations.put(new KeyCodeCombination(KeyCode.R, KeyCombination.CONTROL_DOWN), this::refresh);
        combinations.put(new KeyCodeCombination(KeyCode.D, KeyCombination.CONTROL_DOWN), () -> {
            try {
                deleteButton();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        combinations.put(new KeyCodeCombination(KeyCode.ESCAPE), this::focusSearchBar);
        combinations.put(new KeyCodeCombination(KeyCode.RIGHT, KeyCombination.ALT_DOWN), this::nextCollection);
        combinations.put(new KeyCodeCombination(KeyCode.LEFT, KeyCombination.ALT_DOWN), this::handleAllCollectionsSelected);
        combinations.put(new KeyCodeCombination(KeyCode.D, KeyCombination.ALT_DOWN), this::handleDefaultCollection);
        combinations.put(new KeyCodeCombination(KeyCode.DOWN, KeyCombination.ALT_DOWN), this::nextNote);
        combinations.put(new KeyCodeCombination(KeyCode.UP, KeyCombination.ALT_DOWN), this::previousNote);
        combinations.put(new KeyCodeCombination(KeyCode.ENTER, KeyCombination.CONTROL_DOWN), this::editNoteContent);
        combinations.put(new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN, KeyCombination.ALT_DOWN), this::handleEditCollections);
        combinations.put(new KeyCodeCombination(KeyCode.A, KeyCombination.CONTROL_DOWN, KeyCombination.ALT_DOWN), this::addFile);
        combinations.put(new KeyCodeCombination(KeyCode.DIGIT1, KeyCombination.CONTROL_DOWN), () -> accessMenuBar("settings"));
        combinations.put(new KeyCodeCombination(KeyCode.DIGIT2, KeyCombination.CONTROL_DOWN), () -> accessMenuBar("menu"));
        combinations.put(new KeyCodeCombination(KeyCode.L, KeyCombination.CONTROL_DOWN), this::showLanguages);

        return combinations;
    }

    /**
     * Sets the focus to the search field when the ESC key is pressed.
     * This allows quick access to the search functionality.
     */
    private void focusSearchBar() {
        searchField.requestFocus();
    }

    /**
     * Shows a specific menu from the MenuBar based on the provided menu name.
     * This method is primarily used for keyboard shortcuts to display menu items.
     *
     * @param menu The name of the menu to show. Valid values are:
     *             - "settings" for the first menu
     *             - "menu" for the second menu
     *             Case-sensitive string comparison is used.
     */
    private void accessMenuBar(String menu) {
        ObservableList<Menu> menus = menuBar.getMenus();
        if(!menus.isEmpty()) {
            if(menu.equals("settings")) {
                menus.getFirst().show();
            }
            if(menu.equals("menu")) {
                menus.get(1).show();
            }
        }
    }

    /**
     * Displays the language selection ComboBox.
     * Shows a dropdown list of available languages for the application.
     * This method is triggered via keyboard shortcut CTRL+L.
     */
    private void showLanguages() {
        liveLanguageBox.show();
    }

    /**
     * Navigates to the next collection in the collection box.
     * Skips the first two items ("All" and "Edit Collections...") and cycles through the remaining collections.
     * If no current collection is selected, selects the first available collection.
     * If at the end of the list, wraps around to the beginning.
     */
    public void nextCollection() {
        System.out.println("Next collection selected");
        List<MenuItem> menuItems = collectionBox.getItems();

        if (menuItems.size() <= 2) {
            System.out.println("Not enough collections to scroll through");
            return;
        }

        List<MenuItem> collectionItems = menuItems.subList(2, menuItems.size());

        if (currentCollection == null) {
            System.out.println("No current collection set, defaulting to the first available collection");
            for (MenuItem item : collectionItems) {
                Collection collection = findCollectionByName(item.getText());
                if (collection != null) {
                    currentCollection = collection;
                    handleSpecificCollectionSelected(currentCollection);
                    return;
                }
            }
            System.out.println("No valid collections found.");
            return;
        }

        calculateIndex(collectionItems);
    }

    private void calculateIndex(List<MenuItem> collectionItems) {
        int currentIndex = -1;
        for (int i = 0; i < collectionItems.size(); i++) {
            if (collectionItems.get(i).getText().equals(currentCollection.getName())) {
                currentIndex = i;
                break;
            }
        }

        int nextIndex = (currentIndex + 1) % collectionItems.size();
        while (true) {
            MenuItem nextCollectionItem = collectionItems.get(nextIndex);
            Collection nextCollection = findCollectionByName(nextCollectionItem.getText());
            if (nextCollection != null && !nextCollection.getName().equals(currentCollection.getName())) {
                currentCollection = nextCollection;
                handleSpecificCollectionSelected(currentCollection);
                return;
            }
            nextIndex = (nextIndex + 1) % collectionItems.size();
            if (nextIndex == currentIndex) {
                System.out.println("No other valid collections found.");
                return;
            }
        }
    }


    /**
     * Finds a collection by its name in the server's collection list.
     *
     * @param name The name of the collection to find
     * @return The Collection object if found, null otherwise
     */
    private Collection findCollectionByName(String name) {
        return server.getCollections().stream()
                .filter(collection -> collection.getName().equals(name) || collection.getName().equals(name + "Default"))
                .findFirst()
                .orElse(null);
    }

    /**
     * Moves the selection to the next note in the note list.
     * If the current note is the last one, the selection remains unchanged.
     * Automatically scrolls to make the selected note visible.
     */
    private void nextNote() {

        int currentIndex = noteListView.getSelectionModel().getSelectedIndex();
        if (currentIndex < noteListView.getItems().size() - 1) {
            noteListView.getSelectionModel().select(currentIndex + 1);
            noteListView.scrollTo(currentIndex + 1);
        }
    }

    /**
     * Moves the selection to the previous note in the note list.
     * If the current note is the first one, the selection remains unchanged.
     * Automatically scrolls to make the selected note visible.
     */
    private void previousNote() {

        int currentIndex = noteListView.getSelectionModel().getSelectedIndex();
        if (currentIndex > 0) {
            noteListView.getSelectionModel().select(currentIndex - 1);
            noteListView.scrollTo(currentIndex - 1);
        }
    }

    /**
     * Sets the focus to the note editing area when Ctrl+Enter is pressed.
     * This allows quick access to edit the currently selected note.
     */
    private void editNoteContent() {
        editingArea.requestFocus();
    }

    //FILE LOGIC

    public void addFile() {
        if (currentNote == null) {
            //popup for embedding a file with no note selected
            dialogUtil.showDialog(resourceBundle, AlertType.WARNING, "popup.files.noteNotSelected");
            return;
        }

        File file = mainCtrl.promptFileOpen();
        if (file == null) {
            // User cancelled file dialog
            return;
        }

        System.out.println("Adding file: " + file.getPath());
        try {
            Note note = this.currentNote;
            String fileHash = hashUtil.computeFileHash(Files.readAllBytes(file.toPath()));
            List<FileEntity> existingFiles = server.getFilesForNote(currentNote.getId());
            boolean fileExists = existingFiles.stream()
                    .anyMatch(f -> {
                        try {
                            return hashUtil.computeFileHash(f.getData()).equals(fileHash);
                        } catch (NoSuchAlgorithmException | IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
            if (fileExists) {
                dialogUtil.showDialog(resourceBundle, AlertType.ERROR, "popup.files.alreadyExists"); // check if the file was already added
                return;
            }
            server.createFile(this.currentNote, file);

            // Fetch all files for the note to retrieve their metadata
            List<FileEntity> files = server.getFilesForNote(currentNote.getId());
            FileEntity uploadedFile = files.stream()
                    .filter(f -> f.getName().equals(file.getName()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Uploaded file not found on server"));
            //System.out.println(" " + uploadedFile.getName() + " " + uploadedFile.getId());
            note.getFiles().add(uploadedFile);

            renderFile(uploadedFile, note);

            this.refreshFilesPane(note);

            dialogUtil.showDialog(this.resourceBundle, AlertType.INFORMATION,
                    "popup.files.added");
        } catch (Exception ex) {
            System.err.println("Failed to upload file to server");
            ex.printStackTrace();

            dialogUtil.showDialog(this.resourceBundle, AlertType.ERROR,
                    "popup.files.uploadFailed");
        }

    }

    public void renderFile(FileEntity uploadedFile, Note note) {
        String serverPath = server.getServerPath() + "api/notes/" + currentNote.getId() + "/files/" + uploadedFile.getId();
        String markdownLink = "![" + uploadedFile.getName() + "](" + serverPath + ")";
        editingArea.appendText(markdownLink);

        this.saveChanges(note);
        this.refresh();
        editingArea.setScrollTop(Double.MAX_VALUE);
        noteListView.requestFocus();
    }

    public void unRenderFile(FileEntity deletedFile, Note note) {
        String text = editingArea.getText();
        String serverPath = server.getServerPath() + "api/notes/" + currentNote.getId() + "/files/" + deletedFile.getId();
        String toRemove = "![" + deletedFile.getName() + "](" + serverPath + ")"; // The string you want to remove

        // Remove all occurrences of 'toRemove' from the text in the TextArea
        String updatedText = text.replace(toRemove, "");
        editingArea.setText(updatedText);

        note.getFiles().remove(deletedFile);
        this.saveChanges(note);
        this.refresh();
        editingArea.setScrollTop(Double.MAX_VALUE);
        noteListView.requestFocus();
    }

    public void replaceRender(FileEntity replacedFile, Note note, String oldName) {
        String text = editingArea.getText();
        String serverPath = server.getServerPath() + "api/notes/" + currentNote.getId() + "/files/" + replacedFile.getId();
        String toReplace = "![" + replacedFile.getName() + "](" + serverPath + ")";
        String toRemove = "![" + oldName + "](" + serverPath + ")";
        String updatedText = text.replace(toRemove, toReplace);
        editingArea.setText(updatedText);

        this.saveChanges(note);
        this.refresh();
        // This is for making sure that when the user clicks the editing area it is sent to the end of the text
        editingArea.setScrollTop(Double.MAX_VALUE);
    }
}
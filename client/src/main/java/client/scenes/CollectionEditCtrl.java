package client.scenes;

import client.ConfigManager;
import client.utils.DialogUtil;
import client.utils.ServerUtils;
import commons.Collection;
import jakarta.inject.Inject;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.net.URL;
import java.util.*;

public class CollectionEditCtrl implements Initializable {
    // Utils fields
    private final ServerUtils server;
    private final DialogUtil dialogUtil;
    // Controller fields
    private final NoteEditCtrl noteEditCtrl;
    // Config fields
    private final ConfigManager configManager;
    // Flag fields
    private Collection currentCollection;
    // Language fields
    private ResourceBundle resourceBundle;
    private final ObjectProperty<Locale> selectedLanguage = new SimpleObjectProperty<>();

    @FXML
    private ListView<Collection> collectionListView;

    @FXML
    private TextField serverField;

    @FXML
    private TextField titleField;

    @FXML
    private Label serverStatus;

    @FXML
    private Label defaultLabel;


    @Inject
    public CollectionEditCtrl(ServerUtils server, NoteEditCtrl noteEditCtrl, ConfigManager configManager, DialogUtil dialogUtil) {
        this.server = server;
        this.noteEditCtrl = noteEditCtrl;
        this.configManager = configManager;
        this.dialogUtil = dialogUtil;
        this.currentCollection = null;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.resourceBundle = resourceBundle;

        collectionListView.setEditable(true);
        collectionListView.setCellFactory(c -> new TextFieldListCell<>(new StringConverter<>() {
            @Override
            public String toString(Collection collection) {
                if (collection == null) return "";
                return collection.getName();
            }

            @Override
            public Collection fromString(String newName) {
                Collection selectedCollection = collectionListView.getSelectionModel().getSelectedItem();
                if (selectedCollection != null) {
                    if (newName.isBlank()) {
                        dialogUtil.showDialog(resourceBundle, Alert.AlertType.WARNING,
                                "popup.collections.emptyName");
                        return selectedCollection;
                    }

                    //ensure titles are unique
                    if (server.getCollections().stream()
                            .anyMatch(collection -> collection.getName().equals(newName)
                                    && !collection.equals(selectedCollection))) {
                        System.err.println("Collection name must be unique.");
                        dialogUtil.showDialog(resourceBundle, Alert.AlertType.WARNING,
                                "popup.collections.duplicateName");
                        return selectedCollection;
                    }
                    String oldName = configManager.getCollectionNameById(currentCollection.getId());
                    noteEditCtrl.updateCurrentCollectionDropText(oldName, newName);
                    configManager.changeCollectionName(selectedCollection, newName.strip());
                    selectedCollection.setName(newName.strip());
                    if (selectedCollection.getId() == configManager.getDefaultCollection().getId()) {
                        configManager.setDefaultCollection(selectedCollection);
                    }
                    server.addCollection(selectedCollection);
                    //System.out.println("Collection title changed");
                    refresh();
                }
                return selectedCollection;
            }
        }));
        // Listener for the ListView
        collectionListView.getSelectionModel().selectedItemProperty()
                .addListener((observableValue, old, current) -> {
                    if (current != null && current.getId() == configManager.getDefaultCollection().getId()) {
                        defaultLabel.setText(resourceBundle.getString("default.label.yes"));
                    } else {
                        defaultLabel.setText(resourceBundle.getString("default.label.no"));
                    }
                    handleSelectedCollection(current);
                });
        // Listener for the title change
        titleField.textProperty().addListener((observableValue, old, text) -> statusListenerMethod(text));
        // Listener for the server change
        serverField.textProperty().addListener((observableValue, old, text) -> {
            if (serverField.isFocused())
                serverListenerMethod(text);
        });
        // Change title on double click
        collectionListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                int index = collectionListView.getSelectionModel().getSelectedIndex();
                if (index != -1) {
                    collectionListView.edit(index);
                }
            }
        });
        // On initialization no note is selected
        handleSelectedCollection(null);
    }

    // COLLECTION HANDLING

    /**
     * Method for creating a new collection based on user input
     */
    public void createCollection() {
        // Create a TextInputDialog for entering the collection name
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(resourceBundle.getString("popup.collections.new.title"));
        dialog.setHeaderText(resourceBundle.getString("popup.collections.new.header"));
        dialog.setContentText(resourceBundle.getString("popup.collections.new.input"));

        Stage dialogStage = (Stage) dialog.getDialogPane().getScene().getWindow();
        dialogStage.getIcons().add(new Image("appIcon/NoteIcon.jpg"));

        // Show the dialog and wait for a response
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(collectionName -> {
            if (collectionName.isBlank()) {
                dialogUtil.showDialog(this.resourceBundle, Alert.AlertType.WARNING,
                        "popup.collections.emptyName");
                return;
            }

            List<Collection> existingCollections = server.getCollections();
            if (existingCollections
                    .stream()
                    .anyMatch(collection -> collection.getName().equals(collectionName.strip()))) {
                dialogUtil.showDialog(this.resourceBundle, Alert.AlertType.WARNING,
                        "popup.collections.duplicateName");
                return;
            }
            // Add collection to server
            Collection collection = new Collection(collectionName);
            Collection savedCollection = server.addCollection(collection);

            configManager.addCollection(savedCollection); // add collection to config

            // Add collection to listView
            dialogUtil.showDialog(this.resourceBundle, Alert.AlertType.INFORMATION,
                    "popup.collections.createdSuccessfully");
            collectionListView.getItems().add(savedCollection);
            collectionListView.getSelectionModel().select(savedCollection);
            // Add collection to MenuButton
            this.refresh();
            System.out.println("Collection created successfully");
        });


    }

    /**
     * Delete selected collection
     */
    public void deleteCollection() {
        Collection selectedCollection = collectionListView.getSelectionModel().getSelectedItem();
        if (selectedCollection == null) {
            dialogUtil.showDialog(resourceBundle, Alert.AlertType.WARNING, "popup.Collection.delete.noteSelected");
            System.err.println("Delete attempt with no collection selected");
            return;
        }
        Collection defaultCollection = configManager.getDefaultCollection();
        if (defaultCollection.getId() == selectedCollection.getId()) {
            dialogUtil.showDialog(resourceBundle, Alert.AlertType.WARNING, "popup.Collection.delete.defaultCollectionSelected");
            return;
        }
        if (confirmationDelete(selectedCollection)) {
            try {
                server.deleteCollection(selectedCollection.getId());
                configManager.removeCollection(selectedCollection);
                collectionListView.getItems().remove(selectedCollection);
                this.refresh();
                noteEditCtrl.refresh();
                System.out.println("Collection deleted successfully");
                dialogUtil.showDialog(resourceBundle, Alert.AlertType.INFORMATION, "popup.Collection.delete.successfully");
            } catch (Exception e) {
                System.err.println("Failed to delete collection");
                e.printStackTrace();
            }
        }
    }

    private boolean confirmationDelete(Collection selectedCollection) {
        Optional<ButtonType> response = dialogUtil.showDialog(resourceBundle, Alert.AlertType.CONFIRMATION, "popup.collection.confirmDelete");
        return response.isPresent() && response.get() == ButtonType.OK;
    }

    public void refresh() {
        noteEditCtrl.deleteAllButtons();
        List<Collection> collections = server.getCollections();
        configManager.refreshCollections(collections);
        //System.out.println(collections.toString());

        for (Collection collection : collections) {
            noteEditCtrl.addCollectionToMenuButton(collection, configManager.getDefaultCollection().equals(collection));
        }

        collectionListView.setItems(FXCollections.observableList(collections));

        MenuButton collectionLabel = noteEditCtrl.getCollectionBox();
        if (currentCollection != null) {
            if (collectionLabel.getText().equals(resourceBundle.getString("collections.all")) ||
                    collectionLabel.getText().equals(resourceBundle.getString("collections.defaultCollection"))) {
                return;
            }
            noteEditCtrl.setCollectionLabelText(currentCollection.getName());
        }
        System.out.println("Collections refreshed");
    }

    // UI FIELDS UPDATES

    private void serverListenerMethod(String serverURL) {
        String regex = "^(http://)(localhost(:\\d+)?|[\\w.-]+(\\.[a-z]{2,})+)(/.*)?$";
        if (currentCollection == null) {
            return;
        }
        //System.out.println(serverURL);
        if (!serverURL.matches(regex)) {
            serverStatus.setText(this.resourceBundle.getString("labels.collections.status.invalidPath"));
        } else {
            serverCheckConnection(serverURL);
        }
    }

    private void serverCheckConnection(String serverURL) {
        System.out.println("Request made to: " + serverURL);
        if (server.makeRequest(serverURL, currentCollection) == 200) {
            this.statusListenerMethod(currentCollection.getName());
        } else {
            serverStatus.setText(this.resourceBundle.getString("labels.collections.status.cannotConnect"));
        }
    }

    private void statusListenerMethod(String text) {

        if (server.getCollections()
                .stream()
                .anyMatch(collection -> collection.getName().equals(text))) {
            serverStatus.setText(this.resourceBundle.getString("labels.collections.status.alreadyExists"));
            return;

        }
        if (text.isBlank()) {
            serverStatus.setText(this.resourceBundle.getString("labels.collections.status.blankTitle"));
            return;
        }
        if (server.getCollections()
                .stream()
                .noneMatch(collection -> collection.getName().equals(text))) {
            serverStatus.setText(this.resourceBundle.getString("labels.collections.status.canBeCreated"));
            return;
        }
        serverStatus.setText(this.resourceBundle.getString("labels.collections.status.exists"));
    }

    /**
     * Called by the listener inside initialize method whenever a collection is selected,
     * sets the fields on the right hand side accordingly to the collection selected or
     * if no collection is selected sets a default text in all fields
     *
     * @param selectedCollection the collection that is selected
     */
    public void handleSelectedCollection(Collection selectedCollection) {
        if (selectedCollection == null) {
            //show a basic prompt if no collection selected
            titleField.setEditable(false);
            serverField.setEditable(false);

            String initialText = this.resourceBundle.getString("labels.collections.initialText");
            titleField.setText(initialText);
            serverField.setText(initialText);
            serverStatus.setText(initialText);
            defaultLabel.setText(initialText);

            return;
        }

        //update the current collection
        currentCollection = selectedCollection;
        System.out.println("Collection " + selectedCollection.getName() + " selected");

        //update the fields accordingly
        titleField.setEditable(true);
        serverField.setEditable(true);

        titleField.setText(selectedCollection.getName());
        serverField.setText(server.getServerPath());
        serverStatus.setText(this.resourceBundle.getString("labels.collections.status.exists"));
    }

    public void changeCollectionTitle() {
        if (currentCollection == null) {
            dialogUtil.showDialog(this.resourceBundle, Alert.AlertType.INFORMATION,
                    "popup.collections.noneSelected");
            handleSelectedCollection(null);
            return;
        }
        String oldTitle = currentCollection.getName();
        String newTitle = titleField.getText();
        if (newTitle.isBlank()) {
            System.err.println("Collection name must not be empty.");
            dialogUtil.showDialog(this.resourceBundle, Alert.AlertType.WARNING,
                    "popup.collections.emptyName");
            return;
        }
        if (server.getCollections()
                .stream()
                .anyMatch(collection -> (collection.getName().equals(newTitle) && collection.getId() != currentCollection.getId()))) {
            System.err.println("Collection name must be unique.");
            dialogUtil.showDialog(this.resourceBundle, Alert.AlertType.WARNING,
                    "popup.collections.duplicateName");
            return;
        }

        configManager.changeCollectionName(currentCollection, newTitle.strip());
        currentCollection.setName(newTitle.strip());
        if (currentCollection.getId() == configManager.getDefaultCollection().getId()) {
            configManager.setDefaultCollection(currentCollection);
        }
        server.addCollection(currentCollection);
        noteEditCtrl.updateCurrentCollectionDropText(oldTitle, newTitle);
        System.out.println("Collection title changed to " + newTitle);
        this.refresh();
    }

    public void changeCollectionServer() {
        if (serverStatus.getText().equals(resourceBundle.getString("labels.collections.status.cannotConnect"))) {
            dialogUtil.showDialog(resourceBundle, Alert.AlertType.ERROR, "popup.collections.serverUnavailable");
            return;
        } else if (serverStatus.getText().equals(resourceBundle.getString("labels.collections.status.invalidPath"))) {
            dialogUtil.showDialog(resourceBundle, Alert.AlertType.ERROR, "popup.collections.invalidPath");
            return;
        } else if (serverStatus.getText().equals(resourceBundle.getString("labels.collections.status.alreadyExists"))) {
            dialogUtil.showDialog(resourceBundle, Alert.AlertType.ERROR, "popup.collections.alreadyExistsServer");
            return;
        }
        collectionListView.requestFocus();
    }

    // LANGUAGE

    /**
     * Method to set the Language of the scene
     *
     * @param locale the Locale with the language that needs to be set for the scene
     */
    public void setLanguage(Locale locale) {
        this.selectedLanguage.setValue(locale);
    }

    /**
     * method triggered when pressing the "Make Default Button"
     * it sets the selected method as default and displays an informative message
     */
    public void setCollectionAsDefault() {
        Collection selectedCollection = collectionListView.getSelectionModel().getSelectedItem();
        if (selectedCollection == null) {
            dialogUtil.showDialog(this.resourceBundle, Alert.AlertType.INFORMATION,
                    "popup.collections.noneSelected");
        } else {
            noteEditCtrl.updateButtons(configManager.getDefaultCollection(), configManager.getDefaultCollection().getName());
            configManager.setDefaultCollection(selectedCollection);
            dialogUtil.showDialog(this.resourceBundle, Alert.AlertType.INFORMATION,
                    "popup.collections.defaultChanged",
                    Map.of("%name%", selectedCollection.getName()));
            defaultLabel.setText(resourceBundle.getString("default.label.yes"));
            noteEditCtrl.updateButtons(selectedCollection, selectedCollection.getName() + "(Default)");
        }
    }
}

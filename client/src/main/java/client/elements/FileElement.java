package client.elements;

import client.scenes.MainCtrl;
import client.scenes.NoteEditCtrl;
import client.utils.DialogUtil;
import client.utils.ServerUtils;
import commons.FileEntity;
import commons.Note;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.ResourceBundle;

public class FileElement extends BorderPane {

    private final MainCtrl mainCtrl;
    private final NoteEditCtrl noteEditCtrl;
    private final ServerUtils server;
    private final DialogUtil dialogUtil;
    private final ResourceBundle resourceBundle;
    private final Hyperlink label;

    private final FileEntity file;

    public FileElement(MainCtrl mainCtrl, NoteEditCtrl noteEditCtrl, ServerUtils server, DialogUtil dialogUtil,
                       ResourceBundle resourceBundle, FileEntity file) {
        this.mainCtrl = mainCtrl;
        this.noteEditCtrl = noteEditCtrl;
        this.server = server;
        this.dialogUtil = dialogUtil;
        this.resourceBundle = resourceBundle;
        this.file = file;

        label = new Hyperlink(file.getName());
        label.setAlignment(Pos.CENTER);
        label.setOnAction(a -> {
            label.setVisited(false);
            this.promptDownload();
        });
        label.setPadding(new Insets(0, 5, 0, 0));
        label.setMaxWidth(200);

        Button deleteButton = this.createButton("appIcon/delete_icon.png");
        deleteButton.setOnAction(a -> this.promptDelete());

        Button changeName = this.createButton("appIcon/changeName_icon.png");
        changeName.setOnAction(a -> this.updateFileTitle());

        Button renderFile = this.createButton("appIcon/render_icon.png");
        renderFile.setOnAction(a -> {
            noteEditCtrl.renderFile(file, noteEditCtrl.getCurrentNote());
        });

        HBox buttonBox = new HBox(10);
        buttonBox.getChildren().addAll(renderFile, changeName, deleteButton);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        this.setCenter(label);
        this.setRight(buttonBox);
    }

    private Button createButton(String s) {
        Image addedImage = new Image(s);
        ImageView addedIcon = new ImageView(addedImage);
        addedIcon.setPreserveRatio(true);
        addedIcon.setFitHeight(10);

        Button addedButton = new Button();
        addedButton.setAlignment(Pos.CENTER);
        addedButton.setMaxHeight(10);
        addedButton.setGraphic(addedIcon);
        return addedButton;
    }

    private void updateFileTitle() {
        Note currentNote = noteEditCtrl.getCurrentNote();
        Alert alert = new Alert(AlertType.CONFIRMATION);

        alert.setTitle(resourceBundle.getString("popup.filename.title"));
        alert.setHeaderText(resourceBundle.getString("popup.filename.headerText"));
        alert.getDialogPane().getScene().getWindow().setWidth(400);
        alert.getDialogPane().getScene().getWindow().setHeight(250);

        TextField textField = new TextField();
        textField.setPromptText(resourceBundle.getString("popup.filename.promptText"));
        VBox content = new VBox();
        content.setSpacing(10);
        content.getChildren().add(textField);

        alert.getDialogPane().setContent(content);
        Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
        alertStage.getIcons().add(new Image("appIcon/NoteIcon.jpg"));

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK && !textField.getText().isEmpty()) {
                try {
                    String fileName = file.getName();
                    int lastDotIndex = fileName.lastIndexOf('.');
                    if (lastDotIndex > 0) { // Ensure there's a dot and it's not the first character
                        fileName = textField.getText() + fileName.substring(lastDotIndex);
                    }
                    String oldName = file.getName();
                    file.setName(fileName);
                    label.setText(fileName);

                    //System.out.println(file.getNote() + " " + file.getId() + " " + file.getName());

                    server.updateFileName(file);
                    noteEditCtrl.replaceRender(file, currentNote, oldName);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("Error: " + e.getMessage());
                }
            } else if (response == ButtonType.CANCEL) {
                alert.close();
            }
        });
    }

    private void promptDownload() {
        File saveAt = mainCtrl.promptFileSave(file.getName());
        if (saveAt == null) {
            // User cancelled operation
            return;
        }
        this.downloadTo(saveAt);
    }

    private void downloadTo(File saveAt) {
        System.out.println("Downloading file " + file.getName() + " (" + file.getId() + ") to "
                + saveAt.getAbsolutePath());

        try (FileOutputStream out = new FileOutputStream(saveAt);
             ByteArrayInputStream in = new ByteArrayInputStream(file.getData())) {
            in.transferTo(out);

            dialogUtil.showDialog(this.resourceBundle, AlertType.INFORMATION,
                    "popup.files.downloaded");
        } catch (IOException ex) {
            System.err.println("Failed to save note file to disk");
            ex.printStackTrace();

            dialogUtil.showDialog(this.resourceBundle, AlertType.ERROR,
                    "popup.files.downloadFailed");
        }
    }

    private void promptDelete() {
        Optional<ButtonType> response = dialogUtil.showDialog(this.resourceBundle, AlertType.CONFIRMATION,
                "popup.files.confirmDelete");
        if (response.isPresent() && response.get() == ButtonType.OK) {
            this.delete();
        }
    }

    private void delete() {
        server.deleteFile(file);
        Note currentNote = noteEditCtrl.getCurrentNote();
        noteEditCtrl.unRenderFile(file, currentNote);
        noteEditCtrl.refreshFilesPane(currentNote);
        dialogUtil.showDialog(this.resourceBundle, AlertType.CONFIRMATION,
                "popup.files.deleted");
    }

}

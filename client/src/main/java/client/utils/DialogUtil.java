package client.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.ResourceBundle;

public class DialogUtil {

    public Optional<ButtonType> showDialog(ResourceBundle resourceBundle, AlertType alertType,
                                           String translationKey, ButtonType... buttonTypes) {
        return this.showDialog(resourceBundle, alertType, translationKey, Collections.emptyMap(), buttonTypes);
    }

    public Optional<ButtonType> showDialog(ResourceBundle resourceBundle, AlertType alertType,
                                           String translationKey, Map<String, String> placeholders,
                                           ButtonType... buttonTypes) {
        Alert alert = new Alert(
                alertType,
                this.replace(resourceBundle.getString(translationKey + ".title"), placeholders),
                buttonTypes
        );
        alert.setContentText(this.replace(resourceBundle.getString(translationKey + ".text"), placeholders));

        Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
        alertStage.getIcons().add(new Image("appIcon/NoteIcon.jpg"));

        return alert.showAndWait();
    }

    private String replace(String str, Map<String, String> placeholders) {
        for (Entry<String, String> entry : placeholders.entrySet()) {
            str = str.replace(entry.getKey(), entry.getValue());
        }
        return str;
    }

}

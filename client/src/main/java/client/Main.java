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
package client;

import static com.google.inject.Guice.createInjector;

import atlantafx.base.theme.PrimerDark;
import client.scenes.CollectionEditCtrl;
import client.scenes.NoteEditCtrl;
import com.google.inject.Injector;

import client.scenes.MainCtrl;
import client.utils.ServerUtils;
import commons.Note;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.scene.Parent;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.util.Locale;
import java.util.Objects;

public class Main extends Application {

    private static final Injector INJECTOR = createInjector(new MyModule());
    private static final MyFXML FXML = new MyFXML(INJECTOR);

    private static final Locale DEFAULT_LOCALE = Locale.ENGLISH;

    private NoteEditCtrl noteEditCtrl;
    private MainCtrl mainCtrl;

    private Locale locale = DEFAULT_LOCALE;

    private ConfigManager configManager;

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage primaryStage) {
        ServerUtils serverUtils = INJECTOR.getInstance(ServerUtils.class);
        if (!serverUtils.isServerAvailable()) {
            System.err.println("Server needs to be started before the client, but it does not seem to be available. Shutting down.");
            return;
        }

        Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());

        this.configManager = INJECTOR.getInstance(ConfigManager.class);
        this.locale = configManager.loadLanguage();
        Locale.setDefault(this.locale);

        this.mainCtrl = INJECTOR.getInstance(MainCtrl.class);
        mainCtrl.initialize(primaryStage);
        this.loadScenes();

        primaryStage.setOnCloseRequest(r -> {
            if (noteEditCtrl != null) {
                noteEditCtrl.saveChanges();
                System.out.println("Changes were saved on exit.");
            } else {
                System.err.println("Changes were not saved on exit.");
            }
        });
    }

    public void loadScenes() {
        Pair<NoteEditCtrl, Parent> editView = FXML.load(this.locale, "client", "scenes", "NoteEditView.fxml");
        Pair<CollectionEditCtrl, Parent> collectionView = FXML.load(this.locale, "client", "scenes", "CollectionEditView.fxml");

        editView.getValue().getStylesheets()
                .add(Objects.requireNonNull(getClass().getResource("/css/custom-overrides-noteedit.css")).toExternalForm());
        collectionView.getValue().getStylesheets()
                .add(Objects.requireNonNull(getClass().getResource("/css/custom-overrides-collectionedit.css")).toExternalForm());

        this.noteEditCtrl = editView.getKey();
        noteEditCtrl.setLanguage(this.locale);
        noteEditCtrl.getSelectedLanguage().addListener(this.localeChangeListener);

        mainCtrl.loadScenes(editView, collectionView);
    }

    private final ChangeListener<Locale> localeChangeListener = (observable, locale1, locale2) -> this.handleLocaleChange();

    private void handleLocaleChange() {
        Note currentNote = noteEditCtrl.getCurrentNote();
        noteEditCtrl.saveChanges();
        noteEditCtrl.getSelectedLanguage().removeListener(this.localeChangeListener);

        this.locale = noteEditCtrl.getSelectedLanguage().get();
        Locale.setDefault(this.locale); // Default locale is used to translate e.g. dialogs by JavaFX

        System.out.println("Reloading scenes with new locale: " + locale);
        this.loadScenes();
        noteEditCtrl.setCurrentNote(currentNote); // Reselect the note we had open before reloading the scenes
    }

}
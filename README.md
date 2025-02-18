# NetNote

##Starting template
This README contains information about the description of the project, the team that contributed to the project and instructions on how to initialise and run the app on your local device. If you encounter any issues or have any questions, do not hesitate to contact me at: urumovstepan@gmail.com. Thank you and I hope you will enjoy the app!

##Description of project
NetNote is a project developed during the course CSE1105 - Collaborative Software Engineering Project offered by Technical Univeristy of Delft by class of 2027 composed by the developers presented in the next section.

##Group Members
Group members include: Stepan Urumov, Sergiu Tiron, Victor Bădescu, Luca Bledea Floruţa and Melle Moerkerk

##How to run
To run the template project from the command line, you either need to have [Maven](https://maven.apache.org/install.html) installed on your local system (`mvn`) or you need to use the Maven wrapper (`mvnw`). You can then execute

	mvn clean install

to package and install the artifacts for the three subprojects. Afterwards, you can run ...

	cd server
	mvn spring-boot:run

to start the server or ...

	cd client
	mvn javafx:run

to run the client. Please note that the server needs to be running, before you can start the client.

Once this is working, you can try importing the project into your favorite IDE.

## General guidance

#### Adding files

- Files can be added from the 'add' button inside a note.
- If no note is selected when adding a file, the user is informed.
- Adding the same file twice in the same note is not allowed (the user is notified). If the user wishes to render the embedded file again, there is a button next to the filename for that.
- Changing the filename is done using the button generated next to each file added. The same goes for file deletion and file rendering.
- To download the files locally you need to press the hyperlink of the file.

#### Note handling

- Notes can be added via the 'Add Note' button.
- Note titles can be changed by either changing it in the text field for the title or by double-clicking the note title in the list (Names are unique inside a collection and blank names are not allowed).
- Notes can be deleted if they are selected by pressing the 'Delete' button. A confirmation for deletion appears. If no note is selected, the user sees the prompt text updated in the rendering.
- Notes can also be deleted from the 'Edit' menu at the top bar of the app.
- Notes are saved whenever the user changes between them or exits the app.

#### Note saving

- Notes can either be saved by pressing the 'Save Changes' button, or they are saved automatically after a number of pressed keystrokes.
- The predefined number of keystrokes is set to 100 and can be changed from the 'Settings' menu of the app.
- The user is not allowed to enter a number of keystrokes that is not in the range of 5 to 10000, and if they do otherwise, they are notified that the action is invalid.

#### Note searching

- Notes can be searched through the searchbar above the search.
- Notes are searched based on the current collection that is selected or in all collections if 'All' is selected.
- The search is done in both the note content and the note title.
- There is a search icon that focuses the search bar when pressed.

#### Languages

- The app supports 5 languages: English, Romanian, Dutch, Italian, and Bulgarian, each one having a flag icon in the dropdown.
- Everything gets translated besides the name of the app and the titles of the notes and collections since those are user-handled.
- The selected language is set as the preferred one and is saved in a local config file so it is persistent across app restarts.

#### Collections

- Collections are listed in a dropdown box on the left side of the app.
- They are also stored in a local config file that auto-generates an initial
  collection for each user if no collections exist (Set as default).
- To edit collections, press 'Edit Collections' inside the dropdown box.
- Collection names can be changed either by double-clicking their name in the list or changing their name in the 'Title' text field (Names are unique and blank names are not allowed).
- The server of a collection can be changed, and the user is informed through a label if the server is available or the path is valid (Although collections are not sent to that server as multiple servers do not need to be supported anymore).
- Collections can be deleted if selected, and a confirmation appears. When collections are deleted, all the notes inside them get deleted as well.
- Collections can be added via the 'Add Collection' button.
- There is a button for making the selected collection the default, called 'Make Default'.
- The default collection can be accessed via the 'Default Collection' button inside the dropdown box.
- There is also an "All" button inside the dropdown box which shows the notes across all collections.

#### Note moving

- Notes can be moved from one collection to another through a dropbox next to their title.
- If the user tries to move a note to the same collection as the one it currently resides in, the user is notified through a warning.
- If the user tries to move a note to a collection where there is a note with the same title, it also gets a warning.

#### For multiple clients

- If changes are made inside a client, the others should refresh to get the updates as the app currently does not support WebSockets.

## KeyBinds

The following keyboard shortcuts are added to the application:

- **CTRL + ENTER** : Enter the editing area of a note
- **CTRL + N** : User creates a new note
- **CTRL + D** : User deletes the selected note
- **CTRL + S** : User saves the selected note that they are editing
- **CTRL + R** : User refreshes
- **CTRL + L** : Show languages dropdown
- **CTRL + 1** : Access menu settings
- **CTRL + 2** : Access note deletion menu
- **CTRL + ALT + N** : Open the edit collection scene
- **CTRL + ALT + A** : Add file to current note
- **ALT + RIGHT** : Move to next collection
- **ALT + LEFT** : Set 'All' collections
- **ALT + UP** : Move to the previous note in the list
- **ALT + DOWN** : Move to the next note in the list
- **ALT + D** : Set 'Default collection'
- **ESCAPE** : Focus search bar

---

### Attribution:
Flag images from Twemoji 15.0.3, retrieved via [Emojipedia](https://emojipedia.org).  
Button icons from Icons8, retrieved via [Icons8](https://icons8.com/icons/).  
App icon from Creative Fabrica, retrieved via [CreativeFabrica](https://www.creativefabrica.com/nl/product/note-icon/).

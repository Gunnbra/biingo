package gunn.biingo;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.lang3.RandomStringUtils;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class ModuleCardEditor {
    private  File projectDirectory;
    private  Stage primaryStage;
    private FlowPane previewFlowPane = null;

    public ModuleCardEditor(File projDir, Stage stage) {
        projectDirectory = projDir;
        primaryStage = stage;
    }

    public void tabCardEditor(Tab tab) {
        // Scene Creations
        BorderPane mainLayout = new BorderPane();

        // TOP Panel
        VBox topPanel = new VBox();
        // Drop Files Box
        VBox dropVBox = new VBox();
        dropVBox.setStyle("-fx-border-style: dashed inside; -fx-border-width: 2; -fx-border-insets: 5; -fx-border-radius: 5; -fx-border-color: gray;");
        dropVBox.setMinHeight(100);
        dropVBox.setAlignment(Pos.CENTER);
        dropVBox.setSpacing(10);
        // AddFiles Button
        Button buttonGetFiles = new Button("Choose Files");
        buttonGetFiles.setMinWidth(200);
        buttonGetFiles.setMinHeight(30);
        dropVBox.getChildren().add(buttonGetFiles);
        // AddFiles Text
        Text dropText = new Text("Or drop PNGs here");
        dropVBox.getChildren().add(dropText);
        // Set to layout
        topPanel.getChildren().add(dropVBox);
        mainLayout.setTop(topPanel);

        // Scroll pane to hold icon previews
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToHeight(true);
        scrollPane.setFitToWidth(true);
        // Flow pane, goes inside Scroll pane
        FlowPane previewFlowPane = new FlowPane();
        previewFlowPane.setPadding(new Insets(40, 20, 20, 40));
        previewFlowPane.setVgap(25);
        previewFlowPane.setHgap(25);
        // Set to layout
        scrollPane.setContent(previewFlowPane);
        mainLayout.setCenter(scrollPane);

        // Render Preview Icons
        this.previewFlowPane = previewFlowPane;
        rerenderPreviews();

        tab.setContent(mainLayout);

        // --------------- LISTENERS -----------------------------------

        // DropBox DragOver Event
        dropVBox.setOnDragOver(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent event) {
                Dragboard db = event.getDragboard();
                if (db.hasFiles()) {
                    event.acceptTransferModes(TransferMode.COPY);
                } else {
                    event.consume();
                }
            }
        });

        // DropBox OnDragOver Event
        dropVBox.setOnDragDropped(onDropFileHandler);

        // GetFiles Button OnAction
        buttonGetFiles.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG files (*.png)", "*.png"));
                Object[] files = fileChooser.showOpenMultipleDialog(primaryStage).toArray();

                for (int i = 0; i < files.length; i++) {
                    try {
                        String name = String.format("%s.%s", RandomStringUtils.randomAlphanumeric(8), "png");
                        File tempFile = new File(projectDirectory + "/icons/temp" + name);
                        Files.copy(new File(files[i].toString()).toPath(), tempFile.toPath());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                rerenderPreviews();
            }
        });
    }

    /**
     * Renders icons in Card editor
     */
    public void rerenderPreviews() {
        File iconDir = new File(projectDirectory + "/" + "icons");
        if (iconDir.exists()) {
            String[] fileList = iconDir.list();

            this.previewFlowPane.getChildren().clear(); // Removes all so it can be re rendered
            if (fileList != null) {
                for (int i = 0; i < fileList.length; i++) {
                    final File currentFile = new File(iconDir.getAbsolutePath() + "/" + fileList[i]);

                    HBox hBox = new HBox();
                    hBox.setStyle("-fx-border-style: solid inside; -fx-border-width: 1; -fx-border-insets: 5; -fx-border-radius: 5; -fx-border-color: black;");
                    hBox.setPadding(new Insets(5, 5, 5, 5));
                    hBox.setSpacing(5);
                    hBox.setAlignment(Pos.CENTER);

                    Image image = new Image("file:" + iconDir.getAbsolutePath() + "/" + fileList[i]);
                    ImageView imageView = new ImageView(image);
                    imageView.setFitHeight(100);
                    imageView.setFitWidth(100);
                    hBox.getChildren().add(imageView);

                    ComboBox comboBox = new ComboBox(iconOptions());

                    if (fileList[i].length() == 6) { // That being '00.png' = 6
                        try {
                            comboBox.getSelectionModel().select(fileList[i].substring(0, 2));
                        } catch (Exception e) {
                            Main.warningPopup("TWO DIGIT LONG FILE NAME IS NOT VIABLE (1-75): " + fileList[i].substring(0, 2) + " - rerenderPreview()");
                        }
                    }

                    hBox.getChildren().add(comboBox);
                    this.previewFlowPane.getChildren().add(hBox);


                    comboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                        if (newValue.toString() == " ") {
                            newValue = RandomStringUtils.randomAlphanumeric(8);
                        } else if (newValue.toString().length() == 1) { // Keep length to 2 digits
                            newValue = 0 + newValue.toString();
                        }

                        currentFile.renameTo(new File(iconDir.getAbsolutePath() + "/" + newValue + ".png"));
                        rerenderPreviews();
                    });
                }
            }
        } else {
            Main.warningPopup("ICON DIRECTORY DOESNT EXIST - rerenderPreview()");
        }
    }

    /**
     * Event Listener: On File Drop
     */
    EventHandler<DragEvent> onDropFileHandler = new EventHandler<DragEvent>() {
        @Override
        public void handle(DragEvent event) {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasFiles()) {
                success = true;
                File[] files = db.getFiles().toArray(new File[0]);
                for (int i = 0; i < files.length; i++) { // Checks if all files end with .png, if one doesn't, failure
                    String fileExt = files[i].getName();
                    if (!fileExt.contains(".png")) {
                        success = false;
                        break;
                    }
                }
                if (success) {
                    for (int i = 0; i < files.length; i++) {
                        try {
                            String name = String.format("%s.%s", RandomStringUtils.randomAlphanumeric(8), "png");
                            File tempFile = new File(projectDirectory + "/icons/temp" + name);
                            Files.copy(files[i].toPath(), tempFile.toPath());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    rerenderPreviews();
                }
            }
            event.setDropCompleted(success);
            event.consume();
        }
    };

    /**
     * Generates available icon numbers
     */
    public ObservableList<String> iconOptions() {
        ObservableList<String> options = FXCollections.observableArrayList(
                " "
        );

        for (int i = 0; i < 76; i++) {
            String currentNum = Integer.toString(i);

            if (i == 0) {
                currentNum = "Unlink";
            } else if (i < 10) {
                currentNum = "0" + Integer.toString(i);
            }

            File currentFile = new File(projectDirectory + "/icons/" + currentNum + ".png");

            if (!currentFile.exists()) {
                options.add(currentNum);
            }
        }

        return options;
    }
}

package gunn.biingo;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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

public class ModuleBookletEditor {
    private ModuleGenerate moduleGenerate;
    private File projectDirectory;
    private Stage primaryStage;

    private VBox warnBox;
    private FlowPane previewTemplateFlowPane = null;

    public ModuleBookletEditor(File projDir, Stage stage, ModuleGenerate modGen){
        projectDirectory = projDir;
        primaryStage = stage;
        moduleGenerate = modGen;

        warnBox = new VBox();
    }

    public void tabBookletEditor(Tab tab) {
        // Scene Creations
        BorderPane mainLayout = new BorderPane();

        VBox topPane = new VBox();
        topPane.setPadding(new Insets(5, 2, 2, 2));
        //Pages per Booklet Pane
        HBox pagesPane = new HBox();
        pagesPane.setAlignment(Pos.CENTER);
        pagesPane.setSpacing(10);
        pagesPane.setPadding(new Insets(2, 10, 2, 10));
        // Generate and create numbers for # of cards to generate
        ObservableList<String> genNumbers = FXCollections.observableArrayList(" ");
        for (int i = 1; i <= 25; i++) {
            genNumbers.add(Integer.toString(i));
        }
        Text comboText = new Text("Games per Booklet:");
        ComboBox comboNumber = new ComboBox(genNumbers);
        comboNumber.getSelectionModel().select(moduleGenerate.getGamesPerBooklet());
        pagesPane.getChildren().add(comboText);
        pagesPane.getChildren().add(comboNumber);
        topPane.getChildren().add(pagesPane);

        // Drop Files Box
        VBox dropVBox = new VBox();
        dropVBox.setStyle("-fx-border-style: dashed inside; -fx-border-width: 2; -fx-border-insets: 5; -fx-border-radius: 5; -fx-border-color: gray;");
        dropVBox.setMinHeight(100);
        dropVBox.setAlignment(Pos.CENTER);
        dropVBox.setSpacing(10);
        // AddFiles Button
        Button buttonGetFiles = new Button("Choose Templates");
        buttonGetFiles.setMinWidth(200);
        buttonGetFiles.setMinHeight(30);
        dropVBox.getChildren().add(buttonGetFiles);
        // AddFiles Text
        Text dropText = new Text("Or drop PNGs here");
        dropVBox.getChildren().add(dropText);
        //Warning Text
        warnBox.setAlignment(Pos.CENTER);
        Text warnText = new Text("Set a template to 01!");
        warnText.setStyle("-fx-fill: red; -fx-font-size: 20px; -fx-font-weight: bold");
        warnBox.getChildren().add(warnText);
        // Set Elements to top pane
        topPane.getChildren().add(dropVBox);
        topPane.getChildren().add(warnBox);

        // SET TOP
        mainLayout.setTop(topPane);

        // Center Content
        // Scroll pane to hold template previews
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
        this.previewTemplateFlowPane = previewFlowPane;
        rerenderTemplatePreviews();

        tab.setContent(mainLayout);

        // Listeners ----------------------------------------------------
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
        dropVBox.setOnDragDropped(onDropTemplateHandler);

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
                        File tempFile = new File(projectDirectory + "/templates/temp" + name);
                        Files.copy(new File(files[i].toString()).toPath(), tempFile.toPath());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                rerenderTemplatePreviews();
            }
        });

        //ComboNumbers onChanges
        comboNumber.valueProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                moduleGenerate.setGamesPerBooklet(Integer.parseInt(newValue.toString()));
                rerenderTemplatePreviews();
                moduleGenerate.saveToProperties();
            }
        });
    }

    /**
     * Renders Templates in Card editor
     */
    public void rerenderTemplatePreviews() {
        boolean redo = false;

        File templateDir = new File(projectDirectory + "/" + "templates");
        if (templateDir.exists()) {
            String[] fileList = templateDir.list();

            this.previewTemplateFlowPane.getChildren().clear(); // Removes all so it can be re rendered
            if (fileList != null) {

                final File defaultTemplate = new File(templateDir.getAbsolutePath() + "/01.png");
                if (defaultTemplate.exists()) {
                    moduleGenerate.setButtonGenerateVisible(false);
                    warnBox.setVisible(false);
                } else {
                    moduleGenerate.setButtonGenerateVisible(true);
                    warnBox.setVisible(true);
                }

                for (int i = 0; i < fileList.length; i++) {
                    final File currentFile = new File(templateDir.getAbsolutePath() + "/" + fileList[i]);
                    String numName = currentFile.getName().substring(0, currentFile.getName().length() - 4);

                    if (numName.length() == 2) {
                        if (Integer.parseInt(numName) > moduleGenerate.getGamesPerBooklet()) {
                            String newValue = RandomStringUtils.randomAlphanumeric(8);
                            currentFile.renameTo(new File(templateDir.getAbsolutePath() + "/" + newValue + ".png"));
                            redo = true;
                        }
                    }

                    HBox hBox = new HBox();
                    hBox.setStyle("-fx-border-style: solid inside; -fx-border-width: 1; -fx-border-insets: 5; -fx-border-radius: 5; -fx-border-color: black;");
                    hBox.setPadding(new Insets(5, 5, 5, 5));
                    hBox.setSpacing(5);
                    hBox.setAlignment(Pos.CENTER);

                    Image image = new Image("file:" + templateDir.getAbsolutePath() + "/" + fileList[i]);
                    ImageView imageView = new ImageView(image);
                    imageView.setFitHeight(100);
                    imageView.setFitWidth(100);
                    hBox.getChildren().add(imageView);

                    ComboBox comboBox = new ComboBox(templateOptions());

                    if (fileList[i].length() == 6) { // That being '00.png' = 6
                        try {
                            comboBox.getSelectionModel().select(fileList[i].substring(0, 2));
                        } catch (Exception e) {
                            Main.warningPopup("TWO DIGIT LONG FILE NAME IS NOT VIABLE (1-99): " + fileList[i].substring(0, 2) + " - rerenderTemplatePreview()");
                        }
                    }

                    hBox.getChildren().add(comboBox);
                    this.previewTemplateFlowPane.getChildren().add(hBox);


                    comboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                        if (newValue.toString() == " ") {
                            newValue = RandomStringUtils.randomAlphanumeric(8);
                        } else if (newValue.toString().length() == 1) { // Keep length to 2 digits
                            newValue = 0 + newValue.toString();
                        }

                        currentFile.renameTo(new File(templateDir.getAbsolutePath() + "/" + newValue + ".png"));
                        rerenderTemplatePreviews();
                    });
                }
            }
        } else {
            Main.warningPopup("TEMPLATE DIRECTORY DOESNT EXIST - rerenderTemplatePreview()");
        }

        if (redo) {
            rerenderTemplatePreviews();
        }
    }

    /**
     * Event Listener: On File Drop
     */
    EventHandler<DragEvent> onDropTemplateHandler = new EventHandler<DragEvent>() {
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
                            File tempFile = new File(projectDirectory + "/templates/temp" + name);
                            Files.copy(files[i].toPath(), tempFile.toPath());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    rerenderTemplatePreviews();
                }
            }
            event.setDropCompleted(success);
            event.consume();
        }
    };

    /**
     * Generates available template numbers
     */
    public ObservableList<String> templateOptions() {
        ObservableList<String> options = FXCollections.observableArrayList(
                " "
        );

        for (int i = 0; i < moduleGenerate.getGamesPerBooklet() + 1; i++) {
            String currentNum = Integer.toString(i);

            if (i == 0) {
                currentNum = "Unlink";
            } else if (i < 10) {
                currentNum = "0" + Integer.toString(i);
            }

            File currentFile = new File(projectDirectory + "/templates/" + currentNum + ".png");

            if (!currentFile.exists()) {
                options.add(currentNum);
            }
        }

        return options;
    }
}

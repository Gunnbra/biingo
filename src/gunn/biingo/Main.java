package gunn.biingo;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Random;

public class Main extends Application {
    Stage primaryStage = null;
    File projectDirectory = null;
    boolean allNumbers = true;
    FlowPane previewFlowPane = null;
    FlowPane previewTemplateFlowPane = null;

    public static void main(String[] args) throws Exception {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        primaryStage.setTitle("BINGO Card Generator");
        this.primaryStage = primaryStage;

        windowMainMenu();
    }

    /**
     * Scene: Main Menu
     */
    public void windowMainMenu() {
        // Create 'Load' and 'New' Buttons
        Button buttonLoad = new Button("Load");
        buttonLoad.setMinWidth(100);
        Button buttonNew = new Button("New");
        buttonNew.setMinWidth(100);

        // Create BorderPane layout and put buttons in an HBox at the bottom
        BorderPane borderLayout = new BorderPane();

        Image image = new Image(getClass().getResource("/assets/logo.png").toExternalForm());
        ImageView imageView = new ImageView(image);
        imageView.setFitHeight(200);
        imageView.setFitWidth(200);

        borderLayout.setCenter(imageView);



        VBox bottomBox = new VBox();
        //Buttons for Bottom
        HBox menuHBox = new HBox();
        menuHBox.setAlignment(Pos.BOTTOM_CENTER);
        menuHBox.getChildren().add(buttonLoad);
        menuHBox.getChildren().add(buttonNew);
        menuHBox.setPadding(new Insets(15, 12, 15, 12));
        menuHBox.setSpacing(10);
        bottomBox.getChildren().add(menuHBox);
        //Copyrights
        HBox copyBox = new HBox();
        copyBox.setPadding(new Insets(5,5,5,5));
        copyBox.setAlignment(Pos.CENTER_RIGHT);
        Text copy = new Text("V1.0.2 - Copyright Brady Gunn 2020. All rights reserved");
        copy.setTextAlignment(TextAlignment.RIGHT);
        copyBox.getChildren().add(copy);
        bottomBox.getChildren().add(copyBox);
        //Set to Bottom
        borderLayout.setBottom(bottomBox);

        // Set scene
        Scene mainMenuScene = new Scene(borderLayout, 500, 300);

        // Set Scene
        primaryStage.setScene(mainMenuScene);
        primaryStage.show();

        buttonLoad.setOnAction(value -> {
            DirectoryChooser dirChooser = new DirectoryChooser();

            //Show save file dialog
            File file = dirChooser.showDialog(primaryStage);

            if (file != null) {
                loadProjectDirectory(file);
            }
        });
        buttonNew.setOnAction(value -> {
            FileChooser fileChooser = new FileChooser();

            //Set extension filter for text files
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("BINGO files (*.bingo)", "*.bingo");
            fileChooser.getExtensionFilters().add(extFilter);

            //Show save file dialog
            File file = fileChooser.showSaveDialog(primaryStage);

            if (file != null) {
                createProjectDirectory(file);
            }
        });
    }

    /**
     * Creates file structure for project
     */
    public void createProjectDirectory(File file){
        // Created Project Data Directory
        String parentPath = file.getParent();
        String nameProject = file.getName().substring(0, file.getName().length() - 6);

        // Base Data Directory
        File projectDir = new File(parentPath + "/" + nameProject + ".bingo");
        projectDir.mkdir();

        // Icon Directory
        File iconDir = new File(projectDir + "/" + "icons");
        iconDir.mkdir();

        File templateDir = new File(projectDir + "/" + "templates");
        templateDir.mkdir();

        // Project File
        file.mkdir();

        if(projectDir.exists()){
            projectDirectory = projectDir;
            windowProject();
        } else {
            warningPopup("PROJECT DIRECTORY DOESNT EXIST - createProjectDirectory()");
        }
    }

    /**
     * Load file structure for project
     */
    public void loadProjectDirectory(File file){
        boolean success = true;

        if(file.exists()) {
            projectDirectory = file;
        } else {
            success = false;
        }

        if(!new File(projectDirectory + "/icons").exists()){
            success = false;
        }

        if(success){
            windowProject();
        }else{
            warningPopup("PROJECT DIRECTORY IS INVALID - loadProjectDirectory()");
        }
    }

    /**
     * Scene: Card editor
     */
    public void windowProject() {
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        Tab tabNumEditor = new Tab("Card Editor");
        Tab tabBookletEditor = new Tab("Booklet Editor");
        Tab tabGeneration = new Tab("Generate Cards");
        Tab tabPlay = new Tab("Play Tracker");

        tabPane.getTabs().add(tabNumEditor);
        tabPane.getTabs().add(tabBookletEditor);
        tabPane.getTabs().add(tabGeneration);
        tabPane.getTabs().add(tabPlay);

        tabCardEditor(tabNumEditor);
        tabBookletEditor(tabBookletEditor);
        tabGenerate(tabGeneration);
        tabPlayGame(tabPlay);

        // Setting Scene
        Scene bingoScene = new Scene(tabPane, 750, 950);
        primaryStage.setScene(bingoScene);
        primaryStage.show();
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
        for(int i = 1; i < 100; i++){
            genNumbers.add("x" + Integer.toString(i));
        }
        Text comboText = new Text("Games per Booklet:");
        ComboBox comboNumber = new ComboBox(genNumbers);
        comboNumber.getSelectionModel().select("x1");
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
        Text dropText = new Text("Or drop PDFs here");
        dropVBox.getChildren().add(dropText);
        topPane.getChildren().add(dropVBox);

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

    //OLDDDD-------------------------------------
        /*


        // Bottom panel
        VBox bottomPane = new VBox();
        bottomPane.setAlignment(Pos.CENTER);
        // Template Panel
        HBox templatePane = new HBox();
        templatePane.setAlignment(Pos.CENTER);
        templatePane.setSpacing(10);
        templatePane.setPadding(new Insets(10, 10, 10, 10));
        Button buttonSetTemp = new Button("No Template Specified");
        Button buttonRemTemp = new Button("X");
        templatePane.getChildren().add(buttonSetTemp);
        bottomPane.getChildren().add(templatePane);
        // Generate Panel

        bottomPane.getChildren().add(pagesPane);

        HBox bookPane = new HBox();
        bookPane.setAlignment(Pos.CENTER);
        bookPane.setSpacing(10);
        bookPane.setPadding(new Insets(2, 10, 2, 10));
        Text bookText = new Text("Booklets per PDF:");
        ComboBox comboBook = new ComboBox(genNumbers);
        comboBook.getSelectionModel().select("x1");
        bookPane.getChildren().add(bookText);
        bookPane.getChildren().add(comboBook);
        bottomPane.getChildren().add(bookPane);

        Button buttonGen = new Button("Generate");
        buttonGen.setDisable(true);
        bottomPane.getChildren().add(buttonGen);

       mainLayout.setBottom(bottomPane);


        // If there is a template on load
        if(isThereATemplate) {
            buttonSetTemp.setDisable(true);
            buttonSetTemp.setText("Template Set");
            templatePane.getChildren().add(buttonRemTemp);

            buttonGen.setDisable(false);
        }

         // Generate Button OnAction
        buttonGen.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                try {
                    generatePDF(Integer.parseInt(comboNumber.getValue().toString().substring(1)), Integer.parseInt(comboBook.getValue().toString().substring(1)));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        // GetTemplate Button OnAction
        buttonSetTemp.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF files (*.pdf)", "*.pdf"));
                File file = fileChooser.showOpenDialog(primaryStage);

                if(file != null){
                    templateCard = file;
                    buttonSetTemp.setDisable(true);
                    buttonSetTemp.setText("Template Set");
                    templatePane.getChildren().add(buttonRemTemp);

                    buttonGen.setDisable(false);

                    File tempCardFile = new File(projectDirectory + "/" + "template.pdf");
                    if(tempCardFile.exists()){ // If template already exists, rename it randomly
                        String name = String.format("%s.%s", RandomStringUtils.randomAlphanumeric(8), "pdf");
                        try {
                            Files.copy(tempCardFile.toPath(), new File(projectDirectory + "/" + name).toPath());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        // Remove template file
                        tempCardFile.delete();
                    }

                    tempCardFile = new File(projectDirectory + "/" + "template.pdf");
                    try {
                        Files.copy(file.toPath(), tempCardFile.toPath());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                } else {
                    buttonGen.setDisable(true);
                }
            }
        });

        buttonRemTemp.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                templatePane.getChildren().remove(buttonRemTemp);
                buttonSetTemp.setDisable(false);
                buttonSetTemp.setText("Template Not Specified");
                buttonGen.setDisable(true);
            }
        });

        */

    }

    public void tabGenerate(Tab tab) {
        // Scene Creations
        BorderPane mainLayout = new BorderPane();
        VBox vbox = new VBox();
        vbox.setAlignment(Pos.CENTER);
        vbox.setSpacing(10);
        vbox.setPadding(new Insets(2, 10, 2, 10));

        //  # of Booklets combo box
        HBox comboBox = new HBox();
        comboBox.setAlignment(Pos.CENTER);
        comboBox.setSpacing(10);
        comboBox.setPadding(new Insets(2, 10, 2, 10));
        // Generate 100 numbers
        ObservableList<String> genNumbers = FXCollections.observableArrayList(" ");
        for(int i = 1; i < 100; i++){
            genNumbers.add("x" + Integer.toString(i));
        }
        Text comboText = new Text("Booklets Printed:");
        ComboBox comboNumber = new ComboBox(genNumbers);
        comboNumber.getSelectionModel().select("x1");
        comboBox.getChildren().add(comboText);
        comboBox.getChildren().add(comboNumber);

        //Checkboxes
        HBox checkBox = new HBox();
        checkBox.setAlignment(Pos.CENTER);
        checkBox.setSpacing(10);
        checkBox.setPadding(new Insets(2, 10, 2, 10));
        // Elements
        CheckBox checkPageNum = new CheckBox("Page Numbers");
        checkBox.getChildren().add(checkPageNum);

        // Generate button
        Button buttonGenerate = new Button("Generate");
        // Add all elements to main layout and send to Tab
        vbox.getChildren().add(comboBox);
        vbox.getChildren().add(checkBox);
        vbox.getChildren().add(buttonGenerate);
        mainLayout.setCenter(vbox);
        tab.setContent(mainLayout);
    }

    public void tabPlayGame(Tab tab) {
        // Scene Creations
        BorderPane mainLayout = new BorderPane();

        // CENTER
        VBox callBox = new VBox();
        callBox.setAlignment(Pos.CENTER);
        callBox.setSpacing(10);
        callBox.setPadding(new Insets(0, 10, 0, 10));
        // B I N G O
        HBox bingoBox = new HBox();
        bingoBox.setAlignment(Pos.CENTER);
        bingoBox.setSpacing(90);
        bingoBox.setPadding(new Insets(0, 0, 0, 0));
        Text textB = new Text("B");
        textB.setStyle("-fx-font-size: 50px; -fx-font-weight: bold");
        textB.setTextAlignment(TextAlignment.CENTER);
        Text textI = new Text("I");
        textI.setStyle("-fx-font-size: 50px; -fx-font-weight: bold");
        textI.setTextAlignment(TextAlignment.CENTER);
        Text textN = new Text("N");
        textN.setStyle("-fx-font-size: 50px; -fx-font-weight: bold");
        textN.setTextAlignment(TextAlignment.CENTER);
        Text textG = new Text("G");
        textG.setStyle("-fx-font-size: 50px; -fx-font-weight: bold");
        textG.setTextAlignment(TextAlignment.CENTER);
        Text textO = new Text("O");
        textO.setStyle("-fx-font-size: 50px; -fx-font-weight: bold");
        textO.setTextAlignment(TextAlignment.CENTER);
        bingoBox.getChildren().add(textB);
        bingoBox.getChildren().add(textI);
        bingoBox.getChildren().add(textN);
        bingoBox.getChildren().add(textG);
        bingoBox.getChildren().add(textO);
        callBox.getChildren().add(bingoBox);
        // Called Numbers
        HBox calledNumbers = renderPlayTracker();
        callBox.getChildren().add(calledNumbers);
        mainLayout.setCenter(callBox);

        // BOTTOM
        HBox optionBox = new HBox();
        optionBox.setAlignment(Pos.CENTER);
        optionBox.setSpacing(10);
        optionBox.setPadding(new Insets(10, 10, 10, 10));
        // Elements
        Button buttonReset = new Button("Reset Board");
        Button buttonVerify = new Button("Verification");
        Button buttonLastCalled = new Button("Last Called");
        Button buttonViewCards = new Button("Card List");
        optionBox.getChildren().add(buttonReset);
        optionBox.getChildren().add(buttonVerify);
        optionBox.getChildren().add(buttonLastCalled);
        optionBox.getChildren().add(buttonViewCards);
        mainLayout.setBottom(optionBox);

        tab.setContent(mainLayout);
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
                    rerenderPreviews();
                }
            }
            event.setDropCompleted(success);
            event.consume();
        }
    };

    /**
     * Renders icons in Card editor
     */
    public void rerenderPreviews() {
        File iconDir = new File(projectDirectory + "/" + "icons");
        if(iconDir.exists()) {
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

                    ComboBox comboBox = new ComboBox(options());

                    if (fileList[i].length() == 6) { // That being '00.png' = 6
                        try {
                            comboBox.getSelectionModel().select(fileList[i].substring(0, 2));
                        } catch (Exception e) {
                            warningPopup("TWO DIGIT LONG FILE NAME IS NOT VIABLE (1-75): " + fileList[i].substring(0,2) + " - rerenderPreview()");
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
            warningPopup("ICON DIRECTORY DOESNT EXIST - rerenderPreview()");
        }
    }

    /**
     * Renders Templates in Card editor
     */
    public void rerenderTemplatePreviews() {
        File templateDir = new File(projectDirectory + "/" + "templates");
        if(templateDir.exists()) {
            String[] fileList = templateDir.list();

            this.previewTemplateFlowPane.getChildren().clear(); // Removes all so it can be re rendered
            if (fileList != null) {
                for (int i = 0; i < fileList.length; i++) {
                    final File currentFile = new File(templateDir.getAbsolutePath() + "/" + fileList[i]);

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

                    ComboBox comboBox = new ComboBox(options());

                    if (fileList[i].length() == 6) { // That being '00.png' = 6
                        try {
                            comboBox.getSelectionModel().select(fileList[i].substring(0, 2));
                        } catch (Exception e) {
                            warningPopup("TWO DIGIT LONG FILE NAME IS NOT VIABLE (1-99): " + fileList[i].substring(0,2) + " - rerenderPreview()");
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

                        currentFile.renameTo(new File(templateDir.getAbsolutePath() + "/" + newValue + ".pdf"));
                        rerenderTemplatePreviews();
                    });
                }
            }
        } else {
            warningPopup("TEMPLATE DIRECTORY DOESNT EXIST - rerenderTemplatePreview()");
        }
    }

    /**
     * Renders all possible numbers with a checkbox, to keep track of what has been called
     */
    // TODO WIP
    public HBox renderPlayTracker() {
        // Main PlayBox
        HBox playBox = new HBox();
        playBox.setAlignment(Pos.CENTER);
        playBox.setSpacing(10);
        playBox.setPadding(new Insets(0, 10, 10, 10));

        // File Locations
        File iconDir = new File(projectDirectory + "/" + "icons");
        String[] fileList = iconDir.list();

        for (int i = 0; i < 5; i ++) {
            VBox vBox = new VBox();
            vBox.setAlignment(Pos.CENTER);
            vBox.setPadding(new Insets(0, 10, 0, 10));

            for (int j = 1; j < 16; j++) {
                HBox numBox = new HBox();
                numBox.setAlignment(Pos.CENTER);
                numBox.setPadding(new Insets(0, 10, 0, 10));

                final File fileNum = new File(iconDir.getAbsolutePath() + "/" + "01.png");
                numBox.setStyle("-fx-border-style: solid inside; -fx-border-width: 1; -fx-border-insets: 5; -fx-border-radius: 5; -fx-border-color: black;");
                numBox.setPadding(new Insets(5, 5, 5, 5));
                numBox.setSpacing(5);
                numBox.setAlignment(Pos.CENTER);

                Text textNumber = new Text(Integer.toString(j));
                numBox.getChildren().add(textNumber);

                Image image = new Image("file:" + iconDir.getAbsolutePath() + "/" + fileList[i]);
                ImageView imageView = new ImageView(image);
                imageView.setFitHeight(30);
                imageView.setFitWidth(30);
                numBox.getChildren().add(imageView);


                CheckBox checkNum = new CheckBox();
                numBox.getChildren().add(checkNum);

                vBox.getChildren().add(numBox);
            }
            playBox.getChildren().add(vBox);
        }

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(playBox);

        HBox calledBox = new HBox();
        calledBox.setAlignment(Pos.CENTER);
        calledBox.setSpacing(10);
        calledBox.setPadding(new Insets(0, 10, 0, 10));
        calledBox.getChildren().add(scrollPane);
        return calledBox;
    }

    /**
     * Generates available numbers
     */
    public ObservableList<String> options(){
        ObservableList<String> options =  FXCollections.observableArrayList(
                " "
        );

        for(int i = 0; i < 76; i++){
            String currentNum = Integer.toString(i);

            if (i == 0){
                currentNum = "Unlink";
            } else if (i < 10) {
                currentNum = "0" + Integer.toString(i);
            }

            File currentFile = new File(projectDirectory + "/icons/" + currentNum + ".png");

            if(!currentFile.exists()){
                options.add(currentNum);
            }
        }

        return options;
    }

    /**
     * Generates a 5x5 grid of random numbers, following the BINGO rules per column
     */
    public static int[][] randomizeCard() {
        int[][] card = new int[5][5];
        Random rand = new Random();

        for (int i = 0; i < card.length; i++) {
            for (int j = 0; j < card[i].length; j++) {
                int nextInt;
                while (card[i][j] == 0) {
                    nextInt = rand.nextInt(15) + 1 + (15 * i);

                    if (!contains(card[i], nextInt)) {
                        card[i][j] = nextInt;
                    }
                }
            }
        }
        return card;
    }

    /**
     * Outputs Bingo Card in Debug
     */
    public static void outputCard(int[][] array) {
        String[][] strArray = new String[5][5];
        String[] letterArray = {"B", "I", "N", "G", "O"};

        for (int i = 0; i < array.length; i++) {
            for (int j = 0; j < array[i].length; j++) {
                if (Integer.toString(array[i][j]).length() == 1) {
                    strArray[i][j] = "0" + array[i][j];
                } else {
                    strArray[i][j] = Integer.toString(array[i][j]);
                }
            }
            System.out.println(letterArray[i] + ": " + "[" + strArray[i][0] + "]" + "[" + strArray[i][1] + "]" + "[" + strArray[i][2] + "]" + "[" + strArray[i][3] + "]" + "[" + strArray[i][4] + "]");
        }
    }

    /**
     * Checks if an array has a specified integer value
     */
    public static boolean contains(final int[] array, final int val) {
        boolean result = false;

        for (int i : array) {
            if (i == val) {
                result = true;
                break;
            }
        }

        return result;
    }

    public void generatePDF(int numOfCards, int numOfBooks) throws Exception {
        String templateCard = projectDirectory + "/templates/01.png"; // TODO TEMPORARY

        // Create new PDF
        PDDocument doc = new PDDocument();

        // Create new page based on # of cards specified
        for(int b = 0; b < numOfBooks; b++) {
            for (int n = 0; n < numOfCards; n++) {
                // Create array of card #'s
                int[][] cardNumbers = randomizeCard();

                // Create new page based on template
                PDPage page = new PDPage();
                PDPageContentStream contents = new PDPageContentStream(doc, page, PDPageContentStream.AppendMode.APPEND, true, true);

                // Draw Template TODO
                PDImageXObject template = PDImageXObject.createFromFile(templateCard, doc);
                contents.drawImage(template, 0, 0, page.getCropBox().getWidth(), page.getCropBox().getHeight());

                // Draws Icon
                File iconDir = new File(projectDirectory + "/icons/");
                int iAdder = 0;
                int jAdder = 0;

                // Iterates through each square, places each icon
                for (int i = 0; i < 5; i++) {
                    for (int j = 0; j < 5; j++) {
                        if (i == 2 && j == 2) {
                            // Don't render, Free Space
                        } else {
                            String currentNum = Integer.toString(cardNumbers[i][j]);
                            if (cardNumbers[i][j] < 10) {
                                currentNum = "0" + cardNumbers[i][j];
                            }

                            String path = iconDir + "/" + currentNum + ".png";
                            allNumbers = true;

                            // If icon exists use, otherwise use a Number
                            if (new File(path).exists()) {
                                PDImageXObject icon = PDImageXObject.createFromFile(path, doc);
                                contents.drawImage(icon, 52 + iAdder, 79 + jAdder, 81, 81);
                            } else {
                                allNumbers = false;

                                contents.beginText();
                                contents.setFont(PDType1Font.TIMES_BOLD, 50);
                                contents.newLineAtOffset(52 + iAdder, 79 + jAdder);
                                contents.showText(currentNum);
                                contents.endText();
                            }
                        }
                        // Needs to vary movement every 2nd square
                        if (j % 2 == 0) {
                            jAdder += 105;
                        } else {
                            jAdder += 106;
                        }

                    }
                    jAdder = 0;

                    // Needs to vary movement every 2nd square
                    if (i % 2 == 0) {
                        iAdder += 105;
                    } else {
                        iAdder += 106;
                    }
                }

                if(numOfCards > 1) {
                    // Create Page Number in Corner
                    contents.beginText();
                    contents.setFont(PDType1Font.TIMES_BOLD, 50);

                    if (n + 1 >= 10) {
                        contents.newLineAtOffset(550, 745);
                    } else {
                        contents.newLineAtOffset(575, 745);
                    }
                    contents.showText(Integer.toString(n + 1));
                    contents.endText();
                }


                contents.close();
                doc.addPage(page);
            }
        }
        doc.save(projectDirectory.getParentFile() + "/Bingo Cards -" + RandomStringUtils.randomAlphanumeric(8) + ".pdf");
        doc.close();
    }

    public void warningPopup(String warning){
        BorderPane layout = new BorderPane();
        VBox vbox = new VBox();
        vbox.setAlignment(Pos.CENTER);
        vbox.setPadding(new Insets(10, 10, 10, 10));

        Text errorText = new Text(warning);
        vbox.getChildren().add(errorText);
        layout.setCenter(vbox);

        HBox dismissBox = new HBox();
        dismissBox.setPadding(new Insets(10, 10, 10, 10));
        dismissBox.setAlignment(Pos.CENTER);
        Button dismiss = new Button("Dismiss");
        dismissBox.getChildren().add(dismiss);
        layout.setBottom(dismissBox);

        Scene errorScene = new Scene(layout, 400, 100);
        Stage errorStage = new Stage();
        errorStage.setTitle("ERROR");
        errorStage.setScene(errorScene);
        errorStage.show();

        dismiss.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                errorStage.hide();
            }
        });
    }
}
package gunn.biingo;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.nio.file.Files;
import java.util.Random;

public class Main extends Application {
    Stage primaryStage = null;
    File projectDirectory = null;

    FlowPane previewFlowPane = null;
    FlowPane previewTemplateFlowPane = null;

    int gamesPerBooklet = 1;
    int printedBooklets = 1;
    int nextId = 0;
    boolean pageNumbers = false;
    boolean dividePages = false;
    JSONObject cardDatabase = new JSONObject();

    VBox warnBox;

    Button buttonGenerate;
    Text bookText;
    Text pageText;
    Text timeText;
    ProgressBar progressBar;
    Thread thread;

    HBox databasePane;
    VBox listPane;

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
        // Set scene
        BorderPane borderLayout = new BorderPane();
        borderLayout.setStyle("-fx-background-color: #e65650");
        Scene mainMenuScene = new Scene(borderLayout, 500, 300);
        String css = this.getClass().getResource("/assets/style.css").toExternalForm();
        mainMenuScene.getStylesheets().add(css);

        // Bottom Panes
        VBox bottomBox = new VBox();
        //  Menu Buttons
        HBox menuHBox = new HBox();
        menuHBox.setAlignment(Pos.BOTTOM_CENTER);
        menuHBox.setPadding(new Insets(15, 12, 15, 12));
        menuHBox.setSpacing(10);
        // Elements
        Button buttonLoad = new Button("Load");
        buttonLoad.getStyleClass().add("record-sales");
        buttonLoad.setMinWidth(100);
        Button buttonNew = new Button("New");
        buttonNew.setMinWidth(100);
        buttonNew.getStyleClass().add("record-sales");
        menuHBox.getChildren().add(buttonLoad);
        menuHBox.getChildren().add(buttonNew);
        //Copyrights
        HBox copyBox = new HBox();
        copyBox.setStyle("-fx-background-color: #ed827e");
        copyBox.setPadding(new Insets(5, 5, 5, 5));
        copyBox.setAlignment(Pos.CENTER_RIGHT);
        // Elements
        Text copy = new Text("V1.1.0 - Copyright Brady Gunn 2020. All rights reserved");
        copy.setTextAlignment(TextAlignment.RIGHT);
        copyBox.getChildren().add(copy);
        // Add to Bottom Box
        bottomBox.getChildren().add(menuHBox);
        bottomBox.getChildren().add(copyBox);
        borderLayout.setBottom(bottomBox);

        // Center
        Image image = new Image(getClass().getResource("/assets/logo.png").toExternalForm());
        ImageView imageView = new ImageView(image);
        imageView.setFitHeight(200);
        imageView.setFitWidth(200);
        borderLayout.setCenter(imageView);

        // Set Scene
        primaryStage.setScene(mainMenuScene);
        primaryStage.setResizable(false);
        primaryStage.show();

        // Listeners
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
    public void createProjectDirectory(File file) {
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

        File propertiesFile = new File(projectDir + "/" + "bingo.properties");
        try {
            propertiesFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        File bingoDatabaseFile = new File(projectDir + "/" + "bingo.database");
        try {
            bingoDatabaseFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Project File
        file.mkdir();

        if (projectDir.exists()) {
            projectDirectory = projectDir;
            windowProject();
        } else {
            warningPopup("PROJECT DIRECTORY DOESNT EXIST - createProjectDirectory()");
        }
    }

    /**
     * Load file structure for project
     */
    public void loadProjectDirectory(File file) {
        boolean success = true;

        if (file.exists()) {
            projectDirectory = file;
        } else {
            success = false;
        }

        if (!new File(projectDirectory + "/icons").exists()) {
            success = false;
        }

        // Get Properties
        Object obj = null;
        try {
            obj = new JSONParser().parse(new FileReader(projectDirectory + "/" + "bingo.properties"));
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        JSONObject jo = (JSONObject) obj;

        if (jo.get("games") != null) {
            gamesPerBooklet = Integer.parseInt(jo.get("games").toString());
        }

        if (jo.get("print") != null) {
            printedBooklets = Integer.parseInt(jo.get("print").toString());
        }

        if (jo.get("pageNum") != null) {
            pageNumbers = jo.get("pageNum").toString().equals("true");
        }

        if (jo.get("divide") != null) {
            dividePages = jo.get("divide").toString().equals("true");
        }

        if (jo.get("nextid") != null) {
            nextId = Integer.parseInt(jo.get("nextid").toString());
        }

        // Database
        obj = null;
        try {
            obj = new JSONParser().parse(new FileReader(projectDirectory + "/" + "bingo.database"));
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
         JSONObject joo = (JSONObject) obj;
        if (joo != null) {
            cardDatabase = joo;
        }

        if (success) {
            windowProject();
        } else {
            warningPopup("PROJECT DIRECTORY IS INVALID - loadProjectDirectory()");
        }
    }

    /**
     * Scene: Card editor
     */
    public void windowProject() {
        buttonGenerate = new Button("Generate");
        warnBox = new VBox();

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
        Scene bingoScene = new Scene(tabPane, 750, 550);
        primaryStage.setScene(bingoScene);
        primaryStage.show();
    }

    // ------ TABS ------
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
        for (int i = 1; i <= 25; i++) {
            genNumbers.add(Integer.toString(i));
        }
        Text comboText = new Text("Games per Booklet:");
        ComboBox comboNumber = new ComboBox(genNumbers);
        comboNumber.getSelectionModel().select(gamesPerBooklet);
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
                gamesPerBooklet = Integer.parseInt(newValue.toString());
                rerenderTemplatePreviews();
                saveToProperties();
            }
        });
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
        for (int i = 1; i <= 100; i++) {
            genNumbers.add(Integer.toString(i));
        }
        Text comboText = new Text("Booklets Printed:");
        ComboBox comboNumber = new ComboBox(genNumbers);
        comboNumber.getSelectionModel().select(printedBooklets);
        comboBox.getChildren().add(comboText);
        comboBox.getChildren().add(comboNumber);

        //Checkboxes
        VBox checkBox = new VBox();
        checkBox.setAlignment(Pos.CENTER);
        checkBox.setSpacing(10);
        checkBox.setPadding(new Insets(2, 10, 2, 10));
        // Elements
        CheckBox checkPageNum = new CheckBox("Page Numbers");
        checkPageNum.selectedProperty().set(pageNumbers);
        checkBox.getChildren().add(checkPageNum);
        CheckBox checkDivide = new CheckBox("Divide Booklets");
        checkDivide.selectedProperty().set(dividePages);
        checkBox.getChildren().add(checkDivide);

        // Add all elements to main layout and send to Tab
        vbox.getChildren().add(comboBox);
        vbox.getChildren().add(checkBox);
        vbox.getChildren().add(buttonGenerate);
        mainLayout.setCenter(vbox);
        tab.setContent(mainLayout);

        // Listeners
        //ComboNumbers onChanges
        comboNumber.valueProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                printedBooklets = Integer.parseInt(newValue.toString());
                saveToProperties();
            }
        });

        //CheckPageNum Listener
        checkPageNum.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                pageNumbers = newValue;
                saveToProperties();
            }
        });

        //CheckDivide Listener
        checkDivide.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                dividePages = newValue;
                saveToProperties();
            }
        });

        // Generate Button
        buttonGenerate.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                generatePopup();
            }
        });
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

        // Listeners

        //Button View Cards
        buttonViewCards.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try {
                    databasePopup();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // ------ LISTENERS ------
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
                    rerenderTemplatePreviews();
                }
            }
            event.setDropCompleted(success);
            event.consume();
        }
    };

    // ------ RENDER ------

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
                            warningPopup("TWO DIGIT LONG FILE NAME IS NOT VIABLE (1-75): " + fileList[i].substring(0, 2) + " - rerenderPreview()");
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
        boolean redo = false;

        File templateDir = new File(projectDirectory + "/" + "templates");
        if (templateDir.exists()) {
            String[] fileList = templateDir.list();

            this.previewTemplateFlowPane.getChildren().clear(); // Removes all so it can be re rendered
            if (fileList != null) {

                final File defaultTemplate = new File(templateDir.getAbsolutePath() + "/01.png");
                if (defaultTemplate.exists()) {
                    buttonGenerate.setDisable(false);
                    warnBox.setVisible(false);
                } else {
                    buttonGenerate.setDisable(true);
                    warnBox.setVisible(true);
                }

                for (int i = 0; i < fileList.length; i++) {
                    final File currentFile = new File(templateDir.getAbsolutePath() + "/" + fileList[i]);
                    String numName = currentFile.getName().substring(0, currentFile.getName().length() - 4);

                    if (numName.length() == 2) {
                        if (Integer.parseInt(numName) > gamesPerBooklet) {
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
                            warningPopup("TWO DIGIT LONG FILE NAME IS NOT VIABLE (1-99): " + fileList[i].substring(0, 2) + " - rerenderTemplatePreview()");
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
            warningPopup("TEMPLATE DIRECTORY DOESNT EXIST - rerenderTemplatePreview()");
        }

        if (redo) {
            rerenderTemplatePreviews();
        }
    }

    /**
     * Renders all possible numbers with a checkbox, to keep track of what has been called
     */
    public HBox renderPlayTracker() {
        // Main PlayBox
        HBox playBox = new HBox();
        playBox.setAlignment(Pos.CENTER);
        playBox.setSpacing(10);
        playBox.setPadding(new Insets(0, 10, 10, 10));

        // File Locations
        File iconDir = new File(projectDirectory + "/" + "icons");
        String[] fileList = iconDir.list();

        // Loops through B I N G O
        for (int i = 0; i < 5; i++) {
            VBox vBox = new VBox();
            vBox.setAlignment(Pos.CENTER);
            vBox.setPadding(new Insets(0, 10, 0, 10));

            // Looks through 1-15 of each letter
            for (int j = 1; j < 16; j++) {
                HBox numBox = new HBox();
                numBox.setAlignment(Pos.CENTER);
                numBox.setPadding(new Insets(0, 10, 0, 10));

                final File fileNum = new File(iconDir.getAbsolutePath() + "/" + "01.png");
                numBox.setStyle("-fx-border-style: solid inside; -fx-border-width: 1; -fx-border-insets: 5; -fx-border-radius: 5; -fx-border-color: black;");
                numBox.setPadding(new Insets(5, 5, 5, 5));
                numBox.setSpacing(5);
                numBox.setAlignment(Pos.CENTER);

                // Adds number
                Text textNumber = new Text(Integer.toString(j + (i * 15)));
                numBox.getChildren().add(textNumber);

                String numberName = Integer.toString(j + (i * 15));
                if (Integer.parseInt(numberName) < 10) {
                    numberName = "0" + numberName;
                }

                File numFile = new File(iconDir.getAbsolutePath() + "/" + numberName + ".png");

                // If icon file of number exists, use element, if not use a large text version of number
                if (numFile.exists()) {
                    Image image = new Image("file:" + numFile);
                    ImageView imageView = new ImageView(image);
                    imageView.setFitHeight(30);
                    imageView.setFitWidth(30);
                    numBox.getChildren().add(imageView);
                } else {
                    Text t = new Text(numberName);
                    t.setStyle("-fx-font-size: 22; -fx-font-weight: bold");
                    numBox.getChildren().add(t);
                }

                // Add checkbox
                CheckBox checkNum = new CheckBox();
                numBox.getChildren().add(checkNum);
                vBox.getChildren().add(numBox);
            }
            playBox.getChildren().add(vBox);
        }

        // Put into scrollpane
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(playBox);

        HBox calledBox = new HBox();
        calledBox.setAlignment(Pos.CENTER);
        calledBox.setSpacing(10);
        calledBox.setPadding(new Insets(0, 10, 0, 10));
        calledBox.getChildren().add(scrollPane);
        return calledBox;
    }

    // ------ GENERATION ------

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

    /**
     * Generates available template numbers
     */
    public ObservableList<String> templateOptions() {
        ObservableList<String> options = FXCollections.observableArrayList(
                " "
        );

        for (int i = 0; i < gamesPerBooklet + 1; i++) {
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

    /**
     * Generates a 5x5 grid of random numbers, following the BINGO rules per column
     */
    public static JSONObject randomizeCard() {
        JSONObject card = new JSONObject();
        Random rand = new Random();

        JSONObject letter;
        for (int i = 0; i < 5; i++) {
            letter = new JSONObject();
            for (int j = 0; j < 5; j++) {
                int nextInt;
                while (letter.get(j) == null) {
                    nextInt = rand.nextInt(15) + 1 + (15 * i);

                    if (!letter.containsValue(nextInt)) {
                        letter.put(j, nextInt);
                    }
                }
                card.put(i, letter);
            }
        }

        return card;
    }

    /**
     * Generate id for card
     */
    public String getNewBookID(int next) {
        String bookID = Integer.toString(next);
        next++;

        switch (bookID.length()) {
            case 1:
                bookID = "0000" + bookID;
                break;
            case 2:
                bookID = "000" + bookID;
                break;
            case 3:
                bookID = "00" + bookID;
                break;
            case 4:
                bookID = "0" + bookID;
                break;
        }

        return bookID;
    }

    public String generatePDF() throws Exception {
        final File templateDirectory = new File(projectDirectory + "/templates");
        final File iconDir = new File(projectDirectory + "/icons");
        final File saveDir = projectDirectory.getParentFile();

        // Variables
        PDDocument doc;
        PDPage page;
        PDPageContentStream contents;
        PDImageXObject template;
        String currentNum;
        String path;
        PDImageXObject icon;
        int iAdder;
        int jAdder;
        int b;
        int n;
        int i;
        int j;

        final int books = printedBooklets;
        final int games = gamesPerBooklet;
        final boolean pageNums = pageNumbers;
        final File[] fileTemplates = new File[games + 1];

        JSONObject tempDatabase = (JSONObject) cardDatabase;
        int nextCardID = nextId;

        //Gathering Templates
        // Check if template exists, if not use default [01]
        File templateFile;
        String numFileName;
        for (int t = 1; t < games + 1; t++) {
            // Num File Name
            if (t < 10) {
                numFileName = "0" + t + ".png";
            } else {
                numFileName = t + ".png";
            }

            templateFile = new File(templateDirectory + "/" + numFileName);

            if (templateFile.exists()) {
                fileTemplates[t] = templateFile;
            } else {
                fileTemplates[t] = new File(templateDirectory + "/01.png");
            }
        }

        doc = null;
        if (!dividePages) {
            doc = new PDDocument();
        }

        String bookId;
        String finalId;

        // Create new page based on # of cards specified
        for (b = 1; b < books + 1; b++) { // BOOKLETS PRINTED
            if (dividePages) {
                doc = new PDDocument();
            }

            bookId = getNewBookID(nextCardID);
            nextCardID++;

            // Go through each page
            for (n = 1; n < games + 1; n++) { // GAMES PER BOOKLET
                // Create array of card #'s
                JSONObject cardObj = randomizeCard();

                // Save card to database
                if (n < 10) {
                    finalId = bookId + "0" + n;
                } else {
                    finalId = bookId + n;
                }
                addCardToDatabase(tempDatabase, cardObj, finalId);

                // Create new page based on template
                page = new PDPage();
                contents = new PDPageContentStream(doc, page, PDPageContentStream.AppendMode.APPEND, false, true);

                // Draw Template
                template = PDImageXObject.createFromFile(fileTemplates[n].getAbsolutePath(), doc);
                contents.drawImage(template, 0, 0, page.getCropBox().getWidth(), page.getCropBox().getHeight());

                // Draws Icon
                iAdder = 0;
                jAdder = 0;

                contents.beginText();
                contents.setFont(PDType1Font.TIMES_BOLD, 25);
                contents.newLineAtOffset(250, 750);
                contents.showText("ID: " + finalId);
                contents.endText();

                // Iterates through each square, places each icon
                for (i = 0; i < 5; i++) {
                    for (j = 0; j < 5; j++) {
                        if (i != 2 || j != 2) {
                            currentNum = (((JSONObject) cardObj.get(i)).get(j)).toString();

                            if (Integer.parseInt(currentNum) < 10) {
                                currentNum = "0" + currentNum;
                            }

                            path = iconDir + "/" + currentNum + ".png";

                            // If icon exists use, otherwise use a Number
                            if (new File(path).exists()) {
                                icon = PDImageXObject.createFromFile(path, doc);
                                contents.drawImage(icon, 52 + iAdder, 79 + jAdder, 81, 81);
                            } else {
                                contents.beginText();
                                contents.setFont(PDType1Font.TIMES_BOLD, 50);
                                contents.newLineAtOffset(52 + iAdder, 79 + jAdder);
                                contents.showText(currentNum);
                                contents.endText();
                            }
                        } else {  // Don't render, Free Space
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

                if (pageNums) {
                    if (games > 1) {
                        // Create Page Number in Corner
                        contents.beginText();
                        contents.setFont(PDType1Font.TIMES_BOLD, 50);

                        if (n >= 10) {
                            contents.newLineAtOffset(550, 745);
                        } else {
                            contents.newLineAtOffset(575, 745);
                        }
                        contents.showText(Integer.toString(n));
                        contents.endText();
                    }
                }

                contents.close();
                doc.addPage(page);

                updateProgressBar(b, n, books, games);
            }
            if (dividePages) {
                doc.save(saveDir + "/Cards - " + RandomStringUtils.randomAlphanumeric(8) + ".pdf");
                doc.close();
                cardDatabase = (JSONObject) tempDatabase;
                nextId = nextCardID;
            }
        }
        if (!dividePages) {
            doc.save(saveDir + "/Cards - " + RandomStringUtils.randomAlphanumeric(8) + ".pdf");
            doc.close();
            cardDatabase = (JSONObject) tempDatabase;
            nextId = nextCardID;
        }
        return null;
    }

    // ------ POPUP ------

    /**
     * Popup window for errors
     *
     * @param warning
     */
    public void warningPopup(String warning) {
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

    /**
     * Popup window when generating BINGO Cards
     */
    public void generatePopup() {
        buttonGenerate.setDisable(true);

        Stage genStage = new Stage();
        genStage.setTitle("Generating...");
        genStage.setResizable(false);
        BorderPane mainPane = new BorderPane();

        Text titleText = new Text("BINGO Cards Generating");
        titleText.setStyle("-fx-font-weight: bold; -fx-font-size: 15px");
        Text messageText = new Text("This make take a couple minutes...");
        progressBar = new ProgressBar(0);
        progressBar.setStyle("-fx-pref-width: 200px");
        timeText = new Text("Approximately " + "0 minutes and " + "0 seconds");
        bookText = new Text("Booklet " + "0" + " of " + printedBooklets);
        bookText.setStyle("-fx-font-weight: bold; -fx-fill: green");
        pageText = new Text("Page " + "0" + " of " + gamesPerBooklet);
        pageText.setStyle("-fx-font-weight: bold; -fx-fill: green");

        // Top
        VBox topBox = new VBox();
        topBox.setPadding(new Insets(10, 0, 0, 0));
        topBox.setAlignment(Pos.CENTER);
        topBox.setSpacing(5);
        topBox.getChildren().add(titleText);
        topBox.getChildren().add(messageText);

        // Center
        VBox messageBox = new VBox();
        messageBox.setAlignment(Pos.CENTER);
        messageBox.setSpacing(5);
        messageBox.getChildren().add(timeText);
        messageBox.getChildren().add(progressBar);
        messageBox.getChildren().add(bookText);
        messageBox.getChildren().add(pageText);
        mainPane.setTop(topBox);
        mainPane.setCenter(messageBox);

        // Bottom
        HBox bottomBox = new HBox();
        bottomBox.setAlignment(Pos.CENTER);
        bottomBox.setPadding(new Insets(10, 10, 10, 10));
        Button cancelButton = new Button("Cancel");
        bottomBox.getChildren().add(cancelButton);
        mainPane.setBottom(bottomBox);

        Scene genScene = new Scene(mainPane, 250, 200);
        genStage.setScene(genScene);
        genStage.show();

        // New thread that generates PDFs
        thread = null;
        try {
            Task<Void> executeAppTask = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    Process p = Runtime.getRuntime().exec(generatePDF());
                    p.waitFor();
                    return null;
                }
            };

            executeAppTask.setOnFailed(e -> {
                genStage.close();
                buttonGenerate.setDisable(false);
                saveCardDatabase();
                saveToProperties();
            });

            thread = new Thread(executeAppTask);
            thread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Listeners
        // Cancel Button
        cancelButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                thread.stop();
                genStage.close();
                buttonGenerate.setDisable(false);
                saveCardDatabase();
                saveToProperties();
            }
        });
        // If Stage is closed
        genStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                thread.stop();
                buttonGenerate.setDisable(false);
                saveCardDatabase();
                saveToProperties();
            }
        });
    }

    /**
     * Popup window for card database
     */
    public void databasePopup() throws ParseException {
        Stage stage = new Stage();
        stage.setTitle("Card Database");
        databasePane = new HBox();

        // List Pane
        listPane = new VBox();
        listPane.setAlignment(Pos.CENTER);
        listPane.setSpacing(10);
        listPane.setPadding(new Insets(10, 10, 10, 10));
        listPane.setStyle("-fx-background-color: #57595c");
        // Elements
        Text textTitle = new Text("Bingo Cards");
        textTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-fill: white");
        listPane.getChildren().add(textTitle);
        // Card List Element
        ListView<String> cardList = new ListView<String>();
        ObservableList<String> cardItems = FXCollections.observableArrayList();
        for (Object o : cardDatabase.keySet()) {
            cardItems.add((String) o);
        }
        FXCollections.sort(cardItems);
        cardList.setItems(cardItems);
        listPane.getChildren().add(cardList);
        // SearchBar
        TextField searchField = new TextField();
        listPane.getChildren().add(searchField);
        // Buttons
        Button buttonDelete = new Button("DELETE Card Database");
        listPane.getChildren().add(buttonDelete);
        databasePane.getChildren().add(listPane);

        rerenderDatabaseCard(cardList.getSelectionModel().getSelectedItem());

        Scene scene = new Scene(databasePane, 700, 600);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();

        // Listeners
        // Card list listener
        cardList.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if(!cardList.getSelectionModel().getSelectedItem().equals("No Results Found.")) {

                    try {
                        rerenderDatabaseCard(cardList.getSelectionModel().getSelectedItem());
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        // search box listener
        searchField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                ListView<String> cardList = new ListView<String>();
                ObservableList<String> cardItems = FXCollections.observableArrayList();


                for (Object o : cardDatabase.keySet()) {
                    if(((String) o).contains(newValue)){
                        cardItems.add((String) o);
                    }
                }

                if(cardItems.size() == 0){
                    cardItems.add("No Results Found.");
                }

                FXCollections.sort(cardItems);
                cardList.setItems(cardItems);

                listPane.getChildren().set(1, cardList);

                cardList.setOnMouseClicked(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        if(!cardList.getSelectionModel().getSelectedItem().equals("No Results Found.")) {

                            try {
                                rerenderDatabaseCard(cardList.getSelectionModel().getSelectedItem());
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
            }
        });
    }

    /**
     * Rerender cardview for the database window
     */
    public void rerenderDatabaseCard(String cardID) throws ParseException {
        File templateDir = new File(projectDirectory + "/templates");
        File iconDir = new File(projectDirectory + "/icons");

        // Card pane
        StackPane stackPane = new StackPane();
        Image card = new Image("file:" + templateDir + "/" + "01.png");
        ImageView cardView = new ImageView(card);
        cardView.setFitHeight(600);
        cardView.setFitWidth(500);
        stackPane.getChildren().add(cardView);

        if (cardDatabase.get(cardID) != null) {
            // Render Icons
            JSONObject cardJSON = (JSONObject) new JSONParser().parse((String) cardDatabase.get(cardID));
            JSONObject letterJSON;
            String slotNum;
            for (int i = 0; i < 5; i++) {
                for (int j = 0; j < 5; j++) {
                    if (i != 2 || j != 2) {
                        letterJSON = (JSONObject) cardJSON.get(Integer.toString(i));
                        slotNum = letterJSON.get(Integer.toString(j)).toString();

                        if (slotNum.length() == 1) {
                            slotNum = "0" + slotNum;
                        }

                        File slotIcon = new File(iconDir + "/" + slotNum + ".png");
                        if (slotIcon.exists()) {
                            Image icon = new Image("file:" + slotIcon.getAbsolutePath());
                            ImageView iconView = new ImageView(icon);
                            iconView.setFitWidth(60);
                            iconView.setFitHeight(60);
                            iconView.setTranslateX(-175 + (i * 87));
                            iconView.setTranslateY(210 - (j * 80));
                            stackPane.getChildren().add(iconView);
                        } else {
                            Text text = new Text(slotNum);
                            text.setStyle("-fx-font-size: 20px; -fx-font-weight: bold");
                            text.setTranslateX(-175 + (i * 87));
                            text.setTranslateY(210 - (j * 80));
                            stackPane.getChildren().add(text);
                        }
                    }
                }
            }
        }
        if (databasePane.getChildren().size() > 1) {
            databasePane.getChildren().remove(databasePane.getChildren().size() - 1);
        }
        databasePane.getChildren().add(stackPane);
    }

    /**
     * Save properties to properties file
     */
    public void saveToProperties() {
        JSONObject obj = new JSONObject();
        obj.put("games", gamesPerBooklet);
        obj.put("print", printedBooklets);
        obj.put("pageNum", pageNumbers);
        obj.put("divide", dividePages);
        obj.put("nextid", nextId);

        try {
            FileWriter fileProps = new FileWriter(projectDirectory + "/" + "bingo.properties");
            fileProps.write(obj.toString());
            fileProps.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addCardToDatabase(JSONObject tempDatabase, JSONObject card, String id) {
        tempDatabase.put(id, card.toString());
    }

    public void saveCardDatabase() {
        try {
            FileWriter fileProps = new FileWriter(projectDirectory + "/" + "bingo.database");
            fileProps.write(cardDatabase.toString());
            fileProps.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void updateProgressBar(int b, int n, int books, int games) {
        bookText.setText("Booklet " + b + " of " + books);
        pageText.setText("Page " + n + " of " + games);

        double onNum = (((double) n + (((double) b - 1) * (double) games)));
        double total = ((double) games * (double) books);

        int rawTime = (int) (total - onNum) / 2;
        int minTime = rawTime / 60;
        int secTime = rawTime % 60;
        timeText.setText("Approximately " + minTime + " minutes and " + secTime + " seconds");

        progressBar.setProgress(onNum / total);
    }
}
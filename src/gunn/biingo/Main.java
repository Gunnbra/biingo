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
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ScrollPane;
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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Random;

public class Main extends Application {
    Stage primaryStage = null;

    File projectDirectory = null;
    File templateCard = null;

    boolean allNumbers = true;

    FlowPane previewFlowPane = null;

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
        Text copy = new Text("V1.0.1 - Copyright Brady Gunn 2020. All rights reserved");
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

        // Project File
        file.mkdir();

        if(projectDir.exists()){
            projectDirectory = projectDir;
            windowCardCreation(false);
        } else {
            warningPopup("PROJECT DIRECTORY DOESNT EXIST - createProjectDirectory()");
        }
    }

    /**
     * Load file structure for project
     */
    public void loadProjectDirectory(File file){
        boolean success = true;
        boolean template = false;

        if(file.exists()) {
            projectDirectory = file;
        } else {
            success = false;
        }

        if(!new File(projectDirectory + "/icons").exists()){
            success = false;
        }

        File tempCard = new File(file + "/" + "template.pdf");
        if(tempCard.exists()){
            templateCard = tempCard;
            template = true;
        }

        if(success){
            windowCardCreation(template);
        }else{
            warningPopup("PROJECT DIRECTORY IS INVALID - loadProjectDirectory()");
        }
    }

    /**
     * Scene: Card editor
     */
    public void windowCardCreation(boolean isThereATemplate) {
        // Scene Creations
        BorderPane mainLayout = new BorderPane();

        // TOP Panel
        VBox topPanel = new VBox();
        // Back Button
        VBox backBox = new VBox();
        backBox.setPadding(new Insets(2, 2, 2, 2));
        backBox.setAlignment(Pos.CENTER_LEFT);
        Button buttonBack = new Button("Back");
        backBox.getChildren().add(buttonBack);
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
        topPanel.getChildren().add(backBox);
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

        // Bottom panel
        VBox bottomPane = new VBox();
        bottomPane.setAlignment(Pos.CENTER);
        // Template Panel
        HBox templatePane = new HBox();
        templatePane.setAlignment(Pos.CENTER);
        templatePane.setSpacing(10);
        templatePane.setPadding(new Insets(10, 10, 10, 10));
        Button buttonSetTemp = new Button("No Template Specified");
        templatePane.getChildren().add(buttonSetTemp);
        bottomPane.getChildren().add(templatePane);
        // Generate Panel
        HBox generatePane = new HBox();
        generatePane.setAlignment(Pos.CENTER);
        generatePane.setSpacing(10);
        generatePane.setPadding(new Insets(10, 10, 10, 10));
        Button buttonGen = new Button("Generate");
        buttonGen.setDisable(true);
        // Generate and create numbers for # of cards to generate
        ObservableList<String> genNumbers = FXCollections.observableArrayList(" ");
        for(int i = 1; i < 100; i++){
            genNumbers.add("x" + Integer.toString(i));
        }
        ComboBox comboNumber = new ComboBox(genNumbers);
        comboNumber.getSelectionModel().select("x1");
        generatePane.getChildren().add(comboNumber);
        generatePane.getChildren().add(buttonGen);
        bottomPane.getChildren().add(generatePane);
        mainLayout.setBottom(bottomPane);

        // If there is a template on load
        if(isThereATemplate) {
            buttonSetTemp.setText("Replace Template Card");
            buttonGen.setDisable(false);
        }

        // Render Preview Icons
        this.previewFlowPane = previewFlowPane;
        rerenderPreviews();

        // Setting Scene
        Scene bingoScene = new Scene(mainLayout, 525, 500);
        primaryStage.setScene(bingoScene);
        primaryStage.show();

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

        // Generate Button OnAction
        buttonGen.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                try {
                    generatePDF(Integer.parseInt(comboNumber.getValue().toString().substring(1)));
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
                    buttonSetTemp.setText("Replace Template Card");
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

        buttonBack.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                windowMainMenu();
            }
        });
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

    public void generatePDF(int numOfCards) throws Exception {
        // Loads Template Card File
        PDDocument templateDoc = PDDocument.load(templateCard);
        PDPage templatePage = templateDoc.getDocumentCatalog().getPages().get(0);

        // Create new PDF
        PDDocument doc = new PDDocument();

        // Create new page based on # of cards specified
        for (int n = 0; n < numOfCards; n++) {
            // Create array of card #'s
            int[][] cardNumbers = randomizeCard();

            // Create new page based on template
            PDPage page = templateDoc.importPage(templatePage);
            PDPageContentStream contents = new PDPageContentStream(doc, page, PDPageContentStream.AppendMode.APPEND, true, true);

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


            contents.close();
            doc.addPage(page);
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
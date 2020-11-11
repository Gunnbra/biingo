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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.converter.IntegerStringConverter;
import javafx.util.converter.NumberStringConverter;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Random;

public class Main extends Application {
    File projectLocation = null;
    File tempLocation = null;
    FlowPane previewFlowPane = null;

    public static void main(String[] args) throws Exception {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        primaryStage.setTitle("BINGO Card Generator");

        //  windowMainMenu(primaryStage);

        windowCardCreation(primaryStage);
        // insertIcon();
    }

    /**
     * Scene: Main Menu
     */
    public void windowMainMenu(Stage primaryStage) {
        // Create 'Load' and 'New' Buttons
        Button buttonLoad = new Button("Load");
        buttonLoad.setMinWidth(100);
        Button buttonNew = new Button("New");
        buttonNew.setMinWidth(100);

        // Create BorderPane layout and put buttons in an HBox at the bottom
        BorderPane borderLayout = new BorderPane();

        HBox menuHBox = new HBox();
        menuHBox.setAlignment(Pos.BOTTOM_CENTER);
        menuHBox.getChildren().add(buttonLoad);
        menuHBox.getChildren().add(buttonNew);
        menuHBox.setPadding(new Insets(15, 12, 15, 12));
        menuHBox.setSpacing(10);
        borderLayout.setBottom(menuHBox);

        // Set scene
        Scene mainMenuScene = new Scene(borderLayout, 300, 250);

        // Set Scene
        primaryStage.setScene(mainMenuScene);
        primaryStage.show();

        buttonLoad.setOnAction(value -> {
        });
        buttonNew.setOnAction(value -> {
            windowPopupNew(primaryStage);
        });
    }

    /**
     * Scene: New Project
     */
    public void windowPopupNew(Stage primaryStage) {
        // Main Layout
        VBox layout = new VBox();
        layout.setPadding(new Insets(10));
        layout.setSpacing(8);

        // Message
        Text message = new Text("Where would you like to create the project?");
        layout.getChildren().add(message);

        // Save Location HBox
        HBox saveLocationHBox = new HBox();
        saveLocationHBox.setSpacing(8);
        saveLocationHBox.setAlignment(Pos.CENTER_LEFT);
        // Save Location Elements
        Button buttonLocation = new Button("Save Location");
        Text textLocation = new Text("No Location Specified");
        textLocation.setFill(Color.GRAY);
        saveLocationHBox.getChildren().add(buttonLocation);
        saveLocationHBox.getChildren().add(textLocation);
        // Add save location hBox to main Layout
        layout.getChildren().add(saveLocationHBox);

        // Confirm/Cancel HBox
        HBox confirmHBox = new HBox();
        confirmHBox.setSpacing(8);
        confirmHBox.setAlignment(Pos.CENTER);
        // Add Buttons
        Button confirmButton = new Button("Confirm");
        confirmButton.setMinWidth(100);
        confirmButton.setDisable(true);
        Button cancelButton = new Button("Cancel");
        cancelButton.setMinWidth(100);
        confirmHBox.getChildren().add(confirmButton);
        confirmHBox.getChildren().add(cancelButton);
        // Add confirmHBox to main layout
        layout.getChildren().add(confirmHBox);

        // Set Scene
        Scene popupNewScene = new Scene(layout, 400, 100);
        Stage popupStage = new Stage();
        popupStage.setTitle("Create new Bingo Card?");
        popupStage.setScene(popupNewScene);
        popupStage.show();

        // Button handlers
        buttonLocation.setOnAction(value -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            File selectedDirectory = directoryChooser.showDialog(popupStage);

            if (selectedDirectory == null) {
                textLocation.setText("No Location Specified");
                confirmButton.setDisable(true);
            } else {
                textLocation.setText(selectedDirectory.getAbsolutePath());
                confirmButton.setDisable(false);
            }
            this.tempLocation = selectedDirectory;
        });
        confirmButton.setOnAction(value -> {
            this.projectLocation = tempLocation;
            this.tempLocation = null;

            // Created file structure
            File tempProject = new File("C:/Users/micro/Desktop/TestEnvironment");
            File iconDir = new File(tempProject.getAbsoluteFile() + "/icons");
            iconDir.mkdirs();

            popupStage.hide();
            windowCardCreation(primaryStage);
        });
        cancelButton.setOnAction(value -> {
            this.tempLocation = null;
            popupStage.hide();
        });
    }

    /**
     * Scene: Card editor
     */
    public void windowCardCreation(Stage primaryStage) {
        // Scene Creations
        BorderPane mainLayout = new BorderPane();

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
        // Set Vbox to layout
        mainLayout.setTop(dropVBox);

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

        // Bottom Generate Panel
        HBox generatePane = new HBox();
        generatePane.setAlignment(Pos.CENTER);
        generatePane.setSpacing(10);
        generatePane.setPadding(new Insets(10, 10, 10, 10));
        Button buttonGen = new Button("Generate");
        // Generate and create numbers for # of cards to generate
        ObservableList<String> genNumbers = FXCollections.observableArrayList(" ");
        for(int i = 1; i < 100; i++){
            genNumbers.add("x" + Integer.toString(i));
        }
        ComboBox comboNumber = new ComboBox(genNumbers);
        comboNumber.getSelectionModel().select("x1");

        generatePane.getChildren().add(comboNumber);
        generatePane.getChildren().add(buttonGen);
        mainLayout.setBottom(generatePane);

        this.previewFlowPane = previewFlowPane;
        rerenderPreviews();

        // Setting Scene
        Scene bingoScene = new Scene(mainLayout, 500, 500);
        primaryStage.setScene(bingoScene);
        primaryStage.show();

        // DragOver Event
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

        // OnDragOver Event
        dropVBox.setOnDragDropped(onDropFileHandler);

        buttonGetFiles.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG files (*.png)", "*.png"));
                Object[] files = fileChooser.showOpenMultipleDialog(primaryStage).toArray();

                for (int i = 0; i < files.length; i++) {
                    try {
                        String name = String.format("%s.%s", RandomStringUtils.randomAlphanumeric(8), "png");
                        File tempFile = new File("C:/Users/micro/Desktop/TestEnvironment/icons/temp" + name);
                        Files.copy(new File(files[i].toString()).toPath(), tempFile.toPath());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                rerenderPreviews();
            }
        });

        buttonGen.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                try {
                    generatePDF(10);
                } catch (Exception e) {
                    e.printStackTrace();
                }
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
                            File tempFile = new File("C:/Users/micro/Desktop/TestEnvironment/icons/temp" + name);
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
        File iconDir = new File("C:/Users/micro/Desktop/TestEnvironment/icons");
        String[] fileList = iconDir.list();

        this.previewFlowPane.getChildren().clear(); // Removes all so it can be re rendered
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
                }
                catch(Exception e){
                    System.out.println("2 digit long file name IS NOT A VIABLE NUMBER (1-75)");
                    System.out.println(fileList[i].substring(0, 2));
                }
            }

            hBox.getChildren().add(comboBox);

            this.previewFlowPane.getChildren().add(hBox);


            comboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue.toString() == " "){
                    newValue = RandomStringUtils.randomAlphanumeric(8);
                } else if (newValue.toString().length() == 1){ // Keep length to 2 digits
                    newValue = 0 + newValue.toString();
                }

                currentFile.renameTo(new File(iconDir.getAbsolutePath() + "/" + newValue + ".png"));
                rerenderPreviews();
            });
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

            File currentFile = new File("C:/Users/micro/Desktop/TestEnvironment/icons" + "/" + currentNum + ".png");

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

    public static void generatePDF(int numOfCards) throws Exception {
        // Loads Template Card File
        File file = new File("C:/Users/micro/Desktop/FILES/card.pdf");
        PDDocument doc = PDDocument.load(file);


        for(int n = 0; n < numOfCards; n++){
            int[][] cardNumbers = randomizeCard();

            // Loads Page
            PDPage page = doc.getPage(n);

            // Loads Icon File
            File iconDir = new File("C:/Users/micro/Desktop/FILES/");
            PDPageContentStream contents = new PDPageContentStream(doc, page, PDPageContentStream.AppendMode.APPEND, true, true);

            // Draws Icon
            int iAdder = 0;
            int jAdder = 0;

            // Iterates through each square, places each icon
            for (int i = 0; i < 5; i++) {
                for (int j = 0; j < 5; j++) {
                    PDImageXObject icon = PDImageXObject.createFromFile(iconDir + Integer.toString(cardNumbers[i][j]) + ".png", doc);
                    contents.drawImage(icon, 52 + iAdder, 79 + jAdder, 81, 81);

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

            contents.close();
        }
        doc.save("C:/Users/micro/Desktop/FILES/testCard.pdf");

        doc.close();
    }
}

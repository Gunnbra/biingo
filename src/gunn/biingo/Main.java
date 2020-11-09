package gunn.biingo;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
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
import javafx.stage.Stage;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.util.Random;

public class Main extends Application {
    File projectLocation = null;
    File tempLocation = null;

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        primaryStage.setTitle("BINGO Card Generator");

        //  windowMainMenu(primaryStage);

        windowCardCreation(primaryStage);
        // insertIcon();
    }

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
            popupStage.hide();
            windowCardCreation(primaryStage);
        });
        cancelButton.setOnAction(value -> {
            this.tempLocation = null;
            popupStage.hide();
        });
    }

    public void windowCardCreation(Stage primaryStage) {
        File[] tempIcons = new File[75]; //Temp icon locations
        final int[] counter = {0};

        // Scene Creations
        BorderPane borderLayout = new BorderPane();

        // Drop Box, TOP
        VBox dropVBox = new VBox();
        dropVBox.setStyle("-fx-border-style: dashed inside; -fx-border-width: 2; -fx-border-insets: 5; -fx-border-radius: 5; -fx-border-color: gray;");
        dropVBox.setMinHeight(100);
        dropVBox.setAlignment(Pos.CENTER);
        dropVBox.setSpacing(10);
        // Add Files Button
        Button buttonGetFiles = new Button("Choose Files");
        buttonGetFiles.setMinWidth(200);
        buttonGetFiles.setMinHeight(30);
        dropVBox.getChildren().add(buttonGetFiles);
        // Add Drop Text
        Text dropText = new Text("Or drop PNGs here");
        dropVBox.getChildren().add(dropText);
        // Set Vbox to layout
        borderLayout.setTop(dropVBox);

        // Preview FlowPane
        FlowPane previewFlowPane = new FlowPane();
        for (int i = 0; i < tempIcons.length; i++) {
          //  if(tempIcons[i] != null) {
                Image image = new Image(new File("C:/Users/micro/Desktop/TestEnvironment/icons/temp0.png").getAbsolutePath() /*tempIcons[i].getAbsolutePath()*/);
                ImageView imageView = new ImageView(image);
                previewFlowPane.getChildren().add(imageView);
          //  }
        }
        borderLayout.setCenter(previewFlowPane);


        // Setting Scene
        Scene bingoScene = new Scene(borderLayout, 800, 800);
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
        dropVBox.setOnDragDropped(new EventHandler<DragEvent>() {
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
                        //TODO move to creation of new project, or load
                        File tempProject = new File("C:/Users/micro/Desktop/Test Environment");
                        File iconDir = new File(tempProject.getAbsoluteFile() + "/icons");
                        iconDir.mkdirs();

                        for (int i = 0; i < files.length; i++) {
                            try {
                                File tempFile = new File(iconDir + "/temp" + counter[0] + ".png");
                                Files.copy(files[i].toPath(), tempFile.toPath());
                                counter[0]++;
                                tempIcons[counter[0]] = tempFile;
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                event.setDropCompleted(success);
                event.consume();
            }
        });
    }

    public static void main(String[] args) throws Exception {
        launch(args);
    }

    public static void insertIcon() throws Exception {
        // Loads Template Card File
        File file = new File("C:/Users/micro/Desktop/FILES/card.pdf");
        PDDocument doc = PDDocument.load(file);

        // Loads Page 1
        PDPage page = doc.getPage(0);

        // Loads Icon File
        PDImageXObject icon = PDImageXObject.createFromFile("C:/Users/micro/Desktop/FILES/snake.png", doc);
        PDPageContentStream contents = new PDPageContentStream(doc, page, PDPageContentStream.AppendMode.APPEND, true, true);

        // Draws Icon
        int iAdder = 0;
        int jAdder = 0;

        // Iterates through each square, places each icon
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
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

        doc.save("C:/Users/micro/Desktop/FILES/testCard.pdf");

        doc.close();
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
}

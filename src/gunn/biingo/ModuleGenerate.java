package gunn.biingo;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class ModuleGenerate {
    private final File projectDirectory;
    private ModuleDatabase moduleDatabase;

    private int gamesPerBooklet = 1;
    private int printedBooklets = 1;
    private boolean pageNumbers = false;
    private boolean dividePages = false;
    private Button buttonGenerate;
    private int nextId = 0;

    private Text bookText;
    private Text pageText;
    private Text timeText;
    private ProgressBar progressBar;
    private Thread thread;

    public ModuleGenerate(File projDir, ModuleDatabase modData) {
        projectDirectory = projDir;
        buttonGenerate = new Button("Generate");
        moduleDatabase = modData;
    }

    /**
     * Run generation tab
     */
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

    /**
     * Progress bar for generation popup
     */
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

    /**
     * Generates PDF of cards
     **/
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

        JSONObject tempDatabase = (JSONObject) moduleDatabase.getCardDatabase();
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
                moduleDatabase.setCardDatabase((JSONObject) tempDatabase);
                nextId = nextCardID;
            }
        }
        if (!dividePages) {
            doc.save(saveDir + "/Cards - " + RandomStringUtils.randomAlphanumeric(8) + ".pdf");
            doc.close();
            moduleDatabase.setCardDatabase((JSONObject) tempDatabase);
            nextId = nextCardID;
        }
        return null;
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
     * Adds card to temporary database, added to perm one later
     */
    public void addCardToDatabase(JSONObject tempDatabase, JSONObject card, String id) {
        tempDatabase.put(id, card.toString());
    }

    /**
     * Save Card database to perm database
     */
    public void saveCardDatabase() {
        try {
            FileWriter fileProps = new FileWriter(projectDirectory + "/" + "bingo.database");
            fileProps.write(moduleDatabase.getCardDatabase().toString());
            fileProps.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ------------------- Getters & Setters -----------------------------

    public int getGamesPerBooklet(){
        return gamesPerBooklet;
    }

    public void setGamesPerBooklet(int newNum){
        gamesPerBooklet = newNum;
    }

    public int getPrintedBooklets(){
        return printedBooklets;
    }

    public void setPrintedBooklets(int newNum){
        printedBooklets = newNum;
    }

    public boolean getPageNumbers() {
        return pageNumbers;
    }

    public void setPageNumbers(boolean newBool) {
        pageNumbers = newBool;
    }

    public boolean getDividePages() {
        return dividePages;
    }

    public void setDividePages(boolean newBool) {
        dividePages = newBool;
    }

    /**
     * Generate new id for card
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

    public int getNextId() {
        return nextId;
    }

    public void setNextId(int newID) {
        nextId = newID;
    }

    public void setButtonGenerateVisible(boolean newBool) {
        buttonGenerate.setDisable(newBool);
    }
}

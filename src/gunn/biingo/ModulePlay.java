package gunn.biingo;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import org.json.simple.parser.ParseException;

import java.io.File;

public class ModulePlay {
    private final File projectDirectory;
    private final ModuleDatabase moduleDatabase;
    private final ModuleLastCalled moduleLastCalled;
    private final ModuleScoreboard moduleScoreboard;
    private VBox callBox;

    private Button buttonReset;
    private Button buttonVerify;
    private Button buttonLastCalled;
    private Button buttonScoreboard;

    private boolean[] trackedNumbers = new boolean[76];

    public ModulePlay(File projDir, ModuleDatabase modData, ModuleLastCalled moduleLast, ModuleScoreboard moduleScore) {
        projectDirectory = projDir;
        moduleDatabase = modData;
        moduleLastCalled = moduleLast;
        moduleScoreboard = moduleScore;
    }

    public void tabPlayGame(Tab tab) {
        // Scene Creations
        BorderPane mainLayout = new BorderPane();

        // CENTER
        callBox = new VBox();
        callBox.setAlignment(Pos.CENTER);
        callBox.setSpacing(10);
        callBox.setPadding(new Insets(0, 0, 0, 0));

        // Enter numbers to call/uncall them
        TextField enterField = new TextField();
        enterField.setMaxWidth(50);
        enterField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue,
                                String newValue) {
                if (!newValue.matches("\\d*")) {
                    enterField.setText(newValue.replaceAll("[^\\d]", ""));
                }

                if (!newValue.equals("")) {
                    if ((Integer.parseInt(newValue) < 1) || (Integer.parseInt(newValue) > 75)) {
                        enterField.setText("");
                    }
                }
            }
        });
        enterField.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent ke) {
                if (ke.getCode().equals(KeyCode.ENTER)) {
                    if(!enterField.getText().equals("")) {
                        String numberName = enterField.getText();
                        if (Integer.parseInt(numberName) < 10) {
                            numberName = "0" + numberName;
                        }

                        CheckBox numBox = ((CheckBox)mainLayout.lookupAll("#checkbox_" + numberName).toArray()[0]);

                        if(!numBox.isSelected()) {
                            numBox.setSelected(true);
                        }

                        enterField.setText(""); // Clear
                    }
                }

                if (ke.getCode().equals(KeyCode.DELETE)) {
                    if(!enterField.getText().equals("")) {
                        String numberName = enterField.getText();
                        if (Integer.parseInt(numberName) < 10) {
                            numberName = "0" + numberName;
                        }

                        CheckBox numBox = ((CheckBox)mainLayout.lookupAll("#checkbox_" + numberName).toArray()[0]);

                        if(numBox.isSelected()) {
                            numBox.setSelected(false);
                        }

                        enterField.setText(""); // Clear
                    }
                }
            }
        });

        callBox.getChildren().add(enterField);

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
        optionBox.setSpacing(100);
        optionBox.setPadding(new Insets(10, 5, 10, 5));
        // Elements
        buttonReset = new Button("Reset Board");
        buttonVerify = new Button("Verification");
        buttonLastCalled = new Button("Last Called");
        buttonScoreboard = new Button("Scoreboard");
        optionBox.getChildren().add(buttonReset);
        optionBox.getChildren().add(buttonVerify);
        optionBox.getChildren().add(buttonLastCalled);
        optionBox.getChildren().add(buttonScoreboard);
        mainLayout.setBottom(optionBox);

        tab.setContent(mainLayout);

        // Listeners

        //Button View Cards
        buttonVerify.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try {
                    moduleDatabase.databasePopup();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });

        //Button Reset
        buttonReset.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                resetConfirmation();
            }
        });

        //Button LastValled
        buttonLastCalled.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                moduleLastCalled.lastCalledPopup();
            }
        });

        //Button Scoreboard
        buttonScoreboard.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                moduleScoreboard.scoreBoardPopup();
            }
        });
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

                int rawNumber = j + (i * 15);
                String numberName = Integer.toString(rawNumber);
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
                checkNum.setId("checkbox_" + numberName);
                String finalNumberName = numberName;
                checkNum.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue<? extends Boolean> observableValue, Boolean aBoolean, Boolean t1) {
                        int num = Integer.parseInt(finalNumberName);

                        trackedNumbers[num] = t1;
                        moduleDatabase.setTracked(trackedNumbers);
                        moduleDatabase.rerenderDatabaseCard();

                        if (t1) {
                            moduleLastCalled.addLastCalled(finalNumberName);
                            moduleScoreboard.addCalled(rawNumber);
                        } else {
                            moduleLastCalled.removeLastCalled(finalNumberName);
                            moduleScoreboard.removeCalled(rawNumber);
                        }

                        moduleLastCalled.rerenderLastCalled();
                        moduleScoreboard.renderPlayTracker();
                    }
                });

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

    public void resetConfirmation() {
        Stage resetStage = new Stage();
        resetStage.setTitle("Are you Sure?");
        resetStage.setResizable(false);
        BorderPane mainPane = new BorderPane();

        Text titleText = new Text("Are you sure you want to reset the board?");
        mainPane.setCenter(titleText);

        HBox optionBox = new HBox();
        optionBox.setSpacing(10);
        optionBox.setAlignment(Pos.CENTER);
        optionBox.setPadding(new Insets(10, 10, 10, 10));
        Button buttonConfirm = new Button("Confirm");
        Button buttonCancel = new Button("Cancel");
        optionBox.getChildren().add(buttonConfirm);
        optionBox.getChildren().add(buttonCancel);
        mainPane.setBottom(optionBox);

        Scene genScene = new Scene(mainPane, 300, 100);
        resetStage.setScene(genScene);
        resetStage.show();

        buttonCancel.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                resetStage.close();
            }
        });

        buttonConfirm.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                trackedNumbers = new boolean[76];
                moduleDatabase.setTracked(trackedNumbers);
                callBox.getChildren().remove(callBox.getChildren().size() - 1);
                callBox.getChildren().add(renderPlayTracker());
                moduleDatabase.rerenderDatabaseCard();

                moduleLastCalled.clear();
                moduleScoreboard.clear();

                resetStage.close();
            }
        });
    }

    public void setDisableButtonVerify(boolean bool) {
        buttonVerify.setDisable(bool);
    }

    public void setDisableButtonLastCalled(boolean bool) {
        buttonLastCalled.setDisable(bool);
    }

    public void setDisableButtonScoreboard(boolean bool) {
        buttonScoreboard.setDisable(bool);
    }
}

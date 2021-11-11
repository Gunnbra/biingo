package gunn.biingo;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;

/**
 * Created by BeardlessBrady on 2021-11-09 for Bingo
 * All Rights Reserved
 * https://github.com/Beardlessbrady/Bingo
 */
public class ModuleScoreboard {
    private final File iconDir;
    private final File projectDir;
    private BorderPane mainPane;

    private final LinkedList<Integer> calledNumbers = new LinkedList<Integer>();

    public ModuleScoreboard(File proj) {
        projectDir = proj;
        iconDir = new File(projectDir + "/" + "icons");
    }

    public void scoreBoardPopup() {
        Stage stage = new Stage();
        stage.setTitle("Scoreboard");

        mainPane = new BorderPane();
        mainPane.getChildren().add(new Text(" "));
        renderPlayTracker();

        Scene scene = new Scene(mainPane, 1780, 700);
        stage.setScene(scene);
      //  stage.setResizable(false);
        stage.show();
    }

    /**
     * Renders all possible numbers
     */
    public void renderPlayTracker() {
        // Main PlayBox
        VBox playBox = new VBox();
        playBox.setBackground(new Background(new BackgroundFill(Color.MAGENTA, CornerRadii.EMPTY, Insets.EMPTY)));
        playBox.setAlignment(Pos.CENTER);
        playBox.setSpacing(10);
        playBox.setPadding(new Insets(10, 10, 10, 10));

        // File Locations
        String[] fileList = iconDir.list();

        // Loops through B I N G O
        for (int i = 0; i < 5; i++) {
            HBox vBox = new HBox();
            vBox.setBackground(new Background(new BackgroundFill(Color.MAGENTA, CornerRadii.EMPTY, Insets.EMPTY)));
            vBox.setAlignment(Pos.CENTER);
            vBox.setPadding(new Insets(10, 10, 0, 10));

            // Looks through 1-15 of each letter
            for (int j = 1; j < 16; j++) {
                HBox numBox = new HBox();
                numBox.setAlignment(Pos.CENTER);
                numBox.setPadding(new Insets(0, 10, 0, 10));

                final File fileNum = new File(iconDir.getAbsolutePath() + "/" + "01.png");
                numBox.setStyle("-fx-border-style: solid inside; -fx-border-width: 1; -fx-border-insets: 5; -fx-border-radius: 5; -fx-border-color: black; " +
                        "-fx-background-color: white; -fx-background-radius: 5; -fx-background-insets: 5px");
                numBox.setPadding(new Insets(5, 5, 5, 5));
                numBox.setAlignment(Pos.CENTER);

                int number = j + (i * 15);
                String numberName = Integer.toString(number);
                if (Integer.parseInt(numberName) < 10) {
                    numberName = "0" + numberName;
                }

                File numFile = new File(iconDir.getAbsolutePath() + "/" + numberName + ".png");

                // If icon file of number exists, use element, if not use a large text version of number
                if (numFile.exists()) {
                    Image image = new Image("file:" + numFile);
                    ImageView imageView = new ImageView(image);
                    imageView.setFitHeight(90);
                    imageView.setFitWidth(90);
                    if(!calledNumbers.contains(number)) {
                        imageView.setOpacity(0.1); // Set opaque if NOT called
                    }
                    numBox.getChildren().add(imageView);
                } else {
                    Text t = new Text(numberName);
                    t.setStyle("-fx-font-size: 22; -fx-font-weight: bold");
                    numBox.getChildren().add(t);
                }

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

        VBox callBox = new VBox();
        callBox.setPadding(new Insets(20, 0, 0, 0));
        callBox.getChildren().add(calledBox);

        mainPane.setCenter(callBox);
    }

    public void addCalled(int i) {
        calledNumbers.add(i);
    }

    public void removeCalled(int i) {
        for (int j = 0; j < calledNumbers.size(); j++) {
            if (calledNumbers.get(j) == i) calledNumbers.remove(j);
        }
    }

    public void clear() {
        calledNumbers.clear();

        renderPlayTracker();
    }
}

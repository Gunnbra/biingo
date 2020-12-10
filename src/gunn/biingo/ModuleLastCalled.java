package gunn.biingo;

import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;

public class ModuleLastCalled {
    private final File iconDir;
    private final File projectDir;
    private ModulePlay modulePlay;

    private final LinkedList<String> lastCalled = new LinkedList<String>();
    private StackPane mainPane;

    public ModuleLastCalled(File proj) {
        projectDir = proj;
        iconDir = new File(projectDir + "/" + "icons");
    }

    public void lastCalledPopup() {
        Stage stage = new Stage();
        stage.setTitle("Last Called");

        mainPane = new StackPane();
        mainPane.getChildren().add(new Text(" "));

        rerenderLastCalled();

        Scene scene = new Scene(mainPane, 180, 450);
        stage.setScene(scene);
        stage.setResizable(false);

        stage.show();

        modulePlay.setDisableButtonLastCalled(true);
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                modulePlay.setDisableButtonLastCalled(false);
            }
        });
    }

    public void rerenderLastCalled() {
        if (mainPane != null) {
            if (lastCalled.size() > 0) {
                VBox pane = new VBox();
                pane.setStyle("-fx-background-color: transparent;");
                pane.setPadding(new Insets(10, 10, 10, 10));
                pane.setAlignment(Pos.CENTER);
                pane.setSpacing(10);

                String[] fileList = iconDir.list();
                if (fileList != null) {
                    String file;

                    for (int i = 1; i < 5; i++) {
                        if (lastCalled.size() >= i) {
                            file = lastCalled.get(lastCalled.size() - i) + ".png";
                            if (Arrays.asList(fileList).contains(file)) {
                                Image image = new Image("file:" + iconDir.getAbsolutePath() + "/" + file);
                                ImageView imageView = new ImageView(image);
                                imageView.setFitHeight(100);
                                imageView.setFitWidth(100);
                                pane.getChildren().add(imageView);
                            } else {
                                Text text = new Text(lastCalled.get(lastCalled.size() - i));
                                text.setStyle("-fx-font-size: 80px; -fx-font-weight: bold");
                                pane.getChildren().add(text);
                            }
                        }
                    }
                }

                if(mainPane.getChildren().size() >= 1) {
                    mainPane.getChildren().remove(0);
                }
                mainPane.getChildren().add(pane);
            } else {
                if(mainPane.getChildren().size() >= 1) {
                    mainPane.getChildren().remove(0);
                }
            }
        }
    }

    public void addLastCalled(String i) {
        lastCalled.add(i);

        if (lastCalled.size() > 10) {
            lastCalled.remove();
        }
    }

    public void removeLastCalled(String i) {
        for (int j = 0; j < lastCalled.size(); j++) {
            if (lastCalled.get(j) == i) lastCalled.remove(j);
        }
    }

    public void clear() {
        lastCalled.clear();

        rerenderLastCalled();
    }

    public void setModulePlay(ModulePlay modPlay) {
        modulePlay = modPlay;
    }
}

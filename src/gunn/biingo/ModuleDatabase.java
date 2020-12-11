package gunn.biingo;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.io.File;

public class ModuleDatabase {
    private final File projectDirectory;
    private ModulePlay modulePlay;
    private HBox databasePane;
    private VBox listPane;
    private JSONObject cardDatabase = new JSONObject();
    private ListView<String> cardList;
    private boolean showNums = false;
    private boolean[] tracked = new boolean[76];

    public ModuleDatabase(File projDir) {
        projectDirectory = projDir;
    }

    /**
     * Runs popup
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
        listPane.setStyle("-fx-background-color: #e65650");
        // Elements
        Text textTitle = new Text("Bingo Cards");
        textTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-fill: white");
        listPane.getChildren().add(textTitle);
        // Card List Element
        cardList = new ListView<String>();
        ObservableList<String> cardItems = FXCollections.observableArrayList();
        for (Object o : cardDatabase.keySet()) {
            cardItems.add((String) o);
        }
        FXCollections.sort(cardItems);
        cardList.setItems(cardItems);
        listPane.getChildren().add(cardList);
        // Checkbox
        CheckBox checkShowNum = new CheckBox("Show Numbers");
        checkShowNum.setStyle("-fx-font-weight: bold; -fx-fill: white");
        listPane.getChildren().add(checkShowNum);
        // SearchBar
        TextField searchField = new TextField();
        listPane.getChildren().add(searchField);
        // Buttons
        // TODO DELETE BUTTON
       // Button buttonDelete = new Button("DELETE Card Database");
       // listPane.getChildren().add(buttonDelete);
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

        checkShowNum.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observableValue, Boolean aBoolean, Boolean t1) {
                showNums = t1;
                try {
                    rerenderDatabaseCard(cardList.getSelectionModel().getSelectedItem());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });

        // Disable button in Play Module
        modulePlay.setDisableButtonVerify(true);
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                modulePlay.setDisableButtonVerify(false);
            }
        });
    }

    /**
     * Rerender cardview for the database window
     */
    public void rerenderDatabaseCard() {
        try {
            if(cardList != null) {
                rerenderDatabaseCard(cardList.getSelectionModel().getSelectedItem());
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
    public void rerenderDatabaseCard(String cardID) throws ParseException {
        File templateDir = new File(projectDirectory + "/templates");
        File iconDir = new File(projectDirectory + "/icons");

        // Card pane
        StackPane stackPane = new StackPane();

        Image card = new Image("file:" + templateDir + "/" + "01.png");
        if(cardID != null) {
           String template = cardID.substring(cardID.length() - 2);

           if(new File(templateDir + "/" + template + ".png").exists()){
               card = new Image("file:" + templateDir + "/" + template + ".png");
           }
        }

        ImageView cardView = new ImageView(card);
        cardView.setFitHeight(600);
        cardView.setFitWidth(500);
        stackPane.getChildren().add(cardView);

        // Dabbed Icon
        Image dab = new Image(getClass().getResource("/assets/dab.png").toExternalForm());
        ImageView dabView;


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
                        }

                        if (!slotIcon.exists() || showNums) {
                            Text text = new Text(slotNum);
                            text.setStyle("-fx-font-size: 20px; -fx-font-weight: bold");
                            text.setTranslateX(-175 + (i * 87));
                            text.setTranslateY(210 - (j * 80));
                            stackPane.getChildren().add(text);
                        }

                        // If dabbed, show on card
                        if(tracked[Integer.parseInt(slotNum)]) {
                            // Dabbed Icon
                            dabView = new ImageView(dab);
                            dabView.setFitWidth(60);
                            dabView.setFitHeight(60);
                            dabView.setTranslateX(-175 + (i * 87));
                            dabView.setTranslateY(210 - (j * 80));
                            stackPane.getChildren().add(dabView);
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

    public JSONObject getCardDatabase() {
        return cardDatabase;
    }

    public void setCardDatabase(JSONObject cardDatabase) {
        this.cardDatabase = cardDatabase;
    }

    public void setTracked(boolean[] track){
        tracked = track;
    }

    public void setModulePlay(ModulePlay modPlay) {
        modulePlay = modPlay;
    }
}

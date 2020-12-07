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
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.io.File;

public class ModuleDatabase {
    private final File projectDirectory;
    private HBox databasePane;
    private VBox listPane;
    private JSONObject cardDatabase = new JSONObject();

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

    public JSONObject getCardDatabase() {
        return cardDatabase;
    }

    public void setCardDatabase(JSONObject cardDatabase) {
        this.cardDatabase = cardDatabase;
    }

}

package gunn.biingo;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class Main extends Application {
    private Stage primaryStage = null;
    private File projectDirectory = null;

    private ModuleDatabase moduleDatabase;
    private ModuleGenerate moduleGenerate;
    private  ModuleCardEditor moduleCardEditor;
    private ModuleBookletEditor moduleBookletEditor;
    private ModulePlay modulePlay;
    private ModuleLastCalled moduleLastCalled;

    public static void main(String[] args) throws Exception {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        primaryStage.setTitle("BINGO Card Generator V1.1.3");
        this.primaryStage = primaryStage;

        windowMainMenu();
    }

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
        Text copy = new Text("V1.1.3 - Copyright Brady Gunn 2020. All rights reserved");
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
        primaryStage.getIcons().add(image);
        primaryStage.setResizable(true);
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

    public void windowProject() {
        moduleGenerate.saveToProperties();

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        Tab tabNumEditor = new Tab("Icon Editor");
        Tab tabBookletEditor = new Tab("Booklet Editor");
        Tab tabGeneration = new Tab("Generate Cards");
        Tab tabPlay = new Tab("Play Tracker");

        tabPane.getTabs().add(tabNumEditor);
        tabPane.getTabs().add(tabBookletEditor);
        tabPane.getTabs().add(tabGeneration);
        tabPane.getTabs().add(tabPlay);

        moduleCardEditor.tabCardEditor(tabNumEditor);
        moduleBookletEditor.tabBookletEditor(tabBookletEditor);
        moduleGenerate.tabGenerate(tabGeneration);
        modulePlay.tabPlayGame(tabPlay);

        // Setting Scene
        Scene bingoScene = new Scene(tabPane, 750, 550);
        primaryStage.setScene(bingoScene);
        primaryStage.show();
    }

    private void createModules() {
        moduleDatabase = new ModuleDatabase(projectDirectory);
        moduleGenerate = new ModuleGenerate(projectDirectory, moduleDatabase);
        moduleCardEditor = new ModuleCardEditor(projectDirectory, primaryStage);
        moduleBookletEditor = new ModuleBookletEditor(projectDirectory, primaryStage, moduleGenerate);
        moduleLastCalled = new ModuleLastCalled(projectDirectory);
        modulePlay = new ModulePlay(projectDirectory, moduleDatabase, moduleLastCalled);
        moduleLastCalled.setModulePlay(modulePlay);
        moduleDatabase.setModulePlay(modulePlay);
    }

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

            createModules();
            windowProject();
        } else {
            warningPopup("PROJECT DIRECTORY DOESNT EXIST - createProjectDirectory()");
        }
    }

    public void loadProjectDirectory(File file) {
        boolean success = true;

        if (file.exists()) {
            projectDirectory = file;
        } else {
            success = false;
        }

        // Create Modules
        createModules();

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
            moduleGenerate.setGamesPerBooklet(Integer.parseInt(jo.get("games").toString()));
        }

        if (jo.get("print") != null) {
            moduleGenerate.setPrintedBooklets(Integer.parseInt(jo.get("print").toString()));
        }

        if (jo.get("pageNum") != null) {
            moduleGenerate.setPageNumbers(jo.get("pageNum").toString().equals("true"));
        }

        if (jo.get("divide") != null) {
            moduleGenerate.setDividePages(jo.get("divide").toString().equals("true"));
        }

        if (jo.get("nextid") != null) {
            moduleGenerate.setNextId(Integer.parseInt(jo.get("nextid").toString()));
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
            moduleDatabase.setCardDatabase(joo);
        }

        if (success) {
            windowProject();
        } else {
            warningPopup("PROJECT DIRECTORY IS INVALID - loadProjectDirectory()");
        }
    }

    public static void warningPopup(String warning) {
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
package gunn.biingo;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        primaryStage.setTitle("Hello World");
        primaryStage.setScene(new Scene(root, 300, 275));
        primaryStage.show();

        insertIcon();
    }


    public static void main(String[] args) throws Exception {
        launch(args);
    }

    public static void insertIcon() throws Exception {
        File file = new File("C:/Users/micro/Desktop/FILES/card.pdf");

        System.out.println(file.exists());
        PDDocument doc = PDDocument.load(file);

        PDPage page = doc.getPage(0);

        PDImageXObject icon = PDImageXObject.createFromFile("C:/Users/micro/Desktop/FILES/Turtle.png", doc);

        PDPageContentStream contents = new PDPageContentStream(doc, page, PDPageContentStream.AppendMode.APPEND, true, true);

        contents.drawImage(icon, 0, 0, 100, 100);

        contents.close();

        doc.save("C:/Users/micro/Desktop/FILES/testCard.pdf");

        doc.close();
    }
}

package gunn.biingo;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.io.File;

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

        for(int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                contents.drawImage(icon, 52 + iAdder, 79 + jAdder, 81, 81);

                // Needs to vary movement every 2nd square
                if (j == 0 || j % 2 == 0) {
                    jAdder += 105;
                } else {
                    jAdder += 106;
                }
            }
            jAdder = 0;

            // Needs to vary movement every 2nd square
            if (i == 0 || i % 2 == 0) {
                iAdder += 105;
            } else {
                iAdder += 106;
            }
        }


        contents.close();

        doc.save("C:/Users/micro/Desktop/FILES/testCard.pdf");

        doc.close();
    }
}

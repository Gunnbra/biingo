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
import java.util.Random;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        primaryStage.setTitle("Hello World");
        primaryStage.setScene(new Scene(root, 300, 275));
        primaryStage.show();

        outputCard(randomizeCard());


        // insertIcon();
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

package app.View;

import app.MainApp;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.BorderPane;

import java.io.IOException;

public class MainWindow extends BorderPane {

    public static MainWindow init() throws IOException {
        MainWindow mainWindow = null;
            FXMLLoader loader = MainApp.getResourceLoader("/MainWindow/RootLayout.fxml");
            mainWindow = loader.load();

        return mainWindow;
    }

}

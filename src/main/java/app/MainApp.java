package app;

import app.FXMLController.*;
import app.Model.Structures.Picture.Picture;
import app.Model.Structures.Mediator;
import app.Model.Structures.WindowElements.PaintPane;
import app.Model.Structures.WindowElements.PicEditPane;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MainApp extends Application {
    private Stage primaryStage;
    private Scene mainScene;
    private BorderPane mainWindow;
    private TabPane mainFrame;
    private Picture activePicture = null;
    private Mediator mediator;
    private FunctionBarController functionBarController;
    private MenuBarController menuBarController;
    private EditPictureController editPictureController;
    private HashMap<Integer, Integer> drawOrder;
    private int scale = 1;

    public static void main(String[] args) {
        launch(args);
    }

    public static FXMLLoader getResourceLoader(String path) {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(MainApp.class.getResource(path));
        return loader;
    }

    @Override
    public void start(Stage primaryStage) {
        initPrimaryStage(primaryStage);
        initRootLayout();
        initMenuBar();
        initFunctionBar();
        initMainFrame();
        this.primaryStage.setMinHeight(700);
        this.primaryStage.setMinWidth(900);
        drawOrder = new HashMap<>();
        mediator = new Mediator(this, functionBarController, menuBarController);
    }

    private void initPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("Java Paint");
        this.primaryStage.setMaximized(false);
    }

    private void initRootLayout() {
        try {
            FXMLLoader loader = getResourceLoader("/MainWindow/RootLayout.fxml");
            mainWindow = loader.load();
            mainScene = new Scene(mainWindow);
            primaryStage.setWidth(900);
            primaryStage.setHeight(650);
            primaryStage.setScene(mainScene);
            primaryStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initMainFrame() {
        try {
            FXMLLoader loader = getResourceLoader("/MainWindow/MainFrame.fxml");
            mainFrame = loader.load();
            mainWindow.setCenter(mainFrame);
            mainFrame.setBackground(new Background(new BackgroundFill(Color.rgb(50, 50, 50), CornerRadii.EMPTY, Insets.EMPTY)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initFunctionBar() {
        try {
            FXMLLoader loader = getResourceLoader("/MainWindow/FunctionBar.fxml");
            AnchorPane functionBar = loader.load();
            functionBar.prefHeightProperty().bind(mainWindow.heightProperty());
            mainWindow.setLeft(functionBar);
            initFunctionBarController(loader);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initMenuBar() {
        try {
            FXMLLoader loader = getResourceLoader("/MainWindow/MenuBar.fxml");
            MenuBar menuBar = loader.load();
            mainWindow.setTop(menuBar);
            initMenuBarController(loader);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getScale() {
        return scale;
    }

    public void setScale(String scale) {
        this.scale = Integer.parseInt(scale);
    }

    public Picture getActivePicture() {
        return activePicture;
    }

    public void setActivePicture(Picture picture) {
        activePicture = picture;
    }

    public PaintPane getActivePaintPane() {
        ScrollPane n = (ScrollPane) mainFrame.getSelectionModel().getSelectedItem().getContent();
        return (PaintPane) n.getContent();
    }

    public void addNewTab(Pane pane, String tabName) {
        Tab tab = new Tab();
        tab.setText(tabName);
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.prefViewportHeightProperty().bind(mainFrame.heightProperty());
        scrollPane.prefViewportWidthProperty().bind(mainFrame.widthProperty());
        scrollPane.setContent(pane);
        tab.setContent(scrollPane);
        mainFrame.getTabs().add(tab);
        mainFrame.getSelectionModel().select(tab);
    }

    public void showAlert(String text, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setHeaderText("");
        alert.setContentText(text);
        alert.initOwner(primaryStage);
        alert.showAndWait();
    }

    public File showLoadDialog(String windowTitle, String extInfo, String ext) {
        FileChooser fileChooser = createFileChooser(extInfo, ext);
        Stage stage = new Stage();
        stage.setTitle(windowTitle);
        stage.setResizable(false);
        return fileChooser.showOpenDialog(stage);
    }

    public File showSaveDialog(String windowTitle, String extInfo, String ext) {
        FileChooser fileChooser = createFileChooser(extInfo, ext);
        Stage stage = new Stage();
        stage.setTitle(windowTitle);
        stage.setResizable(false);
        return fileChooser.showSaveDialog(stage);
    }

    private FileChooser createFileChooser(String extInfo, String ext) {
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(extInfo, ext);
        fileChooser.getExtensionFilters().add(extFilter);
        return fileChooser;
    }

    public void showEditPicture(Picture picture) {
        try {
            FXMLLoader paneLoader = getResourceLoader("/MainWindow/EditPicture.fxml");
            FXMLLoader colorPickerLoader = getResourceLoader("/MainWindow/ColorBar.fxml");
            BorderPane editPicture = paneLoader.load();
            AnchorPane colorPicker = colorPickerLoader.load();
            PicEditPane picEditPane = new PicEditPane(picture);
            editPicture.setRight(colorPicker);
            editPicture.setCenter(picEditPane);
            Scene picEditScene = new Scene(editPicture);
            initColorPickerController(colorPickerLoader, picEditPane);
            initEditPictureController(paneLoader, picEditPane);
            primaryStage.setScene(picEditScene);
            BorderPane.setMargin(colorPicker, new Insets(0, 10, 0, 0));
            editPictureController.setPicture(picture);
        } catch (IOException e) {
            System.err.print("IOexception!");
            e.printStackTrace();
        }
    }

    public void showMainWindow() {
        primaryStage.setScene(mainScene);
    }

    private void initEditPictureController(FXMLLoader loader, PicEditPane picEditPane) {
        editPictureController = loader.getController();
        editPictureController.setMediator(mediator);
        editPictureController.setPicEditPane(picEditPane);
    }

    private void initColorPickerController(FXMLLoader loader, PicEditPane picEditPane) {
        ColorBarController colorBarController = loader.getController();
        colorBarController.setMediator(mediator);
        colorBarController.setPicEditPane(picEditPane);
    }

    public void showNewCanvasPopup() {
        try {
            FXMLLoader loader = getResourceLoader("/Popups/NewCanvasPopup.fxml");
            Pane newCanvasPopup = loader.load();
            Scene scene = new Scene(newCanvasPopup);
            Stage stage = new Stage();
            stage.setTitle("New Canvas");
            stage.setResizable(false);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(primaryStage);
            stage.setScene(scene);
            NewCanvasController newCanvasController = loader.getController();
            newCanvasController.setDialogStage(stage);
            newCanvasController.setMediator(mediator);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showNewPicturePopup() {
        try {
            FXMLLoader loader = getResourceLoader("/Popups/NewPicturePopup.fxml");
            AnchorPane newPicturePopup = loader.load();
            Scene scene = new Scene(newPicturePopup);
            Stage stage = new Stage();
            stage.setTitle("New Picture");
            stage.setResizable(false);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(primaryStage);
            stage.setScene(scene);
            NewPictureController newPictureController = loader.getController();
            newPictureController.setDialogStage(stage);
            newPictureController.setMediator(mediator);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showEditDrawOrderPopup() {
        try {
            FXMLLoader loader = getResourceLoader("/Popups/EditDrawOrderPopup.fxml");
            AnchorPane editDrawOrderPopup = loader.load();
            Scene scene = new Scene(editDrawOrderPopup);
            Stage stage = new Stage();
            stage.setTitle("Edit draw order");
            stage.setResizable(false);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(primaryStage);
            stage.setScene(scene);
            EditDrawOrderController editDrawOrderController = loader.getController();
            editDrawOrderController.setDialogStage(stage);
            editDrawOrderController.setMediator(mediator);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void initFunctionBarController(FXMLLoader loader) {
        functionBarController = loader.getController();
        functionBarController.setMediator(mediator);
    }

    private void initMenuBarController(FXMLLoader loader) {
        menuBarController = loader.getController();
        menuBarController.setMediator(mediator);
    }

    public void updateDrawOrder(Map<Integer, Integer> drawOrder, boolean removeOld) {
        if (removeOld) {
            this.drawOrder.clear();
            this.drawOrder.putAll(drawOrder);
        } else {
            this.drawOrder.putAll(drawOrder);
        }
        updateLayers();
    }

    public Map<Integer, Integer> getDrawOrder() {
        return drawOrder;
    }

    private void updateLayers() {
        for (Tab t : mainFrame.getTabs()) {
            ScrollPane s = (ScrollPane) t.getContent();
            PaintPane p = (PaintPane) s.getContent();
            p.updateLayers();
        }
    }

}

package app.Model.Structures;

import app.FXMLController.ColorBarController;
import app.FXMLController.FunctionBarController;
import app.FXMLController.MenuBarController;
import app.MainApp;
import app.Model.Structures.Picture.Picture;
import app.Model.Structures.WindowElements.PaintPane;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

public class Mediator {
    private MainApp mainApp;
    private FunctionBarController functionBarController;
    private ColorBarController colorBarController;

    public Mediator(MainApp mainApp, FunctionBarController functionBarController, MenuBarController menuBarController) {
        this.mainApp = mainApp;
        this.functionBarController = functionBarController;
        functionBarController.setMediator(this);
        menuBarController.setMediator(this);
    }

    public void setColorBarController(ColorBarController colorBarController) {
        this.colorBarController = colorBarController;
    }

    public void addNewTab(int width, int height, Color color, String name) {
        PaintPane paintPane = new PaintPane(this, width, height, color);
        mainApp.addNewTab(paintPane, name);
    }

    public void addNewTab(Image bg, String name) {
        PaintPane paintPane = new PaintPane(this, bg);
        mainApp.addNewTab(paintPane, name);
    }

    public Picture getActivePicture() {
        return mainApp.getActivePicture();
    }

    public File showLoadPicturesDialog() {
        return mainApp.showLoadDialog("Load pictures", "TXT files (*.txt)", "*.txt");
    }

    public void setActivePicture(Picture picture) {
        mainApp.setActivePicture(picture);
    }

    public void showNewCanvasPopup() {
        mainApp.showNewCanvasPopup();
    }

    public void clearActiveCanvas() {
        mainApp.getActivePaintPane().clear();
    }

    public void updateDrawOrder(Map<Integer, Integer> drawOrder, boolean removeOld) {
        mainApp.updateDrawOrder(drawOrder, removeOld);
    }

    public Map<Integer, Integer> getDrawOrder() {
        return mainApp.getDrawOrder();
    }

    public void editPicture(Picture p) {
        mainApp.showEditPicture(p);
    }

    public void closeEditing() {
        mainApp.showMainWindow();
    }

    public Map<String, Picture> getPictures() {
        return functionBarController.getPictures();
    }

    public void showAlert(String text,Alert.AlertType alertType) {
        mainApp.showAlert(text,alertType);
    }

    public void setScale(String scale) {
        mainApp.setScale(scale);
    }

    public int getScale() {
        return mainApp.getScale();
    }

    public LinkedHashMap<String, Integer> getEditedColors() {
        return colorBarController.getEditedColors();
    }

    public void synchronizeTables() {
        functionBarController.synchronizeTables();
    }

    public File showSavePicturesDialog() {
        return mainApp.showSaveDialog("Select path for pictures", "TXT files (*.txt)", "*.txt");
    }

    public File showSaveCanvasDialog() {
        return mainApp.showSaveDialog("Select path for canvas", "PNG files(*.png)", "*.png");
    }

    public Image getCanvasSnapshot() {
        return mainApp.getActivePaintPane().getSnapshot();
    }

    public File showLoadCanvasDialog() {
        return mainApp.showLoadDialog("Select Image", "PNG files(*.png)", "*.png");
    }

    public void showNewPicturePopup() {
        mainApp.showNewPicturePopup();
    }

    public void deleteActivePicture() {
        functionBarController.deletePicture(getActivePicture());
    }

    public void editDrawOrder() {
        mainApp.showEditDrawOrderPopup();
    }
}

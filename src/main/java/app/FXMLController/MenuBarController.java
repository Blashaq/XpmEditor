package app.FXMLController;

import app.Model.Structures.Picture.Picture;
import app.Model.Structures.Mediator;
import app.Utils.PictureTools.PictureSaver;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import org.javatuples.Pair;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Map;


public class MenuBarController {
    private Mediator mediator;

    public void setMediator(Mediator mediator) {
        this.mediator = mediator;
    }

    @FXML
    public void handleNewCanvas() {
        mediator.showNewCanvasPopup();
    }

    @FXML
    public void handleClearCanvas() {
        mediator.clearActiveCanvas();
    }

    @FXML
    public void handleEditPicture() {
        if (mediator.getActivePicture() == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Wybierz obrazek do edycji!");
            alert.showAndWait();
            return;
        }
        mediator.editPicture(mediator.getActivePicture());
    }

    @FXML
    private void handleSavePictures() {
        File file;
        try {
            file = mediator.showSavePicturesDialog();
            Pair<ArrayList<? extends Picture>, Map<Integer, Integer>> picData = Pair.with(new ArrayList<>(mediator.getPictures().values()), mediator.getDrawOrder());
            PictureSaver.saveAsNewFile(file, picData);
            mediator.showAlert("Zapisano obrazki!", Alert.AlertType.CONFIRMATION);
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSaveCanvas() {
        File file;
        try {
            file = mediator.showSaveCanvasDialog();
            BufferedImage bi = SwingFXUtils.fromFXImage(mediator.getCanvasSnapshot(), null);
            ImageIO.write(bi, "png", file);
            mediator.showAlert("Zapisano plik!", Alert.AlertType.CONFIRMATION);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleOpenFile() {
        File file;
        file = mediator.showLoadCanvasDialog();
        if(file==null)
        	return;
        Image image = new Image(file.toURI().toString());
        mediator.addNewTab(image, file.getName());
    }

    @FXML
    private void handleEditDrawOrder() {
        mediator.editDrawOrder();
    }
    @FXML
    private void handleDeletePicture(){
        mediator.deleteActivePicture();
    }
    @FXML
    private void handleNewPicture(){
        mediator.showNewPicturePopup();
    }
}

package app.FXMLController;

import app.Model.ObjectFactories.PictureFactory;
import app.Model.Structures.Picture.Picture;
import app.Model.Structures.Mediator;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Stage;

public class NewPictureController {
    private Mediator mediator;
    private Stage dialogStage;
    @FXML
    private ToggleGroup charsPerColorGroup;
    @FXML
    private ToggleGroup picTypeGroup;
    @FXML
    private TextField widthTextField;
    @FXML
    private TextField heightTextField;
    int charsPerColor = 1;
    private final int maxSize = 40;
    private String picType = "pointpicture";

    @FXML
    public void initialize() {
        forceNumericInput(widthTextField);
        forceNumericInput(heightTextField);
        forceLowerThanXInput(widthTextField, maxSize);
        forceLowerThanXInput(heightTextField, maxSize);
        picTypeGroup.selectedToggleProperty().addListener(((observable, oldValue, newValue) -> {
            picType = picTypeGroup.getSelectedToggle().getUserData().toString();
        }));
        charsPerColorGroup.selectedToggleProperty().addListener(((observable, oldValue, newValue) ->
                charsPerColor = Integer.parseInt(charsPerColorGroup.getSelectedToggle().getUserData().toString())));
    }

    public void setMediator(Mediator mediator) {
        this.mediator = mediator;
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    private void forceNumericInput(TextField textField) {
        textField.textProperty().addListener(((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) textField.setText(newValue.replaceAll("[^\\d]", ""));
        }));
    }

    private void forceLowerThanXInput(TextField textField, int maxSize) {
        textField.textProperty().addListener((observable -> {
            try {
                if (Integer.parseInt(textField.getText()) > maxSize) {
                    //     mediator.showAlert("Maksymalny rozmiar to " + maxSize + " !");
                    textField.setText(Integer.toString(maxSize));
                }
            } catch (NumberFormatException ignored) {
            }
        }));
    }

    @FXML
    private void handleOkButton() {
        try {
            int type = 0;
            int width = Integer.parseInt(widthTextField.getText());
            int height = Integer.parseInt(heightTextField.getText());
            if(width==0||height==0)
                throw new NumberFormatException("width/height==0!");
            while (mediator.getPictures().containsKey(Picture.generateTypeSubtype(type, null)))
                type++;
            mediator.editPicture(PictureFactory.getEmptyPicture(picType, type, null, width, height, charsPerColor));
            dialogStage.close();
        } catch (NumberFormatException e) {
            mediator.showAlert("Niepoprawne dane!", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleCancelButton() {
        dialogStage.close();
    }

}

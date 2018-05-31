package app.FXMLController;

import app.Model.Structures.Mediator;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;


public class NewCanvasController {

    @FXML
    private TextField canvasNameField;
    @FXML
    private TextField canvasWidthField;
    @FXML
    private TextField canvasHeightField;
    @FXML
    private ColorPicker colorPicker;

    private Stage dialogStage;
    private Mediator mediator;

    public void setMediator(Mediator mediator) {
        this.mediator=mediator;
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void initialize() {
        forceNumericInput(canvasHeightField);
        forceNumericInput(canvasWidthField);
    }

    @FXML
    private boolean handleOkButton() {
        if (!checkInput()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Bledna nazwa!");
            return false;
        }
        int width = Integer.parseInt(canvasWidthField.getText());
        int height = Integer.parseInt(canvasHeightField.getText());
        mediator.addNewTab(width,height,colorPicker.getValue(), canvasNameField.getText());
        dialogStage.close();
        return true;
    }

    @FXML
    private void handleCancelButton() {
        dialogStage.close();
    }

    private boolean checkInput() {
        try {
            return canvasNameField.getText().trim().length() > 0 && Integer.parseInt(canvasHeightField.getText()) > 0 && Integer.parseInt(canvasWidthField.getText()) > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void forceNumericInput(TextField textField) {
        textField.textProperty().addListener(((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) textField.setText(newValue.replaceAll("[^\\d]", ""));
        }));
    }
}
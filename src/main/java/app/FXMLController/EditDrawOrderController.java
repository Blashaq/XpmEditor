package app.FXMLController;

import app.Model.Structures.Mediator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.javatuples.Pair;

import java.util.LinkedHashMap;
import java.util.Map;

public class EditDrawOrderController {
    @FXML
    private TableView drawOrderTable;
    @FXML
    TextField typeTextField;
    @FXML
    TextField priorityTextField;

    private Stage dialogStage;
    private Mediator mediator;
    private Map<Integer, Integer> drawOrder;

    @FXML
    private void initialize() {
        drawOrder = new LinkedHashMap<>();
        addRowClickListener(drawOrderTable);
        forceHexInput(typeTextField);
        forceNumericInput(priorityTextField);
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setMediator(Mediator mediator) {
        this.mediator = mediator;
        initTable(mediator.getDrawOrder());
    }

    private void initTable(Map<Integer, Integer> drawOrder) {
        for (Integer key : drawOrder.keySet()) {
            this.drawOrder.put(key, drawOrder.get(key));
        }
        ObservableList<TableColumn> columns = drawOrderTable.getColumns();
        columns.get(0).setCellValueFactory(new PropertyValueFactory<Pair<String, Integer>, String>("Value0"));
        columns.get(1).setCellValueFactory(new PropertyValueFactory<Pair<String, Integer>, Integer>("Value1"));
        updateTable();
    }

    private void updateTable() {
        ObservableList<Pair<String, Integer>> drawOrderList = FXCollections.observableArrayList();
        for (Map.Entry<Integer, Integer> entry : drawOrder.entrySet()) {
            drawOrderList.add(Pair.with(Integer.toHexString(entry.getKey()), entry.getValue()));
        }
        drawOrderTable.setItems(drawOrderList);
    }

    private void addRowClickListener(TableView table) {
        table.setRowFactory(tv -> {
            TableRow<Pair<String, Integer>> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty()) {
                    Pair<String, Integer> order = row.getItem();
                    typeTextField.setText(order.getValue0());
                    priorityTextField.setText(order.getValue1().toString());
                }
            });
            return row;
        });
    }

    private void forceHexInput(TextField textField) {
        textField.textProperty().addListener(((observable, oldValue, newValue) -> {
            if (!newValue.matches("/[0-9a-fA-F]*")) textField.setText(newValue.replaceAll("[^0-9a-fA-F]", ""));
        }));
    }

    private void forceNumericInput(TextField textField) {
        textField.textProperty().addListener(((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) textField.setText(newValue.replaceAll("[^\\d]", ""));
        }));
    }

    private boolean checkInput(TextField textField) {
        boolean itWorked = false;
        try {
            Integer.parseInt(textField.getText(), 16);
            itWorked = true;
        } catch (NumberFormatException ignored) {
        }
        return itWorked;
    }

    @FXML
    private void handleAddButton() {
        if (checkInput(typeTextField) && checkInput(priorityTextField)) {
            drawOrder.put(Integer.parseInt(typeTextField.getText(), 16), Integer.parseInt(priorityTextField.getText()));
            updateTable();
        }
    }

    @FXML
    private void handleDeleteButton() {
        if (checkInput(typeTextField)) {
            drawOrder.remove(Integer.parseInt(typeTextField.getText(), 16));
            updateTable();
        }
    }

    @FXML
    private void handleOkButton() {
        mediator.updateDrawOrder(drawOrder, true);
        mediator.showAlert("Zapisano!", Alert.AlertType.CONFIRMATION);
        dialogStage.close();
    }

    @FXML
    private void handleCancelButton() {
        dialogStage.close();
    }
}

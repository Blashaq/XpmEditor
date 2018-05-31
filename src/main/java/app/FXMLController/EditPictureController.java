package app.FXMLController;

import app.Model.Structures.Picture.LinePicture;
import app.Model.Structures.Picture.Picture;
import app.Model.Structures.Picture.PointPicture;
import app.Model.Structures.Picture.PolygonPicture;
import app.Model.Structures.Mediator;
import app.Model.Structures.WindowElements.PicEditPane;
import app.Utils.ArrayTools.ArrayTools;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.javatuples.Pair;

import java.util.*;

public class EditPictureController {
    @FXML
    private TableView textsTable;
    @FXML
    private TextField typeTextField;
    @FXML
    private TextField subTypeTextField;
    @FXML
    private TextField textKeyTextField;
    @FXML
    private TextField textTextField;
    private Picture picture;
    private Mediator mediator;
    private Pair<Integer, String> editedText;
    private int type;
    private Integer subType;
    private String typeSubType;
    private ObservableMap<Integer, String> textsMap;
    private PicEditPane picEditPane;

    @FXML
    public void initialize() {
        subTypeTextField.focusedProperty().addListener(((observable, oldValue, newValue) -> {
            if (oldValue && checkTypesEdition()) {
                editTypes(typeTextField.getText(), subTypeTextField.getText());
            }
        }));
        typeTextField.focusedProperty().addListener(((observable, oldValue, newValue) -> {
            if (oldValue && checkTypesEdition()) {
                editTypes(typeTextField.getText(), subTypeTextField.getText());
            }
        }));
        textKeyTextField.focusedProperty().addListener(((observable, oldValue, newValue) -> {
            if (oldValue && checkStringEdition()) {
                editText(textKeyTextField.getText(), textTextField.getText());
            }
        }));
        textTextField.focusedProperty().addListener(((observable, oldValue, newValue) -> {
            if (oldValue && checkStringEdition()) {
                editText(textKeyTextField.getText(), textTextField.getText());
            }
        }));
        forceHexInput(typeTextField);
        forceHexInput(textKeyTextField);
    }

    private void editTypes(String newType, String newSubType) {
        type = Integer.parseInt(newType, 16);
        try {
            subType = Integer.parseInt(newSubType, 16);
        } catch (NumberFormatException e) {
            System.err.println("nie udalo sie sparsowac subtypu, czyżby None?");
            subTypeTextField.setText("None");
            subType = null;
        }
        typeSubType = Picture.generateTypeSubtype(type, subType);
    }

    private void editText(String keyString, String newText) {
        int key = Integer.parseInt(keyString, 16);
        textsMap.remove(editedText.getValue0());
        textsMap.put(key, newText);
        updateTextsTable();
        editedText = Pair.with(key, newText);
    }


    private boolean checkStringEdition() {
        Integer key = Integer.parseInt(textKeyTextField.getText(), 16);
        String text = textTextField.getText();
        if (editedText.getValue0().equals(key) && editedText.getValue1().equals(text))
            return false;
        if (!editedText.getValue0().equals(key) && textsMap.containsKey(key)) {
            mediator.showAlert("Pod tym kluczem jest juz zapisany tekst!", Alert.AlertType.ERROR);
            textKeyTextField.setText(Integer.toHexString(editedText.getValue0()));
            return false;
        }
        return true;
    }

    private boolean checkTypesEdition() {
        String key = Picture.generateTypeSubtype(typeTextField.getText(), subTypeTextField.getText());
        if (key.equals(typeSubType))
            return false;
        if (mediator.getPictures().containsKey(key)) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Obrazek o podanym kluczu (typ+subtyp) juz istnieje!");
            alert.showAndWait();
            subTypeTextField.setText(picture.getSubTypeString());
            typeTextField.setText(picture.getTypeString());
            return false;
        }
        return true;
    }

    public void setMediator(Mediator mediator) {
        this.mediator = mediator;
    }

    public void setPicture(Picture picture) {
        this.picture = picture;
        initTextsTable(picture);
        initTypeTextFields();
        addRowClickListener(textsTable);
        updateEditedTextFields(Pair.with(picture.getTexts().firstKey().toString(), picture.getText(picture.getTexts().firstKey())));
        editedText = Pair.with(picture.getTexts().firstKey(), picture.getTexts().get(picture.getTexts().firstKey()));
    }

    private void addRowClickListener(TableView table) {
        table.setRowFactory(tv -> {
            TableRow<Pair<String, String>> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty()) {
                    Pair<String, String> text = row.getItem();
                    editedText = Pair.with(Integer.parseInt(text.getValue0(), 16), text.getValue1());
                    updateEditedTextFields(text);
                }
            });
            return row;
        });
    }

    private void updateEditedTextFields(Pair<String, String> text) {
        textTextField.setText(text.getValue1());
        textKeyTextField.setText(text.getValue0());
    }

    private void resetEditedTextFields() {
        editedText = Pair.with(picture.getTexts().firstKey(), picture.getTexts().get(picture.getTexts().firstKey()));
        updateEditedTextFields(Pair.with(editedText.getValue0().toString(), editedText.getValue1()));
    }

    private void initTypeTextFields() {
        typeTextField.setText(picture.getTypeString());
        subTypeTextField.setText(picture.getSubTypeString());
        type = picture.getType();
        subType = picture.getSubType();
        typeSubType = picture.getTypesubtype();
    }

    private void initTextsTable(Picture p) {
        textsMap = FXCollections.observableHashMap();
        for (Integer key : p.getTexts().keySet()) {
            textsMap.put(key, p.getTexts().get(key));
        }
        ObservableList<TableColumn> columns = textsTable.getColumns();
        columns.get(0).setCellValueFactory(new PropertyValueFactory<Pair<String, String>, String>("Value0"));
        columns.get(1).setCellValueFactory(new PropertyValueFactory<Pair<String, String>, String>("Value1"));
        textsTable.setItems(textsMapToTextsList(textsMap));
    }

    private void updateTextsTable() {
        textsTable.setItems(textsMapToTextsList(textsMap));
    }

    private ObservableList<Pair<String, String>> textsMapToTextsList(ObservableMap<Integer, String> textsMap) {
        ObservableList<Pair<String, String>> textsList = FXCollections.observableArrayList();
        for (Map.Entry<Integer, String> entry : textsMap.entrySet()) {
            textsList.add(Pair.with(Picture.generateTypeString(entry.getKey()), entry.getValue()));
        }
        return textsList;
    }

    private void forceHexInput(TextField textField) {
        textField.textProperty().addListener(((observable, oldValue, newValue) -> {
            if (!newValue.matches("/[0-9a-fA-F]*")) textField.setText(newValue.replaceAll("[^0-9a-fA-F]", ""));
        }));
    }

    public void setPicEditPane(PicEditPane picEditPane) {
        this.picEditPane = picEditPane;
    }

    @FXML
    private void handleCancelButton() {
        mediator.closeEditing();
    }

    @FXML
    private void handleResetTextsButton() {
        mediator.showAlert("Zresetowano teksty i typy!", Alert.AlertType.CONFIRMATION);
        initTextsTable(picture);
        initTypeTextFields();
        resetEditedTextFields();
    }

    @FXML
    private void handleAddTextButton() {
        int key = 0;
        while (textsMap.containsKey(key)) {
            key++;
        }
        textsMap.put(key, "");
        updateTextsTable();
    }

    @FXML
    private void handleDeleteTextButton() {
        if (textsMap.size() == 1) {
            mediator.showAlert("Musisz pozostawic conajmniej jeden tekst!", Alert.AlertType.ERROR);
        } else {
            textsMap.remove(editedText.getValue0());
            updateTextsTable();
        }
    }

    @FXML
    private void handleSavePictureButton() {
        Picture newP;
        LinkedHashMap<String, Integer> colors = mediator.getEditedColors();
        int[] picData = picEditPane.getEditedPictureData();
        TreeMap<Integer, String> newTexts = new TreeMap<>();
        textsMap.forEach(newTexts::put);
        if (picture instanceof PointPicture) {
            newP = new PointPicture(type, subType, picture.getCharsPerColor(), colors, newTexts, ArrayTools.moveArrayToMatrix(picData, picture.getWidth()));
        } else {
            if (picture instanceof PolygonPicture) {
                newP = new PolygonPicture(type, subType, picture.getCharsPerColor(), colors, newTexts, ArrayTools.moveArrayToMatrix(picData, picture.getWidth()));
            } else {
                newP = new LinePicture(type, subType, picture.getCharsPerColor(), colors, newTexts, ArrayTools.moveArrayToMatrix(picData, picture.getWidth()));
            }
        }
        savePicture(newP);
        mediator.synchronizeTables();
        mediator.setActivePicture(newP);
        mediator.showAlert("Zapisano!", Alert.AlertType.CONFIRMATION);
        mediator.closeEditing();
    }

    private void savePicture(Picture p) {
        Map<String, Picture> pictures = mediator.getPictures();
        boolean isReplaced = false;
        ArrayList<Map.Entry<String, Picture>> tempPicList = new ArrayList<>();
        for (Map.Entry<String, Picture> entry : pictures.entrySet()) {
            if (entry.getKey().equals(picture.getTypesubtype())) {
                tempPicList.add(new AbstractMap.SimpleEntry<>(typeSubType, p));
                isReplaced = true;
            } else tempPicList.add(entry);
        }
        if (!isReplaced) {
            tempPicList.add(new AbstractMap.SimpleEntry<>(typeSubType, p));
        }
        pictures.clear();
        for (Map.Entry<String, Picture> entry : tempPicList) {
            pictures.put(entry.getKey(), entry.getValue());
        }
    }
}

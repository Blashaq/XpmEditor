package app.FXMLController;

import app.Model.Structures.Picture.LinePicture;
import app.Model.Structures.Picture.Picture;
import app.Model.Structures.Picture.PointPicture;
import app.Model.Structures.Picture.PolygonPicture;
import app.Model.Structures.Mediator;
import app.Utils.PictureTools.Exceptions.InvalidFileException;
import app.Utils.PictureTools.PictureLoader;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.javatuples.Pair;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class FunctionBarController {
    private Mediator mediator;
    private Map<String, Picture> pictures;
    private ObservableMap<String, PointPicture> observablePointPictures;
    private ObservableMap<String, PolygonPicture> observablePolygonPictures;
    private ObservableMap<String, LinePicture> observableLinePictures;
    private PictureLoader pictureLoader;
    @FXML
    private TableView pointPictureTable;
    @FXML
    private TableView polygonPictureTable;
    @FXML
    private TableView linePictureTable;
    @FXML
    private TableView textsTable;
    @FXML
    private ToggleGroup scaleGroup;

    @FXML
    public void initialize() {
        pictureLoader = new PictureLoader();
        pictures = new LinkedHashMap<>();
        pointPictureTable.setEditable(false);
        polygonPictureTable.setEditable(false);
        linePictureTable.setEditable(false);
        textsTable.setEditable(false);
        addRowClickListener(pointPictureTable);
        addRowClickListener(linePictureTable);
        addRowClickListener(polygonPictureTable);
        scaleGroup.selectedToggleProperty().addListener((ov, old_toggle, new_toggle) -> {
            if (scaleGroup.getSelectedToggle() != null) {
                mediator.setScale(scaleGroup.getSelectedToggle().getUserData().toString());
            }
        });
    }

    public void setMediator(Mediator mediator) {
        this.mediator = mediator;
    }

    public Map<String, Picture> getPictures() {
        return pictures;
    }

    @FXML
    private void handleLoadPicture() {
        File file = mediator.showLoadPicturesDialog();
        loadPictures(file);
    }

    public void loadTestFile() {
        ClassLoader loader = getClass().getClassLoader();
        File file = new File(loader.getResource("textfiles/ludekt1.txt").getFile());
        loadPictures(file);
    }

    private void loadPictures(File file) {
        if (file != null) {
            try {
                openFile(file);
                updateTable(pointPictureTable, observablePointPictures);
                updateTable(polygonPictureTable, observablePolygonPictures);
                updateTable(linePictureTable, observableLinePictures);
            } catch (FileNotFoundException | InvalidFileException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("Blad pliku!!");
                alert.showAndWait();
            }
        }
    }

    private void addRowClickListener(TableView table) {
        table.setRowFactory(tv -> {
            TableRow<Picture> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty()) {
                    Picture p = row.getItem();
                    mediator.setActivePicture(p);
                    updateTextsTable(p);
                }
            });
            return row;
        });
    }

    private void updateTextsTable(Picture p) {
        ObservableList<Pair<String, String>> textsArray = FXCollections.observableArrayList();
        for (Integer key : p.getTexts().keySet()) {
            textsArray.add(Pair.with(Integer.toHexString(key), p.getTexts().get(key)));
        }
        ObservableList<TableColumn> columns = textsTable.getColumns();
        columns.get(0).setCellValueFactory(new PropertyValueFactory<Pair<String, String>, String>("Value0"));
        columns.get(1).setCellValueFactory(new PropertyValueFactory<Pair<String, String>, String>("Value1"));
        textsTable.setItems(textsArray);
    }

    private void updateTable(TableView table, ObservableMap<String, ? extends Picture> pictures) {
        ObservableList<TableColumn> columnList = table.getColumns();
        columnList.get(0).setCellValueFactory(new PropertyValueFactory<Picture, String>("TypeString"));
        columnList.get(1).setCellValueFactory(new PropertyValueFactory<Picture, String>("SubTypeString"));
        columnList.get(2).setCellValueFactory(new PropertyValueFactory<Picture, String>("DefaultText"));
        table.setItems(FXCollections.observableArrayList(pictures.values()));
    }

    private void openFile(File file) throws FileNotFoundException, InvalidFileException {
        Pair<ArrayList<? extends Picture>, Map<Integer, Integer>> picturesAndOrders = pictureLoader.processFile(file);
        for (Picture p : picturesAndOrders.getValue0()) {
            pictures.put(p.getTypesubtype(), p);
        }
        synchronizeTables();
        mediator.updateDrawOrder(picturesAndOrders.getValue1(),false);
    }

    public void synchronizeTables() {
        observableLinePictures = FXCollections.observableHashMap();
        observablePointPictures = FXCollections.observableHashMap();
        observablePolygonPictures = FXCollections.observableHashMap();
        for (Picture p : pictures.values()) {
            if (p instanceof PointPicture)
                observablePointPictures.put(p.getTypesubtype(), (PointPicture) p);
            else {
                if (p instanceof PolygonPicture)
                    observablePolygonPictures.put(p.getTypesubtype(), (PolygonPicture) p);
                else observableLinePictures.put(p.getTypesubtype(), (LinePicture) p);
            }
        }
        updateTable(pointPictureTable, observablePointPictures);
        updateTable(polygonPictureTable, observablePolygonPictures);
        updateTable(linePictureTable, observableLinePictures);
    }

    public void deletePicture(Picture picture) {
        if (picture != null) {
            pictures.remove(picture.getTypesubtype());
            synchronizeTables();
        }
    }
}


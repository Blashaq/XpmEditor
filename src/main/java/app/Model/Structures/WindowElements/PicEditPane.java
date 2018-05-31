package app.Model.Structures.WindowElements;

import app.Model.Structures.Picture.LinePicture;
import app.Model.Structures.Picture.Picture;
import app.Model.Structures.Picture.PointPicture;
import app.Model.Structures.Picture.PolygonPicture;
import app.Utils.ArrayTools.ArrayTools;
import javafx.geometry.Pos;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class PicEditPane extends StackPane {
    private Picture picture;
    private Map<String, Canvas> sideCanvases = new TreeMap<>();
    private Canvas center;
    private Canvas bg;
    private int canvasW;
    private int canvasH;
    private int scale = 4;
    private Image scaledImage;
    private Color primaryColor = Color.TRANSPARENT;
    private Color secondaryColor = Color.TRANSPARENT;
    private boolean canDraw = false;

    public PicEditPane(Picture p) {
        super();
        picture = p;
        canvasW = picture.getWidth() * scale;
        canvasH = picture.getHeight() * scale;
        createCanvases();
        setPrefSize(canvasW * 3 + 4, canvasH * 3 + 4);
        insertCanvases();
        setCanvasPositions();
        scaledImage = picture.getScaledImage(scale);
        initCanvases();
        addEventListeners(center);
        setMaxSize(canvasW * 3 + 4, canvasH * 3 + 4);
    }

    public Picture getPicture() {
        return picture;
    }

    public int[] getEditedPictureData() {
        int[] scaledPicData = new int[canvasW * canvasH];
        SnapshotParameters param = new SnapshotParameters();
        param.setFill(Color.TRANSPARENT);
        WritableImage image = center.snapshot(param, new WritableImage(canvasW, canvasH));
        PixelReader pr = image.getPixelReader();
        pr.getPixels(0, 0, canvasW, canvasH, WritablePixelFormat.getIntArgbInstance(), scaledPicData, 0, canvasW);
        int[][] scaledPicMatrix = ArrayTools.moveArrayToMatrix(scaledPicData, canvasW);
        int[][] picMatrix = new int[picture.getHeight()][picture.getWidth()];
        for (int i = 0; i < picMatrix.length; i++) {
            for (int j = 0; j < picMatrix[0].length; j++) {
                picMatrix[i][j] = scaledPicMatrix[i * scale][j * scale];
            }
        }
        return ArrayTools.moveMatrixToArray(picMatrix);
    }

    public void reset() {
        initCanvases();
    }

    private void addEventListeners(Canvas c) {
        c.addEventHandler(MouseEvent.MOUSE_CLICKED, this::drawPixel);
        c.addEventHandler(MouseEvent.MOUSE_DRAGGED, this::drawPixel);
    }

    private void drawPixel(MouseEvent event) {

        if (event.getButton().equals(MouseButton.PRIMARY)) {
            drawPixel(primaryColor, event);
        } else {
            drawPixel(secondaryColor, event);
        }
        updateCanvases();
    }

    private void drawPixel(Color color, MouseEvent event) {
        if (!canDraw)
            return;
        GraphicsContext gc = center.getGraphicsContext2D();
        if (color.equals(Color.TRANSPARENT)) {
            gc.clearRect(event.getX(), event.getY(), scale, scale);
        } else {
            gc.setFill(color);
            gc.fillRect(event.getX(), event.getY(), scale, scale);
        }
    }

    private void updateCanvases() {
        SnapshotParameters param = new SnapshotParameters();
        param.setFill(Color.TRANSPARENT);
        WritableImage snapshot = center.snapshot(param, new WritableImage(canvasW, canvasH));
        for (Canvas c : sideCanvases.values()) {
            c.getGraphicsContext2D().clearRect(0, 0, canvasW, canvasH);
            c.getGraphicsContext2D().drawImage(snapshot, 0, 0);
        }
    }

    private void initCanvases() {
        initBg();
        center.getGraphicsContext2D().clearRect(0, 0, canvasW, canvasH);
        center.getGraphicsContext2D().drawImage(scaledImage, 0, 0);
        updateCanvases();
    }

    private void initBg() {
        GraphicsContext gc = bg.getGraphicsContext2D();
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);
        gc.strokeLine(0, canvasH, bg.getWidth(), canvasH);
        gc.strokeLine(0, canvasH * 2 + 2, bg.getWidth(), canvasH * 2 + 2);
        gc.strokeLine(canvasW, 0, canvasW, bg.getHeight());
        gc.strokeLine(canvasW * 2 + 2, 0, canvasW * 2 + 2, bg.getHeight());
    }

    private void insertCanvases() {
        getChildren().add(bg);
        getChildren().add(center);
        for (Canvas c : sideCanvases.values()) {
            getChildren().add(c);
        }
    }

    private void setCanvasPositions() {
        StackPane.setAlignment(center, Pos.CENTER);
        if (picture instanceof PolygonPicture) {
            StackPane.setAlignment(sideCanvases.get("lefttop"), Pos.TOP_LEFT);
            StackPane.setAlignment(sideCanvases.get("leftcenter"), Pos.CENTER_LEFT);
            StackPane.setAlignment(sideCanvases.get("leftbottom"), Pos.BOTTOM_LEFT);
            StackPane.setAlignment(sideCanvases.get("centertop"), Pos.TOP_CENTER);
            StackPane.setAlignment(sideCanvases.get("centerbottom"), Pos.BOTTOM_CENTER);
            StackPane.setAlignment(sideCanvases.get("righttop"), Pos.TOP_RIGHT);
            StackPane.setAlignment(sideCanvases.get("rightcenter"), Pos.CENTER_RIGHT);
            StackPane.setAlignment(sideCanvases.get("rightbottom"), Pos.BOTTOM_RIGHT);
        } else if (picture instanceof LinePicture) {
            StackPane.setAlignment(sideCanvases.get("rightcenter"), Pos.CENTER_RIGHT);
            StackPane.setAlignment(sideCanvases.get("leftcenter"), Pos.CENTER_LEFT);
        }

    }

    public Set<Integer> getAllARGBColors() {
        SnapshotParameters param = new SnapshotParameters();
        HashSet<Integer> argbs = new HashSet<>();
        param.setFill(Color.TRANSPARENT);
        WritableImage image = center.snapshot(param, new WritableImage(canvasW, canvasH));
        PixelReader pr = image.getPixelReader();
        for (int i = 0; i < canvasW; i++) {
            for (int j = 0; j < canvasH; j++) {
                argbs.add(pr.getArgb(i, j));
            }
        }
        return argbs;
    }

    private void createCanvases() {
        bg = new Canvas(canvasW * 3 + 2, canvasH * 3 + 2);
        center = new Canvas(canvasW, canvasH);
        if (picture instanceof PolygonPicture) {
            sideCanvases.put("lefttop", new Canvas(canvasW, canvasH));
            sideCanvases.put("leftcenter", new Canvas(canvasW, canvasH));
            sideCanvases.put("leftbottom", new Canvas(canvasW, canvasH));
            sideCanvases.put("centertop", new Canvas(canvasW, canvasH));
            sideCanvases.put("centerbottom", new Canvas(canvasW, canvasH));
            sideCanvases.put("righttop", new Canvas(canvasW, canvasH));
            sideCanvases.put("rightcenter", new Canvas(canvasW, canvasH));
            sideCanvases.put("rightbottom", new Canvas(canvasW, canvasH));
        } else if (picture instanceof LinePicture) {
            sideCanvases.put("leftcenter", new Canvas(canvasW, canvasH));
            sideCanvases.put("rightcenter", new Canvas(canvasW, canvasH));
        }
    }

    public void setSecondaryColor(Color secondaryColor) {
        this.secondaryColor = secondaryColor;
        canDraw = true;
    }

    public void setPrimaryColor(Color primaryColor) {
        this.primaryColor = primaryColor;
        canDraw = true;
    }
}

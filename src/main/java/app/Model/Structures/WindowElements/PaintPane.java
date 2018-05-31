package app.Model.Structures.WindowElements;

import app.Model.Structures.Mediator;
import app.Model.Structures.Picture.LinePicture;
import app.Model.Structures.Picture.Picture;
import app.Model.Structures.Picture.PointPicture;
import app.Model.Structures.Picture.PolygonPicture;
import app.Utils.ArrayTools.ArrayTools;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import org.javatuples.Pair;

import java.util.*;

public class PaintPane extends Pane {
    private Mediator mediator;
    private Point2D beginDrawCoordinates;
    private Point2D lastMousePos;
    private Pair<Boolean, Boolean> lastFlip;
    private LayerCanvas bgCanvas;
    private LayerCanvas previewCanvas;
    private HashMap<Integer, LayerCanvas> layers;
    private Color bgColor;
    private double width, height;
    private int scale = 1;
    private final int DEFAULT_LAYER_NUMBER = 0;

    public PaintPane(Mediator mediator, double width, double height, Color bgColor) {
        super();
        this.width = width;
        this.height = height;
        this.mediator = mediator;
        this.bgColor = bgColor;
        lastFlip = Pair.with(false, false);
        addHandlers();
        initBackground(bgColor);
        initPreview();
        updateLayers();
    }

    public PaintPane(Mediator mediator, Image bg) {
        super();
        this.width = bg.getWidth();
        this.height = bg.getHeight();
        this.mediator = mediator;
        this.bgColor = Color.TRANSPARENT;
        lastFlip = Pair.with(false, false);
        addHandlers();
        initBackground(bg);
        initPreview();
        updateLayers();
    }

    public void updateLayers() {
        layers = new HashMap<>();
        Set<Integer> layerNumbers = new HashSet<>();
        layerNumbers.add(DEFAULT_LAYER_NUMBER);
        Map<Integer, Integer> drawOrder = mediator.getDrawOrder();
        drawOrder.values().forEach(layerNumbers::add);
        for (int layerNumber : layerNumbers) {
            if (!layers.containsKey(layerNumber)) {
                LayerCanvas c = new LayerCanvas(layerNumber, false);
                c.setWidth(width);
                c.setHeight(height);
                layers.put(layerNumber, c);
                this.getChildren().add(c);
            }
        }
        sortLayers();
    }

    public Image getSnapshot() {
        SnapshotParameters param = new SnapshotParameters();
        param.setFill(Color.TRANSPARENT);
        return snapshot(param, null);
    }

    public void clear() {
        for (Node n : this.getChildren()) {
            Canvas c = (Canvas) n;
            c.getGraphicsContext2D().clearRect(0, 0, c.getWidth(), c.getHeight());
        }
        bgCanvas.getGraphicsContext2D().setFill(bgColor);
        bgCanvas.getGraphicsContext2D().fillRect(0, 0, width, height);
    }

    private void addHandlers() {
        this.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> {
            setScale(mediator.getScale());
            registerStartPoint(event);
            previewCanvas.setLayerNumber(selectLayer(mediator.getActivePicture()));
            sortLayers();
        });
        this.addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> showDrawPreview(mediator.getActivePicture(), event));
        this.addEventHandler(MouseEvent.MOUSE_RELEASED, event -> {
            drawPicture(mediator.getActivePicture(), event, layers.get(selectLayer(mediator.getActivePicture())));
            clearLayer(previewCanvas);
        });
    }

    private void initPreview() {
        previewCanvas = new LayerCanvas(Integer.MAX_VALUE, true);
        previewCanvas.setWidth(width);
        previewCanvas.setHeight(height);
        getChildren().add(previewCanvas);
    }

    private void initBackground(Color bgColor) {
        bgCanvas = new LayerCanvas(-1, false);
        bgCanvas.setWidth(width);
        bgCanvas.setHeight(height);
        bgCanvas.getGraphicsContext2D().setFill(bgColor);
        bgCanvas.getGraphicsContext2D().fillRect(0, 0, width, height);
        getChildren().add(bgCanvas);
    }

    private void initBackground(Image bg) {
        bgCanvas = new LayerCanvas(-1, false);
        bgCanvas.setWidth(width);
        bgCanvas.setHeight(height);
        bgCanvas.getGraphicsContext2D().drawImage(bg, 0, 0);
        getChildren().add(bgCanvas);
    }

    private void sortLayers() {
        ObservableList<Node> children = FXCollections.observableArrayList();
        children.addAll(getChildren());
        children.sort((o1, o2) -> ((LayerCanvas) o1).compareTo(o2));
        getChildren().setAll(children);
    }

    private int selectLayer(Picture p) {
        if (p == null)
            return 0;
        int type = p.getType();
        int layer = 0;
        try {
            layer = mediator.getDrawOrder().get(type);
        } catch (NullPointerException e) {
            System.err.println("brak warstwy na liscie! probuje uzyc subtypu");
            try {
                layer = mediator.getDrawOrder().get(p.getSubType());
            } catch (NullPointerException f) {
                System.err.println("nie udalo sie :( uzywam domyslnej warstwy");
            }
        }
        System.out.println("wybralem warstwe: " + layer);
        return layer;
    }

    private void clearLayer(Canvas layer) {
        layer.getGraphicsContext2D().clearRect(0, 0, layer.getWidth(), layer.getHeight());
    }

    private void showDrawPreview(Picture picture, MouseEvent event) {
        if (picture instanceof PolygonPicture)
            showPolygonDrawPreview(picture, event);
        else {
            previewCanvas.getGraphicsContext2D().clearRect(0, 0, previewCanvas.getWidth(), previewCanvas.getHeight());
            drawPicture(picture, event, previewCanvas);
        }
    }

    private void showPolygonDrawPreview(Picture picture, MouseEvent event) {
        Point2D finishPos = new Point2D(event.getX(), event.getY());
        Pair<Boolean, Boolean> flippedCoords = areCoordsFlipped(beginDrawCoordinates, finishPos);
        if (flippedCoords.getValue0() != lastFlip.getValue0() || flippedCoords.getValue1() != lastFlip.getValue1()) {
            previewCanvas.getGraphicsContext2D().clearRect(0, 0, previewCanvas.getWidth(), previewCanvas.getHeight());
            lastFlip = flippedCoords;
            return;
        }
        boolean flippedX = flippedCoords.getValue0();
        boolean flippedY = flippedCoords.getValue1();
        if (!flippedX && !flippedY) {
            previewCanvas.getGraphicsContext2D().clearRect(beginDrawCoordinates.getX(), event.getY(), lastMousePos.getX() - beginDrawCoordinates.getX(),
                    lastMousePos.getY() - event.getY());

            previewCanvas.getGraphicsContext2D().clearRect(event.getX(), beginDrawCoordinates.getY(),
                    lastMousePos.getX() - event.getX(), event.getY() - beginDrawCoordinates.getY());
        }
        if (flippedX && flippedY) {
            previewCanvas.getGraphicsContext2D().clearRect(lastMousePos.getX(), lastMousePos.getY(), event.getX() - lastMousePos.getX(),
                    beginDrawCoordinates.getY() - lastMousePos.getY());
            previewCanvas.getGraphicsContext2D().clearRect(event.getX(), lastMousePos.getY(), beginDrawCoordinates.getX() - event.getX(),
                    event.getY() - lastMousePos.getY());
        } else {
            if (flippedX) {
                previewCanvas.getGraphicsContext2D().clearRect(lastMousePos.getX(), beginDrawCoordinates.getY(),
                        event.getX() - lastMousePos.getX(), lastMousePos.getY() - beginDrawCoordinates.getY());
                previewCanvas.getGraphicsContext2D().clearRect(event.getX(), event.getY(),
                        beginDrawCoordinates.getX() - event.getX(), lastMousePos.getY() - event.getY());
            } else if (flippedY) {
                previewCanvas.getGraphicsContext2D().clearRect(beginDrawCoordinates.getX(), lastMousePos.getY(),
                        event.getX() - beginDrawCoordinates.getX(), event.getY() - lastMousePos.getY());
                previewCanvas.getGraphicsContext2D().clearRect(event.getX(), lastMousePos.getY(),
                        lastMousePos.getX() - event.getX(), beginDrawCoordinates.getY() - lastMousePos.getY());
            }
        }
        lastMousePos = new Point2D(event.getX(), event.getY());
        lastFlip = flippedCoords;
        drawPicture(picture, event, previewCanvas);
    }

    private void registerStartPoint(MouseEvent event) {
        beginDrawCoordinates = new Point2D(event.getX(), event.getY());
        lastMousePos = new Point2D(event.getX(), event.getY());
    }

    private void drawPicture(Picture picture, MouseEvent event, Canvas layer) {
        if (picture == null)
            return;
        if (picture instanceof PointPicture)
            drawPoint(picture, getEventPos(event), layer);
        else {
            if (picture instanceof LinePicture)
                drawLine(picture, event, layer);
            else
                drawPolygon(picture, event, layer);
        }
    }

    private Point2D getEventPos(MouseEvent event) {
        return new Point2D(event.getX(), event.getY());
    }

    private void drawPoint(Picture picture, Point2D drawPos, Canvas layer) {
        double centerX = picture.getWidth() * scale / 2;
        double centerY = picture.getHeight() * scale / 2;
        GraphicsContext gc = layer.getGraphicsContext2D();
        gc.drawImage(picture.getScaledImage(scale), drawPos.getX() - centerX, drawPos.getY() - centerY);
    }

    private void drawLine(Picture picture, MouseEvent event, Canvas layer) {
        Point2D finishCoords = new Point2D(event.getX(), event.getY());
        double distance = beginDrawCoordinates.distance(finishCoords);
        Point2D lineVector = new Point2D(distance, picture.getHeight() * scale);
        Image image = createShape(picture, lineVector);
        Point2D rotationVector = new Point2D(finishCoords.getX() - beginDrawCoordinates.getX(), finishCoords.getY() - beginDrawCoordinates.getY());
        double angle = rotationVector.angle(1, 0);
        if (finishCoords.getY() < beginDrawCoordinates.getY())
            angle = 360 - angle;
        drawRotatedImage(image, angle, beginDrawCoordinates.getX(), beginDrawCoordinates.getY(), layer);
    }

    private void drawPolygon(Picture picture, MouseEvent event, Canvas layer) {
        Point2D finishCoords = new Point2D(event.getX(), event.getY());
        Image image = createShape(picture, getDistance(beginDrawCoordinates, finishCoords));
        drawPolygon(image, beginDrawCoordinates, finishCoords, layer);
    }

    private void drawPolygon(Image image, Point2D beginCoords, Point2D finishCoords, Canvas layer) {
        GraphicsContext gc = layer.getGraphicsContext2D();
        Pair<Boolean, Boolean> flippedCoords = areCoordsFlipped(beginCoords, finishCoords);
        boolean flippedX = flippedCoords.getValue0();
        boolean flippedY = flippedCoords.getValue1();
        if (!flippedX && !flippedY) {
            gc.drawImage(image, beginCoords.getX(), beginCoords.getY());
        } else {
            if (flippedX && flippedY)
                gc.drawImage(image, finishCoords.getX(), finishCoords.getY());
            else {
                if (flippedX)
                    gc.drawImage(image, finishCoords.getX(), beginCoords.getY());
                else
                    gc.drawImage(image, beginCoords.getX(), finishCoords.getY());
            }
        }
    }

    private Pair<Boolean, Boolean> areCoordsFlipped(Point2D beginPos, Point2D finishPos) {
        Boolean flippedX = beginPos.getX() > finishPos.getX();
        Boolean flippedY = beginPos.getY() > finishPos.getY();
        return Pair.with(flippedX, flippedY);
    }

    private Image createShape(Picture picture, Point2D polygonVector) {
        int polygonX = (int) polygonVector.getX();
        int polygonY = (int) polygonVector.getY();
        if (polygonX == 0 || polygonY == 0)
            return null;
        WritableImage image = new WritableImage(polygonX, polygonY);

        int pictureHeight = picture.getHeight() * scale;
        int pictureWidth = picture.getWidth() * scale;
        int[] pictureData = picture.getScaledPictureData(scale);

        int xPixelsToCut = polygonX % pictureWidth;
        int yPixelsToCut = polygonY % pictureHeight;
        int[] pictureCutByHeight = ArrayTools.cutPictureByHeight(pictureData, yPixelsToCut, pictureWidth);
        int[] pictureCutByWidth = ArrayTools.cutPictureByWidth(pictureData, xPixelsToCut, pictureWidth, pictureHeight);
        int[] punyFragment = ArrayTools.cutCorner(pictureData, xPixelsToCut, yPixelsToCut, pictureWidth, pictureHeight);

        PixelWriter pw = image.getPixelWriter();
        int drawX, drawY;
        for (drawY = 0; drawY < polygonY; drawY += pictureHeight) {
            if (drawY + pictureHeight <= polygonY) {
                for (drawX = 0; drawX + pictureWidth < polygonX; drawX += pictureWidth) {
                    pw.setPixels(drawX, drawY, pictureWidth, pictureHeight, PixelFormat.getIntArgbInstance(), pictureData, 0, pictureWidth);
                }
                pw.setPixels(drawX, drawY, xPixelsToCut, pictureHeight, PixelFormat.getIntArgbInstance(), pictureCutByWidth, 0, xPixelsToCut);
            } else {
                for (drawX = 0; drawX + pictureWidth < polygonX; drawX += pictureWidth) {
                    pw.setPixels(drawX, drawY, pictureWidth, yPixelsToCut, PixelFormat.getIntArgbInstance(), pictureCutByHeight, 0, pictureWidth);
                }
                pw.setPixels(drawX, drawY, xPixelsToCut, yPixelsToCut, PixelFormat.getIntArgbInstance(), punyFragment, 0, xPixelsToCut);
            }
        }
        return image;
    }

    private void drawRotatedImage(Image image, double angle, double pivotX, double pivotY, Canvas layer) {
        GraphicsContext gc = layer.getGraphicsContext2D();
        gc.save();
        gc.translate(pivotX, pivotY);
        gc.rotate(angle);
        gc.drawImage(image, 0, 0);
        gc.restore();
    }

    private Point2D getDistance(Point2D ev1, Point2D ev2) {
        return new Point2D(Math.abs(ev2.getX() - ev1.getX()), Math.abs(ev2.getY() - ev1.getY()));
    }

    private void setScale(int scale) {
        this.scale = scale;
    }
}
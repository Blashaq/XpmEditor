package app.FXMLController;

import app.Model.Structures.Picture.Picture;
import app.Model.Structures.Picture.PolygonPicture;
import app.Model.Structures.Mediator;
import app.Model.Structures.WindowElements.PicEditPane;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Alert;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.paint.Color;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

public class ColorBarController {
    @FXML
    TilePane colorPane;
    @FXML
    TilePane selectedColorPane;
    private Mediator mediator;
    @FXML
    private Canvas primaryColorPreview;
    @FXML
    private Canvas secondaryColorPreview;
    @FXML
    private ColorPicker colorPicker;
    private LinkedHashMap<String, Integer> colors;
    private PicEditPane picEditPane;
    private StackPane selectedButton;
    private String allowedChars = "`~!@#$%^&*()_+1234567890-=qwertyuiop{[}]asdfghjkl:;\"'|\\zxcvbnm,<.>/?";

    @FXML
    public void initialize() {
        colorPicker.setVisible(false);
        colorPicker.setOnAction(event ->
                setColor(colorPicker.getValue(), selectedButton)
        );
    }

    public LinkedHashMap<String, Integer> getEditedColors() {
        return colors;
    }

    public void setMediator(Mediator mediator) {
        this.mediator = mediator;
        mediator.setColorBarController(this);
    }

    public void setPicEditPane(PicEditPane picEditPane) {
        this.picEditPane = picEditPane;
        initColors(picEditPane.getPicture());
    }

    private void initColors(Picture p) {
        colors = new LinkedHashMap<>();
        colorPane.getChildren().clear();
        Map<String, Integer> picColors = p.getColors();
        for (Map.Entry<String, Integer> stringIntegerEntry : picColors.entrySet()) {
            initButton(stringIntegerEntry);
        }
        createButton();

    }

    private void initButton(Map.Entry<String, Integer> keyAndColor) {
        StackPane sp = createButton();
        colors.put(keyAndColor.getKey(), keyAndColor.getValue());
        colorButton(toColor(keyAndColor.getValue()), sp);
        setLabel(keyAndColor.getKey(), sp);
    }

    private StackPane createButton() {
        ToggleButton b = new ToggleButton();
        b.setPrefSize(25, 25);
        b.setMouseTransparent(true);
        Canvas c = new Canvas();
        c.setWidth(20);
        c.setHeight(20);
        c.setMouseTransparent(true);
        Label l = new Label();
        l.setText("None");
        l.translateXProperty().setValue(25);
        StackPane sp = new StackPane();
        sp.setMinSize(25, 45);
        sp.getChildren().add(b);
        sp.getChildren().add(c);
        sp.getChildren().add(l);
        colorPane.getChildren().add(sp);
        TilePane.setMargin(sp, new Insets(0, 0, 25, 0));
        addColorHandlers(sp);
        return sp;
    }


    private void addColorHandlers(StackPane sp) {
        sp.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            selectButton(sp);
            colorPicker.setVisible(true);
            colorPicker.setValue(colorFromButton(sp));
            if (event.getButton().equals(MouseButton.SECONDARY)) {
                selectColor(secondaryColorPreview, sp);
            } else if (event.getButton().equals(MouseButton.PRIMARY)) {
                selectColor(primaryColorPreview, sp);
            }
        });
    }

    private void selectButton(StackPane sp) {
        deselectAll();
        ToggleButton b = (ToggleButton) sp.getChildren().get(0);
        b.selectedProperty().setValue(true);
        selectedButton = sp;
    }

    private void deselectAll() {
        for (Node n : colorPane.getChildren()) {
            StackPane sp = (StackPane) n;
            ToggleButton tb = (ToggleButton) sp.getChildren().get(0);
            tb.selectedProperty().setValue(false);
        }
    }

    private void selectColor(Canvas colorPreview, StackPane button) {
        if (getLabelText(button).equals("None"))
            return;
        Color c = colorFromButton(button);
        fillCanvas(colorPreview, c);
        setActiveColor(c, colorPreview);
    }

    private void setActiveColor(Color c, Canvas colorPreview) {
        if (colorPreview == primaryColorPreview) {
            picEditPane.setPrimaryColor(c);
        } else picEditPane.setSecondaryColor(c);
    }

    private void setColor(Color color, StackPane button) {
        Label l = (Label) button.getChildren().get(2);
        if (picEditPane.getPicture() instanceof PolygonPicture && color.equals(Color.TRANSPARENT)) {
            mediator.showAlert("Polygon nie może zawierać przezroczystego koloru!", Alert.AlertType.ERROR);
            return;
        }
        if (colors.containsValue(toARGB(color))) {
            mediator.showAlert("ten kolor jest już w palecie!", Alert.AlertType.ERROR);
            return;
        }
        if (colors.containsKey(l.getText())) {
            if (canIRemoveColor(colorFromButton(button))) {
                colors.put(l.getText(), toARGB(color));
                colorButton(color, button);
            } else {
                mediator.showAlert("nie możesz zamienić tego koloru, gdyż znajduje się on na obrazku!", Alert.AlertType.ERROR);
            }
        } else {
            registerNewColor(color, button);
        }
    }

    private void registerNewColor(Color color, StackPane button) {
        String character = generateNewKey();
        setLabel(character, button);
        colors.put(character, toARGB(color));
        colorButton(color, button);
        createButton();
    }

    private boolean canIRemoveColor(Color color) {
        return !picEditPane.getAllARGBColors().contains(toARGB(color));
    }

    private String generateNewKey() {
        Random r = new Random();
        int keyLength = picEditPane.getPicture().getCharsPerColor();
        char character[] = new char[keyLength];
        String validCharacter;
        do {
            for (int i = 0; i < character.length; i++) {
                character[i] = allowedChars.charAt(r.nextInt(allowedChars.length()));
            }
            validCharacter = new String(character);
        }
        while (colors.containsKey(validCharacter));
        return validCharacter;
    }

    private void setLabel(String text, StackPane button) {
        Label l = (Label) button.getChildren().get(2);
        l.setText(text);
    }

    private String getLabelText(StackPane button) {
        Label l = (Label) button.getChildren().get(2);
        return l.getText();
    }

    private void colorButton(Color color, StackPane button) {
        Canvas c = (Canvas) button.getChildren().get(1);
        fillCanvas(c, color);
    }

    private void fillCanvas(Canvas c, Color color) {
        c.getGraphicsContext2D().setFill(color);
        if (!color.equals(Color.TRANSPARENT)) {
            c.getGraphicsContext2D().fillRect(0, 0, c.getWidth(), c.getHeight());
        } else {
            c.getGraphicsContext2D().clearRect(0, 0, c.getWidth(), c.getHeight());
        }
    }

    private Color colorFromButton(StackPane button) {
        Label label = (Label) button.getChildren().get(2);
        if (!colors.containsKey(label.getText())) {
            return Color.TRANSPARENT;
        }
        int color = colors.get(label.getText());
        return toColor(color);
    }

    private int toARGB(Color color) {
        int red = (int) (color.getRed() * 255);
        int green = (int) (color.getGreen() * 255);
        int blue = (int) (color.getBlue() * 255);
        int opacity = (int) (color.getOpacity() * 255);
        return Integer.parseUnsignedInt(String.format("%02X%02X%02X%02X", opacity, red, green, blue), 16);
    }

    private Color toColor(int argb) {
        long mask = 0x000000ff;
        int[] alphaRGB = new int[4];
        for (int i = alphaRGB.length - 1; i >= 0; i--) {
            long elem = argb & mask;
            mask = mask << 8;
            alphaRGB[i] = (int) (elem >> (8 * (3 - i)));
        }
        double opacity = alphaRGB[0] / 255;
        return Color.rgb(alphaRGB[1], alphaRGB[2], alphaRGB[3], opacity);
    }


    @FXML
    private void handleResetButton() {
        mediator.showAlert("Zresetowano obrazek!", Alert.AlertType.CONFIRMATION);
        initColors(picEditPane.getPicture());
        picEditPane.reset();
    }
}

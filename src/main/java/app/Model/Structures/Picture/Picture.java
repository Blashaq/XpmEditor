package app.Model.Structures.Picture;

import app.Utils.ArrayTools.ArrayTools;
import javafx.scene.image.*;
import org.javatuples.Triplet;

import java.util.*;

public abstract class Picture {
    private final TreeMap<Integer, String> _texts;
    private final LinkedHashMap<String, Integer> _colors;
    private int _type;
    private Integer _subtype;
    private String _typesubtype;
    private int[][] _argb_bitmap;
    private int _height;
    private int _width;
    private int _colorsCount;
    private int _charsPerColor;


    Picture(Triplet<ArrayList<String>, Map<PictureTag, String>, Map<String, String>> parsedData) {
        _texts = new TreeMap<>();
        _colors = new LinkedHashMap<>();

        List<String> bitmap = parsedData.getValue0();
        Map<PictureTag, String> tags = parsedData.getValue1();
        Map<String, String> texts = parsedData.getValue2();
        pullTypes(tags);
        pullTexts(texts);
        pullBitmap(bitmap, tags);
        _typesubtype = generateTypeSubtype(_type, _subtype);
    }

    Picture(int type, Integer subtype, int charsPerColor, LinkedHashMap<String, Integer> colors, TreeMap<Integer, String> texts, int[][] argbBitmap) {
        _type = type;
        _subtype = subtype;
        _typesubtype=generateTypeSubtype(type,subtype);
        _charsPerColor = charsPerColor;
        _colors = colors;
        _colorsCount = _colors.size();
        _texts = texts;
        _argb_bitmap = argbBitmap;
        _width = argbBitmap[0].length;
        _height = argbBitmap.length;
    }

    public int[] getScaledPictureData(int scale) {
        //tylko potegi 2!!
        int newWidth = _width * scale;
        int newHeight = _height * scale;

        int[][] scaledPictureMatrix = new int[newHeight][newWidth];
        for (int i = 0; i < scaledPictureMatrix.length; i++) {
            for (int j = 0; j < scaledPictureMatrix[0].length; j++) {
                int pixel = _argb_bitmap[i / scale][j / scale];
                scaledPictureMatrix[i][j] = pixel;
            }
        }
        return ArrayTools.moveMatrixToArray(scaledPictureMatrix);
    }

    public Image getScaledImage(int scale) {
        int newWidth = _width * scale;
        int newHeight = _height * scale;
        WritableImage scaledImage = new WritableImage(newWidth, newHeight);
        scaledImage.getPixelWriter().setPixels(0, 0, newWidth, newHeight, PixelFormat.getIntArgbInstance(), getScaledPictureData(scale), 0, newWidth);
        return scaledImage;
    }


    public Map<String, Integer> getColors() {
        return _colors;
    }

    public int getColorsCount() {
        return _colorsCount;
    }

    public int getCharsPerColor() {
        return _charsPerColor;
    }

    public TreeMap<Integer, String> getTexts() {
        return _texts;
    }

    public String getText(int key) {
        return _texts.get(key);
    }

    @SuppressWarnings("unused")
    public String getDefaultText() {
        return _texts.containsKey(0x15) ? _texts.get(0x15) : _texts.firstEntry() != null ? _texts.get(_texts.firstKey()) : null;
    }

    public int getType() {
        return _type;
    }

    public Integer getSubType() {
        return _subtype;
    }

    public String getTypeString() {
        return String.format("%2s", Integer.toHexString(_type)).replace(" ", "0");
    }

    public String getSubTypeString() {
        return _subtype == null ? "None" : String.format("%2s", Integer.toHexString(_subtype)).replace(" ", "0");
    }

    public static String generateTypeString(int type) {
        return String.format("%2s", Integer.toHexString(type)).replace(" ", "0");
    }

    public static String generateSubTypeString(Integer subtype) {
        return subtype == null ? "None" : String.format("%2s", Integer.toHexString(subtype)).replace(" ", "0");
    }

    public static String generateTypeSubtype(int type, Integer subtype) {
        String typesubtype;
        if (subtype != null) {
            typesubtype = generateTypeString(type) + generateSubTypeString(subtype);
        } else typesubtype = generateTypeString(type);
        return typesubtype;
    }

    public static String generateTypeSubtype(String typeString, String subtypeString) {
        Integer subtype = null;
        try {
            subtype = Integer.parseInt(subtypeString, 16);
        } catch (NumberFormatException ignored) {

        }
        return subtype == null ? typeString : typeString + generateSubTypeString(subtype);
    }


    public String getTypesubtype() {
        return _typesubtype;
    }

    public int getHeight() {
        return _height;
    }

    public int getWidth() {
        return _width;
    }

    public int[][] getBitmap() {
        return _argb_bitmap;
    }

    private Image buildImage() {
        int[] data = ArrayTools.moveMatrixToArray(_argb_bitmap);
        WritableImage image = new WritableImage(_argb_bitmap.length, _argb_bitmap[0].length);
        PixelWriter pw = image.getPixelWriter();
        pw.setPixels(0, 0, _argb_bitmap.length, _argb_bitmap[0].length, PixelFormat.getIntArgbInstance(), data, 0, _argb_bitmap.length);
        return image;
    }

    private void pullTexts(Map<String, String> texts) {
        texts.forEach((k, v) -> {
            String[] splitName = v.split(",");
            _texts.put(ArrayTools.parseNumber(splitName[0]), splitName[1]);
        });
    }

    private void pullTypes(Map<PictureTag, String> tags) {
        _type = ArrayTools.parseNumber(tags.remove(PictureTag.TYPE));
        if (tags.containsKey(PictureTag.SUBTYPE))
            _subtype = ArrayTools.parseNumber(tags.remove(PictureTag.SUBTYPE));
    }

    private void pullBitmap(List<String> bitmap, Map<PictureTag, String> tags) {

        HashMap<PictureTag, Integer> xpmData = pullXpmData(tags);
        HashMap<String, Integer> colors = pullColors(bitmap, xpmData);
        _height = xpmData.get(PictureTag.HEIGHT);
        _charsPerColor = xpmData.get(PictureTag.CHARSPERCOLOUR);
        _width = xpmData.get(PictureTag.WIDTH);

        _argb_bitmap = new int[_height][_width];
        for (int i = 0; i < _height; i++) {
            String bitMapLine = bitmap.remove(0);
            bitMapLine = bitMapLine.substring(1, bitMapLine.length() - 1);
            for (int j = 0; j < _width; j += 1) {
                String color = bitMapLine.substring(j * _charsPerColor, j * _charsPerColor + _charsPerColor);
                    _argb_bitmap[i][j] = colors.get(color);
            }
        }
    }

    private HashMap<PictureTag, Integer> pullXpmData(Map<PictureTag, String> tags) {
        HashMap<PictureTag, Integer> xpmData = new HashMap<>();
        xpmData.put(PictureTag.HEIGHT, Integer.parseInt(tags.get(PictureTag.HEIGHT)));
        xpmData.put(PictureTag.WIDTH, Integer.parseInt(tags.get(PictureTag.WIDTH)));
        xpmData.put(PictureTag.COLOURSCOUNT, Integer.parseInt(tags.get(PictureTag.COLOURSCOUNT)));
        xpmData.put(PictureTag.CHARSPERCOLOUR, Integer.parseInt(tags.get(PictureTag.CHARSPERCOLOUR)));
        return xpmData;
    }

    private HashMap<String, Integer> pullColors(List<String> bitmap, HashMap<PictureTag, Integer> xpmData) throws StringIndexOutOfBoundsException {
        _colorsCount = xpmData.get(PictureTag.COLOURSCOUNT);
        int colorsCounter = _colorsCount;
        int charsPerColor = xpmData.get(PictureTag.CHARSPERCOLOUR);
        while (colorsCounter != 0) {
            String colorLine = bitmap.remove(0);
            String colorSign = colorLine.substring(1, charsPerColor + 1);
            if (colorLine.toLowerCase().contains("none"))
                _colors.put(colorSign, 0x00000000);
            else {
                colorLine = colorLine.substring(5+_charsPerColor).replace("\"", "").replace(",", "");
                    _colors.put(colorSign, 0xFF000000 + ArrayTools.parseNumber(colorLine));
            }
            colorsCounter--;
        }
        return _colors;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("   Typ: ").append(_type).append("\n");
        builder.append("Subtyp: ").append(_subtype).append("\n");
        _texts.forEach((k, v) -> builder.append(String.format("%4d || %s \n", k, v)));
        for (int[] aBitmap : _argb_bitmap) {
            builder.append("\n");
            for (int j = 0; j < aBitmap.length; j++) {
                builder.append(String.format("%10s", Integer.toHexString(aBitmap[j]) + "||"));
                if (j == aBitmap.length - 1)
                    builder.delete(builder.length() - 2, builder.length());
            }
        }
        return builder.toString();
    }
}

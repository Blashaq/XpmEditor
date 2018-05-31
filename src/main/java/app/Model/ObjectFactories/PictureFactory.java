package app.Model.ObjectFactories;

import app.Model.Structures.Picture.*;
import app.Utils.ArrayTools.ArrayTools;
import org.javatuples.Triplet;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

public class PictureFactory {
    public static Picture getPicture(Triplet<ArrayList<String>, Map<PictureTag, String>, Map<String, String>> parsedSection) {
        String picType = parsedSection.getValue1().get(PictureTag.PICTYPE).toLowerCase();
        if (picType.contains("line"))
            return new LinePicture(parsedSection);
        if (picType.contains("point"))
            return new PointPicture(parsedSection);
        return new PolygonPicture(parsedSection);
    }

    public static Picture getEmptyPicture(String pictype, int type, Integer subtype, int width, int height, int charsPerColor) {
        if (pictype.equals("pointpicture"))
            return getEmptyPoint(type, subtype, width, height, charsPerColor);
        if (pictype.equals("linepicture"))
            return getEmptyLine(type, subtype, width, height, charsPerColor);
        else
            return getEmptyPolygon(pictype, type, subtype, width, height, charsPerColor);

    }

    private static PointPicture getEmptyPoint(int type, Integer subtype, int width, int height, int charsPerColor) {
        LinkedHashMap<String, Integer> colors = new LinkedHashMap<>();
        String transparentColor = "";
        for (int i = 0; i < charsPerColor; i++)
            transparentColor += " ";
        colors.put(transparentColor, 0);
        TreeMap<Integer, String> texts = new TreeMap<>();
        texts.put(0, "default");
        int[][] argbBitmap = new int[height][width];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                argbBitmap[i][j] = 0;
            }
        }
        return new PointPicture(type, subtype, charsPerColor, colors, texts, argbBitmap);
    }

    private static LinePicture getEmptyLine(int type, Integer subtype, int width, int height, int charsPerColor) {
        LinkedHashMap<String, Integer> colors = new LinkedHashMap<>();
        String transparentColor = "";
        for (int i = 0; i < charsPerColor; i++)
            transparentColor += " ";
        colors.put(transparentColor, 0);
        TreeMap<Integer, String> texts = new TreeMap<>();
        texts.put(0, "default");
        int[][] argbBitmap = new int[height][width];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                argbBitmap[i][j] = 0;
            }
        }
        return new LinePicture(type, subtype, charsPerColor, colors, texts, argbBitmap);
    }

    private static PolygonPicture getEmptyPolygon(String pictype, int type, Integer subtype, int width, int height, int charsPerColor) {
        LinkedHashMap<String, Integer> colors = new LinkedHashMap<>();
        String whiteColor = "";
        for (int i = 0; i < charsPerColor; i++)
            whiteColor += " ";
        colors.put(whiteColor, 0xFFFFFFFF);
        TreeMap<Integer, String> texts = new TreeMap<>();
        texts.put(0, "default");
        int[][] argbBitmap = new int[height][width];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                argbBitmap[i][j] = 0xFFFFFFFF;
            }
        }
        return new PolygonPicture(type, subtype, charsPerColor, colors, texts, argbBitmap);
    }

}

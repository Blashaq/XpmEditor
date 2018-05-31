package app.Utils.PictureTools;

import app.Model.Structures.Picture.Picture;
import app.Model.Structures.Picture.PointPicture;
import app.Model.Structures.Picture.PolygonPicture;
import org.javatuples.Pair;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class PictureSaver {

    public static void saveAsNewFile(File file, Pair<ArrayList<? extends Picture>, Map<Integer, Integer>> picData) throws FileNotFoundException, UnsupportedEncodingException {
        PrintWriter out = new PrintWriter(file, "cp1250");
        Map<Integer, Integer> drawOrder = picData.getValue1();
        ArrayList<? extends Picture> pictures = picData.getValue0();
        saveDrawOrder(drawOrder, out);
        savePictures(pictures, out);
        out.close();
    }

    private static void saveDrawOrder(Map<Integer, Integer> drawOrder, PrintWriter out) {
        out.println("[_drawOrder]");
        for (int type : drawOrder.keySet()) {
            String line;
            if (type > 9)
                line = String.format("%s0x%2s,%s", "Type=", Integer.toHexString(type), drawOrder.get(type)).replace(" ", "0");
            else
                line = "Type=" + type + "," + drawOrder.get(type);
            out.println(line);
        }
        out.println("[end]\n");
    }

    private static void savePictures(ArrayList<? extends Picture> pictures, PrintWriter out) {
        for (Picture p : pictures) {
            savePicture(p, out);
        }
    }

    private static void savePicture(Picture p, PrintWriter out) {
        String pictype = getPicType(p);
        out.println(pictype);
        out.println("Type=0x" + p.getTypeString());
        if (p.getSubType() != null) {
            out.println("SubType=0x" + p.getSubTypeString());
        }
        int stringCounter = 1;
        TreeMap<Integer, String> texts = p.getTexts();
        for (int type : texts.keySet()) {
            out.println("string" + stringCounter++ + "=0x" + Integer.toHexString(type) + "," + texts.get(type));
        }
        out.println("dayxpm=\"" + p.getWidth() + " " + p.getHeight() + " " + p.getColorsCount() + " " + p.getCharsPerColor() + "\",");
        Map<String, Integer> colors = p.getColors();
        Map<Integer, String> invertedColors = new HashMap<>();
        for (String character : colors.keySet()) {
            String number;
            if (colors.get(character) != 0)
                number = String.format("#%6s", Integer.toHexString(colors.get(character) - 0xff000000)).replace(" ", "0").toUpperCase();
            else
                number = "None";
            String line = String.format("\"%s\tc %s\",", character, number);
            out.println(line);

            invertedColors.putIfAbsent(colors.get(character), character);
        }
        int[][] bitmap = p.getBitmap();
        for (int i = 0; i < bitmap.length; i++) {
            out.print("\"");
            for (int j = 0; j < bitmap[0].length; j++) {
                String character = invertedColors.get(bitmap[i][j]);
                out.print(character);
            }
            out.println("\"");
        }
        out.println("[end]");
        out.println();

    }

    private static String getPicType(Picture p) {
        String pictype = "";
        if (p instanceof PointPicture) {
            pictype = "[_point]";
        } else if (p instanceof PolygonPicture) {
            pictype = "[_polygon]";
        } else {
            pictype = "[_line]";
        }
        return pictype;
    }

}

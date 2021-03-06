package app.Model.Structures.Picture;

import org.javatuples.Triplet;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

public class PointPicture extends Picture {
    public PointPicture(Triplet<ArrayList<String>,Map<PictureTag,String>, Map<String, String>> parsedData) {
        super(parsedData);
    }
    public PointPicture(int type, Integer subtype, int charsPerColor, LinkedHashMap<String, Integer> colors, TreeMap<Integer, String> texts, int[][] argbBitmap){
        super(type, subtype, charsPerColor, colors, texts, argbBitmap);
    }

}


package app.Utils.PictureTools;

import app.Model.ObjectFactories.PictureFactory;
import app.Model.Structures.Picture.Picture;
import app.Model.Structures.Picture.PictureTag;
import app.Utils.ArrayTools.ArrayTools;
import app.Utils.PictureTools.Exceptions.EmptyBitmapException;
import app.Utils.PictureTools.Exceptions.InvalidFileException;
import app.Utils.PictureTools.Exceptions.XpmNotFoundException;
import org.javatuples.Pair;
import org.javatuples.Triplet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.*;

public class PictureLoader {
    private final PictureFactory _pictureFactory;
    private HashMap<String, PictureTag> tagsMap;

    public PictureLoader() {
        _pictureFactory = new PictureFactory();
        tagsMap = new HashMap<>();
        for (PictureTag tag : PictureTag.values()) {
            tagsMap.put(tag.getMarker(), tag);
        }
        tagsMap.put("dayxpm", PictureTag.XPM);
    }

    public Pair<ArrayList<? extends Picture>, Map<Integer, Integer>> processFile(File file) throws FileNotFoundException, InvalidFileException {
        Scanner sc = new Scanner(new FileInputStream(file), "cp1250");
        Pair<ArrayList<? extends Picture>, Map<Integer, Integer>> picturesAndDrawOrder;
        try {
            ArrayList<String> sections = cutFile(sc);
            picturesAndDrawOrder = createPictures(sections);
        } catch (NoSuchElementException e) {
            throw new InvalidFileException();
        }
        return picturesAndDrawOrder;
    }

    private Pair<ArrayList<? extends Picture>, Map<Integer, Integer>> createPictures(ArrayList<String> sections) throws InvalidFileException {
        ArrayList<Picture> pictures = new ArrayList<>();
        Map<Integer, Integer> drawOrder = new TreeMap<>();
        for (String section : sections) {
            try {
                if (section.toLowerCase().contains("draworder"))
                    drawOrder = parseDrawOrder(section);
                else {
                    Triplet<ArrayList<String>, Map<PictureTag, String>, Map<String, String>> parsedSection = parsePicture(section);
                    pictures.add(_pictureFactory.getPicture(parsedSection));
                }
            } catch (XpmNotFoundException | EmptyBitmapException e) {
                System.err.println(e.getMessage());
            } catch (NoSuchElementException e) {
                throw new InvalidFileException();
            }
        }
        if (pictures.isEmpty())
            throw new InvalidFileException();
        return Pair.with(pictures, drawOrder);
    }

    private ArrayList<String> cutFile(Scanner sc) throws NoSuchElementException {
        ArrayList<String> sections = new ArrayList<>();
        String line = "";
        while (sc.hasNextLine()) {
            StringBuilder section = new StringBuilder();
            line = sc.nextLine();
            while (!line.contains("[end]") && !line.contains("[End]") && sc.hasNextLine()) {
                section.append(line).append("\n");
                line = sc.nextLine();
            }
            sections.add(section.toString());
        }
        return sections;
    }

    private TreeMap<Integer, Integer> parseDrawOrder(String section) {
        TreeMap<Integer, Integer> drawOrder = new TreeMap<>();
        ArrayList<String> sectionLines = splitSectionString(section);
        cleanupSection(sectionLines);
        sectionLines.remove(0);
        for (String sectionLine : sectionLines) {
            String trimmedLine = sectionLine.split("=")[1];
            System.out.println(trimmedLine);
            String[] splitLine = trimmedLine.split(",");
            drawOrder.put(ArrayTools.parseNumber(splitLine[0]), ArrayTools.parseNumber(splitLine[1]));
        }
        return drawOrder;
    }

    private Triplet<ArrayList<String>, Map<PictureTag, String>, Map<String, String>> parsePicture(String section) throws XpmNotFoundException, EmptyBitmapException {
        ArrayList<String> sectionLines = splitSectionString(section);
        cleanupSection(sectionLines);
        ArrayList<String> pictureData = cutOffPictureData(sectionLines);
        Pair<Map<PictureTag, String>, Map<String, String>> tagsAndStrings = mapTags(sectionLines);
        return Triplet.with(pictureData, tagsAndStrings.getValue0(), tagsAndStrings.getValue1());
    }

    private ArrayList<String> splitSectionString(String section) {
        return new ArrayList<>(Arrays.asList(section.split("\n")));
    }

    private void cleanupSection(ArrayList<String> sectionLines) {
        try{
        for (int i = 0; !sectionLines.get(i).matches("\\[(_.*?)\\]"); ) {
            sectionLines.remove(i);
        }
        sectionLines.add(0, "PicType=" + sectionLines.get(0));
        sectionLines.remove(1);}
        catch (IndexOutOfBoundsException e){
            System.err.println("pusta sekcja!");
        }
    }

    private int findXpmSection(ArrayList<String> sectionLines) throws XpmNotFoundException {
        for (String line : sectionLines) {
            if (line.toLowerCase().contains("xpm=\""))
                return sectionLines.indexOf(line);
        }
        throw new XpmNotFoundException();
    }

    private ArrayList<String> cutOffPictureData(ArrayList<String> sectionLines) throws XpmNotFoundException {
        int xpmLineIndex = findXpmSection(sectionLines);

        String[] cutXpm = sectionLines.get(xpmLineIndex).replace("\"", "").replace(",", "").split("=")[1].split("\\s+");

        int height = Integer.parseInt(cutXpm[1]);

        ArrayList<String> pictureData = new ArrayList<>();

        int colorCount = Integer.parseInt(cutXpm[2]);
        int linesToCut = height + colorCount;
        while (linesToCut != 0) {
            String picLine = sectionLines.remove(xpmLineIndex + 1);
            pictureData.add(picLine);
            linesToCut--;
        }
        return pictureData;
    }

    private Pair<Map<PictureTag, String>, Map<String, String>> mapTags(ArrayList<String> sectionLines) throws EmptyBitmapException {
        Map<String, String> texts = new TreeMap<>();
        Map<PictureTag, String> picTags = new HashMap<>();

        for (String s : sectionLines) {
            try {
                String[] splitLine = s.split("=");
                if (splitLine[0].toLowerCase().contains("string"))
                    texts.put(splitLine[0], splitLine[1]);
                else {
                    PictureTag tag = tagsMap.get(splitLine[0].toLowerCase());
                    if (tag != null)
                        picTags.put(tag, splitLine[1]);
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                System.err.println("unparsable line: " + s);
            }

        }
        ParseXpmData(picTags);
        return Pair.with(picTags, texts);
    }

    private void ParseXpmData(Map<PictureTag, String> picTags) throws EmptyBitmapException {
        String xpmLine;

        xpmLine = picTags.remove(PictureTag.XPM);
        xpmLine = xpmLine.replace("\"", "").replace(",", "");
        String[] xpmValues = xpmLine.split("\\s+");
        String width = xpmValues[0];
        String height = xpmValues[1];
        String colorsCount = xpmValues[2];
        String charsPerColor = xpmValues[3];
        if (Integer.parseInt(width) == 0 || Integer.parseInt(height) == 0
                || Integer.parseInt(charsPerColor) == 0 || Integer.parseInt(charsPerColor) == 0)
            throw new EmptyBitmapException("brak wysokosci,szerokosci,ilosci kolorow lub ilosci charow na kolor! xpm: " + xpmLine);
        picTags.put(PictureTag.HEIGHT, height);
        picTags.put(PictureTag.WIDTH, width);
        picTags.put(PictureTag.COLOURSCOUNT, colorsCount);
        picTags.put(PictureTag.CHARSPERCOLOUR, charsPerColor);
    }

}





























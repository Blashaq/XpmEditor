package app.Utils.ArrayTools;

import java.util.Arrays;

public class ArrayTools {

    public static int[] moveMatrixToArray(int[][] matrix) {
        int[] data;
        if(matrix.length==0)
            return new int[0];
        data = new int[matrix.length * matrix[0].length];

        for (int i = 0; i < matrix.length; i++) {
            int beginIndex = i * matrix[0].length;
            for (int j = 0; j < matrix[0].length; j++) {
                data[beginIndex + j] = matrix[i][j];
            }
        }

        return data;
    }

    public static int[][] moveArrayToMatrix(int[] array, int matrixWidth) {
        if (matrixWidth == 0) return new int[0][0];
        int[][] matrix = new int[array.length / matrixWidth][matrixWidth];
        for (int i = 0; i < matrix.length; i++) {
            int offset = i * matrixWidth;
            for (int j = 0; j < matrixWidth; j++) {
                matrix[i][j] = array[offset + j];
            }
        }
        return matrix;
    }

    public static int[] cutCorner(int[] pictureData, int xPixelsToCut, int yPixelsToCut, int pictureWidth, int pictureHeight) {
        int[][] pictureMatrix = moveArrayToMatrix(pictureData, pictureWidth);
        int[][] trimmedMatrix = new int[yPixelsToCut][xPixelsToCut];
        for (int i = 0; i < trimmedMatrix.length; i++) {
            for (int j = 0; j < trimmedMatrix[0].length; j++) {
                trimmedMatrix[i][j] = pictureMatrix[i][j];
            }
        }
        return moveMatrixToArray(trimmedMatrix);
    }

    public static int[] cutPictureByHeight(int[] pictureData, int yPixelsToCut, int pictureWidth) {
        return Arrays.copyOfRange(pictureData, 0, yPixelsToCut * pictureWidth);
    }

    public static int[] cutPictureByWidth(int[] pictureData, int xPixelsToCut, int pictureWidth, int pictureHeight) {
        ;
        int[] pictureCutByX = new int[xPixelsToCut * pictureHeight];
        if (xPixelsToCut == 0) return pictureCutByX;
        for (int i = 0, j = 0; i < pictureData.length; i += pictureWidth) {
            int pixpos = i;
            do {
                pictureCutByX[j++] = pictureData[pixpos++];
            }
            while (j % xPixelsToCut != 0);
        }
        return pictureCutByX;
    }

    public static int parseNumber(String numberLine) {
        numberLine = numberLine.replaceAll("\\s+", "");
        if (numberLine.contains("0x"))
            return Integer.parseInt(numberLine.substring(2), 16);
        else if (numberLine.matches("[0-9]"))
            return Integer.parseInt(numberLine, 10);
        else return Integer.parseInt(numberLine.substring(1, 7), 16);
    }

}


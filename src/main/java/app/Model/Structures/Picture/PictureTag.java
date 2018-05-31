package app.Model.Structures.Picture;

public enum PictureTag {
    PICTYPE("pictype"),
    TYPE("type"),
    SUBTYPE("subtype"),
    NAME("string"),
    XPM("xpm"),
    WIDTH("width"),
    COLOURSCOUNT("colorscount"),
    CHARSPERCOLOUR("charspercolor"),
    HEIGHT("height");

    private final String marker;

    PictureTag(String marker) {
        this.marker = marker;
    }

    public String getMarker() {
        return marker;
    }

}

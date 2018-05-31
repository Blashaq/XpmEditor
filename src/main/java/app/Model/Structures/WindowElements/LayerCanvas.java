package app.Model.Structures.WindowElements;

import javafx.scene.canvas.Canvas;

public class LayerCanvas extends Canvas implements Comparable {
    private int layerNumber;
    private boolean isPreview = false;

    public LayerCanvas(int layerNumber, boolean isPreview) {
        super();
        this.isPreview = isPreview;
        this.layerNumber = layerNumber;
    }

    public Integer getLayerNumber() {
        return layerNumber;
    }

    public void setLayerNumber(int layerNumber) {
        this.layerNumber = layerNumber;
    }

    @Override
    public int compareTo(Object o) {
        if (o instanceof LayerCanvas) {
            if (this.getLayerNumber() < ((LayerCanvas) o).getLayerNumber())
                return -1;
            if (this.getLayerNumber() > ((LayerCanvas) o).getLayerNumber())
                return 1;
            if (isPreview && !((LayerCanvas) o).isPreview) {
                return -1;
            }
        }
        return 0;
    }
}

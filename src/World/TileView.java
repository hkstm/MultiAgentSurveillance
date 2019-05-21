package World;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class TileView extends ImageView {

    private int rowIndex;
    private int colIndex;
    private int state;

    public TileView(Image image, int rowIndex, int colIndex, int state) {
        super(image);
        this.rowIndex = rowIndex;
        this.colIndex = colIndex;
        this.state = state;
    }

    public int getRowIndex() {
        return rowIndex;
    }

    public int getColIndex() {
        return colIndex;
    }

    public int getState() {
        return state;
    }
}

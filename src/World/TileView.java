package World;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * The "tiles" that you see in when running the game (not building), ImageViews that explicitly store their position
 * on the grid and what kinda tiles they are
 * @author Kailhan
 */

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

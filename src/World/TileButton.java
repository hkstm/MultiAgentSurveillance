package World;

import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

public class TileButton extends Button {
    private int r;
    private int c;
    private int tileStatus;

    public TileButton(int r, int c, Image image, int tileSize, int tileStatus) {
        super();
        setMinSize(tileSize, tileSize);
        setMaxSize(tileSize, tileSize);
        BackgroundImage bImage = new BackgroundImage(image, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, new BackgroundSize(tileSize, tileSize, true, true, true, false));
        Background backGround = new Background(bImage);
        setBackground(backGround);
        this.r = r;
        this.c = c;
        this.tileStatus = tileStatus;
    }
    public int getRow() {
        return r;
    }

    public int getColumn() {
        return c;
    }

    public int getTileStatus() {
        return tileStatus;
    }
}

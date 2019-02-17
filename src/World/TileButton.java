package World;

import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

public class TileButton extends Button {
    private int x;
    private int y;
    private int tileStatus;

    public TileButton(int x, int y, Image image, int tileSize, int tileStatus) {
//        super(null, new ImageView(image));
        super(null);
//        BackgroundImage backgroundImage = new BackgroundImage(image, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT, BackgroundSize.DEFAULT);
//        Background background = new Background(backgroundImage);
//        setBackground(background);
        setMinSize(tileSize, tileSize);
        setMaxSize(tileSize, tileSize);
        BackgroundImage bImage = new BackgroundImage(image, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, new BackgroundSize(tileSize, tileSize, true, true, true, false));
        Background backGround = new Background(bImage);
        setBackground(backGround);
        this.x = x;
        this.y = y;
        this.tileStatus = tileStatus;
    }
    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getTileStatus() {
        return tileStatus;
    }

    public void setTileStatus(int tileStatus) {
        this.tileStatus = tileStatus;
    }
}

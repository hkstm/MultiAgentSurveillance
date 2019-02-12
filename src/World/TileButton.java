package World;

import javafx.scene.control.Button;
import javafx.scene.image.ImageView;

public class TileButton extends Button {
    private int x;
    private int y;
    public TileButton(int x, int y, ImageView imageView) {
        super(null, imageView);
        this.x = x;
        this.y = y;
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
}

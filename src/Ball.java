import javax.swing.*;
import java.awt.*;
import java.awt.Graphics2D ;
import java.awt.geom.Ellipse2D;

public class Ball extends JPanel{

        double x = 0;
        double y = 0;
        double angelx = 1;
        double angely = 1;
        double speed = 1.4;

        double angle =  (Math.random() * 0.0 + 361.0);
        double dirX = (speed)* Math.cos(angle);
        double dirY= (speed)* Math.sin(angle);

        //public void checkObs(double dirX, double dirY, /*obstacles*/){
        //    }


        public void move(){
            if (x + dirX < 0){
                dirX = -dirX;
            }
            else if(x + dirX > getWidth() - 50){
                dirX = -dirX;
            }
            else if (y + dirY < 0){
                dirY = -dirY;
            }
            else if (y + dirY > getHeight() - 50){
                dirY = -dirY;
            }

            x = x + dirX;
            y = y + dirY;
        }

        public void paint(Graphics gg){
            super.paint(gg);
            Graphics2D g = (Graphics2D) gg;
            g.fill(new Ellipse2D.Double(x,y,50,50));
        }

        public static void main(String[] args) throws InterruptedException {
            JFrame frame = new JFrame("ball");
            Ball aBall = new Ball();
            frame.add(aBall);
            frame.setSize(800,400);
            frame.setVisible(true);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            while(true){
                aBall.move();
                aBall.repaint();
                Thread.sleep(10);
            }
        }
    }


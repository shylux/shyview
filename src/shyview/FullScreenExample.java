package shyview;

import java.awt.Cursor;
import java.awt.DisplayMode;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
 
import javax.imageio.ImageIO;
import javax.swing.JFrame;
 
/**
 * @author Tom
 * 
 */
public class FullScreenExample extends JFrame {
	private static final long serialVersionUID = 1L;

	BufferStrategy strategy;
 
    Point mouseLocation;
 
    Thread runner = new Thread() {
        public void run() {
            BufferedImage img = null;
 
            try {
                img = ImageIO.read(new File("c:/pac.png"));
            } catch (IOException e) {
                e.printStackTrace();
            }
 
            if (img == null)
                return;
 
            while (true) {
                if (mouseLocation != null && FullScreenExample.this.isVisible()) {
                    Graphics2D g = (Graphics2D) strategy.getDrawGraphics();
                    g.clearRect(0, 0, 800, 600);
                    g.drawImage(img, mouseLocation.x, mouseLocation.y, null);
                    g.dispose();
                    strategy.show();
                }
                try {
                    sleep(50L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
 
        }
    };
 
    public FullScreenExample() {
 
        setUndecorated(true);
 
        GraphicsDevice device = GraphicsEnvironment
                .getLocalGraphicsEnvironment().getDefaultScreenDevice();
 
        setSize(800, 600);
 
        device.setFullScreenWindow(this);
        device.setDisplayMode(new DisplayMode(800, 600, 16, 85));
 
        addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    setVisible(false);
                    dispose();
                    System.exit(0);
                }
            }
        });
 
        addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                mouseLocation = e.getPoint();
            }
        });
 
        setVisible(true);
        createBufferStrategy(2);
        strategy = getBufferStrategy();
 
        // disable local pointer
        BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Cursor cur = Toolkit.getDefaultToolkit().createCustomCursor(img,
                new Point(1, 1), "");
        setCursor(cur);
 
        runner.start();
    }
 
    /**
     * @param args
     */
    public static void main(String[] args) {
        new FullScreenExample();
    }
 
}
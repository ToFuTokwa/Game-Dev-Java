import java.awt.*;
import java.awt.image.*;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class TileManager {

    private TileMap tileMap;
    private int tileWidth = 32;
    private int tileHeight = 32;
    private BufferedImage[] portalFrame; 
    private int animationFrame = 0;
    private long lastAnimationTime = 0;
    private final int animationDelay = 200; 
    private BufferedImage Dirt, GrassDirt, FullGreen, LeftFloat, MiddleFloat, RightFloat;
    
    public TileManager(TileMap tileMap) {
        this.tileMap = tileMap;
        // Using relative paths so it works on any computer/environment
        String portalPath = "Assets/portal/";
        String path = "Assets/tileSet/";

        try {
            Dirt = ImageIO.read(new File(path + "Dirt.png"));
            GrassDirt = ImageIO.read(new File(path + "GrassDirt.png"));
            FullGreen = ImageIO.read(new File(path + "FullGreen.png"));
            LeftFloat = ImageIO.read(new File(path + "LeftFloat.png"));
            MiddleFloat = ImageIO.read(new File(path + "MiddleFloat.png"));
            RightFloat = ImageIO.read(new File(path + "RightFloat.png"));

            portalFrame = new BufferedImage[6];
            for (int i = 0; i < 6; i++) {
                portalFrame[i] = ImageIO.read(new File(portalPath + "Dimensional_Portal-" + (i + 1) + ".png"));
            }

        } catch (IOException e) {
            System.err.println("Error loading tile images: " + e.getMessage());
        }
    }

    public void updateAnimation() {
        long now = System.currentTimeMillis();
        if (now - lastAnimationTime > animationDelay) {
            animationFrame++;
            if (portalFrame != null && animationFrame >= portalFrame.length) {
                animationFrame = 0;
            }
            lastAnimationTime = now;
        }
    }

    public Rectangle getBound(int row, int col) {
        return new Rectangle(col * tileWidth, row * tileHeight, tileWidth, tileHeight);
    }

    public TileMap getTileMap() {
        return tileMap;
    }

    public void draw(Graphics g) {
        updateAnimation(); // Updates the frame index for animated tiles
        
        int[][] map = tileMap.getMap();
        for (int row = 0; row < tileMap.getRows(); row++) {
            for (int col = 0; col < tileMap.getCols(); col++) {
                int tile = map[row][col];
                if (tile != 0) {
                    BufferedImage img = null;
                    switch (tile) {
                        case 1: img = Dirt; break;
                        case 2: img = GrassDirt; break;
                        case 3: img = FullGreen; break;
                        case 4: img = LeftFloat; break;
                        case 5: img = MiddleFloat; break;
                        case 6: img = RightFloat; break;
                        case 7: 
                            if (portalFrame != null && portalFrame.length > 0) {
                                img = portalFrame[animationFrame];
                            }
                            break;
                    }
                    if (img != null)
                        g.drawImage(img, col * tileWidth, row * tileHeight, tileWidth, tileHeight, null);
                }
                
                // Draw tile boundaries for the editor/debugging
                g.setColor(new Color(255, 0, 0, 50));
                g.drawRect(col * tileWidth, row * tileHeight, tileWidth, tileHeight);
            }
        }
    }
}
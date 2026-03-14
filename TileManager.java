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

    // Tile images
    private BufferedImage Dirt, GrassDirt, FullGreen, LeftFloat, MiddleFloat, RightFloat;

    // -----------------------
    // Constructor
    // -----------------------
    public TileManager(TileMap tileMap) {
        this.tileMap = tileMap;
        loadTileImages();
        loadPortalImages();
    }

    private void loadTileImages() {
        String path = "Assets/tileSet/";
        try {
            Dirt = ImageIO.read(new File(path + "Dirt.png"));
            GrassDirt = ImageIO.read(new File(path + "GrassDirt.png"));
            FullGreen = ImageIO.read(new File(path + "FullGreen.png"));
            LeftFloat = ImageIO.read(new File(path + "LeftFloat.png"));
            MiddleFloat = ImageIO.read(new File(path + "MiddleFloat.png"));
            RightFloat = ImageIO.read(new File(path + "RightFloat.png"));
        } catch (IOException e) {
            System.err.println("Error loading tile images: " + e.getMessage());
        }
    }

    private void loadPortalImages() {
        String portalPath = "Assets/portal/";
        portalFrame = new BufferedImage[6];
        try {
            for (int i = 0; i < 6; i++) {
                portalFrame[i] = ImageIO.read(new File(portalPath + "Dimensional_Portal-" + (i + 1) + ".png"));
            }
        } catch (IOException e) {
            System.err.println("Error loading portal images: " + e.getMessage());
        }
    }

    // -----------------------
    // Update portal animation
    // -----------------------
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

    // -----------------------
    // Draw tiles
    // -----------------------
    public void draw(Graphics g) {
        updateAnimation();
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

                // Optional: tile boundaries for debugging
                g.setColor(new Color(255, 0, 0, 50));
                g.drawRect(col * tileWidth, row * tileHeight, tileWidth, tileHeight);
            }
        }
    }

    // -----------------------
    // Get tile bounding rectangle
    // -----------------------
    public Rectangle getBound(int row, int col) {
        return new Rectangle(col * tileWidth, row * tileHeight, tileWidth, tileHeight);
    }

    // -----------------------
    // Helper: is this tile a portal?
    // -----------------------
    public boolean isPortal(int row, int col) {
        if (row < 0 || row >= tileMap.getRows() || col < 0 || col >= tileMap.getCols()) return false;
        return tileMap.getTile(row, col) == 7;
    }

    // -----------------------
    // Getters for player & collision
    // -----------------------
    public int getTileWidth() { return tileWidth; }
    public int getTileHeight() { return tileHeight; }

    public TileMap getTileMap() { return tileMap; }

    // -----------------------
    // Change current TileMap (for level switching)
    // -----------------------
    public void setTileMap(TileMap newMap) {
        this.tileMap = newMap;
    }
}
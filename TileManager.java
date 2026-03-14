import java.awt.*;
import java.awt.image.*;
import javax.imageio.ImageIO;
import java.io.File;

public class TileManager {

    private TileMap tileMap;
    private int tileWidth = 32;
    private int tileHeight = 32;

    private BufferedImage[] portalFrame; 
    private int animationFrame = 0;
    private long lastAnimationTime = 0;
    private final int animationDelay = 150; // Slightly faster for smoother animation

    private BufferedImage Dirt, GrassDirt, FullGreen;

    public TileManager(TileMap tileMap) {
        this.tileMap = tileMap;
        loadTileImages();
        loadPortalImages();
    }

    private void loadTileImages() {
        try {
            String path = "Assets/tileSet/";
            Dirt = ImageIO.read(new File(path + "Dirt.png"));
            GrassDirt = ImageIO.read(new File(path + "GrassDirt.png"));
            FullGreen = ImageIO.read(new File(path + "FullGreen.png"));
        } catch (Exception e) { 
            System.out.println("Check Assets/tileSet/ - Dirt.png, GrassDirt.png, or FullGreen.png missing.");
        }
    }

    private void loadPortalImages() {
        portalFrame = new BufferedImage[6]; // Exactly 6 images
        String path = "Assets/portal/";
        
        for (int i = 0; i < 6; i++) {
            // This looks for portal1.png, portal2.png, ..., portal6.png
            String fileName = "Dimensional_Portal-" + (i + 1) + ".png";
            File file = new File(path + fileName);
            
            try {
                if (file.exists()) {
                    portalFrame[i] = ImageIO.read(file);
                } else {
                    // This will tell you EXACTLY where the file should be in your console
                    System.out.println("CRITICAL: Missing " + file.getAbsolutePath());
                }
            } catch (Exception e) {
                System.out.println("Error reading: " + fileName);
            }
        }
    }

    public void draw(Graphics g) {
        // Animation Logic
        long elapsed = System.currentTimeMillis() - lastAnimationTime;
        if (elapsed > animationDelay) {
            animationFrame = (animationFrame + 1) % 6; // Loops 0-5
            lastAnimationTime = System.currentTimeMillis();
        }

        int[][] map = tileMap.getMap();
        for (int row = 0; row < map.length; row++) {
            for (int col = 0; col < map[row].length; col++) {
                int tileID = map[row][col];

                if (tileID == 7) { // Portal ID
                    if (portalFrame[animationFrame] != null) {
                        // Draws the animated sprite
                        g.drawImage(portalFrame[animationFrame], 
                                    col * tileWidth - 16, row * tileHeight - 32, 
                                    64, 64, null);
                    } else {
                        // Fallback: If image failed to load, draw purple
                        g.setColor(Color.MAGENTA);
                        g.fillRect(col * tileWidth, row * tileHeight, tileWidth, tileHeight);
                    }
                } else if (tileID != 0) {
                    BufferedImage img = null;
                    if (tileID == 1) img = Dirt;
                    else if (tileID == 2) img = GrassDirt;
                    else if (tileID == 3) img = FullGreen;

                    if (img != null) {
                        g.drawImage(img, col * tileWidth, row * tileHeight, tileWidth, tileHeight, null);
                    }
                }
            }
        }
    }

    public Rectangle getBound(int row, int col) {
        return new Rectangle(col * tileWidth, row * tileHeight, tileWidth, tileHeight);
    }

    public Rectangle getPortalBounds(int row, int col) {
        return new Rectangle(col * tileWidth - 16, row * tileHeight - 32, 64, 64);
    }

    public boolean isPortal(int row, int col) {
        return tileMap.getTile(row, col) == 7;
    }

    public void setTileMap(TileMap newMap) { this.tileMap = newMap; }
    public TileMap getTileMap() { return tileMap; }
}
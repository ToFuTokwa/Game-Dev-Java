import java.awt.*;
import java.awt.image.*;
import javax.imageio.ImageIO;
import java.io.File;

public class TileManager {
    private TileMap tileMap;
    private final int SIZE = 32;
    private BufferedImage[] portalFrames = new BufferedImage[6];
    private int animFrame = 0;
    private long lastAnimTime = 0;
    private BufferedImage dirt, grass, fullGreen;

    public TileManager(TileMap map) {
        this.tileMap = map;
        loadImages();
    }

    private void loadImages() {
        try {
            String path = "Assets/tileSet/";
            dirt = ImageIO.read(new File(path + "Dirt.png"));
            grass = ImageIO.read(new File(path + "GrassDirt.png"));
            fullGreen = ImageIO.read(new File(path + "FullGreen.png"));
            
            for (int i = 0; i < 6; i++) {
                portalFrames[i] = ImageIO.read(new File("Assets/portal/Dimensional_Portal-" + (i + 1) + ".png"));
            }
        } catch (Exception e) { System.out.println("Image loading failed."); }
    }

    public void draw(Graphics g) {
        // Portal Animation timing
        if (System.currentTimeMillis() - lastAnimTime > 150) {
            animFrame = (animFrame + 1) % 6;
            lastAnimTime = System.currentTimeMillis();
        }

        int[][] grid = tileMap.getMap();
        for (int r = 0; r < grid.length; r++) {
            for (int c = 0; c < grid[r].length; c++) {
                int id = grid[r][c];
                if (id == 7) { // Portal
                    g.drawImage(portalFrames[animFrame], c * SIZE - 16, r * SIZE - 32, 64, 64, null);
                } else if (id != 0) { // Solid Tiles
                    BufferedImage img = (id == 1) ? dirt : (id == 2) ? grass : (id == 3) ? fullGreen : null;
                    if (img != null) g.drawImage(img, c * SIZE, r * SIZE, SIZE, SIZE, null);
                }
            }
        }
    }

    public Rectangle getBound(int r, int c) { return new Rectangle(c * SIZE, r * SIZE, SIZE, SIZE); }
    public Rectangle getPortalBounds(int r, int c) { return new Rectangle(c * SIZE - 16, r * SIZE - 32, 64, 64); }
    public boolean isPortal(int r, int c) { return tileMap.getTile(r, c) == 7; }
    public void setTileMap(TileMap m) { this.tileMap = m; }
    public TileMap getTileMap() { return tileMap; }
}
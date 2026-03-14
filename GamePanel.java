import java.awt.*;
import javax.swing.*;

public class GamePanel extends JPanel implements Runnable {

    private final int tileSize = 32;
    private final int maxColumns = 40;
    private final int maxRows = 23;
    private final int screenWidth = maxColumns * tileSize;
    private final int screenHeight = maxRows * tileSize;

    private Image backgroundImage = new ImageIcon("Assets/tempBackGround.jpg").getImage();
    private final int framesPerSecond = 60;

    private Player player1 = new Player();
    private LevelManager levelManager = new LevelManager();
    private TileManager tileManager;
    private CheckCollision collisionChecker = new CheckCollision();
    private Thread gameThread;

    public GamePanel() {
        this.setPreferredSize(new Dimension(screenWidth, screenHeight));
        this.setBackground(Color.black);
        this.setDoubleBuffered(true);
        this.addKeyListener(player1);
        this.addMouseListener(player1);
        this.setFocusable(true);

        tileManager = new TileManager(levelManager.getCurrentLevel());
    }

    public void startGameThread() {
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void run() {
        double drawInterval = 1000000000.0 / framesPerSecond;
        double nextDrawTime = System.nanoTime() + drawInterval;

        while (gameThread != null) {
            update();
            repaint();
            try {
                double remainingTime = (nextDrawTime - System.nanoTime()) / 1000000.0;
                if (remainingTime < 0) remainingTime = 0;
                Thread.sleep((long) remainingTime);
                nextDrawTime += drawInterval;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void update() {
        player1.update(collisionChecker, tileManager);

        // --- PORTAL INTERACTION LOGIC ---
        if (player1.isInteractPressed()) {
            Rectangle hit = player1.getBounds();
            int tw = tileManager.getTileWidth();
            int th = tileManager.getTileHeight();
            
            int colStart = hit.x / tw;
            int colEnd = (hit.x + hit.width) / tw;
            int rowStart = hit.y / th;
            int rowEnd = (hit.y + hit.height) / th;

            boolean foundPortal = false;
            for (int r = rowStart; r <= rowEnd; r++) {
                for (int c = colStart; c <= colEnd; c++) {
                    if (tileManager.isPortal(r, c)) {
                        foundPortal = true;
                        break;
                    }
                }
                if (foundPortal) break;
            }

            if (foundPortal) {
                levelManager.loadNextLevel();
                tileManager.setTileMap(levelManager.getCurrentLevel());
                player1.setPosition(100, 100); 
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        if (tileManager != null) tileManager.draw(g);
        player1.draw(g);
    }
}
import java.awt.*;
import javax.swing.*;
import java.io.File;

public class GamePanel extends JPanel implements Runnable {
    private Player player = new Player();
    private LevelManager levelManager = new LevelManager();
    private TileManager tileManager;
    private CheckCollision collisionChecker = new CheckCollision();
    private Image currentBackground;
    private Thread gameThread;

    public GamePanel() {
        this.setPreferredSize(new Dimension(1280, 736));
        this.setFocusable(true);
        this.addKeyListener(player);
        this.addMouseListener(player);
        
        tileManager = new TileManager(levelManager.getCurrentLevel());
        updateLevelVisuals();
    }

    private void updateLevelVisuals() {
        String bgPath = levelManager.getCurrentBackgroundPath();
        if (new File(bgPath).exists()) {
            this.currentBackground = new ImageIcon(bgPath).getImage();
        }
    }

    public void startGameThread() {
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void run() {
        while (gameThread != null) {
            update();
            repaint();
            try { Thread.sleep(16); } catch (InterruptedException e) { e.printStackTrace(); }
        }
    }

    private void update() {
        player.update(collisionChecker, tileManager);

        if (player.isInteractPressed()) {
            checkPortalContact();
        }
    }

    private void checkPortalContact() {
        Rectangle hitbox = player.getHitbox();
        int row = hitbox.y / 32;
        int col = hitbox.x / 32;

        for (int r = row - 1; r <= row + 1; r++) {
            for (int c = col - 1; c <= col + 1; c++) {
                if (tileManager.isPortal(r, c)) {
                    Rectangle portalArea = tileManager.getPortalBounds(r, c);
                    if (hitbox.intersects(portalArea)) {
                        advanceToNextLevel(); // Automatically moves to next level
                        return;
                    }
                }
            }
        }
    }

    private void advanceToNextLevel() {
        player.resetInputs();
        
        // Calculate next level; loops back to 0 if at the end
        int nextLevel = (levelManager.getCurrentLevelIndex() + 1) % 3;

        levelManager.setLevel(nextLevel);
        tileManager.setTileMap(levelManager.getCurrentLevel());
        updateLevelVisuals();
        
        // Reset player to a default starting position
        player.setPosition(100, 100);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (currentBackground != null) g.drawImage(currentBackground, 0, 0, 1280, 736, null);
        tileManager.draw(g);
        player.draw(g);
    }
}
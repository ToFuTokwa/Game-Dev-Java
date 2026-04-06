import java.awt.*;
import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GamePanel extends JPanel implements Runnable {
    private CardLayout cardLayout; // new change "Func/GameOver"
    private JPanel mainPanel; // new change "func/gameover"
    private Player player = new Player();
    private LevelManager levelManager = new LevelManager();
    private TileManager tileManager;
    private CheckCollision collisionChecker = new CheckCollision();
    private List<Enemy> enemies = new ArrayList<>();
    private Image currentBackground;
    private Thread gameThread;

    public GamePanel(CardLayout cardLayout, JPanel mainPanel) {
        this.cardLayout = cardLayout; // --
        this.mainPanel = mainPanel; // --
        this.setPreferredSize(new Dimension(1280, 736));
        this.setFocusable(true);
        this.addKeyListener(player);
        this.addMouseListener(player);
        
        tileManager = new TileManager(levelManager.getCurrentLevel());
        spawnEnemies(); // Load enemies for the first level
        updateLevelVisuals();
    }

    private void spawnEnemies() {
        enemies.clear();
        int[][] grid = tileManager.getTileMap().getMap();
        for (int r = 0; r < grid.length; r++) {
            for (int c = 0; c < grid[r].length; c++) {
                if (grid[r][c] == 8) { // Our Enemy Spawn ID
                    enemies.add(new Enemy(c * 32, r * 32));
                }
            }
        }
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
        // Pass the entire list of enemies
        player.update(collisionChecker, tileManager, enemies); 

        playerDead();

        for (Enemy e : enemies) {
            e.update(1.0f/60.0f, player, collisionChecker, tileManager);
        }

        if (player.isInteractPressed()) {
            checkPortalContact();
        }
    }

    public void playerDead(){ // new
        if (player.isDead()) {
            cardLayout.show(mainPanel, "GameOver");
            mainPanel.getComponent(2).requestFocusInWindow(); // Focus the GameOverPanel
        }
    }

    public void resetGame(){ //new
        player.resetStatus(); // Create this method in Player.java
        levelManager.setLevel(0); // Start back at level 1
        tileManager.setTileMap(levelManager.getCurrentLevel());
        spawnEnemies();
        player.setPosition(100, 100);
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
                        advanceToNextLevel();
                        return;
                    }
                }
            }
        }
    }

    private void advanceToNextLevel() {
        player.resetInputs();
        
        // Move to the next level index
        int nextLevel = (levelManager.getCurrentLevelIndex() + 1) % 3;
        levelManager.setLevel(nextLevel);
        
        // Update the map and visuals
        tileManager.setTileMap(levelManager.getCurrentLevel());
        spawnEnemies(); // SPAWN NEW ENEMIES FOR NEW LEVEL
        updateLevelVisuals();
        
        // Find portal in the new level
        Point spawnPoint = tileManager.getPortalLocation();
        
        // OFFSET: Subtract 48 pixels to spawn the character above the tile
        // This prevents them from being stuck "inside" the floor
        player.setPosition(spawnPoint.x, spawnPoint.y - 48);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (currentBackground != null) g.drawImage(currentBackground, 0, 0, 1280, 736, null);
        tileManager.draw(g);
        for (Enemy e : enemies) {
            e.draw(g);
        }

        player.draw(g);
    }
}
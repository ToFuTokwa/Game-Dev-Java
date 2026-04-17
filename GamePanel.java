import java.awt.*;
import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GamePanel extends JPanel implements Runnable {
    SoundPLayer soundPlayer = new SoundPLayer();
    private CardLayout cardLayout; 
    private JPanel mainPanel; 
    private Player player = new Player();
    private LevelManager levelManager = new LevelManager();
    private TileManager tileManager;
    private CheckCollision collisionChecker = new CheckCollision();
    private List<Enemy> enemies = new ArrayList<>();
    private Image currentBackground;
    private Thread gameThread;
    private int HPMax = player.MAX_HP;

    public GamePanel(CardLayout cardLayout, JPanel mainPanel) {
        soundPlayer.stop("GameMusic");
        soundPlayer.loop("MenuMusic");
        this.cardLayout = cardLayout; 
        this.mainPanel = mainPanel; 
        this.setPreferredSize(new Dimension(1280, 736));
        this.setFocusable(true);
        this.addKeyListener(player);
        this.addMouseListener(player);
        
        tileManager = new TileManager(levelManager.getCurrentLevel());
        spawnEnemies(); 
        updateLevelVisuals();
        spawnPlayer(); 
    }

    private void spawnPlayer() {
        Point spawnPoint = tileManager.getPlayerSpawnLocation();
        player.setPosition(spawnPoint.x, spawnPoint.y - 48);
    }

    private void spawnEnemies() {
        enemies.clear();
        int[][] grid = tileManager.getTileMap().getMap();
        for (int r = 0; r < grid.length; r++) {
            for (int c = 0; c < grid[r].length; c++) {
                if (grid[r][c] == 8) { 
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
        player.update(collisionChecker, tileManager, enemies); 

        playerDead();

        // FIX: Remove dead enemies so the list eventually becomes empty
        enemies.removeIf(e -> e.isDead());

        for (Enemy e : enemies) {
            e.update(1.0f/60.0f, player, collisionChecker, tileManager);
        }

        if (player.isInteractPressed()) {
            checkPortalContact();
        }
    }

    public void playerDead(){ 
        if (player.isDead()) {
            cardLayout.show(mainPanel, "GameOver");
            mainPanel.getComponent(2).requestFocusInWindow(); 
        }
    }

    public void resetGame(){ 
        player.resetStatus(); 
        levelManager.setLevel(0); 
        tileManager.setTileMap(levelManager.getCurrentLevel());
        spawnEnemies();
        spawnPlayer(); 
    }

    private void checkPortalContact() {
        // Guard: Prevent interaction if enemies still exist
        if (!enemies.isEmpty()) {
            return;
        }

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

        // Your existing healing logic
        int healAmount = (int) (HPMax * 0.10); 
        player.heal(healAmount);

        int currentLevel = levelManager.getCurrentLevelIndex();

        // Check if this was the last level (Level 3 is index 2)
        if (currentLevel == 2) {
            cardLayout.show(mainPanel, "Ending");
            mainPanel.getComponent(3).requestFocusInWindow(); // Target the EndingPanel
        } else {
            // Otherwise, move to next level normally
            int nextLevel = currentLevel + 1;
            levelManager.setLevel(nextLevel);
            tileManager.setTileMap(levelManager.getCurrentLevel());
            spawnEnemies(); 
            updateLevelVisuals();
            spawnPlayer();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (currentBackground != null) g.drawImage(currentBackground, 0, 0, 1280, 736, null);
        
        // Pass the condition: draw portal only if enemies list is empty
        tileManager.draw(g, enemies.isEmpty());
        
        for (Enemy e : enemies) {
            e.draw(g);
        }
        player.draw(g);
    }
}
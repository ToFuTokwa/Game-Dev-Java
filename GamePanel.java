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
        refreshVisuals();
    }

    private void refreshVisuals() {
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
            updateGame();
            repaint();
            try { Thread.sleep(16); } catch (InterruptedException e) { e.printStackTrace(); }
        }
    }

    private void updateGame() {
        player.update(collisionChecker, tileManager);

        if (player.isInteractPressed()) { // Fixed: method name from Player.java
            checkPortalInteraction();
        }
    }

    private void checkPortalInteraction() {
        Rectangle pBounds = player.getHitbox(); // Fixed: method name from Player.java
        int row = pBounds.y / 32;
        int col = pBounds.x / 32;

        for (int r = row - 1; r <= row + 1; r++) {
            for (int c = col - 1; c <= col + 1; c++) {
                if (tileManager.isPortal(r, c)) {
                    Rectangle portalArea = tileManager.getPortalBounds(r, c);
                    if (pBounds.intersects(portalArea)) {
                        handleLevelTransition();
                        return;
                    }
                }
            }
        }
    }

    private void handleLevelTransition() {
        player.resetInputs();
        String[] levelChoices = levelManager.getLevelNames();
        int selection = JOptionPane.showOptionDialog(this, "Select Level", "Portal Travel",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, levelChoices, levelChoices[0]);

        if (selection != -1) {
            levelManager.setLevel(selection);
            tileManager.setTileMap(levelManager.getCurrentLevel());
            refreshVisuals();
            player.setPosition(100, 100);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // 1. Draw Background
        if (currentBackground != null) g.drawImage(currentBackground, 0, 0, 1280, 736, null);
        
        // 2. Draw Tiles
        tileManager.draw(g);
        
        // 3. Draw Player (FIX: Added this call so the sprite actually shows up)
        player.draw(g);
    }
}
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

        // Check nearby tiles for a portal
        for (int r = row - 1; r <= row + 1; r++) {
            for (int c = col - 1; c <= col + 1; c++) {
                if (tileManager.isPortal(r, c)) {
                    Rectangle portalArea = tileManager.getPortalBounds(r, c);
                    if (hitbox.intersects(portalArea)) {
                        promptLevelChange();
                        return;
                    }
                }
            }
        }
    }

    private void promptLevelChange() {
        player.resetInputs();
        String[] options = levelManager.getLevelNames();
        int choice = JOptionPane.showOptionDialog(this, "Where would you like to travel?", "Portal Travel",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

        if (choice != -1) {
            levelManager.setLevel(choice);
            tileManager.setTileMap(levelManager.getCurrentLevel());
            updateLevelVisuals();
            player.setPosition(100, 100);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (currentBackground != null) g.drawImage(currentBackground, 0, 0, 1280, 736, null);
        tileManager.draw(g);
        player.draw(g);
    }
}
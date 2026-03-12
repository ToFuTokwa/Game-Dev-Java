import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Player implements KeyListener, MouseListener {

    private final int tileSize = 32;
    private final int screenWidth = 40 * tileSize;

    // Use doubles for internal math to keep movement smooth
    private double playerX = 100;
    private double playerY = 100; 
    private final int playerWidth = 64;
    private final int playerHeight = 64;

    private double playerSpeed = 3.0;
    private double velocityY = 0;
    private final double gravity = 0.5;
    private final double jumpStrength = -11;
    
    private BufferedImage[] idle, walking, jumping, attacking;
    private BufferedImage[] lastAnimation = null;
    private int currentFrameIndex = 0;
    private long lastFrameTime = 0;
    private final int frameDuration = 120; 
    private int attackFrameDuration = 100; 
    
    private boolean facingRight = true;
    private boolean isMoving = false;
    private boolean isGrounded = false;
    private boolean isAttacking = false;

    private boolean leftPressed, rightPressed, spacePressed;

    public Player() {
        loadAnimations();
    }

    // Returns standard Rectangle to fix the "Line 119" error
    public Rectangle getBounds() {
        return new Rectangle((int)playerX + 15, (int)playerY + 10, playerWidth - 35, playerHeight - 10);
    }

    public void playerControl(CheckCollision checker, TileManager tileManager) {
        // --- 1. Horizontal Movement ---
        double oldX = playerX;
        if (leftPressed) playerX -= playerSpeed;
        if (rightPressed) playerX += playerSpeed;
        
        if (checker.isColliding(this, tileManager)) {
            playerX = oldX; 
        }

        // --- 2. Vertical Movement & Gravity ---
        if (!isGrounded) {
            velocityY += gravity; // Only apply gravity if in the air
        } else {
            velocityY = 0; // Solidly lock velocity to 0 on ground
        }

        double oldY = playerY;
        playerY += velocityY;

        if (checker.isColliding(this, tileManager)) {
            if (velocityY > 0) { // Landing
                isGrounded = true;
                // Snap to top of tile
                playerY = ((int)(playerY + playerHeight) / tileSize) * tileSize - playerHeight;
                velocityY = 0;
            } else if (velocityY < 0) { // Hitting ceiling
                playerY = oldY;
                velocityY = 0;
            }
        } else {
            // We aren't colliding, but are we actually in the air?
            // We check 1 pixel below us to see if there's still a floor.
            playerY += 1; 
            if (!checker.isColliding(this, tileManager)) {
                isGrounded = false;
            }
            playerY -= 1; // Move back to original position
        }

        // --- 3. Jump Logic ---
        if (isGrounded && spacePressed) {
            velocityY = jumpStrength;
            isGrounded = false;
        }

        if (leftPressed) facingRight = false;
        if (rightPressed) facingRight = true;
        isMoving = (leftPressed || rightPressed);

        // Screen Bounds
        if (playerX < 0) playerX = 0;
        if (playerX + playerWidth > screenWidth) playerX = screenWidth - playerWidth;
    }

    // --- Animation Logic (Unchanged but included for "One File" request) ---
    private void loadAnimations() {
        String base = "Assets/playerSprite/";
        idle = loadFrames(base + "playerIdle/", 4, 1);
        walking = loadFrames(base + "playerWalk/", 12, 5);
        jumping = loadFrames(base + "playerJump/", 8, 23);
        attacking = loadFrames(base + "playerAttack/", 8, 43);
    }

    private BufferedImage[] loadFrames(String folderPath, int count, int startIndex) {
        BufferedImage[] frames = new BufferedImage[count];
        for (int i = 0; i < count; i++) {
            String fileName = folderPath + "AnimationSheet_Character-" + (startIndex + i) + ".png.png";
            try {
                File f = new File(fileName);
                if (f.exists()) frames[i] = ImageIO.read(f);
            } catch (IOException e) { e.printStackTrace(); }
        }
        return frames;
    }

    public void AnimationStateHandling() {
        BufferedImage[] currentAnim = getCurrentAnimation();
        if (currentAnim == null || currentAnim.length == 0) return;
        long currentTime = System.currentTimeMillis();
        if (currentAnim != lastAnimation) { currentFrameIndex = 0; lastAnimation = currentAnim; }
        int dur = isAttacking ? attackFrameDuration : frameDuration;
        if (currentTime - lastFrameTime > dur) { currentFrameIndex++; lastFrameTime = currentTime; }
        if (currentFrameIndex >= currentAnim.length) {
            if (isAttacking) { isAttacking = false; currentFrameIndex = 0; }
            else if (!isGrounded) currentFrameIndex = currentAnim.length - 1;
            else currentFrameIndex = 0;
        }
    }

    private BufferedImage[] getCurrentAnimation() {
        if (isAttacking) return attacking;
        if (!isGrounded) return jumping;
        if (isMoving) return walking;
        return idle;
    }

    public void draw(Graphics g) {
        BufferedImage[] currentAnim = getCurrentAnimation();
        if (currentAnim != null && currentFrameIndex < currentAnim.length) {
            BufferedImage frame = currentAnim[currentFrameIndex];
            if (facingRight) {
                g.drawImage(frame, (int)playerX, (int)playerY, playerWidth, playerHeight, null);
            } else {
                g.drawImage(frame, (int)playerX + playerWidth, (int)playerY, -playerWidth, playerHeight, null);
            }
        }
        g.setColor(Color.CYAN);
        g.drawRect((int)playerX + 15, (int)playerY + 10, playerWidth - 35, playerHeight - 10);
    }

    public void update(CheckCollision checker, TileManager tileManager) {
        playerControl(checker, tileManager);
        AnimationStateHandling();      
    }

    // --- Input Listeners ---
    @Override public void keyTyped(KeyEvent e) {}
    @Override public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_A) leftPressed = true;
        if (code == KeyEvent.VK_D) rightPressed = true;
        if (code == KeyEvent.VK_SPACE) spacePressed = true;
    }
    @Override public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_A) leftPressed = false;
        if (code == KeyEvent.VK_D) rightPressed = false;
        if (code == KeyEvent.VK_SPACE) spacePressed = false;
    }
    @Override public void mouseClicked(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1 && !isAttacking) {
            isAttacking = true; currentFrameIndex = 0;
        }
    }
    @Override public void mousePressed(MouseEvent e) {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}
}
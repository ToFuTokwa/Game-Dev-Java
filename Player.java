import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Player implements KeyListener, MouseListener {

    private final int tileSize = 32;
    private final int screenWidth = 40 * tileSize;

    private double worldX = 100;
    private double worldY = 100; 
    private final int playerWidth = 64;
    private final int playerHeight = 64;

    private double movementSpeed = 3.5;
    private double verticalSpeed = 0;
    private final double gravityForce = 0.5;
    private final double jumpPower = -11;

    private BufferedImage[] idleAnim, walkingAnim, jumpingAnim, attackingAnim;
    private BufferedImage[] currentlyPlayingAnim = null;
    private int animationFrameIndex = 0;
    private long lastFrameTime = 0;
    private final int normalFrameDelay = 120; 
    private final int attackFrameDelay = 100; 

    private boolean isFacingRight = true;
    private boolean isCurrentlyMoving = false;
    private boolean isOnGround = false;
    private boolean isAttacking = false;

    private boolean isLeftPressed, isRightPressed, isJumpPressed, isInteractPressed;

    public Player() {
        loadAnimations();
    }

    // Adjusted Hitbox to be slightly smaller than the sprite for smoother movement
    public Rectangle getBounds() {
        return new Rectangle((int)worldX + 18, (int)worldY + 12, playerWidth - 36, playerHeight - 14);
    }

    public void handleMovement(CheckCollision collisionChecker, TileManager tileManager) {
        // --- HORIZONTAL MOVEMENT ---
        double dx = 0;
        if (isLeftPressed) dx -= movementSpeed;
        if (isRightPressed) dx += movementSpeed;

        worldX += dx;
        if (collisionChecker.isColliding(this, tileManager)) {
            worldX -= dx; // Push back if hitting a wall
        }

        // --- VERTICAL MOVEMENT ---
        if (!isOnGround) {
            verticalSpeed += gravityForce;
        } else {
            verticalSpeed = 0;
        }

        worldY += verticalSpeed;
        
        if (collisionChecker.isColliding(this, tileManager)) {
            if (verticalSpeed > 0) { // Falling/Landing
                isOnGround = true;
                // Snap to top of the tile to prevent getting stuck
                worldY = (Math.floor((worldY + playerHeight - 2) / tileSize) * tileSize) - (playerHeight - 2);
                verticalSpeed = 0;
            } else if (verticalSpeed < 0) { // Hitting ceiling
                worldY -= verticalSpeed;
                verticalSpeed = 0;
            }
        } else {
            // Check if there is still ground beneath us
            worldY += 1;
            if (!collisionChecker.isColliding(this, tileManager)) {
                isOnGround = false;
            }
            worldY -= 1;
        }

        if (isOnGround && isJumpPressed) {
            verticalSpeed = jumpPower;
            isOnGround = false;
        }

        // Direction and State
        if (dx < 0) isFacingRight = false;
        else if (dx > 0) isFacingRight = true;
        isCurrentlyMoving = (dx != 0);

        // Screen Boundaries
        if (worldX < 0) worldX = 0;
        if (worldX + playerWidth > screenWidth) worldX = screenWidth - playerWidth;
    }

    private void loadAnimations() {
        String path = "Assets/playerSprite/";
        idleAnim = loadFrames(path + "playerIdle/", 4, 1);
        walkingAnim = loadFrames(path + "playerWalk/", 12, 5);
        jumpingAnim = loadFrames(path + "playerJump/", 8, 23);
        attackingAnim = loadFrames(path + "playerAttack/", 8, 43);
    }

    private BufferedImage[] loadFrames(String folder, int count, int startNumber) {
        BufferedImage[] frames = new BufferedImage[count];
        for (int i = 0; i < count; i++) {
            String file = folder + "AnimationSheet_Character-" + (startNumber + i) + ".png.png";
            try {
                File imageFile = new File(file);
                if (imageFile.exists()) {
                    frames[i] = ImageIO.read(imageFile);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return frames;
    }

    public void updateAnimationState() {
        BufferedImage[] currentFrames = determineAnimation();
        if (currentFrames == null || currentFrames.length == 0) return;

        long currentTime = System.currentTimeMillis();
        if (currentFrames != currentlyPlayingAnim) {
            animationFrameIndex = 0;
            currentlyPlayingAnim = currentFrames;
        }

        int delay = isAttacking ? attackFrameDelay : normalFrameDelay;
        if (currentTime - lastFrameTime > delay) {
            animationFrameIndex++;
            lastFrameTime = currentTime;
        }

        if (animationFrameIndex >= currentFrames.length) {
            if (isAttacking) {
                isAttacking = false;
                animationFrameIndex = 0;
            } else if (!isOnGround) {
                animationFrameIndex = currentFrames.length - 1; 
            } else {
                animationFrameIndex = 0;
            }
        }
    }

    private BufferedImage[] determineAnimation() {
        if (isAttacking) return attackingAnim;
        if (!isOnGround) return jumpingAnim;
        if (isCurrentlyMoving) return walkingAnim;
        return idleAnim;
    }

    public void draw(Graphics g) {
        BufferedImage[] currentFrames = currentlyPlayingAnim;
        if (currentFrames != null && animationFrameIndex < currentFrames.length) {
            BufferedImage frame = currentFrames[animationFrameIndex];
            if (isFacingRight) {
                g.drawImage(frame, (int)worldX, (int)worldY, playerWidth, playerHeight, null);
            } else {
                g.drawImage(frame, (int)worldX + playerWidth, (int)worldY, -playerWidth, playerHeight, null);
            }
        }
    }

    public void update(CheckCollision collisionChecker, TileManager tileManager) {
        handleMovement(collisionChecker, tileManager);
        updateAnimationState();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_A) isLeftPressed = true;
        if (code == KeyEvent.VK_D) isRightPressed = true;
        if (code == KeyEvent.VK_SPACE) isJumpPressed = true;
        if (code == KeyEvent.VK_E) isInteractPressed = true;
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_A) isLeftPressed = false;
        if (code == KeyEvent.VK_D) isRightPressed = false;
        if (code == KeyEvent.VK_SPACE) isJumpPressed = false;
        if (code == KeyEvent.VK_E) isInteractPressed = false;
    }

    @Override public void keyTyped(KeyEvent e) {}
    @Override public void mouseClicked(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1 && !isAttacking) {
            isAttacking = true;
            animationFrameIndex = 0;
        }
    }
    @Override public void mousePressed(MouseEvent e) {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}

    public boolean isInteractPressed() { return isInteractPressed; }
    public void setPosition(double x, double y) { this.worldX = x; this.worldY = y; }
}
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Player implements KeyListener, MouseListener {

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

    private boolean isFacingRight = true;
    private boolean isCurrentlyMoving = false;
    private boolean isOnGround = false;
    private boolean isAttacking = false;

    private boolean isLeftPressed, isRightPressed, isJumpPressed, isInteractPressed;

    public Player() {
        loadAnimations();
    }

    // FIX: Renamed from getBounds to getHitbox to match CheckCollision usage
    public Rectangle getHitbox() {
        return new Rectangle((int)worldX + 20, (int)worldY + 10, 24, 50);
    }

    public void loadAnimations() {
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

    public void update(CheckCollision collisionChecker, TileManager tileManager) {
        double dx = 0;
        if (isLeftPressed) dx -= movementSpeed;
        if (isRightPressed) dx += movementSpeed;

        // X Movement and Collision
        worldX += dx;
        if (collisionChecker.isColliding(this, tileManager)) {
            worldX -= dx;
        }

        // Y Movement and Gravity
        if (!isOnGround) verticalSpeed += gravityForce;
        else verticalSpeed = 0;

        worldY += verticalSpeed;
        if (collisionChecker.isColliding(this, tileManager)) {
            if (verticalSpeed > 0) { // Landing on ground
                isOnGround = true;
                // Snap player to the top of the tile to prevent vibrating/falling through
                worldY = (Math.floor((getHitbox().y + getHitbox().height) / 32) * 32) - 60;
                verticalSpeed = 0;
            } else { // Hitting ceiling
                worldY -= verticalSpeed;
                verticalSpeed = 0;
            }
        } else {
            // Check if there is still ground beneath us
            worldY += 1;
            if (!collisionChecker.isColliding(this, tileManager)) isOnGround = false;
            worldY -= 1;
        }

        if (isOnGround && isJumpPressed) {
            verticalSpeed = jumpPower;
            isOnGround = false;
        }

        if (dx != 0) isFacingRight = dx > 0;
        isCurrentlyMoving = (dx != 0);

        updateAnimationState();
    }

    private void updateAnimationState() {
        BufferedImage[] next = isAttacking ? attackingAnim : !isOnGround ? jumpingAnim : isCurrentlyMoving ? walkingAnim : idleAnim;
        
        if (next == null || next.length == 0) return; // FIX: Added safety check
        if (next != currentlyPlayingAnim) {
            currentlyPlayingAnim = next;
            animationFrameIndex = 0;
        }

        long currentTime = System.currentTimeMillis();
        int delay = isAttacking ? 100 : 120;

        if (currentTime - lastFrameTime > delay) {
            animationFrameIndex++;
            lastFrameTime = currentTime;
        }

        if (animationFrameIndex >= currentlyPlayingAnim.length) {
            if (isAttacking) {
                isAttacking = false;
                animationFrameIndex = 0;
            } else if (!isOnGround) {
                animationFrameIndex = currentlyPlayingAnim.length - 1;
            } else {
                animationFrameIndex = 0;
            }
        }
    }

    public void draw(Graphics g) {
        if (currentlyPlayingAnim != null && animationFrameIndex < currentlyPlayingAnim.length) {
            BufferedImage frame = currentlyPlayingAnim[animationFrameIndex];
            if (frame != null) { // FIX: Ensure frame exists before drawing
                if (isFacingRight) {
                    g.drawImage(frame, (int)worldX, (int)worldY, playerWidth, playerHeight, null);
                } else {
                    g.drawImage(frame, (int)worldX + playerWidth, (int)worldY, -playerWidth, playerHeight, null);
                }
            }
        }
    }

    public void resetInputs() {
        isLeftPressed = isRightPressed = isJumpPressed = isInteractPressed = false;
    }

    public void setPosition(double x, double y) {
        this.worldX = x;
        this.worldY = y;
    }

    public boolean isInteractPressed() { return isInteractPressed; }

    @Override public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_A) isLeftPressed = true;
        if (code == KeyEvent.VK_D) isRightPressed = true;
        if (code == KeyEvent.VK_SPACE) isJumpPressed = true;
        if (code == KeyEvent.VK_E) isInteractPressed = true;
    }

    @Override public void keyReleased(KeyEvent e) {
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
}
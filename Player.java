import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Player implements KeyListener, MouseListener {

    // Screen settings
    private final int tileSize = 32;
    private final int columnCount = 40;
    private final int rowCount = 23;
    private final int screenWidth = columnCount * tileSize;
    private final int screenHeight = rowCount * tileSize;

    // Player position and size
    private int playerX = 100;
    private int playerY;
    private final int playerWidth = 64;
    private final int playerHeight = 64;
    
    private Rectangle hitbox;
    private final int hitboxWidth = playerWidth - 41; 
    private final int hitboxHeight = playerHeight - 32; 
    private final int offsetX = (playerWidth - hitboxWidth) / 2; 
    private final int offsetY = (playerHeight - hitboxHeight) / 2; 

    // Movement
    private int playerSpeed = 3;

    // Jump & gravity
    private double velocityY = 0;
    private final double gravity = 0.5;
    private final double jumpStrength = -11;
    
    // PlayerAnimation
    private BufferedImage[] idle;
    private BufferedImage[] walking;
    private BufferedImage[] jumping;
    private BufferedImage[] attacking;

    private BufferedImage[] lastAnimation = null;
    private int currentFrameIndex = 0;
    private long lastFrameTime = 0;
    private final int frameDuration = 260; // Time in ms per frame
    private int attackFrameDuration = 160; 
    
    // Player state
    private boolean facingRight = true;
    private boolean isMoving = false;
    private boolean isGrounded;
    private boolean isAttacking;
    private int groundLevel;

    // Key movement
    private boolean leftPressed, rightPressed, spacePressed;

    public Player() {
        groundLevel = screenHeight - playerHeight;
        playerY = groundLevel;
        hitbox = new Rectangle(playerX + offsetX, playerY + offsetY, hitboxWidth, hitboxHeight);
        loadAnimations();
    }

    private void playerControl() {
        // Process Input and Determine State
        // Movement is allowed while attacking based on user preference
        isMoving = (leftPressed || rightPressed) && isGrounded;
        
        if (leftPressed) {
            playerX -= playerSpeed;
            facingRight = false;
        }
        if (rightPressed) {
            playerX += playerSpeed;
            facingRight = true;
        }
        // Jump
        if (isGrounded && spacePressed) {
            velocityY = jumpStrength;
            isGrounded = false;
        }

        // Physics & Gravity
        velocityY += gravity;
        playerY += velocityY;

        // Ground Collision
        if (playerY >= groundLevel) {
            playerY = groundLevel;
            velocityY = 0;
            isGrounded = true;
        } else {
            isGrounded = false;
        }

        // Wall Collision
        if (playerX < 0) playerX = 0;
        if (playerX + playerWidth > screenWidth) playerX = screenWidth - playerWidth;
    }

    private void loadAnimations() {
        // Absolute paths for testing
        String base = "C:/Users/mark/Desktop/Game-Dev-Java/Assets/playerSprite/";
        idle = loadFrames(base + "playerIdle/", 4, 1);
        walking = loadFrames(base + "playerWalk/", 12, 5);
        jumping = loadFrames(base + "playerJump/", 8, 23);
        attacking = loadFrames(base + "playerAttack/", 8, 43);
    }

    private BufferedImage[] loadFrames(String folderPath, int count, int startIndex) {
        BufferedImage[] frames = new BufferedImage[count];
        try {
            for (int i = 0; i < count; i++) {
                
                String fileName = folderPath + "AnimationSheet_Character-" + (startIndex + i) + ".png.png";
                frames[i] = ImageIO.read(new File(fileName));
            }
        } catch (IOException e) {
            System.err.println("Error loading images from: " + folderPath);
            e.printStackTrace();
        }
        return frames;
    }
    
    public void AnimationStateHandling(){
        // Animation State Handling
        BufferedImage[] currentAnim = getCurrentAnimation();
        long currentTime = System.currentTimeMillis();

        // If animation state changes, reset to frame 0
        if (currentAnim != lastAnimation) {
            currentFrameIndex = 0;
            lastAnimation = currentAnim;
        }

        // Animation Timing
        int currentDuration = isAttacking ? attackFrameDuration : frameDuration;
        if (currentTime - lastFrameTime > currentDuration) {
            currentFrameIndex++;
            lastFrameTime = currentTime;
        }
        
        // Animation Frame Looping & Attack Termination
        if (currentFrameIndex >= currentAnim.length) {
            if (isAttacking) {
                // Attack finished, reset attack state
                isAttacking = false;
                currentFrameIndex = 0;
            } else if (!isGrounded) {
                // Hold the last frame of the jump/fall animation
                currentFrameIndex = currentAnim.length - 1;
            } else {
                // Loop walk/idle animations
                currentFrameIndex = 0;
            }
        }
    }

    private BufferedImage[] getCurrentAnimation() {
        if (isAttacking) return attacking;
        if (!isGrounded) return jumping;
        if (isMoving) return walking;
        return idle;
    }

    public void playerAnimationDraw(Graphics g) {
        BufferedImage[] currentAnim = getCurrentAnimation();
        if (currentAnim != null && currentAnim.length > 0) {
            
            // Safety check for index
            if (currentFrameIndex >= currentAnim.length) currentFrameIndex = 0;
            
            BufferedImage frame = currentAnim[currentFrameIndex];
            
            if (facingRight) {
                g.drawImage(frame, playerX, playerY, playerWidth, playerHeight, null);
            } else {
                // Flip image horizontally
                g.drawImage(frame, playerX + playerWidth, playerY, -playerWidth, playerHeight, null);
            }
        }
    }

    public void update() {
        playerControl();
        AnimationStateHandling();
        hitbox.x = playerX + offsetX;
        hitbox.y = playerY + offsetY;
    }

    public void draw(Graphics g) {
        playerAnimationDraw(g);
        g.setColor(Color.cyan); // Matches the color in your screenshot
        g.drawRect(hitbox.x, hitbox.y, hitbox.width, hitbox.height);
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_A) leftPressed = true;
        if (code == KeyEvent.VK_D) rightPressed = true;
        if (code == KeyEvent.VK_SPACE) spacePressed = true;
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_A) leftPressed = false;
        if (code == KeyEvent.VK_D) rightPressed = false;
        if (code == KeyEvent.VK_SPACE) spacePressed = false;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            if (!isAttacking) { // Prevent spamming reset on attack
                isAttacking = true;
                currentFrameIndex = 0;
            }
        }
    }
    
    // Unused mouse methods
    @Override public void mousePressed(MouseEvent e) {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}
}

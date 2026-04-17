import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.imageio.ImageIO;

public class Player implements KeyListener, MouseListener {
    
    // =====================================================
    // CONSTANTS - FIXED FOR NO JITTER
    // =====================================================
    private static final int PLAYER_WIDTH = 64;
    private static final int PLAYER_HEIGHT = 64;

    public int MAX_HP = 600;
    private int currentHP = MAX_HP;
    private boolean isInvulnerable = false;
    private long invulnerabilityStartTime = 0;
    private static final long INVULNERABILITY_DURATION = 1000; // 1 second
    private static final long INVULNERABILITY_FLASH_INTERVAL = 100; // Flash every 100ms
    private static final int attack_damage = 70; //70
    
    // Hitbox perfectly centered and aligned with sprite edges
    private static final int HITBOX_X_OFFSET = 20;  // (64-24)/2 = 20px from left
    private static final int HITBOX_Y_OFFSET = 8;   // Adjusted for better ground alignment
    private static final int HITBOX_WIDTH = 24;
    private static final int HITBOX_HEIGHT = 56;    // Extended to match sprite height better
    private static final int ATTACK_HITBOX_WIDTH = 17;
    private static final int ATTACK_HITBOX_HEIGHT = 64;
    private static final int ATTACK_HITBOX_Y_OFFSET = 0;
    private static final int ATTACK_HITBOX_X_OFFSET = -17; // ADDED: X offset for fine-tuning
    
    private static final double MOVEMENT_SPEED = 3.0;  // Slightly reduced for smoother feel
    private static final double GRAVITY_FORCE = 0.5;
    private static final double JUMP_POWER = -11;
    
    private static final String SPRITE_PATH = "Assets/playerSprite/";
    private static final int IDLE_FRAMES = 4;
    private static final int WALK_FRAMES = 12;
    private static final int JUMP_FRAMES = 8;
    private static final int ATTACK_FRAMES = 8;
    private static final int IDLE_START = 1;
    private static final int WALK_START = 5;
    private static final int JUMP_START = 23;
    private static final int ATTACK_START = 43;
    
    private static final int ANIMATION_DELAY_NORMAL = 120;
    private static final int ANIMATION_DELAY_ATTACK = 100;
    
    private static final int TILE_SIZE = 32;
    private static final int SCREEN_WIDTH = 1280;
    
    // STATE VARIABLES - USING INT FOR PIXEL-PERFECT POSITIONING
    private int worldX = 100;
    private int worldY = 100;
    
    private double verticalSpeed = 0;
    
    private BufferedImage[] idleAnim, walkingAnim, jumpingAnim, attackingAnim;
    private BufferedImage[] currentlyPlayingAnim = null;
    private int animationFrameIndex = 0;
    private long lastFrameTime = 0;
    
    private boolean isFacingRight = true;
    private boolean isCurrentlyMoving = false;
    private boolean isOnGround = false;
    private boolean isAttacking = false;
    private boolean debugMode = false; // Set to true to visualize hitboxes
    // Input states
    private boolean isLeftPressed, isRightPressed, isJumpPressed, isInteractPressed;

    // CONSTRUCTOR
    public Player() {
        loadAnimations();
    }
    
    // CORE METHODS
    public void update(CheckCollision collisionChecker, TileManager tileManager, List<Enemy> enemies) {
        if (isDead()) return; // Just stop updating, GamePanel will handle the screen switch
    
        handleInvulnerability();
        handleHorizontalMovement(collisionChecker, tileManager);
        handleEnemyCollisions(enemies);
        handleVerticalMovementAndGravity(collisionChecker, tileManager);
        handleJumping();
        enforceScreenBoundaries();
        updateAnimationState();
    }
    
    public void draw(Graphics g) {
        // Invulnerability flashing effect
        if (isInvulnerable) {
            long currentTime = System.currentTimeMillis();
            long flashTime = (currentTime - invulnerabilityStartTime) % (INVULNERABILITY_FLASH_INTERVAL * 2);
            if (flashTime > INVULNERABILITY_FLASH_INTERVAL) {
                return; // Skip drawing during flash
            }
        }
        
        drawHPBar(g);
        
        if (currentlyPlayingAnim != null && animationFrameIndex < currentlyPlayingAnim.length) {
            BufferedImage frame = currentlyPlayingAnim[animationFrameIndex];
            if (frame != null) {
                if (isFacingRight) {
                    g.drawImage(frame, worldX, worldY, PLAYER_WIDTH, PLAYER_HEIGHT, null);
                } else {
                    g.drawImage(frame, worldX + PLAYER_WIDTH, worldY, -PLAYER_WIDTH, PLAYER_HEIGHT, null);
                }
            }
        }
        
        //DEBUG: Visualize hitbox (remove in release)
        DebugDrawHitbox(g);
    }

    private void DebugDrawHitbox(Graphics g) {

        if (!debugMode) return;
        // 1. Draw Player Physics Hitbox (Red)
        g.setColor(Color.RED);
        Rectangle hitbox = getHitbox();
        g.drawRect(hitbox.x, hitbox.y, hitbox.width, hitbox.height);

        // 2. Prepare Attack Hitbox (Blue)
        Rectangle attackHitbox = getAttackHitbox();
        
        if (isAttacking && debugMode == true) { 
            // Show solid attack hitbox during the actual attack
            g.setColor(Color.BLUE);
            // FIX: width and height should be the hitbox variables, not calculated with worldX
            g.drawRect(attackHitbox.x, attackHitbox.y, attackHitbox.width, attackHitbox.height);
        } else {
            // Show semi-transparent preview when NOT attacking
            g.setColor(new Color(0, 0, 255, 100)); 
            // FIX: Use fillRect with the actual hitbox dimensions
            g.fillRect(attackHitbox.x, attackHitbox.y, attackHitbox.width, attackHitbox.height);
        }
    }

    public void resetStatus() {
        this.currentHP = MAX_HP;
        this.isInvulnerable = false;
        this.isAttacking = false;
        this.verticalSpeed = 0;
        this.animationFrameIndex = 0;
        resetInputs();
    }

    private void drawHPBar(Graphics g) {
        final int BAR_X = 10;
        final int BAR_Y = 10;
        final int BAR_WIDTH = 200;
        final int BAR_HEIGHT = 20;
        
        // Red background (empty portion)
        g.setColor(Color.RED);
        g.fillRect(BAR_X, BAR_Y, BAR_WIDTH, BAR_HEIGHT);
        
        // Green foreground (filled portion)
        g.setColor(Color.GREEN);
        int hpWidth = (int)((currentHP / (double)MAX_HP) * BAR_WIDTH);
        g.fillRect(BAR_X, BAR_Y, hpWidth, BAR_HEIGHT);
        
        // White border
        g.setColor(Color.WHITE);
        g.drawRect(BAR_X, BAR_Y, BAR_WIDTH, BAR_HEIGHT);
        
        // HP text
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.drawString(currentHP + "/" + MAX_HP, BAR_X + 5, BAR_Y + 15);
    }
    
    // Complete damage system
    public void takeDamage(int damage) {
        if (isInvulnerable || currentHP <= 0) return;
        
        currentHP -= damage;
        isInvulnerable = true;
        invulnerabilityStartTime = System.currentTimeMillis();
        
        // Knockback effect
        verticalSpeed = -8; // Jump up
        if (isFacingRight) {
            worldX += 10; // Push left
        } else {
            worldX -= 10; // Push right
        }
        
        System.out.println("Player took " + damage + " damage. HP: " + currentHP);
        
        if (currentHP <= 0) {
            System.out.println("PLAYER DIED!");
        }
    }

    public boolean isDead() {
        return currentHP <= 0;
    }

    public void heal(int amount) {
        currentHP = Math.min(currentHP + amount, MAX_HP);
    }
    
    private void handleInvulnerability() {
        if (isInvulnerable) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - invulnerabilityStartTime > INVULNERABILITY_DURATION) {
                isInvulnerable = false;
            }
        }
    }
    
    // FIXED: Attack hitbox calculation WITH X OFFSET
    private Rectangle getAttackHitbox() {
        int attackWidth = ATTACK_HITBOX_WIDTH;
        int attackHeight = ATTACK_HITBOX_HEIGHT;
        int attackX, attackY;
        attackY = worldY + ATTACK_HITBOX_Y_OFFSET;
        
        if (isFacingRight) {
            // Positioned to the right of the player
            attackX = worldX + PLAYER_WIDTH + ATTACK_HITBOX_X_OFFSET;  
        } else {
            // Positioned to the left of the player
            attackX = worldX - attackWidth - ATTACK_HITBOX_X_OFFSET;   
        }
        
        return new Rectangle(attackX, attackY, attackWidth, attackHeight);
    }
      
    // Enemy collision handling
    private void handleEnemyCollisions(List<Enemy> enemies) {
        for (Enemy enemy : enemies) {
            if (getHitbox().intersects(enemy.getEnemyHitbox())) {
                // Enemy attacks player during attack state
                if (enemy.getCurrentState() == Enemy.State.ATTACK) {
                    // Damage is applied by enemy's performAttack() method
                }
            }
            
            // Player attacks enemy during attack animation damage frame
            if (isAttacking && animationFrameIndex == 3) { // Damage frame
                Rectangle attackHitbox = getAttackHitbox();
                if (attackHitbox.intersects(enemy.getEnemyHitbox())) {
                    enemy.takeDamage(attack_damage); // Player sword damage
                    System.out.println("Player hit enemy for " + attack_damage + "damage!");
                }
            }
        }
    }
    
    // MOVEMENT & COLLISION
    private void handleHorizontalMovement(CheckCollision collisionChecker, TileManager tileManager) {
        int dx = calculateHorizontalMovement();
    
        // Move and check collision
        worldX += dx;
    
        if (collisionChecker.isColliding(this, tileManager)) {
            worldX -= dx;

        }
        
        updateFacingDirection(dx);
        isCurrentlyMoving = (dx != 0);
    }
    
    private int calculateHorizontalMovement() {
        int dx = 0;
        if (isLeftPressed) dx -= (int)MOVEMENT_SPEED;
        if (isRightPressed) dx += (int)MOVEMENT_SPEED;
        return dx;
    }
    
    private void handleVerticalMovementAndGravity(CheckCollision collisionChecker, TileManager tileManager) {
    applyGravity();
    
    // 1. Calculate the intended move
    int dy = (int)(verticalSpeed + 0.5); 
    
    // 2. Apply the move
    worldY += dy;
    
    // 3. Check if this move caused a collision
    if (collisionChecker.isColliding(this, tileManager)) {
        // 4. Pass 'dy' into the resolver to fix the position
        resolveVerticalCollision(dy, collisionChecker, tileManager);
    } else {
        // Only check for edge falls if we didn't just hit a floor
        checkEdgeFall(collisionChecker, tileManager);
    }
}

private void resolveVerticalCollision(int dy, CheckCollision collisionChecker, TileManager tileManager) {
    if (dy > 0) { // Falling Down
        isOnGround = true;
        verticalSpeed = 0;

        // Snapping logic: Calculate the bottom of the HITBOX, not the sprite
        int hitboxBottom = worldY + HITBOX_Y_OFFSET + HITBOX_HEIGHT;
        int tileTop = (hitboxBottom / TILE_SIZE) * TILE_SIZE;
        
        // Correct worldY so the hitbox bottom sits perfectly on tileTop
        worldY = tileTop - HITBOX_HEIGHT - HITBOX_Y_OFFSET;

    } else if (dy < 0) { // Jumping Up (Hitting Ceiling)
        verticalSpeed = 0;
        
        // Snapping logic: Calculate the top of the HITBOX
        int hitboxTop = worldY + HITBOX_Y_OFFSET;
        int tileBottom = (hitboxTop / TILE_SIZE) * TILE_SIZE + TILE_SIZE;
        
        // Correct worldY so the hitbox top sits perfectly at tileBottom
        worldY = tileBottom - HITBOX_Y_OFFSET;
    }
}
    
    private void applyGravity() {
        if (!isOnGround) {
            verticalSpeed += GRAVITY_FORCE;
            // Clamp vertical speed to prevent tunneling
            if (verticalSpeed > 12) verticalSpeed = 12;
        } else {
            verticalSpeed = 0;
        }
    }
    
    private void checkEdgeFall(CheckCollision collisionChecker, TileManager tileManager) {
        worldY += 1;
        if (!collisionChecker.isColliding(this, tileManager)) {
            isOnGround = false;
        }
        worldY -= 1;
    }
    
    private void handleJumping() {
        if (isOnGround && isJumpPressed) {
            verticalSpeed = JUMP_POWER;
            isOnGround = false;
        }
    }
    
    private void enforceScreenBoundaries() {
        if (worldX < 0) worldX = 0;
        if (worldX > SCREEN_WIDTH - PLAYER_WIDTH) {
            worldX = SCREEN_WIDTH - PLAYER_WIDTH;
        }
    }
    
    private void updateFacingDirection(int dx) {
        if (dx != 0) {
            isFacingRight = dx > 0;
        }
    }
    
    // ANIMATION SYSTEM
    private void updateAnimationState() {
        BufferedImage[] nextAnim = determineCurrentAnimation();
        if (nextAnim == null || nextAnim.length == 0) return;
        
        if (nextAnim != currentlyPlayingAnim) {
            switchAnimation(nextAnim);
            return;
        }
        
        advanceAnimationFrame();
        handleAnimationCompletion();
    }
    
    private BufferedImage[] determineCurrentAnimation() {
        if (isAttacking) return attackingAnim;
        if (!isOnGround) return jumpingAnim;
        return isCurrentlyMoving ? walkingAnim : idleAnim;
    }
    
    private void switchAnimation(BufferedImage[] newAnim) {
        currentlyPlayingAnim = newAnim;
        animationFrameIndex = 0;
    }
    
    private void advanceAnimationFrame() {
        long currentTime = System.currentTimeMillis();
        int delay = isAttacking ? ANIMATION_DELAY_ATTACK : ANIMATION_DELAY_NORMAL;
        
        if (currentTime - lastFrameTime > delay) {
            animationFrameIndex++;
            lastFrameTime = currentTime;
        }
    }
    
    private void handleAnimationCompletion() {
        if (animationFrameIndex >= currentlyPlayingAnim.length) {
            if (isAttacking) {
                isAttacking = false;
            }
            animationFrameIndex = getResetFrameIndex();
        }
    }
    
    private int getResetFrameIndex() {
        if (!isOnGround) {
            return currentlyPlayingAnim.length - 1;
        }
        return 0;
    }
    
    // RESOURCE LOADING
    private void loadAnimations() {
        idleAnim = loadFrames(SPRITE_PATH + "playerIdle/", IDLE_FRAMES, IDLE_START);
        walkingAnim = loadFrames(SPRITE_PATH + "playerWalk/", WALK_FRAMES, WALK_START);
        jumpingAnim = loadFrames(SPRITE_PATH + "playerJump/", JUMP_FRAMES, JUMP_START);
        attackingAnim = loadFrames(SPRITE_PATH + "playerAttack/", ATTACK_FRAMES, ATTACK_START);
    }
    
    private BufferedImage[] loadFrames(String folder, int count, int startNumber) {
        BufferedImage[] frames = new BufferedImage[count];
        for (int i = 0; i < count; i++) {
            String filename = folder + "AnimationSheet_Character-" + (startNumber + i) + ".png.png";
            try {
                File imageFile = new File(filename);
                if (imageFile.exists()) {
                    frames[i] = ImageIO.read(imageFile);
                }
            } catch (IOException e) {
                System.err.println("Failed to load: " + filename);
            }
        }
        return frames;
    }
    
    // COLLISION & ACCESSORS
    public Rectangle getHitbox() {
        return new Rectangle(
            worldX + HITBOX_X_OFFSET, 
            worldY + HITBOX_Y_OFFSET, 
            HITBOX_WIDTH, 
            HITBOX_HEIGHT
        );
    }
    
    public void setPosition(int x, int y) {
        this.worldX = x;
        this.worldY = y;
    }
    
    public float getX() { return worldX; }
    public float getY() { return worldY; }
    public boolean isInteractPressed() { return isInteractPressed; }
    public int getCurrentHP() { return currentHP; }
    
    // INPUT HANDLING
    public void resetInputs() {
        isLeftPressed = isRightPressed = isJumpPressed = isInteractPressed = false;
    }
    
    // KeyListener
    @Override public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        switch (code) {
            case KeyEvent.VK_A -> isLeftPressed = true;
            case KeyEvent.VK_D -> isRightPressed = true;
            case KeyEvent.VK_SPACE -> isJumpPressed = true;
            case KeyEvent.VK_E -> isInteractPressed = true;
        }
    }
    
    @Override public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();
        switch (code) {
            case KeyEvent.VK_A -> isLeftPressed = false;
            case KeyEvent.VK_D -> isRightPressed = false;
            case KeyEvent.VK_SPACE -> isJumpPressed = false;
            case KeyEvent.VK_E -> isInteractPressed = false;
        }
    }
    
    @Override public void keyTyped(KeyEvent e) {}
    
    // MouseListener
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
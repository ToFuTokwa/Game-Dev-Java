import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;

public class Enemy {
    
    // ============================================================================
    // CONSTANTS
    // ============================================================================
    private static final int ENEMY_WIDTH = 128;
    private static final int ENEMY_HEIGHT = 128;
    private static final int HITBOX_X_OFFSET = 43; 
    private static final int HITBOX_Y_OFFSET = 0;
    private static final int VISUAL_Y_OFFSET = -30; 
    private static final int ATTACK_HITBOX_WIDTH = 28;
    private static final int ATTACK_HITBOX_HEIGHT = 62;
    private static final int ATTACK_HITBOX_X_OFFSET = -5; // Adjust this for better alignment
    private static final int ATTACK_HITBOX_Y_OFFSET = 0; // Adjust this for better alignment
    private static final int DAMAGE_AMOUNT = 25;
    
    private static final float PATROL_SPEED = 0.5f;
    private static final float CHASE_SPEED = 3.0f;
    private static final float GRAVITY = 0.5f;
    private static final float MAX_FALL_SPEED = 12.0f;
    
    private static final float PATROL_RANGE = 50.0f;
    private static final float ATTACK_RANGE = 10.0f;
    private static final int MAX_HEALTH = 100;
    
    private static final float TELEGRAPH_TIME = 0.5f;
    private static final float ATTACK_DAMAGE_FRAME = 0.1f;
    
    private static final int TILE_SIZE = 32;
    private static final float FPS = 60.0f;
    private static final float ANIMATION_SPEED = 0.08f;

    // ============================================================================
    // STATE ENUM
    // ============================================================================
    public enum State {
        PATROL, CHASE, TELEGRAPH, ATTACK, HURT, DEAD
    }
    
    // ============================================================================
    // FIELDS
    // ============================================================================
    // Position & Physics
    private float x, y;
    private float vx, vy;
    private int direction = 1;
    
    // State Management
    private State currentState = State.PATROL;
    private float stateTimer = 0;
    private int health = MAX_HEALTH;
    
    // Animation
    private BufferedImage[] attackFrames;
    private BufferedImage[] patrolFrames;
    private BufferedImage[] chaseFrames;
    private BufferedImage[] telegraphFrames;
    private BufferedImage[] hurtFrames;

    private int currentFrame = 0;
    private float animationCounter = 0;
    private boolean isAttacking = false;
    
    // Timers
    private float attackTimer = 0;
    private float patrolTimer = 0;
    private float chaseTimer = 0;
    private float telegraphTimer = 0;
    private float hurtTimer = 0;

    // Sprite Paths - UPDATE THESE TO MATCH YOUR FILES
    private String attackPath = "C:/Users/mark/Desktop/Game-Dev-Java/Assets/skeletonPack/Attack/Sword";
    private String patrolPath = "C:/Users/mark/Desktop/Game-Dev-Java/Assets/skeletonPack/Walk/Sword";
    private String chasePath = "C:/Users/mark/Desktop/Game-Dev-Java/Assets/skeletonPack/Run/Sword";
    private String telegraphPath = "C:/Users/mark/Desktop/Game-Dev-Java/Assets/skeletonPack/Idle/Sword";
    private String hurtPath = "C:/Users/mark/Desktop/Game-Dev-Java/Assets/skeletonPack/Hurt";
    // ============================================================================
    // CONSTRUCTOR
    // ============================================================================
    public Enemy(float startX, float startY) {
        this.x = startX;
        this.y = startY;
        loadSprites();
    }
    
    // ============================================================================
    // CORE LOOP METHODS
    // ============================================================================
    /**
     * Main update loop
     */
    public void update(float deltaTime, Player player, CheckCollision collisionChecker, TileManager tileManager) {
        if (currentState == State.DEAD) return;
        
        stateTimer += deltaTime;
        updatePhysics(deltaTime);
        updateStateMachine(deltaTime, player);
        handleCollisions(collisionChecker, tileManager);
        handleAttack(player);
        updateAnimation(deltaTime);
        borderHandling(tileManager);
    }
    
    /**
     * Render enemy with state-based visuals - FIXED SPRITE FLIPPING
     */
    public void draw(Graphics g) {
        if (currentState == State.DEAD) return;

        Graphics2D g2 = (Graphics2D) g;
        BufferedImage image = getCurrentFrame();
        int visualY = (int)y + VISUAL_Y_OFFSET;

        if (image != null) {
            int drawX = (int)x;
            int drawY = visualY;
            
            if (direction == 1) {
                // Facing right - Normal draw
                g2.drawImage(image, drawX + ENEMY_WIDTH, drawY, -ENEMY_WIDTH, ENEMY_HEIGHT, null);
            } else {
                // Facing left - Flip horizontally using negative width
                g2.drawImage(image, drawX, drawY, ENEMY_WIDTH, ENEMY_HEIGHT, null);
            }
        } else {
            // Fallback: Show colored rect + state info for debugging
            applyStateVisuals(g);
            g.setFont(new Font("Arial", Font.BOLD, 12));
            g.drawString(currentState.name(), (int)x, (int)(y + VISUAL_Y_OFFSET - 5));
            g.fillRect((int)x + 10, visualY, ENEMY_WIDTH - 20, ENEMY_HEIGHT - 20);
        }

        // **NEW: Exclamation mark above enemy during telegraph state**
        if (currentState == State.TELEGRAPH) {
            g.setColor(Color.RED);
            g.setFont(new Font("Arial", Font.BOLD, 24));
            FontMetrics fm = g.getFontMetrics();
            int exclamationWidth = fm.stringWidth("!");
            int exclamationX = (int)x + (ENEMY_WIDTH / 2) - (exclamationWidth / 2);
            int exclamationY = visualY + 25; // 50 pixels above the sprite
            g.drawString("!", exclamationX, exclamationY);
        }
        
    }

    // ============================================================================
    // SPRITE LOADING
    // ============================================================================
    private void loadSprites() {
        loadAttackFrames();
        loadPatrolFrames();
        loadChaseFrames();
        loadTelegraphFrames();
        loadHurtFrames();
        debugSpriteStatus(); // Debug output
    }

    private void loadHurtFrames() {
        try {
            BufferedImage sheet = ImageIO.read(new File(hurtPath + "/Skeleton_Default_Hurt.png"));
            int frameWidth = sheet.getWidth() / 2;
            int frameHeight = sheet.getHeight();
            hurtFrames = new BufferedImage[2];

            for (int i = 0; i < 2; i++) {
                hurtFrames[i] = sheet.getSubimage(i * frameWidth, 0, frameWidth, frameHeight);
            }
            System.out.println(" Hurt sprites loaded: " + hurtFrames.length + " frames");
        } catch (Exception e) {
            System.err.println(" Failed to load hurt sprites: " + e.getMessage());
            System.err.println("File path: " + hurtPath + "/Skeleton_Default_Hurt.png");
            hurtFrames = null;
        }
    }
    
    private void loadTelegraphFrames() {
        try {
            BufferedImage sheet = ImageIO.read(new File(telegraphPath + "/Skeleton_Default_Idle_Sword.png"));
            int frameWidth = sheet.getWidth() / 6;
            int frameHeight = sheet.getHeight();
            telegraphFrames = new BufferedImage[6];

            for (int i = 0; i < 6; i++) {
                telegraphFrames[i] = sheet.getSubimage(i * frameWidth, 0, frameWidth, frameHeight);
            }
            System.out.println(" Telegraph sprites loaded: " + telegraphFrames.length + " frames");
        } catch (Exception e) {
            System.err.println(" Failed to load telegraph sprites: " + e.getMessage());
            System.err.println("File path: " + telegraphPath + "/Skeleton_Default_Idle_Sword.png");
            telegraphFrames = null;
        }
    }

    private void loadAttackFrames() {
        try {
            BufferedImage sheet = ImageIO.read(new File(attackPath + "/Skeleton_Default_Attack_Sword.png"));
            int frameWidth = sheet.getWidth() / 6;
            int frameHeight = sheet.getHeight();
            attackFrames = new BufferedImage[6];

            for (int i = 0; i < 6; i++) {
                attackFrames[i] = sheet.getSubimage(i * frameWidth, 0, frameWidth, frameHeight);
            }
            System.out.println(" Attack sprites loaded: " + attackFrames.length + " frames");
        } catch (Exception e) {
            System.err.println(" Failed to load attack sprites: " + e.getMessage());
            System.err.println("File path: " + attackPath + "/Skeleton_Default_Attack_Sword.png");
            attackFrames = null;
        }
    }

    private void loadPatrolFrames() {
        try {
            // UPDATE THIS FILENAME TO MATCH YOUR ACTUAL FILE
            BufferedImage sheet = ImageIO.read(new File(patrolPath + "/MP_Skeleton_Default_Walk_Sword.png"));
            int frameWidth = sheet.getWidth() / 6;
            int frameHeight = sheet.getHeight();
            patrolFrames = new BufferedImage[6];

            for (int i = 0; i < 6; i++) {
                patrolFrames[i] = sheet.getSubimage(i * frameWidth, 0, frameWidth, frameHeight);
            }
            System.out.println(" Patrol sprites loaded: " + patrolFrames.length + " frames");
        } catch (Exception e) {
            System.err.println(" Failed to load patrol sprites: " + e.getMessage());
            System.err.println("File path: " + patrolPath + "/MP_Skeleton_Default_Walk_Sword.png");
            patrolFrames = null;
        }
    }

    private void loadChaseFrames() {
        try {
            // UPDATE THIS FILENAME TO MATCH YOUR ACTUAL FILE
            BufferedImage sheet = ImageIO.read(new File(chasePath + "/Skeleton_Default_Run_Sword.png"));
            int frameWidth = sheet.getWidth() / 6;
            int frameHeight = sheet.getHeight();
            chaseFrames = new BufferedImage[6];

            for (int i = 0; i < 6; i++) {
                chaseFrames[i] = sheet.getSubimage(i * frameWidth, 0, frameWidth, frameHeight);
            }
            System.out.println(" Chase sprites loaded: " + chaseFrames.length + " frames");
        } catch (Exception e) {
            System.err.println(" Failed to load chase sprites: " + e.getMessage());
            System.err.println("File path: " + chasePath + "/Skeleton_Default_Run_Sword.png");
            chaseFrames = null;
        }
    }

    // ============================================================================
    // ANIMATION SYSTEM - FIXED FOR PATROL
    // ============================================================================
    private BufferedImage getCurrentFrame() {
        // FIXED: Now returns BOTH attack AND patrol frames!
        if (isAttacking && attackFrames != null && attackFrames.length > 0) {
            return attackFrames[Math.min(currentFrame, attackFrames.length - 1)];
        } else if (currentState == State.PATROL && patrolFrames != null && patrolFrames.length > 0) {
            return patrolFrames[Math.min(currentFrame, patrolFrames.length - 1)];
        } else if (currentState == State.CHASE && chaseFrames != null && chaseFrames.length > 0) {
            return chaseFrames[Math.min(currentFrame, chaseFrames.length - 1)];
        } else if (currentState == State.TELEGRAPH && telegraphFrames != null && telegraphFrames.length > 0) {
            return telegraphFrames[Math.min(currentFrame, telegraphFrames.length - 1)];
        } else if (currentState == State.HURT && hurtFrames != null && hurtFrames.length > 0) {
            return hurtFrames[Math.min(currentFrame, hurtFrames.length - 1)];
        }
        return null;
    }
    
    private void updateAnimation(float deltaTime) {
        if (isAttacking) {
            updateAttackAnimation(deltaTime);
        } else if (currentState == State.PATROL && patrolFrames != null) {
            updatePatrolAnimation(deltaTime);
        } else if (currentState == State.CHASE && chaseFrames != null) {
            updateChaseAnimation(deltaTime);
        } else if (currentState == State.TELEGRAPH && telegraphFrames != null) {
            updateTelegraphAnimation(deltaTime);
        } else if (currentState == State.HURT && hurtFrames != null) {
            updateHurtAnimation(deltaTime);
        }
    }

    private void updateHurtAnimation(float deltaTime) {
        hurtTimer += deltaTime;
        if (hurtTimer >= ANIMATION_SPEED) {
            currentFrame = (currentFrame + 1) % hurtFrames.length;
            hurtTimer = 0;
        }
    }

    private void updateTelegraphAnimation(float deltaTime) { // Make public → private
        telegraphTimer += deltaTime;
        if (telegraphTimer >= ANIMATION_SPEED) { // Use ANIMATION_SPEED
            currentFrame = (currentFrame + 1) % telegraphFrames.length;
            telegraphTimer = 0;
        }
    }
    
    private void updateAttackAnimation(float deltaTime) {
        animationCounter += deltaTime;
        if (animationCounter >= ANIMATION_SPEED) {
            currentFrame++;
            animationCounter = 0;
            
            if (currentFrame >= attackFrames.length) {
                endAttack();
            }
        }
    }
    
    private void updatePatrolAnimation(float deltaTime) {
        patrolTimer += deltaTime;
        if (patrolTimer >= ANIMATION_SPEED) {
            currentFrame = (currentFrame + 1) % patrolFrames.length;
            patrolTimer = 0;
        }
    }

    private void updateChaseAnimation(float deltaTime) {
        chaseTimer += deltaTime;
        if (chaseTimer >= ANIMATION_SPEED) {
            currentFrame = (currentFrame + 1) % chaseFrames.length;
            chaseTimer = 0;
        }
    }

    // ============================================================================
    // PHYSICS SYSTEM
    // ============================================================================
    private void updatePhysics(float deltaTime) {
        if (!isOnGround()) {
            vy += GRAVITY;
            if (vy > MAX_FALL_SPEED) vy = MAX_FALL_SPEED;
        } else {
            vy = 0;
        }
    }
    
    private boolean isOnGround() {
        return vy <= 0.1f;
    }
    
    private void snapToGround() {
        y = ((int)((y + ENEMY_HEIGHT) / TILE_SIZE) * TILE_SIZE) - ENEMY_HEIGHT;
    }

    // ============================================================================
    // COLLISION SYSTEM
    // ============================================================================
    private void handleCollisions(CheckCollision collisionChecker, TileManager tileManager) {
        float oldX = x;
        float oldY = y;
        
        x += vx;
        if (collisionChecker.isColliding(this, tileManager)) {
            x = oldX;
            if (currentState == State.PATROL) {
                direction *= -1;
            }
            vx = 0;
        }
        
        y += vy;
        if (collisionChecker.isColliding(this, tileManager)) {
            y = oldY;
            if (vy > 0) {
                snapToGround();
            }
            vy = 0;
        }
    }

    // ============================================================================
    // STATE MACHINE
    // ============================================================================
    private void updateStateMachine(float deltaTime, Player player) {
        // 1. Calculate centers to fix the "overlap" issue
        float enemyCenterX = getEnemyHitbox().x + (getEnemyHitbox().width / 2);
        float playerCenterX = player.getHitbox().x + (player.getHitbox().width / 2);
        
        // Horizontal distance between centers
        float distToPlayer = Math.abs(playerCenterX - enemyCenterX);

        // 2. Vertical check (Same Level)
        float playerY = player.getY();
        boolean sameLevel = Math.abs(playerY - y) < TILE_SIZE * 2; 

        // 3. Logic for switching states
        if (currentState == State.PATROL && distToPlayer < PATROL_RANGE && sameLevel) {
            transitionToChase();
        }
        
        // 4. Handle specific state behaviors - PASS PLAYER TO ALL METHODS
        switch (currentState) {
            case PATROL -> handlePatrolState(player, deltaTime);
            case CHASE -> handleChaseState(distToPlayer, player, deltaTime); 
            case TELEGRAPH -> handleTelegraphState(deltaTime, player);  // ✅ FIXED
            case ATTACK -> handleAttackState(player);                   // ✅ FIXED
            case HURT -> handleHurtState();
            case DEAD -> {}
        }
    }

    private void handlePatrolState(Player player, float deltaTime) {
        vx = direction * PATROL_SPEED;
        
        // Simplified vision detection using same logic as state machine
        float enemyCenterX = getEnemyHitbox().x + (getEnemyHitbox().width / 2);
        float playerCenterX = player.getHitbox().x + (player.getHitbox().width / 2);
        float distToPlayer = Math.abs(playerCenterX - enemyCenterX);
        boolean sameLevel = Math.abs(player.getY() - y) < TILE_SIZE * 2;
        
        if (distToPlayer < PATROL_RANGE && sameLevel) {
            transitionToChase();
        }
    }

    private void handleChaseState(float distanceToPlayer, Player player, float deltaTime) {
        chaseTimer += deltaTime;
        
        float enemyCenterX = getEnemyHitbox().x + (getEnemyHitbox().width / 2);
        float playerCenterX = player.getHitbox().x + (player.getHitbox().width / 2);
        
        // THE CHEAT: Subtract half the player's width (or a fixed value like 20) 
        // from the distance. This forces the skeleton to "dig in" closer.
        float adjustedDistance = distanceToPlayer - 25.0f; 

        direction = (playerCenterX > enemyCenterX) ? 1 : -1;
        vx = direction * CHASE_SPEED;
        
        // Now it won't stop until it's "cheated" its way into your space
        if (adjustedDistance < ATTACK_RANGE) {
            transitionToTelegraph();
        }
    }

    private void handleTelegraphState(float deltaTime, Player player) {  // ✅ Player param added
        vx = 0;
        stateTimer += deltaTime;
        if (stateTimer > TELEGRAPH_TIME) {
            System.out.println("TELEGRAPH END → ATTACK");
            startAttack(player);  // ✅ Now passes player
        }
    }

    private void handleAttackState(Player player) {  // ✅ Player param added
        vx = 0;
    }

    private void handleHurtState() {
        vx = 0;
        if (stateTimer > 0.3f) {
            if (health <= 0) {
                currentState = State.DEAD;
            } else {
                currentState = State.PATROL; // Back to patrol after hurt
            }
            stateTimer = 0;
        }
    }
    
    // State Transitions
    private void transitionToChase() {
        currentState = State.CHASE;
        stateTimer = 0;
    }
    
    private void transitionToTelegraph() {
        currentState = State.TELEGRAPH;
        stateTimer = 0;
        vx = 0;
    }
    
    private void startAttack(Player player) {
        currentState = State.ATTACK;
        stateTimer = 0;
        isAttacking = true;
        currentFrame = 0;
        animationCounter = 0;
        attackTimer = 0;
        
        // ✅ Null-safe direction calculation
        if (player != null) {
            direction = (player.getX() > x) ? 1 : -1;
        } else {
            System.out.println("WARNING: startAttack called with null player");
        }
    }
    
    private void endAttack() {
        isAttacking = false;
        currentState = State.CHASE;
        stateTimer = 0;
        currentFrame = 0;
        attackTimer = 0;
    }

    // ============================================================================
    // COMBAT SYSTEM
    // ============================================================================

    private Rectangle getAttackHitbox() {
        int attackX;
        // Base the Y on the enemy's current Y + your offset
        int attackY = (int)(y + ATTACK_HITBOX_Y_OFFSET); 

        // Calculate the horizontal start of the body hitbox first
        int bodyHitboxX = (int)(x + HITBOX_X_OFFSET);
        int bodyHitboxWidth = ENEMY_WIDTH / 3;

        if (direction == 1) { // Facing Right
            // The attack starts at the right edge of the body hitbox
            attackX = bodyHitboxX + bodyHitboxWidth + ATTACK_HITBOX_X_OFFSET;
        } else { // Facing Left
            // The attack starts at the left edge of the body and extends further left
            attackX = bodyHitboxX - ATTACK_HITBOX_WIDTH - ATTACK_HITBOX_X_OFFSET;
        }

        return new Rectangle(attackX, attackY, ATTACK_HITBOX_WIDTH, ATTACK_HITBOX_HEIGHT);
    }

    private void handleAttack(Player player) {
        if (currentState != State.ATTACK || !isAttacking) return;
        
        attackTimer += 1.0f / FPS;
        if (attackTimer >= ATTACK_DAMAGE_FRAME && attackTimer < ATTACK_DAMAGE_FRAME + 0.1f) {
            performAttack(player);  // ✅ Now guaranteed non-null
        }
    }
    
    private void performAttack(Player player) {
        // USE the attack hitbox here, not the enemy's body hitbox
        if (getAttackHitbox().intersects(player.getHitbox())) {
            player.takeDamage(DAMAGE_AMOUNT);
        }
    }
    
    public void takeDamage(int damage) {
        if (currentState == State.DEAD || currentState == State.HURT) return;
        
        health -= damage;
        currentState = State.HURT;
        stateTimer = 0;
        vx = -direction * CHASE_SPEED * 1.5f;
        vy = -5.0f;
    }

    // ============================================================================
    // RENDERING HELPERS
    // ============================================================================
    private void applyStateVisuals(Graphics g) {
        switch (currentState) {
            case TELEGRAPH -> g.setColor(Color.YELLOW);
            case ATTACK -> g.setColor(Color.ORANGE);
            case HURT -> g.setColor(Color.MAGENTA);
            case DEAD -> g.setColor(Color.GRAY);
            default -> g.setColor(Color.RED);
        }
    }

    // ============================================================================
    // UTILITY & DEBUG METHODS
    // ============================================================================
    public void borderHandling(TileManager tileManager) {
        int mapWidth = tileManager.getTileMap().getMap()[0].length * TILE_SIZE;
        
        if (x < 0) {
            x = 0;
            direction = 1;
        } else if (x + ENEMY_WIDTH > mapWidth) {
            x = mapWidth - ENEMY_WIDTH;
            direction = -1;
        }
    }
    
    public float distanceTo(Player player) {
        return Math.abs(player.getX() - x);
    }

    // DEBUG: Check sprite loading status
    public void debugSpriteStatus() {
        System.out.println("=== ENEMY SPRITE STATUS ===");
        System.out.println("Attack frames: " + (attackFrames != null ? attackFrames.length : "NULL"));
        System.out.println("Patrol frames: " + (patrolFrames != null ? patrolFrames.length : "NULL"));
        System.out.println("Current state: " + currentState);
        System.out.println("isAttacking: " + isAttacking);
        System.out.println("Position: (" + x + ", " + y + ")");
        System.out.println("========================");
    }

    // ============================================================================
    // ACCESSORS
    // ============================================================================
    public Rectangle getEnemyHitbox() {
        return new Rectangle(
            (int)(x + HITBOX_X_OFFSET),
            (int)(y + HITBOX_Y_OFFSET),
            ENEMY_WIDTH / 3,
            ENEMY_HEIGHT / 2
        );
    }
    
    public float getX() { return x; }
    public float getY() { return y; }
    public State getCurrentState() { return currentState; }
    public int getHealth() { return health; }
    
    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }
    
    public boolean isDead() {
        return currentState == State.DEAD;
    }
    
    public void kill() {
        health = 0;
        currentState = State.DEAD;
    }
    

}
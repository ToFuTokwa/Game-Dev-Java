import java.awt.Rectangle;

public class CheckCollision {
    private final int TILE_SIZE = 32;

    public boolean isColliding(Player player, TileManager tileManager) {
        Rectangle hitbox = player.getHitbox();
        int[][] grid = tileManager.getTileMap().getMap();
        
        // Find grid boundaries of the hitbox
        int topRow = hitbox.y / TILE_SIZE;
        int bottomRow = (hitbox.y + hitbox.height) / TILE_SIZE;
        int leftCol = hitbox.x / TILE_SIZE;
        int rightCol = (hitbox.x + hitbox.width) / TILE_SIZE;

        for (int r = topRow; r <= bottomRow; r++) {
            for (int c = leftCol; c <= rightCol; c++) {
                if (r >= 0 && r < grid.length && c >= 0 && c < grid[0].length) {
                    int id = grid[r][c];
                    // 0 = Air, 7 = Portal (no collision)
                    if (id != 0 && id != 7) {
                        if (hitbox.intersects(tileManager.getBound(r, c))) return true;
                    }
                }
            }
        }
        return false;
    }
}
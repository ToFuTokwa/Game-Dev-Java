import java.awt.Rectangle;

public class CheckCollision {
    private final int TILE_SIZE = 32;

    // Check collision for Player
    public boolean isColliding(Player player, TileManager tileManager) {
        return checkTiles(player.getHitbox(), tileManager);
    }

    // Check collision for Enemy
    public boolean isColliding(Enemy enemy, TileManager tileManager) {
        return checkTiles(enemy.getEnemyHitbox(), tileManager);
    }

    private boolean checkTiles(Rectangle hitbox, TileManager tileManager) {
        int[][] grid = tileManager.getTileMap().getMap();
        
        int topRow = hitbox.y / TILE_SIZE;
        int bottomRow = (hitbox.y + hitbox.height) / TILE_SIZE;
        int leftCol = hitbox.x / TILE_SIZE;
        int rightCol = (hitbox.x + hitbox.width) / TILE_SIZE;

        for (int r = topRow; r <= bottomRow; r++) {
            for (int c = leftCol; c <= rightCol; c++) {
                if (r >= 0 && r < grid.length && c >= 0 && c < grid[0].length) {
                    int id = grid[r][c];
                    // Solid tiles: 1 (Dirt), 2 (Grass), 3 (Green)
                    if (id >= 1 && id <= 3) {
                        if (hitbox.intersects(tileManager.getBound(r, c))) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
}

import java.awt.Rectangle;

public class CheckCollision {
    private final int TILE_SIZE = 32;

    public boolean isColliding(Player player, TileManager tileManager) {
        Rectangle hitbox = player.getHitbox(); // References the updated method in Player.java
        int[][] mapGrid = tileManager.getTileMap().getMap();
        
        int startRow = hitbox.y / TILE_SIZE;
        int endRow = (hitbox.y + hitbox.height) / TILE_SIZE;
        int startCol = hitbox.x / TILE_SIZE;
        int endCol = (hitbox.x + hitbox.width) / TILE_SIZE;

        for (int r = startRow; r <= endRow; r++) {
            for (int c = startCol; c <= endCol; c++) {
                if (r >= 0 && r < mapGrid.length && c >= 0 && c < mapGrid[0].length) {
                    int tileID = mapGrid[r][c];
                    // 0 is air, 7 is portal (walk-through)
                    if (tileID != 0 && tileID != 7) {
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
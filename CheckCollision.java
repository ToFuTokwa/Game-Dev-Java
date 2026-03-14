import java.awt.Rectangle;

public class CheckCollision {

    public boolean isColliding(Player player, TileManager tileManager) {
        if (tileManager == null || tileManager.getTileMap() == null) return false;

        Rectangle playerHitbox = player.getBounds();
        int[][] worldGrid = tileManager.getTileMap().getMap();
        int tw = tileManager.getTileWidth();
        int th = tileManager.getTileHeight();

        // Calculate which tiles the player is touching
        int columnStart = playerHitbox.x / tw;
        int columnEnd = (playerHitbox.x + playerHitbox.width) / tw;
        int rowStart = playerHitbox.y / th;
        int rowEnd = (playerHitbox.y + playerHitbox.height) / th;

        for (int row = rowStart; row <= rowEnd; row++) {
            for (int col = columnStart; col <= columnEnd; col++) {
                // Bounds check
                if (row >= 0 && row < tileManager.getTileMap().getRows() &&
                    col >= 0 && col < tileManager.getTileMap().getCols()) {

                    int tileType = worldGrid[row][col];

                    // Portal (7) and Air (0) are not solid
                    if (tileType == 0 || tileType == 7) {
                        continue;
                    }

                    // Check actual intersection
                    Rectangle tileBounds = tileManager.getBound(row, col);
                    if (playerHitbox.intersects(tileBounds)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
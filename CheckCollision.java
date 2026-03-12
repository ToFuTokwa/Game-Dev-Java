import java.awt.Rectangle;

public class CheckCollision {

    public boolean isColliding(Player player, TileManager tileManager) {
        if (tileManager == null || tileManager.getTileMap() == null) return false;

        Rectangle pRect = player.getBounds();
        int[][] map = tileManager.getTileMap().getMap();
        
        // Optimization: Only check tiles in the player's immediate area
        int startCol = pRect.x / 32;
        int endCol = (pRect.x + pRect.width) / 32;
        int startRow = pRect.y / 32;
        int endRow = (pRect.y + pRect.height) / 32;

        for (int row = startRow; row <= endRow; row++) {
            for (int col = startCol; col <= endCol; col++) {
                // Ensure we stay within array bounds
                if (row >= 0 && row < tileManager.getTileMap().getRows() && 
                    col >= 0 && col < tileManager.getTileMap().getCols()) {
                    
                    if (map[row][col] != 0) {
                        Rectangle tRect = tileManager.getBound(row, col);
                        if (pRect.intersects(tRect)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
}
public class TileMap {

    private int[][] map;

    // Constructor: accepts a 2D array for the map
    public TileMap(int[][] map) {
        if (map == null || map.length == 0 || map[0].length == 0) {
            throw new IllegalArgumentException("Map cannot be null or empty");
        }
        this.map = map;
    }

    // Returns the whole map
    public int[][] getMap() {
        return map;
    }

    // Get tile at specific row and column
    public int getTile(int row, int col) {
        if (!isValidPosition(row, col)) return -1; // outside map
        return map[row][col];
    }

    // Set tile at specific row and column
    public void setTile(int row, int col, int tile) {
        if (!isValidPosition(row, col)) return;
        map[row][col] = tile;
    }

    // Get number of rows
    public int getRows() {
        return map.length;
    }

    // Get number of columns
    public int getCols() {
        return map[0].length;
    }

    // Helper: check if a position is inside the map bounds
    private boolean isValidPosition(int row, int col) {
        return row >= 0 && row < map.length && col >= 0 && col < map[0].length;
    }
}

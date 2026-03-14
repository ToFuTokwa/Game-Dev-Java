import java.io.*;
import java.util.*;

public class TileMap {
    private int[][] mapGrid;

    public TileMap(int[][] initialGrid) {
        this.mapGrid = initialGrid;
    }

    public TileMap(String filePath) {
        List<int[]> rowList = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue; 

                // Splits the text file by commas to get individual tile IDs
                String[] values = line.split(",");
                int[] row = new int[values.length];
                for (int i = 0; i < values.length; i++) {
                    row[i] = Integer.parseInt(values[i].trim());
                }
                rowList.add(row);
            }
        } catch (Exception e) {
            System.err.println("Error loading map: " + e.getMessage());
        }
        mapGrid = rowList.toArray(new int[rowList.size()][]);
    }

    public int[][] getMap() { return mapGrid; }
    public int getTile(int row, int col) {
        if (row < 0 || row >= mapGrid.length || col < 0 || col >= mapGrid[0].length) return -1;
        return mapGrid[row][col];
    }
    public void setTile(int row, int col, int id) { mapGrid[row][col] = id; }
    public int getRows() { return mapGrid.length; }
    public int getCols() { return mapGrid[0].length; }
}
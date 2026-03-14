import java.io.*;
import java.util.*;

public class TileMap {

    private int[][] grid;

    public TileMap(int[][] initialGrid) {
        if (initialGrid == null || initialGrid.length == 0 || initialGrid[0].length == 0) {
            throw new IllegalArgumentException("Grid cannot be null or empty");
        }
        this.grid = initialGrid;
    }

    public TileMap(String filePath) {
        List<int[]> rows = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if(line.isEmpty()) continue; 

                // CHANGED: Split by comma to match TileEditorFrame output
                String[] numbers = line.split(",");
                int[] row = new int[numbers.length];

                for (int i = 0; i < numbers.length; i++) {
                    row[i] = Integer.parseInt(numbers[i].trim());
                }
                rows.add(row);
            }
        } catch (IOException e) {
            System.err.println("Error reading tile map file: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.err.println("Invalid number in tile map file: " + e.getMessage());
        }

        if (rows.isEmpty()) {
            throw new IllegalArgumentException("Tile map file is empty or invalid: " + filePath);
        }
        grid = rows.toArray(new int[rows.size()][]);
    }

    public int[][] getMap() { return grid; }
    public int getTile(int row, int col) {
        if (!isWithinBounds(row, col)) return -1;
        return grid[row][col];
    }
    public void setTile(int row, int col, int tileID) {
        if (!isWithinBounds(row, col)) return;
        grid[row][col] = tileID;
    }
    public int getRows() { return grid.length; }
    public int getCols() { return grid[0].length; }
    private boolean isWithinBounds(int row, int col) {
        return row >= 0 && row < grid.length && col >= 0 && col < grid[0].length;
    }
}
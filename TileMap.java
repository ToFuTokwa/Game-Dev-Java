import java.io.*;
import java.util.*;

public class TileMap {
    private int[][] grid;

    public TileMap(int[][] initial) { this.grid = initial; }
    public TileMap(String path) {
        List<int[]> rows = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] vals = line.trim().split(",");
                int[] row = new int[vals.length];
                for (int i = 0; i < vals.length; i++) row[i] = Integer.parseInt(vals[i].trim());
                rows.add(row);
            }
        } catch (Exception e) { System.err.println("Load error: " + path); }
        grid = rows.toArray(new int[rows.size()][]);
    }

    public int[][] getMap() { return grid; }
    public int getTile(int r, int c) {
        if (r < 0 || r >= grid.length || c < 0 || c >= grid[0].length) return -1;
        return grid[r][c];
    }
}
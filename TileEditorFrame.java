import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.Scanner;

public class TileEditorFrame extends JFrame {

    private int tileSize = 32;
    private int maxColumns = 40;
    private int maxRows = 23;

    private TileMap currentMap;
    private TileManager tileManager;
    private int selectedTileID = 1; 
    private boolean isPainting = false;

    public TileEditorFrame() {
        setTitle("World Editor");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(maxColumns * tileSize + 220, maxRows * tileSize + 40); 
        setLayout(null);

        // Initialize with empty grid
        int[][] emptyGrid = new int[maxRows][maxColumns];
        currentMap = new TileMap(emptyGrid);
        tileManager = new TileManager(currentMap);

        JPanel drawingCanvas = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                tileManager.draw(g);
                // Draw Grid Lines for easier editing
                g.setColor(new Color(255, 255, 255, 50));
                for(int i = 0; i <= maxColumns; i++) g.drawLine(i * tileSize, 0, i * tileSize, maxRows * tileSize);
                for(int i = 0; i <= maxRows; i++) g.drawLine(0, i * tileSize, maxColumns * tileSize, i * tileSize);
            }
        };
        drawingCanvas.setBounds(0, 0, maxColumns * tileSize, maxRows * tileSize);
        drawingCanvas.setBackground(Color.BLACK);

        MouseAdapter canvasMouseControls = new MouseAdapter() {
            private void updateTileAtMouse(MouseEvent e) {
                int col = e.getX() / tileSize;
                int row = e.getY() / tileSize;
                if (row >= 0 && row < maxRows && col >= 0 && col < maxColumns) {
                    currentMap.getMap()[row][col] = selectedTileID;
                    drawingCanvas.repaint();
                }
            }
            @Override public void mousePressed(MouseEvent e) { isPainting = true; updateTileAtMouse(e); }
            @Override public void mouseReleased(MouseEvent e) { isPainting = false; }
            @Override public void mouseDragged(MouseEvent e) { if (isPainting) updateTileAtMouse(e); }
        };

        drawingCanvas.addMouseListener(canvasMouseControls);
        drawingCanvas.addMouseMotionListener(canvasMouseControls);
        add(drawingCanvas);

        // Sidebar for Controls
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new GridLayout(14, 1, 5, 5));
        sidebar.setBounds(maxColumns * tileSize + 10, 0, 180, maxRows * tileSize);

        String[] tileLabels = {"Air (0)", "Dirt (1)", "Grass (2)", "FullGreen (3)", "LeftFloat (4)", "MiddleFloat (5)", "RightFloat (6)", "Portal (7)"};
        for (int i = 0; i < tileLabels.length; i++) {
            final int id = i;
            JButton tileButton = new JButton(tileLabels[i]);
            tileButton.addActionListener(e -> selectedTileID = id);
            sidebar.add(tileButton);
        }

        sidebar.add(new JSeparator());

        // FIX: Added "Clear Map" to start a new level easily
        JButton clearButton = new JButton("Clear / New Map");
        clearButton.addActionListener(e -> {
            int[][] emptyGrid2 = new int[maxRows][maxColumns];
            currentMap = new TileMap(emptyGrid2);
            tileManager.setTileMap(currentMap);
            drawingCanvas.repaint();
        });
        sidebar.add(clearButton);

        JButton saveButton = new JButton("Save Level"); 
        saveButton.addActionListener(e -> saveMapData());
        
        JButton loadButton = new JButton("Load Level"); 
        loadButton.addActionListener(e -> { loadMapData(); drawingCanvas.repaint(); });

        sidebar.add(saveButton);
        sidebar.add(loadButton);
        add(sidebar);

        new Timer(16, e -> drawingCanvas.repaint()).start();
        setVisible(true);
    }

    private void saveMapData() {
        // You can type level1, level2, or level3 here to create multiple files
        String name = JOptionPane.showInputDialog(this, "Enter level name (e.g., level1):", "level1");
        if (name == null || name.isEmpty()) return;

        File dir = new File("levels");
        if (!dir.exists()) dir.mkdir();

        try (PrintWriter writer = new PrintWriter(new File("levels/" + name + ".txt"))) {
            int[][] grid = currentMap.getMap();
            for (int r = 0; r < maxRows; r++) {
                for (int c = 0; c < maxColumns; c++) {
                    // Saves with commas to match your TileMap reader
                    writer.print(grid[r][c] + (c < maxColumns - 1 ? "," : ""));
                }
                writer.println();
            }
            System.out.println("Saved successfully: levels/" + name + ".txt");
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void loadMapData() {
        String name = JOptionPane.showInputDialog(this, "Load level name:", "level1");
        if (name == null || name.isEmpty()) return;
        File file = new File("levels/" + name + ".txt");
        if (!file.exists()) {
            JOptionPane.showMessageDialog(this, "File not found!");
            return;
        }
        try (Scanner reader = new Scanner(file)) {
            int[][] grid = currentMap.getMap();
            int r = 0;
            while (reader.hasNextLine() && r < maxRows) {
                String[] values = reader.nextLine().split(",");
                for (int c = 0; c < values.length && c < maxColumns; c++)
                    grid[r][c] = Integer.parseInt(values[c].trim());
                r++;
            }
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    public static void main(String[] args) { new TileEditorFrame(); }
}
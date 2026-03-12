import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.Scanner;

public class TileEditorFrame extends JFrame {

    private int tileSize = 32;
    private int columns = 40;
    private int rows = 23;

    private TileMap currentMap;
    private TileManager tileManager;

    private int selectedTile = 1; 
    private boolean isDragging = false;

    private File mapFolder = new File("maps");

    public TileEditorFrame() {
        setTitle("Tile Editor");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(columns * tileSize + 220, rows * tileSize + 40); 
        setLayout(null);

        if (!mapFolder.exists()) mapFolder.mkdirs();

        int[][] map = new int[rows][columns];
        currentMap = new TileMap(map);
        tileManager = new TileManager(currentMap);

        JPanel renderPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                tileManager.draw(g);
                
                g.setColor(new Color(255, 255, 255, 50));
                for(int i = 0; i <= columns; i++) g.drawLine(i * tileSize, 0, i * tileSize, rows * tileSize);
                for(int i = 0; i <= rows; i++) g.drawLine(0, i * tileSize, columns * tileSize, i * tileSize);
            }
        };
        renderPanel.setBounds(0, 0, columns * tileSize, rows * tileSize);
        renderPanel.setBackground(Color.BLACK);

        MouseAdapter mouseAdapter = new MouseAdapter() {
            private void paintTile(MouseEvent e) {
                int col = e.getX() / tileSize;
                int row = e.getY() / tileSize;
                if (row >= 0 && row < rows && col >= 0 && col < columns) {
                    currentMap.getMap()[row][col] = selectedTile;
                    renderPanel.repaint();
                }
            }

            @Override public void mousePressed(MouseEvent e) { isDragging = true; paintTile(e); }
            @Override public void mouseReleased(MouseEvent e) { isDragging = false; }
            @Override public void mouseDragged(MouseEvent e) { if (isDragging) paintTile(e); }
        };

        renderPanel.addMouseListener(mouseAdapter);
        renderPanel.addMouseMotionListener(mouseAdapter);
        add(renderPanel);

        // Palette Panel
        JPanel palette = new JPanel();
        palette.setLayout(new GridLayout(12, 1, 5, 5));
        palette.setBounds(columns * tileSize + 10, 0, 180, rows * tileSize);

        String[] labels = {"Empty (0)", "Dirt (1)", "Grass (2)", "FullGreen (3)", "LeftFloat (4)", "MiddleFloat (5)", "RightFloat (6)", "Portal (7)"};
        for (int i = 0; i < labels.length; i++) {
            final int id = i;
            JButton btn = new JButton(labels[i]);
            btn.addActionListener(e -> selectedTile = id);
            palette.add(btn);
        }

        JButton saveBtn = new JButton("Save Map"); saveBtn.addActionListener(e -> saveMap());
        JButton loadBtn = new JButton("Load Map"); loadBtn.addActionListener(e -> { loadMap(); renderPanel.repaint(); });

        palette.add(new JSeparator());
        palette.add(saveBtn);
        palette.add(loadBtn);

        add(palette);

        // REFRESH TIMER: This is what makes the portal animate in the editor
        Timer refreshTimer = new Timer(16, e -> renderPanel.repaint());
        refreshTimer.start();

        setVisible(true);
    }

    private void saveMap() {
        String fileName = JOptionPane.showInputDialog(this, "Enter map name:", "level1");
        if (fileName == null || fileName.isEmpty()) return;

        File file = new File(mapFolder, fileName + ".txt");
        try (PrintWriter pw = new PrintWriter(file)) {
            int[][] map = currentMap.getMap();
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < columns; c++) {
                    pw.print(map[r][c] + (c < columns - 1 ? "," : ""));
                }
                pw.println();
            }
            JOptionPane.showMessageDialog(this, "Map saved!");
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void loadMap() {
        String fileName = JOptionPane.showInputDialog(this, "Enter map name to load:", "level1");
        if (fileName == null || fileName.isEmpty()) return;

        File file = new File(mapFolder, fileName + ".txt");
        if (!file.exists()) return;

        try (Scanner sc = new Scanner(file)) {
            int[][] mapData = currentMap.getMap();
            int r = 0;
            while (sc.hasNextLine() && r < rows) {
                String[] values = sc.nextLine().split(",");
                for (int c = 0; c < values.length && c < columns; c++)
                    mapData[r][c] = Integer.parseInt(values[c]);
                r++;
            }
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    public static void main(String[] args) {
        new TileEditorFrame();
    }
}

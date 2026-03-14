import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.*;

public class TileEditorFrame extends JFrame {
    private final int tileSize = 32;
    private final int rows = 23;
    private final int cols = 40;
    
    private LevelManager levelManager;
    private TileManager tileManager;
    private int selectedTileID = 1;
    private BufferedImage backgroundImage;

    public TileEditorFrame() {
        setTitle("SHinRa World Editor");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(cols * tileSize + 220, rows * tileSize + 100);
        setLayout(null);

        // Initialize LevelManager
        levelManager = new LevelManager();
        
        // Initial setup for Level 0
        updateEditorLevel(0);

        // Drawing Area (Canvas)
        JPanel canvas = new JPanel() {
            @Override 
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                
                // 1. Draw Background from LevelManager
                if (backgroundImage != null) {
                    g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), null);
                } else {
                    g.setColor(Color.BLACK);
                    g.fillRect(0, 0, getWidth(), getHeight());
                }

                // 2. Draw Tiles via TileManager
                if (tileManager != null) {
                    tileManager.draw(g);
                }

                // 3. Draw Grid Overlay
                g.setColor(new Color(255, 255, 255, 40));
                for (int i = 0; i <= cols; i++) g.drawLine(i * tileSize, 0, i * tileSize, rows * tileSize);
                for (int i = 0; i <= rows; i++) g.drawLine(0, i * tileSize, cols * tileSize, i * tileSize);
            }
        };
        
        canvas.setBounds(10, 10, cols * tileSize, rows * tileSize);
        canvas.setOpaque(false);

        // Mouse Controls for Painting
        MouseAdapter ma = new MouseAdapter() {
            private void paint(MouseEvent e) {
                int c = e.getX() / tileSize;
                int r = e.getY() / tileSize;
                if (r >= 0 && r < rows && c >= 0 && c < cols) {
                    // Right click erases (sets to 0), Left click paints selected ID
                    levelManager.getCurrentLevel().getMap()[r][c] = 
                        (SwingUtilities.isRightMouseButton(e)) ? 0 : selectedTileID;
                    canvas.repaint();
                }
            }
            @Override public void mousePressed(MouseEvent e) { paint(e); }
            @Override public void mouseDragged(MouseEvent e) { paint(e); }
        };
        
        canvas.addMouseListener(ma);
        canvas.addMouseMotionListener(ma);
        add(canvas);

        // Sidebar Setup
        JPanel sidebar = new JPanel();
        sidebar.setBounds(cols * tileSize + 20, 10, 180, rows * tileSize);
        sidebar.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

        // Level Selection Dropdown
        sidebar.add(new JLabel("Select Level:"));
        JComboBox<String> levelSelector = new JComboBox<>(levelManager.getLevelNames());
        levelSelector.setPreferredSize(new Dimension(160, 30));
        levelSelector.addActionListener(e -> {
            updateEditorLevel(levelSelector.getSelectedIndex());
            canvas.repaint();
        });
        sidebar.add(levelSelector);

        // Save Button
        JButton btnSave = new JButton("Save Current Level");
        btnSave.setPreferredSize(new Dimension(160, 30));
        btnSave.addActionListener(e -> saveCurrentMap());
        sidebar.add(btnSave);

        sidebar.add(new JSeparator(JSeparator.HORIZONTAL));

        // Tile Selection Buttons
        String[] tileNames = {"Eraser (Air)", "Dirt", "Grass", "Full Green", "Portal (7)"};
        int[] ids = {0, 1, 2, 3, 7};
        for (int i = 0; i < tileNames.length; i++) {
            int id = ids[i];
            JButton b = new JButton(tileNames[i]);
            b.setPreferredSize(new Dimension(160, 30));
            b.addActionListener(e -> selectedTileID = id);
            sidebar.add(b);
        }

        add(sidebar);
        
        // Animation Timer (Keeps portal spinning)
        new Timer(150, e -> canvas.repaint()).start();

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void updateEditorLevel(int index) {
        levelManager.setLevel(index);
        
        // Update TileManager reference
        if (tileManager == null) {
            tileManager = new TileManager(levelManager.getCurrentLevel());
        } else {
            tileManager.setTileMap(levelManager.getCurrentLevel());
        }

        // Load background based on LevelManager path
        try {
            String path = levelManager.getCurrentBackgroundPath();
            File f = new File(path);
            if (f.exists()) {
                backgroundImage = ImageIO.read(f);
            } else {
                backgroundImage = null;
                System.out.println("Background image not found: " + path);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveCurrentMap() {
        String name = JOptionPane.showInputDialog(this, "Save file name (e.g., level1):");
        
        if (name == null || name.trim().isEmpty()) {
            return;
        }

        // Ensure levels directory exists
        File dir = new File("levels");
        if (!dir.exists()) dir.mkdir();

        try (PrintWriter pw = new PrintWriter(new File("levels/" + name + ".txt"))) {
            int[][] grid = levelManager.getCurrentLevel().getMap();
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    pw.print(grid[r][c] + (c < cols - 1 ? "," : ""));
                }
                pw.println();
            }
            JOptionPane.showMessageDialog(this, "Level saved: levels/" + name + ".txt");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error saving: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(TileEditorFrame::new);
    }
}
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class GamePanel extends JPanel implements Runnable {

    //screenVariable
    private int tileSize = 32;
    private int columnCount = 40;
    private int rowCount = 23;
    private int screenWidth = columnCount * tileSize;
    private int screenHeigth = rowCount * tileSize;
    
    // Fixed: Relative path for background
    private ImageIcon backgroundIcon = new ImageIcon("Assets/tempBackGround.jpg");
    private Image backgroundImage = backgroundIcon.getImage();

    // FPS
    int FPS = 60; 

    // Components
    Player player1 = new Player();
    TileMap tileMap;
    TileManager tileManager;
    Thread gameThread;

    public GamePanel(){
        this.setPreferredSize(new Dimension(screenWidth, screenHeigth));
        this.setBackground(Color.black);
        this.setDoubleBuffered(true);
        this.addKeyListener(player1);
        this.addMouseListener(player1);
        this.setFocusable(true);

        loadDefaultMap();
    }

    private void loadDefaultMap() {
        File file = new File("maps/level1.txt");
        if (file.exists()) {
            int[][] data = new int[rowCount][columnCount];
            try (Scanner sc = new Scanner(file)) {
                int r = 0;
                while (sc.hasNextLine() && r < rowCount) {
                    String[] values = sc.nextLine().split(",");
                    for (int c = 0; c < values.length && c < columnCount; c++)
                        data[r][c] = Integer.parseInt(values[c]);
                    r++;
                }
                tileMap = new TileMap(data);
                tileManager = new TileManager(tileMap);
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    public void startGameThread(){
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void run() {
        double drawInterval = 1000000000 / FPS;
        double nextDrawTime = System.nanoTime() + drawInterval;

        while (gameThread != null) {
            update();
            repaint();

            try {
                double remainingTime = (nextDrawTime - System.nanoTime()) / 1000000;
                if (remainingTime < 0) remainingTime = 0;
                Thread.sleep((long) remainingTime);
                nextDrawTime += drawInterval;
            } catch (InterruptedException e) {
                e.printStackTrace();
            } 
        } 
    } 

    public void update(){
        player1.update();
    }

    protected void paintComponent(Graphics g){
        super.paintComponent(g);
        // Draw Background
        g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        
        // Draw Tiles
        if (tileManager != null) {
            tileManager.draw(g);
        }
        
        // Draw Player
        player1.draw(g);
        g.dispose();
    }
}

import java.awt.*;
import java.util.Scanner;
import javax.swing.*;
import java.io.File;

public class GamePanel extends JPanel implements Runnable {

    private int tileSize = 32;
    private int columnCount = 40;
    private int rowCount = 23;
    private int screenWidth = columnCount * tileSize;
    private int screenHeigth = rowCount * tileSize;
    
    private Image backgroundImage = new ImageIcon("Assets/tempBackGround.jpg").getImage();
    int FPS = 60; 

    Player player1 = new Player();
    TileMap tileMap;
    TileManager tileManager;
    CheckCollision collisionChecker = new CheckCollision(); // Added
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
            } catch (InterruptedException e) { e.printStackTrace(); } 
        } 
    } 

    public void update(){
        // Pass necessary objects for collision check
        player1.update(collisionChecker, tileManager);
    }

    protected void paintComponent(Graphics g){
        super.paintComponent(g);
        g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        if (tileManager != null) tileManager.draw(g);
        player1.draw(g);
        g.dispose();
    }
}
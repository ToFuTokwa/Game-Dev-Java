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
    private ImageIcon backgroundIcon = new ImageIcon("C:/Users/mark/Desktop/Game-Dev-Javas/Assets/tempBackGround.jpg");
    private Image backgroundImage = backgroundIcon.getImage();

    //FPS
    int FPS = 60; 

    //player1
    Player player1 = new Player();

    //run = True in Python?
    Thread gameThread;

    public GamePanel(){

        this.setPreferredSize(new Dimension(screenWidth, screenHeigth));
        this.setBackground(Color.black);
        this.setDoubleBuffered(true);
        this.addKeyListener(player1);
        this.addMouseListener(player1);
        this.setFocusable(true);
    }

    public void startGameThread(){

        gameThread = new Thread(this);
        gameThread.start();

    }

    @Override
    public void run() {

        double drawInterval = 1000000000 / FPS;
        double nextDrawTime = System.nanoTime() + drawInterval;

        while (gameThread != null) { // START while

            update();
            repaint();

            try { // START try
                double remainingTime = nextDrawTime - System.nanoTime();
                remainingTime = remainingTime / 1000000;

                if (remainingTime < 0) {
                    remainingTime = 0;
                }

                Thread.sleep((long) remainingTime);
                
                nextDrawTime += drawInterval;

            } catch (InterruptedException e) { // Close try AND start catch
                e.printStackTrace();

            } 
        } 
    } 

    public void update(){
        player1.update();
    }

    protected void paintComponent(Graphics g){

        super.paintComponent(g);
        g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        player1.draw(g);
        g.dispose();
    
    }
}

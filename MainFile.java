import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class MainFile {
    public static void main(String[]args){

        //screenFunction
        JFrame window = new JFrame();
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(false);
        window.setTitle("NewShit");

        //basically what it does is set value for our gameScreen
        GamePanel gamePanel = new GamePanel();
        window.add(gamePanel);

        //drawFunction for screen
        window.pack();
        window.setLocationRelativeTo(null);
        window.setVisible(true);
        gamePanel.startGameThread();

    }
}

import javax.swing.*;
import java.awt.*;

public class MainFile {
    public static void main(String[] args) {
        JFrame window = new JFrame();
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(false);
        window.setTitle("NewShit");
        
        // Create CardLayout and the main container
        CardLayout cardLayout = new CardLayout();
        JPanel mainPanel = new JPanel(cardLayout);

        // pass cardLayout and mainPanel into this constructor)
        GamePanel gamePanel = new GamePanel(cardLayout, mainPanel); 

        // Initialize HomeUI
        HomeUI homeUI = new HomeUI(cardLayout, mainPanel, gamePanel);

        // Make sure you have created the GameOverPanel.java class first.
        GameOverPanel gameOverPanel = new GameOverPanel(cardLayout, mainPanel, gamePanel);

        // Add screens to the main container
        mainPanel.add(homeUI, "Home");
        mainPanel.add(gamePanel, "Game");
        mainPanel.add(gameOverPanel, "GameOver");

        window.add(mainPanel);
        window.pack();
        window.setLocationRelativeTo(null);
        window.setVisible(true);
    }
}
import javax.swing.*;
import java.awt.*;

public class MainFile {
    public static void main(String[] args) {
        JFrame window = new JFrame();
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(false);
        window.setTitle("NewShit");
        
        // 1. Create CardLayout and the main container
        CardLayout cardLayout = new CardLayout();
        JPanel mainPanel = new JPanel(cardLayout);

        // 2. Initialize the GamePanel
        // (Note: If you followed the previous guide, you might need to 
        // pass cardLayout and mainPanel into this constructor)
        GamePanel gamePanel = new GamePanel(cardLayout, mainPanel); 

        // 3. Initialize HomeUI
        HomeUI homeUI = new HomeUI(cardLayout, mainPanel, gamePanel);

        // 4. Create the GameOverPanel object (This is what was missing!)
        // Make sure you have created the GameOverPanel.java class first.
        GameOverPanel gameOverPanel = new GameOverPanel(cardLayout, mainPanel, gamePanel);

        // 5. Add screens to the main container
        mainPanel.add(homeUI, "Home");
        mainPanel.add(gamePanel, "Game");
        mainPanel.add(gameOverPanel, "GameOver"); // Fixed typo: 'mainPanel' instead of 'mainpanel'

        window.add(mainPanel);
        window.pack();
        window.setLocationRelativeTo(null);
        window.setVisible(true);
    }
}
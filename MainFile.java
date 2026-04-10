import javax.swing.*;
import java.awt.*;

public class MainFile {
    public static void main(String[] args) {
        JFrame window = new JFrame();
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(false);
        window.setTitle("Dungeon Venture"); // Updated title
        
        CardLayout cardLayout = new CardLayout();
        JPanel mainPanel = new JPanel(cardLayout);

        // 1. Initialize all panels
        GamePanel gamePanel = new GamePanel(cardLayout, mainPanel); 
        HomeUI homeUI = new HomeUI(cardLayout, mainPanel, gamePanel);
        GameOverPanel gameOverPanel = new GameOverPanel(cardLayout, mainPanel, gamePanel);
        Level3EndPanel endingPanel = new Level3EndPanel(cardLayout, mainPanel);

        // 2. Add screens to the main container in a specific order
        // Index 0
        mainPanel.add(homeUI, "Home");
        // Index 1
        mainPanel.add(gamePanel, "Game");
        // Index 2
        mainPanel.add(gameOverPanel, "GameOver");
        // Index 3
        mainPanel.add(endingPanel, "Ending"); 

        window.add(mainPanel);
        window.pack();
        window.setLocationRelativeTo(null);
        window.setVisible(true);

        // Start at the Home screen
        cardLayout.show(mainPanel, "Home");
    }
}
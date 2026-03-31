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

        // Initialize the GamePanel
        GamePanel gamePanel = new GamePanel();

        // Initialize HomeUI and pass the layout and panel to it
        HomeUI homeUI = new HomeUI(cardLayout, mainPanel, gamePanel);

        // Add both screens to the main container with unique names
        mainPanel.add(homeUI, "Home");
        mainPanel.add(gamePanel, "Game");

        window.add(mainPanel);
        window.pack();
        window.setLocationRelativeTo(null);
        window.setVisible(true);
    }
}

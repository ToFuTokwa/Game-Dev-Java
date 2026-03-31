import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class HomeUI extends JPanel {
    private final CardLayout cardLayout;
    private final JPanel mainPanel;
    private final GamePanel gamePanel;

    // Variable
    private int screenWidth = 1280;
    private int screenHeight = 736;

    // Button
    private JButton startButton;
    private JButton quitButton;

    // Images
    private Image bgImage;
    private final Color COLOR_BG = Color.BLACK;
    private final Color COLOR_ACCENT = (Color.LIGHT_GRAY);
    private final Font FONT_BUTTON = new Font("Arial", Font.BOLD, 22);

    public HomeUI(CardLayout cardLayout, JPanel mainPanel, GamePanel gamePanel) {
        this.cardLayout = cardLayout;
        this.mainPanel = mainPanel;
        this.gamePanel = gamePanel;
        
        imgLoader();
        initializePanel();
        createComponents();
        setupActions();
        layoutComponents();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawBackgroundImage(g);
    }

    private void imgLoader(){
        try {
            // Updated to .png based on your last snippet
            File imgFile = new File("Assets/HomeBackground.png");
            if (imgFile.exists()) {
                this.bgImage = ImageIO.read(imgFile);
            } else {
                System.out.println("Error: Background image not found at " + imgFile.getAbsolutePath());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void drawBackgroundImage(Graphics g) {
        if (bgImage != null) {
            g.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);
        } else {
            g.setColor(COLOR_BG);
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    private void initializePanel() {
        this.setPreferredSize(new Dimension(screenWidth, screenHeight));
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.setOpaque(true); 
    }

    private void createComponents() {
        startButton = createStyledButton("START GAME");
        quitButton = createStyledButton("QUIT");
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(FONT_BUTTON);
        button.setForeground(COLOR_ACCENT);
        
        button.setContentAreaFilled(false); 
        button.setBorderPainted(false);      
        button.setFocusPainted(false);
        button.setOpaque(false);
        
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setForeground(new Color(255, 255, 255));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setForeground(COLOR_ACCENT);
            }
        });
        
        return button;
    }

    private void setupActions() {
        startButton.addActionListener(e -> {
            cardLayout.show(mainPanel, "Game");
            gamePanel.requestFocusInWindow();
            gamePanel.startGameThread();
        });

        quitButton.addActionListener(e -> System.exit(0));
    }

    private void layoutComponents() {
        // This centers the buttons vertically in the screen
        add(Box.createVerticalGlue());
        add(startButton);
        add(Box.createRigidArea(new Dimension(0, 20))); // Space between buttons
        add(quitButton);
        add(Box.createVerticalGlue());
    }
}

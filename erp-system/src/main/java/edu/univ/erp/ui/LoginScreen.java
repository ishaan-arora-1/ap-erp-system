package edu.univ.erp.ui;

import com.formdev.flatlaf.FlatClientProperties;
import edu.univ.erp.auth.AuthService;
import edu.univ.erp.auth.SessionManager;
import edu.univ.erp.domain.User;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URL;

/**
 * LoginScreen - Modern UI design for the University ERP system.
 * 
 * Design Features:
 * - Full-screen background image (nature/campus)
 * - Central white card with rounded corners
 * - Modern typography (Segoe UI)
 * - FlatLaf styling for input fields (placeholders, reveal button)
 * - Custom painted gradient/image background
 */
public class LoginScreen extends JFrame {

    private final AuthService authService;
    private JTextField userField;
    private JPasswordField passField;

    // Asset paths - make sure these exist in src/main/resources/images/
    private static final String BG_IMAGE_PATH = "/images/bg.jpeg"; 
    private static final String LOGO_IMAGE_PATH = "/images/logo2.png";
    
    // Colors from the screenshot
    private static final Color PRIMARY_COLOR = new Color(108, 92, 231); // Purple-ish login button
    private static final Color CARD_BG = Color.WHITE;
    private static final Color TEXT_COLOR = new Color(50, 50, 50);
    private static final Color SUBTEXT_COLOR = new Color(100, 100, 100);

    public LoginScreen() {
        this.authService = new AuthService();

        setTitle("University ERP - Sign In");
        setSize(1000, 700); // Larger default size for the background to look good
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Use a LayeredPane or simple content pane with custom painting
        setContentPane(new BackgroundPanel());
        setLayout(new GridBagLayout()); // Centers the card

        initComponents();
    }

    private void initComponents() {
        // --- The Main Card Panel ---
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createEmptyBorder(40, 50, 50, 50));
        
        // Rounded corners for the card itself (FlatLaf feature)
        card.putClientProperty(FlatClientProperties.STYLE, "arc: 20");
        
        // Limit card width
        card.setPreferredSize(new Dimension(450, 650));
        card.setMaximumSize(new Dimension(450, 700));

        // 1. Logo
        JLabel logoLabel = new JLabel();
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        loadLogo(logoLabel);
        
        // 2. Title "Sign-in to your ERP"
        JLabel titleLabel = new JLabel("Sign-in to your ERP");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(TEXT_COLOR);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // 3. Input Fields
        JPanel fieldsPanel = new JPanel();
        fieldsPanel.setLayout(new BoxLayout(fieldsPanel, BoxLayout.Y_AXIS));
        fieldsPanel.setBackground(CARD_BG);
        fieldsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        fieldsPanel.setMaximumSize(new Dimension(350, 200));

        // Username
        JLabel userLabel = new JLabel("Username");
        userLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        userLabel.setForeground(SUBTEXT_COLOR);
        
        userField = new JTextField();
        userField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter your username");
        userField.putClientProperty(FlatClientProperties.STYLE, "arc: 10; margin: 5,10,5,10");
        userField.setMaximumSize(new Dimension(350, 40));
        userField.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Password
        JLabel passLabel = new JLabel("Password");
        passLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        passLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        passLabel.setForeground(SUBTEXT_COLOR);

        passField = new JPasswordField();
        passField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter your password");
        passField.putClientProperty(FlatClientProperties.STYLE, "arc: 10; margin: 5,10,5,10; showRevealButton: true");
        passField.setMaximumSize(new Dimension(350, 40));
        passField.setAlignmentX(Component.LEFT_ALIGNMENT);

        fieldsPanel.add(userLabel);
        fieldsPanel.add(Box.createVerticalStrut(5));
        fieldsPanel.add(userField);
        fieldsPanel.add(Box.createVerticalStrut(15));
        fieldsPanel.add(passLabel);
        fieldsPanel.add(Box.createVerticalStrut(5));
        fieldsPanel.add(passField);

        // 4. Login Button
        JButton loginBtn = new JButton("Login");
        loginBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        loginBtn.setBackground(PRIMARY_COLOR);
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setFocusPainted(false);
        loginBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginBtn.setMaximumSize(new Dimension(150, 40));
        loginBtn.setPreferredSize(new Dimension(150, 40));
        loginBtn.putClientProperty(FlatClientProperties.STYLE, "arc: 10"); // Rounded button
        
        // Add hover effect
        loginBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { loginBtn.setBackground(PRIMARY_COLOR.brighter()); }
            public void mouseExited(MouseEvent e) { loginBtn.setBackground(PRIMARY_COLOR); }
        });
        loginBtn.addActionListener(e -> handleLogin());
        getRootPane().setDefaultButton(loginBtn);

        // --- Assembly ---
        card.add(Box.createVerticalStrut(10));
        card.add(logoLabel);
        card.add(Box.createVerticalStrut(20));
        card.add(titleLabel);
        card.add(Box.createVerticalStrut(40));
        card.add(fieldsPanel);
        card.add(Box.createVerticalStrut(30));
        card.add(loginBtn);

        add(card);
    }

    private void handleLogin() {
        String username = userField.getText().trim();
        String password = new String(passField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both credentials.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            // Using the existing AuthService logic
            User user = authService.login(username, password);
            if (user != null) {
                SessionManager.login(user);
                dispose();
                new DashboardScreen().setVisible(true);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Login Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Helper to load logo safely with aspect ratio preservation
    private void loadLogo(JLabel label) {
        URL imgUrl = getClass().getResource(LOGO_IMAGE_PATH);
        if (imgUrl != null) {
            ImageIcon icon = new ImageIcon(imgUrl);
            Image img = icon.getImage();
            
            // Preserve aspect ratio while scaling to fit max dimensions
            int maxWidth = 120;
            int maxHeight = 120;
            int originalWidth = img.getWidth(null);
            int originalHeight = img.getHeight(null);
            
            double widthRatio = (double) maxWidth / originalWidth;
            double heightRatio = (double) maxHeight / originalHeight;
            double scale = Math.min(widthRatio, heightRatio);
            
            int scaledWidth = (int) (originalWidth * scale);
            int scaledHeight = (int) (originalHeight * scale);
            
            Image scaledImg = img.getScaledInstance(scaledWidth, scaledHeight, Image.SCALE_SMOOTH);
            label.setIcon(new ImageIcon(scaledImg));
        } else {
            // Fallback text if image missing
            label.setText("<html><h1>IIITD</h1></html>");
            label.setForeground(PRIMARY_COLOR);
        }
    }

    // Custom Panel to paint the background image
    private class BackgroundPanel extends JPanel {
        private Image backgroundImage;

        public BackgroundPanel() {
            try {
                URL imgUrl = getClass().getResource(BG_IMAGE_PATH);
                if (imgUrl != null) {
                    backgroundImage = ImageIO.read(imgUrl);
                }
            } catch (IOException e) {
                // Ignore, will just use color
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (backgroundImage != null) {
                // Draw image scaled to fill the window
                g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
            } else {
                // Fallback gradient if no image
                Graphics2D g2d = (Graphics2D) g;
                GradientPaint gp = new GradientPaint(0, 0, new Color(100, 149, 237), 
                        getWidth(), getHeight(), new Color(255, 255, 255));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        }
    }
}

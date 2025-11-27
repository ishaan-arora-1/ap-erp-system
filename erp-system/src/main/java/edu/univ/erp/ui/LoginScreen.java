package edu.univ.erp.ui;

import edu.univ.erp.auth.AuthService;
import edu.univ.erp.auth.SessionManager;
import edu.univ.erp.domain.User;

import javax.swing.*;
import java.awt.*;

/**
 * LoginScreen - The application's first screen for user authentication
 * 
 * Features:
 * - Centered, modern UI with proper alignment
 * - Password security with JPasswordField
 * - Integration with AuthService for BCrypt password verification
 * - Account lockout after 5 failed attempts
 * - Session management integration
 */
public class LoginScreen extends JFrame {

    // UI Components - declared as final since they won't be reassigned
    private final JTextField userField;
    private final JPasswordField passField;
    
    // Service layer - handles authentication logic
    private final AuthService authService;

    public LoginScreen() {
        // Initialize the authentication service
        this.authService = new AuthService();

        // Configure window properties
        setTitle("University ERP - Login");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);  // Center on screen
        setResizable(false);  // Fixed size for consistent appearance

        // Initialize input fields
        this.userField = new JTextField();
        this.passField = new JPasswordField();  // Password field masks input

        // Build and display the UI
        initComponents();
    }

    /**
     * Initializes and arranges all UI components
     * Uses BoxLayout for vertical stacking with centered alignment
     */
    private void initComponents() {
        // Main container panel with vertical layout
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));  // Top, left, bottom, right padding

        // Title label - "Welcome Back"
        JLabel titleLabel = new JLabel("Welcome Back");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Username label - styled and centered
        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        usernameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Password label - styled and centered
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        passwordLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Username input field - fixed width prevents stretching
        userField.setMaximumSize(new Dimension(300, 30));
        userField.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Password input field - masks characters for security
        passField.setMaximumSize(new Dimension(300, 30));
        passField.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Login button with fixed size and rounded corners (FlatLaf feature)
        JButton loginBtn = new JButton("Login");
        loginBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginBtn.setPreferredSize(new Dimension(120, 35));
        loginBtn.setMaximumSize(new Dimension(120, 35));
        loginBtn.putClientProperty("JButton.buttonType", "roundRect");  // FlatLaf rounded corners
        loginBtn.addActionListener(e -> handleLogin());

        // Make login button the default - pressing Enter triggers login
        getRootPane().setDefaultButton(loginBtn);

        // Assemble components with vertical spacing
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(30));  // Large gap after title
        panel.add(usernameLabel);
        panel.add(Box.createVerticalStrut(5));   // Small gap between label and field
        panel.add(userField);
        panel.add(Box.createVerticalStrut(15));  // Medium gap between fields
        panel.add(passwordLabel);
        panel.add(Box.createVerticalStrut(5));
        panel.add(passField);
        panel.add(Box.createVerticalStrut(30));  // Large gap before button
        panel.add(loginBtn);

        add(panel);
    }

    /**
     * Handles the login button click event
     * 
     * Process:
     * 1. Validates input fields are not empty
     * 2. Calls AuthService to verify credentials (BCrypt password check)
     * 3. On success: Stores user in SessionManager and opens Dashboard
     * 4. On failure: Shows error message (account lockout after 5 attempts)
     */
    private void handleLogin() {
        // Extract and clean user input
        String username = userField.getText().trim();
        String password = new String(passField.getPassword());  // Convert char[] to String

        // Validate input - both fields required
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter both username and password.",
                    "Warning",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            // Attempt authentication - this checks BCrypt hash and lockout status
            User user = authService.login(username, password);
            
            if (user != null) {
                // Successful login - store user in session
                SessionManager.login(user);
                
                // Close login screen and open dashboard based on role
                dispose();
                new DashboardScreen().setVisible(true);
            } else {
                // This shouldn't happen as AuthService throws exceptions
                // but kept as safety fallback
                JOptionPane.showMessageDialog(this,
                        "Invalid username or password.",
                        "Login Failed",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            // Display any authentication errors (wrong password, locked account, etc.)
            JOptionPane.showMessageDialog(this,
                    "Error: " + ex.getMessage(),
                    "System Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}

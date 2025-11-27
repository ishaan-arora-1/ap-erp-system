package edu.univ.erp.ui;

import edu.univ.erp.auth.AuthService;
import edu.univ.erp.auth.SessionManager;
import edu.univ.erp.domain.User;

import javax.swing.*;
import java.awt.*;

public class LoginScreen extends JFrame {

    private final JTextField userField;
    private final JPasswordField passField;
    private final AuthService authService;

    public LoginScreen() {
        this.authService = new AuthService();

        setTitle("University ERP - Login");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        this.userField = new JTextField();
        this.passField = new JPasswordField();

        initComponents();
    }

    private void initComponents() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        JLabel titleLabel = new JLabel("Welcome Back");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        usernameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        passwordLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        userField.setMaximumSize(new Dimension(300, 30));
        userField.setAlignmentX(Component.CENTER_ALIGNMENT);

        passField.setMaximumSize(new Dimension(300, 30));
        passField.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton loginBtn = new JButton("Login");
        loginBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginBtn.setPreferredSize(new Dimension(120, 35));
        loginBtn.setMaximumSize(new Dimension(120, 35));
        loginBtn.putClientProperty("JButton.buttonType", "roundRect");
        loginBtn.addActionListener(e -> handleLogin());

        getRootPane().setDefaultButton(loginBtn);

        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(30));
        panel.add(usernameLabel);
        panel.add(Box.createVerticalStrut(5));
        panel.add(userField);
        panel.add(Box.createVerticalStrut(15));
        panel.add(passwordLabel);
        panel.add(Box.createVerticalStrut(5));
        panel.add(passField);
        panel.add(Box.createVerticalStrut(30));
        panel.add(loginBtn);

        add(panel);
    }

    private void handleLogin() {
        String username = userField.getText().trim();
        String password = new String(passField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter both username and password.",
                    "Warning",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            User user = authService.login(username, password);
            if (user != null) {
                SessionManager.login(user);
                dispose();
                new DashboardScreen().setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Invalid username or password.",
                        "Login Failed",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error: " + ex.getMessage(),
                    "System Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}

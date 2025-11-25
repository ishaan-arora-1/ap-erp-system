package edu.univ.erp.ui;

import edu.univ.erp.auth.AuthService; // Import this
import edu.univ.erp.auth.SessionManager;
import edu.univ.erp.domain.User;
import edu.univ.erp.domain.UserRole;

import javax.swing.*;
import java.awt.*;

public class DashboardScreen extends JFrame {

    private final AuthService authService = new AuthService(); // Add this service

    public DashboardScreen() {
        User user = SessionManager.getCurrentUser();
        if (user == null) {
            dispose();
            new LoginScreen().setVisible(true);
            return;
        }

        setTitle("University ERP - " + user.getRole() + ": " + user.getUsername());
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        header.add(new JLabel("Welcome, " + user.getUsername()), BorderLayout.WEST);

        // --- Header Buttons Panel ---
        JPanel headerBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        // 1. Change Password Button
        JButton passBtn = new JButton("Change Password");
        passBtn.addActionListener(e -> showChangePasswordDialog());
        headerBtns.add(passBtn);

        // 2. Logout Button
        JButton logoutBtn = new JButton("Logout");
        logoutBtn.addActionListener(e -> logout());
        headerBtns.add(logoutBtn);

        header.add(headerBtns, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        if (user.getRole() == UserRole.ADMIN) {
            add(new AdminPanel(), BorderLayout.CENTER);
        } else if (user.getRole() == UserRole.STUDENT) {
            add(new StudentPanel(), BorderLayout.CENTER);
        } else if (user.getRole() == UserRole.INSTRUCTOR) {
            add(new InstructorPanel(), BorderLayout.CENTER);
        } else {
            add(new JLabel("Unknown role", SwingConstants.CENTER), BorderLayout.CENTER);
        }
    }

    private void showChangePasswordDialog() {
        JPanel panel = new JPanel(new GridLayout(3, 2, 5, 5));
        JPasswordField oldPass = new JPasswordField();
        JPasswordField newPass = new JPasswordField();
        JPasswordField confirmPass = new JPasswordField();

        panel.add(new JLabel("Old Password:"));
        panel.add(oldPass);
        panel.add(new JLabel("New Password:"));
        panel.add(newPass);
        panel.add(new JLabel("Confirm New:"));
        panel.add(confirmPass);

        int result = JOptionPane.showConfirmDialog(this, panel, "Change Password", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String p1 = new String(newPass.getPassword());
            String p2 = new String(confirmPass.getPassword());
            String old = new String(oldPass.getPassword());

            if (!p1.equals(p2)) {
                JOptionPane.showMessageDialog(this, "New passwords do not match.");
                return;
            }

            if (p1.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Password cannot be empty.");
                return;
            }

            try {
                authService.changePassword(SessionManager.getCurrentUser().getUserId(), old, p1);
                JOptionPane.showMessageDialog(this, "Password Changed Successfully!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        }
    }

    private void logout() {
        SessionManager.logout();
        dispose();
        new LoginScreen().setVisible(true);
    }
}

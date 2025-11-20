package edu.univ.erp.ui;

import edu.univ.erp.auth.SessionManager;
import edu.univ.erp.domain.User;
import edu.univ.erp.domain.UserRole;

import javax.swing.*;
import java.awt.*;

public class DashboardScreen extends JFrame {

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

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.addActionListener(e -> logout());
        header.add(logoutBtn, BorderLayout.EAST);

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

    private void logout() {
        SessionManager.logout();
        dispose();
        new LoginScreen().setVisible(true);
    }
}

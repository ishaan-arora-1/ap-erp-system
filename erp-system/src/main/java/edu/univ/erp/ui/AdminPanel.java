package edu.univ.erp.ui;

import edu.univ.erp.domain.UserRole;
import edu.univ.erp.service.AdminService;

import javax.swing.*;
import java.awt.*;

public class AdminPanel extends JPanel {

    private final AdminService adminService;

    public AdminPanel() {
        this.adminService = new AdminService();
        setLayout(new BorderLayout());

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Manage Users", createUserPanel());
        tabs.addTab("Manage Courses", createCoursePanel());
        tabs.addTab("System Settings", createSettingsPanel());

        add(tabs, BorderLayout.CENTER);
    }

    private JPanel createUserPanel() {
        JPanel panel = new JPanel(new GridLayout(6, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));

        JTextField userField = new JTextField();
        JPasswordField passField = new JPasswordField();
        JComboBox<UserRole> roleBox = new JComboBox<>(new UserRole[]{UserRole.STUDENT, UserRole.INSTRUCTOR});
        JTextField nameField = new JTextField();
        JTextField extraField = new JTextField();

        JButton createBtn = new JButton("Create User");
        createBtn.addActionListener(e -> {
            try {
                adminService.registerUser(
                        userField.getText().trim(),
                        new String(passField.getPassword()),
                        (UserRole) roleBox.getSelectedItem(),
                        nameField.getText().trim(),
                        extraField.getText().trim()
                );
                JOptionPane.showMessageDialog(this, "User Created Successfully!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });

        panel.add(new JLabel("Username:"));
        panel.add(userField);
        panel.add(new JLabel("Password:"));
        panel.add(passField);
        panel.add(new JLabel("Role:"));
        panel.add(roleBox);
        panel.add(new JLabel("Full Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Roll No / Dept:"));
        panel.add(extraField);
        panel.add(new JLabel(""));
        panel.add(createBtn);

        return panel;
    }

    private JPanel createCoursePanel() {
        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));

        JTextField codeField = new JTextField();
        JTextField titleField = new JTextField();
        JTextField creditsField = new JTextField();
        JButton addBtn = new JButton("Add Course");

        addBtn.addActionListener(e -> {
            try {
                int credits = Integer.parseInt(creditsField.getText().trim());
                adminService.createCourse(
                        codeField.getText().trim(),
                        titleField.getText().trim(),
                        credits
                );
                JOptionPane.showMessageDialog(this, "Course Added!");
            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(this, "Credits must be a number.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });

        panel.add(new JLabel("Course Code (e.g. CS101):"));
        panel.add(codeField);
        panel.add(new JLabel("Course Title:"));
        panel.add(titleField);
        panel.add(new JLabel("Credits:"));
        panel.add(creditsField);
        panel.add(new JLabel(""));
        panel.add(addBtn);

        return panel;
    }

    private JPanel createSettingsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JCheckBox maintToggle = new JCheckBox("Enable Maintenance Mode");

        maintToggle.addActionListener(e -> {
            try {
                adminService.setMaintenanceMode(maintToggle.isSelected());
                String status = maintToggle.isSelected() ? "ON" : "OFF";
                JOptionPane.showMessageDialog(this, "Maintenance Mode is now " + status);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });

        panel.add(maintToggle);
        return panel;
    }
}

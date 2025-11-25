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
        tabs.addTab("Manage Sections", createSectionPanel()); // <--- ADD THIS LINE
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
        // Change layout to BoxLayout Y_AXIS to stack items vertically
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 1. Maintenance Mode Section
        JPanel maintPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JCheckBox maintToggle = new JCheckBox("Enable Maintenance Mode");
        maintToggle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        maintToggle.addActionListener(e -> {
            try {
                adminService.setMaintenanceMode(maintToggle.isSelected());
                String status = maintToggle.isSelected() ? "ON" : "OFF";
                JOptionPane.showMessageDialog(this, "Maintenance Mode is now " + status);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });
        maintPanel.add(maintToggle);
        
        // 2. Backup & Restore Section
        JPanel dbPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        dbPanel.setBorder(BorderFactory.createTitledBorder("Database Operations (Bonus)"));
        
        JButton backupBtn = new JButton("Backup Data");
        backupBtn.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setSelectedFile(new java.io.File("erp_backup.sql"));
            if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                try {
                    adminService.backupDB(fc.getSelectedFile().getAbsolutePath());
                    JOptionPane.showMessageDialog(this, "Backup Successful!");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
                }
            }
        });

        JButton restoreBtn = new JButton("Restore Data");
        restoreBtn.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                int confirm = JOptionPane.showConfirmDialog(this, 
                    "Restoring will OVERWRITE current data. Continue?", 
                    "Warning", JOptionPane.YES_NO_OPTION);
                
                if (confirm == JOptionPane.YES_OPTION) {
                    try {
                        adminService.restoreDB(fc.getSelectedFile().getAbsolutePath());
                        JOptionPane.showMessageDialog(this, "Restore Successful!");
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
                    }
                }
            }
        });

        dbPanel.add(backupBtn);
        dbPanel.add(Box.createHorizontalStrut(10));
        dbPanel.add(restoreBtn);

        // Add everything to main panel
        panel.add(maintPanel);
        panel.add(Box.createVerticalStrut(20));
        panel.add(dbPanel);
        panel.add(Box.createVerticalGlue()); // Push everything up

        return panel;
    }

    private JPanel createSectionPanel() {
        JPanel panel = new JPanel(new GridLayout(6, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));

        // Form Components
        JComboBox<String> courseBox = new JComboBox<>();
        JComboBox<String> instructorBox = new JComboBox<>();
        JTextField timeField = new JTextField(); // e.g. "Mon 10:00"
        JTextField roomField = new JTextField();
        JTextField capField = new JTextField();
        
        // Lists to hold IDs hidden from the UI
        java.util.List<String> courseCodes = new java.util.ArrayList<>();
        java.util.List<Integer> instructorIds = new java.util.ArrayList<>();

        JButton loadBtn = new JButton("Refresh Lists");
        loadBtn.addActionListener(e -> {
            try {
                courseBox.removeAllItems();
                courseCodes.clear();
                instructorBox.removeAllItems();
                instructorIds.clear();

                // Load Courses
                for (java.util.Map<String, String> c : adminService.getAllCourses()) {
                    courseBox.addItem(c.get("code") + ": " + c.get("title"));
                    courseCodes.add(c.get("code"));
                }

                // Load Instructors
                for (java.util.Map<String, String> i : adminService.getAllInstructors()) {
                    instructorBox.addItem(i.get("name"));
                    instructorIds.add(Integer.parseInt(i.get("id")));
                }

                JOptionPane.showMessageDialog(this, "Lists refreshed!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error loading lists: " + ex.getMessage());
            }
        });

        JButton createBtn = new JButton("Create Section");
        createBtn.addActionListener(e -> {
            try {
                int cIdx = courseBox.getSelectedIndex();
                int iIdx = instructorBox.getSelectedIndex();
                
                if (cIdx == -1 || iIdx == -1) {
                    JOptionPane.showMessageDialog(this, "Please select a course and instructor.");
                    return;
                }

                String code = courseCodes.get(cIdx);
                int instId = instructorIds.get(iIdx);
                int cap = Integer.parseInt(capField.getText().trim());

                adminService.createSection(code, instId, 
                        timeField.getText().trim(), 
                        roomField.getText().trim(), 
                        cap);
                        
                JOptionPane.showMessageDialog(this, "Section Created!");
            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(this, "Capacity must be a number.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });

        // Add to Panel
        panel.add(new JLabel("Load Data First:"));
        panel.add(loadBtn);
        
        panel.add(new JLabel("Course:"));
        panel.add(courseBox);
        
        panel.add(new JLabel("Instructor:"));
        panel.add(instructorBox);
        
        panel.add(new JLabel("Day/Time (e.g. Mon 9am):"));
        panel.add(timeField);
        
        panel.add(new JLabel("Room:"));
        panel.add(roomField);
        
        panel.add(new JLabel("Capacity:"));
        panel.add(capField);
        
        panel.add(new JLabel("")); // Spacer
        panel.add(createBtn);

        return panel;
    }
}

package edu.univ.erp.ui;

import com.formdev.flatlaf.FlatClientProperties;
import edu.univ.erp.domain.UserRole;
import edu.univ.erp.service.AdminService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.List;
import java.util.Map;

/**
 * AdminPanel - Modern Sidebar Layout
 *
 * Features:
 * - Left Sidebar Navigation (Dark Theme)
 * - CardLayout for content switching
 * - GridBagLayout for aligned forms
 * - Auto-refreshing dropdowns when panels are opened
 */
public class AdminPanel extends JPanel {

    private final AdminService adminService;
    private final CardLayout contentLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(contentLayout);
    
    // UI Colors
    private final Color SIDEBAR_BG = new Color(45, 48, 65); // Dark Slate
    private final Color SIDEBAR_HOVER = new Color(65, 68, 85);
    private final Color ACCENT_COLOR = new Color(100, 150, 255); // Soft Blue

    public AdminPanel() {
        this.adminService = new AdminService();
        setLayout(new BorderLayout());

        // 1. Create Sidebar
        JPanel sidebar = createSidebar();
        add(sidebar, BorderLayout.WEST);

        // 2. Create Content Area
        // We add specific panels to the CardLayout with unique string IDs
        contentPanel.add(createUserPanel(), "USERS");
        contentPanel.add(createCoursePanel(), "COURSES");
        contentPanel.add(createSectionPanel(), "SECTIONS");
        contentPanel.add(createSettingsPanel(), "SETTINGS");

        // Styling the content area
        contentPanel.setBackground(new Color(245, 247, 250)); // Light Gray Background
        contentPanel.setBorder(new EmptyBorder(20, 30, 20, 30));
        
        add(contentPanel, BorderLayout.CENTER);
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(SIDEBAR_BG);
        sidebar.setPreferredSize(new Dimension(220, 600));
        sidebar.setBorder(new EmptyBorder(20, 10, 20, 10));

        // Sidebar Title
        JLabel title = new JLabel("ADMIN MENU");
        title.setForeground(new Color(150, 155, 170));
        title.setFont(new Font("Segoe UI", Font.BOLD, 12));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        sidebar.add(title);
        sidebar.add(Box.createVerticalStrut(15));

        // Navigation Buttons
        sidebar.add(createNavButton("Manage Users", "USERS"));
        sidebar.add(Box.createVerticalStrut(5));
        sidebar.add(createNavButton("Manage Courses", "COURSES"));
        sidebar.add(Box.createVerticalStrut(5));
        sidebar.add(createNavButton("Manage Sections", "SECTIONS"));
        sidebar.add(Box.createVerticalStrut(5));
        sidebar.add(createNavButton("System Settings", "SETTINGS"));

        return sidebar;
    }

    private JButton createNavButton(String text, String cardName) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btn.setForeground(Color.WHITE);
        btn.setBackground(SIDEBAR_BG);
        btn.setBorder(new EmptyBorder(10, 15, 10, 15));
        btn.setFocusPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setMaximumSize(new Dimension(200, 40));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Hover Effect
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(SIDEBAR_HOVER);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(SIDEBAR_BG);
            }
        });

        // Action: Switch Card
        btn.addActionListener(e -> contentLayout.show(contentPanel, cardName));

        return btn;
    }

    // --- PANEL 1: MANAGE USERS ---
    private JPanel createUserPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.putClientProperty(FlatClientProperties.STYLE, "arc: 15"); // Rounded container
        panel.setBorder(new EmptyBorder(20, 40, 20, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);

        // Header
        JLabel header = new JLabel("Register New User");
        header.setFont(new Font("Segoe UI", Font.BOLD, 20));
        header.setForeground(new Color(50, 50, 50));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(header, gbc);

        // Inputs
        gbc.gridwidth = 1; gbc.gridy++;
        JTextField userField = createStyledField("Username");
        JPasswordField passField = new JPasswordField();
        passField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Password");
        passField.putClientProperty(FlatClientProperties.STYLE, "arc: 10; showRevealButton: true");

        JComboBox<UserRole> roleBox = new JComboBox<>(new UserRole[]{UserRole.STUDENT, UserRole.INSTRUCTOR});
        JTextField nameField = createStyledField("Full Name");
        JTextField extraField = createStyledField("Roll No (Student) or Dept (Instructor)");

        // Layout rows
        addFormRow(panel, gbc, "Username:", userField);
        addFormRow(panel, gbc, "Password:", passField);
        addFormRow(panel, gbc, "Role:", roleBox);
        addFormRow(panel, gbc, "Full Name:", nameField);
        addFormRow(panel, gbc, "Details:", extraField);

        // Action Button
        JButton createBtn = createPrimaryButton("Create User");
        createBtn.addActionListener(e -> {
            try {
                // Validate inputs
                String username = userField.getText().trim();
                String password = new String(passField.getPassword());
                String fullName = nameField.getText().trim();
                String extra = extraField.getText().trim();
                
                if (username.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Username cannot be empty.", "Invalid Input", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                if (password.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Password cannot be empty.", "Invalid Input", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                if (password.length() < 4) {
                    JOptionPane.showMessageDialog(this, "Password must be at least 4 characters long.", "Invalid Input", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                if (fullName.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Full name cannot be empty.", "Invalid Input", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                if (extra.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Please enter Roll Number for students or Department for instructors.", "Invalid Input", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                adminService.registerUser(username, password, (UserRole) roleBox.getSelectedItem(), fullName, extra);
                JOptionPane.showMessageDialog(this, "User account created successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                userField.setText("");
                passField.setText("");
                nameField.setText("");
                extraField.setText("");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Registration Failed", JOptionPane.ERROR_MESSAGE);
            }
        });

        gbc.gridx = 1; gbc.gridy++;
        gbc.anchor = GridBagConstraints.EAST;
        panel.add(createBtn, gbc);

        // Wrapper to center the form
        JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        wrapper.setOpaque(false);
        wrapper.add(panel);
        return wrapper;
    }

    // --- PANEL 2: MANAGE COURSES ---
    private JPanel createCoursePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.putClientProperty(FlatClientProperties.STYLE, "arc: 15");
        panel.setBorder(new EmptyBorder(20, 40, 20, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel header = new JLabel("Add New Course");
        header.setFont(new Font("Segoe UI", Font.BOLD, 20));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(header, gbc);

        gbc.gridwidth = 1; gbc.gridy++;
        JTextField codeField = createStyledField("e.g. CS101");
        JTextField titleField = createStyledField("e.g. Intro to Java");
        JTextField creditsField = createStyledField("e.g. 4");

        addFormRow(panel, gbc, "Course Code:", codeField);
        addFormRow(panel, gbc, "Course Title:", titleField);
        addFormRow(panel, gbc, "Credits:", creditsField);

        JButton addBtn = createPrimaryButton("Add Course");
        addBtn.addActionListener(e -> {
            try {
                // Validate inputs
                String courseCode = codeField.getText().trim();
                String courseTitle = titleField.getText().trim();
                String creditsStr = creditsField.getText().trim();
                
                if (courseCode.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Course code cannot be empty.", "Invalid Input", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                if (courseTitle.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Course title cannot be empty.", "Invalid Input", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                if (creditsStr.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Credits cannot be empty.", "Invalid Input", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                int credits;
                try {
                    credits = Integer.parseInt(creditsStr);
                } catch (NumberFormatException nfe) {
                    JOptionPane.showMessageDialog(this, "Please enter a valid number for credits.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                if (credits <= 0) {
                    JOptionPane.showMessageDialog(this, "Credits must be a positive number.", "Invalid Input", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                if (credits > 10) {
                    JOptionPane.showMessageDialog(this, "Credits cannot exceed 10.", "Invalid Input", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                adminService.createCourse(courseCode, courseTitle, credits);
                JOptionPane.showMessageDialog(this, "Course added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                codeField.setText("");
                titleField.setText("");
                creditsField.setText("");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Unable to Add Course", JOptionPane.ERROR_MESSAGE);
            }
        });

        gbc.gridx = 1; gbc.gridy++;
        gbc.anchor = GridBagConstraints.EAST;
        panel.add(addBtn, gbc);

        JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        wrapper.setOpaque(false);
        wrapper.add(panel);
        return wrapper;
    }

    // --- PANEL 3: MANAGE SECTIONS ---
    private JPanel createSectionPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.putClientProperty(FlatClientProperties.STYLE, "arc: 15");
        panel.setBorder(new EmptyBorder(20, 40, 20, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel header = new JLabel("Schedule New Section");
        header.setFont(new Font("Segoe UI", Font.BOLD, 20));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(header, gbc);

        gbc.gridwidth = 1; gbc.gridy++;
        
        JComboBox<String> courseBox = new JComboBox<>();
        JComboBox<String> instructorBox = new JComboBox<>();
        JTextField timeField = createStyledField("e.g. Mon/Wed 10:00");
        JTextField roomField = createStyledField("e.g. C-101");
        JTextField capField = createStyledField("e.g. 50");
        
        // Drop deadline date picker
        java.util.Date today = new java.util.Date();
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTime(today);
        cal.add(java.util.Calendar.MONTH, 3); // Default: 3 months from now
        java.util.Date defaultDeadline = cal.getTime();
        
        SpinnerDateModel dateModel = new SpinnerDateModel(defaultDeadline, today, null, java.util.Calendar.DAY_OF_MONTH);
        JSpinner deadlineSpinner = new JSpinner(dateModel);
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(deadlineSpinner, "yyyy-MM-dd");
        deadlineSpinner.setEditor(dateEditor);
        deadlineSpinner.setPreferredSize(new Dimension(250, 35));

        // Hidden lists for IDs
        java.util.List<String> courseCodes = new java.util.ArrayList<>();
        java.util.List<Integer> instructorIds = new java.util.ArrayList<>();

        // Auto-load data when this panel is shown
        panel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                loadDropdowns(courseBox, instructorBox, courseCodes, instructorIds);
            }
        });

        addFormRow(panel, gbc, "Select Course:", courseBox);
        addFormRow(panel, gbc, "Assign Instructor:", instructorBox);
        addFormRow(panel, gbc, "Days & Time:", timeField);
        addFormRow(panel, gbc, "Room:", roomField);
        addFormRow(panel, gbc, "Capacity:", capField);
        addFormRow(panel, gbc, "Drop Deadline:", deadlineSpinner);

        // Button panel for Refresh and Create
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);
        
        JButton refreshBtn = new JButton("Refresh Lists");
        refreshBtn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        refreshBtn.putClientProperty(FlatClientProperties.STYLE, "arc: 10");
        refreshBtn.addActionListener(e -> {
            loadDropdowns(courseBox, instructorBox, courseCodes, instructorIds);
            JOptionPane.showMessageDialog(this, "Course and instructor lists refreshed!", "Refreshed", JOptionPane.INFORMATION_MESSAGE);
        });
        
        JButton createBtn = createPrimaryButton("Create Section");
        createBtn.addActionListener(e -> {
            try {
                // Validate selections
                if (courseBox.getSelectedIndex() == -1) {
                    JOptionPane.showMessageDialog(this, "Please select a course.", "Invalid Input", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                if (instructorBox.getSelectedIndex() == -1) {
                    JOptionPane.showMessageDialog(this, "Please select an instructor.", "Invalid Input", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                // Validate text inputs
                String daysTime = timeField.getText().trim();
                String room = roomField.getText().trim();
                String capacityStr = capField.getText().trim();
                
                if (daysTime.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Days and time cannot be empty.", "Invalid Input", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                if (room.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Room cannot be empty.", "Invalid Input", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                if (capacityStr.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Capacity cannot be empty.", "Invalid Input", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                int capacity;
                try {
                    capacity = Integer.parseInt(capacityStr);
                } catch (NumberFormatException nfe) {
                    JOptionPane.showMessageDialog(this, "Please enter a valid number for capacity.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                if (capacity <= 0) {
                    JOptionPane.showMessageDialog(this, "Capacity must be a positive number.", "Invalid Input", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                if (capacity > 500) {
                    JOptionPane.showMessageDialog(this, "Capacity cannot exceed 500.", "Invalid Input", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                String code = courseCodes.get(courseBox.getSelectedIndex());
                int instId = instructorIds.get(instructorBox.getSelectedIndex());
                
                // Get deadline from spinner
                java.util.Date spinnerDate = (java.util.Date) deadlineSpinner.getValue();
                if (spinnerDate == null) {
                    JOptionPane.showMessageDialog(this, "Please select a drop deadline.", "Invalid Input", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                java.time.LocalDate deadline = new java.sql.Date(spinnerDate.getTime()).toLocalDate();
                
                // Validate that deadline is not in the past
                if (deadline.isBefore(java.time.LocalDate.now())) {
                    JOptionPane.showMessageDialog(this, "Drop deadline cannot be in the past.", "Invalid Input", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                adminService.createSection(code, instId, daysTime, room, capacity, deadline);
                
                JOptionPane.showMessageDialog(this, "Section created successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                timeField.setText("");
                roomField.setText("");
                capField.setText("");
                
                // Reset deadline to default (3 months from now)
                java.util.Calendar resetCal = java.util.Calendar.getInstance();
                resetCal.add(java.util.Calendar.MONTH, 3);
                deadlineSpinner.setValue(resetCal.getTime());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Unable to Create Section", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnPanel.add(refreshBtn);
        btnPanel.add(createBtn);
        
        gbc.gridx = 0; gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.EAST;
        panel.add(btnPanel, gbc);

        JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        wrapper.setOpaque(false);
        wrapper.add(panel);
        
        // Initial load
        loadDropdowns(courseBox, instructorBox, courseCodes, instructorIds);
        
        return wrapper;
    }

    // --- PANEL 4: SETTINGS ---
    private JPanel createSettingsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.putClientProperty(FlatClientProperties.STYLE, "arc: 15");
        panel.setBorder(new EmptyBorder(30, 50, 30, 50));

        // 1. Maintenance Toggle
        JLabel maintTitle = new JLabel("System Control");
        maintTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        
        JCheckBox maintToggle = new JCheckBox("Enable Maintenance Mode");
        maintToggle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        maintToggle.setFocusPainted(false);
        
        // Load initial state
        try {
            maintToggle.setSelected(adminService.isMaintenanceMode());
        } catch (Exception ignored) {}

        maintToggle.addActionListener(e -> {
            try {
                adminService.setMaintenanceMode(maintToggle.isSelected());
                String status = maintToggle.isSelected() 
                    ? "Maintenance mode is now ON. Users cannot make changes." 
                    : "Maintenance mode is now OFF. Normal operation resumed.";
                JOptionPane.showMessageDialog(this, status, "Maintenance Mode Updated", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Update Failed", JOptionPane.ERROR_MESSAGE);
            }
        });

        // 2. Database Controls
        JLabel dbTitle = new JLabel("Database Management");
        dbTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        
        JButton backupBtn = new JButton("Backup Database");
        backupBtn.setBackground(new Color(255, 200, 100)); // Orange
        backupBtn.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setSelectedFile(new java.io.File("erp_backup.sql"));
            if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                try {
                    adminService.backupDB(fc.getSelectedFile().getAbsolutePath());
                    JOptionPane.showMessageDialog(this, "Database backup completed successfully!", "Backup Complete", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Unable to backup database. Please check permissions and try again.", "Backup Failed", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        JButton restoreBtn = new JButton("Restore Database");
        restoreBtn.setBackground(new Color(255, 100, 100)); // Red
        restoreBtn.setForeground(Color.WHITE);
        restoreBtn.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                if (JOptionPane.showConfirmDialog(this, "This will overwrite all current data. Are you sure?", "Confirm Restore", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
                    try {
                        adminService.restoreDB(fc.getSelectedFile().getAbsolutePath());
                        JOptionPane.showMessageDialog(this, "Database restored successfully!", "Restore Complete", JOptionPane.INFORMATION_MESSAGE);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(this, "Unable to restore database. Please check the file and try again.", "Restore Failed", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        // Assembly
        panel.add(maintTitle);
        panel.add(Box.createVerticalStrut(10));
        panel.add(maintToggle);
        panel.add(Box.createVerticalStrut(40));
        panel.add(new JSeparator());
        panel.add(Box.createVerticalStrut(20));
        panel.add(dbTitle);
        panel.add(Box.createVerticalStrut(15));
        
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        btnPanel.setOpaque(false);
        btnPanel.add(backupBtn);
        btnPanel.add(Box.createHorizontalStrut(15));
        btnPanel.add(restoreBtn);
        panel.add(btnPanel);

        JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        wrapper.setOpaque(false);
        wrapper.add(panel);
        return wrapper;
    }

    // --- UTILITIES ---

    private void addFormRow(JPanel panel, GridBagConstraints gbc, String labelText, JComponent field) {
        gbc.gridx = 0;
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(new Color(100, 100, 100));
        panel.add(label, gbc);

        gbc.gridx = 1;
        field.setPreferredSize(new Dimension(250, 35));
        panel.add(field, gbc);
        
        gbc.gridy++;
    }

    private JTextField createStyledField(String placeholder) {
        JTextField field = new JTextField();
        field.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, placeholder);
        field.putClientProperty(FlatClientProperties.STYLE, "arc: 10; padding: 5,5,5,5");
        return field;
    }

    private JButton createPrimaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(ACCENT_COLOR);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.putClientProperty(FlatClientProperties.STYLE, "arc: 10; borderWidth: 0");
        return btn;
    }

    private void loadDropdowns(JComboBox<String> courseBox, JComboBox<String> instBox, 
                               List<String> codes, List<Integer> ids) {
        try {
            courseBox.removeAllItems();
            instBox.removeAllItems();
            codes.clear();
            ids.clear();

            for (Map<String, String> c : adminService.getAllCourses()) {
                courseBox.addItem(c.get("code") + ": " + c.get("title"));
                codes.add(c.get("code"));
            }

            for (Map<String, String> i : adminService.getAllInstructors()) {
                instBox.addItem(i.get("name"));
                ids.add(Integer.parseInt(i.get("id")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

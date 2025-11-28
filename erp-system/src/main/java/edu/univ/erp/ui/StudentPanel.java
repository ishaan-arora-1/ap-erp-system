package edu.univ.erp.ui;

import com.formdev.flatlaf.FlatClientProperties;
import edu.univ.erp.auth.SessionManager;
import edu.univ.erp.domain.User;
import edu.univ.erp.service.StudentService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

/**
 * StudentPanel - Modern Sidebar Layout
 *
 * Features:
 * - Left Sidebar Navigation
 * - Dedicated "Academic Record" tab for transcripts
 * - Modern tables with sorting
 */
public class StudentPanel extends JPanel {

    private final StudentService studentService;
    private final CardLayout contentLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(contentLayout);
    
    // UI Colors (Consistent with AdminPanel)
    private final Color SIDEBAR_BG = new Color(45, 48, 65);
    private final Color SIDEBAR_HOVER = new Color(65, 68, 85);
    private final Color ACCENT_COLOR = new Color(100, 150, 255);

    // Data Models
    private DefaultTableModel catalogModel;
    private DefaultTableModel myModel;
    private DefaultTableModel transcriptModel;

    public StudentPanel() {
        this.studentService = new StudentService();
        setLayout(new BorderLayout());

        // 1. Sidebar
        add(createSidebar(), BorderLayout.WEST);

        // 2. Content Area
        contentPanel.add(createCatalogPanel(), "CATALOG");
        contentPanel.add(createMySectionsPanel(), "MY_SECTIONS");
        contentPanel.add(createTranscriptPanel(), "TRANSCRIPT");
        
        contentPanel.setBackground(new Color(245, 247, 250));
        contentPanel.setBorder(new EmptyBorder(20, 30, 20, 30));
        add(contentPanel, BorderLayout.CENTER);

        // Initial Data Load
        refreshCatalog();
        refreshMyData();
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(SIDEBAR_BG);
        sidebar.setPreferredSize(new Dimension(220, 600));
        sidebar.setBorder(new EmptyBorder(20, 10, 20, 10));

        JLabel title = new JLabel("STUDENT PORTAL");
        title.setForeground(new Color(150, 155, 170));
        title.setFont(new Font("Segoe UI", Font.BOLD, 12));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        sidebar.add(title);
        sidebar.add(Box.createVerticalStrut(15));

        sidebar.add(createNavButton("Course Catalog", "CATALOG"));
        sidebar.add(Box.createVerticalStrut(5));
        sidebar.add(createNavButton("My Registrations", "MY_SECTIONS"));
        sidebar.add(Box.createVerticalStrut(5));
        sidebar.add(createNavButton("Academic Record", "TRANSCRIPT"));

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
        
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) { btn.setBackground(SIDEBAR_HOVER); }
            public void mouseExited(java.awt.event.MouseEvent evt) { btn.setBackground(SIDEBAR_BG); }
        });

        btn.addActionListener(e -> {
            contentLayout.show(contentPanel, cardName);
            if (cardName.equals("CATALOG")) refreshCatalog();
            if (cardName.equals("MY_SECTIONS")) refreshMyData();
            if (cardName.equals("TRANSCRIPT")) refreshTranscript();
        });
        return btn;
    }

    // --- PANEL 1: CATALOG ---
    private JPanel createCatalogPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.putClientProperty(FlatClientProperties.STYLE, "arc: 15");
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel header = new JLabel("Available Courses");
        header.setFont(new Font("Segoe UI", Font.BOLD, 18));
        header.setBorder(new EmptyBorder(0, 0, 15, 0));
        panel.add(header, BorderLayout.NORTH);

        String[] cols = {"ID", "Code", "Title", "Credits", "Instructor", "Schedule", "Seats"};
        catalogModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
            
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                // ID (0) and Credits (3) are numeric
                // Seats (6) is String format "X / Y"
                if (columnIndex == 0 || columnIndex == 3) {
                    return Integer.class;
                }
                return String.class;
            }
        };
        JTable table = new JTable(catalogModel);
        table.setAutoCreateRowSorter(true);
        table.setRowHeight(30);
        
        // Enhanced header styling for easier sorting
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setPreferredSize(new Dimension(0, 40)); // Taller header for easier clicking
        table.getTableHeader().setReorderingAllowed(false); // Prevent accidental column dragging
        
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JButton regBtn = createPrimaryButton("Register Selected");
        regBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Please select a course section first.");
                return;
            }
            // Convert view index to model index (handling sorts)
            int modelRow = table.convertRowIndexToModel(row);
            String secId = String.valueOf(catalogModel.getValueAt(modelRow, 0));

            try {
                studentService.register(getCurrentUser(), Integer.parseInt(secId));
                JOptionPane.showMessageDialog(this, "Registration successful! The course has been added to your schedule.", "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshCatalog();
                refreshMyData(); // Update the other tabs silently
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Registration Failed", JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setOpaque(false);
        btnPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
        btnPanel.add(regBtn);
        panel.add(btnPanel, BorderLayout.SOUTH);

        return panel;
    }

    // --- PANEL 2: MY REGISTRATIONS ---
    private JPanel createMySectionsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.putClientProperty(FlatClientProperties.STYLE, "arc: 15");
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel header = new JLabel("My Schedule");
        header.setFont(new Font("Segoe UI", Font.BOLD, 18));
        header.setBorder(new EmptyBorder(0, 0, 15, 0));
        panel.add(header, BorderLayout.NORTH);

        String[] cols = {"ID", "Course", "Time/Room", "Instructor", "Drop Deadline"};
        myModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
            
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                // ID (0) is numeric
                if (columnIndex == 0) {
                    return Integer.class;
                }
                return String.class;
            }
        };
        JTable table = new JTable(myModel);
        table.setAutoCreateRowSorter(true);
        table.setRowHeight(30);
        
        // Enhanced header styling for easier sorting
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setPreferredSize(new Dimension(0, 40)); // Taller header for easier clicking
        table.getTableHeader().setReorderingAllowed(false); // Prevent accidental column dragging
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setOpaque(false);
        btnPanel.setBorder(new EmptyBorder(10, 0, 0, 0));

        JButton gradesBtn = new JButton("Check Grades");
        gradesBtn.putClientProperty(FlatClientProperties.STYLE, "arc: 10");
        gradesBtn.addActionListener(e -> showGradePopup(table));

        JButton dropBtn = new JButton("Drop Section");
        dropBtn.setBackground(new Color(255, 100, 100));
        dropBtn.setForeground(Color.WHITE);
        dropBtn.putClientProperty(FlatClientProperties.STYLE, "arc: 10; borderWidth: 0");
        dropBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) return;
            int modelRow = table.convertRowIndexToModel(row);
            String secId = String.valueOf(myModel.getValueAt(modelRow, 0));
            
            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to drop this course?", "Confirm Drop", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    studentService.drop(getCurrentUser(), Integer.parseInt(secId));
                    refreshMyData();
                    refreshCatalog();
                    JOptionPane.showMessageDialog(this, "Course dropped successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                } catch(Exception ex) { 
                    JOptionPane.showMessageDialog(this, ex.getMessage(), "Unable to Drop", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        btnPanel.add(gradesBtn);
        btnPanel.add(Box.createHorizontalStrut(10));
        btnPanel.add(dropBtn);
        panel.add(btnPanel, BorderLayout.SOUTH);

        return panel;
    }

    // --- PANEL 3: ACADEMIC RECORD (TRANSCRIPT) ---
    private JPanel createTranscriptPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.putClientProperty(FlatClientProperties.STYLE, "arc: 15");
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel header = new JLabel("Academic Record");
        header.setFont(new Font("Segoe UI", Font.BOLD, 18));
        header.setBorder(new EmptyBorder(0, 0, 15, 0));
        panel.add(header, BorderLayout.NORTH);

        String[] cols = {"Course Code", "Course Title", "Final Grade"};
        transcriptModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
            
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                // Final Grade (2) is numeric
                if (columnIndex == 2) {
                    return Double.class;
                }
                return String.class;
            }
        };
        JTable table = new JTable(transcriptModel);
        table.setRowHeight(30);
        table.setEnabled(false); // Read only view
        
        // Enhanced header styling
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setPreferredSize(new Dimension(0, 40)); // Taller header
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JButton exportBtn = createPrimaryButton("Download Transcript (CSV)");
        exportBtn.addActionListener(e -> exportTranscript());
        
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setOpaque(false);
        btnPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
        btnPanel.add(exportBtn);
        panel.add(btnPanel, BorderLayout.SOUTH);

        return panel;
    }

    // --- HELPERS ---

    private void showGradePopup(JTable table) {
        int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Select a section to view grades.");
                return;
            }
        int modelRow = table.convertRowIndexToModel(row);
        String secId = String.valueOf(myModel.getValueAt(modelRow, 0));
        String courseTitle = String.valueOf(myModel.getValueAt(modelRow, 1));
        
            try {
                Map<String, Double> grades = studentService.getGrades(getCurrentUser(), Integer.parseInt(secId));
                double quiz = grades.getOrDefault("Quiz", 0.0);
                double mid = grades.getOrDefault("Midterm", 0.0);
                double end = grades.getOrDefault("EndSem", 0.0);
                double finalGrade = (quiz * 0.2) + (mid * 0.3) + (end * 0.5);
            
            // Nice HTML formatted message
            String message = String.format("<html><h3>%s</h3>" +
                    "<ul>" +
                    "<li><b>Quiz:</b> %.2f</li>" +
                    "<li><b>Midterm:</b> %.2f</li>" +
                    "<li><b>EndSem:</b> %.2f</li>" +
                    "</ul>" +
                    "<hr><b>FINAL GRADE: %.2f</b></html>", 
                        courseTitle, quiz, mid, end, finalGrade);
            
            JOptionPane.showMessageDialog(this, message, "Grade Report", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
    }

    private void exportTranscript() {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Save Transcript");
            fileChooser.setSelectedFile(new File("transcript.csv"));
            
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (PrintWriter writer = new PrintWriter(fileChooser.getSelectedFile())) {
                    List<Map<String, String>> data = studentService.getTranscriptData(getCurrentUser());
                    writer.println("Course Code,Course Title,Final Grade");
                    for (Map<String, String> rowData : data) {
                        writer.println(String.format("%s,%s,%s", 
                            rowData.get("code"), rowData.get("title"), rowData.get("grade")));
                    }
                JOptionPane.showMessageDialog(this, "Your transcript has been saved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Unable to save transcript file. Please check permissions and try again.", "Export Failed", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void refreshCatalog() {
        if (catalogModel == null) return;
        catalogModel.setRowCount(0);
        try {
            List<Map<String, String>> rows = studentService.getCourseCatalog();
            for (Map<String, String> r : rows) {
                try {
                catalogModel.addRow(new Object[]{
                            Integer.parseInt(r.get("id")), // ID as Integer
                            r.get("code"), 
                            r.get("title"), 
                            Integer.parseInt(r.get("credits")), // Credits as Integer
                            r.get("instructor"), 
                            r.get("time"), 
                            r.get("seats") // Seats as String (formatted as "X / Y")
                });
                } catch (NumberFormatException nfe) {
                    // If parsing fails, skip this row
                    System.err.println("Error parsing row: " + nfe.getMessage());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void refreshMyData() {
        if (myModel == null) return;
        myModel.setRowCount(0);
        try {
            List<Map<String, String>> rows = studentService.getMySections(getCurrentUser());
            for (Map<String, String> r : rows) {
                try {
                    myModel.addRow(new Object[]{
                            Integer.parseInt(r.get("id")), // ID as Integer
                            r.get("display"), 
                            r.get("info"), 
                            r.get("instructor"),
                            r.get("deadline") // Drop deadline
                    });
                } catch (NumberFormatException nfe) {
                    // If parsing fails, skip this row
                    System.err.println("Error parsing row: " + nfe.getMessage());
                }
            }
        } catch (Exception e) { 
            e.printStackTrace(); 
        }
    }

    private void refreshTranscript() {
        if (transcriptModel == null) return;
        transcriptModel.setRowCount(0);
        try {
            List<Map<String, String>> data = studentService.getTranscriptData(getCurrentUser());
            for (Map<String, String> row : data) {
                // Parse grade as Double for proper numeric sorting
                Double grade = null;
                try {
                    grade = Double.parseDouble(row.get("grade"));
                } catch (NumberFormatException e) {
                    // If parsing fails, keep as 0.0 or handle as needed
                    grade = 0.0;
                }
                transcriptModel.addRow(new Object[]{
                    row.get("code"), row.get("title"), grade
                });
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private JButton createPrimaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(ACCENT_COLOR);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.putClientProperty(FlatClientProperties.STYLE, "arc: 10; borderWidth: 0");
        return btn;
    }

    private User getCurrentUser() {
        return SessionManager.getCurrentUser();
    }
}

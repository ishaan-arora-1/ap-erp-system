package edu.univ.erp.ui;

import com.formdev.flatlaf.FlatClientProperties;
import edu.univ.erp.auth.SessionManager;
import edu.univ.erp.service.InstructorService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * InstructorPanel - Modern Sidebar Layout
 * 
 * Features:
 * - Dedicated Gradebook view with integrated stats
 * - "Bulk Tools" tab for CSV import
 * - Auto-calc averages and validations intact
 */
public class InstructorPanel extends JPanel {

    private final InstructorService instructorService;
    private final CardLayout contentLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(contentLayout);

    // UI Colors
    private final Color SIDEBAR_BG = new Color(45, 48, 65);
    private final Color SIDEBAR_HOVER = new Color(65, 68, 85);
    private final Color ACCENT_COLOR = new Color(100, 150, 255);

    // Gradebook Components
    private JComboBox<String> sectionSelector;
    private JTable gradeTable;
    private DefaultTableModel tableModel;
    private List<String> sectionIds = new ArrayList<>();
    private JLabel statsLabel;

    public InstructorPanel() {
        this.instructorService = new InstructorService();
        setLayout(new BorderLayout());

        // 1. Sidebar
        add(createSidebar(), BorderLayout.WEST);

        // 2. Content
        contentPanel.add(createGradebookPanel(), "GRADEBOOK");
        contentPanel.add(createToolsPanel(), "TOOLS");

        contentPanel.setBackground(new Color(245, 247, 250));
        contentPanel.setBorder(new EmptyBorder(20, 30, 20, 30));
        add(contentPanel, BorderLayout.CENTER);

        // Load data initially
        loadSections();
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(SIDEBAR_BG);
        sidebar.setPreferredSize(new Dimension(220, 600));
        sidebar.setBorder(new EmptyBorder(20, 10, 20, 10));

        JLabel title = new JLabel("INSTRUCTOR PORTAL");
        title.setForeground(new Color(150, 155, 170));
        title.setFont(new Font("Segoe UI", Font.BOLD, 12));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        sidebar.add(title);
        sidebar.add(Box.createVerticalStrut(15));

        sidebar.add(createNavButton("Gradebook", "GRADEBOOK"));
        sidebar.add(Box.createVerticalStrut(5));
        sidebar.add(createNavButton("Bulk Tools", "TOOLS"));

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
            if (cardName.equals("GRADEBOOK")) loadSections();
        });
        return btn;
    }

    // --- PANEL 1: GRADEBOOK ---
    private JPanel createGradebookPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.putClientProperty(FlatClientProperties.STYLE, "arc: 15");
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Top Control Bar
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setOpaque(false);
        topPanel.add(new JLabel("Select Section:"));

        sectionSelector = new JComboBox<>();
        sectionSelector.setPreferredSize(new Dimension(250, 30));
        sectionSelector.addActionListener(e -> loadStudentList());
        topPanel.add(sectionSelector);

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.putClientProperty(FlatClientProperties.STYLE, "arc: 10");
        refreshBtn.addActionListener(e -> loadSections());
        topPanel.add(refreshBtn);

        panel.add(topPanel, BorderLayout.NORTH);

        // Grade Table
        String[] cols = {"EnrollID", "Student Name", "Roll No", "Quiz (20)", "Midterm (30)", "EndSem (50)", "Final Grade"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return col >= 3 && col <= 5; // Only grades are editable
            }
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                // EnrollID (0) is Integer, Quiz/Midterm/EndSem (3-5) are Double, Final Grade (6) is Double
                if (columnIndex == 0) {
                    return Integer.class;
                }
                if (columnIndex >= 3 && columnIndex <= 6) {
                    return Double.class;
                }
                return String.class;
            }
        };
        
        gradeTable = new JTable(tableModel);
        gradeTable.setRowHeight(30);
        gradeTable.setAutoCreateRowSorter(true);
        
        // Enhanced header styling for easier sorting
        gradeTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        gradeTable.getTableHeader().setPreferredSize(new Dimension(0, 40)); // Taller header for easier clicking
        gradeTable.getTableHeader().setReorderingAllowed(false); // Prevent accidental column dragging
        panel.add(new JScrollPane(gradeTable), BorderLayout.CENTER);

        // Bottom Stats & Save
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(new EmptyBorder(15, 0, 0, 0));

        statsLabel = new JLabel("Class Average: N/A");
        statsLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        statsLabel.setForeground(new Color(80, 80, 80));

        JButton saveBtn = createPrimaryButton("Save All Grades");
        saveBtn.addActionListener(e -> saveGrades());

        bottomPanel.add(statsLabel, BorderLayout.WEST);
        bottomPanel.add(saveBtn, BorderLayout.EAST);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        return panel;
    }

    // --- PANEL 2: BULK TOOLS ---
    private JPanel createToolsPanel() {
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setOpaque(false); // Transparent to show parent gray bg

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.putClientProperty(FlatClientProperties.STYLE, "arc: 15");
        panel.setBorder(new EmptyBorder(40, 40, 40, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0; gbc.insets = new Insets(10, 0, 10, 0);

        JLabel title = new JLabel("Bulk Grade Import");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        panel.add(title, gbc);

        gbc.gridy++;
        JTextArea desc = new JTextArea(
            "Upload a CSV file to bulk import grades.\n" +
            "Expected format: EnrollmentID, Quiz, Midterm, EndSem\n" +
            "This will overwrite existing grades for matching IDs."
        );
        desc.setEditable(false);
        desc.setOpaque(false);
        desc.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        panel.add(desc, gbc);

        gbc.gridy++;
        JButton importBtn = createPrimaryButton("Select CSV File");
        importBtn.setPreferredSize(new Dimension(200, 40));
        importBtn.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                try {
                    instructorService.importGradesFromCSV(fc.getSelectedFile());
                    JOptionPane.showMessageDialog(this, "Grades imported successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadStudentList(); // Refresh table data if we switch back
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, ex.getMessage(), "Import Failed", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        panel.add(importBtn, gbc);

        wrapper.add(panel);
        return wrapper;
    }

    // --- LOGIC ---

    private void loadSections() {
        sectionSelector.removeAllItems();
        sectionIds.clear();
        try {
            List<Map<String, String>> sections = instructorService.getMySections(SessionManager.getCurrentUser());
            for (Map<String, String> s : sections) {
                sectionSelector.addItem(s.get("display"));
                sectionIds.add(s.get("id"));
            }
            if (sectionSelector.getItemCount() > 0) {
                sectionSelector.setSelectedIndex(0);
            } else {
                tableModel.setRowCount(0);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadStudentList() {
        int index = sectionSelector.getSelectedIndex();
        if (index < 0 || index >= sectionIds.size()) {
            tableModel.setRowCount(0);
            statsLabel.setText("Class Average: N/A");
            return;
        }

        String secId = sectionIds.get(index);
        tableModel.setRowCount(0);

        try {
            List<Map<String, Object>> students = instructorService.getClassList(Integer.parseInt(secId), SessionManager.getCurrentUser());
            double totalFinal = 0.0;
            int count = 0;

            for (Map<String, Object> s : students) {
                double quiz = s.containsKey("Quiz") ? (Double) s.get("Quiz") : 0.0;
                double mid = s.containsKey("Midterm") ? (Double) s.get("Midterm") : 0.0;
                double end = s.containsKey("EndSem") ? (Double) s.get("EndSem") : 0.0;
                double finalGrade = (quiz * 0.2) + (mid * 0.3) + (end * 0.5);

                totalFinal += finalGrade;
                count++;

                tableModel.addRow(new Object[]{
                        Integer.parseInt(s.get("enrollment_id").toString()), // EnrollID as Integer
                        s.get("name"),
                        s.get("roll"),
                        quiz,
                        mid,
                        end,
                        finalGrade // Final Grade as Double for proper numeric sorting
                });
            }
            
            if (count > 0) statsLabel.setText(String.format("Class Average: %.2f", totalFinal / count));
            else statsLabel.setText("Class Average: 0.00");

        } catch (Exception e) { e.printStackTrace(); }
    }

    private void saveGrades() {
        if (gradeTable.isEditing()) gradeTable.getCellEditor().stopCellEditing();

        try {
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                int enrollId = Integer.parseInt(tableModel.getValueAt(i, 0).toString());
                double quiz = Double.parseDouble(tableModel.getValueAt(i, 3).toString());
                double midterm = Double.parseDouble(tableModel.getValueAt(i, 4).toString());
                double endsem = Double.parseDouble(tableModel.getValueAt(i, 5).toString());

                if (quiz < 0 || midterm < 0 || endsem < 0) throw new Exception("Scores cannot be negative.");
                if (quiz > 20 || midterm > 30 || endsem > 50) throw new Exception("Score exceeds limits (20/30/50).");

                instructorService.saveGrade(enrollId, "Quiz", quiz);
                instructorService.saveGrade(enrollId, "Midterm", midterm);
                instructorService.saveGrade(enrollId, "EndSem", endsem);
            }
            JOptionPane.showMessageDialog(this, "All grades have been saved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            loadStudentList();
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, "Please enter valid numbers in all grade fields.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Save Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JButton createPrimaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(ACCENT_COLOR);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.putClientProperty(FlatClientProperties.STYLE, "arc: 10; borderWidth: 0");
        return btn;
    }
}

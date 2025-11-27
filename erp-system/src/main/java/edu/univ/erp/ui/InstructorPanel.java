package edu.univ.erp.ui;

import edu.univ.erp.auth.SessionManager;
import edu.univ.erp.service.InstructorService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * InstructorPanel - UI for instructors to manage their sections and grades
 * 
 * Features:
 * - View all assigned sections in dropdown
 * - Display class list for selected section
 * - Edit grades directly in table (Quiz, Midterm, EndSem)
 * - Automatic final grade calculation (20% + 30% + 50%)
 * - Real-time class average calculation
 * - CSV import for bulk grade entry
 * - Input validation (negative values, max score limits)
 * - Sortable table columns for easy data viewing
 * 
 * Layout:
 * - NORTH: Section selector dropdown + Refresh button
 * - CENTER: Editable grade table
 * - SOUTH: Stats label + Import CSV + Save Grades buttons
 */
public class InstructorPanel extends JPanel {

    // Service layer for business logic
    private final InstructorService instructorService;
    
    // UI Components
    private final JComboBox<String> sectionSelector;  // Dropdown to choose section
    private final JTable gradeTable;                  // Editable table with student grades
    private final DefaultTableModel tableModel;       // Table data model
    private final List<String> sectionIds = new ArrayList<>();  // Hidden section IDs (parallel to dropdown display)
    private final JLabel statsLabel;                  // Shows class average

    public InstructorPanel() {
        this.instructorService = new InstructorService();
        setLayout(new BorderLayout());

        // Top panel: Section selector and refresh button
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Select Section:"));

        // Dropdown to select which section to view/grade
        sectionSelector = new JComboBox<>();
        sectionSelector.addActionListener(e -> loadStudentList());  // Load students when selection changes
        topPanel.add(sectionSelector);

        // Manual refresh button to reload section list
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> loadSections());
        topPanel.add(refreshBtn);

        add(topPanel, BorderLayout.NORTH);

        // Grade table setup with 7 columns
        String[] cols = {"EnrollID", "Student Name", "Roll No", "Quiz (20)", "Midterm (30)", "EndSem (50)", "Final Grade"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                // Only grade columns (3, 4, 5) are editable
                // EnrollID, name, roll, and final grade are read-only
                return col >= 3 && col <= 5;
            }
            
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                // Set data type for proper rendering and sorting
                if (columnIndex >= 3 && columnIndex <= 5) {
                    return Double.class;  // Grade columns are numeric
                }
                return String.class;
            }
        };
        
        gradeTable = new JTable(tableModel);
        gradeTable.setAutoCreateRowSorter(true);  // Enable column sorting by clicking header
        add(new JScrollPane(gradeTable), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        
        JButton importBtn = new JButton("Import CSV");
        importBtn.setBackground(new Color(100, 150, 255));
        importBtn.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                try {
                    instructorService.importGradesFromCSV(fc.getSelectedFile());
                    JOptionPane.showMessageDialog(this, "Grades Imported Successfully!");
                    loadStudentList();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
                }
            }
        });
        
        JButton saveBtn = new JButton("Save Grades");
        saveBtn.setBackground(new Color(100, 200, 100));
        saveBtn.addActionListener(e -> saveGrades());

        statsLabel = new JLabel("Class Average: N/A");
        statsLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        statsLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 20));

        bottomPanel.add(new JLabel("<html><i>Edit cells and click Save. OR Import CSV.</i></html>"));
        bottomPanel.add(statsLabel);
        bottomPanel.add(importBtn);
        bottomPanel.add(saveBtn);
        
        add(bottomPanel, BorderLayout.SOUTH);
        
        loadSections();
    }

    /**
     * Load instructor's assigned sections into dropdown
     * 
     * Fetches all sections assigned to the current instructor from database
     * Populates both the visible dropdown (course code + time) 
     * and hidden sectionIds list (for database queries)
     */
    private void loadSections() {
        sectionSelector.removeAllItems();
        sectionIds.clear();
        
        try {
            // Get sections assigned to this instructor
            List<Map<String, String>> sections = instructorService.getMySections(SessionManager.getCurrentUser());
            
            // Populate dropdown and parallel ID list
            for (Map<String, String> s : sections) {
                sectionSelector.addItem(s.get("display"));  // e.g., "CS101 (Mon/Wed 10:00)"
                sectionIds.add(s.get("id"));                // e.g., "1"
            }
            
            // Auto-select first section if any exist
            if (sectionSelector.getItemCount() > 0) {
                sectionSelector.setSelectedIndex(0);  // This triggers loadStudentList()
            } else {
                // No sections assigned - clear table
                tableModel.setRowCount(0);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to load sections: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
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
            List<Map<String, Object>> students =
                    instructorService.getClassList(Integer.parseInt(secId), SessionManager.getCurrentUser());
            
            double totalFinal = 0.0;
            int studentCount = 0;

            for (Map<String, Object> s : students) {
                double quiz = s.containsKey("Quiz") ? (Double) s.get("Quiz") : 0.0;
                double mid = s.containsKey("Midterm") ? (Double) s.get("Midterm") : 0.0;
                double end = s.containsKey("EndSem") ? (Double) s.get("EndSem") : 0.0;
                double finalGrade = (quiz * 0.2) + (mid * 0.3) + (end * 0.5);

                // Accumulate sum
                totalFinal += finalGrade;
                studentCount++;

                tableModel.addRow(new Object[]{
                        s.get("enrollment_id"),
                        s.get("name"),
                        s.get("roll"),
                        quiz,
                        mid,
                        end,
                        String.format("%.2f", finalGrade)
                });
            }

            // --- CHANGE: Update the stats label
            if (studentCount > 0) {
                double avg = totalFinal / studentCount;
                statsLabel.setText(String.format("Class Average: %.2f", avg));
            } else {
                statsLabel.setText("Class Average: 0.00");
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading students: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Save all grades in the table to database
     * 
     * Process:
     * 1. Stop any active cell editing
     * 2. Iterate through all rows in table
     * 3. Validate grades (non-negative, within max limits)
     * 4. Save each grade component to database
     * 5. Reload table to show updated final grades and averages
     * 
     * Validation Rules:
     * - No negative scores
     * - Quiz max: 20 points
     * - Midterm max: 30 points
     * - EndSem max: 50 points
     */
    private void saveGrades() {
        // If user is currently editing a cell, commit the edit
        if (gradeTable.isEditing()) {
            gradeTable.getCellEditor().stopCellEditing();
        }

        int rowCount = tableModel.getRowCount();
        try {
            // Save grades for each student
            for (int i = 0; i < rowCount; i++) {
                // Extract data from table row
                int enrollId = Integer.parseInt(tableModel.getValueAt(i, 0).toString());
                double quiz = Double.parseDouble(tableModel.getValueAt(i, 3).toString());
                double midterm = Double.parseDouble(tableModel.getValueAt(i, 4).toString());
                double endsem = Double.parseDouble(tableModel.getValueAt(i, 5).toString());

                // Validate: no negative scores
                if (quiz < 0 || midterm < 0 || endsem < 0) {
                    throw new Exception("Scores cannot be negative.");
                }
                
                // Validate: scores within maximum limits
                if (quiz > 20 || midterm > 30 || endsem > 50) {
                    throw new Exception("Score exceeds maximum for that component (Quiz: 20, Midterm: 30, EndSem: 50).");
                }

                // Save each grade component separately
                // This allows partial grades (e.g., only Quiz entered so far)
                instructorService.saveGrade(enrollId, "Quiz", quiz);
                instructorService.saveGrade(enrollId, "Midterm", midterm);
                instructorService.saveGrade(enrollId, "EndSem", endsem);
            }
            
            JOptionPane.showMessageDialog(this, "Grades Saved Successfully!");
            // Reload to recalculate final grades and class average
            loadStudentList();
        } catch (NumberFormatException nfe) {
            // User entered non-numeric value
            JOptionPane.showMessageDialog(this, "Please enter valid numeric scores.",
                    "Input Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            // Validation error or database error
            JOptionPane.showMessageDialog(this, "Error saving grades: " + e.getMessage(),
                    "Save Failed", JOptionPane.ERROR_MESSAGE);
        }
    }
}

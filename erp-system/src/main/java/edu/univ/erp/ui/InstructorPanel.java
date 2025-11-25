package edu.univ.erp.ui;

import edu.univ.erp.auth.SessionManager;
import edu.univ.erp.service.InstructorService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class InstructorPanel extends JPanel {

    private final InstructorService instructorService;
    private final JComboBox<String> sectionSelector;
    private final JTable gradeTable;
    private final DefaultTableModel tableModel;
    private final List<String> sectionIds = new ArrayList<>();
    private final JLabel statsLabel; // --- CHANGE: Field for stats

    public InstructorPanel() {
        this.instructorService = new InstructorService();
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Select Section:"));

        sectionSelector = new JComboBox<>();
        sectionSelector.addActionListener(e -> loadStudentList());
        topPanel.add(sectionSelector);

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> loadSections());
        topPanel.add(refreshBtn);

        add(topPanel, BorderLayout.NORTH);

        String[] cols = {"EnrollID", "Student Name", "Roll No", "Quiz (20)", "Midterm (30)", "EndSem (50)", "Final Grade"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return col >= 3 && col <= 5;
            }
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex >= 3 && columnIndex <= 5) {
                    return Double.class;
                }
                return String.class;
            }
        };
        gradeTable = new JTable(tableModel);
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

        // --- CHANGE: Add stats label to bottom panel
        statsLabel = new JLabel("Class Average: N/A");
        statsLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        statsLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 20));

        bottomPanel.add(new JLabel("<html><i>Edit cells and click Save. OR Import CSV.</i></html>"));
        bottomPanel.add(statsLabel); // Add label here
        bottomPanel.add(importBtn);
        bottomPanel.add(saveBtn);
        
        add(bottomPanel, BorderLayout.SOUTH);
        
        loadSections();
    }

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
            
            // --- CHANGE: Variables for average calculation
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

    private void saveGrades() {
        if (gradeTable.isEditing()) {
            gradeTable.getCellEditor().stopCellEditing();
        }

        int rowCount = tableModel.getRowCount();
        try {
            for (int i = 0; i < rowCount; i++) {
                int enrollId = Integer.parseInt(tableModel.getValueAt(i, 0).toString());
                double quiz = Double.parseDouble(tableModel.getValueAt(i, 3).toString());
                double midterm = Double.parseDouble(tableModel.getValueAt(i, 4).toString());
                double endsem = Double.parseDouble(tableModel.getValueAt(i, 5).toString());

                instructorService.saveGrade(enrollId, "Quiz", quiz);
                instructorService.saveGrade(enrollId, "Midterm", midterm);
                instructorService.saveGrade(enrollId, "EndSem", endsem);
            }
            JOptionPane.showMessageDialog(this, "Grades Saved Successfully!");
            loadStudentList();
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, "Please enter valid numeric scores.",
                    "Input Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error saving grades: " + e.getMessage(),
                    "Save Failed", JOptionPane.ERROR_MESSAGE);
        }
    }
}

package edu.univ.erp.ui;

import edu.univ.erp.auth.SessionManager;
import edu.univ.erp.domain.User;
import edu.univ.erp.service.StudentService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class StudentPanel extends JPanel {

    private final StudentService studentService;
    private JTable catalogTable;
    private DefaultTableModel catalogModel;
    private JTable myTable;
    private DefaultTableModel myModel;

    public StudentPanel() {
        this.studentService = new StudentService();
        setLayout(new BorderLayout());

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Course Catalog", createCatalogPanel());
        tabs.addTab("My Registrations", createMyDataPanel());
        tabs.addChangeListener(e -> {
            refreshCatalog();
            refreshMyData();
        });

        add(tabs, BorderLayout.CENTER);
        refreshCatalog();
        refreshMyData();
    }

    private JPanel createCatalogPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        String[] cols = {"ID", "Code", "Title", "Credits", "Instructor", "Schedule", "Seats"};
        catalogModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        catalogTable = new JTable(catalogModel);
        panel.add(new JScrollPane(catalogTable), BorderLayout.CENTER);

        JButton regBtn = new JButton("Register for Selected");
        regBtn.addActionListener(e -> {
            int row = catalogTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Please select a section first.");
                return;
            }

            String secId = String.valueOf(catalogModel.getValueAt(row, 0));
            try {
                studentService.register(getCurrentUser(), Integer.parseInt(secId));
                JOptionPane.showMessageDialog(this, "Registration Successful!");
                refreshCatalog();
                refreshMyData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel btnPanel = new JPanel();
        btnPanel.add(regBtn);
        panel.add(btnPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createMyDataPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        String[] cols = {"ID", "Course", "Time/Room", "Instructor"};
        myModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        myTable = new JTable(myModel);
        panel.add(new JScrollPane(myTable), BorderLayout.CENTER);

        JButton dropBtn = new JButton("Drop Selected Section");
        dropBtn.setBackground(new Color(255, 120, 120));
        dropBtn.addActionListener(e -> {
            int row = myTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Select a section to drop.");
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to drop this course?", "Confirm",
                    JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }

            String secId = String.valueOf(myModel.getValueAt(row, 0));
            try {
                studentService.drop(getCurrentUser(), Integer.parseInt(secId));
                JOptionPane.showMessageDialog(this, "Dropped successfully.");
                refreshMyData();
                refreshCatalog();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });

        JPanel btnPanel = new JPanel();
        btnPanel.add(dropBtn);
        panel.add(btnPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void refreshCatalog() {
        if (catalogModel == null) {
            return;
        }
        catalogModel.setRowCount(0);
        try {
            List<Map<String, String>> rows = studentService.getCourseCatalog();
            for (Map<String, String> r : rows) {
                catalogModel.addRow(new Object[]{
                        r.get("id"), r.get("code"), r.get("title"), r.get("credits"),
                        r.get("instructor"), r.get("time"), r.get("seats")
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void refreshMyData() {
        if (myModel == null) {
            return;
        }
        myModel.setRowCount(0);
        try {
            List<Map<String, String>> rows = studentService.getMySections(getCurrentUser());
            for (Map<String, String> r : rows) {
                myModel.addRow(new Object[]{
                        r.get("id"), r.get("display"), r.get("info"), r.get("instructor")
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private User getCurrentUser() {
        return SessionManager.getCurrentUser();
    }
}

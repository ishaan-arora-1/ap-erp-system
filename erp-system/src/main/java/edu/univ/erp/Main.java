package edu.univ.erp;

import com.formdev.flatlaf.FlatLightLaf;
import edu.univ.erp.ui.LoginScreen;

import javax.swing.*;

/**
 * Main entry point for the University ERP System
 * 
 * This class initializes the application and launches the login screen.
 * Database connections are automatically initialized by DatabaseFactory's static block
 * when the first database call is made.
 */
public class Main {
    public static void main(String[] args) {
        // Initialize FlatLaf Look and Feel for modern UI appearance
        // FlatLaf provides a clean, modern appearance across all platforms
        try {
            FlatLightLaf.setup();
        } catch (Exception ex) {
            // If FlatLaf fails, Swing will fallback to system default look and feel
            System.err.println("Failed to initialize FlatLaf");
        }

        // Launch the login screen on the Event Dispatch Thread (EDT)
        // SwingUtilities.invokeLater ensures thread-safety for Swing components
        SwingUtilities.invokeLater(() -> new LoginScreen().setVisible(true));
    }
}

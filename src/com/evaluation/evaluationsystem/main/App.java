package com.evaluation.evaluationsystem.main; // Or your main package

import com.evaluation.evaluationsystem.ui.LoginFrame;
import com.formdev.flatlaf.FlatLightLaf; // Basic light theme
// For other themes, you might import them like this:
// import com.formdev.flatlaf.FlatDarkLaf;
// import com.formdev.flatlaf.FlatIntelliJLaf;
// import com.formdev.flatlaf.themes.FlatMacLightLaf; // If using intellij-themes.jar

import javax.swing.*;

public class App {

    public static void main(String[] args) {
        // Apply a FlatLaf theme (BEFORE creating any Swing components)
        try {
            // --- CHOOSE ONE THEME TO START WITH ---
            UIManager.setLookAndFeel(new FlatLightLaf());
            // Examples of other themes (uncomment one to try it):
//             UIManager.setLookAndFeel(new FlatDarkLaf());
//             UIManager.setLookAndFeel(new com.formdev.flatlaf.intellijthemes.FlatDarkPurpleIJTheme()); // Example from intellij-themes
//             UIManager.setLookAndFeel(new com.formdev.flatlaf.intellijthemes.FlatLightFlatIJTheme()); // Another example
//             UIManager.setLookAndFeel(new FlatIntelliJLaf()); // A popular one

        } catch (UnsupportedLookAndFeelException e) {
            System.err.println("Failed to initialize FlatLaf L&F: " + e.getMessage());
            // Fallback to default L&F or handle error
        }

        // Ensure GUI updates are on the Event Dispatch Thread
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                // Create and show the login frame
                LoginFrame loginFrame = new LoginFrame();
                loginFrame.setVisible(true);
            }
        });
    }
}
//```        *   **Note on Theme Imports:** For themes from `flatlaf-intellij-themes.jar`, the import path is usually `com.formdev.flatlaf.intellijthemes.<ThemeName>IJTheme`. For example, `com.formdev.flatlaf.intellijthemes.FlatDarkPurpleIJTheme`.
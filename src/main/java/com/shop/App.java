package com.shop;

import com.shop.db.DBConnection;
import com.shop.ui.LoginFrame;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.sql.Connection;

public class App {

    public static void main(String[] args) {
        Thread.setDefaultUncaughtExceptionHandler((thread, error) -> {
            error.printStackTrace();
            showError("An unexpected error occurred:\n" + rootMessage(error));
        });

        try (Connection ignored = DBConnection.getConnection()) {
            // Opening the first connection creates and upgrades the per-user database.
        } catch (Exception e) {
            e.printStackTrace();
            showError("The shop database could not be opened:\n" + rootMessage(e));
            System.exit(1);
        }

        SwingUtilities.invokeLater(LoginFrame::new);
    }

    private static void showError(String message) {
        Runnable dialog = () -> JOptionPane.showMessageDialog(null, message, "Shop Desk", JOptionPane.ERROR_MESSAGE);
        if (SwingUtilities.isEventDispatchThread()) {
            dialog.run();
        } else {
            try {
                SwingUtilities.invokeAndWait(dialog);
            } catch (Exception ignored) {
            }
        }
    }

    private static String rootMessage(Throwable error) {
        Throwable root = error;
        while (root.getCause() != null) {
            root = root.getCause();
        }
        return root.getMessage() == null ? root.getClass().getSimpleName() : root.getMessage();
    }
}

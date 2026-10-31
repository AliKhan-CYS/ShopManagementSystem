package com.shop;

import com.shop.db.DBConnection;
import com.shop.ui.LoginFrame;

import javax.swing.SwingUtilities;
import java.sql.Connection;

public class App {
    public static void main(String[] args) throws Exception {

        //  Ensure database and all tables exist before app starts
        try (Connection ignored = DBConnection.getConnection()) {
            // Opening the connection creates and upgrades the per-user database.
        }
        SwingUtilities.invokeLater(LoginFrame::new);
    }
}

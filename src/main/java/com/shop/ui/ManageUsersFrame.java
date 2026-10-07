package com.shop.ui;

import com.shop.dao.UserDAO;
import com.shop.model.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ManageUsersFrame extends JFrame {

    private JTable table;
    private DefaultTableModel model;
    private final UserDAO dao = new UserDAO();
    private final User currentUser;

    private JTextField searchField;

    public ManageUsersFrame(User currentUser) {

        this.currentUser = currentUser;

        setTitle("Manage Employees");
        setSize(700, 450);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel main = new JPanel(new BorderLayout(10, 10));
        main.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        UITheme.styleRoot(main);

        add(main);

        // TOP (SEARCH)
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        top.setOpaque(false);

        searchField = new JTextField(20);
        JButton searchBtn = new JButton("Search");
        JButton resetBtn = new JButton("Reset");
        UITheme.styleField(searchField);
        UITheme.stylePrimaryButton(searchBtn);
        UITheme.styleButton(resetBtn);

        JLabel searchLabel = new JLabel("Search:");
        UITheme.styleLabel(searchLabel);
        top.add(searchLabel);
        top.add(searchField);
        top.add(searchBtn);
        top.add(resetBtn);

        main.add(top, BorderLayout.NORTH);

        // TABLE
        model = new DefaultTableModel(
                new String[]{"ID", "Username", "Role"}, 0
        ) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        table = new JTable(model);
        UITheme.styleTable(table);

        main.add(UITheme.wrapTable(table), BorderLayout.CENTER);

        //  BUTTONS
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        bottom.setOpaque(false);

        JButton addBtn = new JButton("Add User");
        JButton deleteBtn = new JButton("Delete User");
        UITheme.stylePrimaryButton(addBtn);
        UITheme.styleDangerButton(deleteBtn);

        bottom.add(addBtn);
        bottom.add(deleteBtn);

        main.add(bottom, BorderLayout.SOUTH);

        // ================= ACTIONS =================
        addBtn.addActionListener(e -> addUser());
        deleteBtn.addActionListener(e -> deleteUser());
        searchBtn.addActionListener(e -> searchUser());
        resetBtn.addActionListener(e -> {
            searchField.setText("");
            loadUsers();
        });

        // ENTER KEY SUPPORT
        searchField.addActionListener(e -> searchUser());

        loadUsers();

        setVisible(true);
    }

    // LOAD
    private void loadUsers() {

        model.setRowCount(0);

        List<User> list = dao.getAllUsers();

        for (User u : list) {
            model.addRow(new Object[]{
                    u.getId(),
                    u.getUsername(),
                    u.getRole()
            });
        }
    }

    //  SEARCH
    private void searchUser() {

        String keyword = searchField.getText().trim().toLowerCase();

        if (keyword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter search text");
            return;
        }

        model.setRowCount(0);

        List<User> all = dao.getAllUsers();
        List<User> result = new ArrayList<>();

        for (User u : all) {
            if (u.getUsername().toLowerCase().contains(keyword) ||
                    u.getRole().toLowerCase().contains(keyword)) {
                result.add(u);
            }
        }

        if (result.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No user found");
            return;
        }

        for (User u : result) {
            model.addRow(new Object[]{
                    u.getId(),
                    u.getUsername(),
                    u.getRole()
            });
        }
    }

    //  ADD
    private void addUser() {

        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();

        String[] roles = {"admin", "employee"};
        JComboBox<String> roleBox = new JComboBox<>(roles);
        UITheme.styleField(usernameField);
        UITheme.styleField(passwordField);
        UITheme.styleCombo(roleBox);

        Object[] fields = {
                "Username:", usernameField,
                "Password:", passwordField,
                "Role:", roleBox
        };

        int option = JOptionPane.showConfirmDialog(
                this, fields, "Add User", JOptionPane.OK_CANCEL_OPTION
        );

        if (option == JOptionPane.OK_OPTION) {

            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword()).trim();
            String role = roleBox.getSelectedItem().toString();

            if (username.isEmpty() || password.length() < 6) {
                JOptionPane.showMessageDialog(this, "Username is required and password must be at least 6 characters.");
                return;
            }

            boolean success = dao.addUser(new User(0, username, password, role));

            JOptionPane.showMessageDialog(this,
                    success ? "User added" : "Username already exists or user could not be added.");

            loadUsers();
        }
    }

    // DELETE
    private void deleteUser() {

        int row = table.getSelectedRow();

        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select user first");
            return;
        }

        int id = (int) model.getValueAt(row, 0);
        String username = model.getValueAt(row, 1).toString();
        String role = model.getValueAt(row, 2).toString();

        // prevent self delete
        if (username.equalsIgnoreCase(currentUser.getUsername())) {
            JOptionPane.showMessageDialog(this, "You cannot delete your own account!");
            return;
        }

        //  prevent deleting last admin
        if (role.equalsIgnoreCase("admin")) {
            long adminCount = dao.getAllUsers().stream()
                    .filter(u -> u.getRole().equalsIgnoreCase("admin"))
                    .count();

            if (adminCount <= 1) {
                JOptionPane.showMessageDialog(this, "Cannot delete last admin!");
                return;
            }
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Delete user: " + username + "?",
                "Confirm",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {

            boolean success = dao.deleteUser(id);

            JOptionPane.showMessageDialog(this,
                    success ? "User deleted" : "Delete failed");

            loadUsers();
        }
    }
}

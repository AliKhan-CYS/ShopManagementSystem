package com.shop.ui;

import com.shop.dao.UserDAO;
import com.shop.model.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class AdminPanel extends JFrame {

    private JTable table;
    private DefaultTableModel model;
    private UserDAO dao = new UserDAO();

    public AdminPanel() {

        setTitle("Manage Users");
        setSize(600, 400);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        model = new DefaultTableModel(
                new String[]{"ID", "Username", "Role"}, 0
        );

        table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel panel = new JPanel();

        JButton addBtn = new JButton("Add User");
        JButton deleteBtn = new JButton("Delete User");

        panel.add(addBtn);
        panel.add(deleteBtn);

        add(panel, BorderLayout.SOUTH);

        addBtn.addActionListener(e -> addUser());
        deleteBtn.addActionListener(e -> deleteUser());

        loadUsers();

        setVisible(true);
    }

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

    private void addUser() {

        JTextField username = new JTextField();
        JPasswordField password = new JPasswordField();

        String[] roles = {"admin", "employee"};
        JComboBox<String> roleBox = new JComboBox<>(roles);

        Object[] fields = {
                "Username:", username,
                "Password:", password,
                "Role:", roleBox
        };

        int option = JOptionPane.showConfirmDialog(
                this, fields, "Add User",
                JOptionPane.OK_CANCEL_OPTION
        );

        if (option == JOptionPane.OK_OPTION) {

            boolean success = dao.addUser(
                    new User(0,
                            username.getText(),
                            new String(password.getPassword()).trim(),
                            roleBox.getSelectedItem().toString()
                    )
            );

            JOptionPane.showMessageDialog(this,
                    success ? "User Added" : "Failed");

            loadUsers();
        }
    }

    private void deleteUser() {

        int row = table.getSelectedRow();

        if (row == -1) return;

        int id = (int) model.getValueAt(row, 0);

        dao.deleteUser(id);
        loadUsers();
    }
}
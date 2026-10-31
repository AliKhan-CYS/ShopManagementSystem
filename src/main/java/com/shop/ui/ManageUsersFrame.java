package com.shop.ui;

import com.shop.dao.UserDAO;
import com.shop.model.User;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
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
        if (currentUser == null || !currentUser.isAdmin()) {
            JOptionPane.showMessageDialog(null, "Administrator access is required.", "Access denied", JOptionPane.ERROR_MESSAGE);
            dispose();
            return;
        }

        setTitle("Manage Employees");
        setSize(820, 500);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel main = new JPanel(new BorderLayout(10, 10));
        main.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        UITheme.styleRoot(main);
        add(main);

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

        model = new DefaultTableModel(new String[]{"ID", "Username", "Role", "Status", "Last Login"}, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        table = new JTable(model);
        UITheme.styleTable(table);
        table.getColumnModel().getColumn(3).setCellRenderer(new StatusRenderer());
        main.add(UITheme.wrapTable(table), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 5));
        bottom.setOpaque(false);

        JButton addBtn = new JButton("Add User");
        JButton activateBtn = new JButton("Activate");
        JButton deactivateBtn = new JButton("Deactivate");
        JButton resetPasswordBtn = new JButton("Reset Password");
        UITheme.stylePrimaryButton(addBtn);
        UITheme.styleButton(activateBtn);
        UITheme.styleDangerButton(deactivateBtn);
        UITheme.styleButton(resetPasswordBtn);

        bottom.add(addBtn);
        bottom.add(activateBtn);
        bottom.add(deactivateBtn);
        bottom.add(resetPasswordBtn);
        main.add(bottom, BorderLayout.SOUTH);

        addBtn.addActionListener(e -> addUser());
        activateBtn.addActionListener(e -> setSelectedUserActive(true));
        deactivateBtn.addActionListener(e -> setSelectedUserActive(false));
        resetPasswordBtn.addActionListener(e -> resetSelectedPassword());
        searchBtn.addActionListener(e -> searchUser());
        resetBtn.addActionListener(e -> {
            searchField.setText("");
            loadUsers();
        });
        searchField.addActionListener(e -> searchUser());

        loadUsers();
        setVisible(true);
    }

    private void loadUsers() {
        renderUsers(dao.getAllUsers());
    }

    private void renderUsers(List<User> list) {
        model.setRowCount(0);
        for (User u : list) {
            model.addRow(new Object[]{
                    u.getId(),
                    u.getUsername(),
                    u.getRole(),
                    u.isActive() ? "Active" : "Inactive",
                    u.getLastLogin() == null ? "-" : u.getLastLogin()
            });
        }
    }

    private void searchUser() {
        String keyword = searchField.getText().trim().toLowerCase();
        if (keyword.isEmpty()) {
            loadUsers();
            return;
        }

        List<User> result = new ArrayList<>();
        for (User u : dao.getAllUsers()) {
            if (u.getUsername().toLowerCase().contains(keyword)
                    || u.getRole().toLowerCase().contains(keyword)
                    || (u.isActive() ? "active" : "inactive").contains(keyword)) {
                result.add(u);
            }
        }

        renderUsers(result);
        if (result.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No user found");
        }
    }

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

        int option = JOptionPane.showConfirmDialog(this, fields, "Add User", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());
            String role = roleBox.getSelectedItem().toString();

            if (!User.isValidUsername(username) || !com.shop.service.PasswordSecurity.isValidNewPassword(password)) {
                JOptionPane.showMessageDialog(this, "Use a valid 3–32 character username and a password of at least 12 characters.");
                return;
            }

            boolean success = dao.addUser(new User(0, username, password, role, true, null, null, null));
            JOptionPane.showMessageDialog(this,
                    success ? "User added" : "Username already exists or user could not be added.");
            loadUsers();
        }
    }

    private void setSelectedUserActive(boolean active) {
        User selected = selectedUser();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Select user first");
            return;
        }

        if (selected.getId() == currentUser.getId()) {
            JOptionPane.showMessageDialog(this, "You cannot change your own account status here.");
            return;
        }

        if (!active && selected.isAdmin() && dao.countActiveAdmins() <= 1) {
            JOptionPane.showMessageDialog(this, "Cannot deactivate the last active admin.");
            return;
        }

        String action = active ? "activate" : "deactivate";
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to " + action + " " + selected.getUsername() + "?",
                "Confirm", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = dao.setActive(selected.getId(), active);
            JOptionPane.showMessageDialog(this, success ? "Account updated." : "Update failed.");
            loadUsers();
        }
    }

    private void resetSelectedPassword() {
        User selected = selectedUser();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Select user first");
            return;
        }

        JPasswordField passwordField = new JPasswordField();
        UITheme.styleField(passwordField);
        int option = JOptionPane.showConfirmDialog(this,
                new Object[]{"New temporary password:", passwordField},
                "Reset Password", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String password = new String(passwordField.getPassword());
            if (!com.shop.service.PasswordSecurity.isValidNewPassword(password)) {
                JOptionPane.showMessageDialog(this, "Password must be at least 12 characters.");
                return;
            }
            boolean success = dao.setPassword(selected.getId(), password);
            JOptionPane.showMessageDialog(this, success ? "Password reset." : "Password reset failed.");
        }
    }

    private User selectedUser() {
        int row = table.getSelectedRow();
        if (row == -1) {
            return null;
        }
        int modelRow = table.convertRowIndexToModel(row);
        int id = (int) model.getValueAt(modelRow, 0);
        return dao.findById(id);
    }
    private static class StatusRenderer extends DefaultTableCellRenderer {
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            String status = value == null ? "" : value.toString();
            component.setFont(component.getFont().deriveFont(Font.BOLD));
            if (!isSelected) {
                component.setForeground("Active".equalsIgnoreCase(status) ? UITheme.SUCCESS : UITheme.DANGER);
                component.setBackground(UITheme.SURFACE);
            }
            setHorizontalAlignment(SwingConstants.CENTER);
            return component;
        }
    }
}

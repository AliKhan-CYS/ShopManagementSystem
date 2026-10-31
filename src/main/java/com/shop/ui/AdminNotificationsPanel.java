package com.shop.ui;

import com.shop.dao.AccountRequestDAO;
import com.shop.dao.UserDAO;
import com.shop.model.AccountRequest;
import com.shop.model.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;

public class AdminNotificationsPanel extends JPanel {

    private final AccountRequestDAO requestDAO = new AccountRequestDAO();
    private final UserDAO userDAO = new UserDAO();
    private JTable table;
    private DefaultTableModel model;
    private List<AccountRequest> requests;

    public AdminNotificationsPanel(User currentUser) {
        if (currentUser == null || !currentUser.isAdmin()) {
            throw new SecurityException("Administrator access is required");
        }
        setLayout(new BorderLayout(0, 14));
        UITheme.styleRoot(this);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("Notifications");
        UITheme.styleTitle(title);
        JButton refreshBtn = new JButton("Refresh");
        UITheme.stylePrimaryButton(refreshBtn);
        header.add(title, BorderLayout.WEST);
        header.add(refreshBtn, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        model = new DefaultTableModel(new String[]{"ID", "Type", "Username", "Role", "Status", "Created"}, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(model);
        UITheme.styleTable(table);
        add(UITheme.wrapTable(table), BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        actions.setOpaque(false);
        JButton approveBtn = new JButton("Approve");
        JButton rejectBtn = new JButton("Reject");
        UITheme.stylePrimaryButton(approveBtn);
        UITheme.styleDangerButton(rejectBtn);
        actions.add(approveBtn);
        actions.add(rejectBtn);
        add(actions, BorderLayout.SOUTH);

        refreshBtn.addActionListener(e -> loadRequests());
        approveBtn.addActionListener(e -> approveSelected());
        rejectBtn.addActionListener(e -> rejectSelected());

        loadRequests();
    }

    private void loadRequests() {
        requests = requestDAO.getAllRequests();
        model.setRowCount(0);
        for (AccountRequest request : requests) {
            model.addRow(new Object[]{
                    request.getId(),
                    readableType(request.getRequestType()),
                    request.getUsername(),
                    request.getRequestedRole() == null ? "-" : request.getRequestedRole(),
                    request.getStatus(),
                    request.getCreatedAt()
            });
        }
    }

    private void approveSelected() {
        AccountRequest request = selectedRequest();
        if (request == null || !request.isPending()) {
            JOptionPane.showMessageDialog(this, "Select a pending request first.");
            return;
        }

        if ("NEW_ACCOUNT".equals(request.getRequestType())) {
            approveNewAccount(request);
        } else if ("PASSWORD_RESET".equals(request.getRequestType())) {
            approvePasswordReset(request);
        }

        loadRequests();
    }

    private void approveNewAccount(AccountRequest request) {
        JPasswordField passwordField = new JPasswordField(generateTemporaryPassword());
        UITheme.styleField(passwordField);
        Object[] fields = {
                "Username:", request.getUsername(),
                "Role:", request.getRequestedRole() == null ? "employee" : request.getRequestedRole(),
                "Temporary password:", passwordField
        };

        int option = JOptionPane.showConfirmDialog(this, fields, "Approve New Account", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String password = new String(passwordField.getPassword());
            if (!com.shop.service.PasswordSecurity.isValidNewPassword(password)) {
                JOptionPane.showMessageDialog(this, "Password must be at least 12 characters.");
                return;
            }

            boolean added = userDAO.addUser(new User(0, request.getUsername(), password,
                    request.getRequestedRole() == null ? "employee" : request.getRequestedRole(), true, null, null, null));
            if (added) {
                requestDAO.resolveRequest(request.getId(), "APPROVED", "Account created by admin.");
                JOptionPane.showMessageDialog(this, "Account created. Give the employee the temporary password securely.");
            } else {
                JOptionPane.showMessageDialog(this, "Could not create account. Username may already exist.");
            }
        }
    }

    private void approvePasswordReset(AccountRequest request) {
        User user = userDAO.findByUsername(request.getUsername());
        if (user == null) {
            JOptionPane.showMessageDialog(this, "No account exists for this username.");
            return;
        }

        JPasswordField passwordField = new JPasswordField(generateTemporaryPassword());
        UITheme.styleField(passwordField);
        Object[] fields = {
                "Username:", user.getUsername(),
                "New temporary password:", passwordField
        };

        int option = JOptionPane.showConfirmDialog(this, fields, "Approve Password Reset", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String password = new String(passwordField.getPassword());
            if (!com.shop.service.PasswordSecurity.isValidNewPassword(password)) {
                JOptionPane.showMessageDialog(this, "Password must be at least 12 characters.");
                return;
            }

            if (userDAO.setPassword(user.getId(), password)) {
                requestDAO.resolveRequest(request.getId(), "APPROVED", "Password reset by admin.");
                JOptionPane.showMessageDialog(this, "Password reset. Share the temporary password securely.");
            } else {
                JOptionPane.showMessageDialog(this, "The password could not be reset.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void rejectSelected() {
        AccountRequest request = selectedRequest();
        if (request == null || !request.isPending()) {
            JOptionPane.showMessageDialog(this, "Select a pending request first.");
            return;
        }

        String reason = JOptionPane.showInputDialog(this, "Reason:", "Request rejected by admin.");
        if (reason != null) {
            requestDAO.resolveRequest(request.getId(), "REJECTED", reason);
            loadRequests();
        }
    }

    private AccountRequest selectedRequest() {
        int row = table.getSelectedRow();
        if (row == -1) {
            return null;
        }
        int modelRow = table.convertRowIndexToModel(row);
        int id = (int) model.getValueAt(modelRow, 0);
        return requests.stream().filter(request -> request.getId() == id).findFirst().orElse(null);
    }

    private String readableType(String type) {
        return "NEW_ACCOUNT".equals(type) ? "New account" : "Password reset";
    }

    private String generateTemporaryPassword() {
        byte[] bytes = new byte[9];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}

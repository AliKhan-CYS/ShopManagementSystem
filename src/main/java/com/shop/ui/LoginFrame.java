package com.shop.ui;

import com.shop.dao.AccountRequestDAO;
import com.shop.dao.UserDAO;
import com.shop.model.User;
import com.shop.service.RecoveryService;
import com.shop.service.UserService;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.io.File;

public class LoginFrame extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JLabel statusLabel;
    private JLabel roleIndicator;
    private final UserService userService = new UserService();
    private final UserDAO userDAO = new UserDAO();
    private final AccountRequestDAO requestDAO = new AccountRequestDAO();
    private final RecoveryService recoveryService = new RecoveryService();

    public LoginFrame() {
        UITheme.setup();

        setTitle("Shop Management Login");
        setSize(880, 540);
        setMinimumSize(new Dimension(800, 500));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel(new GridLayout(1, 2));
        UITheme.styleRoot(mainPanel);
        add(mainPanel);

        mainPanel.add(createBrandPanel());
        mainPanel.add(createLoginPanel());

        SwingUtilities.invokeLater(usernameField::requestFocusInWindow);
        setVisible(true);
    }

    private JPanel createBrandPanel() {
        JPanel left = new JPanel(new GridBagLayout());
        left.setBackground(UITheme.BACKGROUND);
        left.setBorder(BorderFactory.createEmptyBorder(42, 42, 42, 42));

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        JLabel logo = new JLabel("SHOP DESK", SwingConstants.CENTER);
        logo.setFont(new Font("Segoe UI", Font.BOLD, 30));
        logo.setForeground(UITheme.TEXT);
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel mark = new JLabel(AvatarUtil.avatarFor(null, 108));
        mark.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel tagline = new JLabel("Simple, secure shop operations.");
        tagline.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        tagline.setForeground(UITheme.MUTED_TEXT);
        tagline.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel detail = new JLabel("Products, customers, sales, reports, and account support.");
        detail.setForeground(UITheme.MUTED_TEXT);
        detail.setAlignmentX(Component.CENTER_ALIGNMENT);

        content.add(mark);
        content.add(Box.createRigidArea(new Dimension(0, 24)));
        content.add(logo);
        content.add(Box.createRigidArea(new Dimension(0, 10)));
        content.add(tagline);
        content.add(Box.createRigidArea(new Dimension(0, 8)));
        content.add(detail);

        left.add(content);
        return left;
    }

    private JPanel createLoginPanel() {
        JPanel wrap = new JPanel(new GridBagLayout());
        UITheme.styleRoot(wrap);
        wrap.setBorder(BorderFactory.createEmptyBorder(36, 42, 36, 42));

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        UITheme.styleCard(card);
        card.setPreferredSize(new Dimension(340, 420));

        JLabel title = new JLabel("Welcome back");
        UITheme.styleTitle(title);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("Sign in with your shop account.");
        UITheme.styleMuted(subtitle);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        usernameField = new JTextField();
        passwordField = new JPasswordField();
        UITheme.styleField(usernameField);
        UITheme.styleField(passwordField);
        usernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        roleIndicator = new JLabel("Role: enter username");
        roleIndicator.setAlignmentX(Component.LEFT_ALIGNMENT);
        UITheme.styleMuted(roleIndicator);

        JButton loginBtn = new JButton("Sign In");
        UITheme.stylePrimaryButton(loginBtn);
        loginBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));

        JCheckBox showPassword = new JCheckBox("Show password");
        showPassword.setOpaque(false);
        showPassword.setForeground(UITheme.MUTED_TEXT);
        showPassword.setAlignmentX(Component.LEFT_ALIGNMENT);
        char defaultEcho = passwordField.getEchoChar();
        showPassword.addActionListener(e -> passwordField.setEchoChar(showPassword.isSelected() ? (char) 0 : defaultEcho));

        JButton forgotBtn = createLinkButton("Forgot password?");
        JButton requestAccountBtn = createLinkButton("Request new employee account");
        JButton adminRecoveryBtn = createLinkButton("Admin recovery file reset");

        statusLabel = new JLabel(" ");
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        UITheme.styleMuted(statusLabel);

        card.add(title);
        card.add(Box.createRigidArea(new Dimension(0, 6)));
        card.add(subtitle);
        card.add(Box.createRigidArea(new Dimension(0, 24)));
        addField(card, "Username", usernameField);
        card.add(Box.createRigidArea(new Dimension(0, 12)));
        addField(card, "Password", passwordField);
        card.add(Box.createRigidArea(new Dimension(0, 8)));
        card.add(showPassword);
        card.add(Box.createRigidArea(new Dimension(0, 8)));
        card.add(roleIndicator);
        card.add(Box.createRigidArea(new Dimension(0, 12)));
        card.add(statusLabel);
        card.add(Box.createRigidArea(new Dimension(0, 14)));
        card.add(loginBtn);
        card.add(Box.createRigidArea(new Dimension(0, 14)));
        card.add(forgotBtn);
        card.add(requestAccountBtn);
        card.add(adminRecoveryBtn);

        usernameField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { updateRoleIndicator(); }
            public void removeUpdate(DocumentEvent e) { updateRoleIndicator(); }
            public void changedUpdate(DocumentEvent e) { updateRoleIndicator(); }
        });

        loginBtn.addActionListener(e -> login());
        passwordField.addActionListener(e -> login());
        forgotBtn.addActionListener(e -> sendPasswordRequest());
        requestAccountBtn.addActionListener(e -> sendNewAccountRequest());
        adminRecoveryBtn.addActionListener(e -> resetWithRecoveryFile());
        getRootPane().setDefaultButton(loginBtn);

        wrap.add(card);
        return wrap;
    }

    private JButton createLinkButton(String text) {
        JButton button = new JButton(text);
        button.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setForeground(UITheme.ACCENT);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    private void addField(JPanel panel, String labelText, JTextField field) {
        JLabel label = new JLabel(labelText);
        UITheme.styleLabel(label);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(label);
        panel.add(Box.createRigidArea(new Dimension(0, 6)));
        panel.add(field);
    }

    private void updateRoleIndicator() {
        String username = usernameField.getText().trim();
        if (username.isEmpty()) {
            roleIndicator.setText("Role: enter username");
            roleIndicator.setForeground(UITheme.MUTED_TEXT);
            return;
        }

        User user = userDAO.findByUsername(username);
        if (user == null) {
            roleIndicator.setText("Role: account not found");
            roleIndicator.setForeground(UITheme.WARNING);
            return;
        }

        roleIndicator.setText("Role: " + user.getRole() + (user.isActive() ? "" : " (inactive)"));
        roleIndicator.setForeground(user.isAdmin() ? UITheme.SUCCESS : UITheme.ACCENT);
    }

    private void login() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            showStatus("Enter username and password.", true);
            return;
        }

        User user = userService.login(username, password);

        if (user != null) {
            showStatus("Opening dashboard...", false);
            new MainFrame(user);
            dispose();
        } else {
            passwordField.setText("");
            passwordField.requestFocusInWindow();
            showStatus("Invalid password or inactive account.", true);
        }
    }

    private void sendPasswordRequest() {
        String username = JOptionPane.showInputDialog(this, "Enter your username:", usernameField.getText().trim());
        if (username == null || username.trim().isEmpty()) {
            return;
        }

        boolean success = requestDAO.createRequest(username.trim(), "PASSWORD_RESET", null, "User requested password reset from login screen.");
        showStatus(success ? "Password reset request sent to admin." : "Could not send request.", !success);
    }

    private void sendNewAccountRequest() {
        JTextField username = new JTextField();
        JTextArea notes = new JTextArea(4, 20);
        UITheme.styleField(username);
        notes.setLineWrap(true);
        notes.setWrapStyleWord(true);
        notes.setBackground(UITheme.SURFACE_ALT);
        notes.setForeground(UITheme.TEXT);

        Object[] fields = {
                "Preferred username:", username,
                "Reason / details:", new JScrollPane(notes)
        };

        int option = JOptionPane.showConfirmDialog(this, fields, "Request Employee Account", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION && !username.getText().trim().isEmpty()) {
            boolean success = requestDAO.createRequest(username.getText().trim(), "NEW_ACCOUNT", "employee", notes.getText().trim());
            showStatus(success ? "New account request sent to admin." : "Could not send request.", !success);
        }
    }

    private void resetWithRecoveryFile() {
        JTextField username = new JTextField();
        JPasswordField newPassword = new JPasswordField();
        JPasswordField confirmPassword = new JPasswordField();
        UITheme.styleField(username);
        UITheme.styleField(newPassword);
        UITheme.styleField(confirmPassword);

        Object[] fields = {
                "Admin username:", username,
                "New password:", newPassword,
                "Confirm password:", confirmPassword
        };

        int option = JOptionPane.showConfirmDialog(this, fields, "Admin Recovery Reset", JOptionPane.OK_CANCEL_OPTION);
        if (option != JOptionPane.OK_OPTION) {
            return;
        }

        String password = new String(newPassword.getPassword());
        String confirm = new String(confirmPassword.getPassword());
        if (username.getText().trim().isEmpty() || password.length() < 6 || !password.equals(confirm)) {
            showStatus("Recovery reset needs matching passwords of 6+ characters.", true);
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select admin recovery file");
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            boolean success = recoveryService.resetAdminPasswordWithFile(username.getText().trim(), file, password);
            showStatus(success ? "Admin password reset. Sign in with the new password." : "Recovery file was invalid or already used.", !success);
        }
    }

    private void showStatus(String message, boolean error) {
        statusLabel.setText(message);
        statusLabel.setForeground(error ? UITheme.DANGER : UITheme.SUCCESS);
    }
}
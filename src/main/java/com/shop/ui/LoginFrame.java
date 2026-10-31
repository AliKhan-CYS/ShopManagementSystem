package com.shop.ui;

import com.shop.dao.AccountRequestDAO;
import com.shop.dao.UserDAO;
import com.shop.model.User;
import com.shop.service.RecoveryService;

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
    private JPanel roleBadge;
    private final UserDAO userDAO = new UserDAO();
    private final AccountRequestDAO requestDAO = new AccountRequestDAO();
    private final RecoveryService recoveryService = new RecoveryService();

    public LoginFrame() {
        UITheme.setup();

        setTitle("Shop Desk Login");
        setIconImage(ShopLogo.render(64));
        setSize(980, 660);
        setMinimumSize(new Dimension(900, 620));
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel(new GridLayout(1, 2));
        UITheme.styleRoot(mainPanel);
        add(mainPanel);

        mainPanel.add(createBrandPanel());
        mainPanel.add(createLoginPanel());

        SwingUtilities.invokeLater(usernameField::requestFocusInWindow);
        setVisible(true);
        SwingUtilities.invokeLater(this::ensureInitialAdmin);
    }

    private void ensureInitialAdmin() {
        long userCount = userDAO.countUsers();
        if (userCount > 0) return;
        if (userCount < 0) {
            JOptionPane.showMessageDialog(this, "The local database could not be read.", "Startup error", JOptionPane.ERROR_MESSAGE);
            dispose();
            return;
        }
        JTextField initialUsername = new JTextField();
        JPasswordField initialPassword = new JPasswordField();
        JPasswordField confirmPassword = new JPasswordField();
        Object[] fields = {"Administrator username (3–32 letters, numbers, . _ -):", initialUsername,
                "Administrator password (at least 12 characters):", initialPassword, "Confirm password:", confirmPassword};
        int choice = JOptionPane.showConfirmDialog(this, fields, "Create First Administrator", JOptionPane.OK_CANCEL_OPTION);
        char[] first = initialPassword.getPassword();
        char[] second = confirmPassword.getPassword();
        String username = initialUsername.getText().trim();
        try {
            if (choice != JOptionPane.OK_OPTION) {
                dispose();
                return;
            }
            if (!User.isValidUsername(username) || first.length < 12
                    || !java.util.Arrays.equals(first, second)) {
                JOptionPane.showMessageDialog(this, "Enter a valid username and matching passwords of at least 12 characters.");
                ensureInitialAdmin();
                return;
            }
            if (!userDAO.createInitialAdmin(username, new String(first))) {
                JOptionPane.showMessageDialog(this, "Could not create the first administrator. Restart the application and try again.",
                        "Setup failed", JOptionPane.ERROR_MESSAGE);
                dispose();
            }
        } finally {
            java.util.Arrays.fill(first, '\0');
            java.util.Arrays.fill(second, '\0');
        }
    }

    private JPanel createBrandPanel() {
        GradientPanel left = new GradientPanel();
        left.setLayout(new GridBagLayout());
        left.setBorder(BorderFactory.createEmptyBorder(48, 48, 48, 48));

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        JLabel mark = new JLabel(new ImageIcon(ShopLogo.render(68)));
        mark.setPreferredSize(new Dimension(68, 68));
        mark.setMaximumSize(new Dimension(68, 68));
        mark.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel logo = new JLabel("Shop Desk");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 36));
        logo.setForeground(Color.WHITE);
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel tagline = new JLabel("A clearer way to run your shop.");
        tagline.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        tagline.setForeground(new Color(203, 213, 225));
        tagline.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel description = new JLabel("Sales, stock, and your team, all in sync.");
        description.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        description.setForeground(new Color(203, 213, 225));
        description.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel chips = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        chips.setOpaque(false);
        chips.setAlignmentX(Component.LEFT_ALIGNMENT);
        chips.add(createHeroChip("LIVE INVENTORY"));
        chips.add(createHeroChip("FASTER CHECKOUT"));
        chips.add(createHeroChip("TEAM READY"));

        JPanel note = new JPanel();
        note.setOpaque(false);
        note.setLayout(new BoxLayout(note, BoxLayout.Y_AXIS));
        note.setBorder(BorderFactory.createEmptyBorder(22, 0, 0, 0));
        note.setAlignmentX(Component.LEFT_ALIGNMENT);
        note.add(createHeroLine("\u2713  Know what is in stock at a glance"));
        note.add(Box.createRigidArea(new Dimension(0, 10)));
        note.add(createHeroLine("\u2713  Keep daily sales and operations together"));
        note.add(Box.createRigidArea(new Dimension(0, 10)));
        note.add(createHeroLine("\u2713  Give every team member the right access"));

        content.add(mark);
        content.add(Box.createRigidArea(new Dimension(0, 22)));
        content.add(logo);
        content.add(Box.createRigidArea(new Dimension(0, 8)));
        content.add(tagline);
        content.add(Box.createRigidArea(new Dimension(0, 6)));
        content.add(description);
        content.add(Box.createRigidArea(new Dimension(0, 24)));
        content.add(chips);
        content.add(Box.createVerticalGlue());
        content.add(note);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        left.add(content, gbc);
        return left;
    }

    private JLabel createHeroChip(String text) {
        JLabel chip = new JLabel(text);
        chip.setFont(new Font("Segoe UI", Font.BOLD, 12));
        chip.setForeground(new Color(226, 232, 240));
        chip.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(96, 165, 250, 120)),
                BorderFactory.createEmptyBorder(7, 12, 7, 12)
        ));
        return chip;
    }

    private JLabel createHeroLine(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        label.setForeground(new Color(203, 213, 225));
        return label;
    }

    private JPanel createLoginPanel() {
        JPanel wrap = new JPanel(new GridBagLayout());
        UITheme.styleRoot(wrap);
        wrap.setBorder(BorderFactory.createEmptyBorder(24, 52, 24, 52));

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        UITheme.styleElevatedCard(card);
        card.setPreferredSize(new Dimension(380, 486));

        JLabel eyebrow = new JLabel("YOUR SHOP, IN GOOD HANDS");
        eyebrow.setForeground(UITheme.ACCENT_SOFT);
        eyebrow.setFont(new Font("Segoe UI", Font.BOLD, 12));
        eyebrow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel title = new JLabel("Welcome back");
        title.setFont(new Font("Segoe UI", Font.BOLD, 30));
        title.setForeground(UITheme.TEXT);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("Sign in to pick up where you left off.");
        UITheme.styleMuted(subtitle);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        usernameField = new JTextField();
        passwordField = new JPasswordField();
        UITheme.styleField(usernameField);
        UITheme.styleField(passwordField);
        usernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        roleIndicator = new JLabel("Your account access is checked securely at sign in");
        roleIndicator.setFont(new Font("Segoe UI", Font.BOLD, 12));
        roleBadge = UITheme.createStatusBadge(roleIndicator, UITheme.MUTED_TEXT, new Color(51, 65, 85));
        roleBadge.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton loginBtn = new JButton("Sign in to Shop Desk   \u2192");
        UITheme.stylePrimaryButton(loginBtn);
        loginBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        JCheckBox showPassword = new JCheckBox("Show password");
        showPassword.setOpaque(false);
        showPassword.setForeground(UITheme.MUTED_TEXT);
        showPassword.setAlignmentX(Component.LEFT_ALIGNMENT);
        char defaultEcho = passwordField.getEchoChar();
        showPassword.addActionListener(e -> passwordField.setEchoChar(showPassword.isSelected() ? (char) 0 : defaultEcho));

        JPanel links = new JPanel(new GridLayout(0, 1, 0, 2));
        links.setOpaque(false);
        links.setAlignmentX(Component.LEFT_ALIGNMENT);
        links.setMaximumSize(new Dimension(Integer.MAX_VALUE, 88));
        JButton forgotBtn = createLinkButton("Forgot password?");
        JButton requestAccountBtn = createLinkButton("Request employee account");
        JButton adminRecoveryBtn = createLinkButton("Reset admin with recovery file");
        links.add(forgotBtn);
        links.add(requestAccountBtn);
        links.add(adminRecoveryBtn);

        JLabel privacyNote = new JLabel("Your workspace is protected with secure sign-in.");
        privacyNote.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        privacyNote.setForeground(UITheme.MUTED_TEXT);
        privacyNote.setAlignmentX(Component.LEFT_ALIGNMENT);

        statusLabel = new JLabel(" ");
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        UITheme.styleMuted(statusLabel);

        card.add(eyebrow);
        card.add(Box.createRigidArea(new Dimension(0, 10)));
        card.add(title);
        card.add(Box.createRigidArea(new Dimension(0, 8)));
        card.add(subtitle);
        card.add(Box.createRigidArea(new Dimension(0, 26)));
        addField(card, "Username", usernameField);
        card.add(Box.createRigidArea(new Dimension(0, 14)));
        addField(card, "Password", passwordField);
        card.add(Box.createRigidArea(new Dimension(0, 10)));
        card.add(showPassword);
        card.add(Box.createRigidArea(new Dimension(0, 12)));
        card.add(roleBadge);
        card.add(Box.createRigidArea(new Dimension(0, 10)));
        card.add(statusLabel);
        card.add(Box.createRigidArea(new Dimension(0, 14)));
        card.add(loginBtn);
        card.add(Box.createRigidArea(new Dimension(0, 18)));
        card.add(links);
        card.add(Box.createRigidArea(new Dimension(0, 7)));
        card.add(privacyNote);

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
        button.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setForeground(UITheme.ACCENT_SOFT);
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
        panel.add(Box.createRigidArea(new Dimension(0, 7)));
        panel.add(field);
    }

    private void updateRoleIndicator() {
        setRoleBadge("Your account access is checked securely at sign in", UITheme.MUTED_TEXT, new Color(51, 65, 85));
    }

    private void setRoleBadge(String text, Color foreground, Color background) {
        roleIndicator.setText(text);
        roleIndicator.setForeground(foreground);
        roleBadge.setBackground(background);
        roleBadge.repaint();
    }

    private void login() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            showStatus("Enter username and password.", true);
            return;
        }

        User user = userDAO.login(username, password);

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
        if (!User.isValidUsername(username.getText())
                || !com.shop.service.PasswordSecurity.isValidNewPassword(password) || !password.equals(confirm)) {
            showStatus("Recovery reset needs a valid username and matching passwords of 12+ characters.", true);
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

    private static class GradientPanel extends JPanel {
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            GradientPaint paint = new GradientPaint(0, 0, new Color(14, 116, 144), getWidth(), getHeight(), new Color(30, 41, 59));
            g2.setPaint(paint);
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.setColor(new Color(255, 255, 255, 22));
            g2.fillOval(getWidth() - 170, 58, 260, 260);
            g2.setColor(new Color(34, 197, 94, 26));
            g2.fillOval(-80, getHeight() - 170, 240, 240);
            g2.dispose();
        }
    }
}

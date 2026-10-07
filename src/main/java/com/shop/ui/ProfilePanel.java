package com.shop.ui;

import com.shop.dao.UserDAO;
import com.shop.model.User;
import com.shop.service.RecoveryService;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class ProfilePanel extends JPanel {

    private final int userId;
    private final UserDAO userDAO = new UserDAO();
    private final RecoveryService recoveryService = new RecoveryService();
    private final Runnable onProfileUpdated;
    private User user;
    private JLabel avatarLabel;
    private JLabel usernameValue;
    private JLabel roleValue;
    private JLabel statusValue;
    private JLabel lastLoginValue;
    private JLabel createdValue;

    public ProfilePanel(User user, Runnable onProfileUpdated) {
        this.user = user;
        this.userId = user.getId();
        this.onProfileUpdated = onProfileUpdated;

        setLayout(new BorderLayout(0, 18));
        UITheme.styleRoot(this);

        add(createHeader(), BorderLayout.NORTH);
        add(createContent(), BorderLayout.CENTER);
        refreshUser();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Profile");
        UITheme.styleTitle(title);
        JLabel subtitle = new JLabel("Account details, profile photo, and password settings.");
        UITheme.styleMuted(subtitle);

        header.add(title);
        header.add(Box.createRigidArea(new Dimension(0, 6)));
        header.add(subtitle);
        return header;
    }

    private JPanel createContent() {
        JPanel content = new JPanel(new BorderLayout(18, 0));
        content.setOpaque(false);

        JPanel identity = new JPanel();
        identity.setLayout(new BoxLayout(identity, BoxLayout.Y_AXIS));
        UITheme.styleCard(identity);
        identity.setPreferredSize(new Dimension(260, 0));

        avatarLabel = new JLabel();
        avatarLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        usernameValue = new JLabel();
        usernameValue.setAlignmentX(Component.CENTER_ALIGNMENT);
        UITheme.styleSectionTitle(usernameValue);

        roleValue = new JLabel();
        roleValue.setAlignmentX(Component.CENTER_ALIGNMENT);
        UITheme.styleMuted(roleValue);

        JButton photoBtn = new JButton("Choose Photo");
        UITheme.stylePrimaryButton(photoBtn);
        photoBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        photoBtn.addActionListener(e -> choosePhoto());

        identity.add(avatarLabel);
        identity.add(Box.createRigidArea(new Dimension(0, 16)));
        identity.add(usernameValue);
        identity.add(Box.createRigidArea(new Dimension(0, 4)));
        identity.add(roleValue);
        identity.add(Box.createRigidArea(new Dimension(0, 20)));
        identity.add(photoBtn);

        JPanel details = new JPanel(new GridLayout(0, 1, 0, 12));
        UITheme.styleCard(details);

        statusValue = addInfoRow(details, "Account status");
        lastLoginValue = addInfoRow(details, "Last login");
        createdValue = addInfoRow(details, "Created at");

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        actions.setOpaque(false);

        JButton passwordBtn = new JButton("Change Password");
        UITheme.stylePrimaryButton(passwordBtn);
        passwordBtn.addActionListener(e -> changePassword());
        actions.add(passwordBtn);

        if (user.isAdmin()) {
            JButton recoveryBtn = new JButton("Create Recovery File");
            UITheme.styleButton(recoveryBtn);
            recoveryBtn.addActionListener(e -> createRecoveryFile());
            actions.add(recoveryBtn);
        }

        JPanel right = new JPanel(new BorderLayout(0, 14));
        right.setOpaque(false);
        right.add(details, BorderLayout.CENTER);
        right.add(actions, BorderLayout.SOUTH);

        content.add(identity, BorderLayout.WEST);
        content.add(right, BorderLayout.CENTER);
        return content;
    }

    private JLabel addInfoRow(JPanel parent, String labelText) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        JLabel label = new JLabel(labelText);
        UITheme.styleMuted(label);
        JLabel value = new JLabel();
        UITheme.styleLabel(value);
        row.add(label, BorderLayout.WEST);
        row.add(value, BorderLayout.EAST);
        parent.add(row);
        return value;
    }

    private void refreshUser() {
        User latest = userDAO.findById(userId);
        if (latest != null) {
            user = latest;
        }

        avatarLabel.setIcon(AvatarUtil.avatarFor(user, 112));
        usernameValue.setText(user.getUsername());
        roleValue.setText(user.getRole());
        statusValue.setText(user.isActive() ? "Active" : "Inactive");
        lastLoginValue.setText(valueOrDash(user.getLastLogin()));
        createdValue.setText(valueOrDash(user.getCreatedAt()));
    }

    private String valueOrDash(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private void choosePhoto() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Choose profile photo");
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selected = chooser.getSelectedFile();
            if (userDAO.updateProfilePhoto(userId, selected.getAbsolutePath())) {
                refreshUser();
                onProfileUpdated.run();
                JOptionPane.showMessageDialog(this, "Profile photo updated.");
            }
        }
    }

    private void changePassword() {
        JPasswordField current = new JPasswordField();
        JPasswordField next = new JPasswordField();
        JPasswordField confirm = new JPasswordField();
        UITheme.styleField(current);
        UITheme.styleField(next);
        UITheme.styleField(confirm);

        Object[] fields = {
                "Current password:", current,
                "New password:", next,
                "Confirm new password:", confirm
        };

        int option = JOptionPane.showConfirmDialog(this, fields, "Change Password", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String newPassword = new String(next.getPassword());
            if (newPassword.length() < 6 || !newPassword.equals(new String(confirm.getPassword()))) {
                JOptionPane.showMessageDialog(this, "New passwords must match and be at least 6 characters.");
                return;
            }

            boolean success = userDAO.changePassword(userId, new String(current.getPassword()), newPassword);
            JOptionPane.showMessageDialog(this, success ? "Password changed." : "Current password is incorrect.");
        }
    }

    private void createRecoveryFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save admin recovery file");
        chooser.setSelectedFile(new File(user.getUsername() + "-recovery.properties"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            boolean success = recoveryService.createRecoveryFile(user, chooser.getSelectedFile());
            JOptionPane.showMessageDialog(this,
                    success ? "Recovery file created. Keep it private; it contains a one-time token, not your password."
                            : "Could not create recovery file.");
        }
    }
}
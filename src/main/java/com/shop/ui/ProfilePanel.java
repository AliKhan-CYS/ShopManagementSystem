package com.shop.ui;

import com.shop.dao.UserDAO;
import com.shop.model.User;
import com.shop.service.RecoveryService;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.util.Iterator;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

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
    private JLabel statusDetailValue;
    private JPanel statusBadge;
    private JLabel lastLoginValue;
    private JLabel createdValue;
    private JLabel photoPathValue;

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
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Profile");
        UITheme.styleTitle(title);
        JLabel subtitle = new JLabel("Account identity, sign-in details, and security settings.");
        UITheme.styleMuted(subtitle);

        text.add(title);
        text.add(Box.createRigidArea(new Dimension(0, 6)));
        text.add(subtitle);
        header.add(text, BorderLayout.WEST);
        return header;
    }

    private JPanel createContent() {
        JPanel content = new JPanel(new BorderLayout(18, 0));
        content.setOpaque(false);

        JPanel identity = new JPanel();
        identity.setLayout(new BoxLayout(identity, BoxLayout.Y_AXIS));
        UITheme.styleElevatedCard(identity);
        identity.setPreferredSize(new Dimension(300, 0));

        avatarLabel = new JLabel();
        avatarLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        usernameValue = new JLabel();
        usernameValue.setAlignmentX(Component.CENTER_ALIGNMENT);
        usernameValue.setFont(new Font("Segoe UI", Font.BOLD, 24));
        usernameValue.setForeground(UITheme.TEXT);

        roleValue = new JLabel();
        roleValue.setAlignmentX(Component.CENTER_ALIGNMENT);
        UITheme.styleMuted(roleValue);

        statusValue = new JLabel();
        statusValue.setFont(new Font("Segoe UI", Font.BOLD, 12));
        statusBadge = UITheme.createStatusBadge(statusValue, UITheme.SUCCESS, new Color(20, 83, 45));
        statusBadge.setAlignmentX(Component.CENTER_ALIGNMENT);

        photoPathValue = new JLabel(" ");
        photoPathValue.setAlignmentX(Component.CENTER_ALIGNMENT);
        photoPathValue.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        photoPathValue.setForeground(UITheme.MUTED_TEXT);

        JButton photoBtn = new JButton("Upload Profile Photo");
        UITheme.stylePrimaryButton(photoBtn);
        photoBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        photoBtn.addActionListener(e -> choosePhoto());

        identity.add(avatarLabel);
        identity.add(Box.createRigidArea(new Dimension(0, 18)));
        identity.add(usernameValue);
        identity.add(Box.createRigidArea(new Dimension(0, 5)));
        identity.add(roleValue);
        identity.add(Box.createRigidArea(new Dimension(0, 14)));
        identity.add(statusBadge);
        identity.add(Box.createRigidArea(new Dimension(0, 22)));
        identity.add(photoBtn);
        identity.add(Box.createRigidArea(new Dimension(0, 12)));
        identity.add(photoPathValue);

        JPanel details = new JPanel(new GridLayout(0, 1, 0, 12));
        UITheme.styleElevatedCard(details);

        statusDetailValue = createValueLabel();
        details.add(createInfoCard("Account status", statusDetailValue, "Green means active, red means inactive."));
        lastLoginValue = createValueLabel();
        details.add(createInfoCard("Last login", lastLoginValue, "Updated after successful sign in."));
        createdValue = createValueLabel();
        details.add(createInfoCard("Created at", createdValue, "Original account creation timestamp."));

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

    private JPanel createInfoCard(String titleText, JLabel value, String hint) {
        JPanel row = new JPanel(new BorderLayout(16, 0));
        row.setOpaque(true);
        row.setBackground(UITheme.SURFACE_ALT);
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(71, 85, 105)),
                BorderFactory.createEmptyBorder(13, 14, 13, 14)
        ));

        JPanel copy = new JPanel();
        copy.setOpaque(false);
        copy.setLayout(new BoxLayout(copy, BoxLayout.Y_AXIS));
        JLabel title = new JLabel(titleText);
        UITheme.styleLabel(title);
        JLabel helper = new JLabel(hint);
        helper.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        helper.setForeground(UITheme.MUTED_TEXT);
        copy.add(title);
        copy.add(Box.createRigidArea(new Dimension(0, 4)));
        copy.add(helper);

        row.add(copy, BorderLayout.WEST);
        row.add(value, BorderLayout.EAST);
        return row;
    }

    private JLabel createValueLabel() {
        JLabel label = new JLabel();
        label.setForeground(UITheme.TEXT);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        return label;
    }

    private void refreshUser() {
        User latest = userDAO.findById(userId);
        if (latest != null) {
            user = latest;
        }

        avatarLabel.setIcon(AvatarUtil.avatarFor(user, 132));
        usernameValue.setText(user.getUsername());
        roleValue.setText(user.getRole().toUpperCase(Locale.ROOT));
        applyStatusBadge(user.isActive());
        lastLoginValue.setText(valueOrDash(user.getLastLogin()));
        createdValue.setText(valueOrDash(user.getCreatedAt()));
        photoPathValue.setText(user.getProfilePhotoPath() == null || user.getProfilePhotoPath().isBlank()
                ? "No photo uploaded"
                : "High-quality photo saved");
    }

    private void applyStatusBadge(boolean active) {
        String text = active ? "ACTIVE" : "INACTIVE";
        statusValue.setText(text);
        statusValue.setForeground(active ? UITheme.SUCCESS : UITheme.DANGER);
        statusDetailValue.setText(text);
        statusDetailValue.setForeground(active ? UITheme.SUCCESS : UITheme.DANGER);
        statusBadge.setBackground(active ? new Color(20, 83, 45) : new Color(127, 29, 29));
        statusBadge.repaint();
    }

    private String valueOrDash(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private void choosePhoto() {
        File selected = openNativeImagePicker();
        if (selected == null) {
            return;
        }

        if (!isSupportedImage(selected)) {
            JOptionPane.showMessageDialog(this, "Choose a valid JPG, PNG, or GIF image.");
            return;
        }

        try {
            File stored = storeOriginalPhoto(selected);
            if (userDAO.updateProfilePhoto(userId, stored.getAbsolutePath())) {
                refreshUser();
                onProfileUpdated.run();
                JOptionPane.showMessageDialog(this, "Profile photo uploaded in original quality.");
            } else {
                JOptionPane.showMessageDialog(this, "Could not update profile photo.");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Could not save profile photo: " + ex.getMessage());
        }
    }

    private File openNativeImagePicker() {
        Window window = SwingUtilities.getWindowAncestor(this);
        Frame owner = window instanceof Frame frame ? frame : null;
        FileDialog dialog = new FileDialog(owner, "Upload profile photo", FileDialog.LOAD);
        dialog.setFilenameFilter(imageFilter());
        dialog.setVisible(true);
        if (dialog.getFile() == null) {
            return null;
        }
        return new File(dialog.getDirectory(), dialog.getFile());
    }

    private FilenameFilter imageFilter() {
        return (dir, name) -> {
            String lower = name.toLowerCase(Locale.ROOT);
            return lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png") || lower.endsWith(".gif");
        };
    }

    private boolean isSupportedImage(File file) {
        try (var input = ImageIO.createImageInputStream(file)) {
            if (file.length() > 10L * 1024 * 1024 || input == null) return false;
            Iterator<javax.imageio.ImageReader> readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext()) return false;
            javax.imageio.ImageReader reader = readers.next();
            try {
                reader.setInput(input, true, true);
                int width = reader.getWidth(0);
                int height = reader.getHeight(0);
                if (width <= 0 || height <= 0 || width > 6000 || height > 6000
                        || (long) width * height > 20_000_000L) return false;
                BufferedImage image = reader.read(0);
                return image != null;
            } finally {
                reader.dispose();
            }
        } catch (Exception ex) {
            return false;
        }
    }

    private File storeOriginalPhoto(File selected) throws Exception {
        if (Files.size(selected.toPath()) > 10L * 1024 * 1024) {
            throw new IllegalArgumentException("Image must be 10 MB or smaller.");
        }
        Path directory = com.shop.AppPaths.profilePhotoDirectory();
        String extension = extensionOf(selected.getName());
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        Path destination = Files.createTempFile(directory, "user-" + userId + "-" + timestamp + "-", extension);
        Files.copy(selected.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);
        return destination.toFile();
    }

    private String extensionOf(String name) {
        int dot = name.lastIndexOf('.');
        if (dot == -1 || dot == name.length() - 1) {
            return ".jpg";
        }
        return name.substring(dot).toLowerCase(Locale.ROOT);
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
            if (!com.shop.service.PasswordSecurity.isValidNewPassword(newPassword)
                    || !newPassword.equals(new String(confirm.getPassword()))) {
                JOptionPane.showMessageDialog(this, "New passwords must match and be at least 12 characters.");
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

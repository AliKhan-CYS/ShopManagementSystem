package com.shop.ui;

import com.shop.model.User;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public final class AvatarUtil {

    private AvatarUtil() {
    }

    public static ImageIcon avatarFor(User user, int size) {
        if (user != null && user.getProfilePhotoPath() != null && !user.getProfilePhotoPath().isBlank()) {
            File file = new File(user.getProfilePhotoPath());
            if (file.exists()) {
                Image image = new ImageIcon(file.getAbsolutePath()).getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH);
                return new ImageIcon(image);
            }
        }

        String initials = initials(user == null ? "U" : user.getUsername());
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(UITheme.ACCENT);
        g.fillOval(0, 0, size, size);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Segoe UI", Font.BOLD, Math.max(16, size / 3)));
        FontMetrics metrics = g.getFontMetrics();
        int x = (size - metrics.stringWidth(initials)) / 2;
        int y = ((size - metrics.getHeight()) / 2) + metrics.getAscent();
        g.drawString(initials, x, y);
        g.dispose();

        return new ImageIcon(image);
    }

    private static String initials(String username) {
        if (username == null || username.isBlank()) {
            return "U";
        }
        String cleaned = username.trim();
        return cleaned.substring(0, 1).toUpperCase();
    }
}
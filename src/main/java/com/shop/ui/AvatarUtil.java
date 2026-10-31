package com.shop.ui;

import com.shop.model.User;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Path;

public final class AvatarUtil {

    private AvatarUtil() {
    }

    public static ImageIcon avatarFor(User user, int size) {
        if (user != null && user.getProfilePhotoPath() != null && !user.getProfilePhotoPath().isBlank()) {
            try {
                Path photo = Path.of(user.getProfilePhotoPath()).toAbsolutePath().normalize();
                Path approvedDirectory = com.shop.AppPaths.profilePhotoDirectory().toAbsolutePath().normalize();
                if (photo.startsWith(approvedDirectory)) {
                    File file = photo.toFile();
                    if (file.isFile() && file.length() <= 10L * 1024 * 1024) {
                        ImageIcon icon = circularPhoto(file, size);
                        if (icon != null) return icon;
                    }
                }
            } catch (Exception ignored) {
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

    private static ImageIcon circularPhoto(File file, int size) {
        try {
            BufferedImage source = ImageIO.read(file);
            if (source == null || source.getWidth() <= 0 || source.getHeight() <= 0
                    || source.getWidth() > 6000 || source.getHeight() > 6000
                    || (long) source.getWidth() * source.getHeight() > 20_000_000L) {
                return null;
            }

            int sourceSize = Math.min(source.getWidth(), source.getHeight());
            int x = (source.getWidth() - sourceSize) / 2;
            int y = (source.getHeight() - sourceSize) / 2;
            BufferedImage cropped = source.getSubimage(x, y, sourceSize, sourceSize);

            BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = image.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.setClip(new java.awt.geom.Ellipse2D.Double(0, 0, size, size));
            g.drawImage(cropped, 0, 0, size, size, null);
            g.dispose();
            return new ImageIcon(image);
        } catch (Exception ex) {
            return null;
        }
    }

    private static String initials(String username) {
        if (username == null || username.isBlank()) {
            return "U";
        }
        String cleaned = username.trim();
        return cleaned.substring(0, 1).toUpperCase();
    }
}

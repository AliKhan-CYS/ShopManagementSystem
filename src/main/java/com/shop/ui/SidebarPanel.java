package com.shop.ui;

import com.shop.dao.AccountRequestDAO;
import com.shop.model.User;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class SidebarPanel extends JPanel {

    public interface NavigationListener {
        void onNavigate(String destination);
    }

    private final Map<String, JButton> buttons = new LinkedHashMap<>();
    private String selectedDestination = "Dashboard";

    public SidebarPanel(User user, NavigationListener listener) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setPreferredSize(new Dimension(230, 0));
        setBackground(UITheme.SURFACE);
        setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, UITheme.BORDER));

        add(createBrandBlock(user));
        add(Box.createVerticalStrut(12));

        add(createButton("Dashboard", listener));
        add(createButton("Profile", listener));
        add(createButton("Products", listener));

        if (user.isAdmin()) {
            add(createButton("Manage Users", listener));
            int pending = new AccountRequestDAO().countPendingRequests();
            add(createButton(pending > 0 ? "Notifications (" + pending + ")" : "Notifications", destination -> listener.onNavigate("Notifications")));
        }

        add(createButton("Customers", listener));
        add(createButton("Sales", listener));
        add(createButton("Reports", listener));

        add(Box.createVerticalGlue());
        add(createButton("Support", listener));
        add(Box.createVerticalStrut(14));

        select("Dashboard");
    }

    private JPanel createBrandBlock(User user) {
        JPanel brand = new JPanel();
        brand.setOpaque(false);
        brand.setLayout(new BoxLayout(brand, BoxLayout.Y_AXIS));
        brand.setBorder(BorderFactory.createEmptyBorder(18, 18, 10, 18));
        brand.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel name = new JLabel("Shop Desk");
        UITheme.styleSectionTitle(name);

        JLabel role = new JLabel(user.isAdmin() ? "Administrator workspace" : "Employee workspace");
        UITheme.styleMuted(role);

        brand.add(name);
        brand.add(Box.createRigidArea(new Dimension(0, 4)));
        brand.add(role);
        return brand;
    }

    private JButton createButton(String text, NavigationListener listener) {
        JButton btn = new JButton(text);
        buttons.put(text, btn);

        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setFocusPainted(false);
        btn.setOpaque(true);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, UITheme.SURFACE),
                BorderFactory.createEmptyBorder(10, 18, 10, 10)
        ));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (!isSelected(text)) {
                    btn.setBackground(UITheme.SURFACE_ALT);
                    btn.setForeground(Color.WHITE);
                }
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                applyButtonStyle(text, btn);
            }
        });

        btn.addActionListener(e -> {
            select(text);
            listener.onNavigate(text);
        });

        return btn;
    }

    private void select(String destination) {
        selectedDestination = normalize(destination);
        buttons.forEach(this::applyButtonStyle);
    }

    private void applyButtonStyle(String destination, JButton btn) {
        boolean active = isSelected(destination);
        btn.setBackground(active ? UITheme.ACCENT : UITheme.SURFACE);
        btn.setForeground(active ? Color.WHITE : UITheme.TEXT);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, active ? UITheme.SUCCESS : UITheme.SURFACE),
                BorderFactory.createEmptyBorder(10, 18, 10, 10)
        ));
    }

    private boolean isSelected(String destination) {
        return normalize(destination).equals(selectedDestination);
    }

    private String normalize(String destination) {
        return destination.startsWith("Notifications") ? "Notifications" : destination;
    }
}
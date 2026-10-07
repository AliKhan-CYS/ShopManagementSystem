package com.shop.ui;

import com.shop.dao.UserDAO;
import com.shop.model.User;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {

    private JPanel contentPanel;
    private JPanel headerPanel;
    private JLabel avatarLabel;
    private JLabel subtitle;
    private User user;
    private final UserDAO userDAO = new UserDAO();

    public MainFrame(User user) {
        this.user = user;

        UITheme.setup();

        setTitle("Shop Management System");
        setSize(1120, 700);
        setMinimumSize(new Dimension(980, 620));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        headerPanel = createHeader();
        add(headerPanel, BorderLayout.NORTH);

        SidebarPanel sidebar = new SidebarPanel(user, this::switchPanel);
        add(sidebar, BorderLayout.WEST);

        contentPanel = new JPanel(new BorderLayout());
        UITheme.styleRoot(contentPanel);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        add(contentPanel, BorderLayout.CENTER);

        switchPanel("Dashboard");
        setVisible(true);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout(16, 0));
        UITheme.styleSurface(header);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.BORDER),
                BorderFactory.createEmptyBorder(12, 18, 12, 18)
        ));

        JPanel titlePanel = new JPanel(new BorderLayout(12, 0));
        titlePanel.setOpaque(false);

        avatarLabel = new JLabel(AvatarUtil.avatarFor(user, 44));

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Shop Management System");
        UITheme.styleSectionTitle(title);

        subtitle = new JLabel();
        UITheme.styleMuted(subtitle);
        refreshHeaderUser();

        text.add(title);
        text.add(Box.createRigidArea(new Dimension(0, 3)));
        text.add(subtitle);

        titlePanel.add(avatarLabel, BorderLayout.WEST);
        titlePanel.add(text, BorderLayout.CENTER);

        JButton logoutBtn = new JButton("Logout");
        UITheme.styleButton(logoutBtn);
        logoutBtn.addActionListener(e -> logout());

        header.add(titlePanel, BorderLayout.WEST);
        header.add(logoutBtn, BorderLayout.EAST);

        return header;
    }

    private void switchPanel(String name) {
        contentPanel.removeAll();

        switch (name) {
            case "Dashboard" -> contentPanel.add(new DashboardPanel(user), BorderLayout.CENTER);
            case "Profile" -> contentPanel.add(new ProfilePanel(user, this::refreshUser), BorderLayout.CENTER);
            case "Products" -> contentPanel.add(new ProductPanel(user), BorderLayout.CENTER);
            case "Manage Users" -> new ManageUsersFrame(user);
            case "Notifications" -> contentPanel.add(new AdminNotificationsPanel(), BorderLayout.CENTER);
            case "Customers" -> contentPanel.add(new CustomerPanel(user), BorderLayout.CENTER);
            case "Sales" -> contentPanel.add(new SalesPanel(user), BorderLayout.CENTER);
            case "Reports" -> contentPanel.add(new ReportPanel(), BorderLayout.CENTER);
            case "Support" -> contentPanel.add(new SupportPanel(user), BorderLayout.CENTER);
            default -> contentPanel.add(new JLabel("Coming Soon...", SwingConstants.CENTER), BorderLayout.CENTER);
        }

        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void refreshUser() {
        User latest = userDAO.findById(user.getId());
        if (latest != null) {
            user = latest;
            refreshHeaderUser();
        }
    }

    private void refreshHeaderUser() {
        if (avatarLabel != null) {
            avatarLabel.setIcon(AvatarUtil.avatarFor(user, 44));
        }
        if (subtitle != null) {
            subtitle.setText("Signed in as " + user.getUsername() + " - " + user.getRole());
        }
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Logout from " + user.getUsername() + "?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            new LoginFrame();
            dispose();
        }
    }
}
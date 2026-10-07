package com.shop.ui;

import com.shop.model.User;

import javax.swing.*;
import java.awt.*;

public class SupportPanel extends JPanel {

    public SupportPanel(User user) {
        setLayout(new BorderLayout(0, 18));
        UITheme.styleRoot(this);

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Support");
        UITheme.styleTitle(title);
        JLabel subtitle = new JLabel("Quick help for daily shop operations and account access.");
        UITheme.styleMuted(subtitle);

        header.add(title);
        header.add(Box.createRigidArea(new Dimension(0, 6)));
        header.add(subtitle);
        add(header, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(1, 3, 14, 0));
        grid.setOpaque(false);
        grid.add(createSupportCard("Account Help", "Ask an administrator to create users, reset passwords, or update roles."));
        grid.add(createSupportCard("Daily Workflow", "Start with products, add customers, record sales, then review reports."));
        grid.add(createSupportCard("Signed In", "Current user: " + user.getUsername() + " (" + user.getRole() + ")"));

        add(grid, BorderLayout.CENTER);
    }

    private JPanel createSupportCard(String titleText, String bodyText) {
        JPanel card = new JPanel(new BorderLayout(0, 10));
        UITheme.styleCard(card);

        JLabel title = new JLabel(titleText);
        UITheme.styleSectionTitle(title);

        JTextArea body = new JTextArea(bodyText);
        body.setWrapStyleWord(true);
        body.setLineWrap(true);
        body.setEditable(false);
        body.setFocusable(false);
        body.setOpaque(false);
        body.setForeground(UITheme.MUTED_TEXT);
        body.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        card.add(title, BorderLayout.NORTH);
        card.add(body, BorderLayout.CENTER);
        return card;
    }
}

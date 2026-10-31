package com.shop.ui;

import com.shop.dao.ReportDAO;
import com.shop.model.User;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import javax.swing.*;
import java.awt.*;

public class DashboardPanel extends JPanel {

    private final ReportDAO reports = new ReportDAO();
    private final User user;
    private final JPanel centerPanel = new JPanel(new GridLayout(1, 2, 15, 15));
    private final JLabel productCountValue = cardValueLabel();
    private final JLabel stockValue = cardValueLabel();
    private final JLabel salesCountValue = cardValueLabel();
    private final JLabel revenueTodayValue = cardValueLabel();

    public DashboardPanel(User user) {
        this.user = user;

        setLayout(new BorderLayout(15, 15));
        UITheme.styleRoot(this);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        add(createHeader(), BorderLayout.NORTH);

        centerPanel.setOpaque(false);
        add(centerPanel, BorderLayout.CENTER);

        JPanel cards = new JPanel(new GridLayout(1, 4, 15, 15));
        cards.setOpaque(false);
        cards.add(createCard("Total Products", productCountValue));
        cards.add(createCard("Units in Stock", stockValue));
        cards.add(createCard("Total Sales", salesCountValue));
        cards.add(createCard("Revenue Today", revenueTodayValue));
        add(cards, BorderLayout.SOUTH);

        loadDashboard();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setOpaque(false);

        JPanel profile = new JPanel(new BorderLayout(12, 0));
        profile.setOpaque(false);
        profile.add(new JLabel(AvatarUtil.avatarFor(user, 56)), BorderLayout.WEST);

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));

        JLabel welcome = new JLabel("Welcome, " + user.getUsername());
        UITheme.styleTitle(welcome);
        JLabel subtitle = new JLabel(user.getRole() + " dashboard");
        UITheme.styleMuted(subtitle);
        text.add(welcome);
        text.add(Box.createRigidArea(new Dimension(0, 4)));
        text.add(subtitle);
        profile.add(text, BorderLayout.CENTER);

        JButton refreshBtn = new JButton("Refresh");
        UITheme.stylePrimaryButton(refreshBtn);
        refreshBtn.addActionListener(e -> loadDashboard());

        header.add(profile, BorderLayout.WEST);
        header.add(refreshBtn, BorderLayout.EAST);
        return header;
    }

    private void loadDashboard() {
        ReportDAO.InventorySummary inventory = reports.inventorySummary();
        ReportDAO.SalesSummary sales = reports.salesSummary();
        productCountValue.setText(String.valueOf(inventory.productCount()));
        stockValue.setText(String.valueOf(inventory.totalQuantity()));
        salesCountValue.setText(String.valueOf(sales.saleCount()));
        revenueTodayValue.setText(Money.format(sales.revenueToday()));

        centerPanel.removeAll();
        centerPanel.add(createCategoryChart());
        centerPanel.add(createSalesChart());
        centerPanel.revalidate();
        centerPanel.repaint();
    }

    private ChartPanel createCategoryChart() {
        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
        reports.stockByCategory().forEach(dataset::setValue);

        JFreeChart chart = ChartFactory.createPieChart("Stock by Category", dataset, true, true, false);
        return UITheme.chartPanel(chart);
    }

    private ChartPanel createSalesChart() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        reports.dailyRevenue().forEach((day, revenue) -> dataset.addValue(revenue, "Sales", day));

        JFreeChart chart = ChartFactory.createLineChart("Sales Report", "Date", "Revenue", dataset);
        return UITheme.chartPanel(chart);
    }

    private static JLabel cardValueLabel() {
        JLabel label = new JLabel("0");
        label.setFont(new Font("Segoe UI", Font.BOLD, 26));
        label.setForeground(UITheme.TEXT);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        return label;
    }

    private JPanel createCard(String title, JLabel valueLabel) {
        JPanel card = new JPanel(new BorderLayout());
        UITheme.styleCard(card);

        JLabel titleLabel = new JLabel(title);
        UITheme.styleMuted(titleLabel);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }
}

package com.shop.ui;

import com.shop.dao.SaleDAO;
import com.shop.model.Product;
import com.shop.model.User;
import com.shop.service.ProductService;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DashboardPanel extends JPanel {

    private final ProductService service = new ProductService();
    private final User user;
    private JPanel centerPanel;

    public DashboardPanel(User user) {
        this.user = user;

        setLayout(new BorderLayout(15, 15));
        UITheme.styleRoot(this);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        add(createHeader(), BorderLayout.NORTH);

        centerPanel = new JPanel(new GridLayout(1, 2, 15, 15));
        centerPanel.setOpaque(false);
        add(centerPanel, BorderLayout.CENTER);

        JPanel cards = new JPanel(new GridLayout(1, 3, 15, 15));
        cards.setOpaque(false);

        cards.add(createCard("Total Products", getTotalProducts()));
        cards.add(createCard("Total Quantity", getTotalQuantity()));
        cards.add(createCard("Sales (Coming)", 0));

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
        centerPanel.removeAll();
        centerPanel.add(createCategoryChart());
        centerPanel.add(createSalesChart());
        refreshUI();
    }

    private ChartPanel createCategoryChart() {
        List<Product> list = service.getAllProducts();
        Map<String, Integer> categoryMap = new HashMap<>();

        for (Product p : list) {
            categoryMap.put(
                    p.getCategory(),
                    categoryMap.getOrDefault(p.getCategory(), 0) + p.getQuantity()
            );
        }

        DefaultPieDataset dataset = new DefaultPieDataset();
        for (String key : categoryMap.keySet()) {
            dataset.setValue(key, categoryMap.get(key));
        }

        JFreeChart chart = ChartFactory.createPieChart(
                "Products by Category",
                dataset,
                true,
                true,
                false
        );

        return UITheme.chartPanel(chart);
    }

    private ChartPanel createSalesChart() {
        SaleDAO saleDAO = new SaleDAO();
        List<Object[]> data = saleDAO.getSalesByDate();

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (Object[] row : data) {
            String date = (String) row[0];
            double total = (double) row[1];
            dataset.addValue(total, "Sales", date);
        }

        JFreeChart chart = ChartFactory.createLineChart(
                "Sales Report",
                "Date",
                "Revenue",
                dataset
        );

        return UITheme.chartPanel(chart);
    }

    private JPanel createCard(String title, int value) {
        JPanel card = new JPanel(new BorderLayout());
        UITheme.styleCard(card);

        JLabel titleLabel = new JLabel(title);
        UITheme.styleMuted(titleLabel);

        JLabel valueLabel = new JLabel(String.valueOf(value));
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        valueLabel.setForeground(UITheme.TEXT);
        valueLabel.setHorizontalAlignment(SwingConstants.CENTER);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }

    private int getTotalProducts() {
        return service.getAllProducts().size();
    }

    private int getTotalQuantity() {
        int total = 0;
        for (Product p : service.getAllProducts()) {
            total += p.getQuantity();
        }
        return total;
    }

    private void refreshUI() {
        centerPanel.revalidate();
        centerPanel.repaint();
    }
}
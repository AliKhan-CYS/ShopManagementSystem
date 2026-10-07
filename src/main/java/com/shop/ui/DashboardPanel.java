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

        // ================= HEADER =================
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel welcome = new JLabel("Dashboard");
        UITheme.styleTitle(welcome);

        JButton refreshBtn = new JButton("Refresh");
        UITheme.stylePrimaryButton(refreshBtn);

        header.add(welcome, BorderLayout.WEST);
        header.add(refreshBtn, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        // ================= CENTER =================
        centerPanel = new JPanel(new GridLayout(1, 2, 15, 15));
        centerPanel.setOpaque(false);
        add(centerPanel, BorderLayout.CENTER);

        // ================= CARDS =================
        JPanel cards = new JPanel(new GridLayout(1, 3, 15, 15));
        cards.setOpaque(false);

        cards.add(createCard("Total Products", getTotalProducts()));
        cards.add(createCard("Total Quantity", getTotalQuantity()));
        cards.add(createCard("Sales (Coming)", 0));

        add(cards, BorderLayout.SOUTH);

        loadDashboard();

        refreshBtn.addActionListener(e -> loadDashboard());
    }

    // ================= LOAD DASHBOARD =================
    private void loadDashboard() {
        centerPanel.removeAll();

        // LEFT → product category chart
        centerPanel.add(createCategoryChart());

        // RIGHT → sales by date chart
        centerPanel.add(createSalesChart());

        refreshUI();
    }

    // ================= CATEGORY CHART =================
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

    // ================= SALES CHART =================
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

    // ================= CARD =================
    private JPanel createCard(String title, int value) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(new Color(245, 247, 250));
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

    // ================= CALCULATIONS =================
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

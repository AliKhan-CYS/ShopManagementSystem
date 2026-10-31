package com.shop.ui;

import com.shop.dao.ReportDAO;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import javax.swing.*;
import java.awt.*;

public class ReportPanel extends JPanel {

    private final ReportDAO reports = new ReportDAO();

    public ReportPanel() {
        setLayout(new GridLayout(1, 3, 10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        UITheme.styleRoot(this);

        add(createProductSalesChart());
        add(createCategorySalesChart());
        add(createDailyRevenueChart());
    }

    private ChartPanel createProductSalesChart() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        reports.quantitySoldByProduct().forEach((product, qty) -> dataset.addValue(qty, "Qty", product));

        JFreeChart chart = ChartFactory.createBarChart("Sales per Product", "Product", "Quantity Sold", dataset);
        return UITheme.chartPanel(chart);
    }

    private ChartPanel createCategorySalesChart() {
        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
        reports.quantitySoldByCategory().forEach(dataset::setValue);

        JFreeChart chart = ChartFactory.createPieChart("Sales by Category", dataset, true, true, false);
        return UITheme.chartPanel(chart);
    }

    private ChartPanel createDailyRevenueChart() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        reports.dailyRevenue().forEach((day, revenue) -> dataset.addValue(revenue, "Revenue", day));

        JFreeChart chart = ChartFactory.createLineChart("Daily Revenue", "Date", "Revenue", dataset);
        return UITheme.chartPanel(chart);
    }
}

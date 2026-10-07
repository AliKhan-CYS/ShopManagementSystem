package com.shop.ui;

import com.shop.service.ReportService;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class ReportPanel extends JPanel {

    private final ReportService service = new ReportService();

    public ReportPanel() {
        setLayout(new GridLayout(1, 3, 10, 10));
        setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        UITheme.styleRoot(this);

        add(createProductSalesChart());
        add(createCategorySalesChart());
        add(createDailyRevenueChart());
    }

    private ChartPanel createProductSalesChart() {

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        Map<String, Integer> productSales = service.getProductSalesQuantity();

        for(String product: productSales.keySet()) {
            dataset.addValue(productSales.get(product), "Qty", product);
        }

        JFreeChart chart = ChartFactory.createBarChart(
                "Sales per Product",
                "Product",
                "Quantity Sold",
                dataset
        );

        return UITheme.chartPanel(chart);
    }

    private ChartPanel createCategorySalesChart() {

        DefaultPieDataset dataset = new DefaultPieDataset();

        Map<String, Integer> catSales = service.getCategorySalesQuantity();

        for(String cat: catSales.keySet()) {
            dataset.setValue(cat, catSales.get(cat));
        }

        JFreeChart chart = ChartFactory.createPieChart(
                "Sales by Category",
                dataset,
                true,
                true,
                false
        );

        return UITheme.chartPanel(chart);
    }

    private ChartPanel createDailyRevenueChart() {

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        Map<String, Double> revenue = service.getDailyRevenue();

        for(String day: revenue.keySet()) {
            dataset.addValue(revenue.get(day), "Revenue", day);
        }

        JFreeChart chart = ChartFactory.createLineChart(
                "Daily Revenue",
                "Date",
                "Revenue",
                dataset
        );

        return UITheme.chartPanel(chart);
    }
}

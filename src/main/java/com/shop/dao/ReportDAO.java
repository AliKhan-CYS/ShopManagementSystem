package com.shop.dao;

import com.shop.db.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

public class ReportDAO {

    public record SalesSummary(int saleCount, double revenue, double revenueToday) {
    }

    public record InventorySummary(int productCount, int totalQuantity) {
    }

    public InventorySummary inventorySummary() {
        String sql = "SELECT COUNT(*) AS product_count, COALESCE(SUM(quantity), 0) AS total_quantity FROM products";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return new InventorySummary(rs.getInt("product_count"), rs.getInt("total_quantity"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return new InventorySummary(0, 0);
    }

    public Map<String, Integer> quantitySoldByProduct() {
        return quantityMap("""
                SELECT p.name AS label, COALESCE(SUM(si.quantity), 0) AS qty
                FROM products p
                LEFT JOIN sale_items si ON si.product_id = p.id
                GROUP BY p.id, p.name
                ORDER BY qty DESC, p.name
                """);
    }

    public Map<String, Integer> quantitySoldByCategory() {
        return quantityMap("""
                SELECT p.category AS label, COALESCE(SUM(si.quantity), 0) AS qty
                FROM products p
                LEFT JOIN sale_items si ON si.product_id = p.id
                GROUP BY p.category
                ORDER BY qty DESC, p.category
                """);
    }

    public Map<String, Integer> stockByCategory() {
        return quantityMap("""
                SELECT category AS label, SUM(quantity) AS qty
                FROM products
                GROUP BY category
                ORDER BY category
                """);
    }

    // sale_date is stored in UTC; group by the shop's local calendar day.
    public Map<String, Double> dailyRevenue() {
        Map<String, Double> map = new LinkedHashMap<>();
        String sql = """
                SELECT DATE(sale_date, 'localtime') AS day, SUM(total) AS revenue
                FROM sales
                GROUP BY day
                ORDER BY day
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                map.put(rs.getString("day"), rs.getDouble("revenue"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return map;
    }

    public SalesSummary salesSummary() {
        String sql = """
                SELECT COUNT(*) AS sale_count,
                       COALESCE(SUM(total), 0) AS revenue,
                       COALESCE(SUM(CASE WHEN DATE(sale_date, 'localtime') = DATE('now', 'localtime')
                                         THEN total END), 0) AS revenue_today
                FROM sales
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return new SalesSummary(rs.getInt("sale_count"), rs.getDouble("revenue"), rs.getDouble("revenue_today"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return new SalesSummary(0, 0, 0);
    }

    private Map<String, Integer> quantityMap(String sql) {
        Map<String, Integer> map = new LinkedHashMap<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                map.merge(rs.getString("label"), rs.getInt("qty"), Integer::sum);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return map;
    }
}

package com.shop.dao;

import com.shop.db.DBConnection;
import com.shop.model.Sale;
import com.shop.model.SaleItem;

import java.sql.*;
import java.util.List;

public class SaleDAO {

    public int createSale(Sale sale, List<SaleItem> items) {

        if (sale == null || items == null || items.isEmpty() || sale.getCustomerId() <= 0
                || !Double.isFinite(sale.getTotal()) || sale.getTotal() < 0) return -1;
        double calculatedTotal = 0;
        for (SaleItem item : items) {
            if (item == null || item.getProductId() <= 0 || item.getQuantity() <= 0
                    || !Double.isFinite(item.getPrice()) || item.getPrice() < 0) return -1;
            calculatedTotal += item.getPrice() * item.getQuantity();
        }
        if (!Double.isFinite(calculatedTotal) || Math.abs(calculatedTotal - sale.getTotal()) > 0.01) return -1;

        String saleSQL =
                "INSERT INTO sales(customer_id, total) VALUES (?, ?)";

        String itemSQL =
                "INSERT INTO sale_items(sale_id, product_id, quantity, price) VALUES (?, ?, ?, ?)";

        String stockSQL =
                "UPDATE products SET quantity = quantity - ? WHERE id=? AND quantity >= ?";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
            int saleId = 0;
            try (PreparedStatement saleStmt = conn.prepareStatement(saleSQL, Statement.RETURN_GENERATED_KEYS)) {
                saleStmt.setInt(1, sale.getCustomerId());
                saleStmt.setDouble(2, sale.getTotal());
                saleStmt.executeUpdate();
                try (ResultSet rs = saleStmt.getGeneratedKeys()) {
                    if (rs.next()) saleId = rs.getInt(1);
                }
            }
            if (saleId <= 0) throw new SQLException("Sale id was not created");

            try (PreparedStatement itemStmt = conn.prepareStatement(itemSQL);
                 PreparedStatement stockStmt = conn.prepareStatement(stockSQL)) {
                for (SaleItem item : items) {
                    itemStmt.setInt(1, saleId);
                    itemStmt.setInt(2, item.getProductId());
                    itemStmt.setInt(3, item.getQuantity());
                    itemStmt.setDouble(4, item.getPrice());
                    itemStmt.addBatch();
                    stockStmt.setInt(1, item.getQuantity());
                    stockStmt.setInt(2, item.getProductId());
                    stockStmt.setInt(3, item.getQuantity());
                    stockStmt.addBatch();
                }
                itemStmt.executeBatch();
                for (int count : stockStmt.executeBatch()) {
                    if (count == 0 || count == Statement.EXECUTE_FAILED) throw new SQLException("Insufficient product stock");
                }
            }

            conn.commit();
            return saleId;
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        } catch (Exception e) {
            return -1;
        }
    }
}

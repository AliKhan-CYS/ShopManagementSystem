package com.shop.ui;

import com.shop.dao.ProductDAO;
import com.shop.model.Product;
import com.shop.model.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProductPanel extends JPanel {

    private final ProductDAO dao = new ProductDAO();
    private final User user;
    private final DefaultTableModel model;
    private final JTable table;
    private final JTextField searchField = new JTextField(20);
    private final List<Product> shownProducts = new ArrayList<>();

    public ProductPanel(User user) {
        this.user = user;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        UITheme.styleRoot(this);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        top.setOpaque(false);

        JButton searchBtn = new JButton("Search");
        JButton resetBtn = new JButton("Reset");
        UITheme.styleField(searchField);
        UITheme.stylePrimaryButton(searchBtn);
        UITheme.styleButton(resetBtn);

        JLabel searchLabel = new JLabel("Search:");
        UITheme.styleLabel(searchLabel);
        top.add(searchLabel);
        top.add(searchField);
        top.add(searchBtn);
        top.add(resetBtn);
        add(top, BorderLayout.NORTH);

        model = new DefaultTableModel(new String[]{"ID", "Name", "Price", "Qty", "Category"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(model);
        UITheme.styleTable(table);
        add(UITheme.wrapTable(table), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        bottom.setOpaque(false);

        JButton addBtn = new JButton("Add");
        JButton editBtn = new JButton("Edit");
        JButton deleteBtn = new JButton("Delete");
        UITheme.stylePrimaryButton(addBtn);
        UITheme.styleButton(editBtn);
        UITheme.styleDangerButton(deleteBtn);
        bottom.add(addBtn);
        bottom.add(editBtn);
        bottom.add(deleteBtn);
        add(bottom, BorderLayout.SOUTH);

        if (!user.isAdmin()) {
            editBtn.setEnabled(false);
            deleteBtn.setEnabled(false);
        }

        addBtn.addActionListener(e -> addProduct());
        editBtn.addActionListener(e -> editProduct());
        deleteBtn.addActionListener(e -> deleteProduct());
        searchBtn.addActionListener(e -> searchProduct());
        searchField.addActionListener(e -> searchProduct());
        resetBtn.addActionListener(e -> {
            searchField.setText("");
            loadProducts();
        });

        loadProducts();
    }

    private void loadProducts() {
        showProducts(dao.getAllProducts());
    }

    private void showProducts(List<Product> products) {
        shownProducts.clear();
        shownProducts.addAll(products);
        model.setRowCount(0);
        for (Product p : products) {
            model.addRow(new Object[]{p.getId(), p.getName(), Money.format(p.getPrice()), p.getQuantity(), p.getCategory()});
        }
    }

    private void searchProduct() {
        String keyword = searchField.getText().trim().toLowerCase(Locale.ROOT);
        if (keyword.isEmpty()) {
            loadProducts();
            return;
        }

        List<Product> result = new ArrayList<>();
        for (Product p : dao.getAllProducts()) {
            if (p.getName().toLowerCase(Locale.ROOT).contains(keyword)
                    || p.getCategory().toLowerCase(Locale.ROOT).contains(keyword)) {
                result.add(p);
            }
        }

        showProducts(result);
        if (result.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No product found");
        }
    }

    private void addProduct() {
        Product product = showProductDialog("Add Product", null);
        if (product == null) return;

        if (dao.addProduct(product)) {
            JOptionPane.showMessageDialog(this, "Product added");
            loadProducts();
        } else {
            JOptionPane.showMessageDialog(this, "Product could not be added.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void editProduct() {
        if (!user.isAdmin()) return;
        Product selected = selectedProduct();
        if (selected == null) return;

        Product product = showProductDialog("Edit Product", selected);
        if (product == null) return;

        if (dao.updateProduct(product)) {
            JOptionPane.showMessageDialog(this, "Product updated");
            loadProducts();
        } else {
            JOptionPane.showMessageDialog(this, "Product could not be updated.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteProduct() {
        if (!user.isAdmin()) return;
        Product selected = selectedProduct();
        if (selected == null) return;

        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete \"" + selected.getName() + "\"?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        if (dao.deleteProduct(selected.getId())) {
            JOptionPane.showMessageDialog(this, "Product deleted");
            loadProducts();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Product could not be deleted. Products that appear in recorded sales are kept for billing history.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Product selectedProduct() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a product first");
            return null;
        }
        return shownProducts.get(table.convertRowIndexToModel(row));
    }

    private Product showProductDialog(String title, Product existing) {
        JTextField name = new JTextField(existing == null ? "" : existing.getName());
        JTextField price = new JTextField(existing == null ? "" : String.valueOf(existing.getPrice()));
        JTextField qty = new JTextField(existing == null ? "" : String.valueOf(existing.getQuantity()));
        JTextField category = new JTextField(existing == null ? "" : existing.getCategory());

        Object[] fields = {"Name:", name, "Price:", price, "Quantity:", qty, "Category:", category};

        while (JOptionPane.showConfirmDialog(this, fields, title, JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            String n = name.getText().trim();
            String c = category.getText().trim();
            double p;
            int q;
            try {
                p = Double.parseDouble(price.getText().trim());
                q = Integer.parseInt(qty.getText().trim());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Price must be a number and quantity a whole number.");
                continue;
            }

            if (n.isEmpty() || c.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Name and category are required.");
            } else if (n.length() > 120 || c.length() > 80) {
                JOptionPane.showMessageDialog(this, "Name must be at most 120 characters and category at most 80.");
            } else if (!Double.isFinite(p) || p < 0 || q < 0) {
                JOptionPane.showMessageDialog(this, "Price and quantity cannot be negative.");
            } else {
                return existing == null ? new Product(n, p, q, c) : new Product(existing.getId(), n, p, q, c);
            }
        }
        return null;
    }
}

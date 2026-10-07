package com.shop.ui;

import com.shop.model.Product;
import com.shop.model.User;
import com.shop.service.ProductService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ProductPanel extends JPanel {

    private JTable table;
    private DefaultTableModel model;
    private final ProductService service = new ProductService();
    private final User user;

    private JTextField searchField;

    public ProductPanel(User user) {

        this.user = user;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        UITheme.styleRoot(this);

        // TOP PANEL (SEARCH)
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        top.setOpaque(false);

        searchField = new JTextField(20);
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

        // TABLE
        model = new DefaultTableModel(
                new String[]{"ID", "Name", "Price", "Qty", "Category"}, 0
        ) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(model);
        UITheme.styleTable(table);

        add(UITheme.wrapTable(table), BorderLayout.CENTER);

        //  BUTTONS
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

        //  ROLE CONTROL
        if (!user.isAdmin()) {
            editBtn.setEnabled(false);
            deleteBtn.setEnabled(false);
        }

        // ACTIONS
        addBtn.addActionListener(e -> addProduct());
        editBtn.addActionListener(e -> editProduct());
        deleteBtn.addActionListener(e -> deleteProduct());
        searchBtn.addActionListener(e -> searchProduct());
        resetBtn.addActionListener(e -> {
            searchField.setText("");
            loadProducts();
        });

        //  ENTER KEY SUPPORT
        searchField.addActionListener(e -> searchProduct());

        loadProducts();
    }

    //  LOAD
    private void loadProducts() {

        model.setRowCount(0);

        List<Product> list = service.getAllProducts();

        for (Product p : list) {
            model.addRow(new Object[]{
                    p.getId(),
                    p.getName(),
                    p.getPrice(),
                    p.getQuantity(),
                    p.getCategory()
            });
        }
    }

    // SEARCH
    private void searchProduct() {

        String keyword = searchField.getText().trim().toLowerCase();

        if (keyword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter search text");
            return;
        }

        model.setRowCount(0);

        List<Product> all = service.getAllProducts();
        List<Product> result = new ArrayList<>();

        for (Product p : all) {
            if (p.getName().toLowerCase().contains(keyword) ||
                    p.getCategory().toLowerCase().contains(keyword)) {
                result.add(p);
            }
        }

        if (result.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No product found");
            return;
        }

        for (Product p : result) {
            model.addRow(new Object[]{
                    p.getId(),
                    p.getName(),
                    p.getPrice(),
                    p.getQuantity(),
                    p.getCategory()
            });
        }
    }

    // ADD
    private void addProduct() {

        JTextField name = new JTextField();
        JTextField price = new JTextField();
        JTextField qty = new JTextField();
        JTextField category = new JTextField();

        Object[] fields = {
                "Name:", name,
                "Price:", price,
                "Quantity:", qty,
                "Category:", category
        };

        int option = JOptionPane.showConfirmDialog(
                this, fields, "Add Product", JOptionPane.OK_CANCEL_OPTION
        );

        if (option == JOptionPane.OK_OPTION) {
            try {
                String n = name.getText().trim();
                double p = Double.parseDouble(price.getText().trim());
                int q = Integer.parseInt(qty.getText().trim());
                String c = category.getText().trim();

                if (n.isEmpty() || c.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Fields cannot be empty");
                    return;
                }

                service.addProduct(new Product(n, p, q, c));
                JOptionPane.showMessageDialog(this, "Product Added");
                loadProducts();

            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Invalid input!");
            }
        }
    }

    //  EDIT
    private void editProduct() {

        if (!user.isAdmin()) {
            JOptionPane.showMessageDialog(this, "Access Denied!");
            return;
        }

        int row = table.getSelectedRow();

        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a product first");
            return;
        }

        int id = (int) model.getValueAt(row, 0);

        JTextField name = new JTextField(model.getValueAt(row, 1).toString());
        JTextField price = new JTextField(model.getValueAt(row, 2).toString());
        JTextField qty = new JTextField(model.getValueAt(row, 3).toString());
        JTextField category = new JTextField(model.getValueAt(row, 4).toString());

        Object[] fields = {
                "Name:", name,
                "Price:", price,
                "Quantity:", qty,
                "Category:", category
        };

        int option = JOptionPane.showConfirmDialog(
                this, fields, "Edit Product", JOptionPane.OK_CANCEL_OPTION
        );

        if (option == JOptionPane.OK_OPTION) {
            try {
                service.updateProduct(new Product(
                        id,
                        name.getText().trim(),
                        Double.parseDouble(price.getText().trim()),
                        Integer.parseInt(qty.getText().trim()),
                        category.getText().trim()
                ));

                JOptionPane.showMessageDialog(this, "Updated Successfully");
                loadProducts();

            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Invalid input!");
            }
        }
    }

    // DELETE
    private void deleteProduct() {

        if (!user.isAdmin()) {
            JOptionPane.showMessageDialog(this, "Access Denied!");
            return;
        }

        int row = table.getSelectedRow();

        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a product first");
            return;
        }

        int id = (int) model.getValueAt(row, 0);

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete?",
                "Confirm",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            service.deleteProduct(id);
            JOptionPane.showMessageDialog(this, "Deleted Successfully");
            loadProducts();
        }
    }
}

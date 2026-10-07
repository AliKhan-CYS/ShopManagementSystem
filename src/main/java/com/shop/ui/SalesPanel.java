package com.shop.ui;

import com.shop.dao.CustomerDAO;
import com.shop.dao.ProductDAO;
import com.shop.dao.SaleDAO;
import com.shop.model.Customer;
import com.shop.model.Product;
import com.shop.model.Sale;
import com.shop.model.SaleItem;
import com.shop.model.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class SalesPanel extends JPanel {

    private final User user;

    private JComboBox<Customer> customerBox;
    private JComboBox<Product> productBox;

    private JTextField qtyField;

    private JTable table;
    private DefaultTableModel model;

    private JLabel totalLabel;

    private double total = 0;

    private final CustomerDAO customerDAO = new CustomerDAO();
    private final ProductDAO productDAO = new ProductDAO();
    private final SaleDAO saleDAO = new SaleDAO();

    private final List<SaleItem> saleItems = new ArrayList<>();

    public SalesPanel(User user) {

        this.user = user;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        UITheme.styleRoot(this);

        // ================= TOP =================
        JPanel top = new JPanel(new GridLayout(2, 1, 10, 10));
        top.setOpaque(false);

        // CUSTOMER PANEL
        JPanel customerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        customerPanel.setOpaque(false);

        customerBox = new JComboBox<>();
        customerBox.setPreferredSize(new Dimension(250, 30));
        UITheme.styleCombo(customerBox);

        loadCustomers();

        JLabel customerLabel = new JLabel("Customer:");
        UITheme.styleLabel(customerLabel);
        customerPanel.add(customerLabel);
        customerPanel.add(customerBox);

        top.add(customerPanel);

        // PRODUCT PANEL
        JPanel productPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        productPanel.setOpaque(false);

        productBox = new JComboBox<>();
        productBox.setPreferredSize(new Dimension(250, 30));
        UITheme.styleCombo(productBox);

        loadProducts();

        qtyField = new JTextField(5);
        UITheme.styleField(qtyField);

        JButton addBtn = new JButton("Add Item");
        UITheme.stylePrimaryButton(addBtn);

        JLabel productLabel = new JLabel("Product:");
        UITheme.styleLabel(productLabel);
        productPanel.add(productLabel);
        productPanel.add(productBox);

        JLabel qtyLabel = new JLabel("Qty:");
        UITheme.styleLabel(qtyLabel);
        productPanel.add(qtyLabel);
        productPanel.add(qtyField);

        productPanel.add(addBtn);

        top.add(productPanel);

        add(top, BorderLayout.NORTH);

        // ================= TABLE =================
        model = new DefaultTableModel(
                new String[]{"Product", "Qty", "Price", "Subtotal"},
                0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(model);
        UITheme.styleTable(table);

        add(UITheme.wrapTable(table), BorderLayout.CENTER);

        // ================= BOTTOM =================
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);

        totalLabel = new JLabel("Total: Rs. 0.0");
        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        totalLabel.setForeground(UITheme.TEXT);

        JButton billBtn = new JButton("Generate Bill");
        UITheme.stylePrimaryButton(billBtn);

        bottom.add(totalLabel, BorderLayout.WEST);
        bottom.add(billBtn, BorderLayout.EAST);

        add(bottom, BorderLayout.SOUTH);

        // ================= ROLE CONTROL =================
        if (!user.isAdmin()) {

            addBtn.setEnabled(false);
            billBtn.setEnabled(false);

            productBox.setEnabled(false);
            qtyField.setEnabled(false);
        }

        // ================= ACTIONS =================
        addBtn.addActionListener(e -> addItem());

        billBtn.addActionListener(e -> generateBill());
    }

    // ================= LOAD CUSTOMERS =================
    private void loadCustomers() {

        List<Customer> customers =
                customerDAO.getAllCustomers();

        customerBox.removeAllItems();

        for (Customer c : customers) {
            customerBox.addItem(c);
        }
    }

    // ================= LOAD PRODUCTS =================
    private void loadProducts() {

        List<Product> products =
                productDAO.getAllProducts();

        productBox.removeAllItems();

        for (Product p : products) {
            productBox.addItem(p);
        }
    }

    // ================= ADD ITEM =================
    private void addItem() {

        try {

            Product p =
                    (Product) productBox.getSelectedItem();

            if (p == null) {
                JOptionPane.showMessageDialog(
                        this,
                        "No product selected"
                );
                return;
            }

            int qty =
                    Integer.parseInt(qtyField.getText().trim());

            if (qty <= 0) {

                JOptionPane.showMessageDialog(
                        this,
                        "Invalid quantity"
                );

                return;
            }

            if (qty > p.getQuantity()) {

                JOptionPane.showMessageDialog(
                        this,
                        "Not enough stock"
                );

                return;
            }

            double subtotal =
                    qty * p.getPrice();

            total += subtotal;

            totalLabel.setText(
                    "Total: Rs. " + total
            );

            model.addRow(new Object[]{
                    p.getName(),
                    qty,
                    p.getPrice(),
                    subtotal
            });

            saleItems.add(
                    new SaleItem(
                            p.getId(),
                            qty,
                            p.getPrice()
                    )
            );

            qtyField.setText("");

        } catch (Exception e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Invalid quantity"
            );
        }
    }

    // ================= GENERATE BILL =================
    private void generateBill() {

        if (saleItems.isEmpty()) {

            JOptionPane.showMessageDialog(
                    this,
                    "Add items first"
            );

            return;
        }

        Customer c =
                (Customer) customerBox.getSelectedItem();

        if (c == null) {

            JOptionPane.showMessageDialog(
                    this,
                    "Select customer"
            );

            return;
        }

        Sale sale =
                new Sale(c.getId(), total);

        int saleId =
                saleDAO.createSale(sale, saleItems);

        if (saleId != -1) {

            JOptionPane.showMessageDialog(
                    this,
                    "Bill Generated Successfully"
            );

            new BillFrame(
                    saleId,
                    c,
                    saleItems,
                    total
            );

            model.setRowCount(0);

            saleItems.clear();

            total = 0;

            totalLabel.setText("Total: Rs. 0.0");

            loadProducts();

        } else {

            JOptionPane.showMessageDialog(
                    this,
                    "Failed to generate bill"
            );
        }
    }
}

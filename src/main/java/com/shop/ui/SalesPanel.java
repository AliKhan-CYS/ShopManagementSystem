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

    private final CustomerDAO customerDAO = new CustomerDAO();
    private final ProductDAO productDAO = new ProductDAO();
    private final SaleDAO saleDAO = new SaleDAO();

    private final JComboBox<Customer> customerBox = new JComboBox<>();
    private final JComboBox<Product> productBox = new JComboBox<>();
    private final JTextField qtyField = new JTextField(5);
    private final JLabel totalLabel = new JLabel();
    private final DefaultTableModel model;
    private final JTable table;

    private final List<CartLine> cart = new ArrayList<>();

    private static final class CartLine {
        final Product product;
        int quantity;

        CartLine(Product product, int quantity) {
            this.product = product;
            this.quantity = quantity;
        }

        double subtotal() {
            return product.getPrice() * quantity;
        }
    }

    public SalesPanel(User user) {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        UITheme.styleRoot(this);

        JPanel top = new JPanel(new GridLayout(2, 1, 10, 10));
        top.setOpaque(false);

        JPanel customerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        customerPanel.setOpaque(false);
        customerBox.setPreferredSize(new Dimension(250, 30));
        UITheme.styleCombo(customerBox);
        JLabel customerLabel = new JLabel("Customer:");
        UITheme.styleLabel(customerLabel);
        customerPanel.add(customerLabel);
        customerPanel.add(customerBox);
        top.add(customerPanel);

        JPanel productPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        productPanel.setOpaque(false);
        productBox.setPreferredSize(new Dimension(250, 30));
        UITheme.styleCombo(productBox);
        UITheme.styleField(qtyField);

        JButton addBtn = new JButton("Add Item");
        JButton removeBtn = new JButton("Remove Item");
        UITheme.stylePrimaryButton(addBtn);
        UITheme.styleButton(removeBtn);

        JLabel productLabel = new JLabel("Product:");
        UITheme.styleLabel(productLabel);
        JLabel qtyLabel = new JLabel("Qty:");
        UITheme.styleLabel(qtyLabel);
        productPanel.add(productLabel);
        productPanel.add(productBox);
        productPanel.add(qtyLabel);
        productPanel.add(qtyField);
        productPanel.add(addBtn);
        productPanel.add(removeBtn);
        top.add(productPanel);
        add(top, BorderLayout.NORTH);

        model = new DefaultTableModel(new String[]{"Product", "Qty", "Price", "Subtotal"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(model);
        UITheme.styleTable(table);
        add(UITheme.wrapTable(table), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        totalLabel.setForeground(UITheme.TEXT);
        JButton billBtn = new JButton("Generate Bill");
        UITheme.stylePrimaryButton(billBtn);
        bottom.add(totalLabel, BorderLayout.WEST);
        bottom.add(billBtn, BorderLayout.EAST);
        add(bottom, BorderLayout.SOUTH);

        if (!user.isAdmin()) {
            addBtn.setEnabled(false);
            removeBtn.setEnabled(false);
            billBtn.setEnabled(false);
            productBox.setEnabled(false);
            qtyField.setEnabled(false);
        }

        addBtn.addActionListener(e -> addItem());
        qtyField.addActionListener(e -> addItem());
        removeBtn.addActionListener(e -> removeItem());
        billBtn.addActionListener(e -> generateBill());

        loadCustomers();
        loadProducts();
        refreshCart();
    }

    private void loadCustomers() {
        customerBox.removeAllItems();
        for (Customer c : customerDAO.getAllCustomers()) {
            customerBox.addItem(c);
        }
    }

    private void loadProducts() {
        productBox.removeAllItems();
        for (Product p : productDAO.getAllProducts()) {
            productBox.addItem(p);
        }
    }

    private void addItem() {
        Product product = (Product) productBox.getSelectedItem();
        if (product == null) {
            JOptionPane.showMessageDialog(this, "No product selected");
            return;
        }

        int qty;
        try {
            qty = Integer.parseInt(qtyField.getText().trim());
        } catch (NumberFormatException e) {
            qty = 0;
        }
        if (qty <= 0) {
            JOptionPane.showMessageDialog(this, "Enter a quantity greater than zero");
            return;
        }

        CartLine existing = cart.stream()
                .filter(line -> line.product.getId() == product.getId())
                .findFirst()
                .orElse(null);
        int inCart = existing == null ? 0 : existing.quantity;
        if (inCart + qty > product.getQuantity()) {
            JOptionPane.showMessageDialog(this, "Not enough stock. Available: " + product.getQuantity()
                    + (inCart > 0 ? ", already in bill: " + inCart : ""));
            return;
        }

        if (existing == null) {
            cart.add(new CartLine(product, qty));
        } else {
            existing.quantity += qty;
        }
        qtyField.setText("");
        refreshCart();
    }

    private void removeItem() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select an item to remove");
            return;
        }
        cart.remove(table.convertRowIndexToModel(row));
        refreshCart();
    }

    private double cartTotal() {
        return cart.stream().mapToDouble(CartLine::subtotal).sum();
    }

    private void refreshCart() {
        model.setRowCount(0);
        for (CartLine line : cart) {
            model.addRow(new Object[]{
                    line.product.getName(),
                    line.quantity,
                    Money.format(line.product.getPrice()),
                    Money.format(line.subtotal())
            });
        }
        totalLabel.setText("Total: " + Money.format(cartTotal()));
    }

    private void generateBill() {
        if (cart.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Add items first");
            return;
        }

        Customer customer = (Customer) customerBox.getSelectedItem();
        if (customer == null) {
            JOptionPane.showMessageDialog(this, "Select a customer (add one on the Customers page first)");
            return;
        }

        List<SaleItem> items = new ArrayList<>();
        List<BillFrame.Line> billLines = new ArrayList<>();
        for (CartLine line : cart) {
            items.add(new SaleItem(line.product.getId(), line.quantity, line.product.getPrice()));
            billLines.add(new BillFrame.Line(line.product.getName(), line.quantity, line.product.getPrice()));
        }

        double total = cartTotal();
        int saleId = saleDAO.createSale(new Sale(customer.getId(), total), items);
        loadProducts();

        if (saleId == -1) {
            JOptionPane.showMessageDialog(this,
                    "The bill could not be saved. Stock may have changed; please check the items and try again.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        cart.clear();
        refreshCart();
        new BillFrame(saleId, customer, billLines, total);
    }
}

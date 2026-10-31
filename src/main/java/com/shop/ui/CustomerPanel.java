package com.shop.ui;

import com.shop.dao.CustomerDAO;
import com.shop.model.Customer;
import com.shop.model.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerPanel extends JPanel {

    private final CustomerDAO dao = new CustomerDAO();
    private final DefaultTableModel model;
    private final JTable table;
    private final JTextField searchField = new JTextField(20);
    private final List<Customer> shownCustomers = new ArrayList<>();

    public CustomerPanel(User user) {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        UITheme.styleRoot(this);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        top.setOpaque(false);
        JButton searchBtn = new JButton("Search");
        JButton resetBtn = new JButton("Reset");
        UITheme.styleField(searchField);
        UITheme.stylePrimaryButton(searchBtn);
        UITheme.styleButton(resetBtn);

        JLabel searchLabel = new JLabel("Search by Name:");
        UITheme.styleLabel(searchLabel);
        top.add(searchLabel);
        top.add(searchField);
        top.add(searchBtn);
        top.add(resetBtn);
        add(top, BorderLayout.NORTH);

        model = new DefaultTableModel(new String[]{"ID", "Name", "Email", "Phone", "Address"}, 0) {
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
        JButton addBtn = new JButton("Add Customer");
        JButton editBtn = new JButton("Edit Customer");
        JButton deleteBtn = new JButton("Delete Customer");
        UITheme.stylePrimaryButton(addBtn);
        UITheme.styleButton(editBtn);
        UITheme.styleDangerButton(deleteBtn);
        bottom.add(addBtn);
        bottom.add(editBtn);
        bottom.add(deleteBtn);
        add(bottom, BorderLayout.SOUTH);

        if (!user.isAdmin()) {
            addBtn.setEnabled(false);
            editBtn.setEnabled(false);
            deleteBtn.setEnabled(false);
        }

        addBtn.addActionListener(e -> addCustomer());
        editBtn.addActionListener(e -> editCustomer());
        deleteBtn.addActionListener(e -> deleteCustomer());
        searchBtn.addActionListener(e -> searchCustomer());
        searchField.addActionListener(e -> searchCustomer());
        resetBtn.addActionListener(e -> {
            searchField.setText("");
            loadCustomers();
        });

        loadCustomers();
    }

    private void loadCustomers() {
        showCustomers(dao.getAllCustomers());
    }

    private void showCustomers(List<Customer> customers) {
        shownCustomers.clear();
        shownCustomers.addAll(customers);
        model.setRowCount(0);
        for (Customer c : customers) {
            model.addRow(new Object[]{c.getId(), c.getName(), c.getEmail(), c.getPhone(), c.getAddress()});
        }
    }

    private void searchCustomer() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            loadCustomers();
            return;
        }

        List<Customer> result = dao.searchByName(keyword);
        showCustomers(result);
        if (result.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No customer found");
        }
    }

    private void addCustomer() {
        Customer customer = showCustomerDialog("Add Customer", null);
        if (customer == null) return;

        if (dao.addCustomer(customer)) {
            JOptionPane.showMessageDialog(this, "Customer added");
            loadCustomers();
        } else {
            JOptionPane.showMessageDialog(this, "Customer could not be added.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void editCustomer() {
        Customer selected = selectedCustomer();
        if (selected == null) return;

        Customer customer = showCustomerDialog("Edit Customer", selected);
        if (customer == null) return;

        if (dao.updateCustomer(customer)) {
            JOptionPane.showMessageDialog(this, "Customer updated");
            loadCustomers();
        } else {
            JOptionPane.showMessageDialog(this, "Customer could not be updated.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteCustomer() {
        Customer selected = selectedCustomer();
        if (selected == null) return;

        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete customer \"" + selected.getName() + "\"?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        if (dao.deleteCustomer(selected.getId())) {
            JOptionPane.showMessageDialog(this, "Customer deleted");
            loadCustomers();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Customer could not be deleted. Customers with recorded sales are kept for billing history.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Customer selectedCustomer() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a customer first");
            return null;
        }
        return shownCustomers.get(table.convertRowIndexToModel(row));
    }

    private Customer showCustomerDialog(String title, Customer existing) {
        JTextField name = new JTextField(existing == null ? "" : textOf(existing.getName()));
        JTextField email = new JTextField(existing == null ? "" : textOf(existing.getEmail()));
        JTextField phone = new JTextField(existing == null ? "" : textOf(existing.getPhone()));
        JTextField address = new JTextField(existing == null ? "" : textOf(existing.getAddress()));

        Object[] fields = {"Name:", name, "Email:", email, "Phone:", phone, "Address:", address};

        while (JOptionPane.showConfirmDialog(this, fields, title, JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            String n = name.getText().trim();
            String e = email.getText().trim();
            String p = phone.getText().trim();
            String a = address.getText().trim();

            if (n.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Customer name is required.");
            } else if (n.length() > 120 || e.length() > 120 || p.length() > 40 || a.length() > 250) {
                JOptionPane.showMessageDialog(this, "One of the fields is too long.");
            } else if (!e.isEmpty() && !e.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) {
                JOptionPane.showMessageDialog(this, "Enter a valid email address or leave it empty.");
            } else {
                int id = existing == null ? 0 : existing.getId();
                return new Customer(id, n, e, p, a);
            }
        }
        return null;
    }

    private static String textOf(String value) {
        return value == null ? "" : value;
    }
}

package com.shop.ui;

import com.shop.dao.CustomerDAO;
import com.shop.model.Customer;
import com.shop.model.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class CustomerPanel extends JPanel {

    private JTable table;
    private DefaultTableModel model;
    private CustomerDAO dao = new CustomerDAO();
    private JTextField searchField;
    private final User user; // store logged-in user

    public CustomerPanel(User user) {
        this.user = user;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        UITheme.styleRoot(this);

        // --- TOP PANEL ---
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        top.setOpaque(false);
        searchField = new JTextField(20);
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

        // --- TABLE ---
        model = new DefaultTableModel(new String[]{"ID","Name","Email","Phone","Address"},0) {
            public boolean isCellEditable(int r,int c){return false;}
        };
        table = new JTable(model);
        UITheme.styleTable(table);
        add(UITheme.wrapTable(table), BorderLayout.CENTER);

        // --- BUTTONS ---
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

        // --- ROLE CONTROL ---
        if(!user.isAdmin()){
            addBtn.setEnabled(false);
            editBtn.setEnabled(false);
            deleteBtn.setEnabled(false);
        }

        // --- ACTIONS ---
        addBtn.addActionListener(e -> addCustomer());
        editBtn.addActionListener(e -> editCustomer());
        deleteBtn.addActionListener(e -> deleteCustomer());
        searchBtn.addActionListener(e -> searchCustomer());
        resetBtn.addActionListener(e -> {
            searchField.setText("");
            loadCustomers();
        });

        loadCustomers();
    }

    private void loadCustomers() {
        model.setRowCount(0);
        List<Customer> list = dao.getAllCustomers();
        for (Customer c : list) {
            model.addRow(new Object[]{c.getId(), c.getName(), c.getEmail(), c.getPhone(), c.getAddress()});
        }
    }

    private void searchCustomer() {
        String keyword = searchField.getText().trim();
        if(keyword.isEmpty()){
            JOptionPane.showMessageDialog(this,"Enter search text");
            return;
        }
        model.setRowCount(0);
        List<Customer> list = dao.searchByName(keyword);
        if(list.isEmpty()){
            JOptionPane.showMessageDialog(this,"No customer found");
            return;
        }
        for(Customer c:list){
            model.addRow(new Object[]{c.getId(),c.getName(),c.getEmail(),c.getPhone(),c.getAddress()});
        }
    }

    private void addCustomer(){
        JTextField name = new JTextField();
        JTextField email = new JTextField();
        JTextField phone = new JTextField();
        JTextField address = new JTextField();

        Object[] fields = {"Name:", name, "Email:", email, "Phone:", phone, "Address:", address};
        int option = JOptionPane.showConfirmDialog(this,fields,"Add Customer",JOptionPane.OK_CANCEL_OPTION);

        if(option==JOptionPane.OK_OPTION){
            Customer c = new Customer(name.getText().trim(),email.getText().trim(),phone.getText().trim(),address.getText().trim());
            if(dao.addCustomer(c)) JOptionPane.showMessageDialog(this,"Customer Added");
            loadCustomers();
        }
    }

    private void editCustomer(){
        int row = table.getSelectedRow();
        if(row==-1){JOptionPane.showMessageDialog(this,"Select a customer first"); return;}

        int id = (int) model.getValueAt(row,0);
        JTextField name = new JTextField(model.getValueAt(row,1).toString());
        JTextField email = new JTextField(model.getValueAt(row,2).toString());
        JTextField phone = new JTextField(model.getValueAt(row,3).toString());
        JTextField address = new JTextField(model.getValueAt(row,4).toString());

        Object[] fields = {"Name:", name, "Email:", email, "Phone:", phone, "Address:", address};
        int option = JOptionPane.showConfirmDialog(this,fields,"Edit Customer",JOptionPane.OK_CANCEL_OPTION);

        if(option==JOptionPane.OK_OPTION){
            Customer c = new Customer(id,name.getText().trim(),email.getText().trim(),phone.getText().trim(),address.getText().trim());
            if(dao.updateCustomer(c)) JOptionPane.showMessageDialog(this,"Customer Updated");
            loadCustomers();
        }
    }

    private void deleteCustomer(){
        int row = table.getSelectedRow();
        if(row==-1){JOptionPane.showMessageDialog(this,"Select a customer first"); return;}
        int id = (int) model.getValueAt(row,0);
        int confirm = JOptionPane.showConfirmDialog(this,"Delete customer?","Confirm",JOptionPane.YES_NO_OPTION);
        if(confirm==JOptionPane.YES_OPTION){
            if(dao.deleteCustomer(id)) JOptionPane.showMessageDialog(this,"Customer Deleted");
            loadCustomers();
        }
    }
}

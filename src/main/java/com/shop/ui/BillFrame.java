package com.shop.ui;

import com.shop.model.Customer;
import com.shop.model.SaleItem;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class BillFrame extends JFrame {

    public BillFrame(
            int invoiceId,
            Customer customer,
            List<SaleItem> items,
            double total
    ) {

        setTitle("Invoice #" + invoiceId);
        setSize(700, 550);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // ================= MAIN PANEL =================
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        UITheme.styleRoot(mainPanel);

        // ================= HEADER =================
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel shopTitle = new JLabel("SHOP MANAGEMENT SYSTEM");
        UITheme.styleTitle(shopTitle);

        JLabel invoiceLabel = new JLabel("Invoice #" + invoiceId);
        UITheme.styleSectionTitle(invoiceLabel);

        header.add(shopTitle, BorderLayout.WEST);
        header.add(invoiceLabel, BorderLayout.EAST);

        mainPanel.add(header, BorderLayout.NORTH);

        // ================= CUSTOMER INFO =================
        JPanel infoPanel = new JPanel(new GridLayout(4, 1));
        infoPanel.setOpaque(false);

        SimpleDateFormat sdf =
                new SimpleDateFormat("dd-MM-yyyy hh:mm a");

        addInfoLabel(infoPanel, "Customer: " + customer.getName());
        addInfoLabel(infoPanel, "Phone: " + customer.getPhone());
        addInfoLabel(infoPanel, "Email: " + customer.getEmail());
        addInfoLabel(infoPanel, "Date: " + sdf.format(new Date()));

        mainPanel.add(infoPanel, BorderLayout.WEST);

        // ================= TABLE =================
        String[] columns = {
                "Product ID",
                "Quantity",
                "Price",
                "Subtotal"
        };

        DefaultTableModel model =
                new DefaultTableModel(columns, 0);

        JTable table = new JTable(model);
        UITheme.styleTable(table);

        double grandTotal = 0;

        for (SaleItem item : items) {

            double subtotal =
                    item.getQuantity() * item.getPrice();

            grandTotal += subtotal;

            model.addRow(new Object[]{
                    item.getProductId(),
                    item.getQuantity(),
                    item.getPrice(),
                    subtotal
            });
        }

        JScrollPane scrollPane =
                UITheme.wrapTable(table);

        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // ================= FOOTER =================
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);

        JLabel totalLabel =
                new JLabel("Total Amount: Rs. " + grandTotal);

        totalLabel.setFont(
                new Font("Segoe UI", Font.BOLD, 20)
        );
        totalLabel.setForeground(UITheme.TEXT);

        JButton printBtn = new JButton("Print Bill");
        UITheme.stylePrimaryButton(printBtn);

        footer.add(totalLabel, BorderLayout.WEST);
        footer.add(printBtn, BorderLayout.EAST);

        mainPanel.add(footer, BorderLayout.SOUTH);

        add(mainPanel);

        // ================= PRINT ACTION =================
        printBtn.addActionListener(e -> {

            try {

                boolean complete =
                        table.print();

                if (complete) {

                    JOptionPane.showMessageDialog(
                            this,
                            "Printing Complete"
                    );

                } else {

                    JOptionPane.showMessageDialog(
                            this,
                            "Printing Cancelled"
                    );
                }

            } catch (Exception ex) {

                JOptionPane.showMessageDialog(
                        this,
                        "Printing Failed"
                );
            }
        });

        setVisible(true);
    }

    private void addInfoLabel(JPanel panel, String text) {
        JLabel label = new JLabel(text);
        UITheme.styleLabel(label);
        panel.add(label);
    }
}

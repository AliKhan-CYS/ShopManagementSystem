package com.shop.ui;

import com.shop.model.Customer;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.print.PrinterException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class BillFrame extends JFrame {

    public record Line(String product, int quantity, double price) {
        double subtotal() {
            return quantity * price;
        }
    }

    public BillFrame(int invoiceId, Customer customer, List<Line> lines, double total) {
        setTitle("Invoice #" + invoiceId);
        setIconImage(ShopLogo.render(64));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(700, 550);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        UITheme.styleRoot(mainPanel);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel shopTitle = new JLabel("SHOP MANAGEMENT SYSTEM");
        UITheme.styleTitle(shopTitle);
        JLabel invoiceLabel = new JLabel("Invoice #" + invoiceId);
        UITheme.styleSectionTitle(invoiceLabel);
        header.add(shopTitle, BorderLayout.WEST);
        header.add(invoiceLabel, BorderLayout.EAST);
        mainPanel.add(header, BorderLayout.NORTH);

        JPanel infoPanel = new JPanel(new GridLayout(4, 1));
        infoPanel.setOpaque(false);
        String date = new SimpleDateFormat("dd-MM-yyyy hh:mm a").format(new Date());
        addInfoLabel(infoPanel, "Customer: " + customer.getName());
        addInfoLabel(infoPanel, "Phone: " + textOf(customer.getPhone()));
        addInfoLabel(infoPanel, "Email: " + textOf(customer.getEmail()));
        addInfoLabel(infoPanel, "Date: " + date);
        mainPanel.add(infoPanel, BorderLayout.WEST);

        DefaultTableModel model = new DefaultTableModel(new String[]{"Product", "Quantity", "Price", "Subtotal"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        for (Line line : lines) {
            model.addRow(new Object[]{line.product(), line.quantity(), Money.format(line.price()), Money.format(line.subtotal())});
        }
        JTable table = new JTable(model);
        UITheme.styleTable(table);
        mainPanel.add(UITheme.wrapTable(table), BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        JLabel totalLabel = new JLabel("Total Amount: " + Money.format(total));
        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        totalLabel.setForeground(UITheme.TEXT);
        JButton printBtn = new JButton("Print Bill");
        UITheme.stylePrimaryButton(printBtn);
        footer.add(totalLabel, BorderLayout.WEST);
        footer.add(printBtn, BorderLayout.EAST);
        mainPanel.add(footer, BorderLayout.SOUTH);

        add(mainPanel);

        MessageFormat printHeader = new MessageFormat(escape("Invoice #" + invoiceId + " - " + customer.getName() + " - " + date));
        MessageFormat printFooter = new MessageFormat(escape("Total Amount: " + Money.format(total)));
        printBtn.addActionListener(e -> {
            try {
                boolean complete = table.print(JTable.PrintMode.FIT_WIDTH, printHeader, printFooter);
                JOptionPane.showMessageDialog(this, complete ? "Printing complete" : "Printing cancelled");
            } catch (PrinterException ex) {
                JOptionPane.showMessageDialog(this, "Printing failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        setVisible(true);
    }

    private static String escape(String text) {
        return text.replace("'", "''").replace("{", "'{'");
    }

    private static String textOf(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private void addInfoLabel(JPanel panel, String text) {
        JLabel label = new JLabel(text);
        UITheme.styleLabel(label);
        panel.add(label);
    }
}

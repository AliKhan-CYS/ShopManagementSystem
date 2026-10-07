package com.shop.ui;

import com.formdev.flatlaf.FlatDarkLaf;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.renderer.category.CategoryItemRenderer;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.JTableHeader;
import java.awt.*;

public final class UITheme {

    public static final Color BACKGROUND = new Color(15, 23, 42);
    public static final Color SURFACE = new Color(24, 34, 54);
    public static final Color SURFACE_ALT = new Color(30, 41, 64);
    public static final Color BORDER = new Color(51, 65, 85);
    public static final Color TEXT = new Color(226, 232, 240);
    public static final Color MUTED_TEXT = new Color(148, 163, 184);
    public static final Color ACCENT = new Color(59, 130, 246);
    public static final Color ACCENT_HOVER = new Color(37, 99, 235);
    public static final Color DANGER = new Color(239, 68, 68);
    public static final Color DANGER_HOVER = new Color(220, 38, 38);
    public static final Color SUCCESS = new Color(34, 197, 94);
    public static final Color WARNING = new Color(245, 158, 11);

    private static final Font BASE_FONT = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 22);
    private static final Font SECTION_FONT = new Font("Segoe UI", Font.BOLD, 16);

    private UITheme() {
    }

    public static void setup() {
        FlatDarkLaf.setup();
        UIManager.put("defaultFont", BASE_FONT);
        UIManager.put("Button.arc", 10);
        UIManager.put("Component.arc", 10);
        UIManager.put("TextComponent.arc", 8);
        UIManager.put("Table.showHorizontalLines", true);
        UIManager.put("Table.showVerticalLines", false);
        UIManager.put("Table.selectionBackground", new Color(37, 99, 235));
        UIManager.put("Table.selectionForeground", Color.WHITE);
        UIManager.put("OptionPane.background", SURFACE);
        UIManager.put("Panel.background", BACKGROUND);
    }

    public static Border paddedBorder(int top, int left, int bottom, int right) {
        return BorderFactory.createEmptyBorder(top, left, bottom, right);
    }

    public static Border surfaceBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)
        );
    }

    public static void styleRoot(JComponent component) {
        component.setBackground(BACKGROUND);
        component.setForeground(TEXT);
    }

    public static void styleSurface(JComponent component) {
        component.setBackground(SURFACE);
        component.setForeground(TEXT);
    }

    public static void styleToolbar(JComponent component) {
        component.setOpaque(false);
        component.setBorder(BorderFactory.createEmptyBorder(4, 0, 6, 0));
    }

    public static void styleCard(JPanel panel) {
        panel.setBackground(SURFACE);
        panel.setForeground(TEXT);
        panel.setBorder(surfaceBorder());
    }

    public static void styleTitle(JLabel label) {
        label.setFont(TITLE_FONT);
        label.setForeground(TEXT);
    }

    public static void styleSectionTitle(JLabel label) {
        label.setFont(SECTION_FONT);
        label.setForeground(TEXT);
    }

    public static void styleMuted(JLabel label) {
        label.setForeground(MUTED_TEXT);
    }

    public static void styleLabel(JLabel label) {
        label.setForeground(TEXT);
        label.setFont(BASE_FONT);
    }

    public static void styleField(JTextField field) {
        field.setBackground(SURFACE_ALT);
        field.setForeground(TEXT);
        field.setCaretColor(TEXT);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
    }

    public static void styleCombo(JComboBox<?> comboBox) {
        comboBox.setBackground(SURFACE_ALT);
        comboBox.setForeground(TEXT);
        comboBox.setBorder(BorderFactory.createLineBorder(BORDER));
    }

    public static void styleButton(JButton button) {
        button.setBackground(SURFACE_ALT);
        button.setForeground(TEXT);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(8, 14, 8, 14)
        ));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    public static void stylePrimaryButton(JButton button) {
        styleButton(button);
        button.setBackground(ACCENT);
        button.setForeground(Color.WHITE);
        addHover(button, ACCENT, ACCENT_HOVER);
    }

    public static void styleDangerButton(JButton button) {
        styleButton(button);
        button.setBackground(DANGER);
        button.setForeground(Color.WHITE);
        addHover(button, DANGER, DANGER_HOVER);
    }

    public static void styleTable(JTable table) {
        table.setBackground(SURFACE);
        table.setForeground(TEXT);
        table.setGridColor(BORDER);
        table.setSelectionBackground(ACCENT);
        table.setSelectionForeground(Color.WHITE);
        table.setRowHeight(30);
        table.setShowVerticalLines(false);
        table.setFillsViewportHeight(true);

        JTableHeader header = table.getTableHeader();
        header.setBackground(SURFACE_ALT);
        header.setForeground(TEXT);
        header.setFont(BASE_FONT.deriveFont(Font.BOLD));
        header.setBorder(BorderFactory.createLineBorder(BORDER));
    }

    public static JScrollPane wrapTable(JTable table) {
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(SURFACE);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER));
        return scrollPane;
    }

    public static void styleChart(JFreeChart chart) {
        chart.setBackgroundPaint(SURFACE);

        if (chart.getTitle() != null) {
            chart.getTitle().setPaint(TEXT);
            chart.getTitle().setFont(SECTION_FONT);
        }

        if (chart.getLegend() != null) {
            chart.getLegend().setBackgroundPaint(SURFACE);
            chart.getLegend().setItemPaint(MUTED_TEXT);
        }

        Plot plot = chart.getPlot();
        plot.setBackgroundPaint(SURFACE);
        plot.setOutlinePaint(BORDER);

        if (plot instanceof CategoryPlot categoryPlot) {
            categoryPlot.setRangeGridlinePaint(BORDER);
            categoryPlot.setDomainGridlinePaint(BORDER);
            styleCategoryAxis(categoryPlot.getDomainAxis());
            styleValueAxis(categoryPlot.getRangeAxis());

            CategoryItemRenderer renderer = categoryPlot.getRenderer();
            renderer.setSeriesPaint(0, ACCENT);
            renderer.setSeriesPaint(1, SUCCESS);
            renderer.setSeriesPaint(2, WARNING);
        } else if (plot instanceof PiePlot piePlot) {
            piePlot.setLabelPaint(TEXT);
            piePlot.setLabelBackgroundPaint(SURFACE_ALT);
            piePlot.setLabelOutlinePaint(BORDER);
            piePlot.setLabelShadowPaint(null);
            piePlot.setSectionPaint(0, ACCENT);
            piePlot.setSectionPaint(1, SUCCESS);
            piePlot.setSectionPaint(2, WARNING);
            piePlot.setSectionPaint(3, new Color(168, 85, 247));
        }
    }

    public static ChartPanelWrapper chartPanel(JFreeChart chart) {
        styleChart(chart);
        return new ChartPanelWrapper(chart);
    }

    private static void styleCategoryAxis(CategoryAxis axis) {
        axis.setLabelPaint(MUTED_TEXT);
        axis.setTickLabelPaint(MUTED_TEXT);
    }

    private static void styleValueAxis(ValueAxis axis) {
        axis.setLabelPaint(MUTED_TEXT);
        axis.setTickLabelPaint(MUTED_TEXT);
    }

    private static void addHover(JButton button, Color normal, Color hover) {
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (button.isEnabled()) {
                    button.setBackground(hover);
                }
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(normal);
            }
        });
    }

    public static class ChartPanelWrapper extends org.jfree.chart.ChartPanel {
        public ChartPanelWrapper(JFreeChart chart) {
            super(chart);
            setBackground(SURFACE);
            setBorder(BorderFactory.createLineBorder(BORDER));
            setMouseWheelEnabled(true);
        }
    }
}

package com.shop.ui;

import com.formdev.flatlaf.FlatDarkLaf;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.JTableHeader;
import java.awt.*;

public final class UITheme {

    public static final Color BACKGROUND = new Color(10, 15, 25);
    public static final Color SURFACE = new Color(18, 27, 43);
    public static final Color SURFACE_ALT = new Color(25, 37, 58);
    public static final Color SURFACE_ELEVATED = new Color(22, 32, 50);
    public static final Color BORDER = new Color(54, 68, 90);
    public static final Color TEXT = new Color(241, 245, 249);
    public static final Color MUTED_TEXT = new Color(148, 163, 184);
    public static final Color ACCENT = new Color(14, 165, 233);
    public static final Color ACCENT_SOFT = new Color(125, 211, 252);
    public static final Color ACCENT_HOVER = new Color(2, 132, 199);
    public static final Color DANGER = new Color(248, 113, 113);
    public static final Color DANGER_HOVER = new Color(220, 38, 38);
    public static final Color SUCCESS = new Color(74, 222, 128);
    public static final Color WARNING = new Color(251, 191, 36);

    private static final Font BASE_FONT = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 24);
    private static final Font SECTION_FONT = new Font("Segoe UI", Font.BOLD, 16);

    private UITheme() {
    }

    public static void setup() {
        FlatDarkLaf.setup();
        UIManager.put("defaultFont", BASE_FONT);
        UIManager.put("Button.arc", 14);
        UIManager.put("Component.arc", 12);
        UIManager.put("TextComponent.arc", 12);
        UIManager.put("ScrollBar.thumbArc", 999);
        UIManager.put("ScrollBar.width", 11);
        UIManager.put("Table.showHorizontalLines", true);
        UIManager.put("Table.showVerticalLines", false);
        UIManager.put("Table.selectionBackground", ACCENT_HOVER);
        UIManager.put("Table.selectionForeground", Color.WHITE);
        UIManager.put("OptionPane.background", SURFACE);
        UIManager.put("Panel.background", BACKGROUND);
    }

    public static Border surfaceBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)
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

    public static void styleCard(JPanel panel) {
        panel.setBackground(SURFACE);
        panel.setForeground(TEXT);
        panel.setBorder(surfaceBorder());
    }

    public static void styleElevatedCard(JPanel panel) {
        panel.setBackground(SURFACE_ELEVATED);
        panel.setForeground(TEXT);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(71, 85, 105)),
                BorderFactory.createEmptyBorder(24, 26, 24, 26)
        ));
    }

    public static JPanel createStatusBadge(JLabel label, Color foreground, Color background) {
        JPanel badge = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        badge.setOpaque(true);
        badge.setBackground(background);
        badge.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 255, 255, 28)),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        label.setForeground(foreground);
        badge.add(label);
        return badge;
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
                BorderFactory.createLineBorder(new Color(71, 85, 105)),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
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
                BorderFactory.createLineBorder(new Color(71, 85, 105)),
                BorderFactory.createEmptyBorder(9, 15, 9, 15)
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
        button.setBackground(DANGER_HOVER);
        button.setForeground(Color.WHITE);
        addHover(button, DANGER_HOVER, new Color(185, 28, 28));
    }

    public static void styleTable(JTable table) {
        table.setBackground(SURFACE);
        table.setForeground(TEXT);
        table.setGridColor(BORDER);
        table.setSelectionBackground(ACCENT_HOVER);
        table.setSelectionForeground(Color.WHITE);
        table.setRowHeight(34);
        table.setShowVerticalLines(false);
        table.setFillsViewportHeight(true);
        table.setIntercellSpacing(new Dimension(0, 1));

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
            if (renderer instanceof LineAndShapeRenderer lineRenderer) {
                // A single day of data has no line segment, so draw the points too.
                lineRenderer.setDefaultShapesVisible(true);
            }
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
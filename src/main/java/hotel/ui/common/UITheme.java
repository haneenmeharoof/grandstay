package hotel.ui.common;

import com.formdev.flatlaf.FlatLightLaf;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Modern design system for GrandStay Hotel Management.
 * Palette: Deep Navy, Crisp Slate/White, and Restrained Gold Accents.
 */
public final class UITheme {

    // Color Palette
    public static final Color NAVY_DARK = new Color(0x0F, 0x17, 0x2A);     // #0F172A
    public static final Color NAVY_MEDIUM = new Color(0x1E, 0x29, 0x3B);   // #1E293B
    public static final Color NAVY_LIGHT = new Color(0x33, 0x41, 0x55);    // #334155

    public static final Color GOLD = new Color(0xD4, 0xAF, 0x37);          // #D4AF37
    public static final Color GOLD_DARK = new Color(0xB8, 0x86, 0x0B);     // #B8860B
    public static final Color GOLD_LIGHT = new Color(0xFE, 0xF9, 0xE7);    // #FEF9E7

    public static final Color BG_LIGHT = new Color(0xF8, 0xFA, 0xFC);      // #F8FAFC
    public static final Color BG_CARD = new Color(0xFF, 0xFF, 0xFF);       // #FFFFFF
    public static final Color BORDER_COLOR = new Color(0xE2, 0xE8, 0xF0);  // #E2E8F0

    public static final Color TEXT_PRIMARY = new Color(0x0F, 0x17, 0x2A);  // #0F172A
    public static final Color TEXT_SECONDARY = new Color(0x64, 0x74, 0x8B);// #64748B
    public static final Color TEXT_MUTED = new Color(0x94, 0xA3, 0xB8);    // #94A3B8

    public static final Color SUCCESS = new Color(0x10, 0xB9, 0x81);       // #10B981
    public static final Color WARNING = new Color(0xF5, 0x9E, 0x0B);       // #F59E0B
    public static final Color DANGER = new Color(0xEF, 0x44, 0x44);        // #EF4444
    public static final Color INFO = new Color(0x3B, 0x82, 0xF6);          // #3B82F6

    // Fonts
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 20);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);

    private UITheme() {}

    public static void setupLookAndFeel() {
        try {
            FlatLightLaf.setup();
            UIManager.put("Button.arc", 8);
            UIManager.put("Component.arc", 8);
            UIManager.put("ProgressBar.arc", 8);
            UIManager.put("TextComponent.arc", 8);
            UIManager.put("TableHeader.background", NAVY_DARK);
            UIManager.put("TableHeader.foreground", Color.WHITE);
            UIManager.put("TableHeader.font", FONT_BOLD);
            UIManager.put("Table.rowHeight", 34);
            UIManager.put("Table.selectionBackground", new Color(0xE0, 0xE7, 0xFF));
            UIManager.put("Table.selectionForeground", TEXT_PRIMARY);
        } catch (Exception e) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
        }
    }

    public static JButton createPrimaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BOLD);
        btn.setForeground(Color.WHITE);
        btn.setBackground(NAVY_DARK);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(9, 18, 9, 18));
        btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        return btn;
    }

    public static JButton createGoldButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BOLD);
        btn.setForeground(NAVY_DARK);
        btn.setBackground(GOLD);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(9, 18, 9, 18));
        btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        return btn;
    }

    public static JButton createSecondaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_REGULAR);
        btn.setForeground(TEXT_PRIMARY);
        btn.setBackground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                BorderFactory.createEmptyBorder(8, 16, 8, 16)
        ));
        btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        return btn;
    }

    public static JButton createDangerButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BOLD);
        btn.setForeground(Color.WHITE);
        btn.setBackground(DANGER);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        return btn;
    }

    public static JPanel createCardPanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(BORDER_COLOR);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 18, 15, 18));
        return panel;
    }

    public static void styleTable(JTable table) {
        table.setFont(FONT_REGULAR);
        table.getTableHeader().setFont(FONT_HEADER);
        table.getTableHeader().setPreferredSize(new Dimension(0, 36));
        table.setRowHeight(34);
        table.setShowGrid(true);
        table.setGridColor(BORDER_COLOR);
        table.setIntercellSpacing(new Dimension(1, 1));

        // Center aligned cell renderer for ID, status, and dates
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++) {
            String colName = table.getColumnName(i).toLowerCase();
            if (colName.contains("id") || colName.contains("status") || colName.contains("date") || colName.contains("floor") || colName.contains("qty")) {
                table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
            }
        }
    }
}

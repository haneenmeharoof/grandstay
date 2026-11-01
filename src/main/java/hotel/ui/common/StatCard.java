package hotel.ui.common;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class StatCard extends JPanel {

    private final JLabel valueLabel;
    private final JLabel subLabel;

    public StatCard(String title, String initialValue, String subtext, Color accentColor) {
        setLayout(new BorderLayout(0, 4));
        setPreferredSize(new Dimension(180, 95));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        JLabel titleLabel = new JLabel(title.toUpperCase());
        titleLabel.setFont(UITheme.FONT_SMALL);
        titleLabel.setForeground(UITheme.TEXT_SECONDARY);

        valueLabel = new JLabel(initialValue);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        valueLabel.setForeground(accentColor != null ? accentColor : UITheme.NAVY_DARK);

        subLabel = new JLabel(subtext != null ? subtext : "");
        subLabel.setFont(UITheme.FONT_SMALL);
        subLabel.setForeground(UITheme.TEXT_MUTED);

        add(titleLabel, BorderLayout.NORTH);
        add(valueLabel, BorderLayout.CENTER);
        add(subLabel, BorderLayout.SOUTH);
    }

    public void setValue(String value) {
        valueLabel.setText(value);
    }

    public void setSubtext(String subtext) {
        subLabel.setText(subtext);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(UITheme.BG_CARD);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
        g2.setColor(UITheme.BORDER_COLOR);
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
        g2.dispose();
        super.paintComponent(g);
    }
}

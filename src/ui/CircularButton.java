package ui;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import javax.swing.*;

/**
 * CircularButton - Custom circular button component
 * Used for send buttons in the multi-session chat UI
 */
public class CircularButton extends JButton {
    private Color buttonColor;
    private Color hoverColor;
    private boolean isHovered = false;
    private int diameter;

    public CircularButton(String icon, Color color, int diameter) {
        super(icon);
        this.buttonColor = color;
        this.diameter = diameter;
        this.hoverColor = color.darker();

        setPreferredSize(new Dimension(diameter, diameter));
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setForeground(Color.WHITE);
        setFont(new Font("Segoe UI", Font.BOLD, 16));
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Hover effect
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                isHovered = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isHovered = false;
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw circular background
        if (isHovered) {
            g2.setColor(hoverColor);
        } else {
            g2.setColor(buttonColor);
        }

        g2.fill(new Ellipse2D.Double(0, 0, diameter, diameter));

        // Draw icon/text
        g2.setColor(Color.WHITE);
        String text = getText();

        if (text.equals("➤") || text.equals("SEND_ICON")) {
            // Draw a vector arrow for send button
            int padding = diameter / 4;
            int size = diameter - (padding * 2);

            // Define arrow polygon (a simple triangle pointing right)
            int[] xPoints = { padding + 2, padding + size, padding + 2 };
            int[] yPoints = { padding, padding + (size / 2), padding + size };

            g2.fillPolygon(xPoints, yPoints, 3);
        } else if (text.equals("AI")) {
            // Special handling for sparkle if needed, or just draw text
            FontMetrics fm = g2.getFontMetrics();
            int x = (diameter - fm.stringWidth(text)) / 2;
            int y = (diameter + fm.getAscent() - fm.getDescent()) / 2;
            g2.drawString(text, x, y);
        } else {
            FontMetrics fm = g2.getFontMetrics();
            int x = (diameter - fm.stringWidth(text)) / 2;
            int y = (diameter + fm.getAscent() - fm.getDescent()) / 2;
            g2.drawString(text, x, y);
        }

        g2.dispose();
    }

    @Override
    protected void paintBorder(Graphics g) {
        // No border
    }

    @Override
    public boolean contains(int x, int y) {
        // Only respond to clicks within the circle
        int cx = diameter / 2;
        int cy = diameter / 2;
        int dx = x - cx;
        int dy = y - cy;
        return dx * dx + dy * dy <= (diameter / 2) * (diameter / 2);
    }
}

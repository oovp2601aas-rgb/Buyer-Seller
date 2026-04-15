package ui;

import java.awt.*;
import java.awt.event.*;
import java.util.List;
import javax.swing.*;
import model.OrderHistory;

/**
 * OrderHistoryDialog - Displays the order history for this session.
 * Each transaction has a Rate button — opens RatingDialog.
 */
public class OrderHistoryDialog extends JDialog {

    private static final Color COL_BG       = Color.WHITE;
    private static final Color COL_HEADER   = new Color(103, 58, 183);
    private static final Color COL_CARD_BG  = new Color(248, 245, 255);
    private static final Color COL_CARD_BDR = new Color(200, 180, 240);
    private static final Color COL_TEAL     = new Color(0, 150, 136);
    private static final Color COL_GREEN    = new Color(27, 94, 32);
    private static final Color COL_GRAY     = new Color(120, 120, 120);
    private static final Color COL_STAR     = new Color(255, 193, 7);

    private static final Font FONT_TITLE  = new Font("Segoe UI Emoji", Font.BOLD, 17);
    private static final Font FONT_BOLD   = new Font("Segoe UI Emoji", Font.BOLD, 13);
    private static final Font FONT_BODY   = new Font("Segoe UI Emoji", Font.PLAIN, 13);
    private static final Font FONT_SMALL  = new Font("Segoe UI Emoji", Font.PLAIN, 12);
    private static final Font FONT_TXID   = new Font("Courier New", Font.BOLD, 12);
    private static final Font FONT_STAR   = new Font("Segoe UI Emoji", Font.PLAIN, 14);

    // Reference to the scroll body so it can be rebuilt after rating is saved
    private JPanel listPanel;

    public OrderHistoryDialog(Frame owner) {
        super(owner, "\uD83D\uDCCB Order History", true);
        setSize(500, 650);
        setResizable(false);
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        getContentPane().setBackground(COL_BG);
        setLayout(new BorderLayout());

        add(buildHeader(), BorderLayout.NORTH);
        add(buildScrollBody(), BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);
    }

    // ── Header ────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER));
        p.setBackground(COL_HEADER);
        p.setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));
        JLabel lbl = new JLabel("\uD83D\uDCCB  Order History");
        lbl.setFont(FONT_TITLE);
        lbl.setForeground(Color.WHITE);
        p.add(lbl);
        return p;
    }

    // ── Scroll body ───────────────────────────────────────────
    private JScrollPane buildScrollBody() {
        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(COL_BG);
        listPanel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        populateList();

        JScrollPane scroll = new JScrollPane(listPanel);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(14);
        return scroll;
    }

    /** Rebuild listPanel with all transactions */
    private void populateList() {
        listPanel.removeAll();

        List<OrderHistory.Transaction> txs = OrderHistory.getInstance().getAll();

        if (txs.isEmpty()) {
            JPanel empty = new JPanel(new BorderLayout());
            empty.setBackground(COL_BG);
            JLabel el = new JLabel("No transactions in this session yet.", SwingConstants.CENTER);
            el.setFont(FONT_BODY);
            el.setForeground(COL_GRAY);
            empty.add(el, BorderLayout.CENTER);
            listPanel.add(empty);
        } else {
            for (int i = 0; i < txs.size(); i++) {
                listPanel.add(buildTransactionCard(txs.get(i), i + 1));
                listPanel.add(Box.createVerticalStrut(12));
            }
        }

        listPanel.revalidate();
        listPanel.repaint();
    }

    // ── Card per transaction ──────────────────────────────────
    private JPanel buildTransactionCard(OrderHistory.Transaction tx, int num) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(COL_CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COL_CARD_BDR, 1, true),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        // ── Row 1: number + tx id + date ──
        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setBackground(COL_CARD_BG);
        topRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        JLabel txIdLbl = new JLabel("#" + num + "  " + tx.txId);
        txIdLbl.setFont(FONT_TXID);
        txIdLbl.setForeground(COL_HEADER);
        JLabel dateLbl = new JLabel(tx.date);
        dateLbl.setFont(FONT_SMALL);
        dateLbl.setForeground(COL_GRAY);
        topRow.add(txIdLbl, BorderLayout.WEST);
        topRow.add(dateLbl, BorderLayout.EAST);
        card.add(topRow);
        card.add(Box.createVerticalStrut(8));

        card.add(separator());
        card.add(Box.createVerticalStrut(8));

        // ── Info ──
        card.add(infoRow("\uD83D\uDCB3 Method", tx.paymentMethod));
        card.add(Box.createVerticalStrut(4));
        if (tx.address != null && !tx.address.isEmpty()) {
            card.add(infoRow("\uD83D\uDCCD Address", tx.address));
            card.add(Box.createVerticalStrut(4));
        }

        // ── Order preview ──
        String[] lines = tx.orderSummary.split("\n");
        StringBuilder preview = new StringBuilder();
        int shown = 0;
        for (String line : lines) {
            String t = line.trim();
            if (!t.isEmpty() && !t.startsWith("-") && shown < 3) {
                preview.append(t).append("\n");
                shown++;
            }
        }
        if (preview.length() > 0) {
            card.add(Box.createVerticalStrut(4));
            JLabel orderLbl = new JLabel("<html><body style='width:360px; color:#444'>"
                    + preview.toString().replace("\n", "<br>") + "</body></html>");
            orderLbl.setFont(FONT_SMALL);
            card.add(orderLbl);
            card.add(Box.createVerticalStrut(8));
        }

        // ── Existing rating (if any) ──
        if (tx.isRated()) {
            card.add(Box.createVerticalStrut(4));
            JPanel ratingRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
            ratingRow.setBackground(COL_CARD_BG);
            ratingRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));

            JLabel starLbl = new JLabel(tx.rating + " / 5  \u2605");
            starLbl.setFont(new Font("Dialog", Font.BOLD, 13));
            starLbl.setForeground(new Color(255, 193, 7));
            ratingRow.add(starLbl);

            if (!tx.review.isEmpty()) {
                JLabel reviewLbl = new JLabel("\"" + tx.review + "\"");
                reviewLbl.setFont(new Font("Segoe UI Emoji", Font.ITALIC, 12));
                reviewLbl.setForeground(COL_GRAY);
                ratingRow.add(reviewLbl);
            }
            card.add(ratingRow);
            card.add(Box.createVerticalStrut(4));
        }

        // ── Separator + Total ──
        card.add(separator());
        card.add(Box.createVerticalStrut(8));

        JPanel bottomRow = new JPanel(new BorderLayout());
        bottomRow.setBackground(COL_CARD_BG);
        bottomRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        JLabel totalKey = new JLabel("Total Paid");
        totalKey.setFont(FONT_BOLD);
        JLabel totalVal = new JLabel(formatRupiah(tx.grandTotal));
        totalVal.setFont(new Font("Segoe UI Emoji", Font.BOLD, 15));
        totalVal.setForeground(COL_GREEN);

        // Rate button
        String rateBtnText = tx.isRated() ? "\u2605 Edit Rating" : "\u2606 Give Rating";
        JButton rateBtn = new JButton(rateBtnText);
        rateBtn.setFont(new Font("Dialog", Font.BOLD, 12));
        rateBtn.setForeground(tx.isRated() ? COL_HEADER : COL_TEAL);
        rateBtn.setBackground(Color.WHITE);
        rateBtn.setOpaque(true);
        rateBtn.setContentAreaFilled(true);
        rateBtn.setBorderPainted(true);
        rateBtn.setFocusPainted(false);
        rateBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        rateBtn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(tx.isRated() ? COL_HEADER : COL_TEAL, 1, true),
                BorderFactory.createEmptyBorder(6, 14, 6, 14)));

        // Click rate → open RatingDialog → refresh card after saving
        rateBtn.addActionListener(e ->
            RatingDialog.show(this, tx, () -> populateList())
        );

        JPanel rightSide = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightSide.setBackground(COL_CARD_BG);
        rightSide.add(totalVal);
        rightSide.add(rateBtn);

        bottomRow.add(totalKey, BorderLayout.WEST);
        bottomRow.add(rightSide, BorderLayout.EAST);
        card.add(bottomRow);

        return card;
    }

    // ── Info row ──────────────────────────────────────────────
    private JPanel infoRow(String key, String value) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setBackground(COL_CARD_BG);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));
        JLabel k = new JLabel(key);   k.setFont(FONT_SMALL); k.setForeground(COL_GRAY);
        k.setPreferredSize(new Dimension(90, 20));
        JLabel v = new JLabel(value); v.setFont(FONT_SMALL); v.setForeground(Color.DARK_GRAY);
        row.add(k, BorderLayout.WEST);
        row.add(v, BorderLayout.CENTER);
        return row;
    }

    private JSeparator separator() {
        JSeparator sep = new JSeparator();
        sep.setForeground(COL_CARD_BDR);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        return sep;
    }

    // ── Footer ────────────────────────────────────────────────
    private JPanel buildFooter() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(COL_BG);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220, 220, 220)),
                BorderFactory.createEmptyBorder(12, 24, 14, 24)));

        JButton closeBtn = new JButton("Close");
        closeBtn.setFont(FONT_BOLD);
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setBackground(COL_HEADER);
        closeBtn.setOpaque(true);
        closeBtn.setContentAreaFilled(true);
        closeBtn.setBorderPainted(false);
        closeBtn.setFocusPainted(false);
        closeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeBtn.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));
        closeBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { closeBtn.setBackground(COL_HEADER.darker()); }
            public void mouseExited (MouseEvent e) { closeBtn.setBackground(COL_HEADER); }
        });
        closeBtn.addActionListener(e -> dispose());
        p.add(closeBtn, BorderLayout.CENTER);
        return p;
    }

    // ── Format Rupiah ─────────────────────────────────────────
    private String formatRupiah(double amount) {
        if (amount <= 0) return "Rp 0";
        long val = Math.round(amount);
        String raw = String.valueOf(val);
        StringBuilder sb = new StringBuilder();
        int start = raw.length() % 3;
        if (start > 0) sb.append(raw, 0, start);
        for (int i = start; i < raw.length(); i += 3) {
            if (sb.length() > 0) sb.append(".");
            sb.append(raw, i, i + 3);
        }
        return "Rp " + sb;
    }

    // ── Static factory ────────────────────────────────────────
    public static void show(Component parent) {
        Frame owner = (Frame) SwingUtilities.getWindowAncestor(parent);
        new OrderHistoryDialog(owner).setVisible(true);
    }
}
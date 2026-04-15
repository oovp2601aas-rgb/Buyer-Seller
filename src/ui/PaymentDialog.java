package ui;

import controller.ChatController;
import java.awt.*;
import java.awt.event.*;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javax.swing.*;

public class PaymentDialog extends JDialog {

    private static final Color COL_BG      = Color.WHITE;
    private static final Color COL_HEADER  = new Color(103, 58, 183);
    private static final Color COL_TEAL    = new Color(0, 150, 136);
    private static final Color COL_LIGHT   = new Color(245, 245, 255);
    private static final Color COL_SUCCESS = new Color(27, 94, 32);
    private static final Color COL_LINE    = new Color(220, 220, 220);
    private static final Color COL_WA      = new Color(37, 211, 102);

    private static final Font FONT_TITLE = new Font("Segoe UI Emoji", Font.BOLD, 17);
    private static final Font FONT_BOLD  = new Font("Segoe UI Emoji", Font.BOLD, 14);
    private static final Font FONT_SMALL = new Font("Segoe UI Emoji", Font.PLAIN, 12);
    private static final Font FONT_MONO  = new Font("Segoe UI Emoji", Font.BOLD, 13);

    private final String         orderSummary;
    private final double         baseTotal;   // subtotal asli sebelum diskon
    private double               finalTotal;  // total setelah diskon, bisa berubah
    private final String         address;
    private final ChatController controller;
    private final List<String[]> chosenSellersInfo;

    private String      selectedMethod = null;
    private ButtonGroup methodGroup;
    private JPanel      cardPanel;
    private CardLayout  cards;

    // Voucher UI
    private JTextField voucherField;
    private JLabel     voucherStatusLabel;
    private JLabel     totalValLabel;      // label Grand Total yang akan diupdate
    private JLabel     discountRowLabel;   // baris info diskon

    // State voucher di dalam dialog (independen dari controller)
    private model.VoucherCode appliedVoucher = null;

    public PaymentDialog(Frame owner, String orderSummary, double grandTotal,
                         String address, ChatController controller,
                         List<String[]> chosenSellersInfo) {
        super(owner, "\uD83D\uDCB3 Payment", true);
        this.orderSummary      = orderSummary;
        this.baseTotal         = grandTotal;
        this.finalTotal        = grandTotal;
        this.address           = address;
        this.controller        = controller;
        this.chosenSellersInfo = chosenSellersInfo;

        setSize(480, 700);
        setResizable(false);
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        getContentPane().setBackground(COL_BG);

        cards     = new CardLayout();
        cardPanel = new JPanel(cards);
        cardPanel.setBackground(COL_BG);
        cardPanel.add(buildPaymentPanel(), "PAYMENT");
        cardPanel.add(buildReceiptPanel(), "RECEIPT");

        add(cardPanel);
        cards.show(cardPanel, "PAYMENT");
    }

    // ══════════════════════════════════════════════════════════
    //  PANEL 1 — PAYMENT
    // ══════════════════════════════════════════════════════════
    private JPanel buildPaymentPanel() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(COL_BG);

        JPanel header = new JPanel(new FlowLayout(FlowLayout.CENTER));
        header.setBackground(COL_HEADER);
        header.setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));
        JLabel title = new JLabel("\uD83D\uDCB3 Payment Confirmation");
        title.setFont(FONT_TITLE); title.setForeground(Color.WHITE);
        header.add(title);
        root.add(header, BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(COL_BG);
        body.setBorder(BorderFactory.createEmptyBorder(18, 24, 10, 24));

        // Order Summary
        body.add(sectionLabel("\uD83D\uDCCB Order Summary"));
        body.add(Box.createVerticalStrut(6));

        JTextArea summaryArea = new JTextArea(orderSummary);
        summaryArea.setEditable(false);
        summaryArea.setFont(FONT_SMALL);
        summaryArea.setBackground(COL_LIGHT);
        summaryArea.setForeground(new Color(40, 40, 80));
        summaryArea.setLineWrap(true);
        summaryArea.setWrapStyleWord(true);
        summaryArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COL_LINE, 1, true),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        summaryArea.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));
        body.add(summaryArea);

        // Baris info diskon (kosong dulu, diisi saat voucher dipakai)
        body.add(Box.createVerticalStrut(8));
        discountRowLabel = new JLabel(" ");
        discountRowLabel.setFont(FONT_SMALL);
        discountRowLabel.setForeground(new Color(27, 94, 32));
        discountRowLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(discountRowLabel);

        // Grand Total
        body.add(Box.createVerticalStrut(4));
        JPanel totalRow = new JPanel(new BorderLayout());
        totalRow.setBackground(COL_BG);
        totalRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        JLabel totalLbl = new JLabel("Grand Total"); totalLbl.setFont(FONT_BOLD);
        totalValLabel = new JLabel(formatRupiah(finalTotal));
        totalValLabel.setFont(new Font("Segoe UI Emoji", Font.BOLD, 16));
        totalValLabel.setForeground(COL_HEADER);
        totalRow.add(totalLbl, BorderLayout.WEST);
        totalRow.add(totalValLabel, BorderLayout.EAST);
        body.add(totalRow);

        // ── Voucher ───────────────────────────────────────────
        body.add(Box.createVerticalStrut(16));
        body.add(separator());
        body.add(Box.createVerticalStrut(14));
        body.add(sectionLabel("\uD83C\uDFF7\uFE0F Voucher"));
        body.add(Box.createVerticalStrut(8));

        voucherField = new JTextField();
        voucherField.setFont(FONT_SMALL);
        voucherField.setPreferredSize(new Dimension(150, 32));
        voucherField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 180, 240), 1, true),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)));

        JButton applyBtn = new JButton("Use");
        applyBtn.setFont(FONT_SMALL);
        applyBtn.setForeground(COL_HEADER);
        applyBtn.setBackground(COL_BG);
        applyBtn.setFocusPainted(false);
        applyBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        applyBtn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 180, 240), 1, true),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)));

        JButton removeBtn = new JButton("Remove");
        removeBtn.setFont(FONT_SMALL);
        removeBtn.setForeground(Color.GRAY);
        removeBtn.setBackground(COL_BG);
        removeBtn.setFocusPainted(false);
        removeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        removeBtn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COL_LINE, 1, true),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)));

        voucherStatusLabel = new JLabel(" ");
        voucherStatusLabel.setFont(FONT_SMALL);

        JPanel voucherRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        voucherRow.setBackground(COL_BG);
        voucherRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        voucherRow.add(voucherField);
        voucherRow.add(applyBtn);
        voucherRow.add(removeBtn);
        voucherRow.add(voucherStatusLabel);
        body.add(voucherRow);

        // Tombol Pakai — hitung dan update label langsung di dialog
        applyBtn.addActionListener(e -> {
            String code = voucherField.getText().trim().toUpperCase().replaceAll("\\s+", "");
            model.VoucherCode voucher = getVoucher(code);
            if (voucher == null) {
                voucherStatusLabel.setText("The voucher code is invalid.");
                voucherStatusLabel.setForeground(new Color(198, 40, 40));
                return;
            }
            if (baseTotal < voucher.getMinOrder()) {
                voucherStatusLabel.setText("Minimum purchase " + formatRupiah(voucher.getMinOrder()) + " for this voucher.");
                voucherStatusLabel.setForeground(new Color(198, 40, 40));
                return;
            }
            appliedVoucher = voucher;
            double disc = voucher.calculateDiscount(baseTotal);
            finalTotal   = baseTotal - disc;

            // Update label Grand Total langsung
            totalValLabel.setText(formatRupiah(finalTotal));

            // Tampilkan info diskon
            String discLabel = voucher.getType() == model.VoucherCode.Type.PERCENT
                    ? "Diskon " + (int) voucher.getValue() + "%"
                    : "Voucher " + voucher.getCode();
            discountRowLabel.setText("Subtotal: " + formatRupiah(baseTotal)
                    + "   |   " + discLabel + ": -" + formatRupiah(disc));

            voucherStatusLabel.setText("The voucher has been successfully applied!");
            voucherStatusLabel.setForeground(new Color(27, 94, 32));
        });

        // Tombol Hapus
        removeBtn.addActionListener(e -> {
            appliedVoucher = null;
            finalTotal     = baseTotal;
            totalValLabel.setText(formatRupiah(finalTotal));
            discountRowLabel.setText(" ");
            voucherField.setText("");
            voucherStatusLabel.setText("Voucher removed.");
            voucherStatusLabel.setForeground(Color.GRAY);
        });

        // ── Metode Pembayaran ─────────────────────────────────
        body.add(Box.createVerticalStrut(16));
        body.add(separator());
        body.add(Box.createVerticalStrut(14));
        body.add(sectionLabel("\uD83D\uDCB0 Select Payment Method"));
        body.add(Box.createVerticalStrut(8));

        methodGroup = new ButtonGroup();
        String[][] methods = {
            { "transfer", "\uD83C\uDFE6  Bank Transfer",         "BCA / Mandiri / BNI / BRI"   },
            { "qris",     "\uD83D\uDCF1  QRIS",                  "Scan QR from any e-wallet"   },
            { "ewallet",  "\uD83D\uDC9C  E-Wallet",              "GoPay / OVO / Dana"          },
            { "cod",      "\uD83D\uDEF5  COD - Cash on Delivery", "Pay when your order arrives" },
        };
        for (String[] m : methods) {
            body.add(buildMethodRow(m[0], m[1], m[2]));
            body.add(Box.createVerticalStrut(6));
        }

        JScrollPane scroll = new JScrollPane(body);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(14);
        root.add(scroll, BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(COL_BG);
        footer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, COL_LINE),
                BorderFactory.createEmptyBorder(12, 24, 14, 24)));

        JButton payBtn = new JButton("\u2705  Pay Now");
        payBtn.setFont(new Font("Segoe UI Emoji", Font.BOLD, 15));
        payBtn.setForeground(Color.WHITE);
        payBtn.setBackground(COL_TEAL);
        payBtn.setOpaque(true); payBtn.setContentAreaFilled(true);
        payBtn.setBorderPainted(false); payBtn.setFocusPainted(false);
        payBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        payBtn.setBorder(BorderFactory.createEmptyBorder(12, 30, 12, 30));
        payBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { payBtn.setBackground(COL_TEAL.darker()); }
            public void mouseExited (MouseEvent e) { payBtn.setBackground(COL_TEAL); }
        });
        payBtn.addActionListener(e -> {
            if (selectedMethod == null) {
                JOptionPane.showMessageDialog(this,
                        "Please select a payment method first!",
                        "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }
            // Simpan dengan finalTotal (sudah dipotong diskon)
            model.OrderHistory.getInstance().add(
                new model.OrderHistory.Transaction(
                    methodLabel(selectedMethod), orderSummary, finalTotal, address)
            );
            if (controller != null) controller.clearCart();
            cardPanel.remove(1);
            cardPanel.add(buildReceiptPanel(), "RECEIPT");
            cards.show(cardPanel, "RECEIPT");
        });

        footer.add(payBtn, BorderLayout.CENTER);
        root.add(footer, BorderLayout.SOUTH);
        return root;
    }

    // ── Daftar voucher yang tersedia ──────────────────────────
    private model.VoucherCode getVoucher(String code) {
        if (code == null || code.isEmpty()) return null;
        switch (code) {
            case "HEMAT10":
                return new model.VoucherCode("HEMAT10",   model.VoucherCode.Type.PERCENT, 10,    0);
            case "DISKON20K":
                return new model.VoucherCode("DISKON20K", model.VoucherCode.Type.FIXED,   20000, 50000);
            default:
                return null;
        }
    }

    private JPanel buildMethodRow(String key, String label, String sub) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setBackground(COL_BG);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COL_LINE, 1, true),
                BorderFactory.createEmptyBorder(8, 14, 8, 14)));
        row.setName(key);
        row.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JRadioButton rb = new JRadioButton();
        rb.setBackground(COL_BG); rb.setFocusPainted(false);
        methodGroup.add(rb);
        rb.addActionListener(e -> { selectedMethod = key; recolorMethodRows(row); });

        JPanel text = new JPanel(new GridLayout(2, 1, 0, 1));
        text.setBackground(COL_BG);
        JLabel lbl    = new JLabel(label); lbl.setFont(FONT_BOLD);
        JLabel sublbl = new JLabel(sub);   sublbl.setFont(FONT_SMALL); sublbl.setForeground(Color.GRAY);
        text.add(lbl); text.add(sublbl);

        MouseAdapter rowClick = new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { rb.doClick(); }
        };
        row.addMouseListener(rowClick); text.addMouseListener(rowClick);
        lbl.addMouseListener(rowClick); sublbl.addMouseListener(rowClick);

        row.add(rb,   BorderLayout.WEST);
        row.add(text, BorderLayout.CENTER);
        return row;
    }

    private void recolorMethodRows(JPanel selected) {
        Container scroll = (Container) ((JPanel) cardPanel.getComponent(0)).getComponent(1);
        JPanel body = (JPanel) ((JScrollPane) scroll).getViewport().getView();
        for (Component c : body.getComponents()) {
            if (c instanceof JPanel && ((JPanel) c).getName() != null) {
                JPanel row = (JPanel) c;
                boolean isSel = (row == selected);
                row.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(isSel ? COL_HEADER : COL_LINE, isSel ? 2 : 1, true),
                        BorderFactory.createEmptyBorder(8, 14, 8, 14)));
                setAllBg(row, isSel ? new Color(237, 231, 246) : COL_BG);
            }
        }
    }

    private void setAllBg(JPanel p, Color c) {
        p.setBackground(c);
        for (Component ch : p.getComponents()) {
            ch.setBackground(c);
            if (ch instanceof JPanel) setAllBg((JPanel) ch, c);
        }
    }

    // ══════════════════════════════════════════════════════════
    //  PANEL 2 — RECEIPT
    // ══════════════════════════════════════════════════════════
    private JPanel buildReceiptPanel() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(COL_BG);

        JPanel header = new JPanel(new FlowLayout(FlowLayout.CENTER));
        header.setBackground(new Color(46, 125, 50));
        header.setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));
        JLabel title = new JLabel("\u2705  Payment Successful!");
        title.setFont(FONT_TITLE); title.setForeground(Color.WHITE);
        header.add(title);
        root.add(header, BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(COL_BG);
        body.setBorder(BorderFactory.createEmptyBorder(20, 28, 20, 28));

        String txId   = "TXN-" + System.currentTimeMillis() % 100000;
        String nowStr = new SimpleDateFormat("dd MMM yyyy, HH:mm").format(new Date());

        body.add(receiptRow("Transaction No.", txId,                       true));
        body.add(Box.createVerticalStrut(6));
        body.add(receiptRow("Date",            nowStr,                      false));
        body.add(Box.createVerticalStrut(6));
        body.add(receiptRow("Payment Method",  methodLabel(selectedMethod), false));
        if (!address.isEmpty()) {
            body.add(Box.createVerticalStrut(6));
            body.add(receiptRow("Address", address, false));
        }

        body.add(Box.createVerticalStrut(14));
        body.add(separator());
        body.add(Box.createVerticalStrut(14));
        body.add(sectionLabel("\uD83D\uDCE6 Order"));
        body.add(Box.createVerticalStrut(6));

        String itemsHtml = "<html><body style='width:380px; font-family: Segoe UI Emoji, Arial; font-size: 11pt; color: #282850;'>"
                + orderSummary.replace("\n", "<br>").replace(" ", "&nbsp;")
                + "</body></html>";
        JLabel items = new JLabel(itemsHtml);
        items.setFont(FONT_MONO);
        items.setOpaque(true);
        items.setBackground(COL_LIGHT);
        items.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COL_LINE, 1, true),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        items.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        body.add(items);

        body.add(Box.createVerticalStrut(14));
        body.add(separator());
        body.add(Box.createVerticalStrut(10));

        // Baris diskon di receipt (kalau ada)
        if (appliedVoucher != null) {
            double disc = appliedVoucher.calculateDiscount(baseTotal);
            String discLabel = appliedVoucher.getType() == model.VoucherCode.Type.PERCENT
                    ? "Diskon " + (int) appliedVoucher.getValue() + "%"
                    : "Voucher " + appliedVoucher.getCode();

            JPanel subRow = new JPanel(new BorderLayout());
            subRow.setBackground(COL_BG);
            subRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
            JLabel sl = new JLabel("Subtotal"); sl.setFont(FONT_SMALL); sl.setForeground(Color.GRAY);
            JLabel sv = new JLabel(formatRupiah(baseTotal)); sv.setFont(FONT_SMALL); sv.setForeground(Color.GRAY);
            subRow.add(sl, BorderLayout.WEST); subRow.add(sv, BorderLayout.EAST);
            body.add(subRow);
            body.add(Box.createVerticalStrut(4));

            JPanel discRow = new JPanel(new BorderLayout());
            discRow.setBackground(COL_BG);
            discRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
            JLabel dl = new JLabel(discLabel); dl.setFont(FONT_SMALL); dl.setForeground(new Color(27, 94, 32));
            JLabel dv = new JLabel("-" + formatRupiah(disc)); dv.setFont(FONT_SMALL); dv.setForeground(new Color(27, 94, 32));
            discRow.add(dl, BorderLayout.WEST); discRow.add(dv, BorderLayout.EAST);
            body.add(discRow);
            body.add(Box.createVerticalStrut(6));
        }

        JPanel totalRow = new JPanel(new BorderLayout());
        totalRow.setBackground(COL_BG);
        totalRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        JLabel tl = new JLabel("TOTAL PAID"); tl.setFont(FONT_BOLD);
        JLabel tv = new JLabel(formatRupiah(finalTotal));   // pakai finalTotal setelah diskon
        tv.setFont(new Font("Segoe UI Emoji", Font.BOLD, 18));
        tv.setForeground(COL_SUCCESS);
        totalRow.add(tl, BorderLayout.WEST);
        totalRow.add(tv, BorderLayout.EAST);
        body.add(totalRow);

        if (chosenSellersInfo != null && !chosenSellersInfo.isEmpty()) {
            body.add(Box.createVerticalStrut(20));
            body.add(separator());
            body.add(Box.createVerticalStrut(14));
            body.add(sectionLabel("\uD83D\uDCAC Contact Seller via WhatsApp"));
            body.add(Box.createVerticalStrut(4));

            JLabel hint = new JLabel("Automatic message with your order details will be sent to the seller.");
            hint.setFont(FONT_SMALL);
            hint.setForeground(Color.GRAY);
            hint.setAlignmentX(Component.LEFT_ALIGNMENT);
            body.add(hint);
            body.add(Box.createVerticalStrut(10));

            for (String[] info : chosenSellersInfo) {
                String sellerName  = info[0];
                String phone       = info[1];
                String sellerItems = info[2];
                String sellerTotal = info[3];
                String waMsg = buildWAMessagePerSeller(txId, nowStr, sellerName, sellerItems, sellerTotal);
                body.add(buildWAButton(sellerName, phone, waMsg));
                body.add(Box.createVerticalStrut(6));
            }
        }

        body.add(Box.createVerticalStrut(10));
        JLabel thanks = new JLabel("\uD83C\uDF89  Thank you for your purchase!");
        thanks.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        thanks.setForeground(COL_TEAL);
        thanks.setAlignmentX(Component.CENTER_ALIGNMENT);
        body.add(thanks);

        JScrollPane scroll = new JScrollPane(body);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(14);
        root.add(scroll, BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(COL_BG);
        footer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, COL_LINE),
                BorderFactory.createEmptyBorder(12, 24, 14, 24)));
        JButton closeBtn = new JButton("Close");
        closeBtn.setFont(FONT_BOLD);
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setBackground(COL_HEADER);
        closeBtn.setOpaque(true); closeBtn.setContentAreaFilled(true);
        closeBtn.setBorderPainted(false); closeBtn.setFocusPainted(false);
        closeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeBtn.setBorder(BorderFactory.createEmptyBorder(12, 30, 12, 30));
        closeBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { closeBtn.setBackground(COL_HEADER.darker()); }
            public void mouseExited (MouseEvent e) { closeBtn.setBackground(COL_HEADER); }
        });
        closeBtn.addActionListener(e -> dispose());
        footer.add(closeBtn, BorderLayout.CENTER);
        root.add(footer, BorderLayout.SOUTH);

        return root;
    }

    private JPanel buildWAButton(String sellerName, String phone, String message) {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(COL_BG);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));

        JButton btn = new JButton("\uD83D\uDCF2  Chat " + sellerName + " on WhatsApp");
        btn.setFont(new Font("Segoe UI Emoji", Font.BOLD, 13));
        btn.setForeground(Color.WHITE);
        btn.setBackground(COL_WA);
        btn.setOpaque(true); btn.setContentAreaFilled(true);
        btn.setBorderPainted(false); btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(COL_WA.darker()); }
            public void mouseExited (MouseEvent e) { btn.setBackground(COL_WA); }
        });
        btn.addActionListener(e -> openWhatsApp(phone, message));

        row.add(btn, BorderLayout.CENTER);
        return row;
    }

    private String buildWAMessagePerSeller(String txId, String date,
                                           String sellerName, String sellerItems,
                                           String sellerTotal) {
        StringBuilder sb = new StringBuilder();
        sb.append("Hello ").append(sellerName).append("! \uD83D\uDE0A\n\n");
        sb.append("I would like to confirm my order:\n\n");
        sb.append("Transaction No. : ").append(txId).append("\n");
        sb.append("Date            : ").append(date).append("\n");
        sb.append("Payment Method  : ").append(methodLabel(selectedMethod)).append("\n");
        if (!address.isEmpty()) {
            sb.append("Address         : ").append(address).append("\n");
        }
        sb.append("\nOrder from ").append(sellerName).append(":\n");
        sb.append(sellerItems);
        sb.append("\nTotal: ").append(sellerTotal).append("\n\n");
        sb.append("Please confirm my order. Thank you! \uD83D\uDE4F");
        return sb.toString();
    }

    private void openWhatsApp(String phone, String message) {
        try {
            String encoded = java.net.URLEncoder.encode(message, "UTF-8");
            Desktop.getDesktop().browse(new URI("https://wa.me/" + phone + "?text=" + encoded));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Cannot open WhatsApp: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ── Helpers ───────────────────────────────────────────────
    private JPanel receiptRow(String key, String value, boolean highlight) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setBackground(COL_BG);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        JLabel kl = new JLabel(key); kl.setFont(FONT_SMALL); kl.setForeground(Color.GRAY);
        kl.setPreferredSize(new Dimension(120, 20));
        JLabel vl = new JLabel(value); vl.setFont(highlight ? FONT_MONO : FONT_SMALL);
        vl.setForeground(highlight ? COL_HEADER : Color.DARK_GRAY);
        row.add(kl, BorderLayout.WEST);
        row.add(vl, BorderLayout.CENTER);
        return row;
    }

    private String methodLabel(String key) {
        if (key == null) return "-";
        switch (key) {
            case "transfer": return "Bank Transfer";
            case "qris":     return "QRIS";
            case "ewallet":  return "E-Wallet (GoPay/OVO/Dana)";
            case "cod":      return "COD - Cash on Delivery";
            default:         return key;
        }
    }

    private JLabel sectionLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_BOLD);
        lbl.setForeground(new Color(70, 70, 100));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private JSeparator separator() {
        JSeparator sep = new JSeparator();
        sep.setForeground(COL_LINE);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        return sep;
    }

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
    public static void show(Component parent, String orderSummary, double grandTotal,
                            String address, ChatController controller,
                            List<String[]> chosenSellersInfo) {
        Frame owner = (Frame) SwingUtilities.getWindowAncestor(parent);
        new PaymentDialog(owner, orderSummary, grandTotal, address,
                controller, chosenSellersInfo).setVisible(true);
    }

    public static void show(Component parent, String orderSummary,
                            double grandTotal, String address, ChatController controller) {
        show(parent, orderSummary, grandTotal, address, controller, new java.util.ArrayList<>());
    }

    public static void show(Component parent, String orderSummary,
                            double grandTotal, String address) {
        show(parent, orderSummary, grandTotal, address, null, new java.util.ArrayList<>());
    }
}

package ui;

import controller.ChatController;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;
import model.ChatRequest;

/**
 * SellerPanel - Dashboard for 1 seller
 *
 * CHANGES:
 * - Has sellerIndex (0, 1, 2) and sellerName ("Seller 1", etc)
 * - Header displays the seller name
 * - sellerIndex is passed to RequestPanel so form submit knows which seller
 */
public class SellerPanel extends JPanel {

    private ChatController controller;
    private JPanel         requestsContainer;
    private Map<Integer, RequestPanel> requestPanels = new HashMap<>();

    private int    sellerIndex; // 0, 1, 2
    private String sellerName;  // "Seller 1", "Seller 2", "Seller 3"

    private static final String[] SELLER_STORE_NAMES = {
        "Maju Jaya Store",
        "Sejahtera Stall",
        "Nusantara Kitchen"
    };

    private static final String[] SELLER_PHONES = {
        "081234567890",
        "082345678901",
        "083456789012"
    };

    private static final String[] SELLER_EMAILS = {
        "seller1@email.com",
        "seller2@email.com",
        "seller3@email.com"
    };

    private static final String[] SELLER_ADDRESSES = {
        "Merdeka St. No. 1, Jakarta",
        "Sudirman St. No. 2, Bandung",
        "Gatot Subroto St. No. 3, Surabaya"
    };

    // Header color per seller
    private static final Color[] SELLER_COLORS = {
        new Color(33, 150, 243),   // Seller 1 - Blue
        new Color(0,  150, 136),   // Seller 2 - Teal
        new Color(233, 30, 99)     // Seller 3 - Pink
    };

    private static final String[] SELLER_ICONS = {
        "\uD83D\uDC68\u200D\uD83C\uDF73",   // 👨‍🍳 Seller 1
        "\uD83D\uDC69\u200D\uD83C\uDF73",   // 👩‍🍳 Seller 2
        "\uD83E\uDDD1\u200D\uD83C\uDF73"    // 🧑‍🍳 Seller 3
    };

    public SellerPanel(int sellerIndex) {
        this.sellerIndex = sellerIndex;
        this.sellerName  = "Seller " + (sellerIndex + 1);
        initComponents();
    }

    /** Default constructor (backward compat) */
    public SellerPanel() {
        this(0);
    }

    private void initComponents() {
        setLayout(new BorderLayout(0, 0));
        setBackground(Color.WHITE);

        String icon      = sellerIndex < SELLER_ICONS.length       ? SELLER_ICONS[sellerIndex]       : "\uD83D\uDC68\u200D\uD83DDCBC";
        Color  color     = sellerIndex < SELLER_COLORS.length      ? SELLER_COLORS[sellerIndex]      : new Color(33, 150, 243);
        String storeName = sellerIndex < SELLER_STORE_NAMES.length ? SELLER_STORE_NAMES[sellerIndex] : sellerName;
        String phone     = sellerIndex < SELLER_PHONES.length      ? SELLER_PHONES[sellerIndex]      : "-";
        String email     = sellerIndex < SELLER_EMAILS.length      ? SELLER_EMAILS[sellerIndex]      : "-";
        String address   = sellerIndex < SELLER_ADDRESSES.length   ? SELLER_ADDRESSES[sellerIndex]   : "-";

        // ── Header ──
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBackground(color);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        // Row 1: icon + seller name + store name
        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        row1.setOpaque(false);

        JLabel iconLabel = new JLabel(icon + " " + sellerName);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.BOLD, 20));
        iconLabel.setForeground(Color.WHITE);

        JLabel storeLabel = new JLabel("— " + storeName);
        storeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        storeLabel.setForeground(new Color(220, 220, 220));

        row1.add(iconLabel);
        row1.add(storeLabel);

        // Row 2: phone + email + address
        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 4));
        row2.setOpaque(false);

        JLabel phoneLabel   = new JLabel("\uD83D\uDCF1 " + phone);
        JLabel emailLabel   = new JLabel("\u2709\uFE0F " + email);
        JLabel addressLabel = new JLabel("\uD83D\uDCCD " + address);

        Font contactFont = new Font("Segoe UI Emoji", Font.PLAIN, 12);
        Color contactColor = new Color(200, 230, 255);

        for (JLabel lbl : new JLabel[]{phoneLabel, emailLabel, addressLabel}) {
            lbl.setFont(contactFont);
            lbl.setForeground(contactColor);
            row2.add(lbl);
        }

        headerPanel.add(row1);
        headerPanel.add(row2);
        add(headerPanel, BorderLayout.NORTH);

        // ── Requests container ──
        requestsContainer = new JPanel();
        requestsContainer.setLayout(new BoxLayout(requestsContainer, BoxLayout.Y_AXIS));
        requestsContainer.setBackground(Color.WHITE);
        requestsContainer.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

        JScrollPane scrollPane = new JScrollPane(requestsContainer);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);
    }

    public void setController(ChatController c) {
        this.controller = c;
    }

    public String getSellerName()  { return sellerName; }
    public int    getSellerIndex() { return sellerIndex; }

    public void addRequest(ChatRequest request) {
        // Pass sellerIndex to RequestPanel
        RequestPanel rp = new RequestPanel(request, controller, sellerIndex);
        requestPanels.put(request.getRequestId(), rp);
        requestsContainer.add(rp);
        requestsContainer.revalidate();
        requestsContainer.repaint();
        scrollToBottom();
    }

    public void updateRequest(ChatRequest r) {
        RequestPanel p = requestPanels.get(r.getRequestId());
        if (p != null) p.updateRequest(r);
    }

    public void fillFormField(int requestId, int formIndex, String value) {
        RequestPanel p = requestPanels.get(requestId);
        if (p != null) p.fillForm(formIndex, value);
    }

    public RequestPanel getRequestPanel(int id) {
        return requestPanels.get(id);
    }

    public void clearAllRequests() {
        requestsContainer.removeAll();
        requestPanels.clear();
        requestsContainer.revalidate();
        requestsContainer.repaint();
    }

    private void scrollToBottom() {
        SwingUtilities.invokeLater(() -> {
            JScrollBar sb = ((JScrollPane) requestsContainer.getParent().getParent()).getVerticalScrollBar();
            sb.setValue(sb.getMaximum());
        });
    }
}
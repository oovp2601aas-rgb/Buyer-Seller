package ui;

import controller.ChatController;
import java.awt.*;
import java.awt.event.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.*;
import model.RecommendationItem;

public class BuyerPanel extends JPanel {

    private JTextField     messageField;
    private JTextField     addressField;
    private CircularButton sendButton;
    private JPanel         chatArea;
    private ChatController controller;

    private Set<String>         appendedReplies = new HashSet<>();
    private Set<String>         removedWaiting  = new HashSet<>();

    private Map<String, JPanel> recBubbles = new HashMap<>();

    public BuyerPanel() {
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout(0, 0));
        setBackground(Color.WHITE);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)));

        JLabel headerLabel = new JLabel("\uD83D\uDECD Buyer Chat", SwingConstants.CENTER);
        headerLabel.setFont(new Font("Segoe UI Emoji", Font.BOLD, 18));
        headerLabel.setForeground(new Color(103, 58, 183));
        headerLabel.setBorder(BorderFactory.createEmptyBorder(12, 0, 12, 0));

        JButton historyBtn = new JButton("\uD83D\uDCCB History");
        historyBtn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 12));
        historyBtn.setForeground(new Color(103, 58, 183));
        historyBtn.setBackground(Color.WHITE);
        historyBtn.setFocusPainted(false);
        historyBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        historyBtn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 180, 240), 1, true),
                BorderFactory.createEmptyBorder(6, 14, 6, 14)));
        historyBtn.addActionListener(e -> OrderHistoryDialog.show(this));

        JPanel historyWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 8));
        historyWrapper.setBackground(Color.WHITE);
        historyWrapper.add(historyBtn);

        headerPanel.add(headerLabel,    BorderLayout.CENTER);
        headerPanel.add(historyWrapper, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        chatArea = new JPanel();
        chatArea.setLayout(new BoxLayout(chatArea, BoxLayout.Y_AXIS));
        chatArea.setBackground(Color.WHITE);
        chatArea.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));

        JScrollPane scrollPane = new JScrollPane(chatArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        JPanel bottomArea = new JPanel(new BorderLayout(0, 0));
        bottomArea.setBackground(Color.WHITE);
        bottomArea.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(230, 230, 230)));

        JPanel addressPanel = new JPanel(new BorderLayout(8, 0));
        addressPanel.setBackground(Color.WHITE);
        addressPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 0, 20));
        JLabel addressIcon = new JLabel("\uD83D\uDCCD Address:");
        addressIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 13));
        addressIcon.setForeground(new Color(103, 58, 183));
        addressIcon.setPreferredSize(new Dimension(80, 32));
        addressField = new JTextField();
        addressField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        addressField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 180, 240), 1, true),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)));
        addressPanel.add(addressIcon,  BorderLayout.WEST);
        addressPanel.add(addressField, BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new BorderLayout(15, 0));
        inputPanel.setBackground(Color.WHITE);
        inputPanel.setBorder(BorderFactory.createEmptyBorder(8, 20, 15, 20));
        messageField = new JTextField();
        messageField.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        messageField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1, true),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)));
        sendButton = new CircularButton("\u27A4", new Color(103, 58, 183), 45);
        inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(sendButton,   BorderLayout.EAST);

        bottomArea.add(addressPanel, BorderLayout.NORTH);
        bottomArea.add(inputPanel,   BorderLayout.SOUTH);
        add(bottomArea, BorderLayout.SOUTH);

        sendButton.addActionListener(e -> sendMessage());
        messageField.addActionListener(e -> sendMessage());
    }

    private void sendMessage() {
        String msg = messageField.getText().trim();
        if (!msg.isEmpty() && controller != null) {
            controller.onBuyerMessageSent(msg);
            messageField.setText("");
        }
    }

    public void setController(ChatController c) { this.controller = c; }
    public String getAddress() { return addressField.getText().trim(); }

    public void displayBuyerMessage(String message) {
        chatArea.add(new Bubble(message, Bubble.BubbleType.BUYER));
        refresh();
    }

    public void displayWaitingForSeller(int requestId, int sellerIndex, String sellerName) {
        Bubble b = new Bubble("\u23F3 Waiting for " + sellerName + " data...", Bubble.BubbleType.WAITING);
        b.setRequestId(requestId);
        b.setFormIndex(sellerIndex);
        chatArea.add(b);
        refresh();
    }

    public void replaceWaitingBubble(int requestId, int sellerIndex, int formIndex, String response) {
        String waitKey = requestId + "-wait-" + sellerIndex;
        if (!removedWaiting.contains(waitKey)) {
            for (int i = 0; i < chatArea.getComponentCount(); i++) {
                Component c = chatArea.getComponent(i);
                if (c instanceof Bubble) {
                    Bubble b = (Bubble) c;
                    if (b.getType() == Bubble.BubbleType.WAITING
                            && b.getRequestId() == requestId
                            && b.getFormIndex() == sellerIndex) {
                        chatArea.remove(i);
                        removedWaiting.add(waitKey);
                        break;
                    }
                }
            }
            refresh();
        }
    }

    public void appendSellerReply(int requestId, int sellerIndex, String response) {
        replaceWaitingBubble(requestId, sellerIndex, 1, response);
    }

    public void displayRecommendationChosen(RecommendationItem item, int quantity) {
        String recKey = recKey(item);
        if (recBubbles.containsKey(recKey)) return;

        JPanel bubble = buildChosenBubble(item, quantity, recKey);
        recBubbles.put(recKey, bubble);
        chatArea.add(bubble);
        refresh();
    }

    public void removeRecommendationChosen(RecommendationItem item) {
        String recKey = recKey(item);
        JPanel bubble = recBubbles.remove(recKey);
        if (bubble != null) {
            chatArea.remove(bubble);
            refresh();
        }
    }

    private JPanel buildChosenBubble(RecommendationItem item, int qty, String recKey) {
        JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        wrapper.setOpaque(false);
        wrapper.setName("REC_BUBBLE:" + recKey);

        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(232, 245, 233));
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                g2.setColor(new Color(129, 199, 132));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));

        JLabel topLabel = new JLabel("✅  Chosen from " + item.getSellerName());
        topLabel.setFont(new Font("Segoe UI Emoji", Font.BOLD, 12));
        topLabel.setForeground(new Color(27, 94, 32));
        card.add(topLabel);
        card.add(Box.createVerticalStrut(4));

        String menuName = stripPrefix(item.getRawMessage());
        String firstLine = menuName.split("\n")[0].trim();
        JLabel menuLabel = new JLabel("<html><body style='width:360px'>" + firstLine + "</body></html>");
        menuLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        menuLabel.setForeground(new Color(40, 40, 40));
        card.add(menuLabel);

        if (item.getUnitPrice() > 0) {
            card.add(Box.createVerticalStrut(4));
            JLabel priceLabel = new JLabel(qty + "x  ×  " + formatRp(item.getUnitPrice())
                    + "  =  " + formatRp(item.getUnitPrice() * qty));
            priceLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
            priceLabel.setForeground(new Color(27, 94, 32));
            card.add(priceLabel);
        }

        wrapper.add(card);
        return wrapper;
    }

    private String recKey(RecommendationItem item) {
        return item.getRequestId() + "-" + item.getSellerIndex()
                + "-" + item.getRawMessage().hashCode();
    }

    private String stripPrefix(String raw) {
        if (raw == null) return "";
        String s = raw.trim();
        if (s.startsWith("[") && s.contains("]")) s = s.substring(s.indexOf(']') + 1).trim();
        return s;
    }

    private String formatRp(double amount) {
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

    public void displayBuyerSummary(String message, double grandTotal,
                                    List<String[]> chosenSellersInfo) {
        for (int i = chatArea.getComponentCount() - 1; i >= 0; i--) {
            Component c = chatArea.getComponent(i);
            if (c instanceof JPanel && "SUMMARY_PANEL".equals(((JPanel) c).getName())) {
                chatArea.remove(i);
            }
        }

        JPanel summaryPanel = new JPanel();
        summaryPanel.setName("SUMMARY_PANEL");
        summaryPanel.setLayout(new BoxLayout(summaryPanel, BoxLayout.Y_AXIS));
        summaryPanel.setOpaque(false);
        summaryPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextArea area = new JTextArea(message);
        area.setWrapStyleWord(true);
        area.setLineWrap(true);
        area.setEditable(false);
        area.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        area.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        area.setBackground(new Color(197, 202, 233));
        area.setForeground(new Color(40, 53, 147));
        area.setMaximumSize(new Dimension(500, Integer.MAX_VALUE));
        area.setAlignmentX(Component.CENTER_ALIGNMENT);
        summaryPanel.add(area);
        summaryPanel.add(Box.createRigidArea(new Dimension(0, 8)));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        btnRow.setOpaque(false);
        btnRow.setAlignmentX(Component.CENTER_ALIGNMENT);

        String address = getAddress();

        JButton mapsBtn = new JButton("\uD83D\uDDFA\uFE0F Open Maps");
        mapsBtn.setFont(new Font("Segoe UI Emoji", Font.BOLD, 13));
        mapsBtn.setForeground(new Color(25, 118, 210));
        mapsBtn.setBackground(Color.WHITE);
        mapsBtn.setOpaque(true);
        mapsBtn.setContentAreaFilled(true);
        mapsBtn.setFocusPainted(false);
        mapsBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        mapsBtn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(25, 118, 210), 1, true),
                BorderFactory.createEmptyBorder(9, 18, 9, 18)));
        mapsBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { mapsBtn.setBackground(new Color(227, 242, 253)); }
            public void mouseExited (MouseEvent e) { mapsBtn.setBackground(Color.WHITE); }
        });
        mapsBtn.addActionListener(e -> openGoogleMaps(address));
        if (address.isEmpty()) {
            mapsBtn.setEnabled(false);
            mapsBtn.setToolTipText("Please fill in the address first");
        }

        Color lavBlue = new Color(121, 134, 203);
        JButton confirmBtn = new JButton("Confirm Purchase \uD83D\uDCB3");
        confirmBtn.setFont(new Font("Segoe UI Emoji", Font.BOLD, 14));
        confirmBtn.setForeground(Color.WHITE);
        confirmBtn.setBackground(lavBlue);
        confirmBtn.setOpaque(true);
        confirmBtn.setContentAreaFilled(true);
        confirmBtn.setBorderPainted(false);
        confirmBtn.setFocusPainted(false);
        confirmBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        confirmBtn.setBorder(BorderFactory.createEmptyBorder(10, 24, 10, 24));
        confirmBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { confirmBtn.setBackground(new Color(94, 108, 194)); }
            public void mouseExited (MouseEvent e) { confirmBtn.setBackground(lavBlue); }
        });
        confirmBtn.addActionListener(e ->
            PaymentDialog.show(this, message, grandTotal, address, controller, chosenSellersInfo)
        );

        btnRow.add(mapsBtn);
        btnRow.add(confirmBtn);
        summaryPanel.add(btnRow);
        summaryPanel.add(Box.createRigidArea(new Dimension(0, 6)));

        chatArea.add(summaryPanel);
        refresh();
    }

    public void displayBuyerSummary(String message, double grandTotal) {
        displayBuyerSummary(message, grandTotal, new ArrayList<>());
    }

    public void displayBuyerSummary(String message) {
        displayBuyerSummary(message, 0, new ArrayList<>());
    }

    private void openGoogleMaps(String address) {
        if (address == null || address.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Address not filled in!", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        try {
            String encoded = java.net.URLEncoder.encode(address.trim(), "UTF-8");
            Desktop.getDesktop().browse(new URI("https://www.google.com/maps/search/?api=1&query=" + encoded));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Cannot open browser: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void clearBuyerSummary() {
        for (int i = chatArea.getComponentCount() - 1; i >= 0; i--) {
            Component c = chatArea.getComponent(i);
            if (c instanceof JPanel && "SUMMARY_PANEL".equals(((JPanel) c).getName())) {
                chatArea.remove(i);
            }
        }
        JPanel notif = new JPanel(new FlowLayout(FlowLayout.CENTER));
        notif.setName("SUMMARY_PANEL");
        notif.setBackground(new Color(232, 245, 233));
        notif.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(165, 214, 167), 1, true),
                BorderFactory.createEmptyBorder(10, 20, 10, 20)));
        notif.setMaximumSize(new Dimension(500, 60));
        JLabel lbl = new JLabel("\u2705 Payment successful! Cart cleared, feel free to place a new order.");
        lbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 13));
        lbl.setForeground(new Color(27, 94, 32));
        notif.add(lbl);
        chatArea.add(notif);
        refresh();
    }

    private void refresh() {
        chatArea.revalidate();
        chatArea.repaint();
        scrollToBottom();
    }

    private void scrollToBottom() {
        SwingUtilities.invokeLater(() -> {
            JScrollBar sb = ((JScrollPane) chatArea.getParent().getParent()).getVerticalScrollBar();
            sb.setValue(sb.getMaximum());
        });
    }

    public void clearChat() {
        chatArea.removeAll();
        addressField.setText("");
        appendedReplies.clear();
        removedWaiting.clear();
        recBubbles.clear();
        refresh();
    }

    public void displayWaitingForSellers(int requestId) {}
    public void displayWaitingMessage(String label, int requestId, int formIndex) {}
    public void replaceSpecificWaitingBubble(int requestId, int formIndex, String response) {}
    public void replaceLastWaitingWithResponse(String response) {}
}
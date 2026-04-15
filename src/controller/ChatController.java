package controller;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.Timer;
import model.ChatRequest;
import model.RecommendationItem;
import service.SellerAIService;
import ui.BuyerPanel;
import ui.RecommendationPanel;
import ui.SellerPanel;
import util.MessageParser;

public class ChatController {

    private BuyerPanel              buyerPanel;
    private List<SellerPanel>       sellerPanels        = new ArrayList<>();
    private RecommendationPanel     recommendationPanel;
    private List<ChatRequest>       activeRequests;
    private int                     requestIdCounter;

    private Map<String, CartItem>   cart                = new LinkedHashMap<>();
    private Set<Integer>            chosenSellerIndices = new LinkedHashSet<>();

    private SellerAIService aiService;
    private Map<String, model.VoucherCode> availableVouchers = new LinkedHashMap<>();
    private model.VoucherCode              appliedVoucher     = null;

    public static class CartItem {
        public String message;
        public int    quantity;
        public double unitPrice;
        public int    sellerIndex;

        CartItem(String msg, int qty, double price, int sellerIdx) {
            this.message     = msg;
            this.quantity    = qty;
            this.unitPrice   = price;
            this.sellerIndex = sellerIdx;
        }
    }

    public ChatController() {
        this.activeRequests   = new ArrayList<>();
        this.requestIdCounter = 1;
        this.aiService        = new SellerAIService();
        registerDefaultVouchers();
    }

    public void setBuyerPanel(BuyerPanel bp) {
        this.buyerPanel = bp;
        bp.setController(this);
    }

    public void addSellerPanel(SellerPanel sp) {
        sellerPanels.add(sp);
        sp.setController(this);
    }

    public void setRecommendationPanel(RecommendationPanel rp) {
        this.recommendationPanel = rp;
        rp.setController(this);
    }

    public void onBuyerMessageSent(String message) {
        ChatRequest request = new ChatRequest(requestIdCounter++, message);
        request.setStatus(ChatRequest.Status.WAITING);
        activeRequests.add(request);

        buyerPanel.displayBuyerMessage(message);

        for (int i = 0; i < sellerPanels.size(); i++) {
            final int sellerIdx = i;
            String sellerName   = sellerPanels.get(i).getSellerName();
            Timer t = new Timer(400 + sellerIdx * 80, e ->
                buyerPanel.displayWaitingForSeller(request.getRequestId(), sellerIdx, sellerName)
            );
            t.setRepeats(false);
            t.start();
        }

        for (SellerPanel sp : sellerPanels) {
            sp.addRequest(request);
        }

        System.out.println("[ChatController] Broadcast REQ-" + request.getRequestId()
                + " to " + sellerPanels.size() + " sellers: " + message);
    }

    public void onSellerFormSubmit(int requestId, int formIndex, String value, int sellerIndex) {
        ChatRequest request = findRequestById(requestId);
        if (request == null) return;

        switch (formIndex) {
            case 1: request.setProductExplanation(value); break;
            case 2: request.setPriceEstimation(value);    break;
            case 3: request.setStockAvailability(value);  break;
        }

        String label       = getSellerName(sellerIndex);
        String displayText = "[" + label + " - Form " + formIndex + "] " + value;
        buyerPanel.replaceWaitingBubble(requestId, sellerIndex, formIndex, displayText);

        if (recommendationPanel != null) {
            // ── DEBUG ──
            System.out.println("[DEBUG] formIndex=" + formIndex + " value='" + value + "'");
            String[] segsDebug = value.split("\\|");
            System.out.println("[DEBUG] segments=" + segsDebug.length);
            for (String s : segsDebug) {
                System.out.println("[DEBUG] seg='" + s.trim() + "'");
                MessageParser.SellerItem it = MessageParser.parseSellerItem(s.trim());
                System.out.println("[DEBUG] parsed=" + (it == null ? "NULL" : it.name + " price=" + it.unitPrice + " size=" + it.sizeLabel + " qty=" + it.quantity));
            }
            MessageParser.BuyerIntent bi = MessageParser.parseBuyer(request.getBuyerMessage());
            System.out.println("[DEBUG] buyerMsg='" + request.getBuyerMessage() + "' intent.qty=" + bi.quantity);
            // ── END DEBUG ──

            String[] segments = value.split("\\|");

            if (segments.length > 1) {
                // ── Multiple variants (S/M/L) ──────────────────
                String sharedName = null;
                for (String seg : segments) {
                    seg = seg.trim();

                    // Segment ke-2+ yang tidak diawali huruf → prepend nama produk
                    if (sharedName != null && !seg.matches("(?i)^[a-zA-Z]+.*")) {
                        seg = sharedName + " " + seg;
                    }

                    MessageParser.SellerItem item = MessageParser.parseSellerItem(seg);
                    if (item == null) {
                        double price = parsePrice(seg);
                        if (price > 0) {
                            String fallbackLabel = (sharedName != null ? sharedName : seg);
                            RecommendationItem rec = new RecommendationItem(
                                    requestId, sellerIndex, label,
                                    fallbackLabel, price, 1, fallbackLabel);
                            recommendationPanel.addRecommendation(rec);
                        }
                        continue;
                    }

                    if (sharedName == null) sharedName = item.name;

                    // Qty: dari seller input, fallback dari buyer message
                    int qty = item.quantity;
                    if (qty == 1 && request != null) {
                        MessageParser.BuyerIntent intent =
                                MessageParser.parseBuyer(request.getBuyerMessage());
                        if (intent.quantity > 1) qty = intent.quantity;
                    }

                    String variantLabel = item.name
                            + (item.sizeLabel.isEmpty() ? "" : " [" + item.sizeLabel + "]")
                            + (item.volume > 0 ? " " + item.volume + item.volumeUnit : "")
                            + (item.readyTimes.isEmpty() ? "" : " (" + String.join("-", item.readyTimes) + ")");

                    RecommendationItem rec = new RecommendationItem(
                            requestId, sellerIndex, label,
                            variantLabel, item.unitPrice, qty, variantLabel);
                    recommendationPanel.addRecommendation(rec);
                }

            } else {
                // ── Single item ────────────────────────────────
                double price = parsePrice(value);

                int qty = 1;
                if (request != null) {
                    java.util.Map<String, Integer> qtyMap = parseBuyerQtyMap(request.getBuyerMessage());
                    String valueLower = value.toLowerCase();
                    for (java.util.Map.Entry<String, Integer> entry : qtyMap.entrySet()) {
                        if (valueLower.contains(entry.getKey())
                                || entry.getKey().contains(valueLower.split("\\s+")[0])) {
                            qty = entry.getValue();
                            break;
                        }
                    }
                    if (qty == 1 && !qtyMap.isEmpty()) {
                        qty = qtyMap.values().iterator().next();
                    }
                }

                MessageParser.SellerItem parsed = MessageParser.parseSellerItem(value);
                if (parsed != null) {
                    if (parsed.unitPrice > 0) price = parsed.unitPrice;
                    if (parsed.quantity  > 1) qty   = parsed.quantity;
                }

                String sellerName = getSellerName(sellerIndex);
                RecommendationItem recItem = new RecommendationItem(
                        requestId, sellerIndex, sellerName,
                        value, price, qty, displayText);
                recommendationPanel.addRecommendation(recItem);

                if (parsed != null && parsed.alternative != null) {
                    MessageParser.SellerItem alt = parsed.alternative;
                    String altLabel = alt.name + " x" + alt.quantity + " (bundle deal)";
                    RecommendationItem recAlt = new RecommendationItem(
                            requestId, sellerIndex, sellerName,
                            altLabel, alt.unitPrice, alt.quantity, altLabel);
                    recommendationPanel.addRecommendation(recAlt);
                }
            }
        }

        System.out.println("[ChatController] " + label + " submitted REQ-" + requestId + " Form-" + formIndex);
    }

    public void onSellerFormSubmit(int requestId, int formIndex, String value) {
        onSellerFormSubmit(requestId, formIndex, value, 0);
    }

    public void onAISuggestRequested(int requestId, int formIndex, int sellerIndex) {
        ChatRequest request = findRequestById(requestId);
        if (request == null) return;

        SellerAIService.ResponseType type;
        switch (formIndex) {
            case 2:  type = SellerAIService.ResponseType.PRICE_ESTIMATION;    break;
            case 3:  type = SellerAIService.ResponseType.STOCK_AVAILABILITY;  break;
            default: type = SellerAIService.ResponseType.PRODUCT_EXPLANATION; break;
        }

        String suggestion = aiService.generateResponse(request.getBuyerMessage(), type, requestId);
        if (sellerIndex >= 0 && sellerIndex < sellerPanels.size()) {
            sellerPanels.get(sellerIndex).fillFormField(requestId, formIndex, suggestion);
        }
    }

    public void onAISuggestRequested(int requestId, int formIndex) {
        onAISuggestRequested(requestId, formIndex, 0);
    }

    public void onRecommendationChosen(RecommendationItem item, int quantity) {
        String key = "rec-" + item.getRequestId() + "-" + item.getSellerIndex()
                   + "-" + item.getRawMessage().hashCode();
        cart.put(key, new CartItem(item.getRawMessage(), quantity,
                                   item.getUnitPrice(), item.getSellerIndex()));
        chosenSellerIndices.add(item.getSellerIndex());
        buyerPanel.displayRecommendationChosen(item, quantity);
        refreshSummary();
    }

    public void onRecommendationUnchosen(RecommendationItem item) {
        String key = "rec-" + item.getRequestId() + "-" + item.getSellerIndex()
                   + "-" + item.getRawMessage().hashCode();
        cart.remove(key);
        chosenSellerIndices.clear();
        for (CartItem ci : cart.values()) {
            chosenSellerIndices.add(ci.sellerIndex);
        }
        buyerPanel.removeRecommendationChosen(item);
        refreshSummary();
    }

    public void onBuyerChoose(int requestId, int sellerIndex, String message, int quantity, double unitPrice) {
        String key = requestId + "-" + sellerIndex + "-" + message.hashCode();
        cart.put(key, new CartItem(message, quantity, unitPrice, sellerIndex));
        chosenSellerIndices.add(sellerIndex);
        refreshSummary();
    }

    public void onBuyerChoose(int requestId, int sellerIndex, String message, int quantity) {
        onBuyerChoose(requestId, sellerIndex, message, quantity, 0.0);
    }

    public void onBuyerChoose(int requestId, int sellerIndex, String message) {
        onBuyerChoose(requestId, sellerIndex, message, 1, 0.0);
    }

    private void refreshSummary() {
        if (buyerPanel == null || cart.isEmpty()) return;

        StringBuilder sb = new StringBuilder();
        sb.append("\uD83E\uDDFE Order Summary\n");
        sb.append("--------------------------\n\n");

        String address = buyerPanel.getAddress();
        if (!address.isEmpty()) {
            sb.append("\uD83D\uDCCD Address: ").append(address).append("\n\n");
        }

        double  grandTotal = 0;
        boolean hasPrice   = false;

        for (CartItem item : cart.values()) {
            String title = item.message.split("\n")[0].trim();
            if (title.startsWith("[") && title.contains("]")) {
                title = title.substring(title.indexOf("]") + 1).trim();
            }
            sb.append("• ").append(title).append("  x").append(item.quantity);

            if (item.unitPrice > 0) {
                double sub = item.unitPrice * item.quantity;
                grandTotal += sub;
                hasPrice = true;
                sb.append("  =  ").append(formatRupiah(sub));
            }
            sb.append("\n\n");
        }

        double finalTotal = grandTotal;

        sb.append("--------------------------\n");
        if (hasPrice) {
            if (appliedVoucher != null) {
                double disc = appliedVoucher.calculateDiscount(grandTotal);
                finalTotal  = grandTotal - disc;
                String discLabel = appliedVoucher.getType() == model.VoucherCode.Type.PERCENT
                    ? "Diskon " + (int)appliedVoucher.getValue() + "%"
                    : "Voucher " + appliedVoucher.getCode();
                sb.append("Subtotal: ").append(formatRupiah(grandTotal)).append("\n");
                sb.append(discLabel).append(": -").append(formatRupiah(disc)).append("\n");
                sb.append("Grand Total: ").append(formatRupiah(finalTotal)).append("\n\n");
            } else {
                sb.append("Grand Total: ").append(formatRupiah(grandTotal)).append("\n\n");
            }
        }
        sb.append("Please confirm your order \uD83D\uDE0A");

        buyerPanel.displayBuyerSummary(sb.toString(), finalTotal, buildChosenSellersInfo());
    }

    public List<String[]> buildChosenSellersInfo() {
        List<String[]> result = new ArrayList<>();

        for (int sellerIdx : chosenSellerIndices) {
            String sellerName = getSellerName(sellerIdx);
            String phone      = getSellerPhone(sellerIdx);

            StringBuilder items = new StringBuilder();
            double sellerTotal  = 0;
            for (CartItem item : cart.values()) {
                if (item.sellerIndex == sellerIdx) {
                    String title = item.message.split("\n")[0].trim();
                    if (title.startsWith("[") && title.contains("]")) {
                        title = title.substring(title.indexOf("]") + 1).trim();
                    }
                    items.append("• ").append(title)
                         .append(" x").append(item.quantity);
                    if (item.unitPrice > 0) {
                        double sub = item.unitPrice * item.quantity;
                        sellerTotal += sub;
                        items.append(" = ").append(formatRupiah(sub));
                    }
                    items.append("\n");
                }
            }

            String totalStr = sellerTotal > 0 ? formatRupiah(sellerTotal) : "-";
            result.add(new String[]{ sellerName, phone, items.toString(), totalStr });
        }

        return result;
    }

    private String getSellerPhone(int sellerIndex) {
        String[] phones = {
            "6285708223820",
            "6289514366861",
            "6285894121730",
        };
        if (sellerIndex >= 0 && sellerIndex < phones.length) return phones[sellerIndex];
        return "";
    }

    public void clearCart() {
        cart.clear();
        chosenSellerIndices.clear();
        appliedVoucher = null;
        if (buyerPanel != null) buyerPanel.clearBuyerSummary();
        if (recommendationPanel != null) recommendationPanel.markCurrentOrderDone();
    }

    private void registerDefaultVouchers() {
        availableVouchers.put("HEMAT10",
            new model.VoucherCode("HEMAT10", model.VoucherCode.Type.PERCENT, 10, 0));
        availableVouchers.put("DISKON20K",
            new model.VoucherCode("DISKON20K", model.VoucherCode.Type.FIXED, 20000, 50000));
    }

    public String applyVoucher(String code) {
        if (code == null || code.trim().isEmpty()) return "Voucher code is empty.";
        model.VoucherCode voucher = availableVouchers.get(code.trim().toUpperCase().replaceAll("\\s+", ""));
        if (voucher == null) return "The voucher code is invalid.";
        double subtotal = calculateSubtotal();
        if (subtotal < voucher.getMinOrder())
            return "Minimum purchase " + formatRupiah(voucher.getMinOrder()) + " for this voucher.";
        appliedVoucher = voucher;
        refreshSummary();
        return "The voucher has been successfully applied!";
    }

    public void removeVoucher() {
        appliedVoucher = null;
        refreshSummary();
    }

    private double calculateSubtotal() {
        double total = 0;
        for (CartItem item : cart.values()) total += item.unitPrice * item.quantity;
        return total;
    }

    public void clearAllChats() {
        activeRequests.clear();
        cart.clear();
        chosenSellerIndices.clear();
        requestIdCounter = 1;
        if (buyerPanel != null) buyerPanel.clearChat();
        for (SellerPanel sp : sellerPanels) sp.clearAllRequests();
        if (recommendationPanel != null) recommendationPanel.clearAll();
        System.out.println("[ChatController] All chats cleared");
    }

    private String getSellerName(int idx) {
        if (idx >= 0 && idx < sellerPanels.size()) return sellerPanels.get(idx).getSellerName();
        return "Seller " + (idx + 1);
    }

    private ChatRequest findRequestById(int id) {
        for (ChatRequest r : activeRequests) {
            if (r.getRequestId() == id) return r;
        }
        return null;
    }

    public List<ChatRequest> getActiveRequests() { return new ArrayList<>(activeRequests); }

    private double parsePrice(String msg) {
        if (msg == null) return 0;
        java.util.regex.Matcher rp = java.util.regex.Pattern
                .compile("(?i)rp\\.?\\s*([\\d.,]+)").matcher(msg);
        if (rp.find()) {
            try { return Double.parseDouble(rp.group(1).replace(".", "").replace(",", "")); }
            catch (NumberFormatException ignored) {}
        }
        java.util.regex.Matcher k = java.util.regex.Pattern
                .compile("(\\d+(?:[.,]\\d+)?)\\s*(?:k|rb|ribu)").matcher(msg.toLowerCase());
        if (k.find()) {
            try { return Double.parseDouble(k.group(1).replace(",", ".")) * 1000; }
            catch (NumberFormatException ignored) {}
        }
        return 0;
    }

    private int parseQuantity(String msg) {
        if (msg == null) return 1;
        java.util.regex.Matcher mx = java.util.regex.Pattern.compile("(\\d+)\\s*[xX×]").matcher(msg);
        if (mx.find()) {
            try { return Integer.parseInt(mx.group(1)); } catch (NumberFormatException ignored) {}
        }
        return 1;
    }

    public java.util.Map<String, Integer> parseBuyerQtyMap(String message) {
        java.util.Map<String, Integer> result = new java.util.LinkedHashMap<>();
        if (message == null || message.isEmpty()) return result;
        java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("([a-zA-Z][a-zA-Z ]{0,30}?)\\s+(\\d+)(?=\\s*[,;]|\\s*$)")
                .matcher(message.trim());
        while (m.find()) {
            String keyword = m.group(1).trim().toLowerCase();
            int qty = Integer.parseInt(m.group(2));
            if (!keyword.isEmpty()) result.put(keyword, qty);
        }
        if (result.isEmpty()) {
            java.util.regex.Matcher m2 = java.util.regex.Pattern
                    .compile("(\\d+)\\s+([a-zA-Z][a-zA-Z ]{0,30}?)(?=\\s*[,;]|\\s*$)")
                    .matcher(message.trim());
            while (m2.find()) {
                int qty = Integer.parseInt(m2.group(1));
                String keyword = m2.group(2).trim().toLowerCase();
                if (!keyword.isEmpty()) result.put(keyword, qty);
            }
        }
        return result;
    }

    private String formatRupiah(double amount) {
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
}
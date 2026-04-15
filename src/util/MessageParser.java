package util;

import java.util.*;
import java.util.regex.*;

public class MessageParser {

    // ── Buyer Intent ───────────────────────────────────────────
    public static class BuyerIntent {
        public String description;
        public int    quantity  = 1;
        public double maxBudget = 0;
    }

    private static final Pattern BUYER_PRICE_QTY = Pattern.compile(
        "(?:(\\d+)[xX×])?(\\d+(?:[.,]\\d+)?)\\s*[kK](?:\\b|$)"
    );

    public static BuyerIntent parseBuyer(String msg) {
        BuyerIntent intent = new BuyerIntent();
        Matcher m = BUYER_PRICE_QTY.matcher(msg);
        if (m.find()) {
            if (m.group(1) != null)
                intent.quantity  = Integer.parseInt(m.group(1));
            intent.maxBudget = Double.parseDouble(
                m.group(2).replace(",", ".")) * 1000;
            intent.description = msg.substring(0, m.start()).trim()
                .replaceAll("(?i)^b:\\s*", "");
        } else {
            intent.description = msg.replaceAll("(?i)^b:\\s*", "").trim();
        }
        return intent;
    }

    // ── Seller Item ────────────────────────────────────────────
    public static class SellerItem {
        public String       name;
        public int          quantity    = 1;
        public double       unitPrice   = 0;
        public int          volume      = 0;
        public String       volumeUnit  = "";
        public List<String> readyTimes  = new ArrayList<>();
        public String       sizeLabel   = "";
        public SellerItem   alternative = null;
    }

    // ── FIXED: volume dan size dipisah groupnya, $ dihapus ────
    private static final Pattern SELLER_ITEM = Pattern.compile(
        "(?i)^(?:S\\d*:\\s*)?" +
        "([a-zA-Z][a-zA-Z0-9 ,&'/\\-]{1,50}?)" +  // [1] nama produk
        "\\s+(?:(\\d+)[xX×])?" +                   // [2] qty (opsional)
        "(\\d+(?:[.,]\\d+)?)[kK]" +                // [3] harga
        "(?:\\s+(\\d+)\\s*(cc|ml|gr|g))?" +        // [4][5] volume (opsional)
        "(?:\\s+([SMLX]+))?" +                     // [6] size (opsional)
        "((?:\\s+\\d+[mh])*)" +                    // [7] waktu (opsional)
        "\\s*",
        Pattern.CASE_INSENSITIVE
    );

    private static final Pattern TIME = Pattern.compile(
        "(\\d+[mh])", Pattern.CASE_INSENSITIVE);

    public static SellerItem parseSellerItem(String raw) {
        if (raw == null) return null;
        raw = raw.trim();

        // Cek alternatif dalam kurung: "A (B)"
        SellerItem alternative = null;
        Matcher alt = Pattern.compile("\\(([^)]+)\\)").matcher(raw);
        if (alt.find()) {
            alternative = parseSellerItem(alt.group(1));
            raw = raw.substring(0, alt.start()).trim();
        }

        Matcher m = SELLER_ITEM.matcher(raw);
        if (!m.find()) return null;

        SellerItem item = new SellerItem();
        item.name       = m.group(1).trim();
        item.quantity   = m.group(2) != null ? Integer.parseInt(m.group(2)) : 1;
        item.unitPrice  = Double.parseDouble(
                m.group(3).replace(",", ".")) * 1000;

        // Volume — group 4 = angka, group 5 = satuan
        if (m.group(4) != null) {
            item.volume     = Integer.parseInt(m.group(4));
            item.volumeUnit = m.group(5).toLowerCase();
        }

        // Size — group 6
        if (m.group(6) != null) item.sizeLabel = m.group(6).toUpperCase();

        // Ready time — group 7
        if (m.group(7) != null) {
            Matcher tm = TIME.matcher(m.group(7));
            while (tm.find()) item.readyTimes.add(tm.group(1).toLowerCase());
        }

        item.alternative = alternative;
        return item;
    }

    // ── Seller Offer (size variants via pipe separator) ────────
    public static class SellerOffer {
        public String           baseName;
        public List<SellerItem> variants = new ArrayList<>();
    }

    public static SellerOffer parseSellerOffer(String line) {
        line = line.replaceAll("(?i)^S\\d*:\\s*", "").trim();
        String[] parts = line.split("\\|");

        SellerOffer offer    = new SellerOffer();
        String      baseName = null;

        for (String part : parts) {
            part = part.trim();
            if (baseName != null && !part.matches("(?i)^[a-zA-Z]+.*")) {
                part = baseName + " " + part;
            }
            SellerItem item = parseSellerItem(part);
            if (item != null) {
                if (baseName == null) baseName = item.name;
                offer.variants.add(item);
            }
        }
        offer.baseName = baseName;
        return offer;
    }

    // ── Helper: check if seller item is within buyer's budget ──
    public static boolean withinBudget(SellerItem item, BuyerIntent buyer) {
        if (buyer.maxBudget <= 0) return true;
        return item.unitPrice <= buyer.maxBudget;
    }
}
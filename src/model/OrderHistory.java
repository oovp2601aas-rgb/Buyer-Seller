package model;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * OrderHistory - Singleton model to store transaction history for this session.
 * Data is lost when the app is closed (in-memory only).
 */
public class OrderHistory {

    // ── Singleton ─────────────────────────────────────────────
    private static OrderHistory instance;
    public static OrderHistory getInstance() {
        if (instance == null) instance = new OrderHistory();
        return instance;
    }
    private OrderHistory() {}

    // ── Inner class: a single transaction record ──────────────
    public static class Transaction {
        private static int counter = 1;

        public final String txId;
        public final String date;
        public final String paymentMethod;
        public final String orderSummary;
        public final double grandTotal;
        public final String address;

        // Rating & review — can be filled in after the transaction
        public int    rating  = 0;    // 0 = not yet rated, 1-5 = already rated
        public String review  = "";   // text comment

        public Transaction(String paymentMethod, String orderSummary,
                           double grandTotal, String address) {
            this.txId          = "TXN-" + String.format("%04d", counter++);
            this.date          = new SimpleDateFormat("dd MMM yyyy, HH:mm").format(new Date());
            this.paymentMethod = paymentMethod;
            this.orderSummary  = orderSummary;
            this.grandTotal    = grandTotal;
            this.address       = address;
        }

        public boolean isRated() { return rating > 0; }

        /** Render stars as a string: ★★★☆☆ */
        public String getStarString() {
            StringBuilder sb = new StringBuilder();
            for (int i = 1; i <= 5; i++) {
                sb.append(i <= rating ? "\u2605" : "\u2606");
            }
            return sb.toString();
        }
    }

    // ── Storage ───────────────────────────────────────────────
    private final List<Transaction> transactions = new ArrayList<>();

    public void add(Transaction t) {
        transactions.add(0, t); // newest on top
    }

    public List<Transaction> getAll() {
        return new ArrayList<>(transactions);
    }

    public boolean isEmpty() { return transactions.isEmpty(); }

    public void clear() { transactions.clear(); }
}
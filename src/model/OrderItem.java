package model;

/**
 * OrderItem - Represents a single line item inside an Order
 * Part of OOP Object hierarchy
 */
public class OrderItem {

    private int    itemId;
    private int    orderId;
    private int    sellerId;
    private String productName;   // snapshot of name at time of order
    private String variantLabel;  // e.g. "S", "M", "L", "300gr"
    private int    quantity;
    private double unitPrice;     // price per unit at time of order

    public OrderItem(int sellerId, String productName,
                     String variantLabel, int quantity, double unitPrice) {
        this.sellerId     = sellerId;
        this.productName  = productName;
        this.variantLabel = variantLabel;
        this.quantity     = quantity;
        this.unitPrice    = unitPrice;
    }

    // Simplified constructor (no variant)
    public OrderItem(int sellerId, String productName,
                     int quantity, double unitPrice) {
        this(sellerId, productName, "", quantity, unitPrice);
    }

    // ── Methods ───────────────────────────────────────────────

    public double getSubtotal() {
        return unitPrice * quantity;
    }

    public String getDisplayName() {
        if (variantLabel != null && !variantLabel.isEmpty()) {
            return productName + " (" + variantLabel + ")";
        }
        return productName;
    }

    public String getFormattedSubtotal() {
        return "Rp " + String.format("%,.0f", getSubtotal());
    }

    // ── Getters & Setters ─────────────────────────────────────
    public int    getItemId()      { return itemId; }
    public int    getOrderId()     { return orderId; }
    public int    getSellerId()    { return sellerId; }
    public String getProductName() { return productName; }
    public String getVariantLabel(){ return variantLabel; }
    public int    getQuantity()    { return quantity; }
    public double getUnitPrice()   { return unitPrice; }

    public void setItemId(int itemId)              { this.itemId      = itemId; }
    public void setOrderId(int orderId)            { this.orderId     = orderId; }
    public void setSellerId(int sellerId)          { this.sellerId    = sellerId; }
    public void setProductName(String name)        { this.productName = name; }
    public void setVariantLabel(String label)      { this.variantLabel= label; }
    public void setQuantity(int quantity)          { this.quantity    = quantity; }
    public void setUnitPrice(double unitPrice)     { this.unitPrice   = unitPrice; }

    @Override
    public String toString() {
        return quantity + "x " + getDisplayName() +
               " @ Rp " + String.format("%,.0f", unitPrice) +
               " = " + getFormattedSubtotal();
    }
}
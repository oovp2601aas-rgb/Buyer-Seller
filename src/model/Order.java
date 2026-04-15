package model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Order - Represents a completed purchase order
 * Part of OOP Object hierarchy
 */
public class Order {

    public enum Status {
        PENDING,    // Order created, not yet paid
        PAID,       // Payment confirmed
        PROCESSING, // Seller is preparing
        DELIVERED,  // Order delivered to buyer
        CANCELLED   // Order cancelled
    }

    private int          orderId;
    private int          buyerId;
    private String       paymentMethod;
    private double       grandTotal;
    private String       deliveryAddress;
    private Status       status;
    private Date         createdAt;
    private List<OrderItem> items;

    public Order(int buyerId, String paymentMethod,
                 String deliveryAddress) {
        this.buyerId         = buyerId;
        this.paymentMethod   = paymentMethod;
        this.deliveryAddress = deliveryAddress;
        this.status          = Status.PENDING;
        this.createdAt       = new Date();
        this.items           = new ArrayList<>();
        this.grandTotal      = 0.0;
    }

    // ── Methods ───────────────────────────────────────────────

    public void addItem(OrderItem item) {
        items.add(item);
        recalculateTotal();
    }

    public void removeItem(OrderItem item) {
        items.remove(item);
        recalculateTotal();
    }

    private void recalculateTotal() {
        grandTotal = 0.0;
        for (OrderItem item : items) {
            grandTotal += item.getSubtotal();
        }
    }

    public void confirmPayment() {
        this.status = Status.PAID;
    }

    public void cancel() {
        this.status = Status.CANCELLED;
    }

    public boolean isPaid() {
        return status == Status.PAID;
    }

    public int getItemCount() {
        return items.size();
    }

    public String getSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("Order #").append(orderId).append("\n");
        sb.append("─────────────────────\n");
        for (OrderItem item : items) {
            sb.append(item.toString()).append("\n");
        }
        sb.append("─────────────────────\n");
        sb.append("Total: Rp ").append(String.format("%,.0f", grandTotal));
        return sb.toString();
    }

    // ── Getters & Setters ─────────────────────────────────────
    public int              getOrderId()         { return orderId; }
    public int              getBuyerId()          { return buyerId; }
    public String           getPaymentMethod()    { return paymentMethod; }
    public double           getGrandTotal()       { return grandTotal; }
    public String           getDeliveryAddress()  { return deliveryAddress; }
    public Status           getStatus()           { return status; }
    public Date             getCreatedAt()        { return createdAt; }
    public List<OrderItem>  getItems()            { return new ArrayList<>(items); }

    public void setOrderId(int orderId)                   { this.orderId         = orderId; }
    public void setBuyerId(int buyerId)                   { this.buyerId         = buyerId; }
    public void setPaymentMethod(String paymentMethod)    { this.paymentMethod   = paymentMethod; }
    public void setGrandTotal(double grandTotal)          { this.grandTotal      = grandTotal; }
    public void setDeliveryAddress(String address)        { this.deliveryAddress = address; }
    public void setStatus(Status status)                  { this.status          = status; }
    public void setCreatedAt(Date createdAt)              { this.createdAt       = createdAt; }

    @Override
    public String toString() {
        return "Order{id=" + orderId + ", buyerId=" + buyerId +
               ", total=" + grandTotal + ", status=" + status + "}";
    }
}
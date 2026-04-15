package model;

/**
 * RecommendationItem - Represents a single menu item sent by the seller
 * to the Recommendations for Buyers panel.
 */
public class RecommendationItem {

    private int    requestId;
    private int    sellerIndex;
    private String sellerName;
    private String menuName;       // menu name / short description
    private double unitPrice;
    private int    quantity;       // quantity suggested by the seller
    private String rawMessage;     // original message from the seller (form submit)

    public RecommendationItem(int requestId, int sellerIndex, String sellerName,
                               String menuName, double unitPrice,
                               int quantity, String rawMessage) {
        this.requestId   = requestId;
        this.sellerIndex = sellerIndex;
        this.sellerName  = sellerName;
        this.menuName    = menuName;
        this.unitPrice   = unitPrice;
        this.quantity    = quantity;
        this.rawMessage  = rawMessage;
    }

    public int    getRequestId()   { return requestId; }
    public int    getSellerIndex() { return sellerIndex; }
    public String getSellerName()  { return sellerName; }
    public String getMenuName()    { return menuName; }
    public double getUnitPrice()   { return unitPrice; }
    public int    getQuantity()    { return quantity; }
    public String getRawMessage()  { return rawMessage; }

    public void setQuantity(int q) { this.quantity = q; }
}
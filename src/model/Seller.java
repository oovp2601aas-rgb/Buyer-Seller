package model;

/**
 * Seller - Extends Entity, represents a seller/merchant
 * OOP: Inheritance + Overriding
 */
public class Seller extends Entity {

    private String storeName;
    private String email;
    private String address;
    private double rating;
    private int    totalRatings;
    private String videoUrl;

    public Seller(int id, String name, String storeName,
                  String phone, String email, String address) {
        super(id, name, phone); // panggil constructor Entity
        this.storeName    = storeName;
        this.email        = email;
        this.address      = address;
        this.rating       = 0.0;
        this.totalRatings = 0;
        this.videoUrl     = "";
    }

    // ── Override abstract methods dari Entity ─────────────────
    @Override
    public String getRole() {
        return "SELLER";
    }

    @Override
    public String getContactInfo() {
        return "Phone: " + getPhone() + " | Email: " + email +
               " | Store: " + storeName;
    }

    // ── Override getSummary dari Entity ───────────────────────
    @Override
    public String getSummary() {
        return "[SELLER] " + getName() + " | " + storeName +
               " | " + getStarString();
    }

    // ── Seller-specific methods ───────────────────────────────
    public void addRating(int stars) {
        if (stars < 1 || stars > 5) return;
        double totalScore = this.rating * this.totalRatings;
        this.totalRatings++;
        this.rating = (totalScore + stars) / this.totalRatings;
    }

    public boolean hasVideo() {
        return videoUrl != null && !videoUrl.isEmpty();
    }

    public String getStarString() {
        int rounded = (int) Math.round(rating);
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= 5; i++) {
            sb.append(i <= rounded ? "\u2605" : "\u2606");
        }
        return sb.toString() + String.format(" (%.1f)", rating);
    }

    public String getWhatsAppUrl(String message) {
        try {
            String encoded = java.net.URLEncoder.encode(message, "UTF-8");
            return "https://wa.me/" + getPhone().replaceAll("[^0-9]", "")
                   + "?text=" + encoded;
        } catch (Exception e) {
            return "https://wa.me/" + getPhone().replaceAll("[^0-9]", "");
        }
    }

    // ── Getters & Setters ─────────────────────────────────────
    public String getStoreName()    { return storeName; }
    public String getEmail()        { return email; }
    public String getAddress()      { return address; }
    public double getRating()       { return rating; }
    public int    getTotalRatings() { return totalRatings; }
    public String getVideoUrl()     { return videoUrl; }

    public void setStoreName(String storeName)  { this.storeName    = storeName; }
    public void setEmail(String email)          { this.email        = email; }
    public void setAddress(String address)      { this.address      = address; }
    public void setRating(double rating)        { this.rating       = rating; }
    public void setTotalRatings(int total)      { this.totalRatings = total; }
    public void setVideoUrl(String videoUrl)    { this.videoUrl     = videoUrl; }

    @Override
    public String toString() {
        return "Seller{id=" + getId() + ", name='" + getName() +
               "', store='" + storeName + "', rating=" + getStarString() + "}";
    }
}

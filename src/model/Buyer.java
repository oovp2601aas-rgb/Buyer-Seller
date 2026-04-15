package model;

/**
 * Buyer - Extends Entity, represents a customer
 * OOP: Inheritance + Overriding
 */
public class Buyer extends Entity {

    private String address;
    private double latitude;
    private double longitude;

    public Buyer(int id, String name, String phone, String address) {
        super(id, name, phone); // panggil constructor Entity
        this.address   = address;
        this.latitude  = 0.0;
        this.longitude = 0.0;
    }

    // ── Override abstract methods dari Entity ─────────────────
    @Override
    public String getRole() {
        return "BUYER";
    }

    @Override
    public String getContactInfo() {
        return "Phone: " + getPhone() + " | Address: " + address;
    }

    // ── Override getSummary dari Entity ───────────────────────
    @Override
    public String getSummary() {
        return "[BUYER] " + getName() + " | " + address;
    }

    // ── Buyer-specific methods ────────────────────────────────
    public void setCoordinates(double latitude, double longitude) {
        this.latitude  = latitude;
        this.longitude = longitude;
    }

    public boolean hasCoordinates() {
        return latitude != 0.0 && longitude != 0.0;
    }

    public String getGoogleMapsUrl() {
        if (hasCoordinates()) {
            return "https://www.google.com/maps?q=" + latitude + "," + longitude;
        }
        return "https://www.google.com/maps/search/?api=1&query="
                + address.replace(" ", "+");
    }

    // ── Getters & Setters ─────────────────────────────────────
    public String getAddress()   { return address; }
    public double getLatitude()  { return latitude; }
    public double getLongitude() { return longitude; }

    public void setAddress(String address)   { this.address   = address; }
    public void setLatitude(double lat)      { this.latitude  = lat; }
    public void setLongitude(double lng)     { this.longitude = lng; }

    @Override
    public String toString() {
        return "Buyer{id=" + getId() + ", name='" + getName() +
               "', address='" + address + "'}";
    }
}
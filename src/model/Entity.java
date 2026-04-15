package model;

import java.util.Date;

/**
 * Entity - Abstract parent class for all people/actors in the system
 * OOP: Abstraction + Inheritance base
 */
public abstract class Entity {

    private int    id;
    private String name;
    private String phone;
    private Date   createdAt;

    public Entity(int id, String name, String phone) {
        this.id        = id;
        this.name      = name;
        this.phone     = phone;
        this.createdAt = new Date();
    }

    // ── Abstract methods — wajib di-override child class ─────
    public abstract String getRole();        // "BUYER" or "SELLER"
    public abstract String getContactInfo(); // different per role

    // ── Common method — bisa di-override ─────────────────────
    public String getSummary() {
        return "[" + getRole() + "] " + name + " | " + phone;
    }

    // ── Getters & Setters ─────────────────────────────────────
    public int    getId()        { return id; }
    public String getName()      { return name; }
    public String getPhone()     { return phone; }
    public Date   getCreatedAt() { return createdAt; }

    public void setId(int id)           { this.id    = id; }
    public void setName(String name)    { this.name  = name; }
    public void setPhone(String phone)  { this.phone = phone; }

    @Override
    public String toString() {
        return getSummary();
    }
}
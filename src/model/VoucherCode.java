// model/VoucherCode.java
package model;

public class VoucherCode {
    public enum Type { PERCENT, FIXED }

    private String code;
    private Type   type;
    private double value;   // 10 = 10% atau Rp10.000
    private double minOrder; // minimum belanja, 0 = tidak ada syarat

    public VoucherCode(String code, Type type, double value, double minOrder) {
        this.code     = code;
        this.type     = type;
        this.value    = value;
        this.minOrder = minOrder;
    }

    public String getCode()     { return code; }
    public Type   getType()     { return type; }
    public double getValue()    { return value; }
    public double getMinOrder() { return minOrder; }

    /** Hitung potongan dari subtotal yang diberikan */
    public double calculateDiscount(double subtotal) {
        if (subtotal < minOrder) return 0;
        if (type == Type.PERCENT) return subtotal * (value / 100.0);
        return Math.min(value, subtotal); // fixed tidak boleh melebihi subtotal
    }
}
package jsoc.model.enums;

public enum Severity {

    LOW(72),
    MEDIUM(24),
    HIGH(4),
    CRITICAL(1);

    private final int slaHours;

    Severity(int slaHours) {
        this.slaHours = slaHours;
    }

    public int getSlaHours() {
        return slaHours;
    }
}

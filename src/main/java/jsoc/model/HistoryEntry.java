package jsoc.model;

import java.time.LocalDateTime;

public final class HistoryEntry {

    private static long nextId = 1;

    private final long id;
    private final String action;
    private final String performedBy;
    private final LocalDateTime timestamp;
    private final String oldValue;
    private final String newValue;

    public HistoryEntry(String action, String performedBy, String oldValue, String newValue) {
        this(generateId(), action, performedBy, LocalDateTime.now(), oldValue, newValue);
    }

    public HistoryEntry(long id, String action, String performedBy,
                        LocalDateTime timestamp, String oldValue, String newValue) {
        if (action == null || action.isBlank()) {
            throw new IllegalArgumentException("L'action est obligatoire.");
        }
        if (performedBy == null || performedBy.isBlank()) {
            throw new IllegalArgumentException("L'auteur de l'action est obligatoire.");
        }
        if (timestamp == null) {
            throw new IllegalArgumentException("L'horodatage est obligatoire.");
        }
        this.id = id;
        this.action = action;
        this.performedBy = performedBy;
        this.timestamp = timestamp;
        this.oldValue = oldValue;
        this.newValue = newValue;
        if (id >= nextId) {
            nextId = id + 1;
        }
    }

    private static synchronized long generateId() {
        return nextId++;
    }

    public long getId() {
        return id;
    }

    public String getAction() {
        return action;
    }

    public String getPerformedBy() {
        return performedBy;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getOldValue() {
        return oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HistoryEntry other)) return false;
        return this.id == other.id;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }

    @Override
    public String toString() {
        return "HistoryEntry{id=" + id
                + ", action='" + action + '\''
                + ", performedBy='" + performedBy + '\''
                + ", timestamp=" + timestamp
                + ", oldValue='" + oldValue + '\''
                + ", newValue='" + newValue + '\''
                + '}';
    }
}

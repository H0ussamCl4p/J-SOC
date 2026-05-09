package jsoc.model;

public final class IOC {

    public enum IOCType {
        IP,
        HASH,
        URL,
        DOMAIN,
        EMAIL
    }

    private static long nextId = 1;

    private final long id;
    private final IOCType type;
    private final String value;
    private final String description;

    public IOC(IOCType type, String value, String description) {
        this(generateId(), type, value, description);
    }

    public IOC(long id, IOCType type, String value, String description) {
        if (type == null) {
            throw new IllegalArgumentException("Le type d'IOC est obligatoire.");
        }
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("La valeur de l'IOC est obligatoire.");
        }
        this.id = id;
        this.type = type;
        this.value = value;
        this.description = description;
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

    public IOCType getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IOC other)) return false;
        return this.id == other.id;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }

    @Override
    public String toString() {
        return "IOC{id=" + id
                + ", type=" + type
                + ", value='" + value + '\''
                + ", description='" + description + '\''
                + '}';
    }
}

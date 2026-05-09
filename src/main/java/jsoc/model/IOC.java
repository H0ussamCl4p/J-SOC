package jsoc.model;

/**
 * IOC (Indicator Of Compromise) — élément technique observé lors d'un incident
 * permettant d'identifier ou de chasser une menace (adresse IP malveillante,
 * hash de fichier suspect, URL de phishing, etc.).
 *
 * <p>Dans un vrai SOC, les IOC sont la matière première du <i>threat hunting</i>
 * et sont souvent partagés entre équipes via des plateformes comme MISP.
 * Ici on en garde une représentation simplifiée.</p>
 *
 * <p>Cette classe contient un <b>enum imbriqué</b> {@link IOCType} pour le type
 * de l'indicateur. C'est une bonne pratique POO : l'enum est si étroitement lié
 * à la classe {@code IOC} qu'on l'imbrique pour éviter de polluer le package.</p>
 */
public final class IOC {

    /**
     * Type d'IOC selon les conventions standard (cf. STIX, MISP).
     */
    public enum IOCType {
        /** Adresse IP (v4 ou v6). */
        IP,
        /** Empreinte cryptographique d'un fichier (MD5, SHA-1, SHA-256). */
        HASH,
        /** URL complète. */
        URL,
        /** Nom de domaine. */
        DOMAIN,
        /** Adresse e-mail (souvent l'expéditeur d'un phishing). */
        EMAIL
    }

    private static long nextId = 1;

    private final long id;
    private final IOCType type;
    private final String value;
    private final String description;

    /**
     * Crée un IOC avec id auto-généré.
     *
     * @param type        catégorie de l'indicateur (non null)
     * @param value       valeur observée (non null, non vide)
     * @param description contexte / commentaire (peut être {@code null})
     */
    public IOC(IOCType type, String value, String description) {
        this(generateId(), type, value, description);
    }

    /**
     * Constructeur complet — utilisé pour la restauration depuis le CSV (Membre 2).
     *
     * @param id          identifiant
     * @param type        type (non null)
     * @param value       valeur (non null, non vide)
     * @param description description (peut être {@code null})
     */
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

    /** @return la description, ou {@code null} si aucune n'a été fournie. */
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

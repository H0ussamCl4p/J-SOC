package jsoc.model;

import java.time.LocalDateTime;

/**
 * Entrée d'historique d'un incident — trace une action effectuée sur l'incident.
 *
 * <p>Chaque action significative (changement de statut, assignation, ajout d'un IOC,
 * escalade, etc.) génère une entrée d'historique. Cela permet ensuite de
 * reconstituer la timeline de l'incident dans la CLI et de générer des rapports
 * d'audit.</p>
 *
 * <p>Les champs {@code oldValue} et {@code newValue} sont <b>optionnels</b> :
 * ils peuvent être {@code null} pour des actions qui ne modifient pas de valeur
 * (par exemple "incident créé" ou "commentaire ajouté").</p>
 *
 * <p>Comme {@link Comment}, cette classe est <b>immutable</b> : un événement
 * historique ne doit jamais être modifié après sa création.</p>
 */
public final class HistoryEntry {

    /** Compteur statique pour générer des identifiants uniques. */
    private static long nextId = 1;

    private final long id;
    private final String action;
    private final String performedBy;
    private final LocalDateTime timestamp;
    private final String oldValue;
    private final String newValue;

    /**
     * Crée une nouvelle entrée d'historique avec id auto-généré et timestamp courant.
     *
     * @param action       libellé de l'action (ex : "STATUS_CHANGE", "ASSIGNED", "IOC_ADDED")
     * @param performedBy  username de l'utilisateur qui a effectué l'action
     * @param oldValue     valeur précédente (peut être {@code null})
     * @param newValue     nouvelle valeur (peut être {@code null})
     */
    public HistoryEntry(String action, String performedBy, String oldValue, String newValue) {
        this(generateId(), action, performedBy, LocalDateTime.now(), oldValue, newValue);
    }

    /**
     * Constructeur complet — utilisé pour la restauration depuis le CSV (Membre 2).
     *
     * @param id           identifiant
     * @param action       libellé de l'action (non null, non vide)
     * @param performedBy  utilisateur (non null, non vide)
     * @param timestamp    horodatage (non null)
     * @param oldValue     valeur précédente (peut être {@code null})
     * @param newValue     nouvelle valeur (peut être {@code null})
     */
    public HistoryEntry(long id, String action, String performedBy,
                        LocalDateTime timestamp, String oldValue, String newValue) {
        if (action == null || action.isBlank()) {
            throw new IllegalArgumentException("L'action est obligatoire dans une entrée d'historique.");
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

    /** @return la valeur avant l'action, ou {@code null} si non applicable. */
    public String getOldValue() {
        return oldValue;
    }

    /** @return la valeur après l'action, ou {@code null} si non applicable. */
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

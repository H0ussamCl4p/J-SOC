package jsoc.model.incident;

import jsoc.interfaces.Assignable;
import jsoc.model.Comment;
import jsoc.model.HistoryEntry;
import jsoc.model.IOC;
import jsoc.model.enums.IncidentStatus;
import jsoc.model.enums.Severity;
import jsoc.model.user.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Incident de sécurité — classe <b>abstraite</b>, pivot du modèle métier J-SOC.
 *
 * <p>Tout incident a un identifiant {@code INC-XXX}, un titre, une description,
 * une sévérité, un statut, une date de création, éventuellement un porteur,
 * une liste de commentaires, un historique d'actions et un ensemble d'IOCs.</p>
 *
 * <p>Les comportements <b>spécifiques au type</b> d'incident sont définis comme
 * méthodes abstraites :</p>
 * <ul>
 *   <li>{@link #getType()} — libellé du type</li>
 *   <li>{@link #getResponseProcedure()} — procédure de réponse à appliquer</li>
 *   <li>{@link #computeDefaultSLAHours()} — SLA par défaut pour ce type</li>
 *   <li>{@link #getCWE()} — code CWE associé (Common Weakness Enumeration)</li>
 * </ul>
 *
 * <p><b>Concepts POO clés :</b></p>
 * <ul>
 *   <li><b>Classe abstraite</b> — on ne peut pas créer un "Incident" générique,
 *       il faut choisir un type concret.</li>
 *   <li><b>Polymorphisme</b> — un service peut traiter une {@code List<Incident>}
 *       sans connaître le type concret, et appeler {@code getResponseProcedure()}
 *       qui sera dispatchée au runtime vers la bonne sous-classe.</li>
 *   <li><b>Implémentation d'interface générique</b> — {@code Assignable<User>}
 *       fixe le paramètre de type à {@code User}.</li>
 *   <li><b>Encapsulation forte</b> — les collections internes sont mutables
 *       mais exposées en lecture seule via {@code Collections.unmodifiable*}.</li>
 * </ul>
 */
public abstract class Incident implements Assignable<User> {

    /** Préfixe utilisé pour générer les identifiants ({@code INC-001}, {@code INC-002}, ...). */
    private static final String ID_PREFIX = "INC-";

    /** Compteur statique pour la génération d'identifiants. */
    private static long nextIdCounter = 1;

    private final String id;
    private String title;
    private String description;
    private Severity severity;
    private IncidentStatus status;
    private final LocalDateTime createdAt;
    private User assignedTo;
    private final List<Comment> comments;
    private final List<HistoryEntry> history;
    private final Set<IOC> iocs;

    /**
     * Constructeur "création neuve" — id auto-généré, statut {@code NEW},
     * créé "maintenant", non assigné.
     *
     * @param title       titre de l'incident (non null, non vide)
     * @param description description (non null, non vide)
     * @param severity    sévérité (non null)
     */
    protected Incident(String title, String description, Severity severity) {
        this(generateId(), title, description, severity,
             IncidentStatus.NEW, LocalDateTime.now(), null);
    }

    /**
     * Constructeur complet — utilisé pour la restauration depuis CSV (M2).
     *
     * @param id          identifiant ({@code INC-XXX})
     * @param title       titre
     * @param description description
     * @param severity    sévérité
     * @param status      statut
     * @param createdAt   date de création
     * @param assignedTo  porteur (peut être {@code null})
     */
    protected Incident(String id, String title, String description,
                       Severity severity, IncidentStatus status,
                       LocalDateTime createdAt, User assignedTo) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("L'id de l'incident est obligatoire.");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Le titre de l'incident est obligatoire.");
        }
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("La description de l'incident est obligatoire.");
        }
        if (severity == null) {
            throw new IllegalArgumentException("La sévérité est obligatoire.");
        }
        if (status == null) {
            throw new IllegalArgumentException("Le statut est obligatoire.");
        }
        if (createdAt == null) {
            throw new IllegalArgumentException("La date de création est obligatoire.");
        }
        this.id = id;
        this.title = title;
        this.description = description;
        this.severity = severity;
        this.status = status;
        this.createdAt = createdAt;
        this.assignedTo = assignedTo;
        this.comments = new ArrayList<>();
        this.history = new ArrayList<>();
        this.iocs = new HashSet<>();
        syncIdCounter(id);
    }

    /** Génère un nouvel id au format {@code INC-XXX} (3 chiffres mini, zéro-paddés). */
    private static synchronized String generateId() {
        return String.format("%s%03d", ID_PREFIX, nextIdCounter++);
    }

    /**
     * Synchronise le compteur d'id après une restauration CSV pour éviter
     * que la prochaine génération ne réutilise un id existant.
     */
    private static synchronized void syncIdCounter(String id) {
        if (id == null || !id.startsWith(ID_PREFIX)) return;
        try {
            long num = Long.parseLong(id.substring(ID_PREFIX.length()));
            if (num >= nextIdCounter) {
                nextIdCounter = num + 1;
            }
        } catch (NumberFormatException ignored) {
            // id non-standard (ex : INC-CUSTOM) — on ne synchronise pas
        }
    }

    // ============================================================
    //  Méthodes abstraites — spécifiques à chaque type d'incident.
    // ============================================================

    /** @return le libellé du type d'incident (ex : "PHISHING", "MALWARE", "DDOS"). */
    public abstract String getType();

    /** @return la procédure de réponse à appliquer pour ce type d'incident. */
    public abstract String getResponseProcedure();

    /** @return le SLA par défaut (en heures) pour ce type d'incident. */
    public abstract int computeDefaultSLAHours();

    /** @return le code CWE associé (Common Weakness Enumeration). */
    public abstract String getCWE();

    // ============================================================
    //  Méthodes concrètes — comportement commun à tous les incidents.
    // ============================================================

    /**
     * Ajoute un commentaire à l'incident.
     *
     * @throws IllegalArgumentException si {@code comment} est {@code null}
     */
    public void addComment(Comment comment) {
        if (comment == null) {
            throw new IllegalArgumentException("Le commentaire ne peut pas être null.");
        }
        this.comments.add(comment);
    }

    /**
     * Ajoute une entrée d'historique à l'incident.
     *
     * @throws IllegalArgumentException si {@code entry} est {@code null}
     */
    public void addHistoryEntry(HistoryEntry entry) {
        if (entry == null) {
            throw new IllegalArgumentException("L'entrée d'historique ne peut pas être null.");
        }
        this.history.add(entry);
    }

    /**
     * Ajoute un IOC. L'unicité est gérée par le {@code Set} (basée sur l'id de l'IOC).
     *
     * @throws IllegalArgumentException si {@code ioc} est {@code null}
     */
    public void addIOC(IOC ioc) {
        if (ioc == null) {
            throw new IllegalArgumentException("L'IOC ne peut pas être null.");
        }
        this.iocs.add(ioc);
    }

    /**
     * Effectue une transition de statut, en validant que la transition est autorisée
     * par {@link IncidentStatus#canTransitionTo(IncidentStatus)}.
     *
     * <p><b>Note</b> : cette méthode <i>n'ajoute pas</i> automatiquement d'entrée
     * d'historique. C'est la responsabilité du service qui appelle (M3), car il
     * connaît l'utilisateur qui effectue l'action.</p>
     *
     * @param newStatus le nouveau statut
     * @throws IllegalArgumentException si {@code newStatus} est {@code null}
     * @throws IllegalStateException    si la transition n'est pas autorisée
     */
    public void transitionTo(IncidentStatus newStatus) {
        if (newStatus == null) {
            throw new IllegalArgumentException("Le nouveau statut est obligatoire.");
        }
        if (!this.status.canTransitionTo(newStatus)) {
            throw new IllegalStateException(
                "Transition interdite : " + this.status + " → " + newStatus);
        }
        this.status = newStatus;
    }

    // ============================================================
    //  Implémentation de Assignable<User>.
    // ============================================================

    @Override
    public void assignTo(User user) {
        if (user == null) {
            throw new IllegalArgumentException("L'utilisateur cible de l'assignation est obligatoire.");
        }
        this.assignedTo = user;
    }

    @Override
    public void unassign() {
        this.assignedTo = null;
    }

    @Override
    public User getAssignee() {
        return assignedTo;
    }

    @Override
    public boolean isAssigned() {
        return assignedTo != null;
    }

    // ============================================================
    //  Getters / setters.
    // ============================================================

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Le titre est obligatoire.");
        }
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("La description est obligatoire.");
        }
        this.description = description;
    }

    public Severity getSeverity() {
        return severity;
    }

    public void setSeverity(Severity severity) {
        if (severity == null) {
            throw new IllegalArgumentException("La sévérité est obligatoire.");
        }
        this.severity = severity;
    }

    /** Statut courant — modifiable uniquement via {@link #transitionTo(IncidentStatus)}. */
    public IncidentStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /** @return liste non modifiable des commentaires (mutation via {@link #addComment(Comment)}). */
    public List<Comment> getComments() {
        return Collections.unmodifiableList(comments);
    }

    /** @return liste non modifiable de l'historique. */
    public List<HistoryEntry> getHistory() {
        return Collections.unmodifiableList(history);
    }

    /** @return ensemble non modifiable des IOCs. */
    public Set<IOC> getIocs() {
        return Collections.unmodifiableSet(iocs);
    }

    // ============================================================
    //  equals / hashCode / toString.
    // ============================================================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Incident other)) return false;
        return this.id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return getType() + "Incident{id=" + id
                + ", title='" + title + '\''
                + ", severity=" + severity
                + ", status=" + status
                + ", assignedTo=" + (assignedTo == null ? "—" : assignedTo.getUsername())
                + ", comments=" + comments.size()
                + ", iocs=" + iocs.size()
                + ", createdAt=" + createdAt
                + '}';
    }
}

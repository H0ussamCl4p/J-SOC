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

public abstract class Incident implements Assignable<User> {

    private static final String ID_PREFIX = "INC-";
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

    protected Incident(String title, String description, Severity severity) {
        this(generateId(), title, description, severity,
             IncidentStatus.NEW, LocalDateTime.now(), null);
    }

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

    private static synchronized String generateId() {
        return String.format("%s%03d", ID_PREFIX, nextIdCounter++);
    }

    private static synchronized void syncIdCounter(String id) {
        if (id == null || !id.startsWith(ID_PREFIX)) return;
        try {
            long num = Long.parseLong(id.substring(ID_PREFIX.length()));
            if (num >= nextIdCounter) {
                nextIdCounter = num + 1;
            }
        } catch (NumberFormatException ignored) {
        }
    }

    public abstract String getType();

    public abstract String getResponseProcedure();

    public abstract int computeDefaultSLAHours();

    public abstract String getCWE();

    public void addComment(Comment comment) {
        if (comment == null) {
            throw new IllegalArgumentException("Le commentaire ne peut pas être null.");
        }
        this.comments.add(comment);
    }

    public void addHistoryEntry(HistoryEntry entry) {
        if (entry == null) {
            throw new IllegalArgumentException("L'entrée d'historique ne peut pas être null.");
        }
        this.history.add(entry);
    }

    public void addIOC(IOC ioc) {
        if (ioc == null) {
            throw new IllegalArgumentException("L'IOC ne peut pas être null.");
        }
        this.iocs.add(ioc);
    }

    public void transitionTo(IncidentStatus newStatus) {
        if (newStatus == null) {
            throw new IllegalArgumentException("Le nouveau statut est obligatoire.");
        }
        if (!this.status.canTransitionTo(newStatus)) {
            throw new IllegalStateException(
                "Transition interdite : " + this.status + " -> " + newStatus);
        }
        this.status = newStatus;
    }

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

    public IncidentStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public List<Comment> getComments() {
        return Collections.unmodifiableList(comments);
    }

    public List<HistoryEntry> getHistory() {
        return Collections.unmodifiableList(history);
    }

    public Set<IOC> getIocs() {
        return Collections.unmodifiableSet(iocs);
    }

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
                + ", assignedTo=" + (assignedTo == null ? "-" : assignedTo.getUsername())
                + ", comments=" + comments.size()
                + ", iocs=" + iocs.size()
                + ", createdAt=" + createdAt
                + '}';
    }
}

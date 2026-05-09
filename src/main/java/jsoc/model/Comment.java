package jsoc.model;

import java.time.LocalDateTime;

/**
 * Commentaire posté sur un incident.
 *
 * <p>Un commentaire est <b>immutable</b> une fois créé : ses champs sont déclarés
 * {@code final} et il n'y a pas de setters. C'est un choix de conception courant
 * pour les "value objects" — il garantit qu'un commentaire ne peut pas être
 * modifié rétroactivement, ce qui est important pour la traçabilité dans un SOC.</p>
 *
 * <p><b>Concepts POO illustrés :</b> immutabilité, encapsulation forte (final + getters),
 * surcharge de {@code equals}/{@code hashCode}/{@code toString} (héritage de {@code Object}).</p>
 */
public final class Comment {

    /** Compteur statique pour générer des identifiants uniques. */
    private static long nextId = 1;

    private final long id;
    private final String author;
    private final String content;
    private final LocalDateTime timestamp;

    /**
     * Crée un nouveau commentaire avec un id auto-généré et l'horodatage courant.
     *
     * @param author   le nom d'utilisateur de l'auteur (non null, non vide)
     * @param content  le texte du commentaire (non null, non vide)
     * @throws IllegalArgumentException si un argument est invalide
     */
    public Comment(String author, String content) {
        this(generateId(), author, content, LocalDateTime.now());
    }

    /**
     * Constructeur complet — utilisé lors de la <b>restauration depuis le CSV</b>
     * (par le Membre 2) pour préserver l'id et l'horodatage d'origine.
     *
     * @param id        identifiant
     * @param author    auteur (non null, non vide)
     * @param content   contenu (non null, non vide)
     * @param timestamp horodatage (non null)
     * @throws IllegalArgumentException si un argument est invalide
     */
    public Comment(long id, String author, String content, LocalDateTime timestamp) {
        if (author == null || author.isBlank()) {
            throw new IllegalArgumentException("L'auteur du commentaire est obligatoire.");
        }
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Le contenu du commentaire est obligatoire.");
        }
        if (timestamp == null) {
            throw new IllegalArgumentException("L'horodatage du commentaire est obligatoire.");
        }
        this.id = id;
        this.author = author;
        this.content = content;
        this.timestamp = timestamp;
        // Synchronisation du compteur pour éviter les collisions d'id après restauration.
        if (id >= nextId) {
            nextId = id + 1;
        }
    }

    /** Génère un nouvel identifiant unique de façon thread-safe. */
    private static synchronized long generateId() {
        return nextId++;
    }

    public long getId() {
        return id;
    }

    public String getAuthor() {
        return author;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    /**
     * Égalité basée sur l'<b>id seul</b> : deux commentaires avec le même id
     * sont considérés égaux, même si leur contenu diffère (cas de test ou de bug).
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Comment other)) return false;
        return this.id == other.id;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }

    @Override
    public String toString() {
        return "Comment{id=" + id
                + ", author='" + author + '\''
                + ", timestamp=" + timestamp
                + ", content='" + content + '\''
                + '}';
    }
}

package jsoc.model;

import java.time.LocalDateTime;

public final class Comment {

    private static long nextId = 1;

    private final long id;
    private final String author;
    private final String content;
    private final LocalDateTime timestamp;

    public Comment(String author, String content) {
        this(generateId(), author, content, LocalDateTime.now());
    }

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

    public String getAuthor() {
        return author;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

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

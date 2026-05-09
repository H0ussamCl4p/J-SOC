package jsoc.model.user;

import jsoc.interfaces.Notifiable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class User implements Notifiable {

    public static final String ACTION_CREATE_INCIDENT = "CREATE_INCIDENT";
    public static final String ACTION_VIEW_ASSIGNED   = "VIEW_ASSIGNED";
    public static final String ACTION_COMMENT         = "COMMENT";
    public static final String ACTION_UPDATE_STATUS   = "UPDATE_STATUS";
    public static final String ACTION_ASSIGN          = "ASSIGN";
    public static final String ACTION_CLOSE           = "CLOSE";
    public static final String ACTION_VIEW_STATS      = "VIEW_STATS";
    public static final String ACTION_DELETE          = "DELETE";

    private static long nextId = 1;

    private final long id;
    private String username;
    private String password;
    private String email;
    private final List<String> notifications;

    protected User(String username, String password, String email) {
        this(generateId(), username, password, email);
    }

    protected User(long id, String username, String password, String email) {
        validateUsername(username);
        validatePassword(password);
        validateEmail(email);
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.notifications = new ArrayList<>();
        if (id >= nextId) {
            nextId = id + 1;
        }
    }

    private static synchronized long generateId() {
        return nextId++;
    }

    public abstract String getRole();

    public abstract boolean canPerformAction(String action);

    @Override
    public void notify(String message) {
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("Le message de notification est obligatoire.");
        }
        notifications.add(message);
    }

    @Override
    public List<String> getNotifications() {
        return Collections.unmodifiableList(notifications);
    }

    @Override
    public void clearNotifications() {
        notifications.clear();
    }

    public long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        validateUsername(username);
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        validatePassword(password);
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        validateEmail(email);
        this.email = email;
    }

    private static void validateUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Le nom d'utilisateur est obligatoire.");
        }
    }

    private static void validatePassword(String password) {
        if (password == null || password.length() < 4) {
            throw new IllegalArgumentException("Le mot de passe doit contenir au moins 4 caractères.");
        }
    }

    private static void validateEmail(String email) {
        if (email == null || !email.contains("@") || email.isBlank()) {
            throw new IllegalArgumentException("L'email est invalide.");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User other)) return false;
        return this.id == other.id;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }

    @Override
    public String toString() {
        return getRole() + "{id=" + id
                + ", username='" + username + '\''
                + ", email='" + email + '\''
                + '}';
    }
}

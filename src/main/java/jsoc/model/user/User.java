package jsoc.model.user;

import jsoc.interfaces.Notifiable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Utilisateur du système J-SOC — classe <b>abstraite</b> qui mutualise les attributs
 * et comportements communs aux différents rôles ({@link Analyst}, {@link Manager}).
 *
 * <p>Cette classe illustre plusieurs concepts POO :</p>
 * <ul>
 *   <li><b>Abstraction</b> : on ne peut pas instancier un {@code User} directement,
 *       il faut choisir un rôle concret.</li>
 *   <li><b>Méthodes abstraites</b> : {@link #getRole()} et {@link #canPerformAction(String)}
 *       sont définies sans corps — chaque sous-classe doit fournir sa propre logique.</li>
 *   <li><b>Implémentation d'interface</b> : {@code User} implémente {@link Notifiable}
 *       et fournit une implémentation par défaut héritée par toutes les sous-classes.</li>
 *   <li><b>Encapsulation</b> : les champs sont {@code private}, accédés via getters/setters
 *       avec validations.</li>
 * </ul>
 *
 * <p><b>Sécurité (note pour le correcteur)</b> : dans un vrai projet, le mot de passe
 * serait hashé (BCrypt, Argon2, etc.) et jamais stocké en clair. On le garde en clair ici
 * uniquement pour rester simple dans le cadre académique — la persistance CSV (M2) ne
 * permet de toute façon pas de stockage sécurisé.</p>
 */
public abstract class User implements Notifiable {

    // ============================================================
    //  Constantes d'actions — utilisées par canPerformAction(String).
    //  Centralisées ici pour éviter les typos dans les sous-classes
    //  et dans les services de M3.
    // ============================================================

    public static final String ACTION_CREATE_INCIDENT = "CREATE_INCIDENT";
    public static final String ACTION_VIEW_ASSIGNED   = "VIEW_ASSIGNED";
    public static final String ACTION_COMMENT         = "COMMENT";
    public static final String ACTION_UPDATE_STATUS   = "UPDATE_STATUS";
    public static final String ACTION_ASSIGN          = "ASSIGN";
    public static final String ACTION_CLOSE           = "CLOSE";
    public static final String ACTION_VIEW_STATS      = "VIEW_STATS";
    public static final String ACTION_DELETE          = "DELETE";

    /** Compteur statique pour générer des identifiants uniques. */
    private static long nextId = 1;

    private final long id;
    private String username;
    private String password;
    private String email;
    private final List<String> notifications;

    /**
     * Constructeur "création neuve" — id auto-généré.
     *
     * <p>Marqué {@code protected} : seules les sous-classes peuvent l'invoquer,
     * cohérent avec le caractère abstrait de la classe.</p>
     */
    protected User(String username, String password, String email) {
        this(generateId(), username, password, email);
    }

    /**
     * Constructeur complet — utilisé pour la restauration depuis le CSV (M2).
     *
     * @throws IllegalArgumentException si un argument est invalide
     */
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

    // ============================================================
    //  Méthodes abstraites — chaque sous-classe doit les implémenter.
    // ============================================================

    /**
     * Retourne le libellé du rôle de l'utilisateur (ex : "ANALYST", "MANAGER").
     * Utile pour l'affichage CLI et les rapports.
     */
    public abstract String getRole();

    /**
     * Indique si l'utilisateur a le droit d'effectuer l'action donnée.
     *
     * @param action nom de l'action (utiliser les constantes {@code ACTION_*})
     * @return {@code true} si l'action est autorisée pour ce rôle
     */
    public abstract boolean canPerformAction(String action);

    // ============================================================
    //  Implémentation de Notifiable.
    // ============================================================

    @Override
    public void notify(String message) {
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("Le message de notification est obligatoire.");
        }
        notifications.add(message);
    }

    @Override
    public List<String> getNotifications() {
        // Vue non modifiable — protège l'état interne contre des modifications externes.
        return Collections.unmodifiableList(notifications);
    }

    @Override
    public void clearNotifications() {
        notifications.clear();
    }

    // ============================================================
    //  Getters / setters.
    // ============================================================

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

    // ============================================================
    //  Validations privées — extraites pour la lisibilité.
    // ============================================================

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
        // Validation minimaliste (pas de regex complexe pour rester lisible).
        if (email == null || !email.contains("@") || email.isBlank()) {
            throw new IllegalArgumentException("L'email est invalide.");
        }
    }

    // ============================================================
    //  equals / hashCode / toString.
    // ============================================================

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

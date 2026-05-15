package jsoc.service;

import jsoc.exception.AuthenticationFailedException;
import jsoc.exception.UnauthorizedActionException;
import jsoc.model.user.Analyst;
import jsoc.model.user.Manager;
import jsoc.model.user.User;
import jsoc.repository.UserRepository;

import java.util.Optional;

/**
 * AuthService — authentification et gestion de session.
 */
public class AuthService {

    private final UserRepository userRepository;
    private User currentUser;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.currentUser = null;
    }

    // ── Login / Logout ────────────────────────────────────────────────────────

    public User login(String username, String password)
            throws AuthenticationFailedException {

        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            throw new AuthenticationFailedException("Les identifiants ne peuvent pas être vides.");
        }

        Optional<User> opt = userRepository.findByUsername(username);

        if (opt.isEmpty() || !opt.get().getPassword().equals(password)) {
            throw new AuthenticationFailedException("Identifiants invalides.");
        }

        this.currentUser = opt.get();
        return currentUser;
    }

    public void logout() {
        if (currentUser != null) {
            System.out.println("[AUTH] Au revoir, " + currentUser.getUsername());
            currentUser = null;
        }
    }

    // ── Session ───────────────────────────────────────────────────────────────

    public User getCurrentUser() {
        return currentUser;
    }

    public boolean isAuthenticated() {
        return currentUser != null;
    }

    public boolean isManager() {
        return currentUser instanceof Manager;
    }

    public boolean isAnalyst() {
        return currentUser instanceof Analyst;
    }

    // ── Guards ────────────────────────────────────────────────────────────────

    public void requireAuthenticated() throws UnauthorizedActionException {
        if (!isAuthenticated()) {
            throw new UnauthorizedActionException(
                "ACTION_REQUIRES_LOGIN",
                "Vous devez être connecté pour effectuer cette action.");
        }
    }

    public void requireManager() throws UnauthorizedActionException {
        requireAuthenticated();
        if (!isManager()) {
            throw new UnauthorizedActionException(
                User.ACTION_ASSIGN,
                "Accès refusé : rôle Manager requis. Utilisateur : "
                + currentUser.getUsername());
        }
    }

    public void requireAnalyst() throws UnauthorizedActionException {
        requireAuthenticated();
        if (!isAnalyst()) {
            throw new UnauthorizedActionException(
                User.ACTION_VIEW_ASSIGNED,
                "Accès refusé : rôle Analyst requis. Utilisateur : "
                + currentUser.getUsername());
        }
    }

    public void requirePermission(String action) throws UnauthorizedActionException {
        requireAuthenticated();
        if (!currentUser.canPerformAction(action)) {
            throw new UnauthorizedActionException(
                action,
                "L'utilisateur '" + currentUser.getUsername()
                + "' n'a pas la permission : " + action);
        }
    }

    // ── Gestion utilisateurs (Manager uniquement) ─────────────────────────────

    public void registerUser(User newUser) throws UnauthorizedActionException {
        requireManager();
        if (userRepository.findByUsername(newUser.getUsername()).isPresent()) {
            throw new IllegalArgumentException(
                "Nom d'utilisateur déjà existant : " + newUser.getUsername());
        }
        userRepository.save(newUser);
        System.out.println("[AUTH] Utilisateur enregistré : " + newUser.getUsername());
    }

    public void changePassword(String oldPassword, String newPassword)
            throws AuthenticationFailedException, UnauthorizedActionException {
        requireAuthenticated();
        if (!currentUser.getPassword().equals(oldPassword)) {
            throw new AuthenticationFailedException("Ancien mot de passe incorrect.");
        }
        currentUser.setPassword(newPassword);
        userRepository.save(currentUser);
        System.out.println("[AUTH] Mot de passe mis à jour : " + currentUser.getUsername());
    }
}

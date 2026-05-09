package jsoc.model.user;

import java.util.Set;

/**
 * Analyste SOC — utilisateur de premier niveau.
 *
 * <p>Un analyste peut :</p>
 * <ul>
 *   <li>Créer un incident</li>
 *   <li>Consulter les incidents qui lui sont assignés</li>
 *   <li>Commenter un incident</li>
 *   <li>Mettre à jour le statut d'un incident</li>
 * </ul>
 *
 * <p>Il ne peut <b>PAS</b> assigner, fermer, supprimer ou consulter les statistiques
 * — ces actions sont réservées au {@link Manager}.</p>
 *
 * <p><b>Concept POO illustré :</b> <i>héritage</i> et <i>polymorphisme</i>.
 * Cette classe étend {@link User} et redéfinit les méthodes abstraites
 * {@code getRole()} et {@code canPerformAction()} pour offrir un comportement
 * spécifique au rôle d'analyste.</p>
 */
public class Analyst extends User {

    /**
     * Ensemble immutable des actions autorisées pour un analyste.
     * Utilisation de {@link Set#of(Object...)} (Java 9+) qui retourne un Set immutable
     * — protection contre les modifications accidentelles à l'exécution.
     */
    private static final Set<String> ALLOWED_ACTIONS = Set.of(
            ACTION_CREATE_INCIDENT,
            ACTION_VIEW_ASSIGNED,
            ACTION_COMMENT,
            ACTION_UPDATE_STATUS
    );

    public Analyst(String username, String password, String email) {
        super(username, password, email);
    }

    public Analyst(long id, String username, String password, String email) {
        super(id, username, password, email);
    }

    @Override
    public String getRole() {
        return "ANALYST";
    }

    @Override
    public boolean canPerformAction(String action) {
        return ALLOWED_ACTIONS.contains(action);
    }
}

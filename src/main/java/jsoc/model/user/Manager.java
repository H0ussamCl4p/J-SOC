package jsoc.model.user;

import java.util.Set;

/**
 * Manager SOC — utilisateur avec pouvoirs étendus.
 *
 * <p>Un manager possède <b>toutes les actions de l'{@link Analyst}</b>, plus :</p>
 * <ul>
 *   <li>Assigner un incident à un analyste</li>
 *   <li>Fermer un incident résolu</li>
 *   <li>Consulter les statistiques globales du SOC</li>
 *   <li>Supprimer un incident</li>
 * </ul>
 *
 * <p><b>Choix de conception (Liskov)</b> : on hérite directement de {@link User}
 * et pas de {@link Analyst}, même si le manager "fait tout ce que l'analyste fait".
 * Hériter d'{@code Analyst} créerait un couplage fragile (un manager <i>n'est pas</i>
 * un analyste, c'est un autre rôle qui se trouve avoir des permissions plus larges).
 * On exprime cette différence par les permissions, pas par la hiérarchie de classes.</p>
 */
public class Manager extends User {

    /**
     * Ensemble des actions autorisées : on liste explicitement toutes les actions
     * (analyste + manager) plutôt que de calculer une union à l'exécution.
     * C'est plus lisible et plus rapide.
     */
    private static final Set<String> ALLOWED_ACTIONS = Set.of(
            // Actions héritées du périmètre analyste
            ACTION_CREATE_INCIDENT,
            ACTION_VIEW_ASSIGNED,
            ACTION_COMMENT,
            ACTION_UPDATE_STATUS,
            // Actions spécifiques au manager
            ACTION_ASSIGN,
            ACTION_CLOSE,
            ACTION_VIEW_STATS,
            ACTION_DELETE
    );

    public Manager(String username, String password, String email) {
        super(username, password, email);
    }

    public Manager(long id, String username, String password, String email) {
        super(id, username, password, email);
    }

    @Override
    public String getRole() {
        return "MANAGER";
    }

    @Override
    public boolean canPerformAction(String action) {
        return ALLOWED_ACTIONS.contains(action);
    }
}

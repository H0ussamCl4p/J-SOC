package jsoc.interfaces;

/**
 * Contrat des entités qui peuvent être <b>assignées à un porteur</b>.
 *
 * <p>Dans J-SOC, ce contrat sera implémenté par {@code Incident} pour permettre
 * de l'assigner à un {@code User} (analyste ou manager). L'interface est générique
 * afin de rester découplée de la hiérarchie {@code User} et de pouvoir être réutilisée
 * dans d'autres contextes (par exemple, assigner une tâche à une équipe).</p>
 *
 * <p><b>Concept POO mis en avant :</b> ce contrat illustre le principe de
 * <i>séparation des responsabilités</i> — la capacité à être assigné est définie
 * indépendamment du type concret de l'entité qui l'implémente.</p>
 *
 * @param <U> le type du porteur auquel on assigne (typiquement {@code User})
 */
public interface Assignable<U> {

    /**
     * Assigne l'entité au porteur donné. Si l'entité était déjà assignée,
     * la nouvelle assignation remplace l'ancienne.
     *
     * @param user le porteur (ne doit pas être {@code null})
     * @throws IllegalArgumentException si {@code user} est {@code null}
     */
    void assignTo(U user);

    /**
     * Retire l'assignation courante. Sans effet si l'entité n'était pas assignée.
     */
    void unassign();

    /**
     * Retourne le porteur courant.
     *
     * @return le porteur, ou {@code null} si l'entité n'est pas assignée
     */
    U getAssignee();

    /**
     * Indique si l'entité est actuellement assignée à un porteur.
     *
     * @return {@code true} si {@link #getAssignee()} n'est pas {@code null}
     */
    boolean isAssigned();
}

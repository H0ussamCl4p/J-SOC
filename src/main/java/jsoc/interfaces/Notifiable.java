package jsoc.interfaces;

import java.util.List;

/**
 * Contrat des entités qui peuvent <b>recevoir des notifications</b>.
 *
 * <p>Dans J-SOC, ce contrat sera implémenté par {@code User} : un analyste ou
 * un manager doit pouvoir recevoir des messages quand un incident lui est assigné,
 * quand un commentaire est posté, quand un SLA est dépassé, etc.</p>
 *
 * <p>L'interface ne précise pas <i>comment</i> les notifications sont stockées
 * (en mémoire, persistées, envoyées par mail, etc.). Cette flexibilité illustre
 * le principe d'<b>abstraction</b> : on définit un comportement attendu sans
 * imposer son implémentation.</p>
 *
 * <p><b>Note technique</b> : la méthode {@link #notify(String)} ne rentre pas
 * en conflit avec {@code Object.notify()} (sans argument, héritée de tous les
 * objets Java pour la synchronisation des threads). Java distingue les deux
 * grâce à leur signature différente — c'est un cas de <i>surcharge</i>
 * (overloading), pas de redéfinition (overriding).</p>
 */
public interface Notifiable {

    /**
     * Envoie une notification à l'entité. Le message est stocké et pourra être
     * récupéré ultérieurement via {@link #getNotifications()}.
     *
     * @param message le contenu de la notification (ne doit pas être {@code null} ni vide)
     * @throws IllegalArgumentException si {@code message} est {@code null} ou vide
     */
    void notify(String message);

    /**
     * Retourne toutes les notifications reçues, dans l'ordre d'arrivée (la plus
     * ancienne en premier).
     *
     * <p>L'implémentation devrait retourner une <i>copie défensive</i> ou une
     * vue non modifiable pour éviter qu'un appelant ne modifie l'état interne.</p>
     *
     * @return la liste des messages reçus (jamais {@code null}, peut être vide)
     */
    List<String> getNotifications();

    /**
     * Vide la liste des notifications. Utile une fois qu'elles ont été affichées
     * à l'utilisateur dans la CLI.
     */
    void clearNotifications();
}

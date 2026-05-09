package jsoc.model.enums;

/**
 * États possibles d'un incident dans son cycle de vie.
 *
 * <p>Le cycle de vie d'un incident est représenté par une <b>machine à états</b> :
 * un incident commence en {@link #NEW}, est ensuite assigné à un analyste,
 * passe en cours de traitement, puis est résolu et finalement clôturé.
 * Un incident en cours peut être escaladé si une intervention de niveau supérieur
 * est nécessaire.</p>
 *
 * <p>Transitions autorisées :</p>
 * <ul>
 *   <li>{@code NEW} → {@code ASSIGNED}</li>
 *   <li>{@code ASSIGNED} → {@code IN_PROGRESS}</li>
 *   <li>{@code IN_PROGRESS} → {@code RESOLVED} ou {@code ESCALATED}</li>
 *   <li>{@code RESOLVED} → {@code CLOSED}</li>
 *   <li>{@code ESCALATED} → {@code IN_PROGRESS} ou {@code RESOLVED}</li>
 *   <li>{@code CLOSED} → état terminal (aucune transition possible)</li>
 * </ul>
 *
 * <p>La méthode {@link #canTransitionTo(IncidentStatus)} centralise cette logique
 * pour éviter de la dupliquer dans la classe {@code Incident}.</p>
 */
public enum IncidentStatus {

    /** Incident nouvellement créé, pas encore assigné. */
    NEW,

    /** Incident assigné à un analyste, en attente de prise en charge. */
    ASSIGNED,

    /** Incident en cours de traitement par un analyste. */
    IN_PROGRESS,

    /** Incident résolu, en attente de clôture par un manager. */
    RESOLVED,

    /** Incident clôturé — état terminal. */
    CLOSED,

    /** Incident escaladé vers un niveau supérieur (ex : manager, équipe externe). */
    ESCALATED;

    /**
     * Indique si une transition vers le statut donné est autorisée depuis le statut courant.
     *
     * <p>Cette validation évite les sauts d'étapes incohérents (par exemple
     * passer directement de {@code NEW} à {@code CLOSED} sans traitement).
     * Elle utilise une <i>switch expression</i> Java 17+ pour rester concise.</p>
     *
     * @param next le statut cible (peut être {@code null}, auquel cas la transition est refusée)
     * @return {@code true} si la transition est valide, {@code false} sinon
     */
    public boolean canTransitionTo(IncidentStatus next) {
        if (next == null) {
            return false;
        }
        return switch (this) {
            case NEW         -> next == ASSIGNED;
            case ASSIGNED    -> next == IN_PROGRESS;
            case IN_PROGRESS -> next == RESOLVED || next == ESCALATED;
            case RESOLVED    -> next == CLOSED;
            case ESCALATED   -> next == IN_PROGRESS || next == RESOLVED;
            case CLOSED      -> false; // état terminal, aucune transition autorisée
        };
    }

    /**
     * Indique si l'incident est dans un état terminal, c'est-à-dire qu'aucune
     * transition n'est plus possible. Utile pour griser les actions dans la CLI.
     *
     * @return {@code true} si le statut est {@link #CLOSED}, {@code false} sinon
     */
    public boolean isTerminal() {
        return this == CLOSED;
    }
}

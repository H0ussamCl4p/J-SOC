package jsoc.model.enums;

/**
 * Niveau de sévérité d'un incident de sécurité.
 *
 * Chaque niveau porte un SLA (Service Level Agreement) par défaut, exprimé en heures.
 * Le SLA correspond au délai maximal attendu entre la création de l'incident
 * et sa résolution. Plus la sévérité est haute, plus le SLA est court.
 *
 * <p>Exemple d'utilisation des concepts POO :
 * cet enum illustre l'<b>encapsulation</b> (le champ {@code slaHours} est privé final,
 * exposé uniquement via un getter) et l'utilisation d'un <b>constructeur d'enum</b>
 * pour associer une donnée métier à chaque constante.</p>
 */
public enum Severity {

    /** Sévérité basse — incident mineur, SLA de 72 heures. */
    LOW(72),

    /** Sévérité moyenne — incident à traiter dans la journée, SLA de 24 heures. */
    MEDIUM(24),

    /** Sévérité haute — incident prioritaire, SLA de 4 heures. */
    HIGH(4),

    /** Sévérité critique — incident bloquant, SLA d'1 heure. */
    CRITICAL(1);

    /** Délai de résolution par défaut associé à cette sévérité, en heures. */
    private final int slaHours;

    /**
     * Constructeur de l'enum, appelé une seule fois par constante au chargement de la classe.
     *
     * @param slaHours délai de résolution par défaut, en heures
     */
    Severity(int slaHours) {
        this.slaHours = slaHours;
    }

    /**
     * Retourne le SLA par défaut associé à cette sévérité.
     *
     * @return le délai en heures (1, 4, 24 ou 72 selon le niveau)
     */
    public int getSlaHours() {
        return slaHours;
    }
}

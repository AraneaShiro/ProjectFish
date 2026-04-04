package fish.nettoyage;

import fish.acquisition.DfIndividu;

/**
 * Utilitaire de nettoyage spécifique à un DfIndividu.
 * Toutes les méthodes sont statiques — cette classe ne s'instancie pas.
 *
 * La colonne Anisakis est "nbvers" en priorité, puis "taux".
 *
 * @author Jules Grenesche
 * @version 0.2
 */
public final class NettoyageDfIndividu {

    private NettoyageDfIndividu() {}

    public static int suppressionInvalid(DfIndividu df) {
        return NettoyageDataframe.suppressionInvalid(df);
    }

    public static int suppressionInvalid(DfIndividu df, double seuilNull) {
        return NettoyageDataframe.suppressionInvalid(df, seuilNull);
    }

    public static int suppressionColonnesVides(DfIndividu df) {
        return NettoyageDataframe.suppressionColonnesVides(df);
    }

    public static int triAnisakis(DfIndividu df) {
        return NettoyageDataframe.triAnisakis(df, getMotCleAnisakis(df));
    }

    public static int reconnaissanceAnisakis(DfIndividu df) {
        return NettoyageDataframe.reconnaissanceAnisakis(df, getMotCleAnisakis(df));
    }

    // ── Utilitaire privé ─────────────────────────────────────────────────────

    /** Priorité : "nbvers" → "taux" */
    private static String getMotCleAnisakis(DfIndividu df) {
        if (NettoyageDataframe.getIndexColonne(df, "nbvers") >= 0) return "nbvers";
        return "taux";
    }
}

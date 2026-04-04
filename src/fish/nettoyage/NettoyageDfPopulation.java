package fish.nettoyage;

import fish.acquisition.DfPopulation;

/**
 * Utilitaire de nettoyage spécifique à un DfPopulation.
 * Toutes les méthodes sont statiques — cette classe ne s'instancie pas.
 *
 * La colonne Anisakis est choisie selon le format :
 * - Multi-période : "prévalence" → "abondance"
 * - Standard      : "infect"    → "abondance"
 *
 * @author Jules Grenesche
 * @version 0.2
 */
public final class NettoyageDfPopulation {

    private NettoyageDfPopulation() {}

    public static int suppressionInvalid(DfPopulation df) {
        return NettoyageDataframe.suppressionInvalid(df);
    }

    public static int suppressionInvalid(DfPopulation df, double seuilNull) {
        return NettoyageDataframe.suppressionInvalid(df, seuilNull);
    }

    public static int suppressionColonnesVides(DfPopulation df) {
        return NettoyageDataframe.suppressionColonnesVides(df);
    }

    public static int triAnisakis(DfPopulation df) {
        return NettoyageDataframe.triAnisakis(df, getMotCleAnisakis(df));
    }

    public static int reconnaissanceAnisakis(DfPopulation df) {
        return NettoyageDataframe.reconnaissanceAnisakis(df, getMotCleAnisakis(df));
    }

    // ── Utilitaire privé ─────────────────────────────────────────────────────

    /** Choisit le mot-clé Anisakis selon le format du DfPopulation. */
    private static String getMotCleAnisakis(DfPopulation df) {
        if (df.isFormatMultiPeriode()) {
            if (NettoyageDataframe.getIndexColonne(df, "prévalence") >= 0) return "prévalence";
            if (NettoyageDataframe.getIndexColonne(df, "prevalence") >= 0) return "prevalence";
            return "abondance";
        }
        if (NettoyageDataframe.getIndexColonne(df, "infect") >= 0) return "infect";
        return "abondance";
    }
}

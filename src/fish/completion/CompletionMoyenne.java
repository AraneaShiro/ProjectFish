package fish.completion;

import fish.acquisition.DataframeComplet;
import fish.exceptions.OutOfBoundException;

import java.util.ArrayList;
import java.util.List;

/**
 * Utilitaire de complétion par moyenne.
 * Toutes les méthodes sont statiques — cette classe ne s'instancie pas.
 *
 * @author Jules Grenesche
 * @version 2
 */
public final class CompletionMoyenne {

    // Empêche l'instanciation
    private CompletionMoyenne() {
    }

    /**
     * Complète TOUTES les colonnes numériques nulles du dataframe par leur moyenne.
     *
     * @param df le dataframe à compléter
     * @return le nombre de cases complétées
     */
    public static int completerTout(DataframeComplet df) {
        int total = 0;
        for (int j = 0; j < df.getNbCol(); j++) {
            total += completerColonne(df, j);
        }
        System.out.println(total + " case(s) complétée(s) par la moyenne.");
        return total;
    }

    /**
     * Complète une colonne spécifique par sa moyenne.
     *
     * @param df  le dataframe à compléter
     * @param col l'index de la colonne
     * @return le nombre de cases complétées dans cette colonne
     */
    public static int completerColonne(DataframeComplet df, int col) {
        double moyenne = calculerMoyenneColonne(df, col);
        if (Double.isNaN(moyenne))
            return 0; // Colonne non numérique

        int completes = 0;
        for (int i = 0; i < df.getNbLignes(); i++) {
            try {
                if (df.getCase(i, col) == null) {
                    df.setCase(i, col, moyenne);
                    completes++;
                }
            } catch (OutOfBoundException e) {
                /* ignoré */ }
        }
        return completes;
    }

    /**
     * Complète uniquement les colonnes dont le nom contient le mot-clé.
     *
     * @param df     le dataframe à compléter
     * @param motCle le mot-clé à rechercher dans les noms de colonnes
     * @return le nombre de cases complétées
     */
    public static int completerColonneParNom(DataframeComplet df, String motCle) {
        int total = 0;
        String[] noms = df.getNomColonnes();
        for (int j = 0; j < noms.length; j++) {
            if (noms[j].toLowerCase().contains(motCle.toLowerCase())) {
                total += completerColonne(df, j);
            }
        }
        return total;
    }

    // ── Utilitaire privé ─────────────────────────────────────────────────────

    private static double calculerMoyenneColonne(DataframeComplet df, int col) {
        List<Double> valeurs = new ArrayList<>();
        for (int i = 0; i < df.getNbLignes(); i++) {
            try {
                Object val = df.getCase(i, col);
                if (val instanceof Number)
                    valeurs.add(((Number) val).doubleValue());
            } catch (OutOfBoundException e) {
                /* ignoré */ }
        }
        if (valeurs.isEmpty())
            return Double.NaN;
        return valeurs.stream().mapToDouble(Double::doubleValue).average().orElse(Double.NaN);
    }
}

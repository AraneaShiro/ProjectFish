// ── Package ───────────────────────────────────────────────────
package fish.completion;

// ── Import ───────────────────────────────────────────────────
import fish.acquisition.DataframeComplet;
import fish.exceptions.OutOfBoundException;
import java.util.ArrayList;
import java.util.List;

// ── TESTE : NON ───────────────────────────────────────────────────
/**
 * Complète les valeurs null d'un Dataframe par la moyenne de leur colonne.
 * Seules les colonnes numériques sont complétées.
 * Les colonnes String/Boolean sont ignorées.
 *
 * @author Jules Grenesche
 * @version 1
 */
public class CompletionMoyenne {

    private final DataframeComplet df;

    public CompletionMoyenne(DataframeComplet df) {
        this.df = df;
    }

    /**
     * Complète TOUTES les colonnes numériques du dataframe.
     *
     * @return le nombre de cases complétées
     */
    public int completerTout() {
        int total = 0;
        for (int j = 0; j < df.getNbCol(); j++) {
            total += completerColonne(j);
        }
        System.out.println(total + " case(s) complétée(s) par la moyenne.");
        return total;
    }

    /**
     * Complète une colonne spécifique par sa moyenne.
     *
     * @param col l'index de la colonne
     * @return le nombre de cases complétées dans cette colonne
     */
    public int completerColonne(int col) {
        double moyenne = calculerMoyenneColonne(col);
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
     * @param motCle le mot-clé à rechercher dans les noms de colonnes
     * @return le nombre de cases complétées
     */
    public int completerColonneParNom(String motCle) {
        int total = 0;
        String[] noms = df.getNomColonnes();
        for (int j = 0; j < noms.length; j++) {
            if (noms[j].toLowerCase().contains(motCle.toLowerCase())) {
                total += completerColonne(j);
            }
        }
        return total;
    }

    // ── Utilitaire ───────────────────────────────────────────────────────────

    private double calculerMoyenneColonne(int col) {
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
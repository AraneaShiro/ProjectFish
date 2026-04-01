package fish.completion;

import fish.acquisition.DataframeComplet;
import fish.exceptions.OutOfBoundException;

import java.util.ArrayList;
import java.util.List;

/**
 * Complète les valeurs null d'une colonne cible par régression linéaire simple
 * à partir d'une colonne source (prédicteur).
 *
 * Formule de la régression linéaire :
 * y = a*x + b
 * a = Cov(X,Y) / Var(X)
 * b = moy(Y) - a * moy(X)
 *
 * @author Jules Grenesche
 * @version 0.1
 */
public class CompletionRegression {

    private final DataframeComplet df;

    public CompletionRegression(DataframeComplet df) {
        this.df = df;
    }

    /**
     * Complète les null de la colonne cible en utilisant la régression linéaire
     * calculée sur les paires (colSource, colCible) non-null existantes.
     *
     * @param colSource l'index de la colonne prédicteur (X)
     * @param colCible  l'index de la colonne à compléter (Y)
     * @return le nombre de cases complétées, ou -1 si la régression est impossible
     */
    public int completerParRegression(int colSource, int colCible) {
        // Collecte les paires (x, y) où ni x ni y ne sont null
        List<double[]> paires = new ArrayList<>();
        for (int i = 0; i < df.getNbLignes(); i++) {
            try {
                Object valX = df.getCase(i, colSource);
                Object valY = df.getCase(i, colCible);
                if (valX instanceof Number && valY instanceof Number) {
                    paires.add(new double[] {
                            ((Number) valX).doubleValue(),
                            ((Number) valY).doubleValue()
                    });
                }
            } catch (OutOfBoundException e) {
                /* ignoré */ }
        }

        if (paires.size() < 2) {
            System.out.println("Régression impossible : pas assez de paires complètes (minimum 2).");
            return -1;
        }

        // Calcul des moyennes
        double moyX = paires.stream().mapToDouble(p -> p[0]).average().orElse(0);
        double moyY = paires.stream().mapToDouble(p -> p[1]).average().orElse(0);

        // Calcul de a (pente) = Cov(X,Y) / Var(X)
        double cov = 0, varX = 0;
        for (double[] p : paires) {
            cov += (p[0] - moyX) * (p[1] - moyY);
            varX += Math.pow(p[0] - moyX, 2);
        }

        if (varX == 0) {
            System.out.println("Régression impossible : variance de X nulle (X constant).");
            return -1;
        }

        double a = cov / varX;
        double b = moyY - a * moyX; // ordonnée à l'origine

        System.out.printf("Régression calculée : y = %.4f * x + %.4f  (sur %d paires)%n",
                a, b, paires.size());

        // Complétion des null dans colCible
        int completes = 0;
        for (int i = 0; i < df.getNbLignes(); i++) {
            try {
                Object valY = df.getCase(i, colCible);
                Object valX = df.getCase(i, colSource);
                if (valY == null && valX instanceof Number) {
                    double x = ((Number) valX).doubleValue();
                    double yPredit = a * x + b;
                    df.setCase(i, colCible, yPredit);
                    completes++;
                }
            } catch (OutOfBoundException e) {
                /* ignoré */ }
        }

        System.out.println(completes + " case(s) complétée(s) par régression.");
        return completes;
    }

    /**
     * Complète toutes les colonnes numériques nulles en cherchant automatiquement
     * le meilleur prédicteur (corrélation maximale) pour chacune.
     *
     * @return le nombre total de cases complétées
     */
    public int completerToutParMeilleurPredicteur() {
        int total = 0;
        int nbCol = df.getNbCol();

        for (int colCible = 0; colCible < nbCol; colCible++) {
            if (!colonneADesNull(colCible) || !colonneEstNumerique(colCible))
                continue;

            int meilleurPredicteur = trouverMeilleurPredicteur(colCible);
            if (meilleurPredicteur < 0)
                continue;

            System.out.println("Complétion de \"" + df.getNomColonnes()[colCible]
                    + "\" via \"" + df.getNomColonnes()[meilleurPredicteur] + "\"");
            total += completerParRegression(meilleurPredicteur, colCible);
        }
        return total;
    }

    // ── Utilitaires ──────────────────────────────────────────────────────────

    /**
     * Trouve la colonne source ayant la corrélation absolue maximale avec colCible.
     * Ne prend en compte que les lignes où les deux colonnes sont non-null.
     *
     * @param colCible la colonne à prédire
     * @return l'index du meilleur prédicteur, ou -1 si aucun trouvé
     */
    private int trouverMeilleurPredicteur(int colCible) {
        int meilleur = -1;
        double maxCorr = 0;
        int nbCol = df.getNbCol();

        for (int j = 0; j < nbCol; j++) {
            if (j == colCible || !colonneEstNumerique(j))
                continue;

            double corr = Math.abs(calculerCorrelation(j, colCible));
            if (corr > maxCorr) {
                maxCorr = corr;
                meilleur = j;
            }
        }
        return meilleur;
    }

    /**
     * Calcule la corrélation de Pearson entre deux colonnes (sur les paires
     * complètes)
     */
    private double calculerCorrelation(int col1, int col2) {
        List<double[]> paires = new ArrayList<>();
        for (int i = 0; i < df.getNbLignes(); i++) {
            try {
                Object v1 = df.getCase(i, col1);
                Object v2 = df.getCase(i, col2);
                if (v1 instanceof Number && v2 instanceof Number) {
                    paires.add(new double[] {
                            ((Number) v1).doubleValue(),
                            ((Number) v2).doubleValue()
                    });
                }
            } catch (OutOfBoundException e) {
                /* ignoré */ }
        }
        if (paires.size() < 2)
            return 0;

        double moy1 = paires.stream().mapToDouble(p -> p[0]).average().orElse(0);
        double moy2 = paires.stream().mapToDouble(p -> p[1]).average().orElse(0);

        double num = 0, d1 = 0, d2 = 0;
        for (double[] p : paires) {
            num += (p[0] - moy1) * (p[1] - moy2);
            d1 += Math.pow(p[0] - moy1, 2);
            d2 += Math.pow(p[1] - moy2, 2);
        }
        double denom = Math.sqrt(d1 * d2);
        return denom == 0 ? 0 : num / denom;
    }

    private boolean colonneADesNull(int col) {
        for (int i = 0; i < df.getNbLignes(); i++) {
            try {
                if (df.getCase(i, col) == null)
                    return true;
            } catch (OutOfBoundException e) {
                /* ignoré */ }
        }
        return false;
    }

    private boolean colonneEstNumerique(int col) {
        for (int i = 0; i < df.getNbLignes(); i++) {
            try {
                Object val = df.getCase(i, col);
                if (val != null)
                    return val instanceof Number;
            } catch (OutOfBoundException e) {
                /* ignoré */ }
        }
        return false;
    }
}

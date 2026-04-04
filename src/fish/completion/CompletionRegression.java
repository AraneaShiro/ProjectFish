package fish.completion;

import fish.acquisition.DataframeComplet;
import fish.exceptions.OutOfBoundException;

import java.util.ArrayList;
import java.util.List;

/**
 * Utilitaire de complétion par régression linéaire simple.
 * Toutes les méthodes sont statiques — cette classe ne s'instancie pas.
 *
 * Formule : y = a*x + b
 * a = Cov(X,Y) / Var(X)
 * b = moy(Y) - a * moy(X)
 *
 * @author Jules Grenesche
 * @version 2
 */
public final class CompletionRegression {

    // Empêche l'instanciation
    private CompletionRegression() {
    }

    /**
     * Complète les null de colCible par régression linéaire sur colSource.
     *
     * @param df        le dataframe à compléter
     * @param colSource l'index de la colonne prédicteur (X)
     * @param colCible  l'index de la colonne à compléter (Y)
     * @return le nombre de cases complétées, ou -1 si la régression est impossible
     */
    public static int completerParRegression(DataframeComplet df, int colSource, int colCible) {
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

        double moyX = paires.stream().mapToDouble(p -> p[0]).average().orElse(0);
        double moyY = paires.stream().mapToDouble(p -> p[1]).average().orElse(0);

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
        double b = moyY - a * moyX;

        System.out.printf("Régression calculée : y = %.4f * x + %.4f  (sur %d paires)%n",
                a, b, paires.size());

        int completes = 0;
        for (int i = 0; i < df.getNbLignes(); i++) {
            try {
                Object valY = df.getCase(i, colCible);
                Object valX = df.getCase(i, colSource);
                if (valY == null && valX instanceof Number) {
                    double yPredit = a * ((Number) valX).doubleValue() + b;
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
     * @param df le dataframe à compléter
     * @return le nombre total de cases complétées
     */
    public static int completerToutParMeilleurPredicteur(DataframeComplet df) {
        int total = 0;
        int nbCol = df.getNbCol();

        for (int colCible = 0; colCible < nbCol; colCible++) {
            if (!colonneADesNull(df, colCible) || !colonneEstNumerique(df, colCible))
                continue;

            int meilleurPredicteur = trouverMeilleurPredicteur(df, colCible);
            if (meilleurPredicteur < 0)
                continue;

            System.out.println("Complétion de \"" + df.getNomColonnes()[colCible]
                    + "\" via \"" + df.getNomColonnes()[meilleurPredicteur] + "\"");
            total += completerParRegression(df, meilleurPredicteur, colCible);
        }
        return total;
    }

    // ── Utilitaires privés ────────────────────────────────────────────────────

    private static int trouverMeilleurPredicteur(DataframeComplet df, int colCible) {
        int meilleur = -1;
        double maxCorr = 0;
        for (int j = 0; j < df.getNbCol(); j++) {
            if (j == colCible || !colonneEstNumerique(df, j))
                continue;
            double corr = Math.abs(calculerCorrelation(df, j, colCible));
            if (corr > maxCorr) {
                maxCorr = corr;
                meilleur = j;
            }
        }
        return meilleur;
    }

    private static double calculerCorrelation(DataframeComplet df, int col1, int col2) {
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

    private static boolean colonneADesNull(DataframeComplet df, int col) {
        for (int i = 0; i < df.getNbLignes(); i++) {
            try {
                if (df.getCase(i, col) == null)
                    return true;
            } catch (OutOfBoundException e) {
                /* ignoré */ }
        }
        return false;
    }

    private static boolean colonneEstNumerique(DataframeComplet df, int col) {
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

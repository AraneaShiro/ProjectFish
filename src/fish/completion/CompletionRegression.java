package fish.completion;

import fish.acquisition.DataframeComplet;
import fish.acquisition.DfIndividu;
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

     public static void main(String[] args) {
        int ok = 0, tot = 0;
        System.out.println("================================================================================");
        System.out.println("                   TESTS CompletionRegression");
        System.out.println("================================================================================");
        System.out.println();

        // TEST 1 : Relation lineaire parfaite y = 2x + 1
        System.out.println("--- TEST 1 : Regression lineaire parfaite y = 2x + 1 --------------------------");
        try {
            Object[][] data = {{1.0, 3.0}, {2.0, 5.0}, {3.0, null}, {4.0, null}};
            DfIndividu df = new DfIndividu(4, new String[]{"x", "y"}, data);
            CompletionRegression cr = new CompletionRegression(df);

            System.out.println("\nAVANT regression :");
            df.afficherPremieresFignes(4);
            System.out.println("\n   -> Paires completes : (1,3) et (2,5)");
            System.out.println("   -> Regression : y = 2x + 1");
            System.out.println("   -> Predictions : x=3 -> y=7, x=4 -> y=9");

            int n = cr.completerParRegression(0, 1);
            tot++; 
            if (n == 2) { 
                ok++; 
                System.out.println("\n[OK] Test 1.1 : completerParRegression -> " + n + " case(s) completee(s)"); 
            } else { 
                System.out.println("\n[FAIL] Test 1.1 : completerParRegression -> " + n + " (attendu 2)"); 
            }

            System.out.println("\nAPRES regression :");
            df.afficherPremieresFignes(4);
            df.afficherStatistiques();

        } catch (Exception e) { 
            System.out.println("[FAIL] Test 1 : Exception - " + e.getMessage()); 
        }
        System.out.println();

        // TEST 2 : Aucun null dans la colonne cible
        System.out.println("--- TEST 2 : Aucune valeur null dans la colonne cible ------------------------");
        try {
            Object[][] data = {{1.0, 2.0}, {2.0, 4.0}, {3.0, 6.0}};
            DfIndividu df = new DfIndividu(3, new String[]{"x", "y"}, data);
            CompletionRegression cr = new CompletionRegression(df);

            System.out.println("\nDonnees (pas de null) :");
            df.afficherPremieresFignes(3);

            int n = cr.completerParRegression(0, 1);
            tot++; 
            if (n == 0) { 
                ok++; 
                System.out.println("\n[OK] Test 2 : completerParRegression -> " + n + " case(s) (rien a completer)"); 
            } else { 
                System.out.println("\n[FAIL] Test 2 : completerParRegression -> " + n + " (attendu 0)"); 
            }

        } catch (Exception e) { 
            System.out.println("[FAIL] Test 2 : Exception - " + e.getMessage()); 
        }
        System.out.println();

        // TEST 3 : Moins de 2 paires completes
        System.out.println("--- TEST 3 : Moins de 2 paires completes -------------------------------------");
        try {
            Object[][] data = {{1.0, null}, {null, 5.0}, {3.0, null}};
            DfIndividu df = new DfIndividu(3, new String[]{"x", "y"}, data);
            CompletionRegression cr = new CompletionRegression(df);

            System.out.println("\nDonnees (pas assez de paires x,y completes) :");
            df.afficherPremieresFignes(3);
            System.out.println("\n   -> Aucune paire complete !");

            int n = cr.completerParRegression(0, 1);
            tot++; 
            if (n == -1) { 
                ok++; 
                System.out.println("\n[OK] Test 3 : completerParRegression -> " + n + " (regression impossible)"); 
            } else { 
                System.out.println("\n[FAIL] Test 3 : completerParRegression -> " + n + " (attendu -1)"); 
            }

        } catch (Exception e) { 
            System.out.println("[FAIL] Test 3 : Exception - " + e.getMessage()); 
        }
        System.out.println();

        // TEST 4 : X constant (variance nulle)
        System.out.println("--- TEST 4 : X constant (variance nulle) -------------------------------------");
        try {
            Object[][] data = {{5.0, 3.0}, {5.0, 7.0}, {5.0, null}};
            DfIndividu df = new DfIndividu(3, new String[]{"x", "y"}, data);
            CompletionRegression cr = new CompletionRegression(df);

            System.out.println("\nDonnees (x constant = 5) :");
            df.afficherPremieresFignes(3);
            System.out.println("\n   -> Variance de X = 0 -> regression impossible");

            int n = cr.completerParRegression(0, 1);
            tot++; 
            if (n == -1) { 
                ok++; 
                System.out.println("\n[OK] Test 4 : completerParRegression -> " + n + " (variance nulle)"); 
            } else { 
                System.out.println("\n[FAIL] Test 4 : completerParRegression -> " + n + " (attendu -1)"); 
            }

        } catch (Exception e) { 
            System.out.println("[FAIL] Test 4 : Exception - " + e.getMessage()); 
        }
        System.out.println();

        // TEST 5 : completerToutParMeilleurPredicteur
        System.out.println("--- TEST 5 : completerToutParMeilleurPredicteur() -----------------------------");
        try {
            Object[][] data = {
                {1.0, 2.0, 5.0},
                {2.0, 4.0, 5.0},
                {3.0, null, 5.0},
                {4.0, 8.0, 5.0}
            };
            DfIndividu df = new DfIndividu(4, new String[]{"x", "y", "z"}, data);
            CompletionRegression cr = new CompletionRegression(df);

            System.out.println("\nAVANT regression :");
            df.afficherPremieresFignes(4);
            System.out.println("\n   -> Correlation x/y est parfaite, z est constant");
            System.out.println("   -> Le meilleur predicteur pour y devrait etre x");

            int n = cr.completerToutParMeilleurPredicteur();
            tot++; 
            if (n == 1) { 
                ok++; 
                System.out.println("\n[OK] Test 5.1 : completerToutParMeilleurPredicteur -> " + n + " case(s) completee(s)"); 
            } else { 
                System.out.println("\n[FAIL] Test 5.1 : completerToutParMeilleurPredicteur -> " + n + " (attendu 1)"); 
            }

            System.out.println("\nAPRES regression :");
            df.afficherPremieresFignes(4);
            df.afficherStatistiques();

        } catch (Exception e) { 
            System.out.println("[FAIL] Test 5 : Exception - " + e.getMessage()); 
        }
        System.out.println();

        // TEST 6 : Dataframe sans nulls
        System.out.println("--- TEST 6 : Dataframe sans valeurs null -------------------------------------");
        try {
            Object[][] data = {{1.0, 10.0}, {2.0, 20.0}, {3.0, 30.0}};
            DfIndividu df = new DfIndividu(3, new String[]{"a", "b"}, data);
            CompletionRegression cr = new CompletionRegression(df);

            System.out.println("\nDonnees (aucune valeur null) :");
            df.afficherPremieresFignes(3);

            int n = cr.completerToutParMeilleurPredicteur();
            tot++; 
            if (n == 0) { 
                ok++; 
                System.out.println("\n[OK] Test 6 : completerToutParMeilleurPredicteur -> " + n + " case(s) (rien a completer)"); 
            } else { 
                System.out.println("\n[FAIL] Test 6 : completerToutParMeilleurPredicteur -> " + n + " (attendu 0)"); 
            }

        } catch (Exception e) { 
            System.out.println("[FAIL] Test 6 : Exception - " + e.getMessage()); 
        }
        System.out.println();

        // TEST 7 : Colonne non numerique
        System.out.println("--- TEST 7 : Colonne non numerique (String) ----------------------------------");
        try {
            Object[][] data = {{"A", 1.0}, {"B", null}, {"C", 3.0}};
            DfIndividu df = new DfIndividu(3, new String[]{"categorie", "valeur"}, data);
            CompletionRegression cr = new CompletionRegression(df);

            System.out.println("\nDonnees (colonne 'categorie' est String) :");
            df.afficherPremieresFignes(3);
            System.out.println("\n   -> La colonne 'categorie' ne peut pas etre un predicteur");
            System.out.println("   -> La colonne 'valeur' n'a pas de predicteur valide");

            int n = cr.completerToutParMeilleurPredicteur();
            tot++; 
            if (n <= 0) { 
                ok++; 
                System.out.println("\n[OK] Test 7 : completerToutParMeilleurPredicteur -> " + n + " (aucune completion possible)"); 
            } else { 
                System.out.println("\n[FAIL] Test 7 : completerToutParMeilleurPredicteur -> " + n + " (attendu <=0)"); 
            }

        } catch (Exception e) { 
            System.out.println("[FAIL] Test 7 : Exception - " + e.getMessage()); 
        }
        System.out.println();

        // TEST 8 : Fichier reel mackerel.97442.csv
        System.out.println("--- TEST 8 : Fichier reel mackerel.97442.csv ---------------------------------");
        try {
            fish.acquisition.lecture.LectureCSV lecteur = new fish.acquisition.lecture.LectureCSV(";");
            DfIndividu dfMack = lecteur.lireCSV("data/mackerel.97442.csv", DfIndividu.class);

            if (dfMack != null) {
                System.out.println("\n[OK] Lecture reussie : " + dfMack.getNbLignes() + " lignes, " + dfMack.getNbCol() + " colonnes");

                System.out.println("\nAVANT regression (3 premieres lignes) :");
                dfMack.afficherPremieresFignes(3);

                CompletionRegression cr = new CompletionRegression(dfMack);
                int n = cr.completerToutParMeilleurPredicteur();
                tot++; ok++; 
                System.out.println("\n[OK] Test 8 : completerToutParMeilleurPredicteur -> " + n + " case(s) completee(s)");

                System.out.println("\nAPRES regression (3 premieres lignes) :");
                dfMack.afficherPremieresFignes(3);

                System.out.println("\nStatistiques APRES regression :");
                dfMack.afficherStatistiques();

            } else {
                System.out.println("[FAIL] Test 8 : Lecture du fichier echouee");
            }

        } catch (Exception e) { 
            System.out.println("[FAIL] Test 8 : Exception - " + e.getMessage()); 
        }
        System.out.println();

        // RESULTAT FINAL
        System.out.println("================================================================================");
        System.out.println("                    RESULTAT DES TESTS");
        System.out.println("================================================================================");
        System.out.println("  CompletionRegression : " + ok + "/" + tot + " tests reussis");
        System.out.println("================================================================================");
    }
}
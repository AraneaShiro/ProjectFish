package fish.completion;

import fish.acquisition.DataframeComplet;
import fish.exceptions.OutOfBoundException;
import java.util.*;

/**
 * Complète les valeurs manquantes (null) d'un dataframe
 * par la méthode des k plus proches voisins (KNN).
 *
 * Principe :
 *   Pour chaque cellule null de la colonne cible, on cherche les k lignes
 *   qui possèdent une valeur dans cette colonne et dont la distance
 *   euclidienne (sur les colonnes numériques communes non-null) est minimale.
 *   La valeur manquante est remplacée par la moyenne pondérée (poids = 1/d)
 *   des k voisins. Si la colonne est catégorielle, on prend la valeur majoritaire.
 *
 * Colonnes numériques uniquement pour la distance ; les colonnes texte
 * sont utilisées comme critère d'égalité (bonus de proximité).
 *
 * @author Jules Grenesche
 * @version 1.0
 */
public class CompletionKNN {

    // ── Couleurs ANSI ────────────────────────────────────────────────────────
    private static final String R     = "\u001B[0m";
    private static final String VER   = "\u001B[32m";
    private static final String ROU   = "\u001B[31m";
    private static final String JAU   = "\u001B[33m";
    private static final String CYA_G = "\u001B[1m\u001B[36m";
    private static final String VER_G = "\u001B[1m\u001B[32m";

    // ── Attributs ────────────────────────────────────────────────────────────

    /** Dataframe source */
    private final DataframeComplet df;

    /** Nombre de voisins à utiliser */
    private int k;

    /** Nombre total de valeurs complétées lors du dernier appel */
    private int nbCompletees;

    // ── Constructeur ─────────────────────────────────────────────────────────

    /**
     * @param df le dataframe à compléter (modifié en place)
     * @param k  le nombre de plus proches voisins (≥ 1)
     */
    public CompletionKNN(DataframeComplet df, int k) {
        this.df = df;
        this.k  = Math.max(1, k);
    }

    public CompletionKNN(DataframeComplet df) { this(df, 5); }

    // ── Getter / Setter ───────────────────────────────────────────────────────

    public int getK()            { return k; }
    public void setK(int k)     { this.k = Math.max(1, k); }
    public int getNbCompletees() { return nbCompletees; }

    // ── API publique ──────────────────────────────────────────────────────────

    /**
     * Complète toutes les colonnes numériques qui contiennent des null.
     *
     * @return le nombre total de valeurs imputées
     */
    public int completerTout() {
        nbCompletees = 0;
        for (int j = 0; j < df.getNbCol(); j++) {
            if (colonneEstNumerique(j) && compterNull(j) > 0) {
                nbCompletees += completerColonne(j);
            }
        }
        System.out.printf(VER_G + "✔ Complétion KNN (k=%d) terminée : %d valeur(s) imputée(s)." + R + "%n",
                k, nbCompletees);
        return nbCompletees;
    }

    /**
     * Complète une colonne spécifique.
     *
     * @param col index de la colonne à compléter
     * @return le nombre de valeurs imputées dans cette colonne
     */
    public int completerColonne(int col) {
        if (col < 0 || col >= df.getNbCol()) {
            System.out.println(ROU + "Index de colonne invalide : " + col + R);
            return 0;
        }
        boolean estNum = colonneEstNumerique(col);
        int count = 0;

        // Indices des colonnes numériques utilisables pour la distance
        List<Integer> colsNum = colonnesNumeriques();

        for (int i = 0; i < df.getNbLignes(); i++) {
            try {
                if (df.getCase(i, col) != null) continue; // déjà renseignée
            } catch (OutOfBoundException e) { continue; }

            // Trouver les k voisins qui ont une valeur dans col
            List<int[]> voisins = trouverVoisins(i, col, colsNum); // [index, distance*1000]

            if (voisins.isEmpty()) continue;

            Object valeur = estNum
                    ? moyennePonderee(voisins, col)
                    : valeurMajoritaire(voisins, col);

            if (valeur != null) {
                try { df.setCase(i, col, valeur); count++; }
                catch (OutOfBoundException e) { /* ignoré */ }
            }
        }

        System.out.printf(JAU + "  Colonne '%s' : %d null(s) complété(s)." + R + "%n",
                df.getNomCol(col), count);
        return count;
    }

    // ── Méthodes internes ─────────────────────────────────────────────────────

    /**
     * Trouve les k plus proches voisins de la ligne {@code cible}
     * qui possèdent une valeur dans la colonne {@code colCible}.
     *
     * @param cible   indice de la ligne à compléter
     * @param colCible colonne manquante
     * @param colsNum  colonnes numériques disponibles pour la distance
     * @return liste de paires {indice, distanceScalée} triée par distance croissante
     */
    private List<int[]> trouverVoisins(int cible, int colCible, List<Integer> colsNum) {
        // PriorityQueue max-heap : on garde les k plus proches
        PriorityQueue<double[]> pq = new PriorityQueue<>(
                (a, b) -> Double.compare(b[1], a[1])); // max en tête

        for (int i = 0; i < df.getNbLignes(); i++) {
            if (i == cible) continue;
            try {
                if (df.getCase(i, colCible) == null) continue; // n'a pas la valeur cherchée
            } catch (OutOfBoundException e) { continue; }

            double dist = distance(cible, i, colsNum);
            if (Double.isNaN(dist)) continue;

            pq.offer(new double[]{i, dist});
            if (pq.size() > k) pq.poll();
        }

        List<int[]> result = new ArrayList<>();
        for (double[] d : pq) result.add(new int[]{(int) d[0], (int)(d[1] * 1000 + 1)});
        return result;
    }

    /**
     * Distance euclidienne normalisée entre deux lignes sur les colonnes numériques.
     * Seules les colonnes où les deux lignes sont non-null sont utilisées.
     */
    private double distance(int i1, int i2, List<Integer> colsNum) {
        double somme = 0;
        int    dims  = 0;
        for (int j : colsNum) {
            try {
                Object v1 = df.getCase(i1, j);
                Object v2 = df.getCase(i2, j);
                if (v1 instanceof Number && v2 instanceof Number) {
                    double d = ((Number)v1).doubleValue() - ((Number)v2).doubleValue();
                    somme += d * d;
                    dims++;
                }
            } catch (OutOfBoundException e) { /* ignoré */ }
        }
        if (dims == 0) return Double.NaN;
        return Math.sqrt(somme / dims); // normalisé par le nb de dims partagées
    }

    /** Moyenne pondérée (poids = 1/distance) des valeurs des voisins. */
    private Double moyennePonderee(List<int[]> voisins, int col) {
        double sommeVal = 0, sommePoids = 0;
        for (int[] v : voisins) {
            try {
                Object val = df.getCase(v[0], col);
                if (val instanceof Number) {
                    double d = v[1] == 0 ? 1e-9 : v[1];
                    double poids = 1.0 / d;
                    sommeVal   += ((Number) val).doubleValue() * poids;
                    sommePoids += poids;
                }
            } catch (OutOfBoundException e) { /* ignoré */ }
        }
        return sommePoids == 0 ? null : sommeVal / sommePoids;
    }

    /** Valeur la plus fréquente parmi les voisins (colonnes catégorielles). */
    private Object valeurMajoritaire(List<int[]> voisins, int col) {
        Map<Object, Integer> freq = new LinkedHashMap<>();
        for (int[] v : voisins) {
            try {
                Object val = df.getCase(v[0], col);
                if (val != null) freq.merge(val, 1, Integer::sum);
            } catch (OutOfBoundException e) { /* ignoré */ }
        }
        return freq.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    // ── Utilitaires ───────────────────────────────────────────────────────────

    /** Fonction utilitaire pour detecté les colonnes numériques
     * @return le tableau des labels des colonnes numériques
     */
    private List<Integer> colonnesNumeriques() {
        List<Integer> res = new ArrayList<>();
        for (int j = 0; j < df.getNbCol(); j++)
            if (colonneEstNumerique(j)) res.add(j);
        return res;
    }

    /** Retourne si la colonne est numérique
     * @param col l'indice de la colonne
     * @return true si elle est numérique ou false sinon
     */
    private boolean colonneEstNumerique(int col) {
        for (int i = 0; i < df.getNbLignes(); i++) {
            try {
                Object v = df.getCase(i, col);
                if (v != null) return v instanceof Number;
            } catch (OutOfBoundException e) { /* ignoré */ }
        }
        return false;
    }

    /** Retourne le nombre de null dans la colonne
     * @param col l'indice de la colonne
     * @return le nombre de null
     */
    private int compterNull(int col) {
        int n = 0;
        for (int i = 0; i < df.getNbLignes(); i++) {
            try { if (df.getCase(i, col) == null) n++; }
            catch (OutOfBoundException e) { /* ignoré */ }
        }
        return n;
    }

    // ── Main — tests ──────────────────────────────────────────────────────────

    public static void main(String[] args) {
        int ok = 0, tot = 0;
        System.out.println(CYA_G + "=== Tests CompletionKNN ===" + R);

        try {
            // Données : col0=x  col1=y (valeurs à compléter)
            Object[][] data = {
                {1.0,  2.0}, {2.0,  4.0}, {3.0,  6.0},
                {4.0,  null}, {5.0, 10.0}, {6.0, null}
            };
            fish.acquisition.DfIndividu df = new fish.acquisition.DfIndividu(
                    6, new String[]{"x", "y"}, data);

            CompletionKNN knn = new CompletionKNN(df, 2);
            int n = knn.completerColonne(1);

            // Test 1 : 2 valeurs complétées
            tot++; if (n == 2) { ok++; System.out.println(VER + "PASS Test 1 : 2 valeurs complétées" + R); }
            else System.out.println(ROU + "FAIL Test 1 : " + n + R);

            // Test 2 : valeur row3 proche de 8 (voisins 3.0→6 et 5.0→10)
            tot++;
            Object v3 = df.getCase(3, 1);
            if (v3 instanceof Number && ((Number)v3).doubleValue() > 5 && ((Number)v3).doubleValue() < 12) {
                ok++; System.out.printf(VER + "PASS Test 2 : valeur complétée ≈ %.2f%n" + R, ((Number)v3).doubleValue());
            } else System.out.println(ROU + "FAIL Test 2 : " + v3 + R);

            // Test 3 : completerTout sur df avec plusieurs colonnes
            tot++;
            Object[][] data2 = {{1.0, null, 3.0}, {null, 2.0, 6.0}, {3.0, 4.0, null}};
            fish.acquisition.DfIndividu df2 = new fish.acquisition.DfIndividu(
                    3, new String[]{"a","b","c"}, data2);
            CompletionKNN knn2 = new CompletionKNN(df2, 2);
            int total = knn2.completerTout();
            if (total >= 1) { ok++; System.out.println(VER + "PASS Test 3 : completerTout = " + total + R); }
            else System.out.println(ROU + "FAIL Test 3" + R);

        } catch (Exception e) {
            System.out.println(ROU + "FAIL général : " + e + R);
            e.printStackTrace();
        }

        System.out.println("\n" + CYA_G + "=== CompletionKNN : " + ok + "/" + tot + " ===" + R);
    }
}
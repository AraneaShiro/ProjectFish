package fish.analyse;

import fish.acquisition.DataframeComplet;
import fish.exceptions.OutOfBoundException;
import java.util.*;

/**
 * Algorithme K-Médoïdes (PAM — Partition Around Medoids).
 *
 * Contrairement à K-Means dont les centroïdes sont des barycentres,
 * les médoïdes sont des points réels du dataset. L'algorithme est donc
 * plus robuste aux outliers et fonctionne avec n'importe quelle métrique.
 *
 * Étapes PAM :
 *   1. Initialisation : sélection de k médoïdes initiaux (stratégie greedy).
 *   2. Phase d'échange (SWAP) : pour chaque paire (médoïde, non-médoïde),
 *      on évalue si l'échange réduit le coût total.
 *      On applique le meilleur échange et on recommence jusqu'à convergence.
 *
 * @author Jules Grenesche
 * @version 1.0
 */
public class KMedoids {

    // ── Couleurs ANSI ────────────────────────────────────────────────────────
    private static final String R     = "\u001B[0m";
    private static final String G     = "\u001B[1m";
    private static final String CYA_G = "\u001B[1m\u001B[36m";
    private static final String JAU   = "\u001B[33m";
    private static final String VER   = "\u001B[32m";
    private static final String ROU   = "\u001B[31m";
    private static final String VER_G = "\u001B[1m\u001B[32m";

    /** Couleur pour les clusters*/
    private static final String[] CLUSTER_COLORS = {
        "\u001B[32m", "\u001B[33m", "\u001B[34m", "\u001B[35m",
        "\u001B[36m", "\u001B[31m", "\u001B[92m", "\u001B[93m"
    };

    // ── Attributs ────────────────────────────────────────────────────────────

    /** Le dataframe évalué */
    private final DataframeComplet df;
    /** Le nombre de label */
    private int k;
    /** Le nombre maximum d'itération */
    private int maxIter;

    /** Indices (dans le df) des médoïdes finaux */
    private int[]   medoids;
    /** Affectation de chaque ligne à un cluster */
    private int[]   labels;
    /** Coût total final (somme des distances au médoïde) */
    private double  cout;
    /** Colonnes numériques utilisées */
    private int[]   colsNum;
    /** Nombre de phases SWAP effectuées */
    private int     nbIter;

    // ── Constructeurs ─────────────────────────────────────────────────────────

    public KMedoids(DataframeComplet df, int k, int maxIter) {
        this.df      = df;
        this.k       = Math.max(2, k);
        this.maxIter = maxIter;
    }

    public KMedoids(DataframeComplet df, int k) { this(df, k, 100); }

    // ── Getters ──────────────────────────────────────────────────────────────

    public int   getK()                    { return k; }
    public void  setK(int k)              { this.k = Math.max(2, k); labels = null; }
    public int[] getMedoids()             { return medoids; }
    public int[] getLabels()              { return labels; }
    public double getCout()               { return cout; }
    public int   getNbIter()              { return nbIter; }

    public List<Integer> getLignesCluster(int c) {
        List<Integer> res = new ArrayList<>();
        if (labels == null) return res;
        for (int i = 0; i < labels.length; i++)
            if (labels[i] == c) res.add(i);
        return res;
    }

    /** Retourne l'indice (df) du médoïde du cluster c.
     * @param c le cluster
     * @return l'indice du medoide
     */
    public int getMedoidCluster(int c) {
        return (medoids != null && c < medoids.length) ? medoids[c] : -1;
    }

    // ── Exécution ─────────────────────────────────────────────────────────────

    /**
     * Lance l'algorithme PAM.
     * Doit être appelé avant {@link #afficher()}.
     */
    public void executer() {
        colsNum = detecterColonnesNumeriques();
        if (colsNum.length == 0) {
            System.out.println(ROU + "Aucune colonne numérique." + R);
            return;
        }

        int n = df.getNbLignes();
        if (n < k) {
            System.out.println(ROU + "Pas assez de lignes pour " + k + " clusters." + R);
            return;
        }

        // Pré-calcul de la matrice de distances (triangulaire supérieure aplatie) Transformé en tableau 1D
        double[] distMat = preCalculerDistances(n);

        System.out.printf(CYA_G + "K-Médoïdes PAM  k=%d  n=%d  colonnes numériques=%d%n" + R,
                k, n, colsNum.length);

        // ── Initialisation greedy ────────────────────────────────────────────
        medoids = initGreedy(n, distMat);
        labels  = new int[n];

        affecterClusters(n, distMat);
        cout = calculerCout(n, distMat);

        // ── Phase SWAP ───────────────────────────────────────────────────────
        for (nbIter = 0; nbIter < maxIter; nbIter++) {
            boolean ameliore = false;

            for (int m = 0; m < k; m++) {
                for (int h = 0; h < n; h++) {
                    if (estMedoid(h)) continue;

                    // Essai du swap medoids[m] ↔ h
                    int ancien = medoids[m];
                    medoids[m] = h;
                    affecterClusters(n, distMat);
                    double nvCout = calculerCout(n, distMat);

                    if (nvCout < cout - 1e-9) {
                        cout = nvCout;
                        ameliore = true;
                        // on garde le swap
                    } else {
                        // on annule
                        medoids[m] = ancien;
                        affecterClusters(n, distMat);
                    }
                }
            }
            if (!ameliore) break;
        }

        System.out.printf(VER_G + "✔ PAM convergé en %d phase(s). Coût = %.2f%n" + R,
                nbIter + 1, cout);
    }

    // ── Affichage ─────────────────────────────────────────────────────────────
    /**
     * Affiche un résumé coloré des clusters :
     */
    public void afficher() {
        if (labels == null) {
            System.out.println(ROU + "Exécutez d'abord executer()." + R);
            return;
        }
        System.out.println();
        System.out.println(CYA_G + "╔═══════════════════════════════════════════════════════╗" + R);
        System.out.printf (CYA_G + "║" + R + "  " + G + "K-Médoïdes PAM  k=%-2d  iter=%-3d  coût=%-10.2f" + R + CYA_G + "║" + R + "%n",
                k, nbIter + 1, cout);
        System.out.println(CYA_G + "╠═══════════════════════════════════════════════════════╣" + R);

        for (int c = 0; c < k; c++) {
            String col = CLUSTER_COLORS[c % CLUSTER_COLORS.length];
            int    med = medoids[c];
            List<Integer> membres = getLignesCluster(c);

            System.out.printf(CYA_G + "║" + R + " " + col + G + "Cluster %-2d" + R
                    + " │ " + JAU + "%4d membres" + R
                    + " │ médoïde=ligne " + VER + "%d" + R
                    + CYA_G + "║" + R + "%n", c, membres.size(), med);

            // Afficher les valeurs du médoïde
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < df.getNbCol(); j++) {
                try {
                    Object v = df.getCase(med, j);
                    sb.append(df.getNomCol(j)).append("=").append(col).append(v).append(R).append("  ");
                } catch (OutOfBoundException e) { /* ignoré */ }
            }
            System.out.println(CYA_G + "║" + R + "           Médoïde: " + sb);
        }
        System.out.println(CYA_G + "╚═══════════════════════════════════════════════════════╝" + R);
    }

    // ── Algorithme interne ────────────────────────────────────────────────────

    /**
     * Pré-calcule toutes les distances inter-lignes.
     * Stockage aplati triangulaire : dist(i,j) = distMat[i*n + j] (i < j).
     * @param n la dimension
     * @return La matrice supérieur aplatie
     */
    private double[] preCalculerDistances(int n) {
        double[] mat = new double[n * n];
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                double d = distEuclid(i, j);
                mat[i * n + j] = d;
                mat[j * n + i] = d;
            }
        }
        return mat;
    }

    /**
     * Calcule la distance euclidienne
     * @param i1 le points 1
     * @param i2 le points 2
     * @return la distance entre les 2 points
     */
    private double distEuclid(int i1, int i2) {
        double s = 0; int dims = 0;
        for (int j : colsNum) {
            try {
                Object v1 = df.getCase(i1, j), v2 = df.getCase(i2, j);
                if (v1 instanceof Number && v2 instanceof Number) {
                    double d = ((Number)v1).doubleValue() - ((Number)v2).doubleValue();
                    s += d * d; dims++;
                }
            } catch (OutOfBoundException e) { /* ignoré */ }
        }
        return dims == 0 ? Double.MAX_VALUE : Math.sqrt(s / dims);
    }

    /**
     * Retourne la distance en i et j
     * @param i le points 1
     * @param j le points 2
     *  @param mat la matrice de dimension 1
     *  @param n taille de la dimension de la matrice
     * @return la distance entre les 2 points
     */
    private double getDist(int i, int j, double[] mat, int n) { return mat[i * n + j]; }

    /** Initialisation greedy : le premier médoïde est le point minimisant la somme des distances. 
     * @param n la taille de la largeur de la matrice
     * @param distMat la matrice des distances
     * @return les médoides 
    */
    private int[] initGreedy(int n, double[] distMat) {
        int[] meds = new int[k];
        boolean[] used = new boolean[n];

        // Premier médoïde = médoïde global
        double bestSum = Double.MAX_VALUE;
        for (int i = 0; i < n; i++) {
            double s = 0;
            for (int j = 0; j < n; j++) s += getDist(i, j, distMat, n);
            if (s < bestSum) { bestSum = s; meds[0] = i; }
        }
        used[meds[0]] = true;

        // Médoïdes suivants : point qui réduit le plus le coût total
        for (int m = 1; m < k; m++) {
            double bestGain = -Double.MAX_VALUE;
            int    bestH    = 0;
            for (int h = 0; h < n; h++) {
                if (used[h]) continue;
                double gain = 0;
                for (int i = 0; i < n; i++) {
                    double curMin = Double.MAX_VALUE;
                    for (int j = 0; j < m; j++) curMin = Math.min(curMin, getDist(i, meds[j], distMat, n));
                    double newDist = getDist(i, h, distMat, n);
                    gain += Math.max(0, curMin - newDist);
                }
                if (gain > bestGain) { bestGain = gain; bestH = h; }
            }
            meds[m] = bestH;
            used[bestH] = true;
        }
        return meds;
    }

    /** Affectation des points au clusters 
     * @param n la taille de la largeur de la matrice
     * @param distMat la matrice des distances
     * 
    */
    private void affecterClusters(int n, double[] distMat) {
        for (int i = 0; i < n; i++) {
            int best = 0;
            double bestD = getDist(i, medoids[0], distMat, n);
            for (int c = 1; c < k; c++) {
                double d = getDist(i, medoids[c], distMat, n);
                if (d < bestD) { bestD = d; best = c; }
            }
            labels[i] = best;
        }
    }
    
    /** Calcule le cout total
     * @param n la taille de la largeur de la matrice
     * @param distMat la matrice des distances
     * @return le cout
    */
    private double calculerCout(int n, double[] distMat) {
        double s = 0;
        for (int i = 0; i < n; i++) s += getDist(i, medoids[labels[i]], distMat, n);
        return s;
    }

    /** Si le point h est médoide
     * @param h le point étudié
     * @return true si il est médoide sinon false
     * 
    */
    private boolean estMedoid(int h) {
        for (int m : medoids) if (m == h) return true;
        return false;
    }

    /** Fonction utilitaire pour detecté si la colonnes est numérique
     * @return le tableau des indices de colonnes numériques
     */
    private int[] detecterColonnesNumeriques() {
        List<Integer> res = new ArrayList<>();
        for (int j = 0; j < df.getNbCol(); j++) {
            for (int i = 0; i < df.getNbLignes(); i++) {
                try {
                    Object v = df.getCase(i, j);
                    if (v != null) { if (v instanceof Number) res.add(j); break; }
                } catch (OutOfBoundException e) { /* ignoré */ }
            }
        }
        return res.stream().mapToInt(Integer::intValue).toArray();
    }

    // ── Main — tests ──────────────────────────────────────────────────────────

    public static void main(String[] args) {
        int ok = 0, tot = 0;
        System.out.println(CYA_G + "=== Tests KMedoids ===" + R);
        try {
            Object[][] data = {
                {1.0,1.0},{1.5,1.2},{0.8,0.9},
                {10.0,10.0},{10.5,9.8},{9.5,10.3},
                {5.0,0.0},{5.1,-0.2},{4.9,0.1}
            };
            fish.acquisition.DfIndividu df = new fish.acquisition.DfIndividu(
                    9, new String[]{"x","y"}, data);

            KMedoids km = new KMedoids(df, 3);
            km.executer();

            tot++; if (km.getLabels() != null && km.getLabels().length == 9) {
                ok++; System.out.println(VER + "PASS Test 1 : 9 labels" + R);
            } else System.out.println(ROU + "FAIL Test 1" + R);

            tot++; if (km.getMedoids() != null && km.getMedoids().length == 3) {
                ok++; System.out.println(VER + "PASS Test 2 : 3 médoïdes" + R);
            } else System.out.println(ROU + "FAIL Test 2" + R);

            tot++; if (km.getCout() < 20.0) {
                ok++; System.out.printf(VER + "PASS Test 3 : coût=%.2f < 20%n" + R, km.getCout());
            } else System.out.println(ROU + "FAIL Test 3 : coût=" + km.getCout() + R);

            km.afficher();

        } catch (Exception e) {
            System.out.println(ROU + "FAIL général : " + e + R);
            e.printStackTrace();
        }
        System.out.println("\n" + CYA_G + "=== KMedoids : " + ok + "/" + tot + " ===" + R);
    }
}

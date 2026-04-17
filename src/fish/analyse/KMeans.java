package fish.analyse;

import fish.acquisition.DataframeComplet;
import fish.exceptions.OutOfBoundException;
import java.util.*;

/**
 * Algorithme K-Means (Lloyd) sur les colonnes numériques d'un dataframe.
 *
 * Fonctionnement :
 *   1. Initialisation des centroïdes par K-Means++ (meilleure convergence).
 *   2. Itération : affectation de chaque ligne au centroïde le plus proche,
 *      puis recalcul des centroïdes comme moyenne des membres du cluster.
 *   3. Arrêt lorsque les affectations ne changent plus ou après maxIter itérations.
 *
 * Seules les colonnes numériques sont utilisées pour la distance.
 * Les lignes contenant des null dans toutes les colonnes numériques sont ignorées.
 *
 * @author Jules Grenesche
 * @version 1.0
 */
public class KMeans {

    // ── Couleurs ANSI ────────────────────────────────────────────────────────
    private static final String R     = "\u001B[0m";
    private static final String G     = "\u001B[1m";
    private static final String CYA   = "\u001B[36m";
    private static final String JAU   = "\u001B[33m";
    private static final String VER   = "\u001B[32m";
    private static final String ROU   = "\u001B[31m";
    private static final String MAG   = "\u001B[35m";
    private static final String CYA_G = "\u001B[1m\u001B[36m";
    private static final String VER_G = "\u001B[1m\u001B[32m";

    /** Couleurs pour visualiser les clusters dans l'affichage */
    private static final String[] CLUSTER_COLORS = {
        "\u001B[32m", "\u001B[33m", "\u001B[34m", "\u001B[35m",
        "\u001B[36m", "\u001B[31m", "\u001B[92m", "\u001B[93m"
    };

    // ── Attributs ────────────────────────────────────────────────────────────

    /** Le dataframe pour les calcules*/
    private final DataframeComplet df;
    /** le nombre de centre */
    private int    k;
    /** Nombre maximum d'iteration voulue */
    private int    maxIter;
    /** Nombre d'itération faites*/
    private int    nbIter;           // itérations effectuées
    /** Somme des inerties */
    private double inertie;          // somme des distances² intra-cluster

    /** Affectation de chaque ligne à un cluster (−1 = ligne ignorée) */
    private int[]      labels;
    /** Centroïdes finaux [k][nbColNum] */
    private double[][] centroides;
    /** Indices des colonnes numériques utilisées */
    private int[]      colsNum;

    // ── Constructeurs ─────────────────────────────────────────────────────────

    /**
     * @param df     dataframe source (non modifié)
     * @param k      nombre de clusters (≥ 2)
     * @param maxIter nombre maximal d'itérations (défaut 300)
     */
    public KMeans(DataframeComplet df, int k, int maxIter) {
        this.df      = df;
        this.k       = Math.max(2, k);
        this.maxIter = maxIter;
    }

    /** Par defaut
     * @param df     dataframe source (non modifié)
     * @param k      nombre de clusters (≥ 2)
     * @param maxIter nombre maximal d'itérations (défaut 300)
     */
    public KMeans(DataframeComplet df, int k) { this(df, k, 300); }

    // ── Getters ──────────────────────────────────────────────────────────────

    public int    getK()         { return k; }
    public void   setK(int k)   { this.k = Math.max(2, k); labels = null; }
    public int[]  getLabels()    { return labels; }
    public int    getNbIter()    { return nbIter; }
    public double getInertie()   { return inertie; }
    public int[]  getColsNum()   { return colsNum; }

    /** Retourne les indices des lignes appartenant au cluster
     * @param c l'indice du cluster
     * @return Liste des indices des lignes appartenant au cluster */
    public List<Integer> getLignesCluster(int c) {
        List<Integer> res = new ArrayList<>();
        if (labels == null) return res;
        for (int i = 0; i < labels.length; i++)
            if (labels[i] == c) res.add(i);
        return res;
    }

    // ── Exécution ─────────────────────────────────────────────────────────────

    /**
     * Lance l'algorithme K-Means.
     * Doit être appelé avant tout appel à {@link #afficher()}.
     */
    public void executer() {
        colsNum = detecterColonnesNumeriques();
        if (colsNum.length == 0) {
            System.out.println(ROU + "Aucune colonne numérique détectée." + R);
            return;
        }

        double[][] points = extrairePoints();
        int n = points.length;
        if (n < k) {
            System.out.println(ROU + "Pas assez de lignes (" + n + ") pour " + k + " clusters." + R);
            return;
        }

        // Initialisation K-Means++
        centroides = initKMeansPP(points);
        labels     = new int[n];
        Arrays.fill(labels, -1);

        System.out.printf(CYA_G + "K-Means k=%d, %d points, %d colonnes numériques%n" + R,
                k, n, colsNum.length);

        for (nbIter = 0; nbIter < maxIter; nbIter++) {
            boolean changed = affecterClusters(points);
            recalculerCentroides(points);
            if (!changed) break;
        }

        inertie = calculerInertie(points);
        System.out.printf(VER_G + "✔ Convergence en %d itération(s). Inertie = %.2f%n" + R,
                nbIter + 1, inertie);
    }

    // ── Affichage ─────────────────────────────────────────────────────────────

    /**
     * Affiche un résumé coloré des clusters :
     * taille, centroïde, inertie intra-cluster.
     */
    public void afficher() {
        if (labels == null) {
            System.out.println(ROU + "Exécutez d'abord executer()." + R);
            return;
        }
        System.out.println();
        System.out.println(CYA_G + "╔════════════════════════════════════════════════════╗" + R);
        System.out.printf(CYA_G  + "║" + R + "  " + G + "K-Means  k=%-2d  iter=%-3d  inertie=%-10.2f" + R + CYA_G + "║" + R + "%n",
                k, nbIter + 1, inertie);
        System.out.println(CYA_G + "╠════════════════════════════════════════════════════╣" + R);

        for (int c = 0; c < k; c++) {
            String col = CLUSTER_COLORS[c % CLUSTER_COLORS.length];
            List<Integer> membres = getLignesCluster(c);
            System.out.printf(CYA_G + "║" + R + " " + col + G + "Cluster %-2d" + R
                    + " │ " + JAU + "%4d membres" + R + CYA_G + "║" + R + "%n",
                    c, membres.size());

            // Centroïde
            StringBuilder sb = new StringBuilder();
            for (int d = 0; d < colsNum.length; d++) {
                if (d > 0) sb.append("  ");
                sb.append(String.format(JAU + df.getNomCol(colsNum[d]) + R
                        + "=" + col + "%.2f" + R, centroides[c][d]));
            }
            System.out.printf(CYA_G + "║" + R + "           Centroïde: %s%n", sb);
        }
        System.out.println(CYA_G + "╚════════════════════════════════════════════════════╝" + R);
    }

    /**
     * Affiche les premières lignes de chaque cluster côte à côte.
     *
     * @param n nombre de lignes par cluster
     */
    public void afficherEchantillons(int n) {
        if (labels == null) { System.out.println(ROU + "Exécutez d'abord executer()." + R); return; }
        for (int c = 0; c < k; c++) {
            String col = CLUSTER_COLORS[c % CLUSTER_COLORS.length];
            System.out.println(col + G + "── Cluster " + c + " ──" + R);
            List<Integer> membres = getLignesCluster(c);
            int afficher = Math.min(n, membres.size());
            for (int i = 0; i < afficher; i++) {
                int lig = membres.get(i);
                StringBuilder sb = new StringBuilder("  [" + lig + "] ");
                for (int j = 0; j < df.getNbCol(); j++) {
                    try {
                        Object v = df.getCase(lig, j);
                        sb.append(df.getNomCol(j)).append("=").append(v).append("  ");
                    } catch (OutOfBoundException e) { /* ignoré */ }
                }
                System.out.println(sb);
            }
        }
    }

    // ── Algorithme interne ────────────────────────────────────────────────────

    /** K-Means++ : choisit des centroïdes initiaux bien espacés. 
     * @param pts les points
     * @return les points des centroides
    */
    private double[][] initKMeansPP(double[][] pts) {
        Random rng = new Random();
        double[][] centres = new double[k][];
        centres[0] = pts[rng.nextInt(pts.length)].clone(); //Clone est une copie

        for (int c = 1; c < k; c++) {
            double[] dists = new double[pts.length];
            double total   = 0;
            for (int i = 0; i < pts.length; i++) {
                double minD = Double.MAX_VALUE;
                for (int j = 0; j < c; j++) minD = Math.min(minD, dist2(pts[i], centres[j]));
                dists[i] = minD;
                total   += minD;
            }
            double seuil = rng.nextDouble() * total;
            double cumul = 0;
            int choix = pts.length - 1;
            for (int i = 0; i < pts.length; i++) {
                cumul += dists[i];
                if (cumul >= seuil) { choix = i; break; }
            }
            centres[c] = pts[choix].clone();
        }
        return centres;
    }

    /** Affecte chaque point au centroïde le plus proche. Retourne true si changement. 
     * @param pts les points
     * @return true si changement sinon false
    */
    private boolean affecterClusters(double[][] pts) {
        boolean changed = false;
        for (int i = 0; i < pts.length; i++) {
            int best = 0;
            double bestD = dist2(pts[i], centroides[0]);
            for (int c = 1; c < k; c++) {
                double d = dist2(pts[i], centroides[c]);
                if (d < bestD) { bestD = d; best = c; }
            }
            if (labels[i] != best) { labels[i] = best; changed = true; }
        }
        return changed;
    }

    /** Recalcule les centroïdes comme moyenne des membres. 
     * @param pts l'ensemble des points
    */
    private void recalculerCentroides(double[][] pts) {
        int dim = colsNum.length;
        double[][] sums   = new double[k][dim];
        int[]      counts = new int[k];
        for (int i = 0; i < pts.length; i++) {
            int c = labels[i];
            counts[c]++;
            for (int d = 0; d < dim; d++) sums[c][d] += pts[i][d];
        }
        for (int c = 0; c < k; c++) {
            if (counts[c] == 0) continue;
            for (int d = 0; d < dim; d++) centroides[c][d] = sums[c][d] / counts[c];
        }
    }

    /** Calcule l'inertie de entre chaque centre
     * @param pts les points dont on veut calculer
     * @return l'inertie
     */
    private double calculerInertie(double[][] pts) {
        double s = 0;
        for (int i = 0; i < pts.length; i++) s += dist2(pts[i], centroides[labels[i]]);
        return s;
    }

    /** Calcule la distance au carré entre les différents parametre de a et b
     * @param ab 2 points
     * @return la distance en double
     */
    private double dist2(double[] a, double[] b) {
        double s = 0;
        for (int d = 0; d < a.length; d++) { double diff = a[d] - b[d]; s += diff * diff; }
        return s;
    }

    // ── Utilitaires ───────────────────────────────────────────────────────────

    /** Extrait les points du dataframe
     * @return un tableau avec les lignes et leurs parametres numériques
     */
    private double[][] extrairePoints() {
        List<double[]> pts = new ArrayList<>();
        for (int i = 0; i < df.getNbLignes(); i++) {
            double[] row = new double[colsNum.length];
            boolean ok = true;
            for (int d = 0; d < colsNum.length; d++) {
                try {
                    Object v = df.getCase(i, colsNum[d]);
                    if (v instanceof Number) row[d] = ((Number) v).doubleValue();
                    else { ok = false; break; }
                } catch (OutOfBoundException e) { ok = false; break; }
            }
            if (ok) pts.add(row);
        }
        return pts.toArray(new double[0][]);
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

  
}
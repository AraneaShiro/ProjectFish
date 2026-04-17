package fish.analyse;

import fish.acquisition.DataframeComplet;
import fish.graphiques.LineChart;
import java.util.*;

/**
 * Méthode du coude (Elbow Method) pour déterminer le nombre optimal de clusters k.
 *
 * Principe :
 *   On exécute KMeans (ou KMedoids) pour k = kMin à kMax.
 *   On trace la courbe inertie/coût en fonction de k.
 *   Le "coude" — point où la décroissance ralentit nettement — indique le k optimal.
 *
 * Détection automatique du coude via la méthode de la dérivée seconde (maximum de courbure).
 *
 * @author Jules Grenesche
 * @version 1.0
 */
public class ElbowMethod {

    // ── ANSI Couleur ─────────────────────────────────────────────────────────────────
    private static final String R     = "\u001B[0m";
    private static final String G     = "\u001B[1m";
    private static final String CYA_G = "\u001B[1m\u001B[36m";
    private static final String JAU   = "\u001B[33m";
    private static final String VER   = "\u001B[32m";
    private static final String ROU   = "\u001B[31m";
    private static final String VER_G = "\u001B[1m\u001B[32m";
    private static final String JAU_G = "\u001B[1m\u001B[33m";

    // ── Paramètres ────────────────────────────────────────────────────────────
    /**
     * Dataframe sur lequel on fait les calcules
     */
    private final DataframeComplet df;
    /**
     * Kmininal
     */
    private int     kMin    = 2;
    /**
     * Kmaximal
     */
    private int     kMax    = 10;
    /**
     * Nombre de répétition
     */
    private int     repets  = 3;  // répétitions par k (prend le min) pour KMeans
    /**
     * Si on est dans un cas medoids
     */
    private boolean medoids = false;

    // ── Résultats ─────────────────────────────────────────────────────────────
    /**
     * Resultats des inerties
     */
    private double[] inertiesParK; // indexé [k - kMin]
    /**
     * K optimal trouvé
     */
    private int      kOptimal = -1;

    // ── Getter───────────────────────────────────────────────────────────────────

    public ElbowMethod(DataframeComplet df)                 { this.df = df; }
    public ElbowMethod kMin(int k)      { this.kMin    = k; return this; }
    public ElbowMethod kMax(int k)      { this.kMax    = k; return this; }
    public ElbowMethod repetitions(int r){ this.repets  = r; return this; }
    public ElbowMethod avecMedoids()    { this.medoids  = true; return this; }

    public int     getKOptimal()        { return kOptimal; }
    public double[] getInertiesParK()   { return inertiesParK; }

    // ── Methode ─────────────────────────────────────────────────────────────

    /**
     * Lance les calculs pour k = kMin à kMax et identifie le coude.
     *
     * @return this (chaînable)
     */
    public ElbowMethod executer() {
        int range = kMax - kMin + 1;
        inertiesParK = new double[range];

        String algo = medoids ? "KMedoids PAM" : "KMeans";
        System.out.println("\n" + CYA_G + "Méthode du coude — " + algo
                + " (k=" + kMin + " à " + kMax + ")" + R);

        for (int k = kMin; k <= kMax; k++) {
            double best = Double.MAX_VALUE;
            int runs = medoids ? 1 : repets; // PAM est déterministe
            for (int r = 0; r < runs; r++) {
                double cost;
                if (medoids) {
                    KMedoids km = new KMedoids(df, k);
                    km.executer();
                    cost = km.getCout();
                } else {
                    KMeans km = new KMeans(df, k);
                    km.executer();
                    cost = km.getInertie();
                }
                best = Math.min(best, cost);
            }
            inertiesParK[k - kMin] = best;
            System.out.printf("  k=%2d → " + JAU + "%.3f" + R + "%n", k, best);
        }

        kOptimal = detecterCoude();
        System.out.println(VER_G + "\n✔ Coude détecté à k=" + kOptimal + R);
        return this;
    }

    // ── Détection du coude ────────────────────────────────────────────────────

    /**
     * Détecte le coude par maximum de courbure (dérivée seconde).
     * Normalise les valeurs pour être indépendant de l'échelle.
     * 
     * @return le k du coude
     */
    private int detecterCoude() {
        int n = inertiesParK.length;
        if (n <= 2) return kMin;

        // Normaliser [0,1]
        double minV = Arrays.stream(inertiesParK).min().getAsDouble();
        double maxV = Arrays.stream(inertiesParK).max().getAsDouble();
        double range = maxV - minV;
        if (range == 0) return kMin;

        double[] norm = new double[n];
        for (int i = 0; i < n; i++) norm[i] = (inertiesParK[i] - minV) / range; //Normalise sur 0-1

        // Dérivée seconde numérique (différences finies centrées)
        double[] d2 = new double[n];
        for (int i = 1; i < n - 1; i++)
            d2[i] = norm[i-1] - 2*norm[i] + norm[i+1]; //yi′′​≈yi−1​−2yi​+yi+1

        // Le coude = maximum de la courbure (concavité maximale)
        int best = 1;
        for (int i = 1; i < n - 1; i++)
            if (d2[i] > d2[best]) best = i;

        return kMin + best;
    }

    // ── Affichage ─────────────────────────────────────────────────────────────

    /**
     * Affiche la courbe du coude avec LineChart.
     * Marque le k optimal d'un symbole spécial.
     */
    public void afficher() {
        if (inertiesParK == null) {
            System.out.println(ROU + "Exécutez d'abord executer()." + R); return;
        }

        int n = inertiesParK.length;
        double[] ks   = new double[n];
        double[] vals = inertiesParK.clone();
        for (int i = 0; i < n; i++) ks[i] = kMin + i;

        String algo = medoids ? "KMedoids" : "KMeans";

        new LineChart()
            .titre("Méthode du coude — " + algo)
            .xLabel("k (nombre de clusters)")
            .yLabel(medoids ? "Coût PAM" : "Inertie")
            .taille(55, 16)
            .serie(algo + " inertie", ks, vals, true)
            .afficher();

        // Affichage textuel complémentaire
        System.out.println("\n" + CYA_G + "── Tableau résumé ──" + R);
        System.out.printf("  %-5s %-12s %-12s %-10s%n", "k", "Inertie", "Δ inertie", "Δ² inertie");
        for (int i = 0; i < n; i++) {
            int k = kMin + i;
            double v  = inertiesParK[i];
            double d1 = i > 0 ? inertiesParK[i] - inertiesParK[i-1] : 0; //derivée premiere
            double d2 = (i > 0 && i < n-1)
                    ? inertiesParK[i-1] - 2*inertiesParK[i] + inertiesParK[i+1] : 0;    //Derivée seconde
            String marqueur = (k == kOptimal) ? VER_G + " ◄ COUDE" + R : "";
            System.out.printf("  " + (k == kOptimal ? VER_G : JAU) + "%-5d" + R
                    + "%-12.3f %-12.3f %-10.3f%s%n",
                    k, v, d1, d2, marqueur);
        }
        System.out.println(VER_G + "\n  Recommandation : k = " + kOptimal + R);
    }

    // ── Main ─────────────────────────────────────────────────────────────────

    public static void main(String[] args) throws Exception {
        // 4 nuages clairs → k optimal = 4
        Object[][] data = new Object[80][2];
        Random rng = new Random(42);
        int idx = 0;
        double[][] centres = {{1,1},{8,1},{1,8},{8,8}};
        for (double[] c : centres)
            for (int i = 0; i < 20; i++, idx++)
                data[idx] = new Object[]{c[0] + rng.nextGaussian()*0.5, c[1] + rng.nextGaussian()*0.5};

        fish.acquisition.DfIndividu df =
                new fish.acquisition.DfIndividu(80, new String[]{"x","y"}, data);

        System.out.println("=== KMeans ===");
        new ElbowMethod(df)
            .kMin(2).kMax(8).repetitions(3)
            .executer()
            .afficher();

        System.out.println("\n=== KMedoids ===");
        new ElbowMethod(df)
            .kMin(2).kMax(6).avecMedoids()
            .executer()
            .afficher();
    }
}

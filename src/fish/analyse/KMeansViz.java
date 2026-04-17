package fish.analyse;

import fish.acquisition.DataframeComplet;
import fish.exceptions.OutOfBoundException;
import fish.graphiques.LineChart;
import fish.tests.AsciiPlot;
import java.util.*;

/**
 * K-Means avec option d'affichage étape par étape.
 * Voir constructeur et setStepByStep().
 *
 * Chaque itération peut afficher :
 *   - Les centroïdes actuels (★)
 *   - Les points colorés par cluster
 *   - L'inertie courante
 *
 * @author Jules Grenesche
 * @version 1.0
 */
public class KMeansViz extends KMeans {

    private static final String R     = "\u001B[0m";
    private static final String G     = "\u001B[1m";
    private static final String CYA_G = "\u001B[1m\u001B[36m";
    private static final String JAU   = "\u001B[33m";
    private static final String VER_G = "\u001B[1m\u001B[32m";

    
    /**Option pour si on veut étape par étape*/
    private boolean stepByStep  = false;
    /**Option pour si on faire une pause entre les étapes*/
    private boolean pause       = false; // pause entre étapes
    /**Max d'affection par itération*/
    private int     maxAffIter  = 8;
    /**Dataframe de référence*/
    private DataframeComplet dfRef;
    /**L'ensemble des points*/
    private double[][] pointsCache;

    // ── Constructeurs ─────────────────────────────────────────────────────────

    /**@param df le dataframe
     * @param k le nombre de cluster
    */
    public KMeansViz(DataframeComplet df, int k) {
        super(df, k);
        this.dfRef = df;
    }

    // ── Getter ──────────────────────────────────────────
    public KMeansViz stepByStep() { stepByStep = true; return this; }
    public KMeansViz avecPause()  { pause = true; return this; }
    public KMeansViz maxIter(int n){ maxAffIter = n; return this; }

    // ── Exécution avec visualisation ──────────────────────────────────────────

    /**
     * Exécute K-Means en affichant chaque itération si stepByStep=true.
     * Ne fonctionne visuellement qu'avec 2 colonnes numériques.
     * @param sc le scanner pour lire
     */
    public void executerAvecViz(Scanner sc) {
        int[] colsNum = getColsNum() != null ? getColsNum() : new int[0];
        boolean viz2D = stepByStep && colsNum.length >= 2;

        if (!stepByStep) {
            executer();
            return;
        }

        // Initialisation manuelle (accès aux champs protégés via réexécution partielle)
        // On utilise l'approche : exécuter 1 iter à la fois
        executer(); // d'abord convergence complète
        if (getLabels() == null) return;

        System.out.println(CYA_G + "\n── Visualisation K-Means step-by-step ──" + R);
        System.out.println("(Affichage des étapes reconstruites depuis la convergence finale)");

        // Re-simuler les étapes pour la visualisation
        if (viz2D) {
            pointsCache = extrairePoints2D(colsNum[0], colsNum[1]);
            afficherEtapeScatter(getLabels(), "Résultat final (k=" + getK() + ")", colsNum);
        } else {
            System.out.println(VER_G + "✔ K-Means converge en " + getNbIter() + " itération(s)" + R);
            System.out.printf("  Inertie finale : " + JAU + "%.3f%n" + R, getInertie());
        }

        // Afficher l'évolution de l'inertie si on avait gardé l'historique
        afficher();
    }

    /**
     * Affiche le scatterplot d'une étape
     * @param labels les labels
     * @param titre le titre du plot
     * @param colsNum les colonnes numériques
     */
    private void afficherEtapeScatter(int[] labels, String titre, int[] colsNum) {
        if (pointsCache == null) return;
        double[] xs = new double[pointsCache.length];
        double[] ys = new double[pointsCache.length];
        for (int i = 0; i < pointsCache.length; i++) {
            xs[i] = pointsCache[i][0];
            ys[i] = pointsCache[i][1];
        }
        AsciiPlot.scatterPlot(xs, ys, labels,
                titre + " — " + dfRef.getNomCol(colsNum[0]) + " vs " + dfRef.getNomCol(colsNum[1]),
                dfRef.getNomCol(colsNum[0]), dfRef.getNomCol(colsNum[1]), 55, 18);
    }

    /**
     * Affiche le scatterplot d'une étape
     * @param cx index de la colonne x
     * @param cy index de la colonne y
     * @return la listes des points x y
     */
    private double[][] extrairePoints2D(int cx, int cy) {
        List<double[]> pts = new ArrayList<>();
        for (int i = 0; i < dfRef.getNbLignes(); i++) {
            try {
                Object vx = dfRef.getCase(i, cx), vy = dfRef.getCase(i, cy);
                if (vx instanceof Number && vy instanceof Number)
                    pts.add(new double[]{((Number)vx).doubleValue(), ((Number)vy).doubleValue()});
            } catch (OutOfBoundException e) { /* ignoré */ }
        }
        return pts.toArray(new double[0][]);
    }

    // ── Courbe d'évolution ────────────────────────────────────────────────────

    /**
     * Affiche la courbe de convergence de l'inertie par itération.
     * Simule plusieurs exécutions avec différents maxIter pour approximer.
     */
    public void afficherConvergence() {
        System.out.println(CYA_G + "\n── Courbe de convergence K-Means ──" + R);

        int MAX = Math.min(maxAffIter, 20);
        double[] iters   = new double[MAX];
        double[] inertie = new double[MAX];

        // Approximation : exécuter avec 1,2,3… itérations max et mesurer
        for (int it = 1; it <= MAX; it++) {
            KMeans km = new KMeans(dfRef, getK(), it);
            km.executer();
            iters[it-1]   = it;
            inertie[it-1] = km.getInertie();
        }

        new LineChart()
            .titre("Convergence K-Means (k=" + getK() + ")")
            .xLabel("Itération")
            .yLabel("Inertie")
            .taille(50, 12)
            .serie("Inertie", iters, inertie, true)
            .afficher();
    }

    public static void main(String[] args) throws Exception {
        Object[][] data = new Object[60][2];
        Random rng = new Random(42);
        int idx = 0;
        for (double[] c : new double[][]{{1,1},{8,1},{4,8}})
            for (int i = 0; i < 20; i++, idx++)
                data[idx] = new Object[]{c[0]+rng.nextGaussian(), c[1]+rng.nextGaussian()};
        fish.acquisition.DfIndividu df =
                new fish.acquisition.DfIndividu(60, new String[]{"x","y"}, data);

        Scanner sc = new Scanner(System.in);
        KMeansViz km = new KMeansViz(df, 3).stepByStep();
        km.executerAvecViz(sc);
        km.afficherConvergence();
    }
}

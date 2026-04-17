package fish.graphiques;

import fish.acquisition.DataframeComplet;
import fish.exceptions.OutOfBoundException;
import java.util.*;

/**
 * Histogramme de distribution ASCII pour une colonne numérique.
 *
 * Calcule automatiquement les tranches (bins) et affiche :
 *   - barres verticales colorées avec fréquence
 *   - axe X gradué avec valeurs des bornes
 *   - axe Y avec fréquences
 *   - courbe de densité approximée (optionnelle)
 *   - statistiques descriptives en en-tête
 *
 * @author Jules Grenesche
 * @version 1.0
 */
public class HistogrammeAscii {

    // ── ANSI ─────────────────────────────────────────────────────────────────
    private static final String R     = "\u001B[0m";
    private static final String G     = "\u001B[1m";
    private static final String CYA   = "\u001B[36m";
    private static final String JAU   = "\u001B[33m";
    private static final String VER   = "\u001B[32m";
    private static final String ROU   = "\u001B[31m";
    private static final String MAG   = "\u001B[35m";
    private static final String CYA_G = "\u001B[1m\u001B[36m";
    private static final String JAU_G = "\u001B[1m\u001B[33m";
    private static final String VER_G = "\u001B[1m\u001B[32m";

    // ── Paramètres ────────────────────────────────────────────────────────────
    /**
     * Les valeurs
     */
    private List<Double> valeurs   = new ArrayList<>();
    /**
     * Le titre
     */
    private String       titre     = "Histogramme";
    /**
     * Le label de x
     */
    private String       xLabel    = "Valeur";
    /**
     * Nombre de bin
     */
    private int          nbBins    = 10;
    /**
     * La hauteur maximum des barres 
     */
    private int          hauteur   = 16;  // hauteur maxi des barres (lignes)
    /**
     * Si on affiche la densité (non utilisé)
     */
    private boolean      afficherDensite = false;

    // ── Stats calculées ───────────────────────────────────────────────────────
    /**
     * Les statistiques calculées
     */
    private double min, max, mean, median, stddev;

    // ── Getter ───────────────────────────────────────────────────────────────────

    public HistogrammeAscii titre(String t)      { this.titre  = t;  return this; }
    public HistogrammeAscii xLabel(String l)     { this.xLabel = l;  return this; }
    public HistogrammeAscii bins(int n)          { this.nbBins = Math.max(2, n); return this; }
    public HistogrammeAscii hauteur(int h)       { this.hauteur = Math.max(4, h); return this; }
    public HistogrammeAscii avecDensite()        { this.afficherDensite = true; return this; }

    /** Charge les valeurs depuis une liste. */
    public HistogrammeAscii depuis(List<Double> vals) {
        this.valeurs = new ArrayList<>(vals);
        return this;
    }

    /** Charge depuis un tableau double[]. */
    public HistogrammeAscii depuis(double[] vals) {
        valeurs.clear();
        for (double v : vals) valeurs.add(v);
        return this;
    }

    /** Charge la colonne {@code col} du dataframe (valeurs numériques seulement). */
    public HistogrammeAscii depuis(DataframeComplet df, int col) {
        valeurs.clear();
        xLabel = df.getNomCol(col);
        for (int i = 0; i < df.getNbLignes(); i++) {
            try {
                Object v = df.getCase(i, col);
                if (v instanceof Number) valeurs.add(((Number) v).doubleValue());
            } catch (OutOfBoundException e) { /* ignoré */ }
        }
        return this;
    }

    // ── Affichage ─────────────────────────────────────────────────────────────

    /** Affiche l'histogramme dans le terminal. */
    public void afficher() {
        if (valeurs.isEmpty()) {
            System.out.println(ROU + "Aucune donnée." + R);
            return;
        }

        calculerStats();
        int[] freq = calculerFrequences();
        int   maxFreq = Arrays.stream(freq).max().getAsInt();
        double binW = (max - min) / nbBins;

        // ── Bandeau ───────────────────────────────────────────────────────────
        System.out.println("\n" + CYA_G + "╔═══════════════════════════════════════════════╗" + R);
        System.out.printf( CYA_G + "║" + R + " " + G + "%-45s" + R + CYA_G + "║" + R + "%n",
                trunc(titre, 45));
        System.out.printf( CYA_G + "║" + R + " n=%-5d  min=%-8.2f  max=%-8.2f  moy=%-8.2f " + CYA_G + "║" + R + "%n",
                valeurs.size(), min, max, mean);
        System.out.printf( CYA_G + "║" + R + " méd=%-7.2f  σ=%-8.2f  bins=%-3d               " + CYA_G + "║" + R + "%n",
                median, stddev, nbBins);
        System.out.println(CYA_G + "╚═══════════════════════════════════════════════╝" + R);
        System.out.println();

        // ── Grille verticale ─────────────────────────────────────────────────
        int barW = 5; // largeur d'une barre en chars
        int totalW = nbBins * barW;

        for (int row = hauteur; row >= 0; row--) {
            // Axe Y
            if (row == hauteur)
                System.out.printf(JAU + "%5d " + R, maxFreq);
            else if (row == hauteur / 2)
                System.out.printf(JAU + "%5d " + R, maxFreq / 2);
            else if (row == 0)
                System.out.printf(JAU + "%5d " + R, 0);
            else
                System.out.print("      ");

            System.out.print(CYA + "│" + R);

            for (int b = 0; b < nbBins; b++) {
                int   barHeight = maxFreq == 0 ? 0 : (int) Math.round((double) freq[b] / maxFreq * hauteur);
                // Couleur selon position : vert centre, jaune côtés
                double ratio = (double) b / nbBins;
                String col = ratio < 0.2 || ratio > 0.8 ? JAU : (ratio < 0.35 || ratio > 0.65 ? VER : VER_G);

                String cell = (row > 0 && row <= barHeight)
                        ? col + "█".repeat(barW) + R
                        : (row == 0 ? CYA + "─".repeat(barW) + R : " ".repeat(barW));
                System.out.print(cell);
            }
            System.out.println(row == 0 ? CYA + "►" + R : "");
        }

        // ── Axe X – ticks ─────────────────────────────────────────────────────
        System.out.print("      " + CYA + "└" + R);
        for (int b = 0; b <= nbBins; b++) {
            int pad = (b < nbBins) ? barW - 1 : 0;
            System.out.print(CYA + "┬" + "─".repeat(pad) + R);
        }
        System.out.println();

        // ── Axe X – labels ────────────────────────────────────────────────────
        System.out.print("       ");
        // Afficher labels tous les 2 ou 3 bins selon la place
        int step = Math.max(1, nbBins / 5);
        for (int b = 0; b <= nbBins; b += step) {
            double val = min + b * binW;
            String lbl = fmt(val);
            System.out.printf("%-" + (barW * step) + "s", lbl);
        }
        System.out.println();
        System.out.printf("       %" + (totalW / 2 + 4) + "s%n", xLabel);

        // ── Fréquences par bin ────────────────────────────────────────────────
        System.out.println("\n" + CYA_G + "── Fréquences ──" + R);
        for (int b = 0; b < nbBins; b++) {
            double lo = min + b * binW, hi = lo + binW;
            double pct = valeurs.size() == 0 ? 0 : freq[b] * 100.0 / valeurs.size();
            String col = freq[b] == maxFreq ? VER_G : JAU;
            System.out.printf("  [%6.2f – %6.2f[ : " + col + "%4d" + R
                    + " (" + JAU + "%5.1f%%" + R + ") %s%n",
                    lo, hi, freq[b], pct, "▮".repeat(Math.min(40, freq[b])));
        }
    }

    // ── Calculs ───────────────────────────────────────────────────────────────

    /**
     * Calcules les fréquences de chaque bin
     * @return le tableau des fréquences
     */
    private int[] calculerFrequences() {
        int[] freq = new int[nbBins];
        double range = max - min;
        if (range == 0) { freq[0] = valeurs.size(); return freq; }
        for (double v : valeurs) {
            int b = (int) ((v - min) / range * nbBins);
            b = Math.min(b, nbBins - 1);
            freq[b]++;
        }
        return freq;
    }

    /**
     * Calcules l'ensemble des statistiques
     * 
     */
    private void calculerStats() {
        List<Double> tri = new ArrayList<>(valeurs);
        Collections.sort(tri);
        min    = tri.get(0);
        max    = tri.get(tri.size() - 1);
        mean   = tri.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        int m  = tri.size() / 2;
        median = tri.size() % 2 == 0 ? (tri.get(m-1) + tri.get(m)) / 2 : tri.get(m);
        double ss = 0;
        for (double v : tri) ss += (v - mean) * (v - mean);
        stddev = Math.sqrt(ss / tri.size());
    }

    /**
     * Format un double en string sous la forme d'un chiffre apres la virgule
     * @param v le double a formaté
     * @return le string obtenue
     */
    private String fmt(double v) {
        long a = Math.round(v);
        return Math.abs(v - a) < 1e-6 ? String.valueOf(a) : String.format("%.1f", v);
    }

    /**
     * Fonction utilitaire pour tronquer un string
     * @param max le nombre max de char
     * @param s le string a tronquer
     * @return  le mot tronquer si besoins
     */
    private String trunc(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }

    // ── Main ─────────────────────────────────────────────────────────────────

    public static void main(String[] args) {
        Random rng = new Random(42);
        List<Double> vals = new ArrayList<>();
        // Distribution bimodale
        for (int i = 0; i < 60; i++) vals.add(rng.nextGaussian() * 2 + 5);
        for (int i = 0; i < 40; i++) vals.add(rng.nextGaussian() * 1.5 + 12);

        new HistogrammeAscii()
            .titre("Distribution bimodale — longueur (cm)")
            .xLabel("longueur (cm)")
            .bins(15)
            .hauteur(14)
            .depuis(vals)
            .afficher();
    }
}

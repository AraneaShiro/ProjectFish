package fish.graphiques;

import fish.acquisition.DataframeComplet;
import fish.exceptions.OutOfBoundException;
import java.util.*;

/**
 * Carte de chaleur (heatmap) ASCII pour visualiser :
 *   - La matrice de corrélation entre colonnes numériques
 *   - N'importe quelle matrice double[][] avec étiquettes
 *
 * Chaque cellule est colorée selon sa valeur :
 *   rouge fort → négatif élevé
 *   gris        → zéro / neutre
 *   vert fort   → positif élevé
 *
 * Valeur affichée à l'intérieur de chaque cellule.
 *
 * @author Jules Grenesche
 * @version 1.0
 */
public class HeatMap {

    // ── ANSI ─────────────────────────────────────────────────────────────────
    private static final String R     = "\u001B[0m";
    private static final String G     = "\u001B[1m";
    private static final String CYA_G = "\u001B[1m\u001B[36m";
    private static final String DIM   = "\u001B[2m";

    // Palette 5 niveaux : rouge fort → orange → gris → vert clair → vert fort
    // Backgrounds ANSI 256 couleurs (xterm)
    private static final String[] BG_NEG = {
        "\u001B[48;5;196m\u001B[97m",  // rouge vif
        "\u001B[48;5;203m\u001B[97m",  // rouge-orange
        "\u001B[48;5;210m\u001B[30m",  // saumon
        "\u001B[48;5;217m\u001B[30m",  // rose pâle
        "\u001B[48;5;224m\u001B[30m",  // rose très pâle
    };
    private static final String[] BG_POS = {
        "\u001B[48;5;194m\u001B[30m",  // vert très pâle
        "\u001B[48;5;157m\u001B[30m",  // vert pâle
        "\u001B[48;5;120m\u001B[30m",  // vert clair
        "\u001B[48;5;82m\u001B[30m",   // vert
        "\u001B[48;5;28m\u001B[97m",   // vert foncé
    };
    private static final String BG_ZERO = "\u001B[48;5;250m\u001B[30m"; // gris

    // ── Données ───────────────────────────────────────────────────────────────
    /**
     * Matrice des données
     */
    private double[][]   mat;
    /**
     * Label des lignes
     */
    private String[]     rowLabels;
    /**
     * Label des colonnes
     */
    private String[]     colLabels;
    /**
     * Titre du graphiques
     */
    private String       titre   = "HeatMap";
    /**
     * Legende
     */
    private String       legende = "valeur";
    /**
     * Si la matrice est symétrique
     */
    private boolean      symetrique = false; // masque triangle bas si true

    // ── Getter ───────────────────────────────────────────────────────────────────

    public HeatMap titre(String t)          { this.titre     = t;    return this; }
    public HeatMap legende(String l)        { this.legende   = l;    return this; }
    public HeatMap symetrique()             { this.symetrique= true; return this; }

    /** Charge une matrice avec étiquettes de lignes et colonnes. */
    public HeatMap depuis(double[][] m, String[] rowL, String[] colL) {
        mat = m; rowLabels = rowL; colLabels = colL;
        return this;
    }

    /**
     * Calcule et charge la matrice de corrélation de Pearson
     * entre toutes les colonnes numériques du dataframe.
     */
    public HeatMap depuisCorrelation(DataframeComplet df) {
        titre = "Matrice de corrélation";
        legende = "r de Pearson";
        symetrique = true;

        // Détection des colonnes numériques
        List<Integer> colsNum = new ArrayList<>();
        List<String>  noms    = new ArrayList<>();
        for (int j = 0; j < df.getNbCol(); j++) {
            for (int i = 0; i < df.getNbLignes(); i++) {
                try {
                    Object v = df.getCase(i, j);
                    if (v instanceof Number) { colsNum.add(j); noms.add(df.getNomCol(j)); break; }
                } catch (OutOfBoundException e) { /* ignoré */ }
            }
        }

        int n = colsNum.size();
        mat       = new double[n][n];
        rowLabels = noms.toArray(new String[0]);
        colLabels = noms.toArray(new String[0]);

        for (int i = 0; i < n; i++) {
            for (int j = i; j < n; j++) {
                try {
                    double r = (i == j) ? 1.0 : df.calculerCorrelation(colsNum.get(i), colsNum.get(j));
                    mat[i][j] = r;
                    mat[j][i] = r;
                } catch (OutOfBoundException e) { /* ignoré */ }
            }
        }
        return this;
    }

    /** Calcule la matrice de covariance entre colonnes numériques. */
    public HeatMap depuisCovariance(DataframeComplet df) {
        titre = "Matrice de covariance";
        legende = "covariance";
        symetrique = true;
        List<Integer> colsNum = new ArrayList<>();
        List<String>  noms    = new ArrayList<>();
        for (int j = 0; j < df.getNbCol(); j++) {
            for (int i = 0; i < df.getNbLignes(); i++) {
                try {
                    Object v = df.getCase(i, j);
                    if (v instanceof Number) { colsNum.add(j); noms.add(df.getNomCol(j)); break; }
                } catch (OutOfBoundException e) { /* ignoré */ }
            }
        }
        int n = colsNum.size();
        mat = new double[n][n];
        rowLabels = noms.toArray(new String[0]);
        colLabels = noms.toArray(new String[0]);
        for (int i = 0; i < n; i++) {
            for (int j = i; j < n; j++) {
                try {
                    double cov = df.calculerCoVariance(colsNum.get(i), colsNum.get(j));
                    mat[i][j] = mat[j][i] = cov;
                } catch (OutOfBoundException e) { /* ignoré */ }
            }
        }
        return this;
    }

    // ── Affichage ─────────────────────────────────────────────────────────────

    /**
     * Affiche la heatmap
     */
    public void afficher() {
        if (mat == null) { System.out.println("\u001B[31mAucune matrice.\u001B[0m"); return; }

        double absMax = 0;
        for (double[] row : mat)
            for (double v : row) absMax = Math.max(absMax, Math.abs(v));
        if (absMax == 0) absMax = 1;

        int nRow = mat.length, nCol = mat[0].length;
        int labW = Arrays.stream(rowLabels).mapToInt(String::length).max().orElse(8);
        labW = Math.min(labW, 14);
        int cellW = 7; // largeur d'une cellule

        // ── Titre ────────────────────────────────────────────────────────────
        System.out.println("\n" + CYA_G + "── " + titre + " ──" + R);

        // ── En-têtes colonnes ─────────────────────────────────────────────────
        System.out.print(" ".repeat(labW + 2));
        for (int j = 0; j < nCol; j++) {
            String lbl = trunc(colLabels[j], cellW - 1);
            System.out.printf(CYA_G + " %-" + (cellW) + "s" + R, lbl);
        }
        System.out.println();

        // ── Lignes ───────────────────────────────────────────────────────────
        for (int i = 0; i < nRow; i++) {
            // Label ligne
            System.out.printf(CYA_G + "%-" + labW + "s" + R + " ", trunc(rowLabels[i], labW));

            for (int j = 0; j < nCol; j++) {
                if (symetrique && j < i) {
                    System.out.print(" ".repeat(cellW + 1));
                    continue;
                }
                double v   = mat[i][j];
                String bg  = bgColor(v, absMax);
                String val = String.format("%.3f", v);
                System.out.print(bg + String.format(" %-" + cellW + "s", val) + R);
            }
            System.out.println();
        }

        // ── Légende ──────────────────────────────────────────────────────────
        System.out.println();
        System.out.print("  Palette " + legende + " : ");
        double[] steps = {-1.0, -0.5, 0.0, 0.5, 1.0};
        for (double s : steps) {
            double sv = s * absMax;
            System.out.print(bgColor(sv, absMax) + String.format(" %+.2f ", sv) + R + " ");
        }
        System.out.println();
    }

    // ── Couleur de fond ───────────────────────────────────────────────────────

    /**
     * Renvoie la couleur en fonction de l'intensité
     * @param v la valeur dont on veut la couleur
     * @param absMax la valeur max
     * @return le string de la  couleur
     */
    private String bgColor(double v, double absMax) {
        if (absMax == 0 || Math.abs(v) < absMax * 0.05) return BG_ZERO;
        double ratio = Math.abs(v) / absMax; // 0..1
        int idx = Math.min(4, (int)(ratio * 5));
        return v < 0 ? BG_NEG[idx] : BG_POS[idx];
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

    public static void main(String[] args) throws Exception {
        // Matrice de corrélation manuelle
        double[][] corr = {
            { 1.00,  0.82, -0.45,  0.30},
            { 0.82,  1.00, -0.60,  0.15},
            {-0.45, -0.60,  1.00, -0.72},
            { 0.30,  0.15, -0.72,  1.00}
        };
        String[] noms = {"Longueur","Masse","Nb.vers","Prévalence"};

        new HeatMap()
            .titre("Matrice de corrélation — poissons")
            .legende("r Pearson")
            .symetrique()
            .depuis(corr, noms, noms)
            .afficher();

        // Matrice quelconque
        double[][] mat2 = {
            {10, 5, 1},
            {3,  8, 4},
            {7,  2, 9}
        };
        String[] r2 = {"Zone A","Zone B","Zone C"};
        String[] c2 = {"2021","2022","2023"};
        new HeatMap()
            .titre("Effectifs par zone et année")
            .legende("effectif")
            .depuis(mat2, r2, c2)
            .afficher();
    }
}

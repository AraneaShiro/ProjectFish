package fish.tests;

import java.util.*;

/**
 * Utilitaire de visualisation ASCII pour les tests.
 *
 * Fournit :
 *   - Scatter plot 2D coloré par cluster
 *   - Histogramme horizontal
 *   - Tableau avant/après (pour la complétion KNN)
 *   - Barre de progression
 *   - Bandeau de section de test
 *
 * @author Jules Grenesche
 * @version 1.0
 */
public class AsciiPlot {

    // ── Couleurs ANSI ────────────────────────────────────────────────────────
    public static final String R     = "\u001B[0m";
    public static final String G     = "\u001B[1m";
    public static final String CYA   = "\u001B[36m";
    public static final String JAU   = "\u001B[33m";
    public static final String VER   = "\u001B[32m";
    public static final String ROU   = "\u001B[31m";
    public static final String MAG   = "\u001B[35m";
    public static final String BLU   = "\u001B[34m";
    public static final String CYA_G = "\u001B[1m\u001B[36m";
    public static final String VER_G = "\u001B[1m\u001B[32m";
    public static final String JAU_G = "\u001B[1m\u001B[33m";
    public static final String ROU_G = "\u001B[1m\u001B[31m";
    public static final String MAG_G = "\u001B[1m\u001B[35m";

    /** Caractères et couleurs par cluster (jusqu'à 8 clusters) */
    private static final String[] CLUSTER_COL = {
        VER, JAU, BLU, MAG, CYA, ROU,
        "\u001B[92m", "\u001B[93m"
    };
    private static final char[] CLUSTER_CHAR = {
        '●', '■', '▲', '◆', '★', '✚', '⬟', '⬡'
    };

    // ── Scatter plot ──────────────────────────────────────────────────────────

    /**
     * Affiche un scatter plot ASCII 2D avec des points colorés par cluster.
     *
     * @param xs     coordonnées X des points
     * @param ys     coordonnées Y des points
     * @param labels affectation de cluster de chaque point (-1 = non assigné)
     * @param titre  titre affiché en haut
     * @param xLabel nom de l'axe X
     * @param yLabel nom de l'axe Y
     * @param w      largeur de la grille (chars)
     * @param h      hauteur de la grille (chars)
     */
    public static void scatterPlot(double[] xs, double[] ys, int[] labels,
                                    String titre, String xLabel, String yLabel,
                                    int w, int h) {
        if (xs.length == 0) return;

        double xMin = Arrays.stream(xs).min().getAsDouble();
        double xMax = Arrays.stream(xs).max().getAsDouble();
        double yMin = Arrays.stream(ys).min().getAsDouble();
        double yMax = Arrays.stream(ys).max().getAsDouble();

        // Marge de 5 %
        double dx = (xMax - xMin) * 0.05 + 1e-9;
        double dy = (yMax - yMin) * 0.05 + 1e-9;
        xMin -= dx; xMax += dx; yMin -= dy; yMax += dy;

        // Grille [h][w]
        char[][]   grid   = new char[h][w];
        String[][] colors = new String[h][w];
        for (char[] row : grid) Arrays.fill(row, ' ');
        for (String[] row : colors) Arrays.fill(row, "");

        // Placer les points
        for (int i = 0; i < xs.length; i++) {
            int px = (int) Math.round((xs[i] - xMin) / (xMax - xMin) * (w - 1));
            int py = h - 1 - (int) Math.round((ys[i] - yMin) / (yMax - yMin) * (h - 1));
            px = Math.max(0, Math.min(w - 1, px));
            py = Math.max(0, Math.min(h - 1, py));

            int c = (labels != null && i < labels.length) ? labels[i] : 0;
            grid[py][px]   = CLUSTER_CHAR[Math.max(0, c) % CLUSTER_CHAR.length];
            colors[py][px] = CLUSTER_COL[Math.max(0, c) % CLUSTER_COL.length];
        }

        // ── Affichage ──────────────────────────────────────────────────────
        String bandeau = " " + titre + " ";
        System.out.println("\n" + CYA_G + "┌" + "─".repeat(bandeau.length()) + "┐" + R);
        System.out.println(CYA_G + "│" + R + G + bandeau + R + CYA_G + "│" + R);
        System.out.println(CYA_G + "└" + "─".repeat(bandeau.length()) + "┘" + R);

        for (int row = 0; row < h; row++) {
            // Axe Y : étiquette tous les ~5 lignes
            if (row == 0)
                System.out.printf(JAU + "%7.2f " + R, yMax);
            else if (row == h - 1)
                System.out.printf(JAU + "%7.2f " + R, yMin);
            else if (row == h / 2)
                System.out.printf(JAU + "%7.2f " + R, (yMin + yMax) / 2);
            else
                System.out.print("        ");

            // Bordure gauche
            System.out.print(CYA + "│" + R);

            for (int col = 0; col < w; col++) {
                String col2 = colors[row][col];
                char   ch   = grid[row][col];
                if (!col2.isEmpty()) System.out.print(G + col2 + ch + R);
                else                 System.out.print(ch);
            }

            System.out.println(CYA + "│" + R);
        }

        // Axe X
        System.out.print("        " + CYA + "└" + "─".repeat(w) + "┘" + R + "\n");
        System.out.printf("        " + JAU + "%-8.2f" + R + "%s"
                + JAU + "%8.2f" + R + "\n",
                xMin, " ".repeat(Math.max(0, w - 16)), xMax);
        System.out.printf("        %" + (w / 2 + 4) + "s%n", xLabel);
        System.out.printf("  %s%n", yLabel);

        // Légende des clusters
        if (labels != null) {
            int nClusters = Arrays.stream(labels).max().getAsInt() + 1;
            System.out.print("  Clusters : ");
            for (int c = 0; c < nClusters; c++) {
                String col = CLUSTER_COL[c % CLUSTER_COL.length];
                char   ch  = CLUSTER_CHAR[c % CLUSTER_CHAR.length];
                System.out.print(G + col + ch + " C" + c + R + "  ");
            }
            System.out.println();
        }
    }

    // ── Surcharge simple ──────────────────────────────────────────────────────

    /**
     * Surcharge simplifiée de {@link #scatterPlot} avec axes et dimensions par défaut.
     *
     * @param xs     coordonnées X des points
     * @param ys     coordonnées Y des points
     * @param labels affectation de cluster de chaque point
     * @param titre  titre affiché en haut
     */
    public static void scatterPlot(double[] xs, double[] ys, int[] labels, String titre) {
        scatterPlot(xs, ys, labels, titre, "X", "Y", 60, 20);
    }

    // ── Histogramme horizontal ────────────────────────────────────────────────

    /**
     * Affiche un histogramme horizontal (barres colorées).
     *
     * @param labels  noms des barres
     * @param valeurs valeurs correspondantes
     * @param titre   titre
     * @param barMax  largeur max d'une barre (chars)
     */
    public static void histogramme(String[] labels, double[] valeurs, String titre, int barMax) {
        double maxVal = Arrays.stream(valeurs).max().getAsDouble();
        if (maxVal == 0) maxVal = 1;

        System.out.println("\n" + CYA_G + G + "── " + titre + " ──" + R);
        for (int i = 0; i < labels.length; i++) {
            int barLen = (int) Math.round(valeurs[i] / maxVal * barMax);
            String col = CLUSTER_COL[i % CLUSTER_COL.length];
            System.out.printf("  %-18s " + CYA + "│" + R + " " + col + "%-" + barMax + "s" + R
                            + " " + JAU + "%.2f" + R + "%n",
                    labels[i],
                    "█".repeat(Math.max(0, barLen)),
                    valeurs[i]);
        }
    }

    // ── Tableau avant/après ───────────────────────────────────────────────────

    /**
     * Affiche deux tableaux côte à côte : avant (avec null) et après (complété).
     *
     * @param avant  tableau original (null autorisés)
     * @param apres  tableau complété
     * @param entetes noms des colonnes
     */
    public static void tableauAvantApres(Object[][] avant, Object[][] apres, String[] entetes) {
        System.out.println("\n" + CYA_G + "── Avant / Après complétion KNN ──" + R);

        // Calcul largeurs
        int[] larg = new int[entetes.length];
        for (int j = 0; j < entetes.length; j++) {
            larg[j] = entetes[j].length() + 2;
            for (Object[] row : avant) {
                int l = (row[j] == null ? 4 : row[j].toString().length()) + 2;
                if (l > larg[j]) larg[j] = l;
            }
        }

        String sep = CYA + "+" + R;
        for (int l : larg) sep += CYA + "-".repeat(l) + "+" + R;
        String sepDouble = CYA_G + "╔" + R;
        for (int l : larg) sepDouble += CYA_G + "═".repeat(l) + "╦" + R;

        // En-têtes
        System.out.println("  " + CYA_G + "AVANT" + R + "                    "
                + CYA_G + "APRÈS" + R);

        // Rangées
        for (int i = 0; i < avant.length; i++) {
            StringBuilder sbA = new StringBuilder(CYA + "│" + R);
            StringBuilder sbP = new StringBuilder(CYA + "│" + R);
            for (int j = 0; j < entetes.length; j++) {
                Object vA = avant[i][j], vP = apres[i][j];
                boolean etaitNull = vA == null;
                String txtA = etaitNull ? "null" : vA.toString();
                String txtP = (vP == null ? "null" : vP.toString());
                String colA = etaitNull ? ROU : (vA instanceof Number ? JAU : "");
                String colP = etaitNull ? VER_G : (vP instanceof Number ? JAU : ""); // vert si complété
                sbA.append(colA + centrer(txtA, larg[j]) + R + CYA + "│" + R);
                sbP.append(colP + centrer(txtP, larg[j]) + R + CYA + "│" + R);
            }
            System.out.println("  " + sbA + "   →   " + sbP);
        }
        System.out.println("  " + ROU + "null" + R + " = valeur manquante   "
                + VER_G + "■" + R + " = valeur imputée");
    }

    // ── Bandeau de section ────────────────────────────────────────────────────

    /**
     * Affiche un bandeau coloré pour séparer les sections de test.
     *
     * @param numero   numéro de la section
     * @param titre    titre de la section
     * @param couleur  code couleur ANSI
     */
    public static void bandeauSection(int numero, String titre, String couleur) {
        String t = String.format("  TEST %d — %s  ", numero, titre);
        System.out.println("\n" + couleur + G + "╔" + "═".repeat(t.length()) + "╗" + R);
        System.out.println(couleur + G + "║" + t + "║" + R);
        System.out.println(couleur + G + "╚" + "═".repeat(t.length()) + "╝" + R);
    }

    // ── Résultat de test ──────────────────────────────────────────────────────

    /**
     * Affiche un message de test réussi (✔ PASS) en vert gras.
     *
     * @param msg le message descriptif du test réussi
     */
    public static void pass(String msg) {
        System.out.println("  " + VER_G + "✔ PASS" + R + " " + msg);
    }

    /**
     * Affiche un message de test échoué (✘ FAIL) en rouge gras.
     *
     * @param msg le message descriptif du test échoué
     */
    public static void fail(String msg) {
        System.out.println("  " + ROU_G + "✘ FAIL" + R + " " + msg);
    }

    /**
     * Affiche un message d'information (ℹ) en cyan.
     *
     * @param msg le message informatif à afficher
     */
    public static void info(String msg) {
        System.out.println("  " + CYA + "ℹ " + R + msg);
    }

    // ── Résumé final ──────────────────────────────────────────────────────────

    /**
     * Affiche le tableau récapitulatif de tous les tests.
     *
     * @param sections  noms des sections
     * @param ok        nombre de tests réussis par section
     * @param total     nombre de tests total par section
     */
    public static void resumeGlobal(String[] sections, int[] ok, int[] total) {
        System.out.println("\n" + CYA_G + G + "╔══════════════════════════════════════════════════╗" + R);
        System.out.println(CYA_G + G + "║          RÉSUMÉ GLOBAL DES TESTS                 ║" + R);
        System.out.println(CYA_G + G + "╠══════════════════════════════════════════════════╣" + R);
        int totalOk = 0, totalTot = 0;
        for (int i = 0; i < sections.length; i++) {
            String col = ok[i] == total[i] ? VER_G : ROU_G;
            System.out.printf(CYA_G + "║" + R + " %-32s " + col + "%2d/%-2d" + R
                    + " %s" + CYA_G + "║" + R + "%n",
                    sections[i], ok[i], total[i],
                    ok[i] == total[i] ? VER + "✔" + R : ROU + "✘" + R + " ".repeat(1));
            totalOk  += ok[i];
            totalTot += total[i];
        }
        System.out.println(CYA_G + G + "╠══════════════════════════════════════════════════╣" + R);
        String colFin = totalOk == totalTot ? VER_G : ROU_G;
        System.out.printf(CYA_G + G + "║" + R + " %-32s " + colFin + "%2d/%-2d" + R
                + " %s" + CYA_G + G + "║" + R + "%n",
                "TOTAL", totalOk, totalTot,
                totalOk == totalTot ? VER + "🎉 Tous réussis !" + R : ROU + "⚠ Certains échoués" + R);
        System.out.println(CYA_G + G + "╚══════════════════════════════════════════════════╝" + R);
    }

    // ── Utilitaire interne ────────────────────────────────────────────────────

    /**
     * Centre une chaîne dans un champ de largeur {@code w}.
     * Tronque si la chaîne est trop longue, complète avec des espaces sinon.
     *
     * @param s la chaîne à centrer
     * @param w la largeur cible en caractères
     * @return la chaîne centrée dans un champ de {@code w} caractères
     */
    private static String centrer(String s, int w) {
        if (s.length() >= w) return s.substring(0, w);
        int g = (w - s.length()) / 2;
        return " ".repeat(g) + s + " ".repeat(w - s.length() - g);
    }
}

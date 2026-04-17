package fish.graphiques;

import java.util.*;

/**
 * Graphique de courbes ASCII avec superposition de plusieurs séries.
 *
 * Fonctionnalités :
 *   - Plusieurs séries superposées, chacune avec son symbole et sa couleur
 *   - Axe X et Y gradués avec échelles lisibles
 *   - Titre et étiquettes d'axes personnalisables
 *   - Légende automatique
 *   - Axes X personnalisables (valeurs numériques ou étiquettes texte)
 *   - Option de remplissage sous la courbe
 *   - Affichage des marqueurs sur chaque point
 *
 * @author Jules Grenesche
 * @version 1.0
 */
public class LineChart {

    // ── ANSI ─────────────────────────────────────────────────────────────────
    private static final String R     = "\u001B[0m";
    private static final String G     = "\u001B[1m";
    private static final String CYA   = "\u001B[36m";
    private static final String JAU   = "\u001B[33m";
    private static final String VER   = "\u001B[32m";
    private static final String DIM   = "\u001B[2m";
    private static final String CYA_G = "\u001B[1m\u001B[36m";
    private static final String JAU_G = "\u001B[1m\u001B[33m";

    private static final String[] COLORS = {
        "\u001B[32m", "\u001B[33m", "\u001B[34m", "\u001B[35m",
        "\u001B[36m", "\u001B[31m", "\u001B[92m", "\u001B[93m"
    };
    private static final char[] MARKERS = {'●','■','▲','◆','★','✚','◉','◈'};
    private static final char[] LINES   = {'─','═','─','─','─','─','─','─'};

    // ── Séries ────────────────────────────────────────────────────────────────

    private static class Serie {
        String   nom;
        double[] xs;
        double[] ys;
        boolean  remplir;
    }

    private final List<Serie>  series     = new ArrayList<>();
    private double[]           xLabelsNum = null;
    private String[]           xLabelsStr = null;

    // ── Options ───────────────────────────────────────────────────────────────
    /**
     * Titre du graphique
     */
    private String  titre  = "Courbes";
    /**
     * Label de l'axe x
     */
    private String  xLabel = "X";
    /**
     * Label de l'axe y
     */
    private String  yLabel = "Y";
    /**
     * Largeur de la grille
     */
    private int     W      = 70;   // largeur grille
    /**
     * Hauteur de la grille
     */
    private int     H      = 20;   // hauteur grille
    /**
     * Si on met les marqueurs
     */
    private boolean marqueurs = true;
    private Double  yMinForce = null, yMaxForce = null;

    // ── Getter ───────────────────────────────────────────────────────────────────

    public LineChart titre(String t)        { this.titre  = t;    return this; }
    public LineChart xLabel(String l)       { this.xLabel = l;    return this; }
    public LineChart yLabel(String l)       { this.yLabel = l;    return this; }
    public LineChart taille(int w, int h)   { W = w; H = h;       return this; }
    public LineChart sansMarqueurs()        { marqueurs = false;   return this; }
    public LineChart yMin(double v)         { yMinForce = v;       return this; }
    public LineChart yMax(double v)         { yMaxForce = v;       return this; }

    /** Étiquettes texte pour l'axe X (partagées par toutes les séries). */
    public LineChart xLabels(String... labs) { xLabelsStr = labs; return this; }

    /**
     * Ajoute une série.
     * @param nom  nom pour la légende
     * @param xs   valeurs X
     * @param ys   valeurs Y correspondantes
     */
    public LineChart serie(String nom, double[] xs, double[] ys) {
        return serie(nom, xs, ys, false);
    }

    public LineChart serie(String nom, double[] xs, double[] ys, boolean remplir) {
        Serie s = new Serie();
        s.nom = nom; s.xs = xs; s.ys = ys; s.remplir = remplir;
        series.add(s);
        return this;
    }

    /** Raccourci : axe X = indices 0,1,2,… */
    public LineChart serie(String nom, double[] ys) {
        double[] xs = new double[ys.length];
        for (int i = 0; i < ys.length; i++) xs[i] = i;
        return serie(nom, xs, ys);
    }

    // ── Affichage ─────────────────────────────────────────────────────────────

    /** Affiche le graphique */
    public void afficher() {
        if (series.isEmpty()) { System.out.println("\u001B[31mAucune donnée.\u001B[0m"); return; }

        // Bornes globales
        double xMin = series.stream().flatMapToDouble(s -> Arrays.stream(s.xs)).min().getAsDouble();
        double xMax = series.stream().flatMapToDouble(s -> Arrays.stream(s.xs)).max().getAsDouble();
        double yMin = yMinForce != null ? yMinForce :
                series.stream().flatMapToDouble(s -> Arrays.stream(s.ys)).min().getAsDouble();
        double yMax = yMaxForce != null ? yMaxForce :
                series.stream().flatMapToDouble(s -> Arrays.stream(s.ys)).max().getAsDouble();
        if (xMin == xMax) xMax = xMin + 1;
        double yPad = (yMax - yMin) * 0.05 + 1e-9;
        yMin -= yPad; yMax += yPad;
        if (yMin == yMax) yMax = yMin + 1;

        // Grille [H][W] char + couleur
        char[][]   grid   = new char[H][W];
        String[][] colors = new String[H][W];
        for (char[] row : grid) Arrays.fill(row, ' ');
        for (String[] row : colors) Arrays.fill(row, "");

        // Dessiner chaque série
        for (int si = 0; si < series.size(); si++) {
            Serie s = series.get(si);
            String col = COLORS[si % COLORS.length];
            char   mk  = MARKERS[si % MARKERS.length];
            char   ln  = LINES[si % LINES.length];

            // Interpoler des segments entre points consécutifs
            for (int pi = 0; pi < s.xs.length; pi++) {
                int px = toPixX(s.xs[pi], xMin, xMax);
                int py = toPixY(s.ys[pi], yMin, yMax);

                if (pi > 0) {
                    int px0 = toPixX(s.xs[pi-1], xMin, xMax);
                    int py0 = toPixY(s.ys[pi-1], yMin, yMax);
                    drawLine(grid, colors, px0, py0, px, py, ln, col);
                }

                // Marqueur
                if (marqueurs && py >= 0 && py < H && px >= 0 && px < W) {
                    grid[py][px]   = mk;
                    colors[py][px] = col + G;
                }

                // Remplissage sous la courbe
                if (s.remplir && py >= 0 && py < H && px >= 0 && px < W) {
                    int base = toPixY(Math.max(0, yMin), yMin, yMax);
                    for (int r = Math.min(py + 1, H - 1); r <= base; r++) {
                        if (grid[r][px] == ' ') { grid[r][px] = '░'; colors[r][px] = col; }
                    }
                }
            }
        }

        // ── Bandeau ───────────────────────────────────────────────────────────
        System.out.println("\n" + CYA_G + "╔══ " + titre + " ══╗" + R);

        // ── Grille ───────────────────────────────────────────────────────────
        for (int row = 0; row < H; row++) {
            double yVal = yMax - (double) row / (H - 1) * (yMax - yMin);
            if (row == 0 || row == H / 2 || row == H - 1)
                System.out.printf(JAU + "%8.2f " + R, yVal);
            else
                System.out.print("         ");

            System.out.print(CYA + "│" + R);
            for (int col = 0; col < W; col++) {
                String c = colors[row][col];
                System.out.print(c.isEmpty() ? " " : c + grid[row][col] + R);
            }
            System.out.println();
        }

        // Axe X bas
        System.out.print("         " + CYA + "└" + "─".repeat(W) + "►" + R);
        System.out.println();

        // Labels axe X
        int nTicks = 6;
        System.out.print("          ");
        for (int t = 0; t <= nTicks; t++) {
            double xVal = xMin + (double) t / nTicks * (xMax - xMin);
            String lbl;
            if (xLabelsStr != null) {
                int idx = (int) Math.round(xVal);
                lbl = (idx >= 0 && idx < xLabelsStr.length) ? xLabelsStr[idx] : "";
            } else {
                lbl = fmt(xVal);
            }
            int pos = (int) ((double) t / nTicks * W);
            System.out.printf("%-" + Math.max(1, W / nTicks) + "s", lbl);
        }
        System.out.println();
        System.out.printf("         %" + (W / 2 + 4) + "s  %s%n", xLabel, "");

        // ── Légende ───────────────────────────────────────────────────────────
        System.out.print("  " + yLabel + "  │  Séries : ");
        for (int si = 0; si < series.size(); si++) {
            String col = COLORS[si % COLORS.length];
            char   mk  = MARKERS[si % MARKERS.length];
            System.out.print(G + col + mk + " " + series.get(si).nom + R + "   ");
        }
        System.out.println();
    }

    // ── Dessin de ligne (Bresenham) ───────────────────────────────────────────

    /** Dessine une ligne
     * @param grid le tableau 2d des caracteres
     * @param colors les couleurs associé au case
     * @param x0,y0 le point de départ de la ligne
     * @param x1,y1 le point final
     * @param ch char a utilisé pour le dessin
     * @param col couleur du trait
    */
    private void drawLine(char[][] grid, String[][] colors,
                           int x0, int y0, int x1, int y1,
                           char ch, String col) {
        int dx = Math.abs(x1 - x0), dy = Math.abs(y1 - y0);
        int sx = x0 < x1 ? 1 : -1, sy = y0 < y1 ? 1 : -1;
        int err = dx - dy;
        while (true) {
            if (x0 >= 0 && x0 < W && y0 >= 0 && y0 < H) {
                if (grid[y0][x0] == ' ') { grid[y0][x0] = ch; colors[y0][x0] = col; }
            }
            if (x0 == x1 && y0 == y1) break;
            int e2 = 2 * err;
            if (e2 > -dy) { err -= dy; x0 += sx; }
            if (e2 < dx)  { err += dx; y0 += sy; }
        }
    }
    /**
     * Transforme une valeur en un position de la grille X
     * @param x la valeur
     * @param xMin Borne minimum de l espace
     * @param xMax Borne maximum de l espace
     * @return la position en X
     */
    private int toPixX(double x, double xMin, double xMax) {
        return (int) Math.round((x - xMin) / (xMax - xMin) * (W - 1));
    }

    /**
     * Transforme une valeur en un position de la grille y
     * @param y la valeur
     * @param yMin Borne minimum de l espace
     * @param yMax Borne maximum de l espace
     * @return la position en Y
     */
    private int toPixY(double y, double yMin, double yMax) {
        return H - 1 - (int) Math.round((y - yMin) / (yMax - yMin) * (H - 1));
    }

    /**
     * Format un double en string sous la forme d'un chiffre apres la virgule
     * @param v le double a formaté
     * @return le string obtenue
     */
    private String fmt(double v) {
        long a = Math.round(v);
        return Math.abs(v - a) < 0.05 ? String.valueOf(a) : String.format("%.1f", v);
    }

    // ── Main ─────────────────────────────────────────────────────────────────

    public static void main(String[] args) {
        // Séries temporelles de parasites
        double[] annees = {2017, 2018, 2019, 2020, 2021, 2022, 2023};
        double[] merlan = {12, 15, 13, 18, 22, 20, 25};
        double[] hareng = {8,  9,  11, 10, 14, 16, 13};
        double[] thon   = {30, 28, 35, 32, 40, 38, 42};

        new LineChart()
            .titre("Évolution de la prévalence parasitaire (2017-2023)")
            .xLabel("Année")
            .yLabel("%")
            .taille(60, 18)
            .serie("Merlan",  annees, merlan)
            .serie("Hareng",  annees, hareng)
            .serie("Thon",    annees, thon, true)
            .afficher();

        // Courbe fonction
        int N = 50;
        double[] xs = new double[N], sines = new double[N], cosines = new double[N];
        for (int i = 0; i < N; i++) {
            xs[i] = i * 2 * Math.PI / N;
            sines[i]   = Math.sin(xs[i]);
            cosines[i] = Math.cos(xs[i]);
        }
        new LineChart()
            .titre("sin(x) et cos(x)")
            .xLabel("x (rad)")
            .yLabel("y")
            .taille(60, 16)
            .serie("sin(x)", xs, sines)
            .serie("cos(x)", xs, cosines)
            .afficher();
    }
}

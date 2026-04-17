package fish.graphiques;

import java.util.*;

/**
 * Camembert ASCII (pie chart) affiché dans le terminal.
 *
 * Représentation : demi-cercle de caractères ANSI colorés + légende détaillée.
 * Chaque secteur est dessiné en projetant une grille polaire sur l'ASCII art.
 *
 * @author Jules Grenesche
 * @version 1.0
 */
public class PieChart {

    // ── ANSI ─────────────────────────────────────────────────────────────────
    private static final String R  = "\u001B[0m";
    private static final String G  = "\u001B[1m";
    private static final String CYA_G = "\u001B[1m\u001B[36m";
    private static final String JAU   = "\u001B[33m";
    private static final String VER   = "\u001B[32m";
    private static final String DIM   = "\u001B[2m";

    private static final String[] COLORS = {
        "\u001B[32m", "\u001B[33m", "\u001B[34m", "\u001B[35m",
        "\u001B[36m", "\u001B[31m", "\u001B[92m", "\u001B[93m",
        "\u001B[94m", "\u001B[95m", "\u001B[96m", "\u001B[91m"
    };
    /**
     * Tableau des textures
     */
    private static final char[] FILLS = {
        '█','▓','▒','░','◼','◆','●','■','▲','◉','◈','◇'
    };

    // ── Données ───────────────────────────────────────────────────────────────
    /**
     * Labels
     */
    private final List<String>  labels  = new ArrayList<>();
    /**
     * Les valeurs
     */
    private final List<Double>  valeurs = new ArrayList<>();
    /**
     * Titre du graphique
     */
    private String titre = "Répartition";

    // ── Getter ───────────────────────────────────────────────────────────────────

    public PieChart titre(String t)              { this.titre = t; return this; }
    public PieChart ajouter(String label, double v) { labels.add(label); valeurs.add(v); return this; }

    /** Charge depuis des tableaux parallèles. */
    public PieChart depuis(String[] labs, double[] vals) {
        for (int i = 0; i < labs.length; i++) ajouter(labs[i], vals[i]);
        return this;
    }

    // ── Affichage ─────────────────────────────────────────────────────────────

    /**
     * Affiche le camembert et sa légende dans le terminal.
     *
     * @param rayon  rayon du cercle ASCII (en lignes, ex: 10 → ≈20 lignes de haut)
     */
    public void afficher(int rayon) {
        if (valeurs.isEmpty()) { System.out.println(G+"\u001B[31m"+"Aucune donnée."+R); return; }

        double total = valeurs.stream().mapToDouble(Double::doubleValue).sum();
        if (total <= 0) { System.out.println(G+"\u001B[31m"+"Total nul."+R); return; }

        // Angles cumulés [0, 2π]
        double[] startAngles = new double[valeurs.size()];
        double[] endAngles   = new double[valeurs.size()];
        double cumul = 0;
        for (int i = 0; i < valeurs.size(); i++) {
            startAngles[i] = cumul;
            cumul += valeurs.get(i) / total * 2 * Math.PI;
            endAngles[i] = cumul;
        }

        int W = rayon * 4;  // largeur (les caractères sont ~2× plus étroits que hauts)
        int H = rayon * 2;

        char[][]   grid   = new char[H][W];
        String[][] colors = new String[H][W];
        for (char[] row : grid) Arrays.fill(row, ' ');
        for (String[] row : colors) Arrays.fill(row, R);

        double cx = W / 2.0, cy = H / 2.0;
        double rx = rayon * 1.9, ry = rayon * 0.95; // ellipse pour compenser le ratio

        for (int row = 0; row < H; row++) {
            for (int col = 0; col < W; col++) {
                double dx = (col - cx) / rx;
                double dy = (row - cy) / ry;
                if (dx * dx + dy * dy > 1.0) continue;

                double angle = Math.atan2(dy, dx);
                if (angle < 0) angle += 2 * Math.PI;

                for (int i = 0; i < valeurs.size(); i++) {
                    if (angle >= startAngles[i] && angle < endAngles[i]) {
                        grid[row][col]   = FILLS[i % FILLS.length];
                        colors[row][col] = COLORS[i % COLORS.length];
                        break;
                    }
                }
            }
        }

        // ── Bandeau titre ─────────────────────────────────────────────────────
        System.out.println("\n" + CYA_G + "── " + titre + " ──" + R);

        // ── Grille ───────────────────────────────────────────────────────────
        for (int row = 0; row < H; row++) {
            System.out.print("  ");
            for (int col = 0; col < W; col++) {
                String c = colors[row][col];
                System.out.print(c + grid[row][col] + R);
            }
            // Légende à droite des premières lignes
            int li = row - 1;
            if (li >= 0 && li < valeurs.size()) {
                String col = COLORS[li % COLORS.length];
                char   ch  = FILLS[li % FILLS.length];
                double pct = valeurs.get(li) / total * 100;
                System.out.printf("   " + col + G + "%c  %-20s" + R
                        + JAU + "%7.2f%%" + R + "  " + DIM + "(%.2f)" + R,
                        ch, trunc(labels.get(li), 20), pct, valeurs.get(li));
            }
            System.out.println();
        }

        // Légende des catégories restantes non affichées à droite
        for (int li = Math.max(0, H - 1); li < valeurs.size(); li++) {
            String col = COLORS[li % COLORS.length];
            char   ch  = FILLS[li % FILLS.length];
            double pct = valeurs.get(li) / total * 100;
            System.out.printf("  " + col + G + "%c  %-20s" + R
                    + JAU + "%7.2f%%" + R + "  " + DIM + "(%.2f)" + R + "%n",
                    ch, trunc(labels.get(li), 20), pct, valeurs.get(li));
        }

        // ── Pied ─────────────────────────────────────────────────────────────
        System.out.printf("  " + DIM + "Total : %.2f — %d catégorie(s)" + R + "%n",
                total, valeurs.size());
    }

    public void afficher() { afficher(10); }

    // ── Utilitaires ───────────────────────────────────────────────────────────

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
        new PieChart()
            .titre("Répartition des espèces")
            .ajouter("Merlan",     45)
            .ajouter("Hareng",     30)
            .ajouter("Thon",       15)
            .ajouter("Anchois",     7)
            .ajouter("Maquereau",   3)
            .afficher(8);

        System.out.println();

        new PieChart()
            .titre("Clusters KMeans")
            .ajouter("Cluster 0",  120)
            .ajouter("Cluster 1",   85)
            .ajouter("Cluster 2",   60)
            .afficher(7);
    }
}

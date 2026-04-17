package fish.graphiques;

import java.util.*;

/**
 * Graphique à barres ASCII — vertical ou horizontal, groupé ou empilé.
 *
 * Supports : - Barres simples ou groupées (plusieurs séries) - Barres empilées
 * (stacked) - Orientation verticale (par défaut) ou horizontale - Titre,
 * étiquettes d'axe, légende des séries - Valeurs affichées sur chaque barre
 *
 * @author Jules Grenesche
 * @version 1.0
 */
public class BarChart {

    // ── ANSI Couleur ─────────────────────────────────────────────────────────────────
    private static final String R = "\u001B[0m";
    private static final String G = "\u001B[1m";
    private static final String CYA = "\u001B[36m";
    private static final String JAU = "\u001B[33m";
    private static final String DIM = "\u001B[2m";
    private static final String CYA_G = "\u001B[1m\u001B[36m";
    private static final String JAU_G = "\u001B[1m\u001B[33m";

    //Couleur pour les graphiques
    /**
     * couleur pour les graphiques
     */
    private static final String[] COLORS = {
        "\u001B[32m", "\u001B[33m", "\u001B[34m", "\u001B[35m",
        "\u001B[36m", "\u001B[31m", "\u001B[92m", "\u001B[93m"
    };

    /**
     * Les différentes textures pour le graphiques
     */
    private static final char[] FILLS = {'█', '▓', '▒', '░', '▪', '▫', '▬', '▭'};

    // ── Données ───────────────────────────────────────────────────────────────
    /**
     * axe X
     */
    private final List<String> categories = new ArrayList<>();

    /**
     * Legende
     */
    private final List<String> seriesNoms = new ArrayList<>();

    /**
     * valeurs[cat]
     */
    private final List<double[]> series = new ArrayList<>();

    // ── Options ───────────────────────────────────────────────────────────────
    /**
     * Titre du graphique
     */
    private String titre = "BarChart";
    /**
     * label axe X
     */
    private String xLabel = "";
    /**
     * label axe y
     */
    private String yLabel = "";
    /**
     * Si le graphique est horizontal
     */
    private boolean horizontal = false;
    /**
     * Si on empile les bar
     */
    private boolean empile = false;
    /**
     * Si on affiche les valeurs 
     */
    private boolean afficherVal = true;
    /**
     * Hauteur de la grille
     */
    private int hauteur = 16;  // hauteur grille verticale
    /**
     * Largeur des barres
     */
    private int barLarg = 6;   // largeur barre (mode vertical)
    /**
     * Hauteur des barres
     */
    private int barHaut = 3;   // hauteur barre (mode horizontal)

    // ── Getter───────────────────────────────────────────────────────────────────
    public BarChart titre(String t) {
        this.titre = t;
        return this;
    }

    public BarChart xLabel(String l) {
        this.xLabel = l;
        return this;
    }

    public BarChart yLabel(String l) {
        this.yLabel = l;
        return this;
    }

    public BarChart horizontal() {
        this.horizontal = true;
        return this;
    }

    public BarChart empile() {
        this.empile = true;
        return this;
    }

    public BarChart sanValeurs() {
        this.afficherVal = false;
        return this;
    }

    public BarChart hauteur(int h) {
        this.hauteur = h;
        return this;
    }

    public BarChart largeurBarre(int w) {
        this.barLarg = w;
        return this;
    }

    /**
     * Ajoute une catégorie (axe X).
     */
    public BarChart categorie(String... cats) {
        categories.addAll(Arrays.asList(cats));
        return this;
    }

    /**
     * Ajoute une série de données.
     *
     * @param nom nom de la série (légende)
     * @param vals valeurs dans l'ordre des catégories
     */
    public BarChart serie(String nom, double... vals) {
        seriesNoms.add(nom);
        series.add(vals);
        return this;
    }

    // ── Affichage ─────────────────────────────────────────────────────────────
    /**
     *Affiche le graphique
     */
    public void afficher() {
        if (series.isEmpty() || categories.isEmpty()) {
            System.out.println(G + "\u001B[31m" + "Aucune donnée." + R);
            return;
        }
        // Bandeau
        System.out.println("\n" + CYA_G + "╔══ " + titre + " ══╗" + R);

        if (horizontal) {
            afficherHorizontal(); 
        }else {
            afficherVertical();
        }

        // Légende
        if (series.size() > 1) {
            System.out.print("  Séries : ");
            for (int s = 0; s < seriesNoms.size(); s++) {
                String col = COLORS[s % COLORS.length];
                char ch = FILLS[s % FILLS.length];
                System.out.print(col + ch + " " + seriesNoms.get(s) + R + "   ");
            }
            System.out.println();
        }
        if (!xLabel.isEmpty()) {
            System.out.printf("  %s%n", xLabel);
        }
    }

    // ── Vertical ──────────────────────────────────────────────────────────────
    /**
     * Affichage vertical
     */
    private void afficherVertical() {
        int nCat = categories.size();
        int nSer = series.size();

        // Calcul max
        double maxVal = 0;
        for (int c = 0; c < nCat; c++) {
            double sum = 0;
            double max = 0;

            for (double[] ser : series) {
                double v = (c < ser.length ? ser[c] : 0);
                sum += v;
                max = Math.max(max, v);
            }

            maxVal = Math.max(maxVal, empile ? sum : max);
        }

        if (maxVal == 0) {
            maxVal = 1;
        }

        int step = Math.max(1, hauteur / 4); // ⚠️ évite division par 0

        // Grille
        for (int row = hauteur; row >= 0; row--) {
            double rowVal = (double) row / hauteur * maxVal;

            // Axe Y
            if (row % step == 0) {
                System.out.printf(JAU + "%7.1f " + R, rowVal); 
            }else {
                System.out.print("        ");
            }

            System.out.print(CYA + "│" + R);

            for (int c = 0; c < nCat; c++) {
                System.out.print(" ");

                if (empile) {
                    double cumul = 0;
                    String drawn = " ".repeat(barLarg);

                    for (int s = 0; s < nSer; s++) {
                        double v = c < series.get(s).length ? series.get(s)[c] : 0;

                        double bottom = cumul / maxVal * hauteur;
                        double top = (cumul + v) / maxVal * hauteur;

                        if (row <= top && row > bottom) {
                            drawn = COLORS[s % COLORS.length]
                                    + String.valueOf(FILLS[s % FILLS.length]).repeat(barLarg)
                                    + R;
                            break;
                        }

                        cumul += v;
                    }

                    if (row == 0) {
                        System.out.print(CYA + "─".repeat(barLarg) + R); 
                    }else {
                        System.out.print(drawn);
                    }

                } else {
                    // Barres groupées
                    for (int s = 0; s < nSer; s++) {
                        double v = c < series.get(s).length ? series.get(s)[c] : 0;
                        int barH = (int) Math.round(v / maxVal * hauteur);

                        String col = COLORS[s % COLORS.length];
                        char ch = FILLS[s % FILLS.length];

                        if (row == 0) {
                            System.out.print(CYA + "──" + R); 
                        }else if (row <= barH) {
                            System.out.print(col + ("" + ch).repeat(2) + R); 
                        }else {
                            System.out.print("  ");
                        }
                    }
                }
            }
            System.out.println();
        }

        // Axe X
        System.out.print("        " + CYA + "└" + R);
        for (int c = 0; c < nCat; c++) {
            int w = empile ? barLarg + 1 : nSer * 2 + 1;
            System.out.print(CYA + "─".repeat(w) + R);
        }
        System.out.println(CYA + "►" + R);

        // Labels catégories
        System.out.print("         ");
        for (int c = 0; c < nCat; c++) {
            int w = empile ? barLarg + 1 : nSer * 2 + 1;
            System.out.printf("%-" + w + "s", trunc(categories.get(c), w));
        }
        System.out.println();
    }

    // ── Horizontal ───────────────────────────────────────────────────────────
    /**
     * Affichage horizontal
     */
    private void afficherHorizontal() {
        int nCat = categories.size();
        int nSer = series.size();
        int BAR_MAX = 50; // largeur max d'une barre

        double maxVal = 0;
        for (double[] ser : series) {
            for (double v : ser) {
                maxVal = Math.max(maxVal, v);
            }
        }
        if (maxVal == 0) {
            maxVal = 1;
        }

        int labW = categories.stream().mapToInt(String::length).max().orElse(10) + 1;

        for (int c = 0; c < nCat; c++) {
            // Label catégorie
            if (nSer == 1) {
                System.out.printf("  " + CYA_G + "%-" + labW + "s" + R + " " + CYA + "│" + R + " ", categories.get(c));
                double v = c < series.get(0).length ? series.get(0)[c] : 0;
                int len = (int) Math.round(v / maxVal * BAR_MAX);
                String col = COLORS[0];
                System.out.print(col + "█".repeat(len) + R);
                if (afficherVal) {
                    System.out.printf("  " + JAU + "%.2f" + R, v);
                }
                System.out.println();
            } else {
                System.out.printf("  " + CYA_G + "%-" + labW + "s" + R + "%n", categories.get(c));
                for (int s = 0; s < nSer; s++) {
                    double v = c < series.get(s).length ? series.get(s)[c] : 0;
                    int len = (int) Math.round(v / maxVal * BAR_MAX);
                    String col = COLORS[s % COLORS.length];
                    char ch = FILLS[s % FILLS.length];
                    System.out.printf("  %-" + labW + "s " + CYA + "│" + R + " " + col + "%s" + R,
                            "  " + seriesNoms.get(s), ("" + ch).repeat(len));
                    if (afficherVal) {
                        System.out.printf("  " + JAU + "%.2f" + R, v);
                    }
                    System.out.println();
                }
            }
        }
    }

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
        System.out.println("=== BarChart vertical groupé ===");
        new BarChart()
                .titre("Parasites par espèce et année")
                .categorie("Merlan", "Hareng", "Thon", "Anchois")
                .serie("2021", 12, 8, 25, 3)
                .serie("2022", 15, 11, 22, 6)
                .serie("2023", 10, 14, 28, 4)
                .hauteur(12)
                .afficher();

        System.out.println("\n=== BarChart horizontal ===");
        new BarChart()
                .titre("Effectifs par zone")
                .horizontal()
                .categorie("Nord", "Sud", "Est", "Ouest", "Centre")
                .serie("Effectifs", 120, 85, 60, 95, 40)
                .afficher();

        System.out.println("\n=== BarChart empilé ===");
        new BarChart()
                .titre("Composition parasitaire")
                .categorie("Merlan", "Hareng", "Thon")
                .serie("Anisakis", 40, 30, 55)
                .serie("Cestodes", 20, 15, 10)
                .serie("Trématodes", 10, 5, 15)
                .empile()
                .hauteur(12)
                .afficher();
    }
}

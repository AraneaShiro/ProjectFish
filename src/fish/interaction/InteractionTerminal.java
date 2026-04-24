package fish.interaction;

import fish.acquisition.*;
import fish.analyse.*;
import fish.completion.*;
import fish.exceptions.*;
import fish.graphiques.*;
import fish.nettoyage.*;
import fish.tests.AsciiPlot;
import java.util.*;

/**
 * Interface en ligne de commande (CLI) principale de l'application FISH.
 *
 * <p>Cette classe orchestre l'ensemble des fonctionnalités du projet via
 * un menu interactif en terminal. Elle délègue chaque opération aux classes
 * spécialisées du projet :</p>
 * <ul>
 *   <li>Chargement et gestion de fichiers CSV ({@link GestionCSV})</li>
 *   <li>Visualisation du dataframe actif ({@link fish.acquisition.DataframeComplet})</li>
 *   <li>Graphiques ASCII (PieChart, BarChart, LineChart, HeatMap, Histogramme)</li>
 *   <li>Nettoyage des outliers, valeurs négatives via boîte à moustaches ({@link BoiteAMoustache})</li>
 *   <li>Complétion des valeurs manquantes (moyenne, régression, KNN)</li>
 *   <li>Clustering (KMeans, KMedoids, méthode du coude)</li>
 *   <li>Manipulation du dataframe (fusion/suppression de colonnes, duplication)</li>
 * </ul>
 *
 * <p>Point d'entrée : {@code java fish.interaction.InteractionTerminal}</p>
 *
 * @author Jules Grenesche
 * @version 1.1
 * @see GestionCSV
 */
public class InteractionTerminal {

    // ── Codes couleur ANSI pour l'affichage terminal ───────────────────────
    private static final String R = "\u001B[0m", G = "\u001B[1m", CYA = "\u001B[36m", JAU = "\u001B[33m",
            VER = "\u001B[32m", ROU = "\u001B[31m", MAG = "\u001B[35m";
    private static final String CYA_G = "\u001B[1m\u001B[36m", VER_G = "\u001B[1m\u001B[32m",
            JAU_G = "\u001B[1m\u001B[33m", ROU_G = "\u001B[1m\u001B[31m";
    private static final String BLANC = "\u001B[1m\u001B[97m";

    /** Gestionnaire de fichiers CSV chargés en mémoire */
    private final GestionCSV gestion = new GestionCSV();

    /** Scanner partagé pour toutes les saisies utilisateur */
    private final Scanner sc = new Scanner(System.in);

    /**
     * Résultat du dernier clustering effectué (KMeans ou KMedoids).
     * Utilisé pour afficher les résultats ou exporter les labels.
     */
    private Object dernierClustering = null;

    /**
     * Point d'entrée principal — lance l'interface terminal.
     *
     * @param args arguments de la ligne de commande (ignorés)
     */
    public static void main(String[] args) {
        new InteractionTerminal().lancer();
    }

    /**
     * Démarre la boucle principale du menu interactif.
     * Affiche le menu principal et dispatch l'action choisie
     * jusqu'à ce que l'utilisateur choisisse de quitter.
     */
    public void lancer() {
        System.out.println("\n" + CYA_G
                + "╔═══════════════════════════════════════════════════╗\n║   FISH — Analyse parasitologique  v1.1              ║\n╚═══════════════════════════════════════════════════╝"
                + R);
        boolean run = true;
        while (run) {
            menuPrincipal();
            switch (lireChoix()) {
                case "1" -> menuGestionCSV();
                case "2" -> menuVisualiser();
                case "3" -> menuGraphiques();
                case "4" -> menuNettoyer();
                case "5" -> menuCompleter();
                case "6" -> menuClustering();
                case "7" -> menuManipulation();
                case "8", "q", "Q" -> {
                    run = false;
                    ok("Au revoir !");
                }
                default -> err("Choix invalide.");
            }
        }
        sc.close();
    }

    /**
     * Affiche le menu principal avec l'état du dataframe actif (nom, dimensions).
     * Chaque option est numérotée et colorée pour faciliter la lecture.
     */
    private void menuPrincipal() {
        System.out.println();
        System.out.println(CYA_G + "╔══════════════════════════════════════════╗" + R);
        GestionCSV.EntreeCSV ea = gestion.getEntreeActive();
        if (ea != null)
            System.out.println(CYA_G + "║" + R + " " + VER + "Actif:" + JAU + trunc(ea.nom, 18) + R + " ("
                    + ea.df.getNbLignes() + "×" + ea.df.getNbCol() + ")        " + CYA_G + "                   ║" + R);
        else
            System.out.println(
                    CYA_G + "║" + R + " " + ROU + "Aucun dataframe chargé.                   " + R + CYA_G + "║" + R);
        System.out.println(CYA_G + "╠══════════════════════════════════════════╣" + R);
        opt("1", "Gestion des CSV (charger/sélectionner)");
        opt("2", "Visualiser le dataframe actif");
        opt("3", "Graphiques (pie/bar/courbe/heatmap/histo)");
        opt("4", "Nettoyage (boîte à moustaches / valeurs négatives)");
        opt("5", "Complétion");
        opt("6", "Clustering (KMeans/KMedoids/Coude/Viz)");
        opt("7", "Manipulation (fusion/suppression/copie)");
        opt("8", "Quitter");
        System.out.println(CYA_G + "╚══════════════════════════════════════════╝" + R);
        System.out.print(JAU_G + "► " + R);
    }

    // ── 1. Gestion des CSV ───────────────────────────────────────────────────

    /**
     * Sous-menu de gestion des fichiers CSV.
     * Permet de charger, sélectionner ou supprimer des dataframes.
     */
    private void menuGestionCSV() {
        boolean run = true;
        while (run) {
            titre("GESTION DES CSV");
            gestion.afficherListe();
            System.out.println();
            opt("1", "Charger un nouveau CSV");
            opt("2", "Sélectionner le CSV actif");
            opt("3", "Supprimer");
            opt("0", "Retour");
            System.out.print(JAU_G + "► " + R);
            switch (lireChoix()) {
                case "1" -> chargerCSV();
                case "2" -> gestion.menuSelectionner(sc);
                case "3" -> {
                    gestion.afficherListe();
                    System.out.print("ID: ");
                    if (gestion.supprimer(lireInt(-1)))
                        ok("Supprimé");
                    else
                        err("Introuvable.");
                }
                case "0" -> run = false;
                default -> err("Invalide.");
            }
        }
    }

    /**
     * Dialogue interactif de chargement d'un fichier CSV.
     * <p>Étapes :</p>
     * <ol>
     *   <li>Saisie du chemin du fichier</li>
     *   <li>Détection automatique du BOM / charset</li>
     *   <li>Sélection du charset et du délimiteur</li>
     *   <li>Choix du type (DfIndividu ou DfPopulation)</li>
     *   <li>Attribution d'un nom affiché</li>
     * </ol>
     */
    private void chargerCSV() {
        System.out.print("Chemin CSV : ");
        String ch = sc.nextLine().trim();

        // ── Détection automatique du charset ──────────────────────────────────
        String charsetDetecte = gestion.detecterBOM(ch, gestion.suggererCharset(ch));
        System.out.println(CYA_G + "── Encodage détecté automatiquement : " + R + JAU + charsetDetecte + R);

        // ── Sélection du charset ──────────────────────────────────────────────
        titre("CHARSET (encodage du fichier)");
        System.out.println("  " + CYA_G + "Suggestion automatique : " + JAU + charsetDetecte + R + "\n");
        String[][] charsets = GestionCSV.CHARSETS;
        for (int i = 0; i < charsets.length; i++) {
            String label = charsets[i][0];
            // Marquer la suggestion
            boolean isSuggestion = charsets[i][1] != null && charsets[i][1].equalsIgnoreCase(charsetDetecte);
            System.out.printf("  %s[%d]%s %s%s%n",
                    isSuggestion ? VER_G : VER_G, i + 1, R,
                    label,
                    isSuggestion ? " " + JAU + "← suggestion auto" + R : "");
        }
        System.out.print(JAU_G + "► " + R + "(Entrée = suggestion auto '" + charsetDetecte + "') : ");
        String choixCs = lireChoix();
        String charset = charsetDetecte; // défaut = suggestion
        if (!choixCs.isEmpty()) {
            try {
                int idx = Integer.parseInt(choixCs) - 1;
                if (idx >= 0 && idx < charsets.length) {
                    if (charsets[idx][1] == null) {
                        System.out.print("Entrez le charset manuellement (ex: windows-1252) : ");
                        charset = sc.nextLine().trim();
                        if (charset.isEmpty())
                            charset = charsetDetecte;
                    } else {
                        charset = charsets[idx][1];
                    }
                } else {
                    err("Numéro invalide, charset auto utilisé.");
                }
            } catch (NumberFormatException e) {
                err("Entrée invalide, charset auto utilisé.");
            }
        }
        System.out.println(VER + "Charset retenu : " + charset + R);

        // ── Délimiteur ────────────────────────────────────────────────────────
        System.out.print("Délimiteur (défaut=',', entrez ';' si besoin) : ");
        String d = sc.nextLine().trim();
        if (d.isEmpty())
            d = ",";
        if(d.equals("\\t"))
            d="\t";

        // ── Type ──────────────────────────────────────────────────────────────
        System.out.println("\nType de données :");
        opt("1", "Individu  (DfIndividu)");
        opt("2", "Population (DfPopulation)");
        System.out.print(JAU_G + "► " + R);
        GestionCSV.TypeCSV t = lireChoix().equals("2") ? GestionCSV.TypeCSV.POPULATION : GestionCSV.TypeCSV.INDIVIDU;

        System.out.print("Nom affiché (Entrée = nom du fichier) : ");
        String n = sc.nextLine().trim();

        gestion.charger(ch, d, charset, t, n.isEmpty() ? null : n);
    }

    // ── 2. Visualisation ─────────────────────────────────────────────────────

    /**
     * Sous-menu de visualisation du dataframe actif.
     * Propose l'affichage paginé, les statistiques globales et l'analyse par colonne.
     */
    private void menuVisualiser() {
        if (!verDf())
            return;
        DataframeComplet df = gestion.getActif();
        boolean run = true;
        while (run) {
            titre("VISUALISER — " + gestion.getEntreeActive().nom);
            opt("1", "Premières lignes (N)");
            opt("2", "Pagination interactive");
            opt("3", "Statistiques globales");
            opt("4", "Infos colonnes");
            opt("5", "Statistiques d'une colonne");
            opt("0", "Retour");
            System.out.print(JAU_G + "► " + R);
            switch (lireChoix()) {
                case "1" -> {
                    System.out.print("N= ");
                    df.afficherPremieresFignes(lireInt(5));
                }
                case "2" -> df.afficherAvecPagination(sc);
                case "3" -> df.afficherStatistiques();
                case "4" -> infoCols(df);
                case "5" -> statsColonne(df);
                case "0" -> run = false;
                default -> err("Invalide.");
            }
        }
    }

    /**
     * Affiche toutes les statistiques disponibles pour une seule colonne :
     * – Infos générales (type, nulls, valeurs uniques)
     * – Stats numériques (min, max, moy, méd, écart-type, variance) si applicable
     * – Histogramme ASCII inline
     * – Top-10 des valeurs les plus fréquentes
     */
    private void statsColonne(DataframeComplet df) {
        infoCols(df);
        System.out.print("Index de la colonne : ");
        int c = lireInt(-1);
        if (!validCol(df, c)) {
            err("Index invalide.");
            return;
        }

        String nom = df.getNomCol(c);
        boolean estNum = cNum(df, c);
        int nbNull = cNull(df, c);
        int nbTotal = df.getNbLignes();

        // ── Cadre principal ────────────────────────────────────────────────
        String inner = "═".repeat(74);
        System.out.println("\n" + CYA_G + "╔" + inner + "╗" + R);
        System.out.printf(CYA_G + "║" + R + " " + G + "%-73s" + R + CYA_G + "║" + R + "%n",
                "  Statistiques — colonne [" + c + "] '" + nom + "'");
        System.out.println(CYA_G + "╠" + inner + "╣" + R);

        // ── Infos générales ───────────────────────────────────────────────
        String typeStr = estNum ? VER + "NUMÉRIQUE" + R : MAG + "TEXTE / MIXTE" + R;
        System.out.printf(CYA_G + "║" + R + "  Type         : %s%n", typeStr + pad(58 - 12));
        System.out.printf(CYA_G + "║" + R + "  Lignes totales: " + JAU + "%d" + R + "   Valeurs nulles : " +
                (nbNull > 0 ? ROU : VER) + "%d" + R + " (" + JAU + "%.1f%%" + R + ")%n",
                nbTotal, nbNull, nbNull * 100.0 / Math.max(1, nbTotal));

        // Unique
        try {
            int nbUniq = df.getUniqueCol(c);
            System.out.printf(CYA_G + "║" + R + "  Valeurs uniques : " + JAU + "%d" + R + " / %d%n", nbUniq, nbTotal);
        } catch (OutOfBoundException e) {
        }

        // ── Stats numériques ──────────────────────────────────────────────
        if (estNum) {
            try {
                System.out.println(CYA_G + "╠" + inner + "╣" + R);
                double moy = df.calculerMoyenne(c);
                double med = df.calculerMediane(c);
                double et = df.calculerEcartType(c);
                double var = df.calculerVariance(c);

                // Min et Max manuels (colonne peut avoir des null)
                double mn = Double.MAX_VALUE, mx = -Double.MAX_VALUE;
                for (int i = 0; i < df.getNbLignes(); i++) {
                    try {
                        Object v = df.getCase(i, c);
                        if (v instanceof Number) {
                            double d = ((Number) v).doubleValue();
                            if (d < mn)
                                mn = d;
                            if (d > mx)
                                mx = d;
                        }
                    } catch (OutOfBoundException ex) {
                    }
                }

                System.out.printf(CYA_G + "║" + R + "  " + JAU + "Min" + R + "     = " + BLANC + "%-12.4f  " + R + JAU
                        + "Max" + R + "     = " + BLANC + "%-12.4f" + R + "%n", mn, mx);
                System.out.printf(CYA_G + "║" + R + "  " + JAU + "Moyenne" + R + " = " + BLANC + "%-12.4f  " + R + JAU
                        + "Médiane" + R + " = " + BLANC + "%-12.4f" + R + "%n", moy, med);
                System.out.printf(CYA_G + "║" + R + "  " + JAU + "Éc.-type" + R + "= " + BLANC + "%-12.4f  " + R + JAU
                        + "Variance" + R + "= " + BLANC + "%-12.4f" + R + "%n", et, var);

                // Intervalles de confiance 95% (µ ± 1.96σ/√n)
                int n = (int) (nbTotal - nbNull);
                if (n > 1) {
                    double se = et / Math.sqrt(n);
                    System.out.printf(
                            CYA_G + "║" + R + "  " + MAG + "IC 95%%" + R + "  = [" + BLANC + "%.4f  ;  %.4f" + R
                                    + "]  (n=%d)%n",
                            moy - 1.96 * se, moy + 1.96 * se, n);
                }

                // Histogramme
                System.out.println(CYA_G + "╠" + inner + "╣" + R);
                System.out.println(CYA_G + "║" + R + " " + G + " Distribution" + R);
                System.out.println(CYA_G + "╚" + inner + "╝" + R);
                new HistogrammeAscii()
                        .titre("Distribution — " + nom)
                        .depuis(df, c)
                        .bins(12).hauteur(10)
                        .afficher();

            } catch (OutOfBoundException e) {
                err("Erreur calcul stats : " + e.getMessage());
            }
        } else {
            // ── Fréquences (colonne texte) ────────────────────────────────
            System.out.println(CYA_G + "╠" + inner + "╣" + R);
            System.out.println(CYA_G + "║" + R + " " + G + " Top valeurs les plus fréquentes" + R);
            System.out.println(CYA_G + "╚" + inner + "╝" + R);
            try {
                java.util.HashMap<Object, Integer> map = df.getUniqueColonneSomme(c);
                map.entrySet().stream()
                        .sorted((a, b) -> b.getValue() - a.getValue())
                        .limit(15)
                        .forEach(e -> {
                            double pct = e.getValue() * 100.0 / Math.max(1, nbTotal);
                            int bar = (int) (pct / 2.5);
                            String val = e.getKey() == null ? "null" : String.valueOf(e.getKey());
                            System.out.printf(
                                    "  %-20s " + CYA + "|" + R + " " + VER + "%-40s" + R + " " + JAU + "%5.1f%%" + R
                                            + " (%d)%n",
                                    val.length() > 20 ? val.substring(0, 19) + "…" : val,
                                    "█".repeat(Math.max(0, bar)), pct, e.getValue());
                        });
                if (map.size() > 15)
                    System.out.println("  " + MAG + "... et " + (map.size() - 15) + " autres valeurs." + R);
            } catch (OutOfBoundException e) {
                err("Erreur fréquences.");
            }
        }
    }

    /**
     * Génère une chaîne d'espaces pour aligner l'affichage dans le cadre de stats.
     *
     * @param n le nombre d'espaces souhaités
     * @return une chaîne de n espaces, ou vide si n ≤ 0
     */
    private String pad(int n) {
        return n > 0 ? " ".repeat(n) : "";
    }

    // ── 3. Graphiques ────────────────────────────────────────────────────────

    /**
     * Sous-menu graphiques ASCII.
     * Propose PieChart, Histogramme, BarChart, LineChart et HeatMap.
     */
    private void menuGraphiques() {
        if (!verDf())
            return;
        DataframeComplet df = gestion.getActif();
        boolean run = true;
        while (run) {
            titre("GRAPHIQUES");
            opt("1", "PieChart");
            opt("2", "Histogramme");
            opt("3", "BarChart");
            opt("4", "LineChart (courbes)");
            opt("5", "HeatMap");
            opt("0", "Retour");
            System.out.print(JAU_G + "► " + R);
            switch (lireChoix()) {
                case "1" -> gPie(df);
                case "2" -> gHisto(df);
                case "3" -> gBar(df);
                case "4" -> gLine(df);
                case "5" -> gHeat(df);
                case "0" -> run = false;
                default -> err("Invalide.");
            }
        }
    }

    /**
     * Affiche un camembert ASCII des fréquences d'une colonne catégorielle.
     *
     * @param df le dataframe actif
     */
    private void gPie(DataframeComplet df) {
        infoCols(df);
        System.out.print("Col catégorielle: ");
        int c = lireInt(-1);
        if (!validCol(df, c)) {
            err("Invalide.");
            return;
        }
        System.out.print("Titre: ");
        String t = sc.nextLine().trim();
        Map<Object, Integer> freq = new LinkedHashMap<>();
        for (int i = 0; i < df.getNbLignes(); i++) {
            try {
                Object v = df.getCase(i, c);
                if (v != null)
                    freq.merge(v, 1, Integer::sum);
            } catch (OutOfBoundException e) {
            }
        }
        PieChart pc = new PieChart().titre(t.isEmpty() ? "Répartition — " + df.getNomCol(c) : t);
        freq.forEach((k, v) -> pc.ajouter(k.toString(), v));
        System.out.print("Rayon (défaut=9): ");
        pc.afficher(lireInt(9));
    }

    /**
     * Affiche un histogramme ASCII de distribution d'une colonne numérique.
     *
     * @param df le dataframe actif
     */
    private void gHisto(DataframeComplet df) {
        infoCols(df);
        System.out.print("Col numérique: ");
        int c = lireInt(-1);
        if (!validCol(df, c)) {
            err("Invalide.");
            return;
        }
        System.out.print("Bins (défaut=10): ");
        int b = lireInt(10);
        System.out.print("Hauteur (défaut=14): ");
        int h = lireInt(14);
        new HistogrammeAscii().titre("Distribution — " + df.getNomCol(c)).depuis(df, c).bins(b).hauteur(h).afficher();
    }

    /**
     * Affiche un graphique à barres ASCII (groupées ou empilées).
     * Permet d'ajouter plusieurs séries de valeurs sur les mêmes catégories.
     *
     * @param df le dataframe actif
     */
    private void gBar(DataframeComplet df) {
        infoCols(df);
        System.out.print("Titre: ");
        String t = sc.nextLine().trim();
        System.out.print("Col catégories: ");
        int cc = lireInt(-1);
        if (!validCol(df, cc)) {
            err("Invalide.");
            return;
        }
        List<String> cats = new ArrayList<>();
        for (int i = 0; i < df.getNbLignes(); i++) {
            try {
                Object v = df.getCase(i, cc);
                if (v != null)
                    cats.add(v.toString());
            } catch (OutOfBoundException e) {
            }
        }
        BarChart bc = new BarChart().titre(t.isEmpty() ? "BarChart" : t).categorie(cats.toArray(new String[0]));
        boolean plus = true;
        while (plus) {
            System.out.print("Col valeurs (-1=fin): ");
            int vc = lireInt(-1);
            if (!validCol(df, vc))
                break;
            System.out.print("Nom série: ");
            String ns = sc.nextLine().trim();
            double[] vals = new double[df.getNbLignes()];
            for (int i = 0; i < df.getNbLignes(); i++) {
                try {
                    Object v = df.getCase(i, vc);
                    vals[i] = (v instanceof Number) ? ((Number) v).doubleValue() : 0;
                } catch (OutOfBoundException e) {
                }
            }
            bc.serie(ns.isEmpty() ? df.getNomCol(vc) : ns, vals);
            System.out.print("Autre série? (o/n): ");
            plus = sc.nextLine().trim().equalsIgnoreCase("o");
        }
        System.out.print("Horizontal? (o/n): ");
        if (sc.nextLine().trim().equalsIgnoreCase("o"))
            bc.horizontal();
        System.out.print("Empilé? (o/n): ");
        if (sc.nextLine().trim().equalsIgnoreCase("o"))
            bc.empile();
        bc.afficher();
    }

    /**
     * Affiche un graphique de courbes ASCII (LineChart) avec une ou plusieurs séries.
     * L'axe X peut être une colonne du dataframe ou simplement l'index de ligne.
     *
     * @param df le dataframe actif
     */
    private void gLine(DataframeComplet df) {
        infoCols(df);
        System.out.print("Titre: ");
        String t = sc.nextLine().trim();
        System.out.print("Col X (-1=index): ");
        int xc = lireInt(-1);
        LineChart lc = new LineChart().titre(t.isEmpty() ? "Courbes" : t)
                .xLabel(validCol(df, xc) ? df.getNomCol(xc) : "X");
        double[] xs = null;
        if (validCol(df, xc)) {
            xs = new double[df.getNbLignes()];
            for (int i = 0; i < df.getNbLignes(); i++) {
                try {
                    Object v = df.getCase(i, xc);
                    xs[i] = (v instanceof Number) ? ((Number) v).doubleValue() : i;
                } catch (OutOfBoundException e) {
                    xs[i] = i;
                }
            }
        }
        boolean plus = true;
        while (plus) {
            System.out.print("Col Y (-1=fin): ");
            int yc = lireInt(-1);
            if (!validCol(df, yc))
                break;
            System.out.print("Nom série: ");
            String ns = sc.nextLine().trim();
            double[] ys = new double[df.getNbLignes()];
            for (int i = 0; i < df.getNbLignes(); i++) {
                try {
                    Object v = df.getCase(i, yc);
                    ys[i] = (v instanceof Number) ? ((Number) v).doubleValue() : 0;
                } catch (OutOfBoundException e) {
                }
            }
            System.out.print("Remplissage sous courbe? (o/n): ");
            boolean f = sc.nextLine().trim().equalsIgnoreCase("o");
            if (xs != null)
                lc.serie(ns.isEmpty() ? df.getNomCol(yc) : ns, xs, ys, f);
            else
                lc.serie(ns.isEmpty() ? df.getNomCol(yc) : ns, ys);
            System.out.print("Autre série? (o/n): ");
            plus = sc.nextLine().trim().equalsIgnoreCase("o");
        }
        lc.afficher();
    }

    /**
     * Affiche une HeatMap de corrélation ou de covariance entre les colonnes numériques.
     *
     * @param df le dataframe actif
     */
    private void gHeat(DataframeComplet df) {
        titre("HEATMAP");
        opt("1", "Corrélation");
        opt("2", "Covariance");
        System.out.print(JAU_G + "► " + R);
        HeatMap hm = new HeatMap();
        if (lireChoix().equals("2"))
            hm.depuisCovariance(df);
        else
            hm.depuisCorrelation(df);
        hm.afficher();
    }

    // ── 4. Nettoyage ─────────────────────────────────────────────────────────

    /**
     * Sous-menu de nettoyage du dataframe actif.
     * Propose la gestion des outliers via boîte à moustaches,
     * ainsi que la suppression ou la mise à null des valeurs négatives.
     */
    private void menuNettoyer() {
        if (!verDf())
            return;
        DataframeComplet df = gestion.getActif();
        boolean run = true;
        while (run) {
            titre("NETTOYAGE");
            infoCols(df);
            System.out.println();
            opt("1", "Boîte à moustaches (outliers)");
            opt("2", "Supprimer les lignes avec valeurs négatives");
            opt("3", "Remplacer les valeurs négatives par null");
            opt("0", "Retour");
            System.out.print(JAU_G + "► " + R);
            switch (lireChoix()) {
                case "1" -> menuNettoyerBoite(df);
                case "2" -> supprimerLignesNegatives(df);
                case "3" -> mettreNegatifNull(df);
                case "0" -> run = false;
                default  -> err("Invalide.");
            }
        }
    }

    /**
     * Sous-menu boîte à moustaches.
     * L'utilisateur choisit une colonne numérique, visualise la boîte,
     * puis peut définir des seuils et supprimer ou remplacer les outliers.
     *
     * @param df le dataframe actif
     */
    private void menuNettoyerBoite(DataframeComplet df) {
        infoCols(df);
        System.out.print("Col à analyser: ");
        int c = lireInt(-1);
        if (!validCol(df, c)) {
            err("Invalide.");
            return;
        }

        BoiteAMoustache bam = new BoiteAMoustache(df, c);
        if (bam.getValeurs().isEmpty()) {
            err("Aucune valeur numérique.");
            return;
        }

        boolean run = true;
        while (run) {
            bam.afficher();
            opt("1", "Choisir seuil (interactif)");
            opt("2", "Seuils manuels");
            opt("3", "Supprimer les lignes hors seuil");
            opt("4", "Remplacer les valeurs hors seuil par null  [mise à jour auto]");
            opt("0", "Retour");
            System.out.print(JAU_G + "► " + R);
            switch (lireChoix()) {
                case "1" -> bam.choisirSeuil();
                case "2" -> {
                    System.out.print("Seuil bas  : ");
                    double sB = lireDouble(bam.getMoustacheBasse());
                    System.out.print("Seuil haut : ");
                    double sH = lireDouble(bam.getMoustacheHaute());
                    bam.setSeuils(sB, sH);
                }
                case "3" -> {
                    if (conf("Supprimer les lignes hors seuil ?")) {
                        bam.supprimerHorsSeuil();
                        run = false; // dataframe structurellement modifié
                    }
                }
                case "4" -> {
                    // Remplacer par null puis reconstruire le bam depuis le df mis à jour
                    int nbRemplaces = bam.remplacerHorsSeuilNull();
                    if (nbRemplaces > 0) {
                        ok(nbRemplaces + " valeur(s) → null. Recalcul de la boîte…");
                        double ancienSeuilBas  = bam.getSeuilBas();
                        double ancienSeuilHaut = bam.getSeuilHaut();
                        bam = new BoiteAMoustache(df, c);
                        if (bam.getValeurs().isEmpty()) {
                            err("Plus aucune valeur numérique dans la colonne. Sortie du nettoyage.");
                            run = false;
                        } else {
                            if (ancienSeuilBas >= bam.getMin() && ancienSeuilHaut <= bam.getMax())
                                bam.setSeuils(ancienSeuilBas, ancienSeuilHaut);
                        }
                    } else {
                        ok("Aucune valeur hors seuil — boîte inchangée.");
                    }
                }
                case "0" -> run = false;
                default  -> err("Invalide.");
            }
        }
    }

    /**
     * Supprime du dataframe toutes les lignes qui contiennent au moins une valeur
     * strictement négative dans la colonne choisie par l'utilisateur.
     * Demande confirmation, puis délègue la suppression à
     * {@link NettoyageDataframe#suppressionLignesNegatives(int)}.
     *
     * @param df le dataframe actif
     */
    private void supprimerLignesNegatives(DataframeComplet df) {
        infoCols(df);
        System.out.print("Col à vérifier (-1 = toutes les colonnes numériques) : ");
        int c = lireInt(-1);
        if (c != -1 && !validCol(df, c)) { err("Index invalide."); return; }

        // Comptage préalable pour la confirmation (parcours inline, sans ligneContientNegatif)
        int nbNeg = 0;
        int debut = (c == -1) ? 0 : c;
        int fin   = (c == -1) ? df.getNbCol() - 1 : c;
        lignes:
        for (int i = 0; i < df.getNbLignes(); i++) {
            for (int j = debut; j <= fin; j++) {
                try {
                    Object v = df.getCase(i, j);
                    if (v instanceof Number && ((Number) v).doubleValue() < 0) { nbNeg++; continue lignes; }
                } catch (OutOfBoundException e) { /* ignoré */ }
            }
        }

        if (nbNeg == 0) { ok("Aucune valeur négative détectée — dataframe inchangé."); return; }

        System.out.println(JAU + nbNeg + " ligne(s) contiennent des valeurs négatives." + R);
        if (!conf("Supprimer ces " + nbNeg + " ligne(s) ?")) { ok("Annulé."); return; }

        // Délégation à NettoyageDataframe
        int nbAvant = df.getNbLignes();
        NettoyageDataframe nettoyeur = creerNettoyeur(df);
        if (c == -1) {
            // Suppression séquentielle par colonne numérique (équivalent au mode "toutes colonnes")
            for (int j = 0; j < df.getNbCol(); j++)
                if (cNum(df, j)) nettoyeur.suppressionLignesNegatives(j);
        } else {
            nettoyeur.suppressionLignesNegatives(c);
        }
        ok((nbAvant - df.getNbLignes()) + " ligne(s) supprimée(s). " + df.getNbLignes() + " ligne(s) restantes.");
    }

    /**
     * Remplace par null toutes les valeurs strictement négatives dans la colonne
     * choisie (ou dans toutes les colonnes numériques si l'index est -1).
     * Délègue à {@link NettoyageDataframe#remplaceNegativeByNull(int)}.
     *
     * @param df le dataframe actif
     */
    private void mettreNegatifNull(DataframeComplet df) {
        infoCols(df);
        System.out.print("Col à traiter (-1 = toutes les colonnes numériques) : ");
        int c = lireInt(-1);
        if (c != -1 && !validCol(df, c)) { err("Index invalide."); return; }

        NettoyageDataframe nettoyeur = creerNettoyeur(df);
        int total = 0;
        if (c == -1) {
            for (int j = 0; j < df.getNbCol(); j++)
                if (cNum(df, j)) { int r = nettoyeur.remplaceNegativeByNull(j); if (r > 0) total += r; }
        } else {
            total = nettoyeur.remplaceNegativeByNull(c);
        }

        if (total <= 0)
            ok("Aucune valeur négative trouvée — dataframe inchangé.");
        else
            ok(total + " valeur(s) négative(s) remplacée(s) par null.");
    }

    /**
     * Crée une instance anonyme de {@link NettoyageDataframe} encapsulant {@code df}.
     * La reconstruction du tableau est déléguée à {@link #appliquerNouveauTableau}
     * via réflexion, évitant de dépendre d'une sous-classe concrète.
     *
     * @param df le dataframe à encapsuler
     * @return une instance de NettoyageDataframe prête à l'emploi
     */
    private NettoyageDataframe creerNettoyeur(DataframeComplet df) {
        return new NettoyageDataframe(df) {
            @Override protected int getIndexColonneAnisakis() { return -1; }
            @Override protected void reconstruireDataframe(Object[][] tab, int n) {
                appliquerNouveauTableau(df, tab, n);
            }
            @Override protected void reconstruireDataframeAvecNoms(Object[][] tab, String[] noms, int n) {
                appliquerNouveauTableau(df, tab, n); // noms de colonnes inchangés dans ce contexte
            }
        };
    }

    /**
     * Applique un nouveau tableau de données au dataframe via réflexion.
     * Utilisé par {@link #creerNettoyeur} pour implémenter les méthodes abstraites
     * de {@link NettoyageDataframe}.
     *
     * @param df          le dataframe à modifier
     * @param nouveauTab  le nouveau tableau de données
     * @param nbLignes    le nombre de lignes du nouveau tableau
     */
    private void appliquerNouveauTableau(DataframeComplet df, Object[][] nouveauTab, int nbLignes) {
        try {
            var fTab  = fish.acquisition.DataframeBase.class.getDeclaredField("tableau");
            var fLig  = fish.acquisition.DataframeBase.class.getDeclaredField("nbLignes");
            var fNoms = fish.acquisition.DataframeBase.class.getDeclaredField("nomColonne");
            fTab.setAccessible(true);
            fLig.setAccessible(true);
            fNoms.setAccessible(true);
            fTab.set(df, nouveauTab);
            fLig.set(df, nbLignes);
            fNoms.set(df, df.getNomColonnes()); // noms inchangés
        } catch (Exception e) {
            err("Erreur interne reconstruction dataframe : " + e.getMessage());
        }
    }

    // ── 5. Complétion des valeurs manquantes ─────────────────────────────────

    /**
     * Sous-menu de complétion des valeurs manquantes (null).
     * Propose trois stratégies : moyenne, régression linéaire, KNN.
     */
    private void menuCompleter() {
        if (!verDf())
            return;
        boolean run = true;
        while (run) {
            titre("COMPLÉTION DES VALEURS MANQUANTES");
            opt("1", "Par la moyenne          (CompletionMoyenne)");
            opt("2", "Par régression linéaire (CompletionRegression)");
            opt("3", "Par KNN                 (CompletionKNN)");
            opt("0", "Retour");
            System.out.print(JAU_G + "► " + R);
            switch (lireChoix()) {
                case "1" -> menuCompleterMoyenne();
                case "2" -> menuCompleterRegression();
                case "3" -> menuCompleterKNN();
                case "0" -> run = false;
                default -> err("Invalide.");
            }
        }
    }

    /**
     * Sous-menu de complétion par la moyenne.
     * Permet de compléter toutes les colonnes numériques, une colonne spécifique
     * ou les colonnes dont le nom contient un mot-clé.
     */
    private void menuCompleterMoyenne() {
        if (!verDf())
            return;
        DataframeComplet df = gestion.getActif();
        CompletionMoyenne cm = new CompletionMoyenne(df);
        boolean run = true;
        while (run) {
            titre("COMPLÉTION PAR MOYENNE");
            infoCols(df);
            System.out.println();
            opt("1", "Compléter toutes les colonnes numériques");
            opt("2", "Compléter une colonne par son index");
            opt("3", "Compléter les colonnes dont le nom contient un mot-clé");
            opt("0", "Retour");
            System.out.print(JAU_G + "► " + R);
            switch (lireChoix()) {
                case "1" -> {
                    int n = cm.completerTout();
                    ok(n + " case(s) complétée(s) par la moyenne.");
                }
                case "2" -> {
                    System.out.print("Index de la colonne : ");
                    int c = lireInt(-1);
                    if (!validCol(df, c)) {
                        err("Index invalide.");
                        break;
                    }
                    int n = cm.completerColonne(c);
                    ok(n + " case(s) complétée(s) dans '" + df.getNomCol(c) + "'.");
                }
                case "3" -> {
                    System.out.print("Mot-clé dans le nom de colonne : ");
                    String mc = sc.nextLine().trim();
                    int n = cm.completerColonneParNom(mc);
                    ok(n + " case(s) complétée(s) pour les colonnes contenant '" + mc + "'.");
                }
                case "0" -> run = false;
                default -> err("Invalide.");
            }
        }
    }

    /**
     * Sous-menu de complétion par régression linéaire.
     * L'utilisateur choisit manuellement le prédicteur (X) et la colonne cible (Y),
     * ou lance une complétion automatique avec le meilleur prédicteur.
     */
    private void menuCompleterRegression() {
        if (!verDf())
            return;
        DataframeComplet df = gestion.getActif();
        CompletionRegression cr = new CompletionRegression(df);
        boolean run = true;
        while (run) {
            titre("COMPLÉTION PAR RÉGRESSION LINÉAIRE");
            infoCols(df);
            System.out.println();
            opt("1", "Compléter une colonne (prédicteur manuel)");
            opt("2", "Compléter tout avec le meilleur prédicteur auto");
            opt("0", "Retour");
            System.out.print(JAU_G + "► " + R);
            switch (lireChoix()) {
                case "1" -> {
                    System.out.print("Index colonne prédicteur (X) : ");
                    int src = lireInt(-1);
                    System.out.print("Index colonne à compléter (Y) : ");
                    int cib = lireInt(-1);
                    if (!validCol(df, src) || !validCol(df, cib)) {
                        err("Index invalide.");
                        break;
                    }
                    int n = cr.completerParRegression(src, cib);
                    if (n >= 0)
                        ok(n + " case(s) complétée(s) par régression y=f(" + df.getNomCol(src) + ").");
                    else
                        err("Régression impossible (voir messages ci-dessus).");
                    afficherRegressionViz(df, src, cib);
                }
                case "2" -> {
                    int n = cr.completerToutParMeilleurPredicteur();
                    ok(n + " case(s) complétée(s) au total (meilleur prédicteur auto).");
                }
                case "0" -> run = false;
                default -> err("Invalide.");
            }
        }
    }

    /**
     * Affiche un mini scatter-plot de la droite de régression après complétion.
     * Permet de visualiser la qualité de l'ajustement.
     */
    private void afficherRegressionViz(DataframeComplet df, int colX, int colY) {
        System.out.println(
                CYA_G + "\n── Visualisation régression " + df.getNomCol(colX) + " → " + df.getNomCol(colY) + " ──" + R);
        double[] xs = cVals(df, colX), ys = cVals(df, colY);
        int[] labels = new int[xs.length]; // tous cluster 0 (même couleur)
        AsciiPlot.scatterPlot(xs, ys, labels,
                "Régression : " + df.getNomCol(colY) + " = f(" + df.getNomCol(colX) + ")",
                df.getNomCol(colX), df.getNomCol(colY), 55, 14);
    }

    /**
     * Sous-menu de complétion par KNN (k plus proches voisins).
     * Permet de modifier k et de compléter toutes les colonnes ou une seule.
     */
    private void menuCompleterKNN() {
        if (!verDf())
            return;
        DataframeComplet df = gestion.getActif();
        System.out.print("k (nombre de voisins, défaut=5) : ");
        CompletionKNN knn = new CompletionKNN(df, lireInt(5));
        boolean run = true;
        while (run) {
            titre("COMPLÉTION PAR KNN (k=" + knn.getK() + ")");
            infoCols(df);
            System.out.println();
            opt("1", "Compléter toutes les colonnes numériques");
            opt("2", "Compléter une colonne spécifique");
            opt("3", "Changer k (actuel : " + knn.getK() + ")");
            opt("0", "Retour");
            System.out.print(JAU_G + "► " + R);
            switch (lireChoix()) {
                case "1" -> knn.completerTout();
                case "2" -> {
                    System.out.print("Index : ");
                    knn.completerColonne(lireInt(-1));
                }
                case "3" -> {
                    System.out.print("Nouveau k : ");
                    knn.setK(lireInt(5));
                }
                case "0" -> run = false;
                default -> err("Invalide.");
            }
        }
    }

    // ── 6. Clustering ────────────────────────────────────────────────────────

    /**
     * Sous-menu de clustering.
     * Propose KMeans, KMeans step-by-step, KMedoids PAM, méthode du coude,
     * et la visualisation du dernier résultat via scatter plot ou camembert.
     */
    private void menuClustering() {
        if (!verDf())
            return;
        DataframeComplet df = gestion.getActif();
        System.out.print("k (défaut=3): ");
        int k = lireInt(3);
        boolean run = true;
        while (run) {
            titre("CLUSTERING k=" + k);
            opt("1", "KMeans");
            opt("2", "KMeans step-by-step+convergence");
            opt("3", "KMedoids PAM");
            opt("4", "Coude KMeans");
            opt("5", "Coude KMedoids");
            opt("6", "Afficher dernier résultat");
            opt("7", "Scatter plot clusters");
            opt("8", "PieChart clusters");
            opt("9", "Changer k (=" + k + ")");
            opt("0", "Retour");
            System.out.print(JAU_G + "► " + R);
            switch (lireChoix()) {
                case "1" -> {
                    KMeans km = new KMeans(df, k);
                    km.executer();
                    km.afficher();
                    dernierClustering = km;
                }
                case "2" -> {
                    KMeansViz kmv = new KMeansViz(df, k).stepByStep();
                    kmv.executerAvecViz(sc);
                    kmv.afficherConvergence();
                    dernierClustering = kmv;
                }
                case "3" -> {
                    KMedoids kmd = new KMedoids(df, k);
                    kmd.executer();
                    kmd.afficher();
                    dernierClustering = kmd;
                }
                case "4" -> {
                    System.out.print("kMin: ");
                    int a = lireInt(2);
                    System.out.print("kMax: ");
                    int b = lireInt(10);
                    System.out.print("Répét: ");
                    int r = lireInt(3);
                    new ElbowMethod(df).kMin(a).kMax(b).repetitions(r).executer().afficher();
                }
                case "5" -> {
                    System.out.print("kMin: ");
                    int a = lireInt(2);
                    System.out.print("kMax: ");
                    int b = lireInt(8);
                    new ElbowMethod(df).kMin(a).kMax(b).avecMedoids().executer().afficher();
                }
                case "6" -> {
                    if (dernierClustering instanceof KMeans km)
                        km.afficher();
                    else if (dernierClustering instanceof KMedoids kmd)
                        kmd.afficher();
                    else
                        err("Aucun clustering.");
                }
                case "7" -> clusterScatter(df);
                case "8" -> clusterPie();
                case "9" -> {
                    System.out.print("k: ");
                    k = Math.max(2, lireInt(3));
                }
                case "0" -> run = false;
                default -> err("Invalide.");
            }
        }
    }

    /**
     * Affiche un scatter plot ASCII des clusters du dernier clustering effectué.
     * L'utilisateur choisit les colonnes X et Y pour les axes.
     *
     * @param df le dataframe actif
     */
    private void clusterScatter(DataframeComplet df) {
        if (dernierClustering == null) {
            err("Lancez d'abord un clustering.");
            return;
        }
        infoCols(df);
        System.out.print("Col X: ");
        int cx = lireInt(0);
        System.out.print("Col Y: ");
        int cy = lireInt(1);
        int[] lbl = dernierClustering instanceof KMeans km ? km.getLabels()
                : ((KMedoids) dernierClustering).getLabels();
        AsciiPlot.scatterPlot(cVals(df, cx), cVals(df, cy), lbl,
                "Clusters — " + df.getNomCol(cx) + " vs " + df.getNomCol(cy), df.getNomCol(cx), df.getNomCol(cy), 60,
                20);
    }

    /**
     * Affiche un camembert ASCII de la répartition des points par cluster
     * pour le dernier clustering effectué (KMeans ou KMedoids).
     */
    private void clusterPie() {
        if (dernierClustering == null) {
            err("Lancez d'abord un clustering.");
            return;
        }
        int k2;
        int[] lbl;
        if (dernierClustering instanceof KMeans km) {
            k2 = km.getK();
            lbl = km.getLabels();
        } else {
            KMedoids kmd = (KMedoids) dernierClustering;
            k2 = kmd.getK();
            lbl = kmd.getLabels();
        }
        PieChart pc = new PieChart().titre("Taille des clusters");
        for (int c = 0; c < k2; c++) {
            final int fc = c;
            long cnt = Arrays.stream(lbl).filter(l -> l == fc).count();
            pc.ajouter("Cluster " + c, cnt);
        }
        pc.afficher(7);
    }

    // ── 7. Manipulation du dataframe ─────────────────────────────────────────

    /**
     * Sous-menu de manipulation structurelle du dataframe actif.
     * Propose la fusion/suppression de colonnes, la duplication du dataframe,
     * l'affichage des valeurs uniques, le renommage de colonnes et le pivot
     * du format brut (Espèce × Paramètre) vers le format multi-période.
     */
    private void menuManipulation() {
        if (!verDf())
            return;
        DataframeComplet df = gestion.getActif();
        boolean run = true;
        while (run) {
            titre("MANIPULATION DU DATAFRAME");
            infoCols(df);
            System.out.println();
            opt("1", "Fusionner deux colonnes complémentaires");
            opt("2", "Supprimer une colonne");
            opt("3", "Dupliquer le dataframe actif (nouvelle entrée)");
            opt("4", "Valeurs uniques d'une colonne");
            opt("5", "Renommer une colonne");
            opt("6", "Voir dimensions et titre");
            opt("7", "Pivoter (format brut → multi-période)");
            opt("0", "Retour");
            System.out.print(JAU_G + "► " + R);
            switch (lireChoix()) {
                case "1" -> fusionnerColonnes(df);
                case "2" -> supprimerColonne(df);
                case "3" -> dupliquerDataframe();
                case "4" -> valeursUniques(df);
                case "5" -> renommerColonne(df);
                case "6" -> {
                    System.out.println(CYA_G + "Titre: " + R + df.getTitle());
                    System.out.println(CYA_G + "Dim:   " + R + df.getDimension());
                }
                case "7" -> { pivoterDataframe(); df = gestion.getActif(); }
                case "0" -> run = false;
                default -> err("Invalide.");
            }
        }
    }

    /**
     * Pivote le dataframe actif du format « brut Peru »
     * (Espèce | Paramètre | Total | 2012 | …) vers le format multi-période
     * (Espèce | N_Total | N_2012 | Prévalence (%)_Total | …).
     *
     * <p>Conditions requises :</p>
     * <ul>
     *   <li>Le dataframe actif doit être un {@link DfPopulation}</li>
     *   <li>Il ne doit pas déjà être en format multi-période</li>
     *   <li>Il doit posséder des colonnes "Espèce" et "Paramètre"</li>
     * </ul>
     *
     * <p>Le résultat remplace le dataframe actif dans le gestionnaire.
     * Les populations multi-période sont automatiquement construites.</p>
     */
    private void pivoterDataframe() {
        GestionCSV.EntreeCSV ea = gestion.getEntreeActive();
        if (ea == null) {
            err("Aucun dataframe actif.");
            return;
        }
        if (!(ea.df instanceof DfPopulation)) {
            err("Le pivot nécessite un DfPopulation.");
            err("Rechargez ce CSV via [1] → Charger → type 'Population'.");
            return;
        }
        DfPopulation dfPop = (DfPopulation) ea.df;
        if (dfPop.isFormatMultiPeriode()) {
            err("Ce dataframe est déjà en format multi-période — pivot inutile.");
            return;
        }

        // ── Aperçu avant pivot ────────────────────────────────────────────────
        titre("PIVOT — " + ea.nom);
        System.out.println(CYA + "Format actif  : " + R + dfPop.getNbLignes()
                + " lignes × " + dfPop.getNbCol() + " colonnes (brut Espèce×Paramètre)");
        System.out.println(CYA + "Après pivot   : " + R
                + "une ligne par espèce, colonnes = Parametre_Periode");

        if (!conf("Confirmer le pivot ?")) {
            ok("Annulé.");
            return;
        }

        // ── Appel pivoter() ──────────────────────────────────────────────────
        System.out.println(CYA_G + "── Pivot en cours… ──" + R);
        DfPopulation pivote = dfPop.pivoter();

        if (pivote == null) {
            err("Pivot impossible : colonnes 'Espèce'/'Paramètre' introuvables "
                    + "ou aucune période détectée (Total / année à 4 chiffres / Moyenne).");
            return;
        }

        // ── Nom du dataframe résultant ────────────────────────────────────────
        System.out.print("Nom du résultat (Entrée = '" + ea.nom + " (pivoté)') : ");
        String nom = sc.nextLine().trim();
        if (nom.isEmpty())
            nom = ea.nom + " (pivoté)";

        // ── Remplacement du dataframe actif ──────────────────────────────────
        ea.df  = pivote;
        ea.nom = nom;

        // ── Résumé ────────────────────────────────────────────────────────────
        String[] periodes = pivote.getPeriodes();
        ok("Pivot réussi → '" + nom + "'");
        System.out.printf(VER + "  Espèces   : %d%n" + R, pivote.getNbLignes());
        System.out.printf(VER + "  Périodes  : %d %s%n" + R,
                periodes.length, java.util.Arrays.toString(periodes));
        System.out.printf(VER + "  Populations : %d (espèces × périodes)%n" + R,
                pivote.getPopulations().length);
        System.out.printf(VER + "  Colonnes  : %d%n" + R, pivote.getNbCol());
        pivote.afficherResume();
    }

    /**
     * Dialogue de fusion de deux colonnes complémentaires du dataframe.
     * Les deux colonnes doivent être complémentaires (jamais toutes les deux
     * non-null sur la même ligne). La colonne résultante remplace les deux.
     *
     * @param df le dataframe actif
     */
    private void fusionnerColonnes(DataframeComplet df) {
        infoCols(df);
        System.out.print("Index colonne 1 (doit avoir null là où col2 a une valeur): ");
        int c1 = lireInt(-1);
        System.out.print("Index colonne 2 : ");
        int c2 = lireInt(-1);
        if (!validCol(df, c1) || !validCol(df, c2)) {
            err("Index invalide.");
            return;
        }
        System.out.print("Nom de la nouvelle colonne fusionnée : ");
        String nom = sc.nextLine().trim();
        if (nom.isEmpty())
            nom = df.getNomCol(c1) + "_" + df.getNomCol(c2);
        try {
            df.fusionCol(c1, c2, nom);
            ok("Colonnes fusionnées → '" + nom + "'. Nouvelles colonnes: " + df.getNbCol());
            infoCols(df);
        } catch (fish.exceptions.NotNullException e) {
            err("Chevauchement détecté : les deux colonnes ont une valeur sur au moins une ligne.");
        } catch (Exception e) {
            err("Erreur fusion : " + e.getMessage());
        }
    }

    /**
     * Dialogue de suppression d'une colonne du dataframe actif.
     * Demande confirmation avant d'effectuer la suppression.
     *
     * @param df le dataframe actif
     */
    private void supprimerColonne(DataframeComplet df) {
        infoCols(df);
        System.out.print("Index de la colonne à supprimer : ");
        int c = lireInt(-1);
        if (!validCol(df, c)) {
            err("Index invalide.");
            return;
        }
        System.out.print(JAU + "Supprimer '" + df.getNomCol(c) + "' ? (o/n): " + R);
        if (!sc.nextLine().trim().equalsIgnoreCase("o")) {
            ok("Annulé.");
            return;
        }
        try {
            String nomSup = df.getNomCol(c);
            df.supprimerColonne(c);
            ok("Colonne '" + nomSup + "' supprimée. Colonnes restantes: " + df.getNbCol());
            infoCols(df);
        } catch (Exception e) {
            err("Erreur suppression : " + e.getMessage());
        }
    }

    /**
     * Duplique le dataframe actif en le rechargeant depuis son fichier source.
     * Le duplicata est ajouté comme nouvelle entrée dans le gestionnaire CSV
     * avec un nom personnalisable (défaut : "{nom} (copie)").
     */
    private void dupliquerDataframe() {
        GestionCSV.EntreeCSV ea = gestion.getEntreeActive();
        if (ea == null) {
            err("Aucun dataframe actif.");
            return;
        }
        System.out.print("Nom du duplicata (Entrée=auto) : ");
        String nom = sc.nextLine().trim();
        if (nom.isEmpty())
            nom = ea.nom + " (copie)";
        // Charger depuis le même chemin avec les mêmes paramètres (charset inclus)
        GestionCSV.EntreeCSV copie = gestion.charger(ea.chemin, ea.delimiteur, ea.charset, ea.type, nom);
        if (copie != null)
            ok("Dataframe dupliqué → #" + copie.id + " '" + nom + "' [charset=" + ea.charset + "]");
        else
            err("Impossible de dupliquer (re-lecture depuis " + ea.chemin + ").");
    }

    /**
     * Affiche les valeurs uniques d'une colonne avec leur fréquence et pourcentage.
     * Limite l'affichage aux 30 valeurs les plus fréquentes.
     *
     * @param df le dataframe actif
     */
    private void valeursUniques(DataframeComplet df) {
        infoCols(df);
        System.out.print("Index de la colonne : ");
        int c = lireInt(-1);
        if (!validCol(df, c)) {
            err("Invalide.");
            return;
        }
        try {
            java.util.HashMap<Object, Integer> map = df.getUniqueColonneSomme(c);
            int total = df.getUniqueCol(c);
            System.out.println(
                    CYA_G + "\n── Valeurs uniques — " + df.getNomCol(c) + " (" + total + " distinctes) ──" + R);
            map.entrySet().stream()
                    .sorted((a, b) -> b.getValue() - a.getValue())
                    .limit(30)
                    .forEach(e -> {
                        double pct = e.getValue() * 100.0 / df.getNbLignes();
                        int bar = Math.max(1, (int) (pct / 2));
                        System.out.printf("  %-25s " + JAU + "%4d" + R + " (" + VER + "%.1f%%" + R + ") %s%n",
                                e.getKey() == null ? "null"
                                        : String.valueOf(e.getKey()).substring(0,
                                                Math.min(25, String.valueOf(e.getKey()).length())),
                                e.getValue(), pct, "█".repeat(bar));
                    });
            if (map.size() > 30)
                System.out.println("  ... et " + (map.size() - 30) + " autres valeurs.");
        } catch (Exception e) {
            err("Erreur : " + e.getMessage());
        }
    }

    /**
     * Renomme une colonne du dataframe actif.
     * La modification est directe sur le tableau des en-têtes.
     *
     * @param df le dataframe actif
     */
    private void renommerColonne(DataframeComplet df) {
        infoCols(df);
        System.out.print("Index de la colonne à renommer : ");
        int c = lireInt(-1);
        if (!validCol(df, c)) {
            err("Invalide.");
            return;
        }
        System.out.print("Ancien nom : " + df.getNomCol(c) + "  →  Nouveau nom : ");
        String nouveau = sc.nextLine().trim();
        if (nouveau.isEmpty()) {
            ok("Annulé.");
            return;
        }
        df.getNomColonnes()[c] = nouveau;
        ok("Colonne renommée → '" + nouveau + "'");
    }

    // ── Méthodes utilitaires d'affichage ─────────────────────────────────────

    /**
     * Affiche un titre de section coloré en cyan gras dans le terminal.
     *
     * @param t le texte du titre
     */
    private void titre(String t) {
        System.out.println("\n" + CYA_G + "── " + t + " ──" + R);
    }

    /**
     * Affiche une ligne d'option de menu formatée avec numéro et libellé.
     *
     * @param n le numéro/touche de l'option (ex : "1", "0", "q")
     * @param l le libellé descriptif de l'option
     */
    private void opt(String n, String l) {
        System.out.println("  " + VER_G + "[" + n + "]" + R + " " + l);
    }

    /**
     * Affiche un message de succès (✔) en vert gras.
     *
     * @param m le message à afficher
     */
    private void ok(String m) {
        System.out.println(VER_G + "✔ " + m + R);
    }

    /**
     * Affiche un message d'erreur (✘) en rouge gras.
     *
     * @param m le message d'erreur à afficher
     */
    private void err(String m) {
        System.out.println(ROU_G + "✘ " + m + R);
    }

    /**
     * Affiche la liste des colonnes du dataframe avec leur index, nom, type
     * (NUM ou TXT) et nombre de valeurs nulles.
     *
     * @param df le dataframe à inspecter
     */
    private void infoCols(DataframeComplet df) {
        System.out.println(CYA_G + "\n── Colonnes ──" + R);
        for (int j = 0; j < df.getNbCol(); j++) {
            boolean num = cNum(df, j);
            int nul = cNull(df, j);
            System.out.printf("  " + CYA + "[%2d]" + R + " %-22s %s  " + ROU + "%d null" + R + "%n", j, df.getNomCol(j),
                    num ? VER + "NUM" + R : MAG + "TXT" + R, nul);
        }
    }

    /**
     * Vérifie qu'un dataframe est bien chargé et actif.
     * Affiche un message d'erreur si ce n'est pas le cas.
     *
     * @return true si un dataframe actif est disponible, false sinon
     */
    private boolean verDf() {
        if (gestion.estVide() || gestion.getActif() == null) {
            err("Aucun CSV — utilisez [1].");
            return false;
        }
        return true;
    }

    /**
     * Vérifie qu'un index de colonne est valide pour le dataframe donné.
     *
     * @param df le dataframe à tester
     * @param c  l'index de colonne à valider
     * @return {@code true} si l'index est dans les bornes [0, nbCol[
     */
    private boolean validCol(DataframeComplet df, int c) {
        return c >= 0 && c < df.getNbCol();
    }

    /**
     * Demande une confirmation (o/n) à l'utilisateur.
     *
     * @param m le message de confirmation à afficher
     * @return {@code true} si l'utilisateur répond "o" (insensible à la casse)
     */
    private boolean conf(String m) {
        System.out.print(JAU + m + " (o/n): " + R);
        return sc.nextLine().trim().equalsIgnoreCase("o");
    }

    /**
     * Lit le choix de l'utilisateur depuis l'entrée standard (une ligne, trimée).
     * Retourne "0" en cas d'erreur de lecture (fin de flux).
     *
     * @return la chaîne saisie, ou "0" si le flux est fermé
     */
    private String lireChoix() {
        try {
            return sc.nextLine().trim();
        } catch (NoSuchElementException e) {
            return "0";
        }
    }

    /**
     * Lit un entier depuis l'entrée standard.
     * Retourne la valeur par défaut si la saisie est vide ou non numérique.
     *
     * @param d la valeur par défaut à retourner si la saisie est invalide
     * @return l'entier saisi ou {@code d}
     */
    private int lireInt(int d) {
        try {
            String s = sc.nextLine().trim();
            return s.isEmpty() ? d : Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return d;
        }
    }

    /**
     * Lit un double depuis l'entrée standard.
     * Retourne la valeur par défaut si la saisie est vide ou non numérique.
     *
     * @param d la valeur par défaut à retourner si la saisie est invalide
     * @return le double saisi ou {@code d}
     */
    private double lireDouble(double d) {
        try {
            String s = sc.nextLine().trim();
            return s.isEmpty() ? d : Double.parseDouble(s);
        } catch (NumberFormatException e) {
            return d;
        }
    }

    /**
     * Extrait les valeurs numériques d'une colonne du dataframe sous forme de
     * tableau {@code double[]}. Les valeurs null ou non numériques sont remplacées par 0.
     *
     * @param df le dataframe source
     * @param c  l'index de la colonne
     * @return un tableau de doubles de taille {@code df.getNbLignes()}
     */
    private double[] cVals(DataframeComplet df, int c) {
        double[] v = new double[df.getNbLignes()];
        for (int i = 0; i < df.getNbLignes(); i++) {
            try {
                Object o = df.getCase(i, c);
                v[i] = (o instanceof Number) ? ((Number) o).doubleValue() : 0;
            } catch (OutOfBoundException e) {
            }
        }
        return v;
    }

    /**
     * Détermine si une colonne est numérique en inspectant la première valeur non-null.
     *
     * @param df le dataframe source
     * @param c  l'index de la colonne
     * @return {@code true} si la première valeur non-null est un {@link Number}
     */
    private boolean cNum(DataframeComplet df, int c) {
        for (int i = 0; i < df.getNbLignes(); i++) {
            try {
                Object v = df.getCase(i, c);
                if (v != null)
                    return v instanceof Number;
            } catch (OutOfBoundException e) {
            }
        }
        return false;
    }

    /**
     * Compte le nombre de valeurs null dans une colonne.
     *
     * @param df le dataframe source
     * @param c  l'index de la colonne
     * @return le nombre de cellules null dans cette colonne
     */
    private int cNull(DataframeComplet df, int c) {
        int n = 0;
        for (int i = 0; i < df.getNbLignes(); i++) {
            try {
                if (df.getCase(i, c) == null)
                    n++;
            } catch (OutOfBoundException e) {
            }
        }
        return n;
    }

    /**
     * Tronque une chaîne à une longueur maximale en ajoutant "…" si nécessaire.
     *
     * @param s la chaîne à tronquer
     * @param m la longueur maximale autorisée
     * @return la chaîne originale si elle est dans les bornes, sinon tronquée avec "…"
     */
    private String trunc(String s, int m) {
        return s.length() <= m ? s : s.substring(0, m - 1) + "…";
    }
}
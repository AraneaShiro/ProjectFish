package fish.nettoyage;

// ── Import ──────────────────────────────────────────────────
import fish.acquisition.DataframeComplet;
import fish.exceptions.OutOfBoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

/**
 * Boîte à moustaches (box plot) pour l'analyse et le nettoyage des valeurs
 * aberrantes d'une colonne numérique d'un Dataframe.
 *
 * Stockage : Q1, médiane, Q3, moustaches basse et haute, outliers.
 *
 * Méthodes :
 *   - importerDepuisColonne()  : charge les données depuis une colonne
 *   - calculerStatistiques()   : calcule Q1, Q3, IQR, moustaches
 *   - importerStatistiques()   : charge des stats déjà calculées
 *   - afficher()               : affiche la boîte à moustaches en ASCII
 *   - choisirSeuil()           : l'utilisateur choisit un seuil interactif
 *   - setSeuils()              : définit les seuils manuellement
 *   - supprimerHorsSeuil()     : supprime les lignes hors seuil dans le dataframe
 *   - remplacerHorsSeuilNull() : remplace les valeurs hors seuil par null
 *
 * @author Jules Grenesche
 * @version 1.0
 */
public class BoiteAMoustache {

    // ── Dimensions du cadre ────────────────────────────────────────────────────
    private static final int FRAME_W = 96;
    private static final int DRAW    = 84;

    // ── Couleurs ANSI ────────────────────────────────────────────────────────

    private static final String RESET      = "\u001B[0m";
    private static final String GRAS       = "\u001B[1m";

    // Texte
    private static final String ROUGE      = "\u001B[31m";
    private static final String VERT       = "\u001B[32m";
    private static final String JAUNE      = "\u001B[33m";
    private static final String BLEU       = "\u001B[34m";
    private static final String MAGENTA    = "\u001B[35m";
    private static final String CYAN       = "\u001B[36m";
    private static final String BLANC      = "\u001B[37m";

    // Combinaisons utiles
    private static final String ROUGE_GRAS  = GRAS + ROUGE;
    private static final String VERT_GRAS   = GRAS + VERT;
    private static final String CYAN_GRAS   = GRAS + CYAN;
    private static final String JAUNE_GRAS  = GRAS + JAUNE;

    // ── Stockage des données ─────────────────────────────────────────────────

    /** Les valeurs numériques extraites de la colonne */
    private List<Double> valeurs;

    /** Index de la colonne dans le dataframe source */
    private int indexColonne;

    /** Nom de la colonne */
    private String nomColonne;

    /** Le dataframe source */
    private DataframeComplet df;

    // ── Statistiques calculées ───────────────────────────────────────────────

    /** Premier quartile (25%) */
    private double q1;

    /** Médiane (50%) */
    private double mediane;

    /** Troisième quartile (75%) */
    private double q3;

    /** Écart interquartile : Q3 - Q1 */
    private double iqr;

    /** Moustache basse : Q1 - 1.5 * IQR */
    private double moustacheBasse;

    /** Moustache haute : Q3 + 1.5 * IQR */
    private double moustacheHaute;

    /** Valeur minimale des données */
    private double min;

    /** Valeur maximale des données */
    private double max;

    /** Seuil bas choisi par l'utilisateur (défaut = moustache basse) */
    private double seuilBas;

    /** Seuil haut choisi par l'utilisateur (défaut = moustache haute) */
    private double seuilHaut;

    /** true si les statistiques ont été calculées */
    private boolean statistiquesCalculees;

    // ── Constructeurs ────────────────────────────────────────────────────────

    /**
     * Constructeur vide — données à importer via importerDepuisColonne()
     */
    public BoiteAMoustache() {
        this.valeurs               = new ArrayList<>();
        this.statistiquesCalculees = false;
    }

    /**
     * Constructeur avec import direct depuis une colonne du dataframe.
     *
     * @param df           le dataframe source
     * @param indexColonne l'index de la colonne numérique
     */
    public BoiteAMoustache(DataframeComplet df, int indexColonne) {
        this();
        importerDepuisColonne(df, indexColonne);
    }

    /**
     * Constructeur avec import par nom de colonne.
     *
     * @param df         le dataframe source
     * @param nomColonne le mot-clé à rechercher dans les noms de colonnes
     */
    public BoiteAMoustache(DataframeComplet df, String nomColonne) {
        this();
        int col = trouverIndexColonne(df, nomColonne);
        if (col >= 0) {
            importerDepuisColonne(df, col);
        } else {
            System.out.println("Colonne '" + nomColonne + "' introuvable.");
        }
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    /** Retourne le premier quartile */
    public double getQ1()             { return q1; }
    /** Retourne la médiane */
    public double getMediane()        { return mediane; }
    /** Retourne le troisième quartile */
    public double getQ3()             { return q3; }
    /** Retourne l'écart interquartile */
    public double getIqr()            { return iqr; }
    /** Retourne la moustache basse */
    public double getMoustacheBasse() { return moustacheBasse; }
    /** Retourne la moustache haute */
    public double getMoustacheHaute() { return moustacheHaute; }
    /** Retourne le minimum */
    public double getMin()            { return min; }
    /** Retourne le maximum */
    public double getMax()            { return max; }
    /** Retourne le seuil bas actuel */
    public double getSeuilBas()       { return seuilBas; }
    /** Retourne le seuil haut actuel */
    public double getSeuilHaut()      { return seuilHaut; }
    /** Retourne les valeurs importées */
    public List<Double> getValeurs()  { return valeurs; }

    // ── Importation des données ──────────────────────────────────────────────

    /**
     * Importe les valeurs numériques d'une colonne du dataframe.
     * Les null et valeurs non numériques sont ignorés.
     * Lance automatiquement le calcul des statistiques.
     *
     * @param df           le dataframe source
     * @param indexColonne l'index de la colonne
     */
    public void importerDepuisColonne(DataframeComplet df, int indexColonne) {
        if (indexColonne < 0 || indexColonne >= df.getNbCol()) {
            System.out.println("Index de colonne invalide : " + indexColonne);
            return;
        }

        this.df           = df;
        this.indexColonne = indexColonne;
        this.nomColonne   = df.getNomCol(indexColonne);
        this.valeurs      = new ArrayList<>();

        for (int i = 0; i < df.getNbLignes(); i++) {
            try {
                Object val = df.getCase(i, indexColonne);
                if (val instanceof Number) {
                    valeurs.add(((Number) val).doubleValue());
                }
            } catch (OutOfBoundException e) { /* ignoré */ }
        }

        System.out.println(VERT + "✔ " + valeurs.size() + " valeur(s) importée(s) depuis '"
                + CYAN + nomColonne + VERT + "'." + RESET);

        if (!valeurs.isEmpty()) {
            calculerStatistiques();
        } else {
            System.out.println(JAUNE + "⚠ Aucune valeur numérique dans cette colonne." + RESET);
        }
    }

    // ── Calcul des statistiques ──────────────────────────────────────────────

    /**
     * Calcule Q1, médiane, Q3, IQR, moustaches basse et haute.
     * Initialise seuilBas et seuilHaut aux moustaches (×1.5 IQR) par défaut.
     * Méthode : interpolation linéaire (comme Python pandas / Excel).
     */
    public void calculerStatistiques() {
        if (valeurs.isEmpty()) {
            System.out.println("Aucune donnée à analyser.");
            return;
        }

        List<Double> triees = new ArrayList<>(valeurs);
        Collections.sort(triees); //Trie par ordre croissant

        this.min     = triees.get(0);
        this.max     = triees.get(triees.size() - 1);
        this.mediane = quartile(triees, 0.50);
        this.q1      = quartile(triees, 0.25);
        this.q3      = quartile(triees, 0.75);
        this.iqr     = q3 - q1;

        // Moustaches de Tukey ×1.5 IQR
        this.moustacheBasse = q1 - 1.5 * iqr;
        this.moustacheHaute = q3 + 1.5 * iqr;

        // Seuils par défaut = moustaches
        this.seuilBas  = moustacheBasse;
        this.seuilHaut = moustacheHaute;

        this.statistiquesCalculees = true;
    }

    /**
     * Calcule le quantile p d'une liste triée par interpolation linéaire.
     *
     * @param triees liste triée
     * @param p      quantile entre 0 et 1
     * @return la valeur du quantile
     */
    private double quartile(List<Double> triees, double p) {
        int    n        = triees.size();
        if (n == 1) return triees.get(0);
        double pos      = p * (n - 1);
        int    bas      = (int) Math.floor(pos); //Arroundi a l'inférieur
        double fraction = pos - bas;
        if (bas + 1 >= n) return triees.get(bas);
        return triees.get(bas) + fraction * (triees.get(bas + 1) - triees.get(bas));
    }

    /**
     * Importe directement des statistiques déjà calculées.
     * Utile si Q1/médiane/Q3 sont déjà disponibles.
     *
     * @param q1      premier quartile
     * @param mediane médiane
     * @param q3      troisième quartile
     */
    public void importerStatistiques(double q1, double mediane, double q3) {
        this.q1      = q1;
        this.mediane = mediane;
        this.q3      = q3;
        this.iqr     = q3 - q1;
        this.moustacheBasse = q1 - 1.5 * iqr;
        this.moustacheHaute = q3 + 1.5 * iqr;
        this.seuilBas  = moustacheBasse;
        this.seuilHaut = moustacheHaute;

        if (!valeurs.isEmpty()) {
            List<Double> triees = new ArrayList<>(valeurs);
            Collections.sort(triees);
            this.min = triees.get(0);
            this.max = triees.get(triees.size() - 1);
        }
        this.statistiquesCalculees = true;
    }

    // ── Affichage ASCII ──────────────────────────────────────────────────────

    /**
     * Affiche la boîte à moustaches en ASCII coloré, grand format.
     */
    public void afficher() {
        if (!statistiquesCalculees) {
            System.out.println(ROUGE + "✘ Statistiques non calculées. Appelez calculerStatistiques()." + RESET);
            return;
        }

        long nbOutliersHaut = valeurs.stream().filter(v -> v > moustacheHaute).count();
        long nbOutliersBas  = valeurs.stream().filter(v -> v < moustacheBasse).count();

        String inner = "═".repeat(FRAME_W - 2);
        String bord  = CYAN + "║" + RESET;
        String sep   = CYAN + "╠" + inner + "╣" + RESET;

        // ── Titre ──────────────────────────────────────────────────────────────
        System.out.println();
        System.out.println(CYAN + "╔" + inner + "╗" + RESET);
        String titreLine = "  " + CYAN_GRAS + "Boîte à moustaches — " + tronquer("'" + nomColonne + "'", 60) + RESET;
        System.out.println(bord + titreLine + padTo(titreLine, FRAME_W - 2) + bord);

        // ── Sous-titre valeurs/outliers ─────────────────────────────────────────
        String couleurOut = (nbOutliersBas + nbOutliersHaut > 0) ? ROUGE_GRAS : VERT;
        String subLine = "  " + BLANC + valeurs.size() + " valeur(s)" + RESET
                + " — " + couleurOut + nbOutliersBas + " outlier(s) bas" + RESET
                + " — " + couleurOut + nbOutliersHaut + " outlier(s) haut" + RESET;
        System.out.println(bord + subLine + padTo(subLine, FRAME_W - 2) + bord);
        System.out.println(sep);

        // ── Statistiques (2 colonnes de 4 items) ──────────────────────────────
        String[][] stats = {
            {"Min     ", String.format("%.4f", min),    "Médiane ", String.format("%.4f", mediane)},
            {"Max     ", String.format("%.4f", max),    "IQR     ", String.format("%.4f", iqr)},
            {"Q1      ", String.format("%.4f", q1),     "Q3      ", String.format("%.4f", q3)},
            {"Mous.B  ", String.format("%.4f", moustacheBasse), "Mous.H  ", String.format("%.4f", moustacheHaute)},
        };
        String[] statCols = {JAUNE, BLANC, MAGENTA, BLANC};
        for (String[] row : stats) {
            String left  = "  " + statCols[0] + row[0] + "= " + RESET + statCols[1] + row[1] + RESET;
            String mid   = "      " + statCols[2] + row[2] + "= " + RESET + statCols[3] + row[3] + RESET;
            String full  = left + mid;
            System.out.println(bord + full + padTo(full, FRAME_W - 2) + bord);
        }
        System.out.println(sep);

        // ── Bloc visuel grand format ───────────────────────────────────────────
        afficherBloc(bord, inner, sep);

        // ── Outliers liste ────────────────────────────────────────────────────
        List<Double> outliersListe = new ArrayList<>();
        for (double v : valeurs)
            if (v < moustacheBasse || v > moustacheHaute) outliersListe.add(v);
        if (!outliersListe.isEmpty()) {
            Collections.sort(outliersListe);
            String outStr = "  " + ROUGE_GRAS + "Outliers" + RESET + " : " + ROUGE
                    + tronquer(outliersListe.toString(), FRAME_W - 16) + RESET;
            System.out.println(bord + outStr + padTo(outStr, FRAME_W - 2) + bord);
        }

        // ── Seuils disponibles ─────────────────────────────────────────────────
        System.out.println(sep);
        String seuilTitre = "  " + CYAN_GRAS + "Seuils disponibles" + RESET + " (pour choisirSeuil()) :";
        System.out.println(bord + seuilTitre + padTo(seuilTitre, FRAME_W - 2) + bord);

        String[][] seuils = {
            {"[1]", "×1.5 IQR (Tukey) ", VERT,    String.format("%.4f", moustacheBasse),   String.format("%.4f", moustacheHaute)},
            {"[2]", "×2.0 IQR (souple)", VERT,    String.format("%.4f", q1-2.0*iqr),       String.format("%.4f", q3+2.0*iqr)},
            {"[3]", "×1.0 IQR (strict)", JAUNE,   String.format("%.4f", q1-1.0*iqr),       String.format("%.4f", q3+1.0*iqr)},
            {"[4]", "Seuil actuel     ", MAGENTA,  String.format("%.4f", seuilBas),         String.format("%.4f", seuilHaut)},
        };
        for (String[] s : seuils) {
            String sl = "  " + BLANC + s[0] + RESET + " " + s[1] + " : [ "
                    + s[2] + s[3] + RESET + "  ;  " + s[2] + s[4] + RESET + " ]";
            System.out.println(bord + sl + padTo(sl, FRAME_W - 2) + bord);
        }
        String perso = "  " + BLANC + "[5]" + RESET + " Personnalisé";
        System.out.println(bord + perso + padTo(perso, FRAME_W - 2) + bord);
        System.out.println(CYAN + "╚" + inner + "╝" + RESET);
    }

    /**
     * Calcule le padding nécessaire (en ignorant les séquences ANSI) pour aligner
     * une ligne à {@code targetLen} caractères visibles.
     * @return le string obtenue
     */
    private String padTo(String s, int targetLen) {
        int visible = s.replaceAll("\u001B\\[[0-9;]*m", "").length();
        int pad = Math.max(0, targetLen - visible);
        return " ".repeat(pad);
    }

    /**
     * Affiche le grand bloc visuel : seuils + boîte en 5 lignes de hauteur + axe gradué.
     * @param bord
     */
    private void afficherBloc(String bord, String inner, String sep) {
        double axeMin = Math.min(min, q1 - 2.0 * iqr);
        double axeMax = Math.max(max, q3 + 2.0 * iqr);
        if (axeMin >= axeMax) { axeMin -= 1; axeMax += 1; }

        final int D = DRAW;

        System.out.println(bord + " ".repeat(FRAME_W - 2) + bord);

        String legende = "  " + BLANC + "├─┤" + RESET + " moustache    "
                + BLEU + "╠═╣" + RESET + " boîte Q1‥Q3    "
                + VERT_GRAS + "█" + RESET + " médiane    "
                + ROUGE_GRAS + "●" + RESET + " outlier";
        System.out.println(bord + legende + padTo(legende, FRAME_W - 2) + bord);
        System.out.println(bord + " ".repeat(FRAME_W - 2) + bord);

        afficherLigneSeuil(bord, D, axeMin, axeMax, q1-1.0*iqr, q3+1.0*iqr, JAUNE,   "[3]");
        afficherLigneSeuil(bord, D, axeMin, axeMax, q1-1.5*iqr, q3+1.5*iqr, VERT,    "[1]");
        afficherLigneSeuil(bord, D, axeMin, axeMax, q1-2.0*iqr, q3+2.0*iqr, MAGENTA, "[2]");
        System.out.println(bord + " ".repeat(FRAME_W - 2) + bord);

        int pMB = posAxe(Math.max(min, moustacheBasse), axeMin, axeMax, D);
        int pQ1 = posAxe(q1,      axeMin, axeMax, D);
        int pMe = posAxe(mediane, axeMin, axeMax, D);
        int pQ3 = posAxe(q3,      axeMin, axeMax, D);
        int pMH = posAxe(Math.min(max, moustacheHaute), axeMin, axeMax, D);

        java.util.List<Integer> outBas  = new java.util.ArrayList<>();
        java.util.List<Integer> outHaut = new java.util.ArrayList<>();
        for (double v : valeurs) {
            if (v < moustacheBasse) outBas.add(posAxe(v, axeMin, axeMax, D));
            if (v > moustacheHaute) outHaut.add(posAxe(v, axeMin, axeMax, D));
        }

        afficherLigneBoite(bord, D, pMB, pQ1, pMe, pQ3, pMH, 'T', outBas, outHaut);
        afficherLigneBoite(bord, D, pMB, pQ1, pMe, pQ3, pMH, 'U', outBas, outHaut);
        afficherLigneBoite(bord, D, pMB, pQ1, pMe, pQ3, pMH, 'M', outBas, outHaut);
        afficherLigneBoite(bord, D, pMB, pQ1, pMe, pQ3, pMH, 'L', outBas, outHaut);
        afficherLigneBoite(bord, D, pMB, pQ1, pMe, pQ3, pMH, 'B', outBas, outHaut);

        System.out.println(bord + " ".repeat(FRAME_W - 2) + bord);

        String ticks   = "     " + CYAN + dessinerAxeTicks(D, axeMin, axeMax) + RESET;
        System.out.println(bord + ticks + padTo(ticks, FRAME_W - 2) + bord);
        String lblLine = "     " + dessinerAxeLabels(D, axeMin, axeMax);
        System.out.println(bord + lblLine + padTo(lblLine, FRAME_W - 2) + bord);
        System.out.println(bord + " ".repeat(FRAME_W - 2) + bord);
        System.out.println(sep);
    }

    /**
     * Dessine une ligne de la boîte à moustaches ASCII selon son rôle vertical.
     *
     * <p>Les types de lignes sont :</p>
     * <ul>
     *   <li>{@code 'T'} — ligne du dessus de la boîte (╔══╗)</li>
     *   <li>{@code 'U'} — ligne supérieure intermédiaire avec moustaches et outliers</li>
     *   <li>{@code 'M'} — ligne médiane (médiane en █, boîte ╠═╣, moustaches ├─┤)</li>
     *   <li>{@code 'L'} — ligne inférieure intermédiaire avec moustaches et outliers</li>
     *   <li>{@code 'B'} — ligne du dessous de la boîte (╚══╝)</li>
     * </ul>
     *
     * @param bord     le caractère de bord ║ coloré à répéter en début/fin
     * @param D        la largeur de la zone de dessin (DRAW)
     * @param pMB      position pixel de la moustache basse
     * @param pQ1      position pixel de Q1
     * @param pMe      position pixel de la médiane
     * @param pQ3      position pixel de Q3
     * @param pMH      position pixel de la moustache haute
     * @param type     le rôle de la ligne ('T', 'U', 'M', 'L', 'B')
     * @param outBas   liste des positions pixel des outliers bas (pour 'U')
     * @param outHaut  liste des positions pixel des outliers hauts (pour 'L')
     */
    private void afficherLigneBoite(String bord, int D,
                                     int pMB, int pQ1, int pMe, int pQ3, int pMH,
                                     char type,
                                     java.util.List<Integer> outBas,
                                     java.util.List<Integer> outHaut) {
        char[] chars = new char[D];
        int[]  types = new int[D];
        java.util.Arrays.fill(chars, ' ');

        switch (type) {
            case 'T' -> {
                for (int i = pQ1; i <= pQ3 && i < D; i++) { chars[i] = '═'; types[i] = 20; }
                if (pQ1 < D) { chars[pQ1] = '╔'; types[pQ1] = 20; }
                if (pQ3 < D) { chars[pQ3] = '╗'; types[pQ3] = 20; }
                if (pMe > pQ1 && pMe < pQ3 && pMe < D) { chars[pMe] = '╤'; types[pMe] = 30; }
            }
            case 'U', 'L' -> {
                for (int i = pMB; i < pQ1 && i < D; i++) { chars[i] = '─'; types[i] = 10; }
                for (int i = pQ3+1; i <= pMH && i < D; i++) { chars[i] = '─'; types[i] = 10; }
                if (pMB < D) { chars[pMB] = '├'; types[pMB] = 10; }
                if (pMH < D) { chars[pMH] = '┤'; types[pMH] = 10; }
                if (pQ1 < D) { chars[pQ1] = '║'; types[pQ1] = 20; }
                if (pQ3 < D) { chars[pQ3] = '║'; types[pQ3] = 20; }
                if (pMe > pQ1 && pMe < pQ3 && pMe < D) { chars[pMe] = '┊'; types[pMe] = 30; }
                java.util.List<Integer> outs = (type == 'U') ? outBas : outHaut;
                for (int p : outs) if (p >= 0 && p < D) { chars[p] = '●'; types[p] = 40; }
            }
            case 'M' -> {
                for (int i = pMB; i <= pMH && i < D; i++) { chars[i] = '─'; types[i] = 10; }
                for (int i = pQ1; i <= pQ3 && i < D; i++) { chars[i] = '═'; types[i] = 20; }
                if (pMB < D) { chars[pMB] = '├'; types[pMB] = 10; }
                if (pMH < D) { chars[pMH] = '┤'; types[pMH] = 10; }
                if (pQ1 < D) { chars[pQ1] = '╠'; types[pQ1] = 20; }
                if (pQ3 < D) { chars[pQ3] = '╣'; types[pQ3] = 20; }
                if (pMe < D) { chars[pMe] = '█'; types[pMe] = 30; }
            }
            case 'B' -> {
                for (int i = pQ1; i <= pQ3 && i < D; i++) { chars[i] = '═'; types[i] = 20; }
                if (pQ1 < D) { chars[pQ1] = '╚'; types[pQ1] = 20; }
                if (pQ3 < D) { chars[pQ3] = '╝'; types[pQ3] = 20; }
                if (pMe > pQ1 && pMe < pQ3 && pMe < D) { chars[pMe] = '╧'; types[pMe] = 30; }
            }
        }

        StringBuilder sb = new StringBuilder("     ");
        String cur = "";
        for (int i = 0; i < D; i++) {
            String col = switch (types[i]) {
                case 10 -> BLANC;
                case 20 -> BLEU;
                case 30 -> VERT_GRAS;
                case 40 -> ROUGE_GRAS;
                default -> "";
            };
            if (!col.equals(cur)) { sb.append(col); cur = col; }
            sb.append(chars[i]);
        }
        sb.append(RESET);
        String line = sb.toString();
        System.out.println(bord + line + padTo(line, FRAME_W - 2) + bord);
    }

    /**
     * Affiche une ligne de seuil colorée avec flèches gauche/droite.
     * Format : ║  [X]◄─────────────────────────────────────────────────────►  ║
     *
     * @param bordure   le caractère ║ coloré
     * @param drawWidth la largeur de la zone de dessin (DRAW)
     * @param axeMin    borne gauche de l'axe
     * @param axeMax    borne droite de l'axe
     * @param seuilG    valeur du seuil gauche
     * @param seuilD    valeur du seuil droit
     * @param couleur   code couleur ANSI
     * @param label3    étiquette de 3 caractères (ex: "[1]")
     */
    private void afficherLigneSeuil(String bord, int drawWidth,
                                     double axeMin, double axeMax,
                                     double seuilG, double seuilD,
                                     String couleur, String label3) {
        double cG = Math.max(seuilG, axeMin);
        double cD = Math.min(seuilD, axeMax);
        int pG = posAxe(cG, axeMin, axeMax, drawWidth);
        int pD = posAxe(cD, axeMin, axeMax, drawWidth);

        char[] chars = new char[drawWidth];
        java.util.Arrays.fill(chars, ' ');
        for (int i = pG; i <= pD && i < drawWidth; i++) chars[i] = '─';
        if (pG >= 0 && pG < drawWidth) chars[pG] = '◄';
        if (pD >= 0 && pD < drawWidth) chars[pD] = '►';

        String line = "  " + couleur + label3 + RESET + " " + couleur + new String(chars) + RESET;
        System.out.println(bord + line + padTo(line, FRAME_W - 2) + bord);
    }

    /**
     * Construit la ligne de ticks de l'axe X (─┬─ avec ► à droite).
     * Ticks aux positions clés : moustaches, Q1, médiane, Q3 et tous seuils.
     *
     * @param drawWidth largeur de la zone de dessin
     * @param axeMin    borne gauche de l'axe
     * @param axeMax    borne droite de l'axe
     * @return chaîne de drawWidth caractères ASCII
     */
    private String dessinerAxeTicks(int drawWidth, double axeMin, double axeMax) {
        char[] chars = new char[drawWidth];
        java.util.Arrays.fill(chars, '─');
        chars[drawWidth - 1] = '►'; // flèche terminale

        double[] ticks = {
            min, max,
            q1 - 2.0 * iqr, q1 - 1.5 * iqr, q1 - 1.0 * iqr,
            q1, mediane, q3,
            q3 + 1.0 * iqr, q3 + 1.5 * iqr, q3 + 2.0 * iqr
        };
        for (double v : ticks) {
            int p = posAxe(v, axeMin, axeMax, drawWidth);
            if (p >= 0 && p < drawWidth - 1) chars[p] = '┬';
        }
        return new String(chars);
    }

    /**
     * Construit la ligne de labels de l'axe X.
     * Place les valeurs numériques clés sous les ticks, sans chevauchement.
     *
     * @param drawWidth largeur de la zone de dessin
     * @param axeMin    borne gauche de l'axe
     * @param axeMax    borne droite de l'axe
     * @return chaîne de drawWidth caractères avec valeurs placées
     */
    private String dessinerAxeLabels(int drawWidth, double axeMin, double axeMax) {
        char[] line = new char[drawWidth];
        java.util.Arrays.fill(line, ' ');

        // Valeurs étiquetées, triées de gauche à droite
        double[] vals = {
            axeMin,
            q1 - 1.5 * iqr,  // moustache basse
            q1, mediane, q3,
            q3 + 1.5 * iqr,  // moustache haute
            axeMax
        };

        int lastEnd = -2;
        for (double v : vals) {
            if (v < axeMin - 1e-9 || v > axeMax + 1e-9) continue;
            int    p     = posAxe(v, axeMin, axeMax, drawWidth);
            String label = fmtAxe(v);
            int    start = Math.max(0, p - label.length() / 2); // centré sous tick
            if (start <= lastEnd) start = lastEnd + 2;           // évite chevauchement
            if (start + label.length() > drawWidth) continue;
            for (int i = 0; i < label.length(); i++) line[start + i] = label.charAt(i);
            lastEnd = start + label.length() - 1;
        }
        return BLANC + new String(line) + RESET;
    }

    /**
     * Version de dessinerBoiteColeur qui utilise un axe personnalisé (axeMin, axeMax)
     * au lieu de min/max des données. Permet d'aligner la boîte avec les seuils.
     *
     * @param largeur largeur de la zone de dessin
     * @param axeMin  borne gauche de l'axe
     * @param axeMax  borne droite de l'axe
     * @return chaîne colorée ANSI
     */
    private String dessinerBoiteColeurRange(int largeur, double axeMin, double axeMax) {
        if (axeMax == axeMin) return CYAN + "─".repeat(largeur) + RESET;

        int posMinMous = posAxe(Math.max(min, moustacheBasse), axeMin, axeMax, largeur);
        int posQ1      = posAxe(q1,      axeMin, axeMax, largeur);
        int posMediane = posAxe(mediane, axeMin, axeMax, largeur);
        int posQ3      = posAxe(q3,      axeMin, axeMax, largeur);
        int posMaxMous = posAxe(Math.min(max, moustacheHaute), axeMin, axeMax, largeur);

        int[]  types = new int[largeur];
        char[] chars = new char[largeur];
        java.util.Arrays.fill(chars, ' ');

        for (int i = posMinMous; i <= posMaxMous && i < largeur; i++) { chars[i] = '─'; types[i] = 1; }
        for (int i = posQ1;      i <= posQ3      && i < largeur; i++) { chars[i] = '═'; types[i] = 2; }
        if (posMinMous < largeur) { chars[posMinMous] = '├'; types[posMinMous] = 5; }
        if (posMaxMous < largeur) { chars[posMaxMous] = '┤'; types[posMaxMous] = 6; }
        if (posQ1      < largeur) { chars[posQ1]      = '╠'; types[posQ1]      = 7; }
        if (posQ3      < largeur) { chars[posQ3]      = '╣'; types[posQ3]      = 8; }
        if (posMediane < largeur) { chars[posMediane] = '█'; types[posMediane] = 3; }

        for (double v : valeurs) {
            if (v < moustacheBasse || v > moustacheHaute) {
                int pos = posAxe(v, axeMin, axeMax, largeur);
                if (pos >= 0 && pos < largeur) { chars[pos] = '●'; types[pos] = 4; }
            }
        }

        StringBuilder sb = new StringBuilder();
        String cur = "";
        for (int i = 0; i < largeur; i++) {
            String col = switch (types[i]) {
                case 1, 5, 6 -> BLANC;
                case 2, 7, 8 -> BLEU;
                case 3       -> VERT_GRAS;
                case 4       -> ROUGE_GRAS;
                default      -> "";
            };
            if (!col.equals(cur)) { sb.append(col); cur = col; }
            sb.append(chars[i]);
        }
        sb.append(RESET);
        return sb.toString();
    }

    /**
     * Mappe une valeur dans [axeMin, axeMax] vers un index de pixel dans [0, largeur-1].
     *
     * @param val     la valeur à projeter
     * @param axeMin  borne gauche
     * @param axeMax  borne droite
     * @param largeur nombre de caractères disponibles
     * @return index clampé entre 0 et largeur-1
     */
    private int posAxe(double val, double axeMin, double axeMax, int largeur) {
        if (axeMax == axeMin) return 0;
        int p = (int) Math.round((val - axeMin) / (axeMax - axeMin) * (largeur - 1));
        return Math.max(0, Math.min(largeur - 1, p));
    }

    /**
     * Formate une valeur double en chaîne courte pour l'axe.
     * Préfère les entiers, puis le minimum de décimales nécessaire (max 2).
     *
     * @param v la valeur à formater
     * @return représentation courte
     */
    private String fmtAxe(double v) {
        long arrondi = Math.round(v);
        if (Math.abs(v - arrondi) < 1e-6) return String.valueOf(arrondi);
        for (int d = 1; d <= 2; d++) {
            String s = String.format("%." + d + "f", v);
            if (Math.abs(Double.parseDouble(s) - v) < 5e-3) return s;
        }
        return String.format("%.2f", v);
    }

    /**
     * Dessine une boîte à moustaches ASCII colorée sur `largeur` caractères.
     * Utilise min/max des données comme étendue. Voir dessinerBoiteColeurRange()
     * pour une version avec étendue personnalisée.
     *
     * Couleurs : BLANC=moustaches  BLEU=boîte Q1..Q3  VERT=médiane  ROUGE=outliers
     */
    private String dessinerBoiteColeur(int largeur) {
        if (max == min) return CYAN + "─".repeat(largeur) + RESET;

        int posMinMous = normaliser(Math.max(min,  moustacheBasse), min, max, largeur);
        int posQ1      = normaliser(q1,                             min, max, largeur);
        int posMediane = normaliser(mediane,                        min, max, largeur);
        int posQ3      = normaliser(q3,                             min, max, largeur);
        int posMaxMous = normaliser(Math.min(max,  moustacheHaute), min, max, largeur);

        // 0=espace 1=moustache 2=boite 3=mediane 4=outlier 5=borneMinMous 6=borneMaxMous 7=q1 8=q3
        int[]  types = new int[largeur];
        char[] chars = new char[largeur];
        java.util.Arrays.fill(chars, ' ');

        for (int i = posMinMous; i <= posMaxMous && i < largeur; i++) { chars[i] = '─'; types[i] = 1; }
        for (int i = posQ1;      i <= posQ3      && i < largeur; i++) { chars[i] = '═'; types[i] = 2; }
        if (posMinMous < largeur) { chars[posMinMous] = '├'; types[posMinMous] = 5; }
        if (posMaxMous < largeur) { chars[posMaxMous] = '┤'; types[posMaxMous] = 6; }
        if (posQ1      < largeur) { chars[posQ1]      = '╠'; types[posQ1]      = 7; }
        if (posQ3      < largeur) { chars[posQ3]      = '╣'; types[posQ3]      = 8; }
        if (posMediane < largeur) { chars[posMediane] = '█'; types[posMediane] = 3; }

        for (double v : valeurs) {
            if (v < moustacheBasse || v > moustacheHaute) {
                int pos = normaliser(v, min, max, largeur);
                if (pos >= 0 && pos < largeur) { chars[pos] = '●'; types[pos] = 4; }
            }
        }

        StringBuilder sb = new StringBuilder();
        String currentColor = "";
        for (int i = 0; i < largeur; i++) {
            String color = switch (types[i]) {
                case 1, 5, 6 -> BLANC;
                case 2, 7, 8 -> BLEU;
                case 3       -> VERT_GRAS;
                case 4       -> ROUGE_GRAS;
                default      -> "";
            };
            if (!color.equals(currentColor)) { sb.append(color); currentColor = color; }
            sb.append(chars[i]);
        }
        sb.append(RESET);
        return sb.toString();
    }

    /**
     * Dessine une boîte à moustaches ASCII (sans couleur) sur `largeur` caractères.
     *
     * @deprecated Utiliser dessinerBoiteColeur() pour l'affichage console.
     */
    private String dessinerBoite(int largeur) {
        if (max == min) return "─".repeat(largeur);

        int posMinMous = normaliser(Math.max(min,  moustacheBasse), min, max, largeur);
        int posQ1      = normaliser(q1,                             min, max, largeur);
        int posMediane = normaliser(mediane,                        min, max, largeur);
        int posQ3      = normaliser(q3,                             min, max, largeur);
        int posMaxMous = normaliser(Math.min(max,  moustacheHaute), min, max, largeur);

        char[] ligne = new char[largeur];
        java.util.Arrays.fill(ligne, ' ');

        // Moustaches
        for (int i = posMinMous; i <= posMaxMous && i < largeur; i++) ligne[i] = '─';
        // Boîte Q1..Q3
        for (int i = posQ1; i <= posQ3 && i < largeur; i++)           ligne[i] = '═';
        // Éléments clés (ordre important : les suivants écrasent les précédents)
        if (posMinMous < largeur) ligne[posMinMous] = '├';
        if (posMaxMous < largeur) ligne[posMaxMous] = '┤';
        if (posQ1      < largeur) ligne[posQ1]      = '╠';
        if (posQ3      < largeur) ligne[posQ3]      = '╣';
        if (posMediane < largeur) ligne[posMediane] = '█';

        // Outliers en ●
        for (double v : valeurs) {
            if (v < moustacheBasse || v > moustacheHaute) {
                int pos = normaliser(v, min, max, largeur);
                if (pos >= 0 && pos < largeur) ligne[pos] = '●';
            }
        }

        return new String(ligne);
    }

    /**
     * Mappe une valeur dans [min, max] vers un index entier dans [0, largeur-1].
     * Équivalent de {@link #posAxe} mais avec min/max fixes au lieu d'axeMin/axeMax.
     *
     * @param val     la valeur à normaliser
     * @param min     borne inférieure
     * @param max     borne supérieure
     * @param largeur nombre de caractères disponibles
     * @return index clampé entre 0 et largeur-1
     */
    private int normaliser(double val, double min, double max, int largeur) {
        if (max == min) return 0;
        return (int) Math.round((val - min) / (max - min) * (largeur - 1));
    }

    /**
     * Tronque une chaîne à {@code maxLen} caractères en ajoutant "…" si nécessaire.
     *
     * @param s      la chaîne à tronquer
     * @param maxLen la longueur maximale autorisée
     * @return la chaîne originale si dans les bornes, sinon tronquée avec "…"
     */
    private String tronquer(String s, int maxLen) {
        return s.length() <= maxLen ? s : s.substring(0, maxLen - 1) + "…";
    }

    // ── Choix du seuil ───────────────────────────────────────────────────────

    /**
     * Affiche les seuils disponibles et permet à l'utilisateur d'en choisir un
     * interactivement via la console. Met à jour seuilBas et seuilHaut.
     */
    public void choisirSeuil() {
        if (!statistiquesCalculees) {
            System.out.println("Statistiques non calculées.");
            return;
        }

        afficher();
        Scanner scanner = new Scanner(System.in);
        System.out.print("\n" + CYAN_GRAS + "Choisissez un seuil [1-5] : " + RESET);

        try {
            int choix = Integer.parseInt(scanner.nextLine().trim());
            switch (choix) {
                case 1 -> {
                    seuilBas  = moustacheBasse;
                    seuilHaut = moustacheHaute;
                }
                case 2 -> {
                    seuilBas  = q1 - 2.0 * iqr;
                    seuilHaut = q3 + 2.0 * iqr;
                }
                case 3 -> {
                    seuilBas  = q1 - 1.0 * iqr;
                    seuilHaut = q3 + 1.0 * iqr;
                }
                case 4 -> { /* inchangé */ }
                case 5 -> {
                    System.out.print("Seuil bas  : ");
                    seuilBas  = Double.parseDouble(scanner.nextLine().trim());
                    System.out.print("Seuil haut : ");
                    seuilHaut = Double.parseDouble(scanner.nextLine().trim());
                }
                default -> { System.out.println("Choix invalide. Seuil inchangé."); return; }
            }
            System.out.printf(VERT_GRAS + "✔ Seuil appliqué : [" + BLANC + "%.3f" + VERT_GRAS + ", " + BLANC + "%.3f" + VERT_GRAS + "]" + RESET + "%n", seuilBas, seuilHaut);
        } catch (NumberFormatException e) {
            System.out.println("Entrée invalide. Seuil inchangé.");
        }
    }

    /**
     * Définit les seuils manuellement sans interaction console.
     *
     * @param bas  le seuil bas
     * @param haut le seuil haut
     */
    public void setSeuils(double bas, double haut) {
        this.seuilBas  = bas;
        this.seuilHaut = haut;
        System.out.printf(VERT + "✔ Seuil défini : [" + BLANC + "%.3f" + VERT + ", " + BLANC + "%.3f" + VERT + "]" + RESET + "%n", seuilBas, seuilHaut);
    }

    // ── Suppression des lignes hors seuil ────────────────────────────────────

    /**
     * Supprime du dataframe les lignes dont la valeur dans la colonne source
     * est en dehors de [seuilBas, seuilHaut].
     * Les valeurs null et non numériques sont conservées.
     *
     * @return le nombre de lignes supprimées, ou 0 si aucune
     */
    public int supprimerHorsSeuil() {
        if (df == null) {
            System.out.println("Aucun dataframe associé.");
            return 0;
        }
        if (!statistiquesCalculees) {
            System.out.println("Statistiques non calculées.");
            return 0;
        }

        int nbLignes = df.getNbLignes();
        int nbCol    = df.getNbCol();
        List<Integer> lignesValides = new ArrayList<>();

        for (int i = 0; i < nbLignes; i++) {
            try {
                Object val = df.getCase(i, indexColonne);
                if (!(val instanceof Number)) {
                    lignesValides.add(i); // null / String conservé
                } else {
                    double v = ((Number) val).doubleValue();
                    if (v >= seuilBas && v <= seuilHaut) lignesValides.add(i);
                }
            } catch (OutOfBoundException e) {
                lignesValides.add(i);
            }
        }

        int supprimees = nbLignes - lignesValides.size();
        if (supprimees == 0) {
            System.out.printf(VERT + "✔ Aucune ligne hors seuil [" + BLANC + "%.3f" + VERT + ", " + BLANC + "%.3f" + VERT + "]." + RESET + "%n", seuilBas, seuilHaut);
            return 0;
        }

        // Reconstruction du tableau
        Object[][] nouveauTableau = new Object[lignesValides.size()][nbCol];
        for (int i = 0; i < lignesValides.size(); i++) {
            int src = lignesValides.get(i);
            for (int j = 0; j < nbCol; j++) {
                try { nouveauTableau[i][j] = df.getCase(src, j); }
                catch (OutOfBoundException e) { /* ignoré */ }
            }
        }

        reconstruireDataframe(nouveauTableau, lignesValides.size(), df.getNomColonnes());
        System.out.printf(ROUGE + "✘ %d ligne(s) supprimée(s)" + RESET + " (hors [" + BLANC + "%.3f" + RESET + ", " + BLANC + "%.3f" + RESET + "])." + RESET + "%n",
                supprimees, seuilBas, seuilHaut);
        return supprimees;
    }

    // ── Remplacement par null ────────────────────────────────────────────────

    /**
     * Remplace par null les valeurs hors seuil dans la colonne source.
     * Les lignes sont conservées — seule la valeur aberrante devient null.
     *
     * @return le nombre de valeurs remplacées par null
     */
    public int remplacerHorsSeuilNull() {
        if (df == null) {
            System.out.println("Aucun dataframe associé.");
            return 0;
        }
        if (!statistiquesCalculees) {
            System.out.println("Statistiques non calculées.");
            return 0;
        }

        int remplacees = 0;
        for (int i = 0; i < df.getNbLignes(); i++) {
            try {
                Object val = df.getCase(i, indexColonne);
                if (val instanceof Number) {
                    double v = ((Number) val).doubleValue();
                    if (v < seuilBas || v > seuilHaut) {
                        df.setCase(i, indexColonne, null);
                        remplacees++;
                    }
                }
            } catch (OutOfBoundException e) { /* ignoré */ }
        }

        System.out.printf(JAUNE + "⚠ %d valeur(s) remplacée(s) par null" + RESET + " (hors [" + BLANC + "%.3f" + RESET + ", " + BLANC + "%.3f" + RESET + "])." + RESET + "%n",
                remplacees, seuilBas, seuilHaut);
        return remplacees;
    }

    // ── Utilitaires internes ─────────────────────────────────────────────────

    /**
     * Reconstruit le dataframe source avec un nouveau tableau.
     * Utilise la réflexion pour modifier les champs protégés.
     */
    private void reconstruireDataframe(Object[][] tab, int nbLignes, String[] noms) {
        try {
            var fTab   = fish.acquisition.DataframeBase.class.getDeclaredField("tableau");
            var fLig   = fish.acquisition.DataframeBase.class.getDeclaredField("nbLignes");
            var fNoms  = fish.acquisition.DataframeBase.class.getDeclaredField("nomColonne");
            fTab.setAccessible(true);
            fLig.setAccessible(true);
            fNoms.setAccessible(true);
            fTab.set(df,  tab);
            fLig.set(df,  nbLignes);
            fNoms.set(df, noms);
        } catch (Exception e) {
            System.out.println("Erreur reconstruction dataframe : " + e.getMessage());
        }
    }

    /**
     * Recherche l'index d'une colonne dont le nom contient le mot-clé donné
     * (insensible à la casse).
     *
     * @param df     le dataframe à parcourir
     * @param motCle le mot-clé à rechercher dans les noms de colonnes
     * @return l'index de la première colonne correspondante, ou -1 si aucune
     */
    private int trouverIndexColonne(DataframeComplet df, String motCle) {
        String[] noms = df.getNomColonnes();
        for (int j = 0; j < noms.length; j++) {
            if (noms[j].toLowerCase().contains(motCle.toLowerCase())) return j;
        }
        return -1;
    }

    /**
     * Tests unitaires de la classe BoiteAMoustache.
     * Vérifie l'import depuis colonne, le calcul des quartiles, la détection
     * des outliers, l'affichage, les seuils, le remplacement et la suppression.
     *
     * 
     */
    public static void main(String[] args) {
        int ok = 0, tot = 0;
        System.out.println(CYAN_GRAS + "=== Tests BoiteAMoustache ===" + RESET + "\n");

        try {
            // Données : [1..9] + outlier 100
            Object[][] data = {{1.0},{2.0},{3.0},{4.0},{5.0},
                               {6.0},{7.0},{8.0},{9.0},{100.0}};
            fish.acquisition.DfIndividu df =
                new fish.acquisition.DfIndividu(10, new String[]{"valeur"}, data);

            // Test 1 : import depuis colonne
            tot++;
            BoiteAMoustache bam = new BoiteAMoustache(df, 0);
            if (bam.getValeurs().size() == 10) {
                System.out.println("PASS Test 1 : 10 valeurs importées"); ok++;
            } else System.out.println("FAIL Test 1 : " + bam.getValeurs().size());

            // Test 2 : Q1 < médiane < Q3
            tot++;
            if (bam.getQ1() < bam.getMediane() && bam.getMediane() < bam.getQ3()) {
                System.out.printf("PASS Test 2 : Q1=%.2f Méd=%.2f Q3=%.2f IQR=%.2f%n",
                        bam.getQ1(), bam.getMediane(), bam.getQ3(), bam.getIqr());
                ok++;
            } else System.out.println("FAIL Test 2 : ordre Q1/méd/Q3 incorrect");

            // Test 3 : moustache haute < 100 (outlier détecté)
            tot++;
            if (bam.getMoustacheHaute() < 100.0) {
                System.out.printf("PASS Test 3 : moustache haute=%.2f < 100%n",
                        bam.getMoustacheHaute());
                ok++;
            } else System.out.println("FAIL Test 3 : " + bam.getMoustacheHaute());

            // Test 4 : affichage sans exception
            tot++;
            try {
                bam.afficher();
                System.out.println("PASS Test 4 : affichage sans exception");
                ok++;
            } catch (Exception e) { System.out.println("FAIL Test 4 : " + e); }

            // Test 5 : setSeuils manuels
            tot++;
            bam.setSeuils(2.0, 8.0);
            if (bam.getSeuilBas() == 2.0 && bam.getSeuilHaut() == 8.0) {
                System.out.println("PASS Test 5 : setSeuils(2.0, 8.0)"); ok++;
            } else System.out.println("FAIL Test 5");

            // Test 6 : remplacerHorsSeuilNull → [1.0, 9.0, 100.0] → null
            tot++;
            int remplaces = bam.remplacerHorsSeuilNull();
            if (remplaces == 3) {
                System.out.println("PASS Test 6 : 3 valeurs → null"); ok++;
            } else System.out.println("FAIL Test 6 : " + remplaces + " valeurs remplacées");

            // Test 7 : supprimerHorsSeuil
            tot++;
            fish.acquisition.DfIndividu df2 =
                new fish.acquisition.DfIndividu(10, new String[]{"valeur"}, data);
            BoiteAMoustache bam2 = new BoiteAMoustache(df2, 0);
            bam2.setSeuils(2.0, 8.0);
            int suppr = bam2.supprimerHorsSeuil();
            if (suppr == 3 && df2.getNbLignes() == 7) {
                System.out.println("PASS Test 7 : 3 lignes supprimées, 7 restantes"); ok++;
            } else System.out.println("FAIL Test 7 : supprimées=" + suppr
                    + ", lignes=" + df2.getNbLignes());

            // Test 8 : importerStatistiques
            tot++;
            BoiteAMoustache bam3 = new BoiteAMoustache();
            bam3.importerStatistiques(20.0, 30.0, 40.0);
            if (Math.abs(bam3.getIqr() - 20.0) < 1e-9
                    && Math.abs(bam3.getMoustacheHaute() - 70.0) < 1e-9) {
                System.out.println("PASS Test 8 : IQR=20.0, MoustacheHaute=70.0"); ok++;
            } else System.out.println("FAIL Test 8 : IQR=" + bam3.getIqr()
                    + " MH=" + bam3.getMoustacheHaute());

            // Test 9 : colonne sans données numériques
            tot++;
            fish.acquisition.DfIndividu dfStr =
                new fish.acquisition.DfIndividu(3, new String[]{"txt"},
                    new Object[][]{{"A"},{"B"},{"C"}});
            BoiteAMoustache bam4 = new BoiteAMoustache(dfStr, 0);
            if (bam4.getValeurs().isEmpty()) {
                System.out.println("PASS Test 9 : colonne String → aucune valeur"); ok++;
            } else System.out.println("FAIL Test 9");

        } catch (Exception e) {
            System.out.println("FAIL général : " + e);
            e.printStackTrace();
        }

        System.out.println("\n" + CYAN + "=== BoiteAMoustache : " + RESET
                + (ok == tot ? VERT_GRAS : ROUGE_GRAS) + ok + "/" + tot + RESET
                + CYAN + " tests réussis ===" + RESET);
    }
}
// ── Package ───────────────────────────────────────────────────

package fish.acquisition;

import fish.exceptions.NullParameterException;
import fish.exceptions.OutOfBoundException;

// ── Test : OUI ────────────────────────────────────────────────────────────
/**
 * Classe abstraite dataframe.
 * Point d'entrée public du dataframe. Hérite de toutes les couches précédentes.
 * Ajouter ici uniquement ce qui concerne la présentation des données.
 *
 * Hiérarchie :
 * DataframeBase -> DataframeColonnes -> DataframeStatistiques -> DataframeComplet
 * 
 * @see DataframeStatistiques
 * @author Jules Grenesche
 * @version 1
 */
public abstract class DataframeComplet extends DataframeStatistiques {

    // ── Couleurs ANSI ────────────────────────────────────────────────────────
    private static final String R     = "\u001B[0m";
    private static final String G     = "\u001B[1m";
    private static final String CYA   = "\u001B[36m";
    private static final String JAU   = "\u001B[33m";
    private static final String VER   = "\u001B[32m";
    private static final String ROU   = "\u001B[31m";
    private static final String MAG   = "\u001B[35m";
    private static final String CYA_G = "\u001B[1m\u001B[36m";
    private static final String VER_G = "\u001B[1m\u001B[32m";
    private static final String JAU_G = "\u001B[1m\u001B[33m";

    /** Lignes par page (pagination verticale) */
    private static final int LIGNES_PAR_PAGE = 20;
    /** Colonnes par page (pagination horizontale) */
    private static final int COLS_PAR_PAGE   = 6;

    // ── Constructeurs (délégation vers DataframeStatistiques) ─────────────────

    /**
     * Constructeur de dataframe pour les classes en héritant
     *
     * @param nbLignes   le nombre de lignes
     * @param nomColonne un tableau des entetes
     * @return un dataframe avec nb Ligne x le nombre de colonne
     */
    public DataframeComplet(int nbLignes, String[] nomColonne) {
        super(nbLignes, nomColonne);
    }

    /**
     * Constructeur de dataframe pour les classes en héritant
     *
     * @param nbLignes   le nombre de lignes
     * @param nomColonne un tableau des entetes
     * @param newtab     un tableau d'objet
     * @throws OutOfBoundException    si le tableau et le nombre ligne x colonne ne
     *                                sont pas bonnes
     * @throws NullParameterException si on s'attendait un a null
     * @return un dataframe avec nb Ligne x le nombre de colonne
     */
    public DataframeComplet(int nbLignes, String[] nomColonne, Object[][] newtab)
            throws fish.exceptions.OutOfBoundException, fish.exceptions.NullParameterException {
        super(nbLignes, nomColonne, newtab);
    }

    // ── Methode utilitaire interne────────────────────────────────────

    /**
     * Calcule la largeur maximale de chaque colonne pour un affichage aligné.
     * 
     * @param nbLignes le nombre de ligne du df que l'on veut
     *  @return le nombre de largeur de chaque colonnes
     */
    private int[] calculerLargeurs(int nbLignes) {
        int[] largeurs = new int[this.nbCol];
        for (int j = 0; j < this.nbCol; j++) {
            largeurs[j] = this.nomColonne[j].length();
            for (int i = 0; i < nbLignes; i++) {
                Object val = null;
                try { val = getCase(i, j); } catch (OutOfBoundException e) { /* ignoré */ }
                int len = (val == null) ? 4 : val.toString().length();
                if (len > largeurs[j]) largeurs[j] = len;
            }
            largeurs[j] = Math.min(largeurs[j] + 2, 22);
        }
        return largeurs;
    }

    /** Calcule les largeurs de colonne pour un sous-ensemble de colonnes. */
    private int[] calculerLargeursCol(int[] cols) {
        int[] largeurs = new int[cols.length];
        for (int k = 0; k < cols.length; k++) {
            int j = cols[k];
            largeurs[k] = this.nomColonne[j].length();
            for (int i = 0; i < this.nbLignes; i++) {
                Object val = null;
                try { val = getCase(i, j); } catch (OutOfBoundException e) { /* ignoré */ }
                int len = (val == null) ? 4 : val.toString().length();
                if (len > largeurs[k]) largeurs[k] = len;
            }
            largeurs[k] = Math.min(largeurs[k] + 2, 22);
        }
        return largeurs;
    }

    /**
     * Affiche une ligne séparatrice.
     * 
     * @param largeurs un tableau de la taille de chaque colonnes
     */
    private void afficherSeparateur(int[] largeurs) {
        StringBuilder sb = new StringBuilder(CYA + "+" + R);
        for (int l : largeurs)
            sb.append(CYA + "-".repeat(l) + "+" + R);
        System.out.println(sb);
    }

    private void afficherSeparateur(int[] largeurs, int[] cols) {
        StringBuilder sb = new StringBuilder(CYA + "+" + R);
        for (int l : largeurs)
            sb.append(CYA + "-".repeat(l) + "+" + R);
        System.out.println(sb);
    }

    /**
     * Affiche la ligne d'en-têtes.
     * 
     * @param largeurs un tableau de la taille de chaque colonnes
     */
    private void afficherLigneEntetes(int[] largeurs) {
        StringBuilder sb = new StringBuilder(CYA + "|" + R);
        for (int j = 0; j < this.nbCol; j++) {
            sb.append(CYA_G + centrer(this.nomColonne[j], largeurs[j]) + R + CYA + "|" + R);
        }
        System.out.println(sb);
    }

    private void afficherLigneEntetes(int[] largeurs, int[] cols) {
        StringBuilder sb = new StringBuilder(CYA + "|" + R);
        for (int j = 0; j < cols.length; j++) {
            sb.append(CYA_G + centrer(this.nomColonne[cols[j]], largeurs[j]) + R + CYA + "|" + R);
        }
        System.out.println(sb);
    }

    /**
     * Affiche une ligne de données.
     * 
     * @param ligne    la ligne de donné a affiché
     * @param largeurs un tableau de la taille de chaque colonnes
     */
    private void afficherLigneDonnees(int ligne, int[] largeurs) {
        StringBuilder sb = new StringBuilder(CYA + "|" + R);
        for (int j = 0; j < this.nbCol; j++) {
            Object val = null;
            try { val = getCase(ligne, j); } catch (OutOfBoundException e) { /* ignoré */ }
            String texte = (val == null) ? "null" : val.toString();
            String couleur = (val == null) ? ROU : (val instanceof Number ? JAU : "\u001B[37m");
            sb.append(couleur + centrer(tronquer(texte, largeurs[j] - 1), largeurs[j]) + R + CYA + "|" + R);
        }
        System.out.println(sb);
    }

    private void afficherLigneDonnees(int ligne, int[] largeurs, int[] cols) {
        StringBuilder sb = new StringBuilder(CYA + "|" + R);
        for (int j = 0; j < cols.length; j++) {
            Object val = null;
            try { val = getCase(ligne, cols[j]); } catch (OutOfBoundException e) { /* ignoré */ }
            String texte = (val == null) ? "null" : val.toString();
            String couleur = (val == null) ? ROU : (val instanceof Number ? JAU : "\u001B[37m");
            sb.append(couleur + centrer(tronquer(texte, largeurs[j] - 1), largeurs[j]) + R + CYA + "|" + R);
        }
        System.out.println(sb);
    }

    /**
     * Centre un texte dans une largeur donnée.
     * 
     * @param texte   le texte a centre
     * @param largeur la largeur de la colonne
     * @return Le texte centré
     */
    private String centrer(String texte, int largeur) {
        if (texte.length() >= largeur)
            return texte.substring(0, largeur);
        int gauche = (largeur - texte.length()) / 2;
        int droite = largeur - texte.length() - gauche;
        return " ".repeat(gauche) + texte + " ".repeat(droite);
    }

    /**
     * Tronque un texte à maxLen caractères avec "…" si nécessaire.
     * 
     * @param texte  le texte
     * @param maxLen la largeur maximal
     * @return Le texte tronquer
     */
    private String tronquer(String texte, int maxLen) {
        if (texte.length() <= maxLen)
            return texte;
        return texte.substring(0, maxLen - 1) + "…";
    }

    /**
     * Compte les valeurs null dans une colonne.
     * 
     * @param col le numero de la colonne
     * @return le nombre de null dans la colonne
     */
    private int compterNull(int col) {
        int count = 0;
        for (int i = 0; i < this.nbLignes; i++) {
            try {
                if (getCase(i, col) == null)
                    count++;
            } catch (OutOfBoundException e) {
                /* ignoré */ }
        }
        return count;
    }

    /**
     * Vérifie si une colonne contient au moins une valeur numérique.
     * 
     * @param col le numero de la colonne
     * @return si true la colonne a au moins un nombre sinon false
     * 
     */
    private boolean colonneEstNumerique(int col) {
        for (int i = 0; i < this.nbLignes; i++) {
            try {
                Object val = getCase(i, col);
                if (val != null)
                    return val instanceof Number;
            } catch (OutOfBoundException e) {
                /* ignoré */ }
        }
        return false;
    }
    // ── Affichage tabulaire ───────────────────────────────────────────────────

    /**
     * Affiche une fenêtre précise du dataframe (sous-ensemble lignes × colonnes).
     * Utilisée en interne par la pagination.
     *
     * @param debutL  première ligne à afficher (inclusive)
     * @param finL    dernière ligne (exclusive)
     * @param cols    indices des colonnes à afficher
     * @param pageL   numéro de page ligne (0-based)
     * @param totPL   total de pages lignes
     * @param pageC   numéro de page colonne (0-based)
     * @param totPC   total de pages colonnes
     */
    private void afficherFenetre(int debutL, int finL, int[] cols,
                                  int pageL, int totPL, int pageC, int totPC) {
        int[] largeurs = calculerLargeursCol(cols);

        // ── Bandeau titre ──────────────────────────────────────────────────
        String titrePage = String.format(" %s  [Lignes %d-%d / %d]  [Cols page %d/%d] ",
                getTitle(), debutL + 1, finL, this.nbLignes, pageC + 1, totPC);
        System.out.println();
        System.out.println(CYA_G + "╔" + "═".repeat(titrePage.length()) + "╗" + R);
        System.out.println(CYA_G + "║" + titrePage + "║" + R);
        System.out.println(CYA_G + "╚" + "═".repeat(titrePage.length()) + "╝" + R);

        // ── Tableau ────────────────────────────────────────────────────────
        afficherSeparateur(largeurs, cols);
        afficherLigneEntetes(largeurs, cols);
        afficherSeparateur(largeurs, cols);
        for (int i = debutL; i < finL; i++) {
            afficherLigneDonnees(i, largeurs, cols);
        }
        afficherSeparateur(largeurs, cols);

        // ── Légende de navigation ──────────────────────────────────────────
        System.out.println(
                VER + "[s]" + R + "uivant  " +
                VER + "[p]" + R + "récédent  " +
                (totPC > 1 ? VER + "[c]" + R + "ols→  " + VER + "[b]" + R + "ols←  " : "") +
                VER + "[q]" + R + "uitter pagination"
        );
        System.out.print(JAU_G + "► " + R);
    }

    /**
     * Lance l'affichage paginé interactif du dataframe dans le terminal.
     * Supporte la pagination verticale (lignes) et horizontale (colonnes).
     *
     * Navigation :
     *   s / Entrée = page suivante    p = page précédente
     *   c = colonnes suivantes        b = colonnes précédentes
     *   q = quitter
     *
     * @param sc le Scanner à utiliser pour lire les commandes
     */
    public void afficherAvecPagination(java.util.Scanner sc) {
        if (this.nbLignes == 0) {
            System.out.println(ROU + "Dataframe vide." + R);
            return;
        }
        int totPL = Math.max(1, (this.nbLignes + LIGNES_PAR_PAGE - 1) / LIGNES_PAR_PAGE);
        int totPC = Math.max(1, (this.nbCol   + COLS_PAR_PAGE   - 1) / COLS_PAR_PAGE);

        int pageL = 0, pageC = 0;

        while (true) {
            int debutL = pageL * LIGNES_PAR_PAGE;
            int finL   = Math.min(debutL + LIGNES_PAR_PAGE, this.nbLignes);
            int debutC = pageC * COLS_PAR_PAGE;
            int finC   = Math.min(debutC + COLS_PAR_PAGE, this.nbCol);

            int[] cols = new int[finC - debutC];
            for (int k = 0; k < cols.length; k++) cols[k] = debutC + k;

            afficherFenetre(debutL, finL, cols, pageL, totPL, pageC, totPC);

            String cmd = sc.nextLine().trim().toLowerCase();
            switch (cmd) {
                case "", "s" -> { if (pageL < totPL - 1) pageL++; else System.out.println(JAU + "Dernière page." + R); }
                case "p"     -> { if (pageL > 0)         pageL--; else System.out.println(JAU + "Première page." + R); }
                case "c"     -> { if (pageC < totPC - 1) pageC++; else System.out.println(JAU + "Dernière page de colonnes." + R); }
                case "b"     -> { if (pageC > 0)         pageC--; else System.out.println(JAU + "Première page de colonnes." + R); }
                case "q"     -> { System.out.println(VER + "Pagination terminée." + R); return; }
                default      -> System.out.println(ROU + "Commande inconnue." + R);
            }
        }
    }

    /**
     * Surcharge : crée son propre Scanner (pratique pour les appels autonomes).
     */
    public void afficherAvecPagination() {
        try (java.util.Scanner sc = new java.util.Scanner(System.in)) {
            afficherAvecPagination(sc);
        }
    }

    /**
     * Affiche les n premières lignes du dataframe sous forme de tableau.
     * Si n dépasse le nombre de lignes, affiche toutes les lignes.
     * Si n=0 on n'affiche rien.
     * Si le dataframe a plus de COLS_PAR_PAGE colonnes, la pagination
     * horizontale est automatiquement activée (une section par appel).
     *
     * @param n le nombre de lignes à afficher
     */
    public void afficherPremieresFignes(int n) {
        if (n <= 0) return;

        int nbAfficher = Math.min(n, this.nbLignes);
        int totPC = Math.max(1, (this.nbCol + COLS_PAR_PAGE - 1) / COLS_PAR_PAGE);

        for (int pageC = 0; pageC < totPC; pageC++) {
            int debutC = pageC * COLS_PAR_PAGE;
            int finC   = Math.min(debutC + COLS_PAR_PAGE, this.nbCol);
            int[] cols = new int[finC - debutC];
            for (int k = 0; k < cols.length; k++) cols[k] = debutC + k;

            int[] largeurs = calculerLargeursCol(cols);

            if (totPC > 1) {
                System.out.println(CYA_G + "── Colonnes " + (debutC + 1) + "–" + finC
                        + " / " + this.nbCol + " ──" + R);
            }
            afficherSeparateur(largeurs, cols);
            afficherLigneEntetes(largeurs, cols);
            afficherSeparateur(largeurs, cols);
            for (int i = 0; i < nbAfficher; i++) {
                afficherLigneDonnees(i, largeurs, cols);
            }
            afficherSeparateur(largeurs, cols);
        }

        if (n > this.nbLignes) {
            System.out.println(VER + "(Toutes les " + this.nbLignes + " lignes affichées)" + R);
        } else {
            System.out.println(JAU + "(" + nbAfficher + " / " + this.nbLignes + " lignes affichées)" + R);
        }
    }

    /**
     * Affiche toutes les statistiques du dataframe :
     * dimensions, nulls, valeurs uniques, et stats numériques par colonne.
     */
    public void afficherStatistiques() {
        String titre = "  STATISTIQUES — " + getTitle() + "  ";
        System.out.println(CYA_G + "╔" + "═".repeat(titre.length()) + "╗" + R);
        System.out.println(CYA_G + "║" + titre + "║" + R);
        System.out.println(CYA_G + "╠" + "═".repeat(titre.length()) + "╣" + R);
        System.out.println(CYA_G + "║" + R + "  Dimensions : " + JAU + getDimension() + R
                + " ".repeat(Math.max(0, titre.length() - 17 - getDimension().length()))
                + CYA_G + "║" + R);
        System.out.println(CYA_G + "╠" + "═".repeat(titre.length()) + "╣" + R);

        System.out.printf(CYA_G + "║" + R + "  " + G + "%-20s %-6s %-7s %-8s %-8s %-8s %-8s" + R + CYA_G + "║" + R + "%n",
                "Colonne", "Null", "Uniq.", "Moy.", "Méd.", "ÉcartT.", "Var.");
        System.out.println(CYA_G + "╠" + "═".repeat(titre.length()) + "╣" + R);

        for (int j = 0; j < this.nbCol; j++) {
            String nom   = this.nomColonne[j];
            int nbNull   = compterNull(j);
            int nbUniq   = 0;
            try { nbUniq = getUniqueCol(j); } catch (OutOfBoundException e) { /* ignoré */ }

            String nullC = nbNull > 0 ? ROU : VER;
            if (colonneEstNumerique(j)) {
                try {
                    double moy  = calculerMoyenne(j);
                    double med  = calculerMediane(j);
                    double ecar = calculerEcartType(j);
                    double var  = calculerVariance(j);
                    System.out.printf(CYA_G + "║" + R + "  %-20s " + nullC + "%-6d" + R + " %-7d "
                                    + JAU + "%-8.2f %-8.2f %-8.2f %-8.2f" + R + CYA_G + "║" + R + "%n",
                            tronquer(nom, 20), nbNull, nbUniq, moy, med, ecar, var);
                } catch (OutOfBoundException e) { /* ignoré */ }
            } else {
                System.out.printf(CYA_G + "║" + R + "  %-20s " + nullC + "%-6d" + R
                                + " %-7d " + MAG + "%-8s %-8s %-8s %-8s" + R + CYA_G + "║" + R + "%n",
                        tronquer(nom, 20), nbNull, nbUniq, "—", "—", "—", "—");
            }
        }

        if (!this.statistiques.isEmpty()) {
            System.out.println(CYA_G + "╠" + "═".repeat(titre.length()) + "╣" + R);
            System.out.println(CYA_G + "║" + R + "  " + G + "Statistiques calculées :" + R
                    + " ".repeat(Math.max(0, titre.length() - 26)) + CYA_G + "║" + R);
            for (var entry : this.statistiques.entrySet()) {
                System.out.printf(CYA_G + "║" + R + "    %-35s : " + JAU + "%.4f" + R
                                + CYA_G + "║" + R + "%n",
                        tronquer(entry.getKey(), 35), entry.getValue());
            }
        }
        System.out.println(CYA_G + "╚" + "═".repeat(titre.length()) + "╝" + R);
    }
    
        public static void main(String[] args) {
        int ok = 0, tot = 0;
        System.out.println("=== Tests DataframeComplet ===");

        try {
            // Données avec un mélange de types
            Object[][] data = {
                {"Merlan", 30.5, 200.0, 5},
                {"Hareng", 25.0, 150.0, 0},
                {"Merlan", 32.0, 210.0, 3},
                {"Thon", 80.0, null, 12},
                {null, 35.0, 180.0, null}
            };
            DfIndividu df = new DfIndividu(5, new String[]{"espece", "longueur", "poids", "nbvers"}, data);
            df.setTitre("Test DataframeComplet");

            // ── Test getTitle (avec DfIndividu) ───────────────────────────────────
            // DfIndividu.getTitle() retourne "Etude d'individu" ou "Etude d'individu : titre"
            String title = df.getTitle();
            tot++; if (title != null && title.contains("Etude d'individu")) { 
                ok++; System.out.println("PASS getTitle() = \"" + title + "\""); 
            } else { 
                System.out.println("FAIL getTitle() = \"" + title + "\""); 
            }

            // Test avec titre personnalisé
            DfIndividu df2 = new DfIndividu(1, new String[]{"x"}, new Object[][]{{1}}, "Mon titre");
            String title2 = df2.getTitle();
            tot++; if (title2 != null && title2.contains("Mon titre")) { 
                ok++; System.out.println("PASS getTitle() avec titre = \"" + title2 + "\""); 
            } else { 
                System.out.println("FAIL getTitle() avec titre = \"" + title2 + "\""); 
            }

            // ── Test affichage (vérification sans exception) ──────────────────────
            System.out.println("\n── Test affichage ────────────────────────────────────");
            try {
                df.afficherPremieresFignes(3);
                tot++; ok++; System.out.println("PASS afficherPremieresFignes(3)");
            } catch (Exception e) { System.out.println("FAIL afficherPremieresFignes : " + e); }

            try {
                df.afficherPremieresFignes(10); // plus que le nb de lignes
                tot++; ok++; System.out.println("PASS afficherPremieresFignes(10)");
            } catch (Exception e) { System.out.println("FAIL afficherPremieresFignes(10) : " + e); }

            try {
                df.afficherPremieresFignes(0); // ne doit rien afficher
                tot++; ok++; System.out.println("PASS afficherPremieresFignes(0)");
            } catch (Exception e) { System.out.println("FAIL afficherPremieresFignes(0) : " + e); }

            try {
                df.afficherStatistiques();
                tot++; ok++; System.out.println("PASS afficherStatistiques()");
            } catch (Exception e) { System.out.println("FAIL afficherStatistiques : " + e); }

            // ── Test avec dataframe vide ──────────────────────────────────────────
            DfIndividu dfVide = new DfIndividu(0, new String[]{"a", "b"});
            try {
                dfVide.afficherPremieresFignes(5);
                tot++; ok++; System.out.println("PASS afficherPremieresFignes sur df vide");
            } catch (Exception e) { System.out.println("FAIL afficherPremieresFignes sur df vide"); }

            try {
                dfVide.afficherStatistiques();
                tot++; ok++; System.out.println("PASS afficherStatistiques sur df vide");
            } catch (Exception e) { System.out.println("FAIL afficherStatistiques sur df vide"); }

            // ── Test avec une seule ligne ─────────────────────────────────────────
            Object[][] data1 = {{"Merlan", 30.5}};
            DfIndividu df1 = new DfIndividu(1, new String[]{"espece", "longueur"}, data1);
            try {
                df1.afficherPremieresFignes(1);
                tot++; ok++; System.out.println("PASS afficherPremieresFignes sur 1 ligne");
            } catch (Exception e) { System.out.println("FAIL afficherPremieresFignes sur 1 ligne"); }

            // ── Test avec des valeurs null nombreuses ─────────────────────────────
            Object[][] dataNull = {{null, null}, {null, null}};
            DfIndividu dfNull = new DfIndividu(2, new String[]{"a", "b"}, dataNull);
            try {
                dfNull.afficherPremieresFignes(2);
                tot++; ok++; System.out.println("PASS afficherPremieresFignes avec valeurs null");
            } catch (Exception e) { System.out.println("FAIL afficherPremieresFignes avec null"); }

            // ── Test avec mackerel (fichier réel qui fonctionne) ──────────────────
            System.out.println("\n── Test avec fichier mackerel.97442.csv ──────────────");
            try {
                fish.acquisition.lecture.LectureCSV lecteur = new fish.acquisition.lecture.LectureCSV(";");
                DfIndividu dfMack = lecteur.lireCSV("data/mackerel.97442.csv", DfIndividu.class);
                if (dfMack != null) {
                    dfMack.afficherPremieresFignes(3);
                    dfMack.afficherStatistiques();
                    tot++; ok++; System.out.println("PASS lecture mackerel");
                } else {
                    System.out.println("FAIL lecture mackerel (df null)");
                }
            } catch (Exception e) { System.out.println("FAIL lecture mackerel : " + e); }

        } catch (Exception e) {
            System.out.println("FAIL général : " + e);
        }

        System.out.println("\n=== DataframeComplet : " + ok + "/" + tot + " ===");
    }
}
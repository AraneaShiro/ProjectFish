// ── Package ───────────────────────────────────────────────────

package fish.acquisition;

import fish.exceptions.NullParameterException;
import fish.exceptions.OutOfBoundException;

// ── Test : NON ────────────────────────────────────────────────────────────
/**
 * Classe abstraite dataframe.
 * Point d'entrée public du dataframe. Hérite de toutes les couches précédentes.
 * Ajouter ici uniquement ce qui concerne la présentation des données.
 *
 * Hiérarchie :
 * DataframeBase -> DataframeColonnes -> DataframeStatistiques -> Dataframe
 * 
 * @see DataframeStatistiques
 * @author Jules Grenesche
 * @version 1
 */
public abstract class DataframeComplet extends DataframeStatistiques {

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
     */
    private int[] calculerLargeurs(int nbLignes) {
        int[] largeurs = new int[this.nbCol];
        for (int j = 0; j < this.nbCol; j++) { // pour chaque colonnes
            largeurs[j] = this.nomColonne[j].length(); // la largeur de base
            for (int i = 0; i < nbLignes; i++) {
                Object val = null;
                try {
                    val = getCase(i, j);
                } catch (OutOfBoundException e) {
                    /* ignoré */ }
                int len = (val == null) ? 4 : val.toString().length(); // Si val est null on met 4 sinon la taille de la
                                                                       // valeur
                if (len > largeurs[j])
                    largeurs[j] = len; // si la taille est sup a la taille de base
            }
            largeurs[j] = Math.min(largeurs[j] + 2, 22); // On prend le min entre la largeur calculé et 22 qui est
                                                         // choisie par nous meme
        }
        return largeurs;
    }

    /**
     * Affiche une ligne séparatrice.
     * 
     * @param largeurs un tableau de la taille de chaque colonnes
     */
    private void afficherSeparateur(int[] largeurs) {
        StringBuilder sb = new StringBuilder("+"); // StringBuilder est comme un string qui peut etre modifier sans cree
                                                   // un nouvelle objet
        for (int l : largeurs)
            sb.append("-".repeat(l)).append("+"); // Pour chaque largeur de colonne
        System.out.println(sb);
    }

    /**
     * Affiche la ligne d'en-têtes.
     * 
     * @param largeurs un tableau de la taille de chaque colonnes
     */
    private void afficherLigneEntetes(int[] largeurs) {
        StringBuilder sb = new StringBuilder("|");
        for (int j = 0; j < this.nbCol; j++) {
            sb.append(centrer(this.nomColonne[j], largeurs[j])).append("|");
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
        StringBuilder sb = new StringBuilder("|"); // meme idée que precedement
        for (int j = 0; j < this.nbCol; j++) {
            Object val = null;
            try {
                val = getCase(ligne, j);
            } catch (OutOfBoundException e) {
                /* ignoré */ }
            String texte = (val == null) ? "null" : val.toString();
            sb.append(centrer(tronquer(texte, largeurs[j] - 1), largeurs[j])).append("|");
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
     * Affiche les n premières lignes du dataframe sous forme de tableau.
     * Si n dépasse le nombre de lignes, affiche toutes les lignes.
     * Si n=0 on n'affiche rien
     *
     * @param n le nombre de lignes à afficher
     */
    public void afficherPremieresFignes(int n) {
        if (n > 0) {

            int nbAfficher = Math.min(n, this.nbLignes);
            int[] largeurs = calculerLargeurs(nbAfficher);

            afficherSeparateur(largeurs);
            afficherLigneEntetes(largeurs);
            afficherSeparateur(largeurs);

            for (int i = 0; i < nbAfficher; i++) {
                afficherLigneDonnees(i, largeurs);
            }
            afficherSeparateur(largeurs);

            if (n > this.nbLignes) {
                System.out.println("(Toutes les " + this.nbLignes + " lignes affichées)");
            } else {
                System.out.println("(" + nbAfficher + " / " + this.nbLignes + " lignes affichées)");
            }
        }
    }

    /**
     * Affiche toutes les statistiques du dataframe :
     * dimensions, nulls, valeurs uniques, et stats numériques par colonne.
     */
    public void afficherStatistiques() {
        System.out.println("╔══════════════════════════════════════════════════════╗");
        System.out.println("║  STATISTIQUES — " + getTitle());
        System.out.println("╠══════════════════════════════════════════════════════╣");
        System.out.println("║  Dimensions   : " + getDimension());
        System.out.println("╠══════════════════════════════════════════════════════╣");

        System.out.printf("║  %-20s %-6s %-7s %-8s %-8s %-8s %-8s%n",
                "Colonne", "Null", "Uniq.", "Moy.", "Méd.", "ÉcartT.", "Var.");
        System.out.println("╠══════════════════════════════════════════════════════╣");

        // Pour chaque colonnes
        for (int j = 0; j < this.nbCol; j++) {
            String nom = this.nomColonne[j];
            int nbNull = compterNull(j);
            int nbUniq = 0;
            try {
                nbUniq = getUniqueCol(j);
            } catch (OutOfBoundException e) {
                /* ignoré */ }

            if (colonneEstNumerique(j)) { // Si c est une colonne numérique
                try {
                    double moy = calculerMoyenne(j);
                    double med = calculerMediane(j);
                    double ecart = calculerEcartType(j);
                    double variance = calculerVariance(j);
                    // On affiche les statistiques generals
                    System.out.printf("║  %-20s %-6d %-7d %-8.2f %-8.2f %-8.2f %-8.2f%n",
                            tronquer(nom, 20), nbNull, nbUniq, moy, med, ecart, variance);
                } catch (OutOfBoundException e) {
                    /* ignoré */ }
            } else {
                System.out.printf("║  %-20s %-6d %-7d %-8s %-8s %-8s %-8s%n",
                        tronquer(nom, 20), nbNull, nbUniq, "—", "—", "—", "—"); // On affiche que les nombres de null et
                                                                                // d'unique
            }
        }

        if (!this.statistiques.isEmpty()) { // Les statisques deja calculé
            System.out.println("╠══════════════════════════════════════════════════════╣");
            System.out.println("║  Statistiques calculées :");
            for (var entry : this.statistiques.entrySet()) {
                System.out.printf("║    %-35s : %.4f%n",
                        tronquer(entry.getKey(), 35), entry.getValue());
            }
        }

        System.out.println("╚══════════════════════════════════════════════════════╝");
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

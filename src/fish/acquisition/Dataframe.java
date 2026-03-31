package fish.acquisition;

import fish.exceptions.OutOfBoundException;

/**
 * Couche 4/4 — Affichage.
 * Point d'entrée public du dataframe. Hérite de toutes les couches précédentes.
 * Ajouter ici uniquement ce qui concerne la présentation des données.
 *
 * Hiérarchie :
 *   DataframeBase → DataframeColonnes → DataframeStatistiques → Dataframe
 *
 * @author Jules Grenesche
 * @version 0.4
 */
public abstract class Dataframe extends DataframeStatistiques {

    // ── Constructeurs (délégation vers DataframeStatistiques) ─────────────────

    public Dataframe(int nbLignes, String[] nomColonne) {
        super(nbLignes, nomColonne);
    }

    public Dataframe(int nbLignes, String[] nomColonne, Object[][] newtab)
            throws fish.exceptions.OutOfBoundException, fish.exceptions.NullParameterException {
        super(nbLignes, nomColonne, newtab);
    }

    // ── Affichage tabulaire ───────────────────────────────────────────────────

    /**
     * Affiche les n premières lignes du dataframe sous forme de tableau.
     * Si n dépasse le nombre de lignes, affiche toutes les lignes.
     *
     * @param n le nombre de lignes à afficher
     */
    public void afficherPremieresFignes(int n) {
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

        for (int j = 0; j < this.nbCol; j++) {
            String nom    = this.nomColonne[j];
            int    nbNull = compterNull(j);
            int    nbUniq = 0;
            try { nbUniq = getUniqueCol(j); } catch (OutOfBoundException e) { /* ignoré */ }

            if (colonneEstNumerique(j)) {
                try {
                    double moy      = calculerMoyenne(j);
                    double med      = calculerMediane(j);
                    double ecart    = calculerEcartType(j);
                    double variance = calculerVariance(j);
                    System.out.printf("║  %-20s %-6d %-7d %-8.2f %-8.2f %-8.2f %-8.2f%n",
                            tronquer(nom, 20), nbNull, nbUniq, moy, med, ecart, variance);
                } catch (OutOfBoundException e) { /* ignoré */ }
            } else {
                System.out.printf("║  %-20s %-6d %-7d %-8s %-8s %-8s %-8s%n",
                        tronquer(nom, 20), nbNull, nbUniq, "—", "—", "—", "—");
            }
        }

        if (!this.statistiques.isEmpty()) {
            System.out.println("╠══════════════════════════════════════════════════════╣");
            System.out.println("║  Statistiques calculées :");
            for (var entry : this.statistiques.entrySet()) {
                System.out.printf("║    %-35s : %.4f%n",
                        tronquer(entry.getKey(), 35), entry.getValue());
            }
        }

        System.out.println("╚══════════════════════════════════════════════════════╝");
    }

    // ── Utilitaires privés d'affichage ────────────────────────────────────────

    /** Calcule la largeur maximale de chaque colonne pour un affichage aligné. */
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

    /** Affiche une ligne séparatrice. */
    private void afficherSeparateur(int[] largeurs) {
        StringBuilder sb = new StringBuilder("+");
        for (int l : largeurs) sb.append("-".repeat(l)).append("+");
        System.out.println(sb);
    }

    /** Affiche la ligne d'en-têtes. */
    private void afficherLigneEntetes(int[] largeurs) {
        StringBuilder sb = new StringBuilder("|");
        for (int j = 0; j < this.nbCol; j++) {
            sb.append(centrer(this.nomColonne[j], largeurs[j])).append("|");
        }
        System.out.println(sb);
    }

    /** Affiche une ligne de données. */
    private void afficherLigneDonnees(int ligne, int[] largeurs) {
        StringBuilder sb = new StringBuilder("|");
        for (int j = 0; j < this.nbCol; j++) {
            Object val = null;
            try { val = getCase(ligne, j); } catch (OutOfBoundException e) { /* ignoré */ }
            String texte = (val == null) ? "null" : val.toString();
            sb.append(centrer(tronquer(texte, largeurs[j] - 1), largeurs[j])).append("|");
        }
        System.out.println(sb);
    }

    /** Centre un texte dans une largeur donnée. */
    private String centrer(String texte, int largeur) {
        if (texte.length() >= largeur) return texte.substring(0, largeur);
        int gauche = (largeur - texte.length()) / 2;
        int droite = largeur - texte.length() - gauche;
        return " ".repeat(gauche) + texte + " ".repeat(droite);
    }

    /** Tronque un texte à maxLen caractères avec "…" si nécessaire. */
    private String tronquer(String texte, int maxLen) {
        if (texte.length() <= maxLen) return texte;
        return texte.substring(0, maxLen - 1) + "…";
    }

    /** Compte les valeurs null dans une colonne. */
    private int compterNull(int col) {
        int count = 0;
        for (int i = 0; i < this.nbLignes; i++) {
            try { if (getCase(i, col) == null) count++; }
            catch (OutOfBoundException e) { /* ignoré */ }
        }
        return count;
    }

    /** Vérifie si une colonne contient au moins une valeur numérique. */
    private boolean colonneEstNumerique(int col) {
        for (int i = 0; i < this.nbLignes; i++) {
            try {
                Object val = getCase(i, col);
                if (val != null) return val instanceof Number;
            } catch (OutOfBoundException e) { /* ignoré */ }
        }
        return false;
    }
}

package fish.acquisition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import fish.exceptions.*;

/**
 * Couche 2/4 — Manipulation de colonnes.
 * Gère la fusion, la suppression et l'analyse des valeurs uniques.
 *
 * @author Jules Grenesche
 * @version 0.4
 */
public abstract class DataframeColonnes extends DataframeBase {

    // ── Constructeurs (délégation vers DataframeBase) ─────────────────────────

    public DataframeColonnes(int nbLignes, String[] nomColonne) {
        super(nbLignes, nomColonne);
    }

    public DataframeColonnes(int nbLignes, String[] nomColonne, Object[][] newtab)
            throws OutOfBoundException, NullParameterException {
        super(nbLignes, nomColonne, newtab);
    }

    // ── Fusion / Suppression ──────────────────────────────────────────────────

    /**
     * Fusionne deux colonnes complémentaires en une seule.
     * Conditions :
     * - Les deux colonnes doivent avoir le même type de valeurs
     * - Quand l'une a une valeur, l'autre doit être null (pas de chevauchement)
     *
     * @param col1           l'index de la première colonne
     * @param col2           l'index de la deuxième colonne
     * @param nomNouvColonne le nom de la nouvelle colonne fusionnée
     * @throws OutOfBoundException si un index est invalide
     * @throws NotNullException    si les 2 colonnes sont pleines
     */
    public void fusionCol(int col1, int col2, String nomNouvColonne)
            throws OutOfBoundException, IllegalArgumentException, NotNullException {

        if (col1 < 0 || col1 >= this.nbCol) {
            throw new OutOfBoundException(0, col1, this.nbLignes, this.nbCol);
        }
        if (col2 < 0 || col2 >= this.nbCol) {
            throw new OutOfBoundException(0, col2, this.nbLignes, this.nbCol);
        }

        // ── Vérification de la complémentarité ──────────────────────────────
        for (int i = 0; i < this.nbLignes; i++) {
            Object valCol1 = this.tableau[i][col1];
            Object valCol2 = this.tableau[i][col2];
            if (valCol1 != null && valCol2 != null) {
                throw new NotNullException(i, col1, col2);
            }
        }

        // ── Construction de la colonne fusionnée ─────────────────────────────
        int nouvelleNbCol  = this.nbCol - 1;
        Object[][] nouveauTableau = new Object[this.nbLignes][nouvelleNbCol];
        String[] nouveauxNoms     = new String[nouvelleNbCol];

        int indexFusion = Math.min(col1, col2);

        // Noms de colonnes
        int curseur = 0;
        for (int j = 0; j < this.nbCol; j++) {
            if (j == indexFusion) {
                nouveauxNoms[curseur++] = nomNouvColonne;
            } else if (j != Math.max(col1, col2)) {
                nouveauxNoms[curseur++] = this.nomColonne[j];
            }
        }

        // Données fusionnées
        for (int i = 0; i < this.nbLignes; i++) {
            curseur = 0;
            for (int j = 0; j < this.nbCol; j++) {
                if (j == indexFusion) {
                    Object v1 = this.tableau[i][col1];
                    Object v2 = this.tableau[i][col2];
                    nouveauTableau[i][curseur++] = (v1 != null) ? v1 : v2;
                } else if (j != Math.max(col1, col2)) {
                    nouveauTableau[i][curseur++] = this.tableau[i][j];
                }
            }
        }

        // ── Mise à jour du dataframe ──────────────────────────────────────────
        this.tableau    = nouveauTableau;
        this.nomColonne = nouveauxNoms;
        this.nbCol      = nouvelleNbCol;
    }

    /**
     * Supprime une colonne.
     *
     * @param nCol l'index de la colonne à supprimer
     * @throws OutOfBoundException si l'index est invalide
     */
    public void supprimerColonne(int nCol) throws OutOfBoundException {
        if (nCol == -1 || nCol > this.nbCol - 1) {
            throw new OutOfBoundException(nCol, nbLignes, nbCol);
        }

        Object[][] nouveauTableau = new Object[this.nbLignes][this.nbCol - 1];
        for (int i = 0; i < this.nbLignes; i++) {
            int newCol = 0;
            for (int j = 0; j < this.nbCol; j++) {
                if (j != nCol) {
                    nouveauTableau[i][newCol++] = this.tableau[i][j];
                }
            }
        }

        String[] nouvellesColonnes = new String[this.nbCol - 1];
        int k = 0;
        for (int i = 0; i < this.nbCol; i++) {
            if (i != nCol) {
                nouvellesColonnes[k++] = this.nomColonne[i];
            }
        }

        this.tableau    = nouveauTableau;
        this.nomColonne = nouvellesColonnes;
        this.nbCol--;
    }

    // ── Valeurs uniques ───────────────────────────────────────────────────────

    /**
     * Retourne le nombre de valeurs uniques dans une colonne.
     *
     * @param col la colonne à analyser
     * @return le nombre de valeurs uniques (null inclus)
     * @throws OutOfBoundException si l'index est invalide
     */
    public int getUniqueCol(int col) throws OutOfBoundException {
        if (col < 0 || col >= this.nbCol) {
            throw new OutOfBoundException(col, this.nbLignes, this.nbCol);
        }
        List<Object> valeursUniques = new ArrayList<>();
        for (int i = 0; i < this.nbLignes; i++) {
            Object val = this.tableau[i][col];
            if (!valeursUniques.contains(val)) {
                valeursUniques.add(val);
            }
        }
        return valeursUniques.size();
    }

    /**
     * Retourne le nombre de valeurs uniques pour chaque colonne.
     *
     * @return int[] où chaque case correspond au nb de valeurs uniques
     */
    public int[] getUniqueTab() {
        int[] resultat = new int[this.nbCol];
        for (int j = 0; j < this.nbCol; j++) {
            try {
                resultat[j] = getUniqueCol(j);
            } catch (OutOfBoundException e) {
                System.out.println(e.getMessage());
            }
        }
        return resultat;
    }

    /**
     * Retourne chaque valeur unique d'une colonne avec son nombre d'apparitions.
     *
     * @param col l'index de la colonne
     * @return une Map clé=valeur unique, valeur=nb d'apparitions
     * @throws OutOfBoundException si l'index est invalide
     */
    public HashMap<Object, Integer> getUniqueColonneSomme(int col) throws OutOfBoundException {
        if (col < 0 || col >= this.nbCol) {
            throw new OutOfBoundException(0, col, this.nbLignes, this.nbCol);
        }
        HashMap<Object, Integer> occurrences = new HashMap<>();
        for (int i = 0; i < this.nbLignes; i++) {
            Object valeur = this.tableau[i][col];
            occurrences.put(valeur, occurrences.getOrDefault(valeur, 0) + 1);
        }
        return occurrences;
    }
}

package fish.acquisition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import fish.exceptions.*;

/**
 * Couche 1/4 — Attributs, constructeurs et accès aux cases.
 * Toutes les autres couches héritent de cette classe.
 *
 * @author Jules Grenesche
 * @version 0.4
 */
public abstract class DataframeBase implements fish.calcul.Statistique {

    // ── Attributs ────────────────────────────────────────────────────────────

    /** Le nombre de colonnes du dataframe */
    protected int nbCol;

    /** Noms des colonnes */
    protected String[] nomColonne;

    /** Le nombre de lignes du dataframe */
    protected int nbLignes;

    /** Tableau contenant l'ensemble des données */
    protected Object[][] tableau;

    /** HashMap des statistiques calculées */
    protected HashMap<String, Double> statistiques = new HashMap<>();

    // ── Accesseurs ───────────────────────────────────────────────────────────

    public int getNbCol()                        { return this.nbCol; }
    public int getNbLignes()                     { return this.nbLignes; }
    public Object[][] getTableau()               { return this.tableau; }
    public String[] getNomColonnes()             { return this.nomColonne; }
    public String getNomCol(int col)             { return this.nomColonne[col]; }
    public HashMap<String, Double> getStatistique() { return this.statistiques; }

    // ── Constructeurs ────────────────────────────────────────────────────────

    /**
     * Constructeur sans tableau de données.
     *
     * @param nbLignes   le nombre de lignes
     * @param nomColonne les noms des colonnes
     */
    public DataframeBase(int nbLignes, String[] nomColonne) {
        this.nbLignes    = nbLignes;
        this.nbCol       = nomColonne.length;
        this.nomColonne  = nomColonne;
        this.tableau     = new Object[nbLignes][this.nbCol];
    }

    /**
     * Constructeur avec tableau de données.
     *
     * @param nbLignes   le nombre de lignes
     * @param nomColonne les noms des colonnes
     * @param newtab     le tableau de données
     * @throws OutOfBoundException    si les dimensions ne correspondent pas
     * @throws NullParameterException si les paramètres sont vides ou null
     */
    public DataframeBase(int nbLignes, String[] nomColonne, Object[][] newtab)
            throws OutOfBoundException, NullParameterException {

        if (nbLignes == 0 || nomColonne.length == 0 || newtab == null) {
            throw new NullParameterException();
        }
        if (newtab.length != nbLignes) {
            throw new OutOfBoundException(nbLignes, newtab.length, newtab[0].length);
        }
        if (newtab[0].length != nomColonne.length) {
            throw new OutOfBoundException(nomColonne.length, newtab.length, newtab[0].length);
        }

        this.nbLignes   = nbLignes;
        this.nbCol      = nomColonne.length;
        this.nomColonne = nomColonne;
        this.tableau    = newtab;
    }

    // ── Méthode abstraite ─────────────────────────────────────────────────────

    /** Retourne le titre / type du dataframe. */
    public abstract String getTitle();

    // ── Dimensions ────────────────────────────────────────────────────────────

    /** Retourne les dimensions sous forme de tableau [nbLignes, nbCol]. */
    public int[] getSize() {
        return new int[] { this.nbLignes, this.nbCol };
    }

    /** Retourne les dimensions sous forme lisible "X lignes x Y colonnes". */
    public String getDimension() {
        return this.nbLignes + " lignes x " + this.nbCol + " colonnes";
    }

    // ── Accès aux cases ───────────────────────────────────────────────────────

    /**
     * Retourne tous les éléments d'une colonne.
     *
     * @param col l'index de la colonne
     * @return List<Object> des éléments de la colonne
     * @throws OutOfBoundException si l'index est invalide
     */
    public List<Object> getColonne(int col) throws OutOfBoundException {
        if (col < 0 || col >= this.nbCol) {
            throw new OutOfBoundException(col, this.nbLignes, this.nbCol);
        }
        List<Object> colonne = new ArrayList<>();
        for (int i = 0; i < this.nbLignes; i++) {
            colonne.add(this.tableau[i][col]);
        }
        return colonne;
    }

    /**
     * Retourne la valeur d'une case.
     *
     * @param lig la ligne
     * @param col la colonne
     * @return l'objet à cette position
     * @throws OutOfBoundException si les coordonnées sont invalides
     */
    public Object getCase(int lig, int col) throws OutOfBoundException {
        if (lig < 0 || lig >= this.nbLignes || col < 0 || col >= this.nbCol) {
            throw new OutOfBoundException(lig, col, this.nbLignes, this.nbCol);
        }
        return this.tableau[lig][col];
    }

    /**
     * Modifie la valeur d'une case.
     *
     * @param lig    la ligne
     * @param col    la colonne
     * @param newObj la nouvelle valeur
     * @return true si la modification a réussi
     * @throws OutOfBoundException si les coordonnées sont invalides
     */
    public boolean setCase(int lig, int col, Object newObj) throws OutOfBoundException {
        if (lig < 0 || lig >= this.nbLignes || col < 0 || col >= this.nbCol) {
            throw new OutOfBoundException(lig, col, this.nbLignes, this.nbCol);
        }
        this.tableau[lig][col] = newObj;
        return true;
    }
}

package fish.acquisition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import fish.acquisition.lecture.LectureCSV;
import fish.exceptions.*;
import fish.calcul.*;

/**
 * Classe abstraite représentant un tableau de données structuré
 *
 * @author Jules Grenesche
 * @version 0.3
 */
public abstract class Dataframe implements Statistique {

    ////////////////////////////// Attributs ////////////////////

    /** Le nombre de colonnes du dataframe */
    private int nbCol;

    /** Noms des colonnes */
    private String[] nomColonne;

    /** Le nombre de lignes du dataframe */
    private int nbLignes;

    /** Tableau contenant l'ensemble des données */
    private Object[][] tableau;

    ////////////////////////////// Getter / Setter

    public int getNbCol() {
        return this.nbCol;
    }

    private void setNbCol(int nbCol) {
        this.nbCol = nbCol;
    }

    public int getNbLignes() {
        return this.nbLignes;
    }

    private void setNbLignes(int nbLignes) {
        this.nbLignes = nbLignes;
    }

    public Object[][] getTableau() {
        return this.tableau;
    }

    private void setTableau(Object[][] tab) {
        this.tableau = tab;
    }

    public String[] getNomColonne() {
        return this.nomColonne;
    }

    ////////////////////////////// Constructeurs ////////////////////

    /**
     * Constructeur sans tableau de données
     *
     * @param nbLignes   le nombre de lignes
     * @param nomColonne les noms des colonnes
     */
    public Dataframe(int nbLignes, String[] nomColonne) {
        this.nbLignes = nbLignes;
        this.nbCol = nomColonne.length;
        this.nomColonne = nomColonne;
        this.tableau = new Object[nbLignes][this.nbCol];
    }

    /**
     * Constructeur avec tableau de données
     *
     * @param nbLignes   le nombre de lignes
     * @param nomColonne les noms des colonnes
     * @param newtab     le tableau de données
     * @throws OutOfBoundException    si les dimensions ne correspondent pas
     * @throws NullParameterException si les paramètres sont vides ou null
     */
    public Dataframe(int nbLignes, String[] nomColonne, Object[][] newtab)
            throws OutOfBoundException, NullParameterException {

        if (nbLignes == 0 || nomColonne.length == 0 || newtab == null) {
            throw new NullParameterException();
        }
        if (newtab.length != nbLignes) {

            throw new OutOfBoundException(
                    nbLignes, newtab.length, newtab[0].length);
        }
        if (newtab[0].length != nomColonne.length) {
            throw new OutOfBoundException(
                    nomColonne.length, newtab.length, newtab[0].length);
        }

        this.nbLignes = nbLignes;
        this.nbCol = nomColonne.length;
        this.nomColonne = nomColonne;
        this.tableau = newtab;
    }

    ////////////////////////////// Méthodes ////////////////////

    /**
     * Retourne le titre / type du dataframe
     *
     * @return une description du type de dataframe
     */
    public abstract String getTitle();

    // ───────────────────────────────── Dimension
    // ────────────────────────────────────────────────────────

    /**
     * Retourne les dimensions sous forme de tableau [nbLignes, nbCol]
     *
     * @return int[] de taille 2
     */
    public int[] getSize() {
        return new int[] { this.nbLignes, this.nbCol };
    }

    /**
     * Retourne les dimensions sous forme lisible
     *
     * @return String "X lignes x Y colonnes"
     */
    public String getDimension() {
        return this.nbLignes + " lignes x " + this.nbCol + " colonnes";
    }

    // Manipulation-du-tableau────────────────────────────────────────────────────────

    /**
     * Retourne tous les éléments d'une colonne
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
     * Retourne la valeur d'une case
     *
     * @param lig la ligne
     * @param col la colonne
     * @return l'objet à cette position, ou null si hors limites
     * @throws OutOfBoundException si les coordonnées sont invalides
     */
    public Object getCase(int lig, int col) throws OutOfBoundException {
        if (lig < 0 || lig >= this.nbLignes || col < 0 || col >= this.nbCol) {
            throw new OutOfBoundException(lig, col, this.nbLignes, this.nbCol);
        }
        return this.tableau[lig][col];
    }

    /**
     * Modifie la valeur d'une case
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

        // ── Vérification de la complémentarité ──────────────────────

        // Pour chaque ligne de chaque colonne
        for (int i = 0; i < this.nbLignes; i++) {
            Object valCol1 = this.tableau[i][col1];
            Object valCol2 = this.tableau[i][col2];

            // Les deux ont une valeur pas complémentaires
            if (valCol1 != null && valCol2 != null) {
                throw new NotNullException(i, col1, col2);
            }
        }

        // ── Construction de la colonne fusionnée et des nouvelles structures ──────

        int nouvelleNbCol = this.nbCol - 1; // On remplace 2 colonnes par 1
        Object[][] nouveauTableau = new Object[this.nbLignes][nouvelleNbCol];
        String[] nouveauxNoms = new String[nouvelleNbCol]; // Nouveau nom des labels

        // Index de la colonne fusionnée = position de col1 dans le nouveau tableau
        int indexFusion = Math.min(col1, col2);

        // Remplissage des noms de colonnes
        int curseur = 0;
        for (int j = 0; j < this.nbCol; j++) {
            if (j == indexFusion) {
                nouveauxNoms[curseur++] = nomNouvColonne; // Nouvelle colonne fusionnée
            } else if (j != Math.max(col1, col2)) {
                nouveauxNoms[curseur++] = this.nomColonne[j]; // Colonnes inchangées
            }
        }

        // Remplissage du nouveau tableau
        for (int i = 0; i < this.nbLignes; i++) {
            curseur = 0;
            for (int j = 0; j < this.nbCol; j++) {
                if (j == indexFusion) {
                    // On prend la valeur non null entre les deux colonnes
                    Object valCol1 = this.tableau[i][col1];
                    Object valCol2 = this.tableau[i][col2];
                    nouveauTableau[i][curseur++] = (valCol1 != null) ? valCol1 : valCol2;
                } else if (j != Math.max(col1, col2)) {
                    nouveauTableau[i][curseur++] = this.tableau[i][j];
                }
            }
        }

        // ── Mise à jour du dataframe ──────────────────────────────────────────────
        this.tableau = nouveauTableau;
        this.nomColonne = nouveauxNoms;
        this.nbCol = nouvelleNbCol;

    }

    // ─────────────────Unique────────────────────────────────────────────────────────

    /**
     * Retourne le nombre de valeurs uniques dans une colonne
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
            if (!valeursUniques.contains(val)) { // Si il contient deja la valeur ou non
                valeursUniques.add(val);
            }
        }
        return valeursUniques.size();
    }

    /**
     * Retourne le nombre de valeurs uniques pour chaque colonne
     *
     * @return int[] où chaque case correspond au nb de valeurs uniques de la
     *         colonne
     */
    public int[] getUniqueTab() {
        int[] resultat = new int[this.nbCol];
        for (int j = 0; j < this.nbCol; j++) {
            try {
                resultat[j] = getUniqueCol(j);
            } catch (OutOfBoundException e) { // Si on dépasse
                System.out.println(e.getMessage());
            }

        }
        return resultat;
    }

    /**
     * Retourne chaque valeur unique d'une colonne avec son nombre d'apparitions
     *
     * @param col l'index de la colonne
     * @return une Map où chaque clé est une valeur unique et la valeur est son
     *         nombre d'apparitions
     * @throws OutOfBoundException si l'index est invalide
     */
    public HashMap<Object, Integer> getUniqueColonneSomme(int col) throws OutOfBoundException {
        if (col < 0 || col >= this.nbCol) {
            throw new OutOfBoundException(0, col, this.nbLignes, this.nbCol);
        }

        HashMap<Object, Integer> occurrences = new HashMap<>();

        for (int i = 0; i < this.nbLignes; i++) {
            Object valeur = this.tableau[i][col];
            if (occurrences.containsKey(valeur)) {
                occurrences.put(valeur, occurrences.get(valeur) + 1); // On incrémente si déjà présente
            } else {
                occurrences.put(valeur, 1); // Première apparition
            }
        }

        return occurrences;
    }

    /**
     * Récupère toutes les valeurs numériques d'une colonne donnée
     *
     * @param col l'index de la colonne
     * @return liste des valeurs numériques, null ignorés
     * @throws OutOfBoundException si l'index est invalide
     */
    private List<Double> getValeursNumeriquesColonne(int col) throws OutOfBoundException {
        if (col < 0 || col >= this.nbCol) {
            throw new OutOfBoundException(0, col, this.nbLignes, this.nbCol);
        }
        List<Double> valeurs = new ArrayList<>();
        for (int i = 0; i < this.nbLignes; i++) {
            Object val = this.tableau[i][col];
            if (val instanceof Number) {
                valeurs.add(((Number) val).doubleValue());
            }
        }
        return valeurs;
    }

    // ── Statistiques ─────────────────────────────────────────────────────────────

    /**
     * Calcule la moyenne des valeurs numériques d'une colonne
     *
     * @param col l'index de la colonne
     * @return la moyenne, ou 0.0 si aucune valeur numérique
     * @throws OutOfBoundException si l'index est invalide
     */
    @Override
    public double calculerMoyenne(int col) throws OutOfBoundException {
        List<Double> valeurs = getValeursNumeriquesColonne(col);
        if (valeurs.isEmpty())
            return 0.0;

        double somme = 0.0;
        for (double v : valeurs)
            somme += v;
        return somme / valeurs.size();
    }

    /**
     * Calcule la médiane des valeurs numériques d'une colonne
     *
     * @param col l'index de la colonne
     * @return la médiane, ou 0.0 si aucune valeur numérique
     * @throws OutOfBoundException si l'index est invalide
     */
    @Override
    public double calculerMediane(int col) throws OutOfBoundException {
        List<Double> valeurs = getValeursNumeriquesColonne(col);
        if (valeurs.isEmpty())
            return 0.0;

        Collections.sort(valeurs);
        int milieu = valeurs.size() / 2;

        if (valeurs.size() % 2 == 0) {
            return (valeurs.get(milieu - 1) + valeurs.get(milieu)) / 2.0;
        }
        return valeurs.get(milieu);
    }

    /**
     * Calcule l'écart type des valeurs numériques d'une colonne
     *
     * @param col l'index de la colonne
     * @return l'écart type, ou 0.0 si aucune valeur numérique
     * @throws OutOfBoundException si l'index est invalide
     */
    @Override
    public double calculerEcartType(int col) throws OutOfBoundException {
        List<Double> valeurs = getValeursNumeriquesColonne(col);
        if (valeurs.isEmpty())
            return 0.0;

        double moyenne = calculerMoyenne(col);
        double sommeCarre = 0.0;
        for (double v : valeurs) {
            sommeCarre += Math.pow(v - moyenne, 2);
        }
        return Math.sqrt(sommeCarre / valeurs.size());
    }

    /**
     * Calcule la variance des valeurs numériques d'une colonne
     * Variance = moyenne des carrés des écarts à la moyenne
     *
     * @param col l'index de la colonne
     * @return la variance, ou 0.0 si aucune valeur numérique
     * @throws OutOfBoundException si l'index est invalide
     */
    public double calculerVariance(int col) throws OutOfBoundException {
        List<Double> valeurs = getValeursNumeriquesColonne(col);
        if (valeurs.isEmpty())
            return 0.0;

        double moyenne = calculerMoyenne(col);
        double sommeCarre = 0.0;
        for (double v : valeurs) {
            sommeCarre += Math.pow(v - moyenne, 2); // carre de la difference
        }
        return sommeCarre / valeurs.size();
    }

    /**
     * Calcule la covariance entre deux colonnes numériques
     * CoVariance = moyenne des produits des écarts à la moyenne de chaque colonne
     *
     * @param col1 l'index de la première colonne
     * @param col2 l'index de la deuxième colonne
     * @return la covariance, ou 0.0 si aucune valeur numérique
     * @throws OutOfBoundException si un index est invalide
     */
    public double calculerCoVariance(int col1, int col2) throws OutOfBoundException {
        List<Double> valeurs1 = getValeursNumeriquesColonne(col1);
        List<Double> valeurs2 = getValeursNumeriquesColonne(col2);
        if (valeurs1.isEmpty() || valeurs2.isEmpty())
            return 0.0;
        // Devrait t il etre de la meme taille ? je veux dormir
        int taille = Math.min(valeurs1.size(), valeurs2.size());
        double moyenne1 = calculerMoyenne(col1);
        double moyenne2 = calculerMoyenne(col2);

        double somme = 0.0;
        for (int i = 0; i < taille; i++) {
            somme += (valeurs1.get(i) - moyenne1) * (valeurs2.get(i) - moyenne2);
        }
        return somme / taille;
    }

    /**
     * Calcule la corrélation de Pearson entre deux colonnes numériques
     * Corrélation = CoVariance(col1, col2) / (EcartType(col1) * EcartType(col2))
     *
     * @param col1 l'index de la première colonne
     * @param col2 l'index de la deuxième colonne
     * @return le coefficient de corrélation entre -1 et 1, ou 0.0 si impossible
     * @throws OutOfBoundException si un index est invalide
     */
    @Override
    public double calculerCorrelation(int col1, int col2) throws OutOfBoundException {
        double ecartType1 = calculerEcartType(col1);
        double ecartType2 = calculerEcartType(col2);

        // Evite la division par zéro si une colonne est constante
        if (ecartType1 == 0.0 || ecartType2 == 0.0)
            return 0.0;

        return calculerCoVariance(col1, col2) / (ecartType1 * ecartType2);
    }
}
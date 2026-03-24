package fish.acquisition.lecture;

import fish.acquisition.Dataframe;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

import fish.exceptions.*;

/**
 * LectureCSV est la classe qui permet de lire un csv et le convertir dans le
 * dataframe c'est la version par defaut qui lie les fichier horizentalement
 * 
 * @author Arthur BERNARD et Jules Grenesche
 * @version 1
 *          Date : 18/03
 */

//////////// Changer l'exception
public class LectureCSV {
    ////////////////////////////// Attributs ////////////////////

    /**
     * Séparateur reconnue par le lecteur
     * 
     */
    private String separateur;

    /**
     * Les entetes
     * 
     */
    private String[] entetes;

    /**
     * Le tableau de données chargé depuis le CSV
     */
    private Object[][] tableau;

    /**
     * Le nombre de lignes du tableau
     */
    private int nbLignes;

    /**
     * Le nombre de colonnes du tableau
     */
    private int nbCol;

    ////////////////////////////// Getter / Setter

    public String[] getEntetes() {
        return this.entetes;
    }

    public Object[][] getTableau() {
        return this.tableau;
    }

    public int getNbLignes() {
        return this.nbLignes;
    }

    public int getNbCol() {
        return this.nbCol;
    }

    /**
     * Retourne les dimensions du tableau [nbLignes, nbColonnes]
     *
     * @return un int[] de taille 2 : [nbLignes, nbCol]
     */
    public int[] getSize() {
        return new int[] { this.nbLignes, this.nbCol };
    }

    private void setEntetes(String[] entete) {
        this.entetes = entete;
    }

    private void setTableau(Object[][] tab) {
        this.tableau = tab;
    }

    private void setNbLignes(int nb) {
        this.nbLignes = nb;
    }

    private void setNbCol(int nb) {
        this.nbCol = nb;
    }

    //////////////////////////////Constructor////////////////////
    /**
     * Constructeur avec séparateur personnalisé
     *
     * @param separateur le séparateur du fichier CSV
     */
    public LectureCSV(String separateur) {
        this.separateur = separateur;
    }

    /**
     * Constructeur par défaut avec virgule comme séparateur
     */
    public LectureCSV() {
        this(",");
    }

    //////////////////////////////Methodes////////////////////
    /**
     * Convertit une String en son type Java approprié :
     * Integer → Long → Double → Boolean → String
     *
     * @param valeur la valeur brute lue dans le CSV
     * @return l'objet typé correspondant, ou null si vide
     */
    private Object convertirType(String valeur) {
        if (valeur == null || valeur.isBlank())
            return null;

        String v = valeur.trim();

        // Tentative Integer
        try {
            return Integer.parseInt(v);
        } catch (NumberFormatException ignored) { // a regarder
        }

        // Tentative Long (grands entiers)
        try {
            return Long.parseLong(v);
        } catch (NumberFormatException ignored) {
        }

        // Tentative Double (virgule → point pour les CSV français)
        try {
            return Double.parseDouble(v.replace(",", "."));
        } catch (NumberFormatException ignored) {
        }

        // Tentative Boolean
        if (v.equalsIgnoreCase("true") || v.equalsIgnoreCase("false")) {
            return Boolean.parseBoolean(v);
        }

        // Sinon → String
        return v;
    }

    /**
     * Lit un fichier CSV et stocke les données dans le tableau interne
     * Les cases vides sont remplacées par null
     *
     * @param cheminFichier le chemin vers le fichier CSV
     */
    public void lireCSV(String cheminFichier) throws FileEmpty {

        List<String[]> lignesBrutes = new ArrayList<>();

        try (Scanner scanner = new Scanner(new File(cheminFichier))) { // On essaye de lire le fichier

            boolean premiereLigne = true;

            while (scanner.hasNextLine()) {
                String ligne = scanner.nextLine();

                if (ligne.trim().isEmpty())
                    continue;

                String[] valeurs = ligne.split(Pattern.quote(this.separateur), -1);

                if (premiereLigne) {
                    this.entetes = valeurs;
                    premiereLigne = false;
                } else {
                    lignesBrutes.add(valeurs);
                }
            }

        } catch (FileNotFoundException e) {
            System.out.println("Fichier introuvable : " + cheminFichier);
            return;
        }

        if (this.entetes == null) {
            throw new FileEmpty(cheminFichier);
        }

        this.nbLignes = lignesBrutes.size();
        this.nbCol = this.entetes.length;
        this.tableau = new Object[this.nbLignes][this.nbCol];

        for (int i = 0; i < this.nbLignes; i++) {
            for (int j = 0; j < this.nbCol; j++) {
                String valeur = (j < lignesBrutes.get(i).length)
                        ? lignesBrutes.get(i)[j].trim()
                        : "";
                this.tableau[i][j] = convertirType(valeur);
            }
        }
    }

    /**
     * Affiche les entêtes avec leur numéro de colonne
     * 
     * @throws NoFileLoaded si aucun fichier n'a été chargé
     */
    public void afficherEntetes() throws NoFileLoaded {
        if (this.entetes == null) {
            throw new NoFileLoaded();
        }
        for (int i = 0; i < this.entetes.length; i++) {
            System.out.println("[" + i + "] " + this.entetes[i]);
        }
    }

    /**
     * Retourne un sous-tableau avec uniquement les colonnes choisies
     *
     * @param colonnesVoulues les index des colonnes à garder
     * @return un Object[][] contenant uniquement les colonnes sélectionnées
     * @throws NoFileLoaded           si aucun fichier n'a été chargé
     * @throws NullParameterException si le parametre est null ou si il est de
     *                                taille 0
     * @throws OutOfBoundException    si un index est invalide ou si le tableau
     */
    public Object[][] selectionnerColonnes(int[] colonnesVoulues)
            throws NoFileLoaded, NullParameterException, OutOfBoundException {
        if (this.tableau == null) {
            throw new NoFileLoaded();
        }
        // Tableau de colonnes vide
        if (colonnesVoulues == null || colonnesVoulues.length == 0) {
            throw new NullParameterException();
        }
        // Vérification des index demandés
        for (int col : colonnesVoulues) {
            if (col < 0 || col >= this.nbCol) {
                throw new OutOfBoundException(
                        0, col, this.nbLignes, this.nbCol);
            }
        }
        // Construction du tableau filtré
        Object[][] resultat = new Object[this.nbLignes][colonnesVoulues.length];
        for (int i = 0; i < this.nbLignes; i++) {
            for (int j = 0; j < colonnesVoulues.length; j++) {
                resultat[i][j] = this.tableau[i][colonnesVoulues[j]];
            }
        }
        return resultat;
    }

    /**
     * Retourne tous les éléments d'une colonne dans une liste
     *
     * @param col l'index de la colonne voulue
     * @throws NoFileLoaded si aucun fichier n'a été chargé
     * @return une List<Object> contenant tous les éléments de la colonne
     */
    public List<Object> getColonne(int col) throws NoFileLoaded, OutOfBoundException {
        if (this.tableau == null) {
            throw new NoFileLoaded();
        }
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
     * Retourne tous les éléments d'une ligne dans une liste
     *
     * @param ligne l'index de la ligne voulue
     * @return une List<Object> contenant tous les éléments de la ligne
     * @throws NoFileLoaded        si aucun fichier n'a été chargé
     * @throws OutOfBoundException si l'index est invalide
     */
    public List<Object> getLigne(int ligne) throws NoFileLoaded, OutOfBoundException {
        if (this.tableau == null) {
            throw new NoFileLoaded();
        }
        if (ligne < 0 || ligne >= this.nbLignes) {
            throw new OutOfBoundException(ligne, 0, this.nbLignes, this.nbCol);
        }

        List<Object> ligneResultat = new ArrayList<>();
        for (int j = 0; j < this.nbCol; j++) {
            ligneResultat.add(this.tableau[ligne][j]);
        }
        return ligneResultat;
    }

    /**
     * Retourne un sous-tableau avec uniquement les lignes choisies
     *
     * @param lignesVoulues les index des lignes à garder
     * @return un Object[][] contenant uniquement les lignes sélectionnées
     * @throws NoFileLoaded           si aucun fichier n'a été chargé
     * @throws NullParameterException si le paramètre est null ou de taille 0
     * @throws OutOfBoundException    si un index est invalide
     */
    public Object[][] selectionnerLignes(int[] lignesVoulues)
            throws NoFileLoaded, NullParameterException, OutOfBoundException {
        if (this.tableau == null) {
            throw new NoFileLoaded();
        }
        if (lignesVoulues == null || lignesVoulues.length == 0) {
            throw new NullParameterException();
        }
        for (int lig : lignesVoulues) {
            if (lig < 0 || lig >= this.nbLignes) {
                throw new OutOfBoundException(lig, 0, this.nbLignes, this.nbCol);
            }
        }

        Object[][] resultat = new Object[lignesVoulues.length][this.nbCol];
        for (int i = 0; i < lignesVoulues.length; i++) {
            for (int j = 0; j < this.nbCol; j++) {
                resultat[i][j] = this.tableau[lignesVoulues[i]][j];
            }
        }
        return resultat;
    }
}

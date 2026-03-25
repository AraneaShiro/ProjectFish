package fish.acquisition.lecture;

import fish.acquisition.Dataframe;
import fish.acquisition.DfIndividu;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

import fish.exceptions.*;

/**
 * LectureCSV est la classe qui permet de lire un csv et le convertir dans le
 * dataframe c'est la version par defaut qui lie les fichier avec les entetes dans la 1er ligne
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
    protected String[] entetes;

    /**
     * Le nombre de lignes du tableau
     */
    protected int nbLignes;

    /**
     * Le nombre de colonnes du tableau
     */
    protected int nbCol;

    ////////////////////////////// Getter / Setter

    /**
     * Retourne les entetes
     */
    public String[] getEntetes() {
        return this.entetes;
    }

    /**
     * Retourne le separateur
     */
    public String getSeparateur() {
        return this.separateur;
    }

    /**
     * Retourne le nombre de ligne
     */
    public int getNbLignes() {
        return this.nbLignes;
    }

    /**
     * Retourne le nombre de colonne
     */
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

    /**
     * Set les entetes
     *
     * @param entetes Une liste de String des entetes
     */
    private void setEntetes(String[] entete) {
        this.entetes = entete;
    }

    /**
     * Set le nombre de ligne
     *
     * @param nb le nombre de ligne
     */
    private void setNbLignes(int nb) {
        this.nbLignes = nb;
    }

    /**
     * Set le nombre de ligne
     *
     * @param nb le nombre de colonne
     */
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
    protected Object convertirType(String valeur) {
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
     * Lit un fichier CSV et retourne une instance de la classe héritée de Dataframe
     * choisie
     *
     * @param <T>           le type héritant de Dataframe
     * @param cheminFichier le chemin vers le fichier CSV
     * @param type          la classe cible (ex: DfIndividu.class)
     * @return une instance de T construite à partir du fichier
     * @throws FileEmpty si le fichier est vide ou sans en-têtes
     */
    public <T extends Dataframe> T lireCSV(String cheminFichier, Class<T> type) throws FileEmpty {

        List<String[]> lignesBrutes = new ArrayList<>();

        try (Scanner scanner = new Scanner(new File(cheminFichier))) { // Ouverture du fichier csv

            boolean premiereLigne = true;

            while (scanner.hasNextLine()) {
                String ligne = scanner.nextLine();
                if (ligne.trim().isEmpty()) // Si la ligne est vide
                    continue;

                String[] valeurs = ligne.split(Pattern.quote(this.separateur), -1); // on découpe la ligne par le
                                                                                    // séparateur

                if (premiereLigne) {
                    this.entetes = valeurs;
                    premiereLigne = false;
                } else {
                    lignesBrutes.add(valeurs);
                }
            }

        } catch (FileNotFoundException e) {
            System.out.println("Fichier introuvable : " + cheminFichier);
            return null;
        }

        if (this.entetes == null) {
            throw new FileEmpty(cheminFichier);
        }

        this.nbLignes = lignesBrutes.size();
        this.nbCol = this.entetes.length;

        Object[][] tableau = new Object[this.nbLignes][this.nbCol];
        for (int i = 0; i < this.nbLignes; i++) {
            for (int j = 0; j < this.nbCol; j++) {
                String valeur = (j < lignesBrutes.get(i).length) ? lignesBrutes.get(i)[j].trim() : "";
                tableau[i][j] = convertirType(valeur);
            }
        }

        // Instanciation de la classe cible via réflexion
        try {
            return type.getConstructor(int.class, String[].class, Object[][].class)
                    .newInstance(this.nbLignes, this.entetes, tableau);

        } catch (Exception e) {
            System.out.println("Impossible d'instancier " + type.getSimpleName() + " : " + e.getMessage());
            return null;
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

}

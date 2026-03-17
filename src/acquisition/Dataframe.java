package acquisition;

import java.util.ArrayList;

public class Dataframe {
////////////////////////////// Attributs ////////////////////
    /**
     * Le nombre de colonnes du dataframe
     */
    private int nbCol;
    /**
     * Le nombre de lignes du dataframe
     */
    private int nbLignes;
    //A ajouter tableau du dataframe et liste des noms de colonnes

//////////////////////////////Getter / Setter////////////////////
    /**
     * Méthode get sur le nombre de colonnes
     * @return le nombre de colonnes du dataframe
     */
    public int getNbCol(){
        return this.nbCol;
    }

    /**
     * Méthode set du nombre de colonnes du dataframe
     * @param nbCol le nouveau nombre de colonnes du dataframe
     */
    private void setNbCol(int nbCol){
        this.nbCol = nbCol;
    }

    /**
     * Méthode get sur le nombre de lignes
     * @return le nombre de lignes du dataframe
     */
    public int getNbLignes(){
        return this.nbLignes;
    }

    /**
     * Méthode set du nombre de lignes du dataframe
     * @param nbLignes le nouveau nombre de lignes du dataframe
     */
    private void setNbLignes(int nbLignes){
        this.nbLignes = nbLignes;
    }
    
/////////////////////////////////Constructor////////////////////

//////////////////////////////Methodes////////////////////

}

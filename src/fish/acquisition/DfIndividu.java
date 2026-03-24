package fish.acquisition;

import fish.acquisition.*;
import fish.poisson.Population;
import fish.acquisition.lecture.*;

/**
 * Dataframe pour un csv de poisson individuel
 * 
 * @author Jules Grenesche
 * @version 0.1
 * @see Dataframe
 * 
 */
public class DfIndividu extends Dataframe {
    //////////////////////////////Attribut////////////////////
    /**
     * La population de poisson étudiée
     */
    private Population population;

    ////////////////////////////// Getter Setter

    /**
     * Méthode get la population étudier
     * 
     * @return la population
     */
    public Population getPopulation() {
        return population;
    }

    ////////////////////////////// Constructeur ////////////////////////

    /**
     * Constructeur du Dataframe sans tableau
     *
     * @param nbLignes   le nombre de lignes du tableau
     * @param nomColonne le tableau des noms des colonnes
     */
    public DfIndividu(int nbLigne, String[] entete) {
        super(nbLigne, entete);
    }

    /**
     * Constructeur du Dataframe
     *
     * @param nbLignes   le nombre de lignes du tableau
     * @param nomColonne le tableau des noms des colonnes
     * @param newtab     le tableau de données
     */
    public DfIndividu(int nbLigne, String[] entete, Object[][] newtab) {
        super(nbLigne, entete, newtab);
    }

    //////////////////////////////Methode////////////////////
    public String getTitle() {
        return "Etude Individu";
    }

}

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

    /**
     * Le type d'étude
     */
    private static String type = "Etude d'individu";

    /**
     * Le titre
     */
    private String titre;

    ////////////////////////////// Getter Setter

    /**
     * Méthode get la population étudier
     * 
     * @return la population
     */
    public Population getPopulation() {
        return population;
    }

    /**
     * Méthode get le type d'étude
     * 
     * @return un string du type d'étude
     */
    public String getType() {
        return type;
    }

    /**
     * Méthode get le titre
     * 
     * @return un string du titre
     */
    public String getTitre() {
        return titre;
    }

    /**
     * Set la population
     * 
     * @param pop la population que l'on veut mettre
     */
    private void setPopulation(Population pop) {
        this.population = pop;
    }

    /**
     * Set le titre
     * 
     * @param newTitle le titre que l on veut mettre
     */
    private void setTitre(String newTitle) {
        this.titre = newTitle;
    }

    ////////////////////////////// Constructeur ////////////////////////

    public DfIndividu(int nbLignes, String[] nomColonne) {

    }

    //////////////////////////////Methode////////////////////
    public String getTitle() {
        return this.type + ": " + this.getTitre();
    }

}

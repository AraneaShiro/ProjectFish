package fish.calcul;

import java.util.List;
import fish.exceptions.*;

/**
 * Interface de methode de calcule statistique
 * 
 * @author Jules Grenesche
 * @version 0.1
 *          Date : 18/03
 */

/**
 * Interface de méthodes de calcul statistique
 *
 * @author Jules Grenesche
 * @version 0.2
 */
public interface Statistique {

    /**
     * Calcule la moyenne des valeurs numériques d'une colonne
     *
     * @param col l'index de la colonne
     * @return la moyenne des éléments non null de la colonne
     */
    public double calculerMoyenne(int col) throws OutOfBoundException;

    /**
     * Calcule la médiane des valeurs numériques d'une colonne
     *
     * @param col l'index de la colonne
     * @return la médiane des éléments non null de la colonne
     */
    public double calculerMediane(int col) throws OutOfBoundException;

    /**
     * Calcule l'écart type des valeurs numériques d'une colonne
     *
     * @param col l'index de la colonne
     * @return l'écart type des éléments non null de la colonne
     */
    public double calculerEcartType(int col) throws OutOfBoundException;

    /**
     * Calcule la corrélation de Pearson entre deux colonnes numériques
     *
     * @param col1 l'index de la première colonne
     * @param col2 l'index de la deuxième colonne
     * @return le coefficient de corrélation entre -1 et 1
     */
    public double calculerCorrelation(int col1, int col2) throws OutOfBoundException;
}

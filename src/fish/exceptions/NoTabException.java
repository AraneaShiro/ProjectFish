package fish.exceptions;
/**
 * Exception lancée quand on veut faire des opérations sur le tableau mais qu'il n'est pas renseigné
 * @author Arthur BERNARD
 * @version 1.0
 */
public class NoTabException extends Exception{
    public NoTabException(){
        super("Le tableau d'individus de la population n'est pas renseigné !");
    }
}

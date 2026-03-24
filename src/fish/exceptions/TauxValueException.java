package fish.exceptions;
/**
 * Exception lancée quand le taux d'infestation n'est pas compris entre 0 et 1
 * @author Arthur BERNARD
 * @version 1.0
 */
public class TauxValueException extends Exception{
    public TauxValueException(){
        super("Taux non compris entre 0 et 1 !!!");
    }
}

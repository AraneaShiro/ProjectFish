package fish.exceptions;
/**
 * Exception lancée quand une valeur est négative lorsqu'elle ne devrait pas etre
 * @author Grenesche Jules
 * @version 1.0
 */
public class NegativeValueException extends Exception{
    public NegativeValueException(){
        super("Valeur négative imposible !");
    }
}

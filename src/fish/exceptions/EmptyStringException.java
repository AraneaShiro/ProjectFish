package fish.exceptions;

/**
 * Exception lancée quand un indice est supérieur à la range possible
 * @author Arthur BERNARD
 * @version 1.0
 */

public class EmptyStringException extends Exception{
    public EmptyStringException(){
        super("Le string est vide !!");
    }
}

package fish.exceptions;
/**
 * Exception lancée quand un indice est supérieur à la range possible
 * @author Arthur BERNARD
 * @version 1.0
 */
public class IndiceException extends Exception{
    public IndiceException(){
        super("Indice out of range !");
    }
}

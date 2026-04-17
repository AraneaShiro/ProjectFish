package fish.exceptions;

/**
 * Exception lancée quand une valeur doit etre un nombre mais ne l'est pas
 * @author Grenesche Jules
 * @version 1.0
 */
public class NotANumberException extends Exception {

    public NotANumberException(String val) {
        super("La valeur \"" + val + "\" n'est pas un nombre.");
    }

}

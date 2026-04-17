package fish.exceptions;

/**
 * Exception lancée quand un parametre est null mais ne doit pas l'etre
 * @author Grenesche Jules
 * @version 1.0
 */
public class NullParameterException extends Exception {
    public NullParameterException() {
        super("Parametre null non autorisé");
    }
}

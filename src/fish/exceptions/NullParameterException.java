package fish.exceptions;

public class NullParameterException extends Exception {
    public NullParameterException() {
        super("Parametre null non autorisé");
    }
}

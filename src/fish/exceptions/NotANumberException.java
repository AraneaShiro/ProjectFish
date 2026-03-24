package fish.exceptions;

public class NotANumberException extends Exception {

    public NotANumberException(String val) {
        super("La valeur \"" + val + "\" n'est pas un nombre.");
    }

}

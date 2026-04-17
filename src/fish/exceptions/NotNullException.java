package fish.exceptions;

/**
 * Exception lancée quand une des 2 valeurs n'est pas null lorsqu'elle devrait l'etre
 * @author Grenesche
 * @version 1.0
 */
public class NotNullException extends Exception {

    public NotNullException(int ligne, int col1, int col2) {
        super("Les colonnes ne sont pas complémentaires : "
                + "les deux ont une valeur à la ligne " + ligne
                + " (" + col1 + " / " + col2 + ")");
    }

}

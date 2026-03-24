package fish.exceptions;

public class NotNullException extends Exception {

    public NotNullException(int ligne, int col1, int col2) {
        super("Les colonnes ne sont pas complémentaires : "
                + "les deux ont une valeur à la ligne " + ligne
                + " (" + col1 + " / " + col2 + ")");
    }

}

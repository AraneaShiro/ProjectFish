package fish.exceptions;

public class OutOfBoundException extends Exception {

    public OutOfBoundException(int row, int col, int maxRow, int maxCol) {
        super("Coordonnées (" + row + ", " + col + ") hors limites. "
                + "Le tableau accepte des indices entre [0, " + (maxRow - 1) + "] "
                + "et [0, " + (maxCol - 1) + "].");
    }

    public OutOfBoundException(int col, int maxRow, int maxCol) {
        super("Index (" + col + ") hors limites. "
                + "Le tableau accepte des indices entre [0, " + (maxRow - 1) + "] "
                + "et [0, " + (maxCol - 1) + "].");
    }

    public OutOfBoundException(String message) {
        super(message);
    }
}
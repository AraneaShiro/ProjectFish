package fish.exceptions;

public class WrongPathException extends Exception {

    public WrongPathException(String path) {
        super("Mauvais chemin de fichier" + path);
    }

}

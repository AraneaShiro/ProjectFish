package fish.exceptions;

/**
 * Exception lancée quand le chemin d'un fichier n'est pas correcte
 * @author Grenesche Jules
 * @version 1.0
 */
public class WrongPathException extends Exception {

    public WrongPathException(String path) {
        super("Mauvais chemin de fichier" + path);
    }

}

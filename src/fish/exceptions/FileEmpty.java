package fish.exceptions;
/**
 * Exception lancée quand le fichier donnée est vide
 * @author Grenesche Jules
 * @version 1.0
 */
public class FileEmpty extends Exception {
    public FileEmpty(String cheminFichier) {
        super("Le fichier est vide : " + cheminFichier);
    }
}

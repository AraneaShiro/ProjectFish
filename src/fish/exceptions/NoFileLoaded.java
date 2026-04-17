package fish.exceptions;
/**
 * Exception lancée quand on veut faire des opérations qui necessite le chargement d'un fichier qui n'est pas fait
 * au préalable
 * @author Grenesche Jules
 * @version 1.0
 */
public class NoFileLoaded extends Exception {
    public NoFileLoaded() {
        super("Pas de fichier chargé.");
    }
}

package fish.exceptions;
/**
 * Exception lancée quand le nombre de vers n'est pas connu
 * @author Arthur BERNARD
 * @version 1.0
 */
public class NoNbVersException extends Exception {
    public NoNbVersException(){
        super("Nombre de vers non renseigné !");
    }
}

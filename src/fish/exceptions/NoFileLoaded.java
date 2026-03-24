package fish.exceptions;

public class NoFileLoaded extends Exception {
    public NoFileLoaded() {
        super("Pas de fichier chargé.");
    }
}

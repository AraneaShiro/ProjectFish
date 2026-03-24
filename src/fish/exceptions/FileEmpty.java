package fish.exceptions;

public class FileEmpty extends Exception {
    public FileEmpty(String cheminFichier) {
        super("Le fichier est vide : " + cheminFichier);
    }
}

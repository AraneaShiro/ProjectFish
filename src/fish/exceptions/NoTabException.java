package fish.exceptions;

public class NoTabException extends Exception{
    public NoTabException(){
        super("Le tableau d'individus de la population n'est pas renseigné !");
    }
}

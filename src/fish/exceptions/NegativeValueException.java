package fish.exceptions;

public class NegativeValueException extends Exception{
    public NegativeValueException(){
        super("Valeur négative imposible !");
    }
}

package fish.visualisation;
import javafx.scene.control.Label;
import javafx.scene.Node;

/**
 * Classe permettant de gérer les label utilisés dans les différentes classes de l'application
 * @author Arthur BERNARD
 * @version 0.1
 */

public class AppLabel extends Label{

    // ================ Constructeurs ===============
    /**
     * Constructeur sans paramètre pour créer un label vide
     */
    public AppLabel(){
        super();
    }


    /**
     * Constructeur à un paramètre pour créer un label avec le texte en paramètre
     * @param text le texte à afficher
     */
    public AppLabel(String text){
        super(text);
    }


    /**
     * Constructeur à 2 paramètres pour créer un label avec le graphic et le texte en paramètre
     * @param text le texte à afficher
     * @param graphic le node du label
     */
    public AppLabel(String text, Node graphic){
        super(text, graphic);
    }
}

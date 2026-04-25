package fish.visualisation;

import javafx.scene.control.Button;
import javafx.scene.Node;

/**
 * Classe permettant de gérer les boutons utilisés dans les différentes classes de l'application.

 * @author Arthur BERNARD
 * @version 0.1
 */
public class AppButton extends Button {

    // ================ Constructeurs ===============

    /**
     * Constructeur sans paramètre — bouton vide
     */
    public AppButton() {
        super();
    }

    /**
     * Constructeur à un paramètre — bouton avec texte
     * @param text le texte à afficher sur le bouton
     */
    public AppButton(String text) {
        super(text);
    }

    /**
     * Constructeur à deux paramètres — bouton avec texte et icône
     * @param text    le texte à afficher
     * @param graphic le node (icône) du bouton
     */
    public AppButton(String text, Node graphic) {
        super(text, graphic);
    }
}

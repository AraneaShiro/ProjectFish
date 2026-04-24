package fish.visualisation;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Classe pour afficher le titre de l'application
 *
 * @author Arthur BERNARD
 * @version 0.2
 */
public class TitlePane extends VBox {

    // ========= Attributs ===========
    private Scene    scene;
    private AppLabel titleLabel;

    // ========== Constructeur ===========
    /**
     * Constructeur du panel titre
     * @param scene la scène principale
     * @param title le texte du titre
     */
    public TitlePane(Scene scene, String title) {
        this.scene = scene;

        // Label
        titleLabel = new AppLabel(title);
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 26));
        titleLabel.setTextFill(Color.WHITE);

        // Conteneur
        this.getChildren().add(titleLabel);
        this.setAlignment(Pos.CENTER);
        this.setPadding(new Insets(10));
        this.setStyle("-fx-background-color: #2c3e50;");
    }
}
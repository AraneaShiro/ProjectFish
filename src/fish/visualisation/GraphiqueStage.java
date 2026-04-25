package fish.visualisation;

import fish.acquisition.DataframeComplet;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Fenêtre secondaire affichant les graphiques d'un DataframeComplet.
 * Permet à l'utilisateur de choisir :
 *   - les colonnes X et Y pour le nuage de points + régression
 *   - la colonne numérique pour l'histogramme
 * 
 * @author Arthur BERNARD
 * @version 0.3
 */
public class GraphiqueStage extends Stage {

    // ========= Attributs ===========
    private DataframeComplet df; // Les données à afficher
    private ComboBox<String> comboColX; // Menu pour choisir la colonne X
    private ComboBox<String> comboColY;// Menu pour choisir la colonne Y
    private ComboBox<String> comboColHistogramme; // Menu pour choisir la colonne de l'histogramme
    private AppButton btnGenerer; // Bouton pour générer les graphiques
    private ScrollPane scrollZoneGraphiques; // Zone défilante pour les graphiques
    private VBox root; // Conteneur principal

    // ========== Constructeur ===========
    
    /**
     * Crée et affiche la fenêtre de graphiques pour le dataframe donné.
     * @param df le dataframe à visualiser
     */
    public GraphiqueStage(DataframeComplet df) {
        this.df = df;
        this.setTitle("Graphiques — " + df.getTitle());
        buildUI(); // On construit l'interface
        addListeners();// On ajoute les écouteurs d'événements
        
        // quand la fenêtre s'affiche, on génère les graphiques automatiquement
        this.setOnShown(e -> genererGraphiques());
        
        Scene scene = new Scene(root, 900, 850);
        this.setScene(scene);
        this.show(); // On affiche la fenêtre
    }
    
    /**
     * Cette méthode permet de rafraîchir les graphiques avec les nouvelles données
     * sans avoir à fermer et rouvrir la fenêtre.
     * @param nouveauDf le nouveau dataframe (après nettoyage/complétion)
     */
    public void mettreAJourDataframe(DataframeComplet nouveauDf) {
        // On remplace l'ancien dataframe par le nouveau
        this.df = nouveauDf;
        
        // On met à jour le titre de la fenêtre
        this.setTitle("Graphiques — " + df.getTitle());
        
        // On récupère les anciennes sélections pour les conserver si possible
        String ancienneX = comboColX.getValue();
        String ancienneY = comboColY.getValue();
        String ancienneHisto = comboColHistogramme.getValue();
        
        // On récupère les nouveaux noms de colonnes
        String[] noms = df.getNomColonnes();
        
        // On vide les menus déroulants
        comboColX.getItems().clear();
        comboColY.getItems().clear();
        comboColHistogramme.getItems().clear();
        
        // On remplit les menus avec les nouveaux noms de colonnes
        comboColX.getItems().addAll(noms);
        comboColY.getItems().addAll(noms);
        comboColHistogramme.getItems().addAll(noms);
        
        // On restaure les sélections si possible
        // Pour la colonne X
        if (comboColX.getItems().contains(ancienneX)) {
            comboColX.setValue(ancienneX);
        } else if (noms.length > 1) {
            comboColX.setValue(noms[1]); // Sinon on prend la 2ème colonne
        } else {
            comboColX.setValue(noms[0]); // Sinon la 1ère
        }
        
        // Pour la colonne Y
        if (comboColY.getItems().contains(ancienneY)) {
            comboColY.setValue(ancienneY);
        } else if (noms.length > 2) {
            comboColY.setValue(noms[2]);
        } else {
            comboColY.setValue(noms[0]);
        }
        
        // Pour l'histogramme
        if (comboColHistogramme.getItems().contains(ancienneHisto)) {
            comboColHistogramme.setValue(ancienneHisto);
        } else if (noms.length > 1) {
            comboColHistogramme.setValue(noms[1]);
        } else {
            comboColHistogramme.setValue(noms[0]);
        }
        
        // On régénère les graphiques avec les nouvelles données
        genererGraphiques();
    }

    // ========== Construction de l'interface ===========
    /**
     * Construit les composants graphiques de la fenêtre.
     */
    private void buildUI() {
        root = new VBox(15);
        root.setPadding(new Insets(20));

        // Titre
        AppLabel titre = new AppLabel("Visualisation : " + df.getTitle());
        titre.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // Sélecteurs pour le nuage de points
        AppLabel labelScatter = new AppLabel("Nuage de points — choisir les colonnes :");
        labelScatter.setStyle("-fx-font-weight: bold;");
        String[] noms = df.getNomColonnes();
        
        comboColX = new ComboBox<>();
        comboColX.getItems().addAll(noms);
        comboColX.setValue(noms.length > 1 ? noms[1] : noms[0]);
        
        comboColY = new ComboBox<>();
        comboColY.getItems().addAll(noms);
        comboColY.setValue(noms.length > 2 ? noms[2] : noms[0]);
        
        HBox ligneScatter = new HBox(10,
            new AppLabel("X :"), comboColX,
            new AppLabel("Y :"), comboColY
        );
        ligneScatter.setAlignment(Pos.CENTER_LEFT);

        // Sélecteurs pour l'histogramme
        AppLabel labelHisto = new AppLabel("Histogramme — choisir la colonne numérique :");
        labelHisto.setStyle("-fx-font-weight: bold;");

        comboColHistogramme = new ComboBox<>();
        comboColHistogramme.getItems().addAll(noms);
        comboColHistogramme.setValue(noms.length > 1 ? noms[1] : noms[0]);

        HBox ligneHisto = new HBox(10,
            new AppLabel("Colonne à analyser :"), comboColHistogramme
        );
        ligneHisto.setAlignment(Pos.CENTER_LEFT);
        
        // Petit texte explicatif
        Label lblInfo = new Label("L'histogramme regroupe automatiquement les valeurs en tranches (bins)");
        lblInfo.setStyle("-fx-text-fill: #666; -fx-font-size: 11px;");

        // Bouton générer
        btnGenerer = new AppButton("Générer les graphiques");

        // Zone de graphiques (scrollable)
        scrollZoneGraphiques = new ScrollPane();
        scrollZoneGraphiques.setFitToWidth(true);
        scrollZoneGraphiques.setPrefHeight(600);

        // Assemblage
        root.getChildren().addAll(
            titre,
            labelScatter, ligneScatter,
            labelHisto, ligneHisto, lblInfo,
            btnGenerer,
            scrollZoneGraphiques
        );
    }

    // ========== Gestion des événements ===========
    /**
     * Enregistre les écouteurs sur les composants de la fenêtre.
     */
    private void addListeners() {
        btnGenerer.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                genererGraphiques();
            }
        });
    }

    /**
     * Récupère les index des colonnes sélectionnées et génère le GraphiquePane.
     */
    private void genererGraphiques() {
        String[] noms = df.getNomColonnes();
        int colX = trouverIndex(noms, comboColX.getValue());
        int colY = trouverIndex(noms, comboColY.getValue());
        int colHistogramme = trouverIndex(noms, comboColHistogramme.getValue());
        
        // Vérification : X et Y doivent être différentes pour le scatter
        if (colX == colY) {
            scrollZoneGraphiques.setContent(
                new AppLabel("Les colonnes X et Y du nuage de points doivent être différentes.")
            );
            return;
        }
        
        // On crée le panneau des graphiques avec les colonnes choisies
        GraphiquePane graphiquePane = new GraphiquePane(df, colX, colY, colHistogramme);
        scrollZoneGraphiques.setContent(graphiquePane);
    }

    /**
     * Retourne l'index d'un nom de colonne dans le tableau des noms.
     * @param noms le tableau des noms de colonnes
     * @param valeur le nom recherché
     * @return l'index, ou 0 si non trouvé
     */
    private int trouverIndex(String[] noms, String valeur) {
        for (int i = 0; i < noms.length; i++) {
            if (noms[i].equals(valeur)) return i;
        }
        return 0;
    }
}
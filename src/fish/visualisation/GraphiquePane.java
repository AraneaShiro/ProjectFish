package fish.visualisation;

import fish.acquisition.DataframeComplet;
import fish.exceptions.OutOfBoundException;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Panneau qui affiche deux graphiques :
 * 1. Un nuage de points avec une droite de régression
 * 2. Un histogramme (distribution des valeurs)
 * 
 * @author Arthur BERNARD
 * @version 0.1
 */
public class GraphiquePane extends VBox {

    // ========== ATTRIBUTS ==========
    
    private final DataframeComplet df; // Les données à afficher
    private final int colX; // Colonne pour l'axe X du nuage
    private final int colY; // Colonne pour l'axe Y du nuage
    private final int colHistogramme; // Colonne pour l'histogramme

    // ========== CONSTRUCTEUR ==========
    
    /**
     * Crée le panneau avec les graphiques.
     * @param df Les données
     * @param colX Colonne pour l'axe X
     * @param colY Colonne pour l'axe Y
     * @param colHistogramme Colonne pour l'histogramme
     */
    public GraphiquePane(DataframeComplet df, int colX, int colY, int colHistogramme) {
        this.df = df;
        this.colX = colX;
        this.colY = colY;
        this.colHistogramme = colHistogramme;
        construireInterface();
    }

    // ========== CONSTRUCTION DE L'INTERFACE ==========
    
    /**
     * Construit l'interface avec les deux graphiques.
     */
    private void construireInterface() {
        this.setSpacing(20);               // Espace entre les graphiques
        this.setPadding(new Insets(20));   // Marges autour
        this.setAlignment(Pos.TOP_CENTER); // Aligné en haut au centre
        
        // Ajoute les deux graphiques (chacun avec sa légende)
        this.getChildren().addAll(
            construireNuagePoints(),
            construireHistogramme()
        );
    }

    // ==================== NUAGE DE POINTS ====================
    
    /**
     * Construit le panneau du nuage de points (graphique + légende).
     * La formule de régression est affichée en superposition sur le graphique.
     */
    private VBox construireNuagePoints() {
        VBox panneau = new VBox(5);
        panneau.setAlignment(Pos.TOP_CENTER);

        // Label pour la formule — rempli par creerNuagePoints()
        Label lblFormule = new Label();
        lblFormule.setStyle(
            "-fx-background-color: rgba(255,255,255,0.82);" +
            "-fx-border-color: #e74c3c;" +
            "-fx-border-radius: 4px;" +
            "-fx-background-radius: 4px;" +
            "-fx-padding: 4 8 4 8;" +
            "-fx-text-fill: #c0392b;" +
            "-fx-font-size: 12px;" +
            "-fx-font-weight: bold;"
        );

        // Le graphique + label superposé dans un StackPane
        ScatterChart<Number, Number> graphique = creerNuagePoints(lblFormule);
        StackPane stack = new StackPane(graphique, lblFormule);
        StackPane.setAlignment(lblFormule, Pos.TOP_RIGHT);
        StackPane.setMargin(lblFormule, new Insets(40, 20, 0, 0));

        // La légende en dessous
        HBox legende = creerLegendeNuagePoints();

        panneau.getChildren().addAll(stack, legende);
        return panneau;
    }
    
    /**
     * Crée le nuage de points avec la droite de régression.
     * Renseigne lblFormule avec l'équation calculée.
     */
    private ScatterChart<Number, Number> creerNuagePoints(Label lblFormule) {
        // Récupère les noms des colonnes pour les axes
        String nomX = df.getNomCol(colX);
        String nomY = df.getNomCol(colY);

        // Crée les axes
        NumberAxis axeX = new NumberAxis();
        NumberAxis axeY = new NumberAxis();
        axeX.setLabel(nomX);
        axeY.setLabel(nomY);

        // Crée le graphique
        ScatterChart<Number, Number> graphique = new ScatterChart<>(axeX, axeY);
        graphique.setTitle("Nuage de points : " + nomX + " vs " + nomY);
        graphique.setPrefHeight(350);
        graphique.setLegendVisible(false);  // On cache la légende par défaut

        // Série 1 : Les points réels (BLEU)
        XYChart.Series<Number, Number> seriePoints = new XYChart.Series<>();
        
        // Variables pour calculer la régression
        double sommeX = 0, sommeY = 0, sommeXY = 0, sommeX2 = 0;
        int nombrePoints = 0;
        double xMin = Double.MAX_VALUE;
        double xMax = Double.MIN_VALUE;

        // Parcours toutes les lignes du dataframe
        for (int i = 0; i < df.getNbLignes(); i++) {
            try {
                Object valeurX = df.getCase(i, colX);
                Object valeurY = df.getCase(i, colY);
                // Vérifie que les deux valeurs sont des nombres
                if (valeurX instanceof Number && valeurY instanceof Number) {
                    double x = ((Number) valeurX).doubleValue();
                    double y = ((Number) valeurY).doubleValue();
                    // Ajoute le point
                    XYChart.Data<Number, Number> point = new XYChart.Data<>(x, y);
                    seriePoints.getData().add(point);
                    // Calcule les sommes pour la régression
                    sommeX += x;
                    sommeY += y;
                    sommeXY += x * y;
                    sommeX2 += x * x; 
                    // Garde trace des valeurs min et max de X
                    if (x < xMin){
                        xMin = x; 
                    } 
                    if (x > xMax){
                        xMax = x;
                    } 
                    nombrePoints++;
                }
            } catch (OutOfBoundException e) {
                // Ignore les erreurs
            }
        }
        // Ajoute la série au graphique
        graphique.getData().add(seriePoints);
        // Colore les points en BLEU
        for (XYChart.Data<Number, Number> point : seriePoints.getData()) {
            if (point.getNode() != null) {
                point.getNode().setStyle("-fx-background-color: #3498db; -fx-background-radius: 5px;");
            }
        }
        // Série 2 : La droite de régression (ROUGE)
        if (nombrePoints >= 2) {
            // Calcule la pente (a) et l'ordonnée à l'origine (b)
            double a = (nombrePoints * sommeXY - sommeX * sommeY) / (nombrePoints * sommeX2 - sommeX * sommeX);
            double b = (sommeY - a * sommeX) / nombrePoints;

            // Affiche la formule dans le label superposé
            String signe = b >= 0 ? " + " : " - ";
            lblFormule.setText(
                "Régression : y = " + String.format("%.4f", a) + "x" +
                signe + String.format("%.4f", Math.abs(b))
            );
            
            // Crée une série pour la droite
            XYChart.Series<Number, Number> serieRegression = new XYChart.Series<>();
            serieRegression.setName("Régression: y = " + String.format("%.2f", a) + "x + " + String.format("%.2f", b));

            // Ajoute plein de petits points pour former une ligne continue
            int nbPoints = 100;
            for (int i = 0; i <= nbPoints; i++) {
                double x = xMin + (xMax - xMin) * i / nbPoints;
                double y = a * x + b;
                XYChart.Data<Number, Number> point = new XYChart.Data<>(x, y);
                serieRegression.getData().add(point);
            }
            
            // Ajoute la série au graphique
            graphique.getData().add(serieRegression);
            
            // Colore les points en ROUGE
            for (XYChart.Data<Number, Number> point : serieRegression.getData()) {
                if (point.getNode() != null) {
                    point.getNode().setStyle("-fx-background-color: #e74c3c; -fx-background-radius: 1px;");
                }
            }
        }
        return graphique;
    }
    
    /**
     * Crée la légende pour le nuage de points.
     */
    private HBox creerLegendeNuagePoints() {
        HBox legende = new HBox(20);
        legende.setAlignment(Pos.CENTER);
        legende.setPadding(new Insets(10, 0, 0, 0));
        
        // Point bleu
        Circle cercleBleu = new Circle(6);
        cercleBleu.setFill(Color.web("#3498db"));
        Label lblPoints = new Label("Données réelles");
        
        // Point rouge
        Circle cercleRouge = new Circle(6);
        cercleRouge.setFill(Color.web("#e74c3c"));
        Label lblRegression = new Label("Droite de régression");
        
        legende.getChildren().addAll(cercleBleu, lblPoints, cercleRouge, lblRegression);
        return legende;
    }

    // ==================== HISTOGRAMME ====================
    /**
     * Construit le panneau de l'histogramme (graphique + légende).
     */
    private VBox construireHistogramme() {
        VBox panneau = new VBox(5);
        panneau.setAlignment(Pos.TOP_CENTER);
        
        // Le graphique
        BarChart<String, Number> graphique = creerHistogramme();
        
        // La légende en dessous
        HBox legende = creerLegendeHistogramme();
        
        panneau.getChildren().addAll(graphique, legende);
        return panneau;
    }
    
    /**
     * Crée l'histogramme (distribution des valeurs en tranches).
     */
    private BarChart<String, Number> creerHistogramme() {
        String nomValeur = df.getNomCol(colHistogramme);
        
        // ===== 1. Récupère toutes les valeurs numériques =====
        List<Double> valeurs = new ArrayList<>();
        for (int i = 0; i < df.getNbLignes(); i++) {
            try {
                Object val = df.getCase(i, colHistogramme);
                if (val instanceof Number) {
                    valeurs.add(((Number) val).doubleValue());
                }
            } catch (OutOfBoundException e) {
                // Ignoré
            }
        }
        
        // Si pas de données, on affiche un message
        if (valeurs.isEmpty()) {
            BarChart<String, Number> graphique = new BarChart<>(new CategoryAxis(), new NumberAxis());
            graphique.setTitle("Histogramme de " + nomValeur + " (aucune donnée numérique)");
            return graphique;
        }
        //Trie les valeurs et trouve min/max
        Collections.sort(valeurs);
        double min = valeurs.get(0);
        double max = valeurs.get(valeurs.size() - 1);
        
        // On calcule le nombre de tranches
        // Règle de Sturges : nbBins = log2(n) + 1
        int nbBins = (int) Math.ceil(Math.log(valeurs.size()) / Math.log(2) + 1);
        nbBins = Math.max(5, Math.min(nbBins, 20));
        
        // Largeur de chaque tranche
        double largeurTranche = (max - min) / nbBins;
        // Crée les tranches
        String[] labelsTranches = new String[nbBins];
        int[] frequences = new int[nbBins];

        for (int i = 0; i < nbBins; i++) {
            double debut = min + i * largeurTranche;
            double fin = debut + largeurTranche;
            
            if (i == nbBins - 1) {
                labelsTranches[i] = String.format("[%.1f - %.1f]", debut, fin);
            } else {
                labelsTranches[i] = String.format("[%.1f - %.1f[", debut, fin);
            }
            frequences[i] = 0;
        }
        
        // On compte combien de valeurs dans chaque tranche
        for (double val : valeurs) {
            int index = (int) ((val - min) / largeurTranche);
            if (index >= nbBins) index = nbBins - 1;
            frequences[index]++;
        }
        
        // On crée le graphique
        CategoryAxis axeX = new CategoryAxis();
        NumberAxis axeY = new NumberAxis();
        axeX.setLabel(nomValeur + " (tranches)");
        axeY.setLabel("Nombre d'occurrences");
        BarChart<String, Number> graphique = new BarChart<>(axeX, axeY);
        graphique.setTitle("Histogramme de " + nomValeur);
        graphique.setPrefHeight(350);
        graphique.setLegendVisible(false);  // On cache la légende par défaut
        // Crée la série de barres
        XYChart.Series<String, Number> serie = new XYChart.Series<>();
        serie.setName("Effectifs");
        for (int i = 0; i < nbBins; i++) {
            XYChart.Data<String, Number> barre = new XYChart.Data<>(labelsTranches[i], frequences[i]);
            serie.getData().add(barre);
        }
        graphique.getData().add(serie);
        // Colore les barres en BLEU
        for (XYChart.Data<String, Number> barre : serie.getData()) {
            if (barre.getNode() != null) {
                barre.getNode().setStyle("-fx-bar-fill: #3498db;");
            }
        }
        return graphique;
    }
    
    /**
     * Crée la légende pour l'histogramme.
     */
    private HBox creerLegendeHistogramme() {
        HBox legende = new HBox(20);
        legende.setAlignment(Pos.CENTER);
        legende.setPadding(new Insets(10, 0, 0, 0));
        
        // Rectangle bleu
        Rectangle rectBleu = new Rectangle(12, 12);
        rectBleu.setFill(Color.web("#3498db"));
        Label lblEffectifs = new Label("Effectifs (nombre d'occurrences)");
        
        legende.getChildren().addAll(rectBleu, lblEffectifs);
        return legende;
    }
}
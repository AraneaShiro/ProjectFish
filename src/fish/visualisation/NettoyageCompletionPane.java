package fish.visualisation;

import fish.acquisition.DataframeComplet;
import fish.acquisition.DfIndividu;
import fish.acquisition.DfPopulation;
import fish.completion.CompletionKNN;
import fish.completion.CompletionMoyenne;
import fish.completion.CompletionRegression;
import fish.nettoyage.BoiteAMoustache;
import fish.nettoyage.NettoyageDfIndividu;
import fish.nettoyage.NettoyageDfPopulation;
import fish.nettoyage.NettoyageDataframe;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Panneau pour nettoyer et compléter les données d'un dataframe.
 * 
 * NETTOYAGE :
 *   - Supprimer les lignes avec trop de valeurs manquantes
 *   - Supprimer les colonnes vides
 *   - Supprimer les lignes avec des valeurs négatives
 *   - Remplacer les valeurs négatives par null
 *   - Gérer les valeurs aberrantes (outliers)
 * 
 * COMPLÉTION :
 *   - Remplacer les valeurs manquantes par la moyenne
 *   - Remplacer par la méthode KNN (k plus proches voisins)
 *   - Remplacer par régression linéaire
 * 
 * @author Arthur BERNARD
 * @version 0.1
 */
public class NettoyageCompletionPane extends VBox {

    // ========== ATTRIBUTS ==========
    
    private DataframeComplet df; // Les données à nettoyer
    private final Runnable onRefresh; // Fonction pour rafraîchir l'affichage
    
    // Composants de l'interface
    private ComboBox<String> comboColonnes; // Choix de la colonne
    private TextField txtSeuilNull; // Seuil de null (en %)
    private TextField txtKNN; // Nombre de voisins pour KNN
    private TextField txtSeuilBas; // Seuil bas personnalisé
    private TextField txtSeuilHaut; // Seuil haut personnalisé
    private Label lblStatut; // Message de statut
    private Label lblNbLignes; // Nombre de lignes
    private Label lblNbColonnes; // Nombre de colonnes
    private Label lblType; // Type de dataframe (Individu/Population)
    
    private boolean isIndividu; // True = DfIndividu, False = DfPopulation

    // ========== CONSTRUCTEUR ==========
    
    /**
     * Crée le panneau de nettoyage et complétion.
     * @param df Les données à traiter
     * @param onRefresh Fonction appelée après chaque modification
     */
    public NettoyageCompletionPane(DataframeComplet df, Runnable onRefresh) {
        this.df = df;
        this.onRefresh = onRefresh;
        this.isIndividu = (df instanceof DfIndividu); // Vérifie le type
        buildUI();
        mettreAJourInfos();
    }
    
    /**
     * Retourne les données actuelles.
     */
    public DataframeComplet getDataframe() {
        return df;
    }
    
    /**
     * Met à jour les données après un chargement ou une modification.
     * @param nouveauDf Le nouveau dataframe
     */
    public void mettreAJourDataframe(DataframeComplet nouveauDf) {
        this.df = nouveauDf;
        this.isIndividu = (df instanceof DfIndividu);
        remplirComboColonnes(); // Met à jour la liste des colonnes
        mettreAJourInfos(); // Met à jour les infos (nb lignes, etc.)
        setStatut("Données mises à jour", true);
    }

    // ========== CONSTRUCTION DE L'INTERFACE ==========
    /**
     * Crée tous les éléments graphiques du panneau.
     */
    private void buildUI() {
        this.setSpacing(10);
        this.setPadding(new Insets(15));
        this.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #ccc; -fx-border-radius: 5px;");
        // Panneau avec défilement
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: #f0f0f0; -fx-background-color: #f0f0f0;");
        VBox contenu = new VBox(10);
        contenu.setPadding(new Insets(10));
        
        // Titre
        Label titre = new Label("Nettoyage et Complétion");
        titre.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        
        // Infos Dataframe
        HBox infoBox = new HBox(20);
        infoBox.setAlignment(Pos.CENTER_LEFT);
        infoBox.setPadding(new Insets(5, 0, 10, 0));

        lblNbLignes = new Label();
        lblNbColonnes = new Label();
        lblType = new Label();
        
        infoBox.getChildren().addAll(
            new Label("Lignes:"), lblNbLignes,
            new Label("Colonnes:"), lblNbColonnes,
            new Label("Type:"), lblType
        );
        
        // Selection colonne
        HBox colonneBox = new HBox(10);
        colonneBox.setAlignment(Pos.CENTER_LEFT);
        colonneBox.setPadding(new Insets(5, 0, 10, 0));
        
        comboColonnes = new ComboBox<>();
        colonneBox.getChildren().addAll(new Label("Colonne cible:"), comboColonnes);
        
        // ========== SECTION NETTOYAGE ==========
        TitledPane nettoyagePane = new TitledPane("Nettoyage", null);
        nettoyagePane.setCollapsible(true);
        nettoyagePane.setExpanded(true);
        
        VBox nettoyageContenu = new VBox(8);
        nettoyageContenu.setPadding(new Insets(10));
        
        // Supprimer les lignes avec trop de null
        HBox ligne1 = new HBox(10);
        txtSeuilNull = new TextField("50");
        txtSeuilNull.setPrefWidth(60);
        Button btnSupprimerInvalid = new Button("Supprimer lignes avec trop de null");
        btnSupprimerInvalid.setOnAction(e -> supprimerLignesInvalid());
        ligne1.getChildren().addAll(new Label("Seuil null (%):"), txtSeuilNull, btnSupprimerInvalid);
        
        // Supprimer les colonnes vides
        Button btnSupprimerColonnesVides = new Button("Supprimer les colonnes vides");
        btnSupprimerColonnesVides.setOnAction(e -> supprimerColonnesVides());
        
        // Gérer les valeurs négatives
        HBox ligne3 = new HBox(10);
        Button btnSupprimerNegatives = new Button("Supprimer lignes avec valeurs négatives");
        btnSupprimerNegatives.setOnAction(e -> supprimerLignesNegatives());
        Button btnRemplacerNegatives = new Button("Remplacer négatifs par null");
        btnRemplacerNegatives.setOnAction(e -> remplacerNegatifsParNull());
        ligne3.getChildren().addAll(btnSupprimerNegatives, btnRemplacerNegatives);
        
        // Gérer les valeurs aberrantes
        Button btnOutliers = new Button("Détecter et gérer les valeurs aberrantes");
        btnOutliers.setOnAction(e -> gererOutliers());
        
        // Seuils personnalisés
        HBox ligne5 = new HBox(10);
        txtSeuilBas = new TextField();
        txtSeuilBas.setPromptText("Seuil bas");
        txtSeuilBas.setPrefWidth(100);
        txtSeuilHaut = new TextField();
        txtSeuilHaut.setPromptText("Seuil haut");
        txtSeuilHaut.setPrefWidth(100);
        Button btnAppliquerSeuils = new Button("Appliquer seuils personnalisés");
        btnAppliquerSeuils.setOnAction(e -> appliquerSeuilsPersonnalises());
        ligne5.getChildren().addAll(new Label("Seuils:"), txtSeuilBas, txtSeuilHaut, btnAppliquerSeuils);
        
        nettoyageContenu.getChildren().addAll(ligne1, btnSupprimerColonnesVides, ligne3, btnOutliers, ligne5);
        nettoyagePane.setContent(nettoyageContenu);
        
        // ========== SECTION COMPLÉTION ==========
        TitledPane completionPane = new TitledPane("Complétion (remplacer les valeurs manquantes)", null);
        completionPane.setCollapsible(true);
        completionPane.setExpanded(true);
        
        VBox completionContenu = new VBox(8);
        completionContenu.setPadding(new Insets(10));
        
        // Complétion par moyenne
        HBox ligneComp1 = new HBox(10);
        Button btnCompletionMoyenne = new Button("Compléter TOUTES les colonnes (moyenne)");
        btnCompletionMoyenne.setOnAction(e -> completerParMoyenne());
        Button btnCompletionMoyenneColonne = new Button("Compléter la colonne sélectionnée (moyenne)");
        btnCompletionMoyenneColonne.setOnAction(e -> completerColonneParMoyenne());
        ligneComp1.getChildren().addAll(btnCompletionMoyenne, btnCompletionMoyenneColonne);
        
        // Complétion par KNN
        HBox ligneComp2 = new HBox(10);
        txtKNN = new TextField("5");
        txtKNN.setPrefWidth(60);
        Button btnCompletionKNN = new Button("Compléter par KNN (k plus proches voisins)");
        btnCompletionKNN.setOnAction(e -> completerParKNN());
        ligneComp2.getChildren().addAll(new Label("k (voisins):"), txtKNN, btnCompletionKNN);
        
        // Complétion par régression
        Button btnCompletionRegression = new Button("Compléter par régression linéaire");
        btnCompletionRegression.setOnAction(e -> completerParRegression());
        
        completionContenu.getChildren().addAll(ligneComp1, ligneComp2, btnCompletionRegression);
        completionPane.setContent(completionContenu);
        
        // ========== BOUTONS ACTIONS ==========
        HBox actionsBox = new HBox(10);
        actionsBox.setPadding(new Insets(10, 0, 5, 0));
        
        Button btnActualiser = new Button("Actualiser les infos");
        btnActualiser.setOnAction(e -> {
            mettreAJourInfos();
            setStatut("Informations mises à jour", true);
        });
        
        Button btnStatistiques = new Button("Afficher statistiques (dans la console)");
        btnStatistiques.setOnAction(e -> afficherStatistiques());
        
        actionsBox.getChildren().addAll(btnActualiser, btnStatistiques);
        
        // Statut
        lblStatut = new Label("Prêt");
        lblStatut.setStyle("-fx-text-fill: #4CAF50;");
        
        // Assemblage
        contenu.getChildren().addAll(titre, infoBox, colonneBox, nettoyagePane, completionPane, actionsBox, lblStatut);
        scrollPane.setContent(contenu);
        this.getChildren().add(scrollPane);
        
        // Remplir la liste des colonnes
        remplirComboColonnes();
    }
    
    /**
     * Remplit le menu déroulant avec les noms des colonnes.
     */
    private void remplirComboColonnes() {
        if (comboColonnes == null) return;
        comboColonnes.getItems().clear();
        if (df == null) return;
        
        for (String nom : df.getNomColonnes()) {
            comboColonnes.getItems().add(nom);
        }
        if (!comboColonnes.getItems().isEmpty()) {
            comboColonnes.setValue(comboColonnes.getItems().get(0));
        }
    }
    
    /**
     * Met à jour les informations affichées (nb lignes, nb colonnes, type).
     */
    private void mettreAJourInfos() {
        if (df == null) return;
        lblNbLignes.setText(String.valueOf(df.getNbLignes()));
        lblNbColonnes.setText(String.valueOf(df.getNbCol()));
        lblType.setText(isIndividu ? "DfIndividu" : "DfPopulation");
    }
    
    /**
     * Retourne l'index de la colonne sélectionnée dans le menu.
     */
    private int getColonneIndex() {
        String nom = comboColonnes.getValue();
        if (nom == null || df == null) return -1;
        
        String[] noms = df.getNomColonnes();
        for (int i = 0; i < noms.length; i++) {
            if (noms[i].equals(nom)) return i;
        }
        return -1;
    }
    
    /**
     * Retourne le seuil de null (en pourcentage converti en décimal).
     */
    private double getSeuilNull() {
        try {
            return Double.parseDouble(txtSeuilNull.getText()) / 100.0;
        } catch (NumberFormatException e) {
            return 0.5;  // 50% par défaut
        }
    }
    
    /**
     * Retourne le nombre de voisins pour KNN.
     */
    private int getK() {
        try {
            return Integer.parseInt(txtKNN.getText());
        } catch (NumberFormatException e) {
            return 5;  // 5 par défaut
        }
    }
    
    /**
     * Affiche un message de statut (succès ou erreur).
     */
    private void setStatut(String message, boolean succes) {
        if (succes) {
            lblStatut.setStyle("-fx-text-fill: #4CAF50;");
            lblStatut.setText(message);
        } else {
            lblStatut.setStyle("-fx-text-fill: #f44336;");
            lblStatut.setText(message);
        }
    }

    // ========== MÉTHODES DE NETTOYAGE ==========
    
    /**
     * Supprime les lignes qui ont trop de valeurs manquantes (null).
     */
    private void supprimerLignesInvalid() {
        if (df == null) {
            setStatut("Aucun dataframe chargé", false);
            return;
        }
        
        try {
            System.out.println("=== Suppression des lignes invalides ===");
            System.out.println("Avant: " + df.getNbLignes() + " lignes");
            
            NettoyageDataframe nettoyeur = isIndividu 
                ? new NettoyageDfIndividu((DfIndividu) df, getSeuilNull())
                : new NettoyageDfPopulation((DfPopulation) df, getSeuilNull());
            
            int supprimees = nettoyeur.suppressionInvalid();
            
            System.out.println("Après: " + df.getNbLignes() + " lignes");
            System.out.println("Supprimées: " + supprimees);
            
            setStatut(supprimees + " ligne(s) supprimée(s)", true);
            mettreAJourInfos();
            
            if (onRefresh != null) onRefresh.run();
        } catch (Exception e) {
            setStatut("Erreur: " + e.getMessage(), false);
            e.printStackTrace();
        }
    }
    
    /**
     * Supprime les colonnes qui sont entièrement vides.
     */
    private void supprimerColonnesVides() {
        if (df == null) {
            setStatut("Aucun dataframe chargé", false);
            return;
        }
        
        try {
            System.out.println("=== Suppression des colonnes vides ===");
            System.out.println("Avant: " + df.getNbCol() + " colonnes");
            
            NettoyageDataframe nettoyeur = isIndividu 
                ? new NettoyageDfIndividu((DfIndividu) df)
                : new NettoyageDfPopulation((DfPopulation) df);
            
            int supprimees = nettoyeur.suppressionColonnesVides();
            
            System.out.println("Après: " + df.getNbCol() + " colonnes");
            System.out.println("Supprimées: " + supprimees);
            
            setStatut(supprimees + " colonne(s) supprimée(s)", true);
            remplirComboColonnes();
            mettreAJourInfos();
            
            if (onRefresh != null) onRefresh.run();
        } catch (Exception e) {
            setStatut("Erreur: " + e.getMessage(), false);
            e.printStackTrace();
        }
    }
    
    /**
     * Supprime les lignes qui contiennent des valeurs négatives dans la colonne sélectionnée.
     */
    private void supprimerLignesNegatives() {
        if (df == null) {
            setStatut("Aucun dataframe chargé", false);
            return;
        }
        
        int col = getColonneIndex();
        if (col < 0) {
            setStatut("Sélectionnez une colonne", false);
            return;
        }
        
        try {
            System.out.println("=== Suppression des lignes avec valeurs négatives ===");
            System.out.println("Colonne: " + comboColonnes.getValue());
            System.out.println("Avant: " + df.getNbLignes() + " lignes");
            
            NettoyageDataframe nettoyeur = isIndividu 
                ? new NettoyageDfIndividu((DfIndividu) df)
                : new NettoyageDfPopulation((DfPopulation) df);
            
            int supprimees = nettoyeur.suppressionLignesNegatives(col);
            
            System.out.println("Après: " + df.getNbLignes() + " lignes");
            System.out.println("Supprimées: " + supprimees);
            
            setStatut(supprimees + " ligne(s) supprimée(s)", true);
            mettreAJourInfos();
            
            if (onRefresh != null) onRefresh.run();
        } catch (Exception e) {
            setStatut("Erreur: " + e.getMessage(), false);
            e.printStackTrace();
        }
    }
    
    /**
     * Remplace les valeurs négatives par null dans la colonne sélectionnée.
     */
    private void remplacerNegatifsParNull() {
        if (df == null) {
            setStatut("Aucun dataframe chargé", false);
            return;
        }
        
        int col = getColonneIndex();
        if (col < 0) {
            setStatut("Sélectionnez une colonne", false);
            return;
        }
        
        try {
            System.out.println("=== Remplacement des négatifs par null ===");
            System.out.println("Colonne: " + comboColonnes.getValue());
            
            NettoyageDataframe nettoyeur = isIndividu 
                ? new NettoyageDfIndividu((DfIndividu) df)
                : new NettoyageDfPopulation((DfPopulation) df);
            
            int remplaces = nettoyeur.remplaceNegativeByNull(col);
            
            System.out.println("Remplacés: " + remplaces);
            
            setStatut(remplaces + " valeur(s) remplacée(s) par null", true);
            
            if (onRefresh != null) onRefresh.run();
        } catch (Exception e) {
            setStatut("Erreur: " + e.getMessage(), false);
            e.printStackTrace();
        }
    }
    
    /**
     * Détecte et gère les valeurs aberrantes (outliers) avec une boîte à moustaches.
     */
    private void gererOutliers() {
        if (df == null) {
            setStatut("Aucun dataframe chargé", false);
            return;
        }
        
        int col = getColonneIndex();
        if (col < 0) {
            setStatut("Sélectionnez une colonne", false);
            return;
        }
        
        BoiteAMoustache bam = new BoiteAMoustache(df, col);
        if (bam.getValeurs().isEmpty()) {
            setStatut("Pas de valeurs numériques dans cette colonne", false);
            return;
        }
        
        // Affiche la boîte à moustaches dans la console
        bam.afficher();
        bam.choisirSeuil();
        
        // Demande à l'utilisateur ce qu'il veut faire
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Outliers");
        alert.setHeaderText("Que faire des valeurs aberrantes ?");
        
        ButtonType btnSupprimer = new ButtonType("Supprimer les lignes");
        ButtonType btnRemplacer = new ButtonType("Remplacer par null");
        ButtonType btnAnnuler = new ButtonType("Annuler");
        
        alert.getButtonTypes().setAll(btnSupprimer, btnRemplacer, btnAnnuler);
        
        alert.showAndWait().ifPresent(response -> {
            if (response == btnSupprimer) {
                int supprimees = bam.supprimerHorsSeuil();
                setStatut(supprimees + " outlier(s) supprimé(s)", true);
                mettreAJourInfos();
                if (onRefresh != null) onRefresh.run();
            } else if (response == btnRemplacer) {
                int remplaces = bam.remplacerHorsSeuilNull();
                setStatut(remplaces + " outlier(s) remplacé(s) par null", true);
                if (onRefresh != null) onRefresh.run();
            }
        });
    }
    
    /**
     * Applique des seuils personnalisés (valeurs min et max à garder).
     */
    private void appliquerSeuilsPersonnalises() {
        if (df == null) {
            setStatut("Aucun dataframe chargé", false);
            return;
        }
        
        int col = getColonneIndex();
        if (col < 0) {
            setStatut("Sélectionnez une colonne", false);
            return;
        }
        
        try {
            double bas = Double.parseDouble(txtSeuilBas.getText());
            double haut = Double.parseDouble(txtSeuilHaut.getText());
            
            BoiteAMoustache bam = new BoiteAMoustache(df, col);
            bam.setSeuils(bas, haut);
            
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Appliquer les seuils");
            alert.setHeaderText("Que faire des valeurs hors seuil ?");
            
            ButtonType btnSupprimer = new ButtonType("Supprimer les lignes");
            ButtonType btnRemplacer = new ButtonType("Remplacer par null");
            ButtonType btnAnnuler = new ButtonType("Annuler");
            
            alert.getButtonTypes().setAll(btnSupprimer, btnRemplacer, btnAnnuler);
            
            alert.showAndWait().ifPresent(response -> {
                if (response == btnSupprimer) {
                    int supprimees = bam.supprimerHorsSeuil();
                    setStatut(supprimees + " ligne(s) supprimée(s)", true);
                    mettreAJourInfos();
                    if (onRefresh != null) onRefresh.run();
                } else if (response == btnRemplacer) {
                    int remplaces = bam.remplacerHorsSeuilNull();
                    setStatut(remplaces + " valeur(s) remplacée(s) par null", true);
                    if (onRefresh != null) onRefresh.run();
                }
            });
        } catch (NumberFormatException e) {
            setStatut("Seuils invalides", false);
        }
    }

    // ========== MÉTHODES DE COMPLÉTION ==========
    
    /**
     * Complète toutes les colonnes par leur moyenne.
     */
    private void completerParMoyenne() {
        if (df == null) {
            setStatut("Aucun dataframe chargé", false);
            return;
        }
        
        try {
            System.out.println("=== Complétion par moyenne ===");
            CompletionMoyenne cm = new CompletionMoyenne(df);
            int completes = cm.completerTout();
            setStatut(completes + " valeur(s) complétée(s) par moyenne", true);
            if (onRefresh != null) onRefresh.run();
        } catch (Exception e) {
            setStatut("Erreur: " + e.getMessage(), false);
            e.printStackTrace();
        }
    }
    
    /**
     * Complète la colonne sélectionnée par sa moyenne.
     */
    private void completerColonneParMoyenne() {
        if (df == null) {
            setStatut("Aucun dataframe chargé", false);
            return;
        }
        
        int col = getColonneIndex();
        if (col < 0) {
            setStatut("Sélectionnez une colonne", false);
            return;
        }
        
        try {
            System.out.println("=== Complétion colonne par moyenne ===");
            System.out.println("Colonne: " + comboColonnes.getValue());
            CompletionMoyenne cm = new CompletionMoyenne(df);
            int completes = cm.completerColonne(col);
            setStatut(completes + " valeur(s) complétée(s) dans " + comboColonnes.getValue(), true);
            if (onRefresh != null) onRefresh.run();
        } catch (Exception e) {
            setStatut("Erreur: " + e.getMessage(), false);
            e.printStackTrace();
        }
    }
    
    /**
     * Complète les valeurs manquantes par la méthode KNN (k plus proches voisins).
     */
    private void completerParKNN() {
        if (df == null) {
            setStatut("Aucun dataframe chargé", false);
            return;
        }
        
        try {
            System.out.println("=== Complétion par KNN ===");
            System.out.println("k = " + getK());
            CompletionKNN knn = new CompletionKNN(df, getK());
            int completes = knn.completerTout();
            setStatut(completes + " valeur(s) complétée(s) par KNN", true);
            if (onRefresh != null) onRefresh.run();
        } catch (Exception e) {
            setStatut("Erreur: " + e.getMessage(), false);
            e.printStackTrace();
        }
    }
    
    /**
     * Complète les valeurs manquantes par régression linéaire.
     */
    private void completerParRegression() {
        if (df == null) {
            setStatut("Aucun dataframe chargé", false);
            return;
        }
        
        try {
            System.out.println("=== Complétion par régression ===");
            CompletionRegression cr = new CompletionRegression(df);
            int completes = cr.completerToutParMeilleurPredicteur();
            setStatut(completes + " valeur(s) complétée(s) par régression", true);
            if (onRefresh != null) onRefresh.run();
        } catch (Exception e) {
            setStatut("Erreur: " + e.getMessage(), false);
            e.printStackTrace();
        }
    }
    
    /**
     * Affiche les statistiques du dataframe dans la console.
     */
    private void afficherStatistiques() {
        if (df == null) {
            setStatut("Aucun dataframe chargé", false);
            return;
        }
        System.out.println("\n=== STATISTIQUES DU DATAFRAME ===");
        df.afficherStatistiques();
        System.out.println("=================================\n");
        setStatut("Statistiques affichées dans la console", true);
    }
}
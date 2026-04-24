package fish.visualisation;

import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import fish.acquisition.DataframeComplet;
import fish.exceptions.OutOfBoundException;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Panneau pour afficher toutes les lignes d'un dataframe dans un tableau.
 * Permet de naviguer page par page.
 * 
 * Version OPTIMISÉE : les colonnes sont créées une seule fois pour éviter le clignotement.
 * 
 * @author Arthur BERNARD
 * @version 0.2
 */
public class DataframeViewerPane extends VBox {

    // ========== ATTRIBUTS ==========
    
    private DataframeComplet df; // Les données à afficher
    
    // Composants graphiques
    private TableView<Object[]> tableView; // Le tableau
    private Label lblPageInfo; // "Page 1 / 10"
    private ComboBox<Integer> comboNbLignesPage; // Choix: 10, 20, 50, 100 lignes par page
    private Button btnPremier; // "Première page"
    private Button btnPrecedent; // "Page précédente"
    private Button btnSuivant; // "Page suivante"
    private Button btnDernier; // "Dernière page"
    private Label lblStatut; // Message de statut
    private Label lblTotalLignes; // "Total: 100 lignes"
    
    // Variables pour la pagination
    private int pageCourante = 0; // Page actuelle (0 = première)
    private int nbLignesParPage = 20; // Lignes par page
    private int totalPages = 1; // Nombre total de pages
    
    // permet de savoir si les colonnes ont déjà été créées pour ne pas les recréer à chaque maj
    private boolean colonnesInitialisees = false;

    // ========== CONSTRUCTEUR ==========
    
    public DataframeViewerPane(DataframeComplet df) {
        this.df = df;
        buildUI(); // Crée les boutons
        initialiserColonnes(); // Crée les colonnes du tableau (UNE SEULE FOIS)
        chargerDonnees();// Charge les premières données
    }
    
    public DataframeComplet getDataframe() {
        return df;
    }
    
    /**
     * Met à jour les données après un nettoyage/complétion.
     */
    public void mettreAJourDataframe(DataframeComplet nouveauDf) {
        this.df = nouveauDf;
        pageCourante = 0;  // On revient à la première page
        
        // si le nombre de colonnes a changé on doit recréer les colonnes
        if (colonnesInitialisees && df.getNbCol() != tableView.getColumns().size()) {
            tableView.getColumns().clear();
            initialiserColonnes();
        }
        
        // On recharge les données
        rechargerDonnees();
    }

    // ========== CONSTRUCTION DE L'INTERFACE ==========
    
    private void buildUI() {
        this.setSpacing(10);
        this.setPadding(new Insets(15));
        this.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #ccc; -fx-border-radius: 5px;");
        
        // Titre
        Label titre = new Label("Visualisation des données");
        titre.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        
        // Barre d'infos
        HBox infoBox = new HBox(15);
        infoBox.setAlignment(Pos.CENTER_LEFT);
        lblTotalLignes = new Label();
        infoBox.getChildren().add(lblTotalLignes);
        
        // Tableau
        tableView = new TableView<>();
        tableView.setPrefHeight(400);
        
        // Contrôles de pagination
        HBox paginationBox = new HBox(10);
        paginationBox.setAlignment(Pos.CENTER);
        paginationBox.setPadding(new Insets(10, 0, 5, 0));
        
        btnPremier = new Button("Première");
        btnPremier.setOnAction(e -> allerPage(0));
        
        btnPrecedent = new Button("Précédent");
        btnPrecedent.setOnAction(e -> allerPage(pageCourante - 1));
        
        lblPageInfo = new Label();
        lblPageInfo.setMinWidth(120);
        lblPageInfo.setAlignment(Pos.CENTER);
        
        btnSuivant = new Button("Suivant");
        btnSuivant.setOnAction(e -> allerPage(pageCourante + 1));
        
        btnDernier = new Button("Dernière");
        btnDernier.setOnAction(e -> allerPage(totalPages - 1));
        
        paginationBox.getChildren().addAll(btnPremier, btnPrecedent, lblPageInfo, btnSuivant, btnDernier);
        
        // Choix du nombre de lignes par page
        HBox pageSizeBox = new HBox(10);
        pageSizeBox.setAlignment(Pos.CENTER_RIGHT);
        
        comboNbLignesPage = new ComboBox<>();
        comboNbLignesPage.getItems().addAll(10, 20, 50, 100);
        comboNbLignesPage.setValue(nbLignesParPage);
        comboNbLignesPage.setOnAction(e -> {
            nbLignesParPage = comboNbLignesPage.getValue();
            pageCourante = 0;
            mettreAJourAffichageLignes();
            mettreAJourBoutonsPagination();
        });
        
        pageSizeBox.getChildren().addAll(new Label("Lignes par page:"), comboNbLignesPage);
        
        // Bouton actualiser
        HBox actionsBox = new HBox(10);
        actionsBox.setPadding(new Insets(10, 0, 5, 0));
        
        Button btnActualiser = new Button("Actualiser");
        btnActualiser.setOnAction(e -> rechargerDonnees());
        actionsBox.getChildren().add(btnActualiser);
        
        // Statut
        lblStatut = new Label("✓ Prêt");
        lblStatut.setStyle("-fx-text-fill: #4CAF50;");
        
        // Assemblage
        this.getChildren().addAll(
            titre, infoBox, tableView,
            paginationBox, pageSizeBox, actionsBox, lblStatut
        );
    }

    // ========== INITIALISATION DES COLONNES ==========
    
    /**
     * Crée les colonnes du tableau.
     * Cette méthode n'est appelée qu'une seule fois au démarrage.
     */
    private void initialiserColonnes() {
        if (df == null || df.getNbLignes() == 0) return;
        
        String[] nomsColonnes = df.getNomColonnes();
        
        // Pour chaque colonne du dataframe, on crée une colonne dans le tableau
        for (int j = 0; j < nomsColonnes.length; j++) {
            final int indexColonne = j;  // final = ne peut pas changer (nécessaire pour la lambda)
            
            TableColumn<Object[], String> colonne = new TableColumn<>(nomsColonnes[j]);
            
            // Dit comment récupérer la valeur pour cette colonne
            colonne.setCellValueFactory(donneesCellule -> {
                Object val = donneesCellule.getValue()[indexColonne];
                return new javafx.beans.property.SimpleStringProperty(
                    val != null ? val.toString() : ""  // null → chaîne vide
                );
            });
            
            colonne.setPrefWidth(150);
            tableView.getColumns().add(colonne);
        }
        
        colonnesInitialisees = true;  // les colonnes sont prêtes
    }
    
    // ========== GESTION DES DONNÉES ==========
    
    /**
     * Charge les données pour la première fois
     */
    private void chargerDonnees() {
        if (df == null) return;
        
        lblTotalLignes.setText("Total: " + df.getNbLignes() + " lignes");
        
        totalPages = (int) Math.ceil((double) df.getNbLignes() / nbLignesParPage);
        if (totalPages == 0) totalPages = 1;
        
        if (pageCourante >= totalPages) pageCourante = totalPages - 1;
        if (pageCourante < 0) pageCourante = 0;
        
        mettreAJourAffichageLignes();
        mettreAJourBoutonsPagination();
    }
    
    /**
     * Recharge les données après un nettoyage
     */
    private void rechargerDonnees() {
        if (df == null) return;
        
        lblTotalLignes.setText("Total: " + df.getNbLignes() + " lignes");
        
        totalPages = (int) Math.ceil((double) df.getNbLignes() / nbLignesParPage);
        if (totalPages == 0) totalPages = 1;
        
        if (pageCourante >= totalPages) pageCourante = totalPages - 1;
        if (pageCourante < 0) pageCourante = 0;
        
        mettreAJourAffichageLignes();
        mettreAJourBoutonsPagination();
    }
    
    /**
     * Met à jour l'affichage des lignes pour la page courante
     * On vide les anciennes données et on met les nouvelles
     */
    private void mettreAJourAffichageLignes() {
        tableView.getItems().clear();  // On vide les anciennes lignes
        
        if (df == null || df.getNbLignes() == 0) return;
        
        String[] nomsColonnes = df.getNomColonnes();
        
        int debut = pageCourante * nbLignesParPage;
        int fin = Math.min(debut + nbLignesParPage, df.getNbLignes());
        
        for (int i = debut; i < fin; i++) {
            Object[] ligne = new Object[nomsColonnes.length];
            for (int j = 0; j < nomsColonnes.length; j++) {
                try {
                    ligne[j] = df.getCase(i, j);
                } catch (OutOfBoundException e) {
                    ligne[j] = null;
                }
            }
            tableView.getItems().add(ligne);  // On ajoute la ligne
        }
        
        lblStatut.setText("✓ Lignes " + (debut + 1) + " à " + fin + " sur " + df.getNbLignes());
    }
    
    /**
     * Active/désactive les boutons de pagination selon la page courante
     */
    private void mettreAJourBoutonsPagination() {
        lblPageInfo.setText((pageCourante + 1) + " / " + totalPages);
        
        btnPremier.setDisable(pageCourante == 0);
        btnPrecedent.setDisable(pageCourante == 0);
        btnSuivant.setDisable(pageCourante >= totalPages - 1);
        btnDernier.setDisable(pageCourante >= totalPages - 1);
    }
    
    /**
     * Va à une page spécifique
     */
    private void allerPage(int page) {
        if (page < 0 || page >= totalPages) return;
        
        pageCourante = page;
        mettreAJourAffichageLignes();
        mettreAJourBoutonsPagination();
    }
}
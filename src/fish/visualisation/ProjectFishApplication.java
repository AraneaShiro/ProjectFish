package fish.visualisation;

import fish.acquisition.DataframeComplet;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Classe principale de l'application.
 * C'est le point d'entrée du programme.
 * 
 * @author Arthur Bernard
 * @version 0.3
 */
public class ProjectFishApplication extends Application {

    // ========== ATTRIBUTS ==========
    
    private Scene scene; // La scène (contient tous les éléments graphiques)
    private AnchorPane root; // Conteneur principal
    private TitlePane appTitle; // Bandeau du titre en haut
    private FileLoaderPane fileLoaderPane; // Panneau pour charger les fichiers
    private NettoyageCompletionPane nettoyagePane;  // Panneau pour nettoyer les données
    private DataframeViewerPane viewerPane; // Panneau pour voir les données en tableau
    private TabPane tabPane; // Conteneur à onglets
    private GraphiqueStage graphiqueStage; // Référence à la fenêtre des graphiques (si ouverte)

    // ========== MÉTHODE PRINCIPALE ==========
    
    public static void main(String[] args) {
        launch(args);  // Lance l'application JavaFX
    }

    // ========== MÉTHODE START ==========
    
    @Override
    public void start(Stage stage) {
        stage.setTitle("Project Fish");
        buildUI(stage);
        stage.show();
    }

    // ========== CONSTRUCTION DE L'INTERFACE ==========
    private void buildUI(Stage stage) {
        // Conteneur principal
        root = new AnchorPane();
        // Scène
        scene = new Scene(root, 1200, 800);
        stage.setScene(scene);
        
        // titre
        appTitle = new TitlePane(scene, "Project Fish");
        root.getChildren().add(appTitle);
        AnchorPane.setTopAnchor(appTitle, 0.0);
        AnchorPane.setLeftAnchor(appTitle, 0.0);
        AnchorPane.setRightAnchor(appTitle, 0.0);
        
        // Zone défilante
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        root.getChildren().add(scrollPane);
        AnchorPane.setTopAnchor(scrollPane, 60.0);
        AnchorPane.setLeftAnchor(scrollPane, 0.0);
        AnchorPane.setRightAnchor(scrollPane, 0.0);
        AnchorPane.setBottomAnchor(scrollPane, 0.0);
        
        // Conteneur vertical pour organiser les panneaux
        VBox contenu = new VBox(15);
        contenu.setPadding(new Insets(20));
        scrollPane.setContent(contenu);
        
        // Panneau de chargement
        fileLoaderPane = new FileLoaderPane(stage);
        // "this::onFichierCharge" signifie "appelle la méthode onFichierCharge de cette classe"
        fileLoaderPane.setOnFichierCharge(this::onFichierCharge);
        contenu.getChildren().add(fileLoaderPane);
        
        //Séparateur
        Separator separator = new Separator();
        contenu.getChildren().add(separator);
        
        //Panneau à onglets
        tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);  // Empêche de fermer les onglets
        contenu.getChildren().add(tabPane);
        
        // Onglet Nettoyage
        Tab ongletNettoyage = new Tab("Nettoyer les données");
        Label messageAttente = new Label("Charger un fichier pour commencer");
        messageAttente.setPadding(new Insets(50));
        ongletNettoyage.setContent(messageAttente);
        ongletNettoyage.setDisable(true);
        tabPane.getTabs().add(ongletNettoyage);
        
        // Onglet Visualisation
        Tab ongletVisualisation = new Tab("Voir les données");
        Label messageAttente2 = new Label("Charger un fichier pour commencer");
        messageAttente2.setPadding(new Insets(50));
        ongletVisualisation.setContent(messageAttente2);
        ongletVisualisation.setDisable(true);
        tabPane.getTabs().add(ongletVisualisation);
    }
    
    /**
     * Cette méthode est appelée AUTOMATIQUEMENT quand un fichier est chargé avec succès.
     * C'est ici qu'on crée les panneaux de nettoyage et de visualisation.
     * 
     * @param df Le dataframe qui vient d'être chargé
     */
    private void onFichierCharge(DataframeComplet df) {
        // Récupère les deux onglets
        Tab ongletNettoyage = tabPane.getTabs().get(0);
        Tab ongletVisualisation = tabPane.getTabs().get(1);
        
        // Active les onglets
        ongletNettoyage.setDisable(false);
        ongletVisualisation.setDisable(false);
        
        // Crée le panneau de nettoyage
        if (nettoyagePane == null) {
            // "this::rafraichir" appelle la méthode rafraichir() quand le nettoyage est fait
            nettoyagePane = new NettoyageCompletionPane(df, this::rafraichir);
            ongletNettoyage.setContent(nettoyagePane);
        } else {
            // Si le panneau existe déjà, on met juste à jour ses données
            nettoyagePane.mettreAJourDataframe(df);
        }
        
        // Crée le panneau de visualisation
        if (viewerPane == null) {
            viewerPane = new DataframeViewerPane(df);
            ongletVisualisation.setContent(viewerPane);
        } else {
            // Si le panneau existe déjà, on met juste à jour ses données
            viewerPane.mettreAJourDataframe(df);
        }
    }
    
    /**
     * Cette méthode est appelée apres chaque opération de nettoyage ou complétion.
     * Elle permet de rafraîchir tous les panneaux avec les données modifiées.
     */
    public void rafraichir() {
        // Récupère le dataframe modifié depuis le panneau de nettoyage
        DataframeComplet dfModifie = nettoyagePane != null ? nettoyagePane.getDataframe() : null;
        
        if (dfModifie != null) {
            // Met à jour le tableau de visualisation
            if (viewerPane != null) {
                viewerPane.mettreAJourDataframe(dfModifie);
            }
            
            // Met à jour le panneau de chargement (pour que le bouton graphiques
            // utilise les bonnes données si on ouvre une nouvelle fenêtre)
            fileLoaderPane.setDataframeCharge(dfModifie);
            
            // Met à jour la fenêtre des graphiques si elle est ouverte
            if (graphiqueStage != null && graphiqueStage.isShowing()) {
                graphiqueStage.mettreAJourDataframe(dfModifie);
            } else if (graphiqueStage != null) {
                // La fenêtre a été fermée, on oublie la référence
                graphiqueStage = null;
            }
        }
    }
}
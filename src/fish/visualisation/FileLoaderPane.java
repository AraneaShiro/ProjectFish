package fish.visualisation;

import fish.acquisition.DataframeComplet;
import fish.acquisition.DfIndividu;
import fish.acquisition.DfPopulation;
import fish.acquisition.lecture.LectureCSV;
import fish.acquisition.lecture.LectureParasitesPeru;
import fish.exceptions.FileEmpty;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.function.Consumer; // Permet de passer une fonction en paramètre (callback)

/**
 * Panneau pour charger un fichier CSV.
 * L'utilisateur choisit le type de données et le séparateur,
 * puis sélectionne un fichier à ouvrir.
 * 
 * @author Arthur BERNARD
 * @version 0.3
 */
public class FileLoaderPane extends VBox {

    // ========== ATTRIBUTS ==========
    
    private final Stage stage; // La fenêtre principale
    private ComboBox<String> comboType; // Menu déroulant pour le type de données
    private TextField txtSeparateur; // Zone de texte pour le séparateur CSV
    private AppButton btnCharger; // Bouton pour charger un fichier
    private AppButton btnGraphiques; // Bouton pour ouvrir les graphiques
    private Label labelStatut; // Texte qui affiche le statut
    private DataframeComplet dataframeCharge; // Les données chargées (null si rien)
    
    // C'est une fonction qui sera appelée quand un fichier est chargé
    // pour mettre à jour l'interface de l'application javafx
    private Consumer<DataframeComplet> onFichierCharge;

    // ========== CONSTRUCTEUR ==========
    
    public FileLoaderPane(Stage stage) {
        this.stage = stage;
        buildUI(); // On crée les boutons
        ajouterEcouteurs(); // On dit quoi faire quand on clique
    }

    // ========== ACCESSEURS (getters/setters) ==========
    
    /**
     * Retourne les données actuellement chargées
     */
    public DataframeComplet getDataframeCharge() {
        return dataframeCharge;
    }
    
    /**
     * Permet de modifier les données depuis l'extérieur (après nettoyage par exemple)
     */
    public void setDataframeCharge(DataframeComplet nouveauDf) {
        this.dataframeCharge = nouveauDf;
        if (nouveauDf != null) {
            // Succès : on met à jour l'affichage
            labelStatut.setText(
                "✓ Données mises à jour — " + 
                nouveauDf.getNbLignes() + " lignes x " + nouveauDf.getNbCol() + " colonnes"
            );
            labelStatut.setStyle("-fx-text-fill: #4CAF50;");  // Texte vert
            btnGraphiques.setDisable(false);  // On active le bouton graphiques
        } else {
            // Pas de données
            labelStatut.setText("Aucun fichier chargé.");
            labelStatut.setStyle("-fx-text-fill: #666;");  // Texte gris
            btnGraphiques.setDisable(true);
        }
    }
    
    /**
     * Permet d'enregistrer une fonction à appeler quand un fichier est chargé
     * Exemple: fileLoaderPane.setOnFichierCharge(df -> { faire quelque chose avec df });
     */
    public void setOnFichierCharge(Consumer<DataframeComplet> callback) {
        this.onFichierCharge = callback;
    }

    // ========== CONSTRUCTION DE L'INTERFACE ==========
    
    private void buildUI() {
        // Style du panneau
        this.setSpacing(10);
        this.setPadding(new Insets(15));
        this.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #ccc; -fx-border-radius: 5px;");

        // ----- Type de données -----
        HBox ligneType = new HBox(10);
        ligneType.setAlignment(Pos.CENTER_LEFT);
        
        Label labelType = new Label("Type de données :");
        comboType = new ComboBox<>();
        comboType.getItems().addAll(
            "Individus (poissons individuels)",
            "Population (format standard)",
            "Population (format ParasitesPeru)"
        );
        comboType.setValue("Individus (poissons individuels)");
        ligneType.getChildren().addAll(labelType, comboType);

        // ----- Séparateur -----
        HBox ligneSep = new HBox(10);
        ligneSep.setAlignment(Pos.CENTER_LEFT);

        Label labelSep = new Label("Séparateur CSV :");
        txtSeparateur = new TextField(",");
        txtSeparateur.setPrefWidth(60);
        txtSeparateur.setPromptText("ex: ,  ;  \\t");

        Label labelSepAide = new Label("(virgule par défaut, \\t pour tabulation)");
        labelSepAide.setStyle("-fx-text-fill: #888; -fx-font-size: 11px;");

        ligneSep.getChildren().addAll(labelSep, txtSeparateur, labelSepAide);

        // ----- Boutons -----
        HBox ligneBoutons = new HBox(10);
        ligneBoutons.setAlignment(Pos.CENTER_LEFT);
        
        btnCharger = new AppButton("Choisir un fichier CSV");
        btnGraphiques = new AppButton("Voir les graphiques");
        btnGraphiques.setDisable(true);  // Désactivé au départ
        
        ligneBoutons.getChildren().addAll(btnCharger, btnGraphiques);

        // ----- Statut -----
        labelStatut = new Label("Aucun fichier chargé.");
        labelStatut.setStyle("-fx-text-fill: #666;");

        // ----- Assemblage -----
        this.getChildren().addAll(ligneType, ligneSep, ligneBoutons, labelStatut);
    }

    // ========== GESTION DES ÉVÉNEMENTS ==========
    
    private void ajouterEcouteurs() {
        btnCharger.setOnAction(event -> chargerFichier());
        btnGraphiques.setOnAction(event -> ouvrirGraphiques());
    }
    
    private void ouvrirGraphiques() {
        if (dataframeCharge != null) {
            new GraphiqueStage(dataframeCharge); // Ouvre une nouvelle fenêtre
        }
    }
    
    private void chargerFichier() {
        // Boîte de dialogue pour choisir un fichier
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir un fichier CSV");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Fichiers CSV", "*.csv") // filtre pour les fichiers csv
        );
        
        File fichier = fileChooser.showOpenDialog(stage);
        if (fichier == null) return; // L'utilisateur a annulé
        
        // Récupérer les paramètres
        String separateur = getSeparateurChoisi();
        String typeChoisi = comboType.getValue();
        
        try {
            DataframeComplet df = null;
            
            // Créer le bon lecteur selon le type choisi
            if (typeChoisi.startsWith("Population (format ParasitesPeru)")) {
                LectureParasitesPeru lecteur = new LectureParasitesPeru(separateur);
                df = lecteur.lireCSV(fichier.getAbsolutePath(), DfPopulation.class);
            } else if (typeChoisi.startsWith("Population")) {
                LectureCSV lecteur = new LectureCSV(separateur);
                df = lecteur.lireCSV(fichier.getAbsolutePath(), DfPopulation.class);
            } else {
                LectureCSV lecteur = new LectureCSV(separateur);
                df = lecteur.lireCSV(fichier.getAbsolutePath(), DfIndividu.class);
            }
            
            // Vérifier si le chargement a réussi
            if (df == null) {
                labelStatut.setText("Échec du chargement");
                dataframeCharge = null;
                btnGraphiques.setDisable(true);
            } else {
                // Succès : on garde les données
                dataframeCharge = df;
                labelStatut.setText(
                    "✓ " + fichier.getName() + 
                    " — " + df.getNbLignes() + " lignes × " + df.getNbCol() + " colonnes"
                );
                labelStatut.setStyle("-fx-text-fill: #4CAF50;");
                btnGraphiques.setDisable(false);
                
                // Cela permet à l'application de créer les onglets de nettoyage/visualisation
                if (onFichierCharge != null) {
                    onFichierCharge.accept(df);
                }
            }
            
        } catch (FileEmpty ex) {
            labelStatut.setText("Le fichier est vide : " + fichier.getName());
            labelStatut.setStyle("-fx-text-fill: #f44336;"); // Rouge
            dataframeCharge = null;
            btnGraphiques.setDisable(true);
        } catch (Exception ex) {
            labelStatut.setText("Erreur : " + ex.getMessage());
            labelStatut.setStyle("-fx-text-fill: #f44336;"); // Rouge
            dataframeCharge = null;
            btnGraphiques.setDisable(true);
        }
    }
    
    /**
     * Récupère le séparateur entré par l'utilisateur.
     * "\\t" (deux caractères) est converti en vrai caractère de tabulation.
     * Si le champ est vide, on utilise la virgule par défaut.
     */
    private String getSeparateurChoisi() {
        String val = txtSeparateur.getText();
        if (val == null || val.isEmpty()) return ",";
        if (val.equals("\\t")) return "\t";
        return val;
    }
}
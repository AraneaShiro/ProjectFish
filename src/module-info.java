
/** Projet SAE ULCO Bernard et Grenesche
 * Logiciel de lecture,nettoyage et analyse de CSV de poissons
 * 
 */
module ProjetFish {
     requires javafx.controls;
    requires javafx.graphics;
    requires javafx.base;
    requires javafx.fxml;

    exports fish.acquisition;
    exports fish.acquisition.lecture;
    exports fish.analyse;
    exports fish.calcul;
    exports fish.completion;
    exports fish.conversion;
    exports fish.exceptions;
    exports fish.graphiques;
    exports fish.interaction;
    exports fish.nettoyage;
    exports fish.poisson;
    exports fish.visualisation;
}
package fish.acquisition.lecture;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

import fish.acquisition.DataframeComplet;
import fish.exceptions.FileEmpty;

/**
 * LectureCSV est la classe qui permet de lire un csv et le convertir dans le
 * dataframe c'est la version par defaut qui lie les fichier avec les entetes
 * dans la 1er colonnes
 * 
 * @author Jules Grenesche
 * @version 0.1
 *          Date : 25/03
 */
public class LectureHorizontal extends LectureCSV {

    public LectureHorizontal(String separteur) {
        super(separteur);
    }

    /**
     * Lit un fichier CSV et retourne une instance de la classe héritée de Dataframe
     * choisie
     *
     * @param <T>           le type héritant de Dataframe
     * @param cheminFichier le chemin vers le fichier CSV
     * @param type          la classe cible (ex: DfIndividu.class)
     * @return une instance de T construite à partir du fichier
     * @throws FileEmpty si le fichier est vide ou sans en-têtes
     */
    public <T extends DataframeComplet> T lireCSV(String cheminFichier, Class<T> type) throws FileEmpty {

        List<String[]> lignesBrutes = new ArrayList<>();

        try (Scanner scanner = new Scanner(new File(cheminFichier))) {

            while (scanner.hasNextLine()) {
                String ligne = scanner.nextLine();
                if (ligne.trim().isEmpty())
                    continue;

                String[] valeurs = ligne.split(Pattern.quote(this.getSeparateur()), -1);
                lignesBrutes.add(valeurs);
            }

        } catch (FileNotFoundException e) {
            System.out.println("Fichier introuvable : " + cheminFichier);
            return null;
        }

        if (lignesBrutes.isEmpty()) {
            throw new FileEmpty(cheminFichier);
        }

        // entêtes = première colonne
        this.nbLignes = lignesBrutes.get(0).length - 1; // nb de colonnes de données
        this.nbCol = lignesBrutes.size(); // nb d'entêtes

        this.entetes = new String[this.nbCol];
        for (int i = 0; i < this.nbCol; i++) {
            this.entetes[i] = lignesBrutes.get(i)[0];
        }

        // construction du tableau (transposé)
        Object[][] tableau = new Object[this.nbLignes][this.nbCol];

        for (int i = 0; i < this.nbCol; i++) {
            String[] ligne = lignesBrutes.get(i);

            for (int j = 1; j < ligne.length; j++) {
                String valeur = ligne[j].trim();
                tableau[j - 1][i] = convertirType(valeur);
            }
        }

        // Instanciation de la classe cible via réflexion
        try {
            return type.getConstructor(int.class, String[].class, Object[][].class)
                    .newInstance(this.nbLignes, this.entetes, tableau);

        } catch (Exception e) {
            System.out.println("Impossible d'instancier " + type.getSimpleName() + " : " + e.getMessage());
            return null;
        }

    }
}

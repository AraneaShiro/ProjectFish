package fish.acquisition.lecture;

import fish.acquisition.DataframeComplet;
import fish.exceptions.FileEmpty;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Lecteur spécifique pour le CSV ParasitesPeru2021 avec un pivot
 *
 * Résultat pivoté :
 * Une ligne par espèce, avec pour chaque paramètre × année une colonne.
 * Ex : "N_Total", "N_2012", "Prévalence (%)_2013", etc.
 *
 * @author Jules Grenesche
 * @version 0.1
 */
public class LectureParasitesPeru extends LectureCSV {

    /**
     * Index de la colonne contenant le nom de l'espèce (clé de groupement)
     */
    private static final int COL_ESPECE = 0;

    /**
     * Index de la colonne contenant le nom du paramètre
     */
    private static final int COL_PARAMETRE = 1;

    /**
     * Index à partir duquel commencent les valeurs (Total, 2012, 2013...)
     */
    private static final int COL_DEBUT_VALEURS = 2;

    public LectureParasitesPeru(String separateur) {
        super(separateur);
    }

    public LectureParasitesPeru() {
        super(",");
    }

    /**
     * Lit le CSV ParasitesPeru et retourne un Dataframe pivoté :
     * une ligne par espèce, une colonne par (paramètre × année).
     *
     * @param cheminFichier le chemin du fichier
     * @param type          la classe cible héritant de Dataframe
     * @return une instance de T avec les données pivotées
     * @throws FileEmpty si le fichier est vide
     */
    @Override
    public <T extends DataframeComplet> T lireCSV(String cheminFichier, Class<T> type) throws FileEmpty {

        List<String[]> lignesBrutes = new ArrayList<>();
        String[] headersCSV = null;

        // ── Lecture brute ────────────────────────────────────────────────────
        try (Scanner scanner = new Scanner(new File(cheminFichier))) {

            boolean premiereLigne = true;
            while (scanner.hasNextLine()) {
                String ligne = scanner.nextLine();
                if (ligne.trim().isEmpty())
                    continue;

                String[] valeurs = ligne.split(Pattern.quote(this.getSeparateur()), -1);
                if (premiereLigne) {
                    headersCSV = valeurs; // Ex: ["Espèce","Paramètre","Total","2012","2013","2014"]
                    premiereLigne = false;
                } else {
                    lignesBrutes.add(valeurs);
                }
            }

        } catch (FileNotFoundException e) {
            System.out.println("Fichier introuvable : " + cheminFichier);
            return null;
        }

        if (headersCSV == null || lignesBrutes.isEmpty()) {
            throw new FileEmpty(cheminFichier);
        }

        // Noms des colonnes "valeur" : Total, 2012, 2013, 2014
        String[] anneesLabels = Arrays.copyOfRange(headersCSV, COL_DEBUT_VALEURS, headersCSV.length);

        // ── Groupement par espèce ────────────────────────────────────────────
        // LinkedHashMap pour conserver l'ordre d'apparition des espèces
        LinkedHashMap<String, List<String[]>> parEspece = new LinkedHashMap<>();
        for (String[] ligne : lignesBrutes) {
            String espece = ligne[COL_ESPECE].trim();
            parEspece.computeIfAbsent(espece, k -> new ArrayList<>()).add(ligne);
        }

        // ── Construction des entêtes pivotées ────────────────────────────────
        // "Espèce" + pour chaque paramètre × année : "N_Total", "N_2012", ...
        List<String> nouvellesEntetes = new ArrayList<>();
        nouvellesEntetes.add("Espèce");

        // On récupère les paramètres depuis la première espèce
        List<String[]> premierEspece = parEspece.values().iterator().next();
        List<String> parametres = new ArrayList<>();
        for (String[] ligneParam : premierEspece) {
            parametres.add(ligneParam[COL_PARAMETRE].trim());
        }

        for (String parametre : parametres) {
            for (String annee : anneesLabels) {
                nouvellesEntetes.add(parametre + "_" + annee);
            }
        }

        this.entetes = nouvellesEntetes.toArray(new String[0]);
        this.nbCol = this.entetes.length;
        this.nbLignes = parEspece.size();

        // ── Construction du tableau pivoté ───────────────────────────────────
        Object[][] tableau = new Object[this.nbLignes][this.nbCol];
        int ligneIdx = 0;

        for (Map.Entry<String, List<String[]>> entry : parEspece.entrySet()) {
            String espece = entry.getKey();
            List<String[]> lignes = entry.getValue();

            tableau[ligneIdx][0] = espece; // Colonne "Espèce"

            int colIdx = 1;
            for (String[] ligneParam : lignes) {
                // Pour chaque valeur (Total, 2012, 2013, 2014)
                for (int v = COL_DEBUT_VALEURS; v < ligneParam.length; v++) {
                    tableau[ligneIdx][colIdx++] = convertirType(ligneParam[v].trim());
                }
            }
            ligneIdx++;
        }

        // ── Instanciation via réflexion ──────────────────────────────────────
        try {
            return type.getConstructor(int.class, String[].class, Object[][].class)
                    .newInstance(this.nbLignes, this.entetes, tableau);
        } catch (Exception e) {
            System.out.println("Impossible d'instancier " + type.getSimpleName() + " : " + e.getMessage());
            return null;
        }
    }
}

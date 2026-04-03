package fish.acquisition.lecture;

import fish.acquisition.*;
import fish.acquisition.DfIndividu;
import fish.acquisition.DfPopulation;
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

    public static void main(String[] args) {
        int ok = 0, total = 0;

        LectureParasitesPeru lecteur = new LectureParasitesPeru(",");

        // ── Lecture ParasitesPeru2021.csv ─────────────────────────────────────
        System.out.println("── Lecture ParasitesPeru2021.csv ────────────────────");

        DfPopulation dfPeru = null;
        try {
            dfPeru = lecteur.lireCSV("data/ParasitesPeru2021.csv", DfPopulation.class);
        } catch (Exception e) { System.out.println("Chargement échoué : " + e); }

        // Test 1 : df non null
        total++;
        if (dfPeru != null) {
            System.out.println("PASS Test 1 : ParasitesPeru chargé");
            ok++;
            dfPeru.afficherPremieresFignes(5);
            dfPeru.afficherStatistiques();
        } else {
            System.out.println("FAIL Test 1 : dfPeru null");
        }

        // Test 2 : une ligne par espèce (le pivot regroupe les paramètres)
        total++;
        if (dfPeru != null && dfPeru.getNbLignes() > 0) {
            System.out.println("PASS Test 2 : " + dfPeru.getNbLignes() + " ligne(s) (espèces)");
            ok++;
        } else {
            System.out.println("FAIL Test 2 : 0 ligne ou null");
        }

        // Test 3 : entêtes pivotées contiennent "_" (format parametre_annee)
        total++;
        if (dfPeru != null) {
            String[] noms = dfPeru.getNomColonnes();
            boolean auMoinsUnPivot = false;
            for (String n : noms) {
                if (n.contains("_")) { auMoinsUnPivot = true; break; }
            }
            if (auMoinsUnPivot) {
                System.out.println("PASS Test 3 : entêtes pivotées contiennent '_'");
                ok++;
            } else {
                System.out.println("FAIL Test 3 : aucune entête pivotée trouvée");
            }
        } else {
            System.out.println("SKIP Test 3 : dfPeru null");
            total--;
        }

        // Test 4 : première colonne = "Espèce" (colonne de regroupement)
        total++;
        if (dfPeru != null && dfPeru.getNomColonnes().length > 0) {
            String premierNom = dfPeru.getNomColonnes()[0];
            if (premierNom.toLowerCase().contains("esp")) {
                System.out.println("PASS Test 4 : première colonne = '" + premierNom + "'");
                ok++;
            } else {
                System.out.println("FAIL Test 4 : première col = '" + premierNom + "'");
            }
        } else {
            System.out.println("SKIP Test 4 : dfPeru null ou sans colonnes");
            total--;
        }

        // Test 5 : valeur numérique dans colonne N_Total cohérente
        total++;
        if (dfPeru != null) {
            try {
                // La colonne dont le nom contient "N_Total" ou similaire
                String[] noms = dfPeru.getNomColonnes();
                int colN = -1;
                for (int j = 0; j < noms.length; j++) {
                    if (noms[j].contains("N_") || noms[j].equalsIgnoreCase("N_Total")) {
                        colN = j; break;
                    }
                }
                if (colN >= 0) {
                    Object val = dfPeru.getCase(0, colN);
                    if (val instanceof Number) {
                        System.out.println("PASS Test 5 : valeur numérique en col " + noms[colN] + " = " + val);
                        ok++;
                    } else {
                        System.out.println("FAIL Test 5 : valeur non numérique → " + val);
                    }
                } else {
                    System.out.println("SKIP Test 5 : colonne N introuvable");
                    total--;
                }
            } catch (Exception e) { System.out.println("FAIL Test 5 : " + e); }
        } else {
            System.out.println("SKIP Test 5 : dfPeru null");
            total--;
        }

        // ── Lecture test_peru_format.csv ──────────────────────────────────────
        System.out.println("\n── Lecture test_peru_format.csv ─────────────────────");

        DfPopulation dfTest = null;
        try {
            dfTest = lecteur.lireCSV("data/Test/test_peru_format.csv", DfPopulation.class);
        } catch (Exception e) { System.out.println("Chargement échoué : " + e); }

        // Test 6 : df non null
        total++;
        if (dfTest != null) {
            System.out.println("PASS Test 6 : test_peru_format chargé, "
                    + dfTest.getNbLignes() + " espèce(s), " + dfTest.getNbCol() + " colonnes");
            ok++;
            dfTest.afficherPremieresFignes(4);
            dfTest.afficherStatistiques();
        } else {
            System.out.println("FAIL Test 6 : dfTest null");
        }

        // Test 7 : nombre correct d'espèces (4 dans test_peru_format.csv)
        total++;
        if (dfTest != null && dfTest.getNbLignes() == 4) {
            System.out.println("PASS Test 7 : 4 espèces détectées");
            ok++;
        } else {
            System.out.println("FAIL Test 7 : attendu 4, obtenu "
                    + (dfTest != null ? dfTest.getNbLignes() : "N/A"));
        }

        // ── Fichier inexistant ────────────────────────────────────────────────
        System.out.println("\n── Cas d'erreur ─────────────────────────────────────");

        // Test 8 : fichier inexistant → null
        total++;
        try {
            DfPopulation dfNull = lecteur.lireCSV("fichier_inexistant.csv", DfPopulation.class);
            if (dfNull == null) {
                System.out.println("PASS Test 8 : fichier inexistant → null");
                ok++;
            } else {
                System.out.println("FAIL Test 8 : attendu null");
            }
        } catch (Exception e) { System.out.println("FAIL Test 8 : exception inattendue " + e); }

        System.out.println("\n═══════════════════════════════════════════════════");
        System.out.println("=== LectureParasitesPeru : " + ok + "/" + total + " tests réussis ===");
        System.out.println("═══════════════════════════════════════════════════");
    }
}
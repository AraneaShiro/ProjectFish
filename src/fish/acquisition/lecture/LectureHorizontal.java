package fish.acquisition.lecture;

import fish.acquisition.*;
import fish.exceptions.FileEmpty;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * Variante de {@link LectureCSV} pour les CSV dont les en-têtes se trouvent
 * dans la <em>première colonne</em> (format horizontal/transposé).
 *
 * <p>Chaque ligne du fichier représente une variable (en-tête en colonne 0),
 * et chaque colonne supplémentaire représente une observation.
 * Le tableau est transposé lors de la lecture pour obtenir le format standard
 * lignes × colonnes attendu par les dataframes.</p>
 *
 * @author Jules Grenesche
 * @version 0.1
 * @see LectureCSV
 */
public class LectureHorizontal extends LectureCSV {

    /**
     * Crée un lecteur horizontal avec le séparateur de champs donné.
     *
     * @param separteur le délimiteur de colonnes (ex : ",", ";", "\t")
     */
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

    /**
     * Tests unitaires de la lecture horizontale (format transposé).
     *
     * @param args arguments de la ligne de commande (ignorés)
     */
    public static void main(String[] args) {

        // Le format horizontal place les entêtes en 1ère colonne,
        // les valeurs en colonnes suivantes → transposition.
        // On utilise merlu2018_75164.csv (format séparateur ;, entêtes en ligne 1)
        // comme données réelles et test_sep.csv pour les cas simples.

        // ── Lecture fichier standard (entêtes en ligne) ───────────────────────
        System.out.println("── Lecture merlu2018_75164.csv via LectureHorizontal ─");

        int total=0; 
        int ok=0;
        
        LectureHorizontal lh = new LectureHorizontal(";");
        DfIndividu dfMerlu = null;
        try {
            dfMerlu = lh.lireCSV("data/merlu2018_75164.csv", DfIndividu.class);
        } catch (Exception e) { System.out.println("Chargement : " + e); }

        // Test 1 : lecture sans exception et df non null
        total++;
        if (dfMerlu != null) {
            System.out.println("PASS Test 1 : merlu chargé (horizontal), "
                    + dfMerlu.getNbLignes() + " lignes, " + dfMerlu.getNbCol() + " cols");
            ok++;
            dfMerlu.afficherPremieresFignes(5);
            dfMerlu.afficherStatistiques();
        } else {
            System.out.println("FAIL Test 1 : dfMerlu null");
        }

        // Test 2 : dimensions cohérentes (transposition : lignes ↔ colonnes vs CSV standard)
        total++;
        if (dfMerlu != null && dfMerlu.getNbLignes() > 0 && dfMerlu.getNbCol() > 0) {
            System.out.println("PASS Test 2 : dimensions cohérentes ["
                    + dfMerlu.getNbLignes() + "x" + dfMerlu.getNbCol() + "]");
            ok++;
        } else {
            System.out.println("FAIL Test 2 : dimensions invalides");
        }

        // Test 3 : entêtes = première colonne du fichier horizontal
        total++;
        if (dfMerlu != null && dfMerlu.getNomColonnes() != null
                && dfMerlu.getNomColonnes().length > 0) {
            System.out.println("PASS Test 3 : entêtes présentes, 1ère = '"
                    + dfMerlu.getNomColonnes()[0] + "'");
            ok++;
        } else {
            System.out.println("FAIL Test 3 : entêtes null ou vides");
        }

        // ── Fichier inexistant ────────────────────────────────────────────────
        System.out.println("\n── Cas d'erreur ─────────────────────────────────────");

        // Test 4 : fichier inexistant → null
        total++;
        try {
            DfIndividu dfNull = lh.lireCSV("fichier_inexistant.csv", DfIndividu.class);
            if (dfNull == null) {
                System.out.println("PASS Test 4 : fichier inexistant → null");
                ok++;
            } else {
                System.out.println("FAIL Test 4 : attendu null");
            }
        } catch (Exception e) { System.out.println("FAIL Test 4 : exception inattendue " + e); }

        // Test 5 : séparateur différent préservé par getSeparateur()
        total++;
        LectureHorizontal lhVirgule = new LectureHorizontal(",");
        if (",".equals(lhVirgule.getSeparateur())) {
            System.out.println("PASS Test 5 : getSeparateur() retourne ','");
            ok++;
        } else {
            System.out.println("FAIL Test 5 : " + lhVirgule.getSeparateur());
        }

        // ── Cohérence getSize ─────────────────────────────────────────────────
        System.out.println("\n── getSize cohérent ─────────────────────────────────");

        // Test 6 : getSize() = [nbLignes, nbCol]
        total++;
        if (dfMerlu != null) {
            int[] size = dfMerlu.getSize();
            if (size[0] == dfMerlu.getNbLignes() && size[1] == dfMerlu.getNbCol()) {
                System.out.println("PASS Test 6 : getSize() cohérent avec getNbLignes/getNbCol");
                ok++;
            } else {
                System.out.println("FAIL Test 6 : incohérence getSize");
            }
        } else {
            System.out.println("SKIP Test 6 : dfMerlu null");
            total--;
        }

        System.out.println("\n═══════════════════════════════════════════════════");
        System.out.println("=== LectureHorizontal : " + ok + "/" + total + " tests réussis ===");
        System.out.println("═══════════════════════════════════════════════════");
    }
}
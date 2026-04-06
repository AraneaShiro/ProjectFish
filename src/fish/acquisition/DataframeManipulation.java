// ── Package ────────────────────────────────────────────────────────────
package fish.acquisition;

// ── Import ────────────────────────────────────────────────────────────
import fish.exceptions.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

// ── Test : NON ────────────────────────────────────────────────────────────
/**
 * Classe héritant de dataframe de base mais qui ajoute les méthodes de
 * manipulation du tableau
 * Gère la fusion, la suppression et l'analyse des valeurs uniques.
 *
 * @author Jules Grenesche
 * @version 1.0
 * 
 */
public abstract class DataframeManipulation extends DataframeBase {

    // ── Constructeurs (délégation vers DataframeBase) ─────────────────────────

    /**
     * Constructeur sans tableau de données.
     *
     * @param nbLignes   le nombre de lignes
     * @param nomColonne les noms des colonnes
     */
    public DataframeManipulation(int nbLignes, String[] nomColonne) {
        super(nbLignes, nomColonne);
    }

    /**
     * Constructeur avec tableau de données.
     *
     * @param nbLignes   le nombre de lignes
     * @param nomColonne les noms des colonnes
     * @param newtab     le tableau de données
     * @throws OutOfBoundException    si les dimensions ne correspondent pas
     * @throws NullParameterException si les paramètres sont vides ou null
     */
    public DataframeManipulation(int nbLignes, String[] nomColonne, Object[][] newtab)
            throws OutOfBoundException, NullParameterException {
        super(nbLignes, nomColonne, newtab);
    }

    // ── Fusion / Suppression ──────────────────────────────────────────────────

    /**
     * Fusionne deux colonnes complémentaires en une seule.
     * Conditions :
     * - Quand l'une a une valeur, l'autre doit être null (pas de chevauchement)
     *
     * @param col1           l'index de la première colonne
     * @param col2           l'index de la deuxième colonne
     * @param nomNouvColonne le nom de la nouvelle colonne fusionnée
     * @throws OutOfBoundException si un index est invalide
     * @throws NotNullException    si les 2 colonnes sont pleines
     */
    public void fusionCol(int col1, int col2, String nomNouvColonne)
            throws OutOfBoundException, IllegalArgumentException, NotNullException {

        if (col1 < 0 || col1 >= this.nbCol) {
            throw new OutOfBoundException(0, col1, this.nbLignes, this.nbCol);
        }
        if (col2 < 0 || col2 >= this.nbCol) {
            throw new OutOfBoundException(0, col2, this.nbLignes, this.nbCol);
        }

        // ── Vérification de la complémentarité ──────────────────────────────
        for (int i = 0; i < this.nbLignes; i++) {
            Object valCol1 = this.tableau[i][col1];
            Object valCol2 = this.tableau[i][col2];
            if (valCol1 != null && valCol2 != null) {
                throw new NotNullException(i, col1, col2);
            }
        }

        // ── Construction de la colonne fusionnée ─────────────────────────────
        int nouvelleNbCol = this.nbCol - 1;
        Object[][] nouveauTableau = new Object[this.nbLignes][nouvelleNbCol];
        String[] nouveauxNoms = new String[nouvelleNbCol];

        int indexFusion = Math.min(col1, col2);

        // Noms de colonnes
        int curseur = 0;
        for (int j = 0; j < this.nbCol; j++) {
            if (j == indexFusion) {
                nouveauxNoms[curseur++] = nomNouvColonne;
            } else if (j != Math.max(col1, col2)) {
                nouveauxNoms[curseur++] = this.nomColonne[j];
            }
        }

        // Données fusionnées
        for (int i = 0; i < this.nbLignes; i++) {
            curseur = 0;
            for (int j = 0; j < this.nbCol; j++) {
                if (j == indexFusion) {
                    Object v1 = this.tableau[i][col1];
                    Object v2 = this.tableau[i][col2];
                    nouveauTableau[i][curseur++] = (v1 != null) ? v1 : v2;
                } else if (j != Math.max(col1, col2)) {
                    nouveauTableau[i][curseur++] = this.tableau[i][j];
                }
            }
        }

        // ── Mise à jour du dataframe ──────────────────────────────────────────
        this.tableau = nouveauTableau;
        this.nomColonne = nouveauxNoms;
        this.nbCol = nouvelleNbCol;
    }

    /**
     * Supprime une colonne.
     *
     * @param nCol l'index de la colonne à supprimer
     * @throws OutOfBoundException si l'index est invalide
     */
    public void supprimerColonne(int nCol) throws OutOfBoundException {
        if (nCol == -1 || nCol > this.nbCol - 1) {
            throw new OutOfBoundException(nCol, nbLignes, nbCol);
        }

        // ── Creation d'un nouveau tableau sans la colonnes
        Object[][] nouveauTableau = new Object[this.nbLignes][this.nbCol - 1];
        for (int i = 0; i < this.nbLignes; i++) {
            int newCol = 0;
            for (int j = 0; j < this.nbCol; j++) {
                if (j != nCol) {
                    nouveauTableau[i][newCol++] = this.tableau[i][j];
                }
            }
        }

        String[] nouvellesColonnes = new String[this.nbCol - 1];
        int k = 0;
        for (int i = 0; i < this.nbCol; i++) {
            if (i != nCol) {
                nouvellesColonnes[k++] = this.nomColonne[i];
            }
        }

        // ── Mise à jour du dataframe ──────────────────────────────────────────
        this.tableau = nouveauTableau;
        this.nomColonne = nouvellesColonnes;
        this.nbCol--;
    }

    // ── Valeurs uniques ───────────────────────────────────────────────────────

    /**
     * Retourne le nombre de valeurs uniques dans une colonne.
     *
     * @param col la colonne à analyser
     * @return le nombre de valeurs uniques (null inclus)
     * @throws OutOfBoundException si l'index est invalide
     */
    public int getUniqueCol(int col) throws OutOfBoundException {
        if (col < 0 || col >= this.nbCol) {
            throw new OutOfBoundException(col, this.nbLignes, this.nbCol);
        }
        // ── Liste d'unique ───────────────────────
        List<Object> valeursUniques = new ArrayList<>();
        for (int i = 0; i < this.nbLignes; i++) {
            Object val = this.tableau[i][col];
            if (!valeursUniques.contains(val)) {
                valeursUniques.add(val);
            }
        }
        return valeursUniques.size();
    }

    /**
     * Retourne le nombre de valeurs uniques pour chaque colonne.
     *
     * @return int[] où chaque case correspond au nb de valeurs uniques
     */
    public int[] getUniqueTab() {
        int[] resultat = new int[this.nbCol];
        for (int j = 0; j < this.nbCol; j++) {
            try {
                resultat[j] = getUniqueCol(j);
            } catch (OutOfBoundException e) {
                System.out.println(e.getMessage());
            }
        }
        return resultat;
    }

    /**
     * Retourne chaque valeur unique d'une colonne avec son nombre d'apparitions.
     *
     * @param col l'index de la colonne
     * @return une Map clé=valeur unique, valeur=nb d'apparitions
     * @throws OutOfBoundException si l'index est invalide
     */
    public HashMap<Object, Integer> getUniqueColonneSomme(int col) throws OutOfBoundException {
        if (col < 0 || col >= this.nbCol) {
            throw new OutOfBoundException(0, col, this.nbLignes, this.nbCol);
        }
        HashMap<Object, Integer> occurrences = new HashMap<>();
        for (int i = 0; i < this.nbLignes; i++) {
            Object valeur = this.tableau[i][col];
            occurrences.put(valeur, occurrences.getOrDefault(valeur, 0) + 1); //Get la valeur et sinon deonne le defaut
        }
        return occurrences;
    }

    // ── TEST A FAIRE ───────────────────────
    public static void main(String[] args) {
        int ok = 0, tot = 0;
        System.out.println("=== Tests DataframeManipulation ===");

        try {
            // ── 1. getUniqueCol ──────────────────────────────────────────────────
            Object[][] data = {
                {"Merlan", 30.0, null},
                {"Hareng", 25.0, "A"},
                {"Merlan", 35.0, "B"},
                {"Thon",   null, "A"}
            };
            DfIndividu df = new DfIndividu(4, new String[]{"espece","longueur","cat"}, data);

            tot++; if (df.getUniqueCol(0) == 3) { ok++; System.out.println("PASS getUniqueCol espece=3"); } else System.out.println("FAIL getUniqueCol espece=" + df.getUniqueCol(0));
            tot++; if (df.getUniqueCol(1) == 4) { ok++; System.out.println("PASS getUniqueCol longueur=4 (inclut null)"); } else System.out.println("FAIL getUniqueCol longueur=" + df.getUniqueCol(1));
            tot++; if (df.getUniqueCol(2) == 3) { ok++; System.out.println("PASS getUniqueCol cat=3"); } else System.out.println("FAIL getUniqueCol cat=" + df.getUniqueCol(2));

            // Exception sur index invalide
            try {
                df.getUniqueCol(99);
                System.out.println("FAIL getUniqueCol index invalide");
            } catch (OutOfBoundException e) { tot++; ok++; System.out.println("PASS getUniqueCol index invalide → exception"); }

            // ── 2. getUniqueTab ───────────────────────────────────────────────────
            int[] tab = df.getUniqueTab();
            tot++; if (tab.length == 3 && tab[0] == 3 && tab[1] == 4 && tab[2] == 3) { ok++; System.out.println("PASS getUniqueTab"); } else System.out.println("FAIL getUniqueTab = " + java.util.Arrays.toString(tab));

            // ── 3. getUniqueColonneSomme ──────────────────────────────────────────
            HashMap<Object, Integer> map = df.getUniqueColonneSomme(0);
            tot++; if (map.get("Merlan") == 2 && map.get("Hareng") == 1 && map.get("Thon") == 1) { ok++; System.out.println("PASS getUniqueColonneSomme"); } else System.out.println("FAIL getUniqueColonneSomme");

            // ── 4. supprimerColonne ───────────────────────────────────────────────
            Object[][] d2 = {{"A", 1.0, true}, {"B", 2.0, false}, {"C", 3.0, true}};
            DfIndividu df2 = new DfIndividu(3, new String[]{"x", "y", "z"}, d2);
            df2.supprimerColonne(1); // supprime "y"
            tot++; if (df2.getNbCol() == 2 && "x".equals(df2.getNomColonnes()[0]) && "z".equals(df2.getNomColonnes()[1])) { ok++; System.out.println("PASS supprimerColonne"); } else System.out.println("FAIL supprimerColonne");

            // Exception sur index invalide
            try {
                df2.supprimerColonne(99);
                System.out.println("FAIL supprimerColonne index invalide");
            } catch (OutOfBoundException e) { tot++; ok++; System.out.println("PASS supprimerColonne index invalide → exception"); }

            // ── 5. fusionCol (complémentarité) ────────────────────────────────────
            Object[][] d3 = {{"Merlan", null, 30.5}, {"Hareng", 25.0, null}};
            DfIndividu df3 = new DfIndividu(2, new String[]{"esp", "lon1", "lon2"}, d3);
            df3.fusionCol(1, 2, "longueur");

            tot++; if (df3.getNbCol() == 2 && "longueur".equals(df3.getNomColonnes()[1])) { ok++; System.out.println("PASS fusionCol noms"); } else System.out.println("FAIL fusionCol noms");
            tot++; if (Double.valueOf(30.5).equals(df3.getCase(0, 1)) && Double.valueOf(25.0).equals(df3.getCase(1, 1))) { ok++; System.out.println("PASS fusionCol valeurs"); } else System.out.println("FAIL fusionCol valeurs");

            // ── 6. fusionCol avec chevauchement → NotNullException ────────────────
            Object[][] d4 = {{"A", 1.0, 2.0}};
            DfIndividu df4 = new DfIndividu(1, new String[]{"x", "a", "b"}, d4);
            try {
                df4.fusionCol(1, 2, "m");
                System.out.println("FAIL fusionCol chevauchement (exception attendue)");
            } catch (NotNullException e) { tot++; ok++; System.out.println("PASS fusionCol chevauchement → NotNullException"); }

            // ── 7. fusionCol avec index invalide → OutOfBoundException ────────────
            try {
                df3.fusionCol(0, 99, "x");
                System.out.println("FAIL fusionCol index invalide");
            } catch (OutOfBoundException e) { tot++; ok++; System.out.println("PASS fusionCol index invalide → OutOfBoundException"); }

        } catch (Exception e) {
            System.out.println("FAIL général : " + e);
        }

        System.out.println("\n=== DataframeManipulation : " + ok + "/" + tot + " ===");
    }
}

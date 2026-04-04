// ── Package ────────────────────────────────────────────────────────────
package fish.acquisition;

// ── Import ────────────────────────────────────────────────────────────
import fish.exceptions.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

// ── Test : NON ────────────────────────────────────────────────────────────
/**
 * Squelette de base du dataframe
 * Attributs, constructeurs et accès aux cases.
 * Toutes les autres couches héritent de cette classe.
 * 
 * @see fish.calcul.Statistique
 * @author Jules Grenesche
 * @version 1.0
 */
public abstract class DataframeBase implements fish.calcul.Statistique {

    // ── Attributs ────────────────────────────────────────────────────────────

    /** Le nombre de colonnes du dataframe */
    protected int nbCol;

    /** Noms des colonnes */
    protected String[] nomColonne;

    /** Le nombre de lignes du dataframe */
    protected int nbLignes;

    /** Tableau contenant l'ensemble des données */
    protected Object[][] tableau;

    /** HashMap des statistiques calculées */
    protected HashMap<String, Double> statistiques = new HashMap<>();

    // ── Accesseurs ───────────────────────────────────────────────────────────

    /**
     * Get le nombre de colonne
     * 
     * @return le nombre de colonne du dataframe
     */
    public int getNbCol() {
        return this.nbCol;
    }

    /**
     * Get le nombre de ligne
     * 
     * @return le nombre de ligne du dataframe
     */
    public int getNbLignes() {
        return this.nbLignes;
    }

    /**
     * Get le tableau de données
     * 
     * @return le tableau de données du dataframe
     */
    public Object[][] getTableau() {
        return this.tableau;
    }

    /**
     * Get les entetes
     * 
     * @return une liste des entetes du dataframe
     */
    public String[] getNomColonnes() {
        return this.nomColonne;
    }

    /**
     * Get le nom de la colonne
     * 
     * @param col le numéro de la colonne voulue
     * @return l'entete de la colonne
     */
    public String getNomCol(int col) {
        return this.nomColonne[col];
    }

    /**
     * Get les statistiques du dataframe
     * 
     * @return une hashMap des statistiques du dataframe
     */
    public HashMap<String, Double> getStatistique() {
        return this.statistiques;
    }

    // ── Constructeurs ────────────────────────────────────────────────────────

    /**
     * Constructeur sans tableau de données.
     *
     * @param nbLignes   le nombre de lignes
     * @param nomColonne les noms des colonnes
     */
    public DataframeBase(int nbLignes, String[] nomColonne) {
        this.nbLignes = nbLignes;
        this.nbCol = nomColonne.length;
        this.nomColonne = nomColonne;
        this.tableau = new Object[nbLignes][this.nbCol];
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
    public DataframeBase(int nbLignes, String[] nomColonne, Object[][] newtab)
            throws OutOfBoundException, NullParameterException {

        if (nbLignes == 0 || nomColonne.length == 0 || newtab == null) {
            throw new NullParameterException();
        }
        if (newtab.length != nbLignes) {
            throw new OutOfBoundException(nbLignes, newtab.length, newtab[0].length);
        }
        if (newtab[0].length != nomColonne.length) {
            throw new OutOfBoundException(nomColonne.length, newtab.length, newtab[0].length);
        }

        this.nbLignes = nbLignes;
        this.nbCol = nomColonne.length;
        this.nomColonne = nomColonne;
        this.tableau = newtab;
    }

    // ── Méthode abstraite ─────────────────────────────────────────────────────

    /** Retourne le titre / type du dataframe. */
    public abstract String getTitle();

    // ── Dimensions ────────────────────────────────────────────────────────────

    /**
     * Retourne les dimensions sous forme de tableau [nbLignes, nbCol].
     * 
     * @return un tableau ligne x col des dimensions
     */
    public int[] getSize() {
        return new int[] { this.nbLignes, this.nbCol };
    }

    /**
     * Retourne les dimensions sous forme lisible "X lignes x Y colonnes".
     * 
     * @return un string des dimensions
     */
    public String getDimension() {
        return this.nbLignes + " lignes x " + this.nbCol + " colonnes";
    }

    // ── Accès aux cases ───────────────────────────────────────────────────────

    /**
     * Retourne tous les éléments d'une colonne.
     *
     * @param col l'index de la colonne
     * @return List<Object> des éléments de la colonne
     * @throws OutOfBoundException si l'index est invalide
     */
    public List<Object> getColonne(int col) throws OutOfBoundException {
        if (col < 0 || col >= this.nbCol) {
            throw new OutOfBoundException(col, this.nbLignes, this.nbCol);
        }
        List<Object> colonne = new ArrayList<>();
        for (int i = 0; i < this.nbLignes; i++) {
            colonne.add(this.tableau[i][col]);
        }
        return colonne;
    }

    /**
     * Retourne la valeur d'une case.
     *
     * @param lig la ligne
     * @param col la colonne
     * @return l'objet à cette position
     * @throws OutOfBoundException si les coordonnées sont invalides
     */
    public Object getCase(int lig, int col) throws OutOfBoundException {
        if (lig < 0 || lig >= this.nbLignes || col < 0 || col >= this.nbCol) {
            throw new OutOfBoundException(lig, col, this.nbLignes, this.nbCol);
        }
        return this.tableau[lig][col];
    }

    /**
     * Modifie la valeur d'une case.
     *
     * @param lig    la ligne
     * @param col    la colonne
     * @param newObj la nouvelle valeur
     * @return true si la modification a réussi
     * @throws OutOfBoundException si les coordonnées sont invalides
     */
    public boolean setCase(int lig, int col, Object newObj) throws OutOfBoundException {
        if (lig < 0 || lig >= this.nbLignes || col < 0 || col >= this.nbCol) {
            throw new OutOfBoundException(lig, col, this.nbLignes, this.nbCol);
        }
        this.tableau[lig][col] = newObj;
        return true;
    }

    public static void main(String[] args) {
    int ok = 0, tot = 0;
    System.out.println("=== Tests DataframeBase ===");

    // ── 1. Constructeur sans tableau ─────────────────────────────────────────
    DfIndividu df = new DfIndividu(3, new String[]{"a", "b"});

    tot++; if (df.getNbLignes() == 3) { ok++; System.out.println("PASS getNbLignes()"); } else System.out.println("FAIL getNbLignes");
    tot++; if (df.getNbCol() == 2) { ok++; System.out.println("PASS getNbCol()"); } else System.out.println("FAIL getNbCol");
    tot++; if (df.getTableau() != null) { ok++; System.out.println("PASS getTableau() non null"); } else System.out.println("FAIL getTableau");
    tot++; if ("a".equals(df.getNomColonnes()[0])) { ok++; System.out.println("PASS getNomColonnes()"); } else System.out.println("FAIL getNomColonnes");
    tot++; if ("b".equals(df.getNomCol(1))) { ok++; System.out.println("PASS getNomCol()"); } else System.out.println("FAIL getNomCol");
    tot++; if (df.getStatistique() != null) { ok++; System.out.println("PASS getStatistique()"); } else System.out.println("FAIL getStatistique");

    int[] size = df.getSize();
    tot++; if (size[0] == 3 && size[1] == 2) { ok++; System.out.println("PASS getSize()"); } else System.out.println("FAIL getSize");

    tot++; if (df.getDimension().contains("3") && df.getDimension().contains("2")) { ok++; System.out.println("PASS getDimension()"); } else System.out.println("FAIL getDimension");

    // ── 2. Constructeur avec tableau valide ───────────────────────────────────
    try {
        Object[][] data = {{"Merlan", 30.5}, {"Hareng", 25.0}};
        DfIndividu df2 = new DfIndividu(2, new String[]{"espece", "longueur"}, data);
        tot++; if (df2.getNbLignes() == 2 && df2.getNbCol() == 2) { ok++; System.out.println("PASS constructeur avec tableau"); } else System.out.println("FAIL constructeur avec tableau");
    } catch (Exception e) { System.out.println("FAIL constructeur avec tableau : " + e); }

    // ── 3. Constructeur avec dimensions incohérentes → OutOfBoundException ────
    try {
        new DfIndividu(5, new String[]{"x"}, new Object[][]{{"A"}, {"B"}});
        System.out.println("FAIL nbLignes incohérent (exception attendue)");
    } catch (OutOfBoundException e) { tot++; ok++; System.out.println("PASS nbLignes incohérent → OutOfBoundException"); }
    catch (Exception e) { System.out.println("FAIL mauvaise exception"); }

    // ── 4. Constructeur avec nbLignes=0 → NullParameterException ─────────────
    try {
        new DfIndividu(0, new String[]{"x"}, new Object[0][1]);
        System.out.println("FAIL nbLignes=0 (exception attendue)");
    } catch (NullParameterException e) { tot++; ok++; System.out.println("PASS nbLignes=0 → NullParameterException"); }
    catch (Exception e) { System.out.println("FAIL mauvaise exception"); }

    // ── 5. Constructeur avec nomColonne vide → NullParameterException ────────
    try {
        new DfIndividu(3, new String[]{}, new Object[3][0]);
        System.out.println("FAIL nomColonne vide (exception attendue)");
    } catch (NullParameterException e) { tot++; ok++; System.out.println("PASS nomColonne vide → NullParameterException"); }
    catch (Exception e) { System.out.println("FAIL mauvaise exception"); }

    // ── 6. getCase et setCase ────────────────────────────────────────────────
    try {
        Object[][] data = {{"Merlan", 30.5}, {"Hareng", 25.0}};
        DfIndividu df3 = new DfIndividu(2, new String[]{"espece", "longueur"}, data);

        tot++; if ("Merlan".equals(df3.getCase(0, 0))) { ok++; System.out.println("PASS getCase(0,0)"); } else System.out.println("FAIL getCase");

        df3.setCase(0, 1, 99.9);
        tot++; if (Double.valueOf(99.9).equals(df3.getCase(0, 1))) { ok++; System.out.println("PASS setCase()"); } else System.out.println("FAIL setCase");

        // getColonne
        List<Object> col = df3.getColonne(0);
        tot++; if (col.size() == 2 && "Merlan".equals(col.get(0))) { ok++; System.out.println("PASS getColonne()"); } else System.out.println("FAIL getColonne");

        // getCase hors borne
        try {
            df3.getCase(99, 0);
            System.out.println("FAIL getCase index invalide");
        } catch (OutOfBoundException e) { tot++; ok++; System.out.println("PASS getCase index invalide → exception"); }

        // setCase hors borne
        try {
            df3.setCase(0, 99, "X");
            System.out.println("FAIL setCase index invalide");
        } catch (OutOfBoundException e) { tot++; ok++; System.out.println("PASS setCase index invalide → exception"); }

        // getColonne hors borne
        try {
            df3.getColonne(99);
            System.out.println("FAIL getColonne index invalide");
        } catch (OutOfBoundException e) { tot++; ok++; System.out.println("PASS getColonne index invalide → exception"); }

    } catch (Exception e) { System.out.println("FAIL getCase/setCase : " + e); }

    // ── 7. getTitle (version corrigée) ────────────────────────────────────────
    // DfIndividu.getTitle() retourne "Etude d'individu" ou "Etude d'individu : titre"
    DfIndividu dfTitle = new DfIndividu(1, new String[]{"x"});
    String title = dfTitle.getTitle();
    tot++; if (title != null && title.contains("Etude") && title.contains("individu")) { 
        ok++; System.out.println("PASS getTitle() = \"" + title + "\""); 
    } else { 
        System.out.println("FAIL getTitle() = \"" + title + "\""); 
    }

    // Test avec titre personnalisé
    try {
        DfIndividu dfTitle2 = new DfIndividu(1, new String[]{"x"}, new Object[][]{{1}}, "Mon test");
        String title2 = dfTitle2.getTitle();
        tot++; if (title2 != null && title2.contains("Mon test")) { 
            ok++; System.out.println("PASS getTitle() avec titre = \"" + title2 + "\""); 
        } else { 
            System.out.println("FAIL getTitle() avec titre = \"" + title2 + "\""); 
        }
    } catch (Exception e) { System.out.println("FAIL getTitle avec titre : " + e); }

    System.out.println("\n=== DataframeBase : " + ok + "/" + tot + " ===");
}
}

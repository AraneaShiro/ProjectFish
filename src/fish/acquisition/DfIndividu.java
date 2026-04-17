// ── Package ────────────────────────────────────────────────────────────
package fish.acquisition;

// ── Import ────────────────────────────────────────────────────────────
import fish.exceptions.*;
import fish.poisson.Contenu;
import fish.poisson.Individu;
import fish.poisson.Population;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// ── Test : NON ────────────────────────────────────────────────────────────

/**
 * Dataframe pour un CSV de poissons individuels.
 * Contient une unique Population composée de plusieurs Individus.
 *
 * @author Jules Grenesche
 * @version 0.3
 */
public class DfIndividu extends DataframeComplet implements Utilitaire {

    ////////////////////////////// Attributs ////////////////////

    /**
     * La population étudiée
     * 
     */
    private Population population;

    /** Le type d'étude ici Individu */
    private static final String TYPE = "Etude d'individu";

    /** Le titre du dataFrame */
    private String titre;

    // ───────────────────────────────── Constantes colonnes

    /* Les différents cle de base */
    /** la constante de la colonne pour espece */
    private static final String CLE_ESPECE = "espece";
    /** la constante de la colonne pour longueur */
    private static final String CLE_LONGUEUR = "longueur";
    /** la constante de la colonne pour poids */
    private static final String CLE_POIDS = "poids";
    /** la constante de la colonne pour nbvers */
    private static final String CLE_NBVERS = "nbvers";
    /** la constante de la colonne pour taux */
    private static final String CLE_TAUX = "taux";

    // ─────────────────────────────────Getter / Setter

    /**
     * Get la population du dataframe
     * 
     * @return la population du dataframe
     */
    public Population getPopulation() {
        return population;
    }

    /**
     * Get le titre du dataframe
     * 
     * @return le titre du dataframe
     */
    public String getTitre() {
        return titre;
    }

    /**
     * Renomme manuellement le dataframe
     *
     * @param titre le nouveau titre
     * @throws EmptyStringException si le titre est vide
     */
    public void setTitre(String titre) throws EmptyStringException {
        if (titre == null || titre.isBlank()) {
            throw new EmptyStringException();
        }
        this.titre = titre;
    }

    // ───────────────────────────── Constructeurs ─────────────────────────────

    /**
     * Constructeur sans tableau de données.
     *
     * @param nbLignes   le nombre de lignes
     * @param nomColonne les noms des colonnes
     */
    /**
     * Constructeur sans tableau de données.
     * Crée un dataframe vide avec le nombre de lignes et les noms de colonnes donnés.
     *
     * @param nbLignes   le nombre de lignes
     * @param nomColonne les noms des colonnes
     */
    public DfIndividu(int nbLignes, String[] nomColonne) {
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
    public DfIndividu(int nbLignes, String[] nomColonne, Object[][] newtab)
            throws OutOfBoundException, NullParameterException {
        super(nbLignes, nomColonne, newtab);
        try {
            this.population = construirePopulation();
        } catch (Exception e) {
            System.out.println("Avertissement : impossible de construire la population : " + e.getMessage());
        }
    }

    /**
     * Constructeur avec tableau ET titre manuel
     *
     * @param nbLignes   le nombre de lignes
     * @param nomColonne les noms des colonnes
     * @param newtab     le tableau de données
     * @param titre      le nom du dataframe
     */
    public DfIndividu(int nbLignes, String[] nomColonne, Object[][] newtab, String titre)
            throws OutOfBoundException, NullParameterException, EmptyStringException {
        this(nbLignes, nomColonne, newtab);
        setTitre(titre);
    }

    // ─────────────────────── Utilitaires ───────────────────────

    /**
     * Fonction utilitaire qui trouve l'index de la
     * colonne en fonction du nom donné
     *
     * @param motCle le nom de la colonne que l'on recherche
     * @return le numéro de la colonne ou -1 si elle n'est pas trouvée
     */

    public int getIndexColonne(String motCle) {
        String[] noms = getNomColonnes();
        for (int j = 0; j < noms.length; j++) {
            if (noms[j].toLowerCase().contains(motCle.toLowerCase()))
                return j;
        }
        return -1;
    }

    /**
     * Fonction utilitaire qui lit si c est possible en Float
     * et le renvoie ou une valeur par defaut
     *
     * @param ligne  la ligne de la case a lire
     * @param col    la colonne de la case a lire
     * @param defaut la valeur par defaut si illisible ou mauvaise coordonnée
     * @return la valeur ou la valeur par defaut
     */
    public float lireFloat(int ligne, int col, float defaut) {
        if (col < 0)
            return defaut;
        try {
            Object val = getCase(ligne, col);
            if (val instanceof Number)
                return ((Number) val).floatValue();
        } catch (OutOfBoundException e) {
            /* ignoré */ }
        return defaut;
    }

    /**
     * Fonction utilitaire qui lit si c est possible en int
     * et le renvoie ou une valeur par defaut
     *
     * @param ligne  la ligne de la case a lire
     * @param col    la colonne de la case a lire
     * @param defaut la valeur par defaut si illisible ou mauvaise coordonnée
     * @return la valeur ou la valeur par defaut
     */
    public int lireInt(int ligne, int col, int defaut) {
        if (col < 0)
            return defaut;
        try {
            Object val = getCase(ligne, col);
            if (val instanceof Number)
                return ((Number) val).intValue();
        } catch (OutOfBoundException e) {
            /* ignoré */ }
        return defaut;
    }

    /**
     * Fonction utilitaire qui lit si c est possible en String
     * et le renvoie ou une valeur par defaut
     *
     * @param ligne la ligne de la case a lire
     * @param col   la colonne de la case a lire
     * @return la valeur ou un string vide
     */
    public String lireString(int ligne, int col) {
        if (col < 0)
            return "";
        try {
            Object val = getCase(ligne, col);
            return val != null ? val.toString() : "";
        } catch (OutOfBoundException e) {
            return "";
        }
    }

    // ───────────────────────── Construction ─────────────────────────

    /**
     * Construit une liste d'individu a partir du dataframe
     *
     * @return la liste des individus obtenus
     */
    public List<Individu> construireIndividus() {
        List<Individu> individus = new ArrayList<>(); // Liste pour le resultat

        // On prend tous les index de base
        int iEspece = getIndexColonne(CLE_ESPECE);
        int iLongueur = getIndexColonne(CLE_LONGUEUR);
        int iPoids = getIndexColonne(CLE_POIDS);
        int iNbVers = getIndexColonne(CLE_NBVERS);
        int iTaux = getIndexColonne(CLE_TAUX);

        String[] noms = getNomColonnes();
        // On recupére les colonnes d'organes
        List<Integer> colonnesOrganes = new ArrayList<>();
        for (int j = 0; j < noms.length; j++) {
            String n = noms[j].toLowerCase();
            if (!n.contains(CLE_ESPECE) && !n.contains(CLE_LONGUEUR)
                    && !n.contains(CLE_POIDS) && !n.contains(CLE_NBVERS)
                    && !n.contains(CLE_TAUX)) {
                colonnesOrganes.add(j);
            }
        }
        // pour chaque ligne du dataframe
        for (int i = 0; i < getNbLignes(); i++) {

            // On met les données des colonnes connus
            String espece = lireString(i, iEspece);
            if (espece == null || espece.isBlank()) {
                espece = "";
            }
            float longueur = lireFloat(i, iLongueur, 0f);
            float poids = lireFloat(i, iPoids, 0f);
            int nbVers = lireInt(i, iNbVers, -1);
            float taux = lireFloat(i, iTaux, -1f);

            ArrayList<Contenu> contenus = new ArrayList<>();

            for (int colOrgane : colonnesOrganes) {
                try {
                    Object val = getCase(i, colOrgane);
                    if (val instanceof Number) {
                        float valF = ((Number) val).floatValue();
                        contenus.add(valF <= 1f // Si la valeur est inf a 1 c est un taux
                                ? new Contenu(noms[colOrgane], valF)
                                : new Contenu(noms[colOrgane], (int) valF));
                    }
                } catch (Exception e) {
                    System.out.println("Contenu ignoré ligne " + i + " : " + e.getMessage());
                }
            }

            try {
                Individu ind = nbVers >= 0
                        ? new Individu(espece, longueur, poids, nbVers, contenus)
                        : taux >= 0
                                ? new Individu(espece, longueur, poids, taux, contenus)
                                : new Individu(espece, longueur, poids, 0, contenus);
                individus.add(ind);
            } catch (Exception e) {
                System.out.println("Individu ligne " + i + " ignoré : " + e.getMessage());
            }
        }
        return individus;
    }

    /**
     * Construit la population
     *
     * @return la population produite
     * @throws NegativeValueException si une valeur negative est trouvée
     * @throws EmptyStringException   si le string est vide alors qu'il ne devrait pas
     * @throws TauxValueException     si le taux est mauvais
     */
    public Population construirePopulation()
            throws NegativeValueException, EmptyStringException, TauxValueException, NoTabException {
        List<Individu> individus = construireIndividus(); // On construit notre liste d'individu
        if (individus.isEmpty())
            return null;

        Individu[] tableau = individus.toArray(new Individu[0]);
        String espece = tableau[0].getEspece(); // on récupère l'espece étudiée

        String[] noms = getNomColonnes();
        StringBuilder parties = new StringBuilder();
        for (String n : noms) {
            String nl = n.toLowerCase();
            if (!nl.contains(CLE_ESPECE) && !nl.contains(CLE_LONGUEUR)
                    && !nl.contains(CLE_POIDS) && !nl.contains(CLE_NBVERS)
                    && !nl.contains(CLE_TAUX)) {
                if (parties.length() > 0)
                    parties.append(", ");
                parties.append(n);
            }
        }

        String partie = parties.length() > 0 ? parties.toString() : "corps entier";
        String[] tabParties = {partie};

        return new Population(individus.size(), espece,
                tabParties,
                tableau);
    }

    /**
     * Recharge la Population après un setCase()
     * 
     */
    public void majPopulation() {
        try {
            this.population = construirePopulation();
        } catch (Exception e) {
            System.out.println("Mise à jour population échouée : " + e.getMessage());
        }
    }

    /**
     * Permet de classer les individus par especes
     * 
     * @return une map avec <Espece, la liste d'individu de l'espece>
     * 
     */
    public Map<String, List<Individu>> getIndividusParEspece() {
        LinkedHashMap<String, List<Individu>> map = new LinkedHashMap<>();
        for (Individu ind : construireIndividus()) {
            map.computeIfAbsent(ind.getEspece(), k -> new ArrayList<>()).add(ind); // le fait si il n'est deja pas
                                                                                   // present
        }
        return map;
    }

    /**
     * Surcharge de la methode toString
     * 
     * @return un string sous la forme de Individu Titre
     */
    @Override
    public String getTitle() {
        return TYPE + (titre != null ? " : " + titre : "");
    }

    /**
     * Crée une copie profonde (deep copy) du dataframe.
     * Le tableau de données et les noms de colonnes sont dupliqués indépendamment.
     *
     * @return un nouveau {@code DfIndividu} avec les mêmes données
     * @throws Exception si la construction du duplicata échoue
     */
   public DfIndividu copy() throws  Exception{
    String[] newCols = this.nomColonne.clone();

    Object[][] newTab = new Object[this.nbLignes][this.nbCol];
    for (int i = 0; i < this.nbLignes; i++) {
        for (int j = 0; j < this.nbCol; j++) {
            newTab[i][j] = this.tableau[i][j];
        }
    }

    DfIndividu copyDf = new DfIndividu(this.nbLignes, newCols, newTab);
    copyDf.statistiques = new HashMap<>(this.statistiques);

    return copyDf;
}

    /**
     * Tests unitaires du DfIndividu.
     * Vérifie la construction depuis un tableau, les getters, la construction
     * de la population et l'indexation des colonnes.
     *
     * @param args arguments de la ligne de commande (ignorés)
     */
    public  static void main(String[] args) {
        int ok = 0, total = 0;

        //Lecture du fichier mackerel.97442.csv (séparateur ;)
        fish.acquisition.lecture.LectureCSV lecteur = new fish.acquisition.lecture.LectureCSV(";");

        // ── Construction depuis mackerel.97442.csv ────────────────────────────
        System.out.println("── Lecture mackerel.97442.csv ───────────────────────");

        DfIndividu dfMackerel = null;
        try {
            dfMackerel = lecteur.lireCSV("data/mackerel.97442.csv", DfIndividu.class);
        } catch (Exception e) { System.out.println("Chargement échoué : " + e); }

        // Test 1 : lecture sans exception, df non null
        total++;
        if (dfMackerel != null) {
            System.out.println("PASS Test 1 : mackerel chargé, " + dfMackerel.getNbLignes() + " lignes");
            ok++;
            dfMackerel.afficherPremieresFignes(5);
            
        } else {
            System.out.println("FAIL Test 1 : dfMackerel est null");
        }

        // Test 2 : nb colonnes = 20 (entêtes du fichier)
        total++;
        if (dfMackerel != null && dfMackerel.getNbCol() == 20) {
            System.out.println("PASS Test 2 : 20 colonnes");
            ok++;
        } else {
            System.out.println("FAIL Test 2 : colonnes = " + (dfMackerel != null ? dfMackerel.getNbCol() : "N/A"));
        }

        // Test 3 : construireIndividus() → liste non vide
        total++;
        try {
            List<Individu> inds = dfMackerel.construireIndividus();
            if (!inds.isEmpty()) {
                System.out.println("PASS Test 3 : construireIndividus() → " + inds.size() + " individus");
                ok++;
            } else {
                System.out.println("FAIL Test 3 : liste vide");
            }
        } catch (Exception e) { System.out.println("FAIL Test 3 : " + e); }

        // Test 4 : construirePopulation() → population non null
        total++;
        try {
            Population pop = dfMackerel.construirePopulation();
            if (pop != null && pop.getEffectif() > 0) {
                System.out.println("PASS Test 4 : construirePopulation() → effectif " + pop.getEffectif());
                ok++;
            } else {
                System.out.println("FAIL Test 4 : population null ou effectif 0");
            }
        } catch (Exception e) { System.out.println("FAIL Test 4 : " + e); }

        // ── Construction depuis merlu2018_75164.csv ───────────────────────────
        System.out.println("\n── Lecture merlu2018_75164.csv ──────────────────────");

        DfIndividu dfMerlu = null;
        fish.acquisition.lecture.LectureCSV lecteurMerlu = new fish.acquisition.lecture.LectureCSV(";","windows-1252");
        try {
            dfMerlu = lecteurMerlu.lireCSV("data/merlu2018_75164.csv", DfIndividu.class);
        } catch (Exception e) { System.out.println("Chargement échoué : " + e); }

        // Test 5 : lecture sans exception
        total++;
        if (dfMerlu != null) {
            System.out.println("PASS Test 5 : merlu chargé, " + dfMerlu.getNbLignes() + " lignes");
            ok++;
            dfMerlu.afficherPremieresFignes(5);
            for(int i=0;i<dfMerlu.nomColonne.length;i++){
                System.out.println(i+" : " +dfMerlu.nomColonne[i]);
            }

        } else {
            System.out.println("FAIL Test 5 : dfMerlu est null");
        }
/* 
        // Test 6 : 20 lignes de données
        total++;
        if (dfMerlu != null && dfMerlu.getNbLignes() == 20) {
            System.out.println("PASS Test 6 : 20 lignes");
            ok++;
        } else {
            System.out.println("FAIL Test 6 : lignes = " + (dfMerlu != null ? dfMerlu.getNbLignes() : "N/A"));
        }

        // ── getIndexColonne ───────────────────────────────────────────────────
        System.out.println("\n── getIndexColonne ──────────────────────────────────");

        // Test 7 : recherche d'un mot-clé présent
        total++;
        try {
            Object[][] data = {{"Merlan", 30.5}};
            DfIndividu dfTest = new DfIndividu(1, new String[]{"espece", "longueur"}, data);
            int idx = dfTest.getIndexColonne("longueur");
            if (idx == 1) {
                System.out.println("PASS Test 7 : getIndexColonne('longueur') = 1");
                ok++;
            } else {
                System.out.println("FAIL Test 7 : attendu 1, obtenu " + idx);
            }
        } catch (Exception e) { System.out.println("FAIL Test 7 : " + e); }

        // Test 8 : mot-clé absent → -1
        total++;
        try {
            Object[][] data = {{"Merlan"}};
            DfIndividu dfTest = new DfIndividu(1, new String[]{"espece"}, data);
            int idx = dfTest.getIndexColonne("inexistant");
            if (idx == -1) {
                System.out.println("PASS Test 8 : getIndexColonne mot-clé absent → -1");
                ok++;
            } else {
                System.out.println("FAIL Test 8 : attendu -1, obtenu " + idx);
            }
        } catch (Exception e) { System.out.println("FAIL Test 8 : " + e); }

        // ── setTitre / getTitre ───────────────────────────────────────────────
        System.out.println("\n── setTitre / getTitre ──────────────────────────────");

        // Test 9 : setTitre valide
        total++;
        try {
            DfIndividu dfTest = new DfIndividu(1, new String[]{"x"});
            dfTest.setTitre("Test Maquereau");
            if ("Test Maquereau".equals(dfTest.getTitre())) {
                System.out.println("PASS Test 9 : setTitre/getTitre fonctionnels");
                ok++;
            } else {
                System.out.println("FAIL Test 9 : titre = " + dfTest.getTitre());
            }
        } catch (Exception e) { System.out.println("FAIL Test 9 : " + e); }

        // Test 10 : setTitre vide → EmptyStringException
        total++;
        try {
            DfIndividu dfTest = new DfIndividu(1, new String[]{"x"});
            dfTest.setTitre("");
            System.out.println("FAIL Test 10 : EmptyStringException attendue non levée");
        } catch (fish.exceptions.EmptyStringException e) {
            System.out.println("PASS Test 10 : setTitre('') → EmptyStringException");
            ok++;
        } catch (Exception e) { System.out.println("FAIL Test 10 : " + e); }

        // ── lireFloat / lireInt / lireString ──────────────────────────────────
        System.out.println("\n── lireFloat / lireInt / lireString ────────────────");

        // Test 11 : lireFloat sur une valeur Double → ok
        total++;
        try {
            Object[][] data = {{30.5, 5}};
            DfIndividu dfTest = new DfIndividu(1, new String[]{"lon", "nv"}, data);
            float f = dfTest.lireFloat(0, 0, -1f);
            if (Math.abs(f - 30.5f) < 1e-4) {
                System.out.println("PASS Test 11 : lireFloat → 30.5");
                ok++;
            } else {
                System.out.println("FAIL Test 11 : attendu 30.5, obtenu " + f);
            }
        } catch (Exception e) { System.out.println("FAIL Test 11 : " + e); }

        // Test 12 : lireInt sur un Integer → ok
        total++;
        try {
            Object[][] data = {{30.5, 5}};
            DfIndividu dfTest = new DfIndividu(1, new String[]{"lon", "nv"}, data);
            int i = dfTest.lireInt(0, 1, -1);
            if (i == 5) {
                System.out.println("PASS Test 12 : lireInt → 5");
                ok++;
            } else {
                System.out.println("FAIL Test 12 : attendu 5, obtenu " + i);
            }
        } catch (Exception e) { System.out.println("FAIL Test 12 : " + e); }

        // Test 13 : lireString sur String → ok
        total++;
        try {
            Object[][] data = {{"Merlan"}};
            DfIndividu dfTest = new DfIndividu(1, new String[]{"espece"}, data);
            String s = dfTest.lireString(0, 0);
            if ("Merlan".equals(s)) {
                System.out.println("PASS Test 13 : lireString → 'Merlan'");
                ok++;
            } else {
                System.out.println("FAIL Test 13 : attendu 'Merlan', obtenu '" + s + "'");
            }
        } catch (Exception e) { System.out.println("FAIL Test 13 : " + e); }

        // ── getIndividusParEspece ─────────────────────────────────────────────
        System.out.println("\n── getIndividusParEspece ────────────────────────────");

        // Test 14 : regroupement par espèce sur mackerel
        total++;
        try {
            Map<String, List<Individu>> map = dfMackerel.getIndividusParEspece();
            if (!map.isEmpty()) {
                System.out.println("PASS Test 14 : " + map.size() + " espèce(s) distincte(s)");
                ok++;
            } else {
                System.out.println("FAIL Test 14 : map vide");
            }
        } catch (Exception e) { System.out.println("FAIL Test 14 : " + e); }
        System.out.println("\n═══════════════════════════════════════════════════");
        System.out.println("=== DfIndividu : " + ok + "/" + total + " tests réussis ===");
        System.out.println("═══════════════════════════════════════════════════");*/
    }
}
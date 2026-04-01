// ── Package ────────────────────────────────────────────────────────────
package fish.acquisition;

// ── Import ────────────────────────────────────────────────────────────
import fish.exceptions.*;
import fish.poisson.Contenu;
import fish.poisson.Individu;
import fish.poisson.Population;

import java.util.ArrayList;
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
public class DfIndividu extends DataframeComplet {

    ////////////////////////////// Attributs ////////////////////

    /**
     * La population étudie
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
     * colonne en fonction du nom donnée
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
     * Fonction utilitaire qui lis si c est possible en Float
     * et le renvoie ou une valeur par defaut
     *
     * @param ligne  la ligne de la case a lire
     * @param col    la colonne de la case a lire
     * @param defaut la valeur par defaut si illisible ou mauvaise coordonné
     * @return la valeur ou la valeur par defaut
     */
    private float lireFloat(int ligne, int col, float defaut) {
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

    private int lireInt(int ligne, int col, int defaut) {
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

    private String lireString(int ligne, int col) {
        if (col < 0)
            return "";
        try {
            Object val = getCase(ligne, col);
            return val != null ? val.toString() : "";
        } catch (OutOfBoundException e) {
            return "";
        }
    }

    ////////////////////////////// Construction ////////////////////

    public List<Individu> construireIndividus() {
        List<Individu> individus = new ArrayList<>();

        int iEspece = getIndexColonne(CLE_ESPECE);
        int iLongueur = getIndexColonne(CLE_LONGUEUR);
        int iPoids = getIndexColonne(CLE_POIDS);
        int iNbVers = getIndexColonne(CLE_NBVERS);
        int iTaux = getIndexColonne(CLE_TAUX);

        String[] noms = getNomColonnes();
        List<Integer> colonnesOrganes = new ArrayList<>();
        for (int j = 0; j < noms.length; j++) {
            String n = noms[j].toLowerCase();
            if (!n.contains(CLE_ESPECE) && !n.contains(CLE_LONGUEUR)
                    && !n.contains(CLE_POIDS) && !n.contains(CLE_NBVERS)
                    && !n.contains(CLE_TAUX)) {
                colonnesOrganes.add(j);
            }
        }

        for (int i = 0; i < getNbLignes(); i++) {
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
                        contenus.add(valF <= 1f
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

    public Population construirePopulation()
            throws NegativeValueException, EmptyStringException, TauxValueException {
        List<Individu> individus = construireIndividus();
        if (individus.isEmpty())
            return null;

        Individu[] tableau = individus.toArray(new Individu[0]);
        String espece = tableau[0].getEspece();

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

        return new Population(individus.size(), espece,
                parties.length() > 0 ? parties.toString() : "corps entier",
                tableau);
    }

    /** Recharge la Population après un setCase() */
    public void majPopulation() {
        try {
            this.population = construirePopulation();
        } catch (Exception e) {
            System.out.println("Mise à jour population échouée : " + e.getMessage());
        }
    }

    public Map<String, List<Individu>> getIndividusParEspece() {
        LinkedHashMap<String, List<Individu>> map = new LinkedHashMap<>();
        for (Individu ind : construireIndividus()) {
            map.computeIfAbsent(ind.getEspece(), k -> new ArrayList<>()).add(ind); // le fait si il n'est deja pas
                                                                                   // present
        }
        return map;
    }

    @Override
    public String getTitle() {
        return TYPE + (titre != null ? " : " + titre : "");
    }
}
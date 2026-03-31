package fish.acquisition;

import fish.exceptions.*;
import fish.poisson.Population;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Dataframe pour un CSV de populations.
 *
 * Supporte deux formats :
 *
 * FORMAT STANDARD — une ligne = une population :
 * Colonnes : espece | effectif/N | infectes | partie | intensite | abondance
 *
 * FORMAT MULTI-PERIODE (ex: ParasitesPeru après pivot) — une ligne = une
 * espèce,
 * les colonnes sont de la forme "Paramètre_Période" :
 * Espèce | N_Total | N_2012 | Prévalence (%)_Total | Abondance moyenne_2013 |
 * ...
 * produit une Population par espèce × période
 *
 * @author Jules Grenesche
 * @version 0.3
 */
public class DfPopulation extends Dataframe {

    ////////////////////////////// Attributs ////////////////////

    /** Populations construites depuis le tableau */
    private Population[] populations;

    /** true si le format est multi-période (style Peru) */
    private boolean formatMultiPeriode;

    /**
     * Périodes détectées dans le format multi-période (ex: ["Total","2012","2013"])
     */
    private String[] periodes;

    ////////////////////////////// Constantes colonnes

    private static final String CLE_ESPECE = "espece";
    private static final String CLE_INFECTES = "infect";
    private static final String CLE_PREVALENCE = "prévalence";
    private static final String CLE_INTENSITE = "intensit";
    private static final String CLE_ABONDANCE = "abondance";
    private static final String CLE_N = "n_"; // ex: "N_Total", "N_2012"
    private static final String CLE_N_EXACT = "n";

    ////////////////////////////// Getter ////////////////////

    public Population[] getPopulations() {
        return populations;
    }

    public boolean isFormatMultiPeriode() {
        return formatMultiPeriode;
    }

    public String[] getPeriodes() {
        return periodes;
    }

    /**
     * Retourne la population à l'index donné
     *
     * @param index l'index de la population
     * @throws OutOfBoundException si l'index est invalide
     */
    public Population getPopulation(int index) throws OutOfBoundException {
        if (index < 0 || index >= populations.length) {
            throw new OutOfBoundException(index, populations.length, 1);
        }
        return populations[index];
    }

    /**
     * Retourne toutes les populations d'une espèce (utile en multi-période)
     *
     * @param espece le nom de l'espèce
     */
    public List<Population> getPopulationsParEspece(String espece) {
        List<Population> resultat = new ArrayList<>();
        for (Population pop : populations) {
            if (pop.getEspece().equalsIgnoreCase(espece))
                resultat.add(pop);
        }
        return resultat;
    }

    ////////////////////////////// Constructeurs ////////////////////

    public DfPopulation(int nbLignes, String[] nomColonne) {
        super(nbLignes, nomColonne);
    }

    /**
     * Constructeur avec tableau — utilisé par la réflexion dans LectureCSV.
     * Détecte automatiquement le format et construit les populations.
     */
    public DfPopulation(int nbLignes, String[] nomColonne, Object[][] newtab)
            throws OutOfBoundException, NullParameterException {
        super(nbLignes, nomColonne, newtab);
        this.formatMultiPeriode = detecterFormatMultiPeriode();
        if (this.formatMultiPeriode) {
            this.periodes = extrairePeriodes();
            this.populations = construirePopulationsMultiPeriode();
        } else {
            this.populations = construirePopulationsStandard();
        }
    }

    ////////////////////////////// Détection du format

    /**
     * Détecte si le tableau est au format multi-période :
     * au moins 2 colonnes contiennent un "_" et le même préfixe avant "_"
     * (ex: "N_Total" et "N_2012" → préfixe "N" présent 2 fois)
     */
    private boolean detecterFormatMultiPeriode() {
        String[] noms = getNomColonnes();
        Map<String, Integer> compteurPrefixes = new LinkedHashMap<>();
        for (String nom : noms) {
            int idx = nom.lastIndexOf('_');
            if (idx > 0) {
                String prefixe = nom.substring(0, idx);
                compteurPrefixes.merge(prefixe, 1, Integer::sum);
            }
        }
        // Multi-période si au moins un préfixe apparaît 2+ fois
        for (int count : compteurPrefixes.values()) {
            if (count >= 2)
                return true;
        }
        return false;
    }

    /**
     * Extrait les suffixes de période depuis les noms de colonnes.
     * Ex: ["N_Total","N_2012","N_2013"] → ["Total","2012","2013"]
     */
    private String[] extrairePeriodes() {
        String[] noms = getNomColonnes();

        // Cherche le préfixe le plus fréquent avec "_"
        Map<String, List<String>> prefixToSuffixes = new LinkedHashMap<>();
        for (String nom : noms) {
            int idx = nom.lastIndexOf('_');
            if (idx > 0) {
                String prefixe = nom.substring(0, idx);
                String suffixe = nom.substring(idx + 1);
                prefixToSuffixes.computeIfAbsent(prefixe, k -> new ArrayList<>()).add(suffixe);
            }
        }

        // Prend les suffixes du préfixe le plus fréquent
        String meilleurPrefixe = null;
        int maxCount = 0;
        for (Map.Entry<String, List<String>> e : prefixToSuffixes.entrySet()) {
            if (e.getValue().size() > maxCount) {
                maxCount = e.getValue().size();
                meilleurPrefixe = e.getKey();
            }
        }

        if (meilleurPrefixe == null)
            return new String[0];
        return prefixToSuffixes.get(meilleurPrefixe).toArray(new String[0]);
    }

    ////////////////////////////// Utilitaires ////////////////////

    public int getIndexColonne(String motCle) {
        String[] noms = getNomColonnes();
        for (int j = 0; j < noms.length; j++) {
            if (noms[j].toLowerCase().contains(motCle.toLowerCase()))
                return j;
        }
        return -1;
    }

    /**
     * Cherche une colonne dont le nom contient motCle ET suffixe (ex: "N" + "2012")
     */
    private int getIndexColonnePeriode(String motCle, String periode) {
        String[] noms = getNomColonnes();
        for (int j = 0; j < noms.length; j++) {
            String n = noms[j].toLowerCase();
            if (n.contains(motCle.toLowerCase()) && noms[j].endsWith("_" + periode))
                return j;
        }
        return -1;
    }

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
            return "Inconnue";
        try {
            Object val = getCase(ligne, col);
            return val != null ? val.toString() : "Inconnue";
        } catch (OutOfBoundException e) {
            return "Inconnue";
        }
    }

    ////////////////////////////// Format standard

    /**
     * Construit un tableau de Population : une par ligne (format standard)
     */
    public Population[] construirePopulationsStandard() {
        List<Population> liste = new ArrayList<>();

        int iEspece = getIndexColonne(CLE_ESPECE);
        int iEffectif = getIndexColonne("effectif");
        if (iEffectif < 0)
            iEffectif = getIndexColonne(CLE_N_EXACT);
        int iInfectes = getIndexColonne(CLE_INFECTES);
        int iPartie = getIndexColonne("partie");
        int iIntensite = getIndexColonne(CLE_INTENSITE);
        int iAbondance = getIndexColonne(CLE_ABONDANCE);

        for (int i = 0; i < getNbLignes(); i++) {
            String espece = lireString(i, iEspece);
            int effectif = lireInt(i, iEffectif, 0);
            int infectes = lireInt(i, iInfectes, 0);
            String partie = lireString(i, iPartie);
            float intensite = lireFloat(i, iIntensite, 0f);
            float abondance = lireFloat(i, iAbondance, 0f);

            try {
                liste.add(new Population(effectif, espece, infectes, partie, intensite, abondance));
            } catch (Exception e) {
                System.out.println("Population ligne " + i + " ignorée : " + e.getMessage());
            }
        }
        return liste.toArray(new Population[0]);
    }

    ////////////////////////////// Format multi-période (style Peru)

    /**
     * Construit un tableau de Population : une par espèce × période.
     * Ex: 4 espèces × 3 périodes (Total, 2012, 2013) = 12 populations
     */
    public Population[] construirePopulationsMultiPeriode() {
        List<Population> liste = new ArrayList<>();

        int iEspece = getIndexColonne(CLE_ESPECE);

        for (int i = 0; i < getNbLignes(); i++) {
            String espece = lireString(i, iEspece);

            for (String periode : periodes) {

                // Effectif : colonne "N_Periode"
                int iN = getIndexColonnePeriode(CLE_N, periode);
                if (iN < 0)
                    iN = getIndexColonnePeriode(CLE_N_EXACT, periode);
                int effectif = lireInt(i, iN, 0);

                // Prévalence → nombre infectés = prévalence% × effectif
                int iPrevalence = getIndexColonnePeriode(CLE_PREVALENCE, periode);
                float prevalence = lireFloat(i, iPrevalence, 0f);
                int infectes = Math.round(prevalence / 100f * effectif);

                // Intensité et abondance
                int iIntensite = getIndexColonnePeriode(CLE_INTENSITE, periode);
                int iAbondance = getIndexColonnePeriode(CLE_ABONDANCE, periode);
                float intensite = lireFloat(i, iIntensite, 0f);
                float abondance = lireFloat(i, iAbondance, 0f);

                // Partie du corps = la période (ex: "Total", "2012")
                String partie = periode;

                try {
                    if (effectif > 0) { // On ignore les périodes sans données
                        liste.add(new Population(effectif, espece, infectes, partie, intensite, abondance));
                    }
                } catch (Exception e) {
                    System.out.println("Population " + espece + " / " + periode + " ignorée : " + e.getMessage());
                }
            }
        }
        return liste.toArray(new Population[0]);
    }

    ////////////////////////////// Mise à jour

    /**
     * Recharge toutes les populations depuis le tableau actuel.
     * À appeler après un setCase().
     */
    public void majPopulations() {
        if (this.formatMultiPeriode) {
            this.populations = construirePopulationsMultiPeriode();
        } else {
            this.populations = construirePopulationsStandard();
        }
    }

    /**
     * Met à jour une population à l'index et répercute dans le tableau.
     *
     * @param index       l'index de la population à modifier
     * @param nouvellePop la nouvelle population
     * @throws OutOfBoundException si l'index est invalide
     */
    public void setPopulation(int index, Population nouvellePop) throws OutOfBoundException {
        if (index < 0 || index >= populations.length) {
            throw new OutOfBoundException(index, populations.length, 1);
        }
        populations[index] = nouvellePop;
        if (!formatMultiPeriode) {
            repercuterDansTableau(index, nouvellePop);
        }
    }

    private void repercuterDansTableau(int ligne, Population pop) {
        int iEspece = getIndexColonne(CLE_ESPECE);
        int iEffectif = getIndexColonne("effectif");
        if (iEffectif < 0)
            iEffectif = getIndexColonne(CLE_N_EXACT);
        int iInfectes = getIndexColonne(CLE_INFECTES);
        int iIntensite = getIndexColonne(CLE_INTENSITE);
        int iAbondance = getIndexColonne(CLE_ABONDANCE);
        try {
            if (iEspece >= 0)
                setCase(ligne, iEspece, pop.getEspece());
            if (iEffectif >= 0)
                setCase(ligne, iEffectif, pop.getEffectif());
            if (iInfectes >= 0)
                setCase(ligne, iInfectes, pop.getNbInfectes());
            if (iIntensite >= 0)
                setCase(ligne, iIntensite, pop.getIntensite());
            if (iAbondance >= 0)
                setCase(ligne, iAbondance, pop.getAbondance());
        } catch (OutOfBoundException e) {
            System.out.println("Erreur répercussion tableau : " + e.getMessage());
        }
    }

    ////////////////////////////// Affichage ////////////////////

    public void afficherResume() {
        System.out.println("=== " + getTitle() + " ===");
        if (formatMultiPeriode) {
            System.out.printf("%-35s %-12s %-10s %-10s %-10s%n",
                    "Espèce", "Période", "Effectif", "Infectés", "Abondance");
            System.out.println("-".repeat(80));
            for (Population pop : populations) {
                System.out.printf("%-35s %-12s %-10d %-10d %-10.2f%n",
                        pop.getEspece(), pop.getPartieCorps(),
                        pop.getEffectif(), pop.getNbInfectes(), pop.getAbondance());
            }
        } else {
            System.out.printf("%-35s %-10s %-10s %-10s%n",
                    "Espèce", "Effectif", "Infectés", "Abondance");
            System.out.println("-".repeat(70));
            for (Population pop : populations) {
                System.out.printf("%-35s %-10d %-10d %-10.2f%n",
                        pop.getEspece(), pop.getEffectif(),
                        pop.getNbInfectes(), pop.getAbondance());
            }
        }
    }

    @Override
    public String getTitle() {
        return "Etude de population"
                + (formatMultiPeriode ? " multi-période" : "")
                + " (" + (populations != null ? populations.length : 0) + " populations)";
    }
}
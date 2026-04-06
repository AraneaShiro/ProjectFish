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
 * @version 0.4
 */
public class DfPopulation extends DataframeComplet implements Utilitaire {

    ////////////////////////////// Attributs ////////////////////

    /** Populations construites depuis le tableau */
    private Population[] populations;

    /** true si le format est multi-période (style Peru) */
    private boolean formatMultiPeriode;

    /**
     * Périodes détectées dans le format multi-période (ex: ["Total","2012","2013"])
     */
    private String[] periodes;

    ////////////////////////////// Constantes colonnes ////////////////////

    private static final String CLE_ESPECE    = "espece";
    private static final String CLE_INFECTES  = "infect";
    private static final String CLE_PREVALENCE= "prevalence"; // sans accent, normaliser() gère
    private static final String CLE_INTENSITE = "intensit";
    private static final String CLE_ABONDANCE = "abondance";
    private static final String CLE_N         = "n_";         // ex: "N_Total", "N_2012"
    private static final String CLE_N_EXACT   = "n";

    ////////////////////////////// Getter ////////////////////

    /**
     * Retourne le tableau des populations construites
     *
     * @return le tableau de Population
     */
    public Population[] getPopulations() {
        return populations;
    }

    /**
     * Retourne si le format est multi-période
     *
     * @return true si multi-période
     */
    public boolean isFormatMultiPeriode() {
        return formatMultiPeriode;
    }

    /**
     * Retourne les périodes détectées
     *
     * @return tableau des périodes
     */
    public String[] getPeriodes() {
        return periodes;
    }

    /**
     * Retourne la population à l'index donné
     *
     * @param index l'index de la population
     * @return la Population à cet index
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
     * @return une liste de populations de cette espèce
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

    /**
     * Constructeur sans tableau de données.
     *
     * @param nbLignes   le nombre de lignes
     * @param nomColonne les noms des colonnes
     */
    public DfPopulation(int nbLignes, String[] nomColonne) {
        super(nbLignes, nomColonne);
    }

    /**
     * Constructeur avec tableau de données.
     * Détecte automatiquement le format et construit les populations.
     *
     * @param nbLignes   le nombre de lignes
     * @param nomColonne les noms des colonnes
     * @param newtab     le tableau de données
     * @throws OutOfBoundException    si les dimensions ne correspondent pas
     * @throws NullParameterException si les paramètres sont vides ou null
     */
    public DfPopulation(int nbLignes, String[] nomColonne, Object[][] newtab)
            throws OutOfBoundException, NullParameterException {
        super(nbLignes, nomColonne, newtab);
        this.formatMultiPeriode = detecterFormatMultiPeriode();
        if (this.formatMultiPeriode) {
            this.periodes   = extrairePeriodes();
            this.populations = construirePopulationsMultiPeriode();
        } else {
            this.populations = construirePopulationsStandard();
        }
    }

    // ───────────────── Détection du format ─────────────────────────────────────

    /**
     * Détecte si le tableau est au format multi-période.
     * Un suffixe de période valide est : une année (4 chiffres) ou "Total" ou "Moyenne".
     * Cela évite de confondre des colonnes comme "d13C_raw" / "d13C_corr" avec
     * des colonnes de période.
     *
     * @return true si le format est multi-période
     */
    private boolean detecterFormatMultiPeriode() {
        String[] noms = getNomColonnes();
        Map<String, Integer> compteurPrefixes = new LinkedHashMap<>();

        for (String nom : noms) {
            int idx = nom.lastIndexOf('_');
            if (idx > 0) {
                String suffixe = nom.substring(idx + 1);
                // Suffixe valide = année (4 chiffres), "Total" ou "Moyenne"
                if (suffixe.matches("\\d{4}")
                        || suffixe.equalsIgnoreCase("Total")
                        || suffixe.equalsIgnoreCase("Moyenne")) {
                    String prefixe = nom.substring(0, idx);
                    compteurPrefixes.merge(prefixe, 1, Integer::sum);
                }
            }
        }

        for (int count : compteurPrefixes.values()) {
            if (count >= 2) return true;
        }
        return false;
    }

    /**
     * Extrait les suffixes de période depuis les noms de colonnes.
     * Ex: ["N_Total","N_2012","N_2013"] → ["Total","2012","2013"]
     *
     * @return tableau des périodes détectées
     */
    private String[] extrairePeriodes() {
        String[] noms = getNomColonnes();

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
                maxCount        = e.getValue().size();
                meilleurPrefixe = e.getKey();
            }
        }

        if (meilleurPrefixe == null) return new String[0];
        return prefixToSuffixes.get(meilleurPrefixe).toArray(new String[0]);
    }

    ////////////////////////////// Utilitaires ////////////////////

    /**
     * Normalise un String en retirant les accents et en mettant en minuscules.
     * Permet de matcher "Espèce" avec "espece", "Prévalence" avec "prevalence", etc.
     *
     * @param s le String à normaliser
     * @return le String normalisé
     */
    private String normaliser(String s) {
        if (s == null) return "";
        return java.text.Normalizer
                .normalize(s, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}", "")
                .toLowerCase();
    }

    /**
     * Retourne l'index de la première colonne dont le nom normalisé contient motCle.
     *
     * @param motCle le nom de la colonne que l'on recherche
     * @return le numéro de la colonne ou -1 si elle n'est pas trouvée
     */
    public int getIndexColonne(String motCle) {
        String[] noms    = getNomColonnes();
        String   motNorm = normaliser(motCle);
        for (int j = 0; j < noms.length; j++) {
            if (normaliser(noms[j]).contains(motNorm)) return j;
        }
        return -1;
    }

    /**
     * Cherche une colonne dont le nom contient motCle ET se termine par "_periode".
     * Ex: getIndexColonnePeriode("N", "2012") → index de "N_2012"
     *
     * @param motCle  le mot clé recherché
     * @param periode la période recherchée (ex: "2019", "Total")
     * @return l'indice trouvé ou -1 sinon
     */
    private int getIndexColonnePeriode(String motCle, String periode) {
        String[] noms    = getNomColonnes();
        String   motNorm = normaliser(motCle);
        for (int j = 0; j < noms.length; j++) {
            if (normaliser(noms[j]).contains(motNorm)
                    && noms[j].endsWith("_" + periode)) return j;
        }
        return -1;
    }

    /**
     * Lit si possible en Float et retourne la valeur ou le défaut.
     *
     * @param ligne  la ligne de la case à lire
     * @param col    la colonne de la case à lire
     * @param defaut la valeur par défaut si illisible ou mauvaise coordonnée
     * @return la valeur ou la valeur par défaut
     */
    public float lireFloat(int ligne, int col, float defaut) {
        if (col < 0) return defaut;
        try {
            Object val = getCase(ligne, col);
            if (val instanceof Number) return ((Number) val).floatValue();
        } catch (OutOfBoundException e) { /* ignoré */ }
        return defaut;
    }

    /**
     * Lit si possible en int et retourne la valeur ou le défaut.
     *
     * @param ligne  la ligne de la case à lire
     * @param col    la colonne de la case à lire
     * @param defaut la valeur par défaut si illisible ou mauvaise coordonnée
     * @return la valeur ou la valeur par défaut
     */
    public int lireInt(int ligne, int col, int defaut) {
        if (col < 0) return defaut;
        try {
            Object val = getCase(ligne, col);
            if (val instanceof Number) return ((Number) val).intValue();
        } catch (OutOfBoundException e) { /* ignoré */ }
        return defaut;
    }

    /**
     * Lit si possible en String et retourne la valeur ou "Inconnue".
     *
     * @param ligne la ligne de la case à lire
     * @param col   la colonne de la case à lire
     * @return la valeur ou "Inconnue"
     */
    public String lireString(int ligne, int col) {
        if (col < 0) return "Inconnue";
        try {
            Object val = getCase(ligne, col);
            return val != null ? val.toString() : "Inconnue";
        } catch (OutOfBoundException e) {
            return "Inconnue";
        }
    }

    ////////////////////////////// Format standard /////////////////////////////

    /**
     * Construit un tableau de Population : une par ligne (format standard).
     *
     * @return un tableau de Population
     */
    public Population[] construirePopulationsStandard() {
        List<Population> liste = new ArrayList<>();

        int iEspece   = getIndexColonne(CLE_ESPECE);
        int iEffectif = getIndexColonne("effectif");
        if (iEffectif < 0) iEffectif = getIndexColonne(CLE_N_EXACT);
        int iInfectes  = getIndexColonne(CLE_INFECTES);
        int iPartie    = getIndexColonne("partie");
        int iIntensite = getIndexColonne(CLE_INTENSITE);
        int iAbondance = getIndexColonne(CLE_ABONDANCE);

        for (int i = 0; i < getNbLignes(); i++) {
            String espece   = lireString(i, iEspece);
            int    effectif = lireInt   (i, iEffectif,  0);
            int    infectes = lireInt   (i, iInfectes,  0);
            String partie   = lireString(i, iPartie);
            float  intensite= lireFloat (i, iIntensite, 0f);
            float  abondance= lireFloat (i, iAbondance, 0f);

            try {
                
                liste.add(new Population(effectif, espece, infectes, partie, intensite, abondance));
            } catch (Exception e) {
                System.out.println("Population ligne " + i + " ignorée : " + e.getMessage());
            }
        }
        return liste.toArray(new Population[0]);
    }

    ////////////////////////////// Format multi-période (style Peru) ///////////

    /**
     * Construit un tableau de Population : une par espèce × période.
     * Ex: 4 espèces × 3 périodes (Total, 2012, 2013) = 12 populations.
     *
     * @return un tableau de Population
     */
    public Population[] construirePopulationsMultiPeriode() {
        List<Population> liste = new ArrayList<>();

        int iEspece = getIndexColonne(CLE_ESPECE);

        for (int i = 0; i < getNbLignes(); i++) {
            String espece = lireString(i, iEspece);

            for (String periode : periodes) {

                // Effectif : colonne "N_Periode"
                int iN = getIndexColonnePeriode(CLE_N, periode);
                if (iN < 0) iN = getIndexColonnePeriode(CLE_N_EXACT, periode);
                int effectif = lireInt(i, iN, 0);
                if (effectif == 0) continue; // On ignore les périodes sans données

                // Prévalence → nombre infectés = prévalence% × effectif
                int   iPrevalence = getIndexColonnePeriode(CLE_PREVALENCE, periode);
                float prevalence  = lireFloat(i, iPrevalence, 0f);
                int   infectes    = Math.round(prevalence / 100f * effectif);

                // Intensité et abondance
                int   iIntensite = getIndexColonnePeriode(CLE_INTENSITE, periode);
                int   iAbondance = getIndexColonnePeriode(CLE_ABONDANCE, periode);
                float intensite  = lireFloat(i, iIntensite, 0f);
                float abondance  = lireFloat(i, iAbondance, 0f);

                try {
                    
                    liste.add(new Population(effectif, espece, infectes, periode, intensite, abondance));
                } catch (Exception e) {
                    System.out.println("Population " + espece + " / " + periode
                            + " ignorée : " + e.getMessage());
                }
            }
        }
        return liste.toArray(new Population[0]);
    }

    ////////////////////////////// Mise à jour /////////////////////////////////

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

    /**
     * Répercute les données d'une Population dans la ligne du tableau.
     * Uniquement pour le format standard (le multi-période est en lecture seule).
     *
     * @param ligne l'index de la ligne à modifier
     * @param pop   la nouvelle population
     */
    private void repercuterDansTableau(int ligne, Population pop) {
        int iEspece   = getIndexColonne(CLE_ESPECE);
        int iEffectif = getIndexColonne("effectif");
        if (iEffectif < 0) iEffectif = getIndexColonne(CLE_N_EXACT);
        int iInfectes  = getIndexColonne(CLE_INFECTES);
        int iIntensite = getIndexColonne(CLE_INTENSITE);
        int iAbondance = getIndexColonne(CLE_ABONDANCE);
        try {
            if (iEspece   >= 0) setCase(ligne, iEspece,    pop.getEspece());
            if (iEffectif >= 0) setCase(ligne, iEffectif,  pop.getEffectif());
            if (iInfectes >= 0) setCase(ligne, iInfectes,  pop.getNbInfectes());
            if (iIntensite>= 0) setCase(ligne, iIntensite, pop.getIntensite());
            if (iAbondance>= 0) setCase(ligne, iAbondance, pop.getAbondance());
        } catch (OutOfBoundException e) {
            System.out.println("Erreur répercussion tableau : " + e.getMessage());
        }
    }

    ////////////////////////////// Affichage ///////////////////////////////////

    /**
     * Affiche un résumé tabulaire des populations dans la console.
     * Format adapté selon standard ou multi-période.
     */
    public void afficherResume() {
        System.out.println("=== " + getTitle() + " ===");
        if (formatMultiPeriode) {
            System.out.printf("%-35s %-12s %-10s %-10s %-10s%n",
                    "Espèce", "Période", "Effectif", "Infectés", "Abondance");
            System.out.println("-".repeat(80));
            for (Population pop : populations) {
                
                System.out.printf("%-35s %-12s %-10d %-10d %-10.2f%n",
                        pop.getEspece(),
                        pop.getPartieCorps(),
                        pop.getEffectif(),
                        pop.getNbInfectes(),
                        pop.getAbondance());
            }
        } else {
            System.out.printf("%-35s %-10s %-10s %-10s%n",
                    "Espèce", "Effectif", "Infectés", "Abondance");
            System.out.println("-".repeat(70));
            for (Population pop : populations) {
                System.out.printf("%-35s %-10d %-10d %-10.2f%n",
                        pop.getEspece(),
                        pop.getEffectif(),
                        pop.getNbInfectes(),
                        pop.getAbondance());
            }
        }
    }

    /**
     * Retourne le titre du dataframe.
     *
     * @return un String décrivant le type et le nombre de populations
     */
    @Override
    public String getTitle() {
        return "Etude de population"
                + (formatMultiPeriode ? " multi-période" : "")
                + " (" + (populations != null ? populations.length : 0) + " populations)";
    }

    ////////////////////////////// Main — tests ////////////////////////////////

    public static void main(String[] args) {
        int ok = 0, total = 0;
                                                                    
        // ── Format standard ───────────────────────────────────────────────────
        System.out.println("── Lecture mackerel.97442.csv (format standard) ─────");

        DfPopulation dfMerlu = null;
        try {               
            fish.acquisition.lecture.LectureCSV lecteur =
                    new fish.acquisition.lecture.LectureCSV(",");
            dfMerlu = lecteur.lireCSV("data/processed_data_anisakis.csv", DfPopulation.class);
        } catch (Exception e) { System.out.println("Chargement échoué : " + e); }

        // Test 1 : lecture sans exception
        total++;
        if (dfMerlu != null) {
            System.out.println("PASS Test 1 : chargé, " + dfMerlu.getNbLignes() + " lignes");
            ok++;
            dfMerlu.afficherPremieresFignes(5);
        } else {
            System.out.println("FAIL Test 1 : dfMerlu null");
        }

        // Test 2 : populations construites
        total++;
        if (dfMerlu != null && dfMerlu.getPopulations() != null) {
            System.out.println("PASS Test 2 : " + dfMerlu.getPopulations().length + " population(s)");
            ok++;
        } else {
            System.out.println("FAIL Test 2 : populations null");
        }

        // Test 3 : format standard détecté (pas multi-période)
        total++;
        if (dfMerlu != null && !dfMerlu.isFormatMultiPeriode()) {
            System.out.println("PASS Test 3 : format standard détecté");
            ok++;
        } else {
            System.out.println("FAIL Test 3 : multi-période détecté à tort");
        }

        // ── Format multi-période ──────────────────────────────────────────────
        System.out.println("\n── Lecture ParasitesPeru2021.csv (multi-période) ────");

        DfPopulation dfPeru = null;
        try {
            fish.acquisition.lecture.LectureParasitesPeru lecteurPeru =
                    new fish.acquisition.lecture.LectureParasitesPeru(",");
            dfPeru = lecteurPeru.lireCSV("data/ParasitesPeru2021.csv", DfPopulation.class);
        } catch (Exception e) { System.out.println("Chargement échoué : " + e); }

        // Test 4 : lecture sans exception
        total++;
        if (dfPeru != null) {
            System.out.println("PASS Test 4 : Peru chargé, " + dfPeru.getNbLignes() + " ligne(s)");
            ok++;
            dfPeru.afficherResume();
        } else {
            System.out.println("FAIL Test 4 : dfPeru null");
        }

        // Test 5 : format multi-période détecté
        total++;
        if (dfPeru != null && dfPeru.isFormatMultiPeriode()) {
            System.out.println("PASS Test 5 : format multi-période détecté");
            ok++;
        } else {
            System.out.println("FAIL Test 5 : multi-période non détecté");
        }

        // Test 6 : populations non vides
        total++;
        if (dfPeru != null && dfPeru.getPopulations() != null
                && dfPeru.getPopulations().length > 0) {
            System.out.println("PASS Test 6 : " + dfPeru.getPopulations().length + " population(s)");
            ok++;
        } else {
            System.out.println("FAIL Test 6 : populations vides ou null");
        }

        // Test 7 : périodes détectées
        total++;
        if (dfPeru != null && dfPeru.getPeriodes() != null
                && dfPeru.getPeriodes().length > 0) {
            System.out.println("PASS Test 7 : périodes = "
                    + java.util.Arrays.toString(dfPeru.getPeriodes()));
            ok++;
        } else {
            System.out.println("FAIL Test 7 : périodes null ou vides");
        }

        // Test 8 : espèce correctement lue (pas "Inconnue")
        total++;
        if (dfPeru != null) {
            try {
                Population p0    = dfPeru.getPopulation(0);
                String     esp   = p0.getEspece();
                String     prtie = p0.getPartieCorps(); // doit être "Total", "2012", etc.
                if (!"Inconnue".equals(esp) && !esp.isBlank()
                        && !"Inconnue".equals(prtie)
                        && !prtie.startsWith("[L")) {       // plus de [Ljava.lang...
                    System.out.println("PASS Test 8 : espèce='" + esp
                            + "', période='" + prtie + "'");
                    ok++;
                } else {
                    System.out.println("FAIL Test 8 : espèce='" + esp
                            + "', période='" + prtie + "'");
                }
            } catch (Exception e) { System.out.println("FAIL Test 8 : " + e); }
        } else {
            System.out.println("SKIP Test 8 : dfPeru non disponible"); total--;
        }

        // Test 9 : getPopulation index invalide → OutOfBoundException
        total++;
        try {
            if (dfMerlu != null) dfMerlu.getPopulation(9999);
            System.out.println("FAIL Test 9 : exception attendue non levée");
        } catch (fish.exceptions.OutOfBoundException e) {
            System.out.println("PASS Test 9 : index invalide → OutOfBoundException");
            ok++;
        } catch (Exception e) { System.out.println("FAIL Test 9 : " + e); }

        // Test 10 : getPopulationsParEspece espèce connue
        total++;
        if (dfPeru != null) {
            try {
                Population       p0    = dfPeru.getPopulation(0);
                List<Population> liste = dfPeru.getPopulationsParEspece(p0.getEspece());
                if (!liste.isEmpty()) {
                    System.out.println("PASS Test 10 : " + liste.size() + " population(s) pour '"
                            + p0.getEspece() + "'");
                    ok++;
                } else {
                    System.out.println("FAIL Test 10 : liste vide");
                }
            } catch (Exception e) { System.out.println("FAIL Test 10 : " + e); }
        } else {
            System.out.println("SKIP Test 10 : dfPeru non disponible"); total--;
        }

        // Test 11 : getPopulationsParEspece espèce inconnue → liste vide
        total++;
        if (dfPeru != null) {
            List<Population> liste = dfPeru.getPopulationsParEspece("EspeceInconnueXYZ");
            if (liste.isEmpty()) {
                System.out.println("PASS Test 11 : espèce inconnue → liste vide");
                ok++;
            } else {
                System.out.println("FAIL Test 11 : liste non vide");
            }
        } else {
            System.out.println("SKIP Test 11 : dfPeru non disponible"); total--;
        }

        // Test 12 : setPopulation met à jour correctement
        total++;
        try {
            Object[][] data = {
                {"Merlan", 50, 30, "foie",    3.2, 1.5},
                {"Hareng", 40, 20, "estomac", 2.0, 0.8}
            };
            DfPopulation df = new DfPopulation(2,
                    new String[]{"espece","effectif","infectes","partie","intensite","abondance"},
                    data);
            Population nouvelle = new Population(100, "Thon", 60, "muscle", 4.0, 2.4);
            df.setPopulation(0, nouvelle);
            Population apres = df.getPopulation(0);
            if ("Thon".equals(apres.getEspece()) && apres.getEffectif() == 100) {
                System.out.println("PASS Test 12 : setPopulation → Thon, 100 individus");
                ok++;
            } else {
                System.out.println("FAIL Test 12 : espece=" + apres.getEspece()
                        + ", eff=" + apres.getEffectif());
            }
        } catch (Exception e) { System.out.println("FAIL Test 12 : " + e); }

        // Test 13 : getIndexColonne mot-clé présent
        total++;
        try {
            Object[][] data = {{"Merlan", 50}};
            DfPopulation df = new DfPopulation(1, new String[]{"espece","effectif"}, data);
            int idx = df.getIndexColonne("effectif");
            if (idx == 1) {
                System.out.println("PASS Test 13 : getIndexColonne('effectif') = 1");
                ok++;
            } else {
                System.out.println("FAIL Test 13 : attendu 1, obtenu " + idx);
            }
        } catch (Exception e) { System.out.println("FAIL Test 13 : " + e); }

        // Test 14 : getIndexColonne mot-clé absent → -1
        total++;
        try {
            Object[][] data = {{"Merlan"}};
            DfPopulation df = new DfPopulation(1, new String[]{"espece"}, data);
            int idx = df.getIndexColonne("absente");
            if (idx == -1) {
                System.out.println("PASS Test 14 : getIndexColonne absent → -1");
                ok++;
            } else {
                System.out.println("FAIL Test 14 : attendu -1, obtenu " + idx);
            }
        } catch (Exception e) { System.out.println("FAIL Test 14 : " + e); }

        // Test 15 : getIndexColonne avec accent → normaliser()
        total++;
        try {
            Object[][] data = {{"Merlan", 50}};
            DfPopulation df = new DfPopulation(1, new String[]{"Espèce","effectif"}, data);
            int idx = df.getIndexColonne("espece"); // sans accent
            if (idx == 0) {
                System.out.println("PASS Test 15 : getIndexColonne avec accent normalisé");
                ok++;
            } else {
                System.out.println("FAIL Test 15 : attendu 0, obtenu " + idx);
            }
        } catch (Exception e) { System.out.println("FAIL Test 15 : " + e); }

        System.out.println("\n═══════════════════════════════════════════════════");
        System.out.println("=== DfPopulation : " + ok + "/" + total + " tests réussis ===");
        System.out.println("═══════════════════════════════════════════════════");
    }
}
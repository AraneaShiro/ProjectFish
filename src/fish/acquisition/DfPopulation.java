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
 * FORMAT MULTI-PERIODE — deux variantes :
 *
 * a) Format tidy (produit par pivoter()) — une ligne = une espèce × une période :
 * Espece | Periode | N | Longueur | Poids | Prévalence (%) | Abondance … 
 * Détecté par la présence des colonnes "Espece" ET "Periode".
 *
 * b) Format large (historique) — une ligne = une espèce,
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
    /**
     * Retourne la population à l'index donné.
     *
     * @param index l'index de la population dans le tableau
     * @return la {@link Population} correspondante
     * @throws OutOfBoundException si l'index est hors bornes
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

        // ── Format tidy (issu de pivoter()) : colonnes "Espece" + "Periode" ──
        boolean aEspece  = false;
        boolean aPeriode = false;
        for (String nom : noms) {
            String n = normaliser(nom);
            if (n.contains("espece"))  aEspece  = true;
            if (n.contains("periode")) aPeriode = true;
        }
        if (aEspece && aPeriode) return true;

        // ── Format large (ancien) : colonnes "Parametre_Periode" ─────────────
        Map<String, Integer> compteurPrefixes = new LinkedHashMap<>();
        for (String nom : noms) {
            int idx = nom.lastIndexOf('_');
            if (idx > 0) {
                String suffixe = nom.substring(idx + 1);
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
     * Retourne {@code true} si le format tidy (colonne "Periode" explicite)
     * est utilisé, par opposition au format large (colonnes "Param_Periode").
     */
    public boolean isFormatTidy() {
        for (String nom : getNomColonnes()) {
            if (normaliser(nom).contains("periode")) return true;
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
        // ── Format tidy : lire les valeurs uniques de la colonne "Periode" ───
        int iPeriode = getIndexColonne("periode");
        if (iPeriode >= 0) {
            List<String> periodes = new ArrayList<>();
            for (int i = 0; i < getNbLignes(); i++) {
                String p = lireString(i, iPeriode);
                if (!periodes.contains(p)) periodes.add(p);
            }
            return periodes.toArray(new String[0]);
        }

        // ── Format large : extraire les suffixes des colonnes "Param_Periode" ─
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

        // ── Format tidy : une ligne = une (espèce × période) ─────────────────
        if (isFormatTidy()) {
            int iEspece    = getIndexColonne(CLE_ESPECE);
            int iPeriode   = getIndexColonne("periode");
            int iN         = getIndexColonne(CLE_N_EXACT);
            int iPrevalence= getIndexColonne(CLE_PREVALENCE);
            int iIntensite = getIndexColonne(CLE_INTENSITE);
            int iAbondance = getIndexColonne(CLE_ABONDANCE);

            for (int i = 0; i < getNbLignes(); i++) {
                String espece    = lireString(i, iEspece);
                String periode   = lireString(i, iPeriode);
                int    effectif  = lireInt(i, iN, 0);
                if (effectif == 0) continue;

                float prevalence = lireFloat(i, iPrevalence, 0f);
                int   infectes   = Math.round(prevalence / 100f * effectif);
                float intensite  = lireFloat(i, iIntensite, 0f);
                float abondance  = lireFloat(i, iAbondance, 0f);

                try {
                    liste.add(new Population(effectif, espece, infectes, periode, intensite, abondance));
                } catch (Exception e) {
                    System.out.println("Population " + espece + "/" + periode
                            + " ignorée : " + e.getMessage());
                }
            }
            return liste.toArray(new Population[0]);
        }

        // ── Format large : une ligne = une espèce, colonnes = Param_Periode ──
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

    ////////////////////////////// Calculs statistiques ////////////////////////

    /**
     * Calcule la moyenne pondérée d'une colonne numérique par les effectifs.
     *
     * La formule est : Σ(effectif_i × valeur_i) / Σ(effectif_i)
     *
     * Cette pondération est nécessaire pour que les populations plus grandes
     * contribuent proportionnellement plus au résultat que les petites.
     *
     * @param nomColonne le nom (ou mot-clé) de la colonne à moyenner
     * @return la moyenne pondérée, ou NaN si aucun effectif valide n'est trouvé
     */
    public double calculerMoyennePonderee(String nomColonne) {
        int iVal = getIndexColonne(nomColonne);
        if (iVal < 0) {
            System.out.println("Colonne introuvable : " + nomColonne);
            return Double.NaN;
        }

        // Recherche de la colonne effectif
        int iEff = getIndexColonne("effectif");
        if (iEff < 0) iEff = getIndexColonne(CLE_N_EXACT);

        double sommeEffectifs = 0;
        double sommePonderee  = 0;

        for (int i = 0; i < getNbLignes(); i++) {
            double valeur   = lireFloat(i, iVal, 0f);
            double effectif = (iEff >= 0) ? lireInt(i, iEff, 0) : 1; // poids = 1 si colonne absente

            if (effectif <= 0) continue; // ignorer les lignes sans individus

            sommePonderee  += effectif * valeur;
            sommeEffectifs += effectif;
        }

        if (sommeEffectifs == 0) return Double.NaN;
        return sommePonderee / sommeEffectifs;
    }

    /**
     * Calcule la moyenne pondérée directement depuis le tableau des populations.
     *
     * Pratique pour les colonnes déjà extraites (intensité, abondance, prévalence).
     * Utilise pop.getEffectif() comme poids.
     *
     * @param extracteur fonction qui extrait la valeur numérique d'une Population
     *                   (ex: Population::getAbondance)
     * @return la moyenne pondérée, ou NaN si aucune population valide
     */
    public double calculerMoyennePonderee(java.util.function.ToDoubleFunction<Population> extracteur) {
        double sommeEffectifs = 0;
        double sommePonderee  = 0;

        for (Population pop : populations) {
            if (pop == null || pop.getEffectif() <= 0) continue;
            double effectif = pop.getEffectif();
            sommePonderee  += effectif * extracteur.applyAsDouble(pop);
            sommeEffectifs += effectif;
        }

        if (sommeEffectifs == 0) return Double.NaN;
        return sommePonderee / sommeEffectifs;
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

    ////////////////////////////// Pivot ////////////////////////////////////////

    /**
     * Pivote un dataframe au format « brut Peru »
     *   (Espèce | Paramètre | Total | 2012 | 2013 | …)
     * vers le format tidy multi-période :
     *   (Espece | Periode | N | Longueur … | Poids … | Prévalence (%) | …)
     *
     * <p>Avant pivot : une ligne = une espèce × un paramètre ;
     * les colonnes de valeurs sont les périodes.</p>
     *
     * <p>Après pivot : une ligne = une espèce × une période.
     * La colonne {@code Periode} porte l'année (ou "Total", "Moyenne").
     * Chaque paramètre du CSV source devient une colonne indépendante.</p>
     *
     * <p>Ce format tidy est automatiquement reconnu par
     * {@link #detecterFormatMultiPeriode()} (présence de la colonne "Periode")
     * et {@link #construirePopulationsMultiPeriode()}.</p>
     *
     * @return un nouveau {@link DfPopulation} en format tidy multi-période,
     *         ou {@code null} si le pivot est impossible (colonnes absentes, etc.)
     */
    public DfPopulation pivoter() {
        String[] noms = getNomColonnes();

        // ── 1. Colonnes structurelles : Espèce et Paramètre ──────────────────
        int iEspece    = getIndexColonne("espece");
        int iParametre = getIndexColonne("parametre");
        if (iEspece < 0 || iParametre < 0) {
            System.out.println("pivoter() : colonnes 'Espèce' et/ou 'Paramètre' introuvables.");
            return null;
        }

        // ── 2. Colonnes de périodes (4 chiffres, "Total" ou "Moyenne") ───────
        List<String>  periodesCols = new ArrayList<>();
        List<Integer> periodesIdx  = new ArrayList<>();
        for (int j = 0; j < noms.length; j++) {
            if (j == iEspece || j == iParametre) continue;
            String nom = noms[j].trim();
            if (nom.matches("\\d{4}")
                    || nom.equalsIgnoreCase("Total")
                    || nom.equalsIgnoreCase("Moyenne")) {
                periodesCols.add(nom);
                periodesIdx.add(j);
            }
        }
        if (periodesCols.isEmpty()) {
            System.out.println("pivoter() : aucune colonne de période détectée.");
            return null;
        }

        // ── 3. Espèces et paramètres uniques (ordre de première apparition) ──
        List<String> especesOrdre = new ArrayList<>();
        List<String> parametres   = new ArrayList<>();
        for (int i = 0; i < getNbLignes(); i++) {
            String espece    = lireString(i, iEspece);
            String parametre = lireString(i, iParametre);
            if (!especesOrdre.contains(espece))    especesOrdre.add(espece);
            if (!parametres.contains(parametre))   parametres.add(parametre);
        }

        // ── 4. Noms des colonnes : Espece | Periode | param1 | param2 | … ───
        List<String> nouvellesColonnes = new ArrayList<>();
        nouvellesColonnes.add("Espece");
        nouvellesColonnes.add("Periode");
        nouvellesColonnes.addAll(parametres);

        // ── 5. Tableau de données — une ligne par (espece × periode) ─────────
        // Construction d'un index (espece, parametre) → ligne source
        Map<String, Map<String, Integer>> index = new LinkedHashMap<>();
        for (int i = 0; i < getNbLignes(); i++) {
            String espece    = lireString(i, iEspece);
            String parametre = lireString(i, iParametre);
            index.computeIfAbsent(espece, k -> new LinkedHashMap<>()).put(parametre, i);
        }

        int nbLignes = especesOrdre.size() * periodesCols.size();
        int nbCols   = nouvellesColonnes.size();
        Object[][] donnees = new Object[nbLignes][nbCols];

        int ligne = 0;
        for (String espece : especesOrdre) {
            for (int p = 0; p < periodesCols.size(); p++) {
                String periode = periodesCols.get(p);
                donnees[ligne][0] = espece;
                donnees[ligne][1] = periode;          // ← l'année dans sa colonne

                // Remplir les colonnes de paramètres
                Map<String, Integer> paramsEspece = index.getOrDefault(espece, new LinkedHashMap<>());
                for (int c = 0; c < parametres.size(); c++) {
                    String param      = parametres.get(c);
                    Integer ligneSource = paramsEspece.get(param);
                    if (ligneSource == null) { donnees[ligne][2 + c] = null; continue; }
                    try {
                        donnees[ligne][2 + c] = getCase(ligneSource, periodesIdx.get(p));
                    } catch (OutOfBoundException e) {
                        donnees[ligne][2 + c] = null;
                    }
                }
                ligne++;
            }
        }

        // ── 6. Construction du nouveau DfPopulation ───────────────────────────
        try {
            DfPopulation pivote = new DfPopulation(
                    nbLignes,
                    nouvellesColonnes.toArray(new String[0]),
                    donnees);
            System.out.println("pivoter() : " + especesOrdre.size() + " espèce(s) × "
                    + periodesCols.size() + " période(s) = " + nbLignes + " lignes, "
                    + parametres.size() + " paramètre(s) en colonnes.");
            return pivote;
        } catch (OutOfBoundException | NullParameterException e) {
            System.out.println("pivoter() : erreur construction → " + e.getMessage());
            return null;
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

    /**
     * Tests unitaires du DfPopulation.
     * Vérifie la lecture et la construction de populations en format standard
     * et multi-période, ainsi que les calculs de moyennes pondérées.
     *
     * @param args arguments de la ligne de commande (ignorés)
     */
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

        // ── Pivot brut Peru → formatMultiPériode ─────────────────────────────
        System.out.println("\n── Pivot du format brut Peru ────────────────────────");

        // Test 8b : pivoter() renvoie un DfPopulation non-null
        total++;
        DfPopulation dfPeruPivote = null;
        if (dfPeru != null && !dfPeru.isFormatMultiPeriode()) {
            // dfPeru est encore dans son format brut avant pivot
            dfPeruPivote = dfPeru.pivoter();
        } else if (dfPeru != null) {
            // Si le lecteur a déjà pivoté, on crée un df brut manuellement pour tester
            Object[][] brutData = {
                {"Trachurus symmetricus murphyi", "N",                    105, 40, 30, 35},
                {"Trachurus symmetricus murphyi", "Prevalence (%)",  64.76, 62.5, 70.0, 62.86},
                {"Merluccius gayi peruanus",      "N",                     85, 28, 32, 25},
                {"Merluccius gayi peruanus",      "Prevalence (%)",  77.65, 78.57, 75.0, 80.0}
            };
            try {
                DfPopulation dfBrut = new DfPopulation(4,
                        new String[]{"Espece", "Parametre", "Total", "2012", "2013", "2014"},
                        brutData);
                dfPeruPivote = dfBrut.pivoter();
            } catch (Exception e) {
                System.out.println("Construction df brut échouée : " + e);
            }
        }
        if (dfPeruPivote != null) {
            System.out.println("PASS Test 8b : pivot réussi → "
                    + dfPeruPivote.getNbLignes() + " ligne(s), "
                    + dfPeruPivote.getNomColonnes().length + " colonne(s)");
            ok++;
        } else {
            System.out.println("FAIL Test 8b : pivoter() a retourné null");
        }

        // Test 8c : le résultat pivote est reconnu comme formatMultiPeriode
        total++;
        if (dfPeruPivote != null && dfPeruPivote.isFormatMultiPeriode()) {
            System.out.println("PASS Test 8c : format multi-période détecté après pivot");
            ok++;
        } else {
            System.out.println("FAIL Test 8c : format multi-période non détecté après pivot");
        }

        // Test 8d : les populations pivotées sont non vides
        total++;
        if (dfPeruPivote != null && dfPeruPivote.getPopulations() != null
                && dfPeruPivote.getPopulations().length > 0) {
            System.out.println("PASS Test 8d : " + dfPeruPivote.getPopulations().length
                    + " population(s) construite(s) depuis le pivot");
            ok++;
            dfPeruPivote.afficherResume();
        } else {
            System.out.println("FAIL Test 8d : populations vides après pivot");
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
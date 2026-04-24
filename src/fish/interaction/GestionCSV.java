package fish.interaction;

import fish.acquisition.*;
import fish.acquisition.lecture.*;
import java.util.*;

/**
 * Gestionnaire de CSV en mémoire.
 *
 * Permet de :
 *   - Charger plusieurs fichiers CSV avec leur type (Individu / Population)
 *   - Les nommer et les lister
 *   - En sélectionner un comme "actif"
 *   - Supprimer / renommer des entrées
 *
 * @author Jules Grenesche
 * @version 1.0
 */
public class GestionCSV {

    // ── ANSI ─────────────────────────────────────────────────────────────────
    private static final String R     = "\u001B[0m";
    private static final String G     = "\u001B[1m";
    private static final String CYA_G = "\u001B[1m\u001B[36m";
    private static final String JAU   = "\u001B[33m";
    private static final String VER   = "\u001B[32m";
    private static final String ROU   = "\u001B[31m";
    private static final String MAG   = "\u001B[35m";
    private static final String VER_G = "\u001B[1m\u001B[32m";
    private static final String JAU_G = "\u001B[1m\u001B[33m";
    private static final String ROU_G = "\u001B[1m\u001B[31m";

    /** Types de CSV supportés */
    public enum TypeCSV { INDIVIDU, POPULATION, INCONNU }

    /** Charsets proposés dans le menu — index -> {label, valeur Java} */
    public static final String[][] CHARSETS = {
        {"UTF-8               (Unicode — défaut universel)",   "UTF-8"},
        {"windows-1252        (Excel FR / Europe occidentale)","windows-1252"},
        {"ISO-8859-1          (Latin-1, ancien web FR)",       "ISO-8859-1"},
        {"ISO-8859-15         (Latin-9, inclut €)",            "ISO-8859-15"},
        {"UTF-16              (Notepad Windows)",              "UTF-16"},
        {"US-ASCII            (7 bits, sans accents)",         "US-ASCII"},
        {"Personnalisé",                                        null},
    };

    /** Entrée du registre */
    public static class EntreeCSV {
        public  int              id;
        public  String           nom;
        public  String           chemin;
        public  TypeCSV          type;
        public  DataframeComplet df;
        public  String           delimiteur;
        public  String           charset;

        EntreeCSV(int id, String nom, String chemin, String delim,
                  String charset, TypeCSV type, DataframeComplet df) {
            this.id = id; this.nom = nom; this.chemin = chemin;
            this.delimiteur = delim; this.charset = charset;
            this.type = type; this.df = df;
        }
    }

    // ── État ─────────────────────────────────────────────────────────────────
     /**
     * Liste des entrées CSV
     */
    private final List<EntreeCSV> registre = new ArrayList<>();
     /**
     * Compteur pour les ID
     */
    private int                   nextId   = 1;
     /**
     * Variable qui suit celui actif
     */
    private int                   actifId  = -1;

    // ── Chargement ────────────────────────────────────────────────────────────

    
    /**
     * Charge un CSV et l'ajoute au registre.
     *
     * @param chemin     chemin du fichier
     * @param delimiteur séparateur de colonnes
     * @param charset    encodage du fichier (ex: "UTF-8", "windows-1252")
     * @param type       type de dataframe (INDIVIDU ou POPULATION)
     * @param nom        nom affiché dans le menu (null → nom du fichier)
     * @return l'entrée créée, ou null si échec
     */
    public EntreeCSV charger(String chemin, String delimiteur, String charset,
                              TypeCSV type, String nom) {
        // Détection BOM UTF-8 si nécessaire
        String charsetEffectif = detecterBOM(chemin, charset);

        DataframeComplet df = null;
        try {
            LectureCSV lec = new LectureCSV(delimiteur, charsetEffectif);
            if (type == TypeCSV.POPULATION) {
                df = lec.lireCSV(chemin, DfPopulation.class);
            } else {
                df = lec.lireCSV(chemin, DfIndividu.class);
            }
        } catch (Exception e) {
            System.out.println(ROU_G + "✘ Erreur chargement : " + e.getMessage() + R);
            return null;
        }

        if (df == null) { System.out.println(ROU_G + "✘ Dataframe null." + R); return null; }

        String nomFinal = (nom != null && !nom.isBlank()) ? nom
                : chemin.substring(Math.max(0, chemin.lastIndexOf('/'  ) + 1));

        EntreeCSV e = new EntreeCSV(nextId++, nomFinal, chemin, delimiteur,
                charsetEffectif, type, df);
        registre.add(e);
        actifId = e.id;

        System.out.printf(VER_G + "✔ Chargé [#%d] '%s' — %s — charset=%s — %dx%d" + R + "%n",
                e.id, nomFinal, type, charsetEffectif, df.getNbLignes(), df.getNbCol());
        return e;
    }

    /** Compatibilité rétrograde : UTF-8 par défaut. */
    public EntreeCSV charger(String chemin, String delimiteur, TypeCSV type, String nom) {
        return charger(chemin, delimiteur, "UTF-8", type, nom);
    }

    /**
     * Tente de détecter le BOM (Byte Order Mark) d'un fichier pour identifier
     * automatiquement l'encodage réel. Utile pour les exports Excel.
     *
     * BOM UTF-8  : EF BB BF  → charset = "UTF-8"
     * BOM UTF-16 : FF FE / FE FF → charset = "UTF-16"
     *
     * @param chemin  chemin du fichier
     * @param hint    charset indiqué par l'utilisateur (utilisé si pas de BOM)
     * @return le charset effectif à utiliser
     */
    public String detecterBOM(String chemin, String hint) {
        try (java.io.InputStream is = new java.io.FileInputStream(chemin)) { //On lit les premiere ligne
            byte[] bom = new byte[3];
            int lu = is.read(bom, 0, 3);
            if (lu >= 3 && (bom[0] & 0xFF) == 0xEF
                        && (bom[1] & 0xFF) == 0xBB
                        && (bom[2] & 0xFF) == 0xBF) {
                System.out.println(JAU + "ℹ BOM UTF-8 détecté → charset forcé à UTF-8" + R);
                return "UTF-8";
            }
            if (lu >= 2 && ((bom[0] & 0xFF) == 0xFF && (bom[1] & 0xFF) == 0xFE)
                        || ((bom[0] & 0xFF) == 0xFE && (bom[1] & 0xFF) == 0xFF)) {
                System.out.println(JAU + "ℹ BOM UTF-16 détecté → charset forcé à UTF-16" + R);
                return "UTF-16";
            }
        } catch (Exception ignored) {}
        return hint;
    }

    /**
     * Essaie de détecter heuristiquement le charset d'un fichier en lisant
     * les 4096 premiers octets et en cherchant des séquences invalides en UTF-8.
     *
     * @param chemin chemin du fichier
     * @return suggestion de charset ("UTF-8" ou "windows-1252")
     */
    public String suggererCharset(String chemin) {
        try (java.io.InputStream is = new java.io.FileInputStream(chemin)) {
            byte[] buf = new byte[4096];
            int lu = is.read(buf, 0, buf.length);
            if (lu < 0) return "UTF-8";
            // Vérifier si les octets sont valides en UTF-8
            try {
                // Présence d'octets >127 suggère windows-1252 si pas de séquence UTF-8 valide
                for (int i = 0; i < lu - 1; i++) {
                    int b = buf[i] & 0xFF;
                    if (b >= 0xC0 && b <= 0xDF) return "UTF-8"; // séquence 2 octets UTF-8
                    if (b >= 0xE0 && b <= 0xEF) return "UTF-8"; // séquence 3 octets UTF-8
                }
                // Octets 0x80-0xFF sans séquence multi-octets → windows-1252
                for (int i = 0; i < lu; i++) {
                    if ((buf[i] & 0xFF) > 0x7F) return "windows-1252"; //regarde si on dépasse l'ASCII de base
                }
                return "UTF-8";
            } catch (Exception e) { return "windows-1252"; }
        } catch (Exception e) { return "UTF-8"; }
    }



    // ── Sélection ─────────────────────────────────────────────────────────────

    /** Sélectionne le CSV actif par son id. 
     * @param id l'id que l'on veut
     * @return true si on a reussie sinon false
    */
    public boolean selectionner(int id) {
        for (EntreeCSV e : registre) {
            if (e.id == id) { actifId = id; return true; }
        }
        System.out.println(ROU + "ID introuvable : " + id + R);
        return false;
    }

    /** Retourne le dataframe actif ou null. 
     * @return le dataframe actif
    */
    public DataframeComplet getActif() {
        return registre.stream().filter(e -> e.id == actifId)
                .findFirst().map(e -> e.df).orElse(null);
    }

    /** Retourne l'entrée active ou null.
     * @return l'entrée
     */
    public EntreeCSV getEntreeActive() {
        return registre.stream().filter(e -> e.id == actifId).findFirst().orElse(null);
    }

    /** Retourne toutes les entrées.
     * @return les entrées
     */
    public List<EntreeCSV> getTout() { return Collections.unmodifiableList(registre); }

    /** Retourne vrai si le registre est vide. 
     * @return true si vide false sinon
    */
    public boolean estVide() { return registre.isEmpty(); }

    // ── Suppression ───────────────────────────────────────────────────────────

    /**
     * Supprime l'entrée de registre correspondant à l'id donné.
     * Si l'entrée supprimée était active, sélectionne automatiquement
     * la première entrée restante (ou aucun actif si le registre est vide).
     *
     * @param id l'identifiant de l'entrée à supprimer
     * @return true si la suppression a réussi,  false si l'id est inconnu
     */
    public boolean supprimer(int id) {
        boolean ok = registre.removeIf(e -> e.id == id);
        if (ok && actifId == id) actifId = registre.isEmpty() ? -1 : registre.get(0).id;
        return ok;
    }

    // ── Affichage du registre ─────────────────────────────────────────────────

    /**
     * Affiche la liste de tous les CSV chargés sous forme de tableau ASCII coloré.
     * Indique pour chaque entrée : son id, nom, type, charset, dimensions et statut actif.
     */
    public void afficherListe() {
        if (registre.isEmpty()) {
            System.out.println(JAU + "  (aucun CSV chargé)" + R);
            return;
        }
        System.out.println(CYA_G + "╔═════════════════════════════════════════════════════════════════════════════╗" + R);
        System.out.println(CYA_G + "║" + R + G + "  # │ Nom                  │ Type       │ Charset      │ Dim        │ Actif" + R + CYA_G + "  ║" + R);
        System.out.println(CYA_G + "╠═════════════════════════════════════════════════════════════════════════════╣" + R);
        for (EntreeCSV e : registre) {
            boolean act = e.id == actifId;
            String tCol = e.type == TypeCSV.INDIVIDU ? VER : MAG;
            String actMark = act ? VER_G + " ◄ actif" + R : "";
            String cs = e.charset != null ? e.charset : "UTF-8";
            System.out.printf(CYA_G + "║" + R
                    + (act ? VER_G : JAU) + " %2d" + R + " │ %-20s │ " + tCol + "%-10s" + R
                    + " │ %-12s │ %4dx%-4d  │%s" + CYA_G + "║" + R + "%n",
                    e.id, trunc(e.nom, 20), e.type, cs,
                    e.df.getNbLignes(), e.df.getNbCol(), actMark);
        }
        System.out.println(CYA_G + "╚═════════════════════════════════════════════════════════════════════════════╝" + R);
    }

    // ── Menu interactif de sélection ──────────────────────────────────────────

    /**
     * Affiche la liste et invite l'utilisateur à choisir un CSV.
     *@return le dataframe sélectionné ou l'actuel si annulé.
     */
    public DataframeComplet menuSelectionner(Scanner sc) {
        afficherListe();
        if (registre.isEmpty()) return null;
        System.out.print(JAU + "Entrez l'ID à sélectionner (Entrée = actuel) : " + R);
        String s = sc.nextLine().trim();
        if (!s.isEmpty()) {
            try { selectionner(Integer.parseInt(s)); }
            catch (NumberFormatException e) { System.out.println(ROU + "ID invalide." + R); }
        }
        return getActif();
    }

    // ── Utilitaire ────────────────────────────────────────────────────────────

    /**
     * Tronque une chaîne à  max caractères en ajoutant "…" si nécessaire.
     *
     * @param s   la chaîne à tronquer
     * @param max la longueur maximale autorisée
     * @return la chaîne tronquée si nécessaire
     */
    private String trunc(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }
}
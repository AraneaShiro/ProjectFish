package fish.acquisition.lecture;

import fish.acquisition.*;
import fish.exceptions.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * LectureCSV est la classe qui permet de lire un csv et le convertir dans le
 * dataframe. C'est la version par défaut qui lit les fichiers avec les entêtes
 * dans la 1ère ligne.
 *
 * @author Arthur BERNARD et Jules Grenesche
 * @version 1.1
 *          Date : 18/03
 */
public class LectureCSV {

    ////////////////////////////// Attributs ////////////////////

    /** Séparateur reconnu par le lecteur */
    private String separateur;

    /** Les entêtes */
    protected String[] entetes;

    /** Le nombre de lignes du tableau */
    protected int nbLignes;

    /** Le nombre de colonnes du tableau */
    protected int nbCol;

    /** Charset utilisé pour la lecture */
    private String charset;

    ////////////////////////////// Getter / Setter

    /** Retourne les entêtes */
    public String[] getEntetes() {
        return this.entetes;
    }

    /** Retourne le séparateur */
    public String getSeparateur() {
        return this.separateur;
    }

    /** Retourne le nombre de lignes */
    public int getNbLignes() {
        return this.nbLignes;
    }

    /** Retourne le nombre de colonnes */
    public int getNbCol() {
        return this.nbCol;
    }

    /** Retourne le charset */
    public String getCharset() { return this.charset; }

    /**
     * Retourne les dimensions du tableau [nbLignes, nbColonnes]
     *
     * @return un int[] de taille 2 : [nbLignes, nbCol]
     */
    public int[] getSize() {
        return new int[] { this.nbLignes, this.nbCol };
    }

    private void setEntetes(String[] entete) {
        this.entetes = entete;
    }

    private void setNbLignes(int nb) {
        this.nbLignes = nb;
    }

    private void setNbCol(int nb) {
        this.nbCol = nb;
    }

    ////////////////////////////// Constructeurs ////////////////////

   /** Constructeur avec séparateur — UTF-8 par défaut */
public LectureCSV(String separateur) {
    this(separateur, "UTF-8");
}
    /**
 * Constructeur avec séparateur ET charset
 *
 * @param separateur le séparateur du fichier CSV
 * @param charset    l'encodage du fichier (ex: "UTF-8", "windows-1252")
 */
public LectureCSV(String separateur, String charset) {
    this.separateur = separateur;
    this.charset    = charset;
}

    /** Constructeur par défaut — virgule + UTF-8 */
public LectureCSV() {
    this(",", "UTF-8");
}

    ////////////////////////////// Méthodes ////////////////////

    /**
     * Convertit une String en son type Java approprié.
     *
     * Gère les cas spéciaux suivants :
     * - Vide / null → null
     * - "&lt;LOQ" / "&lt;LOD" → 0.0 (limite de quantification non atteinte)
     * - "&lt;X" → X/2 (estimation convention Bush et al.)
     * - "X ± SD" → X (SD ignoré)
     * - "min-max" → (min+max)/2 (centre de l'intervalle)
     * - Integer / Long / Double / Boolean → type Java correspondant
     * - Sinon → String
     *
     * @param valeur la valeur brute lue dans le CSV
     * @return l'objet typé correspondant, ou null si vide
     */
    protected Object convertirType(String valeur) {
        if (valeur == null || valeur.isBlank())
            return null;

        String v = valeur.trim();

        // ── <LOQ / <LOD → 0.0 ───────────────────────────────────────────────
        if (v.equalsIgnoreCase("<LOQ") || v.equalsIgnoreCase("<LOD"))
            return 0.0;

        // ── <X → X/2 (ex: "<12.5" → 6.25) ─────────────────────────────────
        if (v.startsWith("<") && v.length() > 1) {
            try {
                return Double.parseDouble(v.substring(1).trim()) / 2.0;
            } catch (NumberFormatException ignored) {
            }
        }

        // ── "X ± SD" (ex: "34.83 ± 2.32") — on garde uniquement X ─────────
        if (v.contains("±")) {
            String valeurPrincipale = v.split("±")[0].trim();
            try {
                return Double.parseDouble(valeurPrincipale);
            } catch (NumberFormatException ignored) {
            }
        }

        // ── "min-max" (ex: "55.47-74.05") — centre de l'intervalle ─────────
        // Attention : les négatifs comme "-18.23" ne doivent pas matcher.
        // On ne matche que si les DEUX parties commencent par un chiffre.
        if (v.matches("\\d[\\d.]*-\\d[\\d.]*")) {
            String[] parts = v.split("-");
            try {
                double min = Double.parseDouble(parts[0].trim());
                double max = Double.parseDouble(parts[1].trim());
                return (min + max) / 2.0;
            } catch (NumberFormatException ignored) {
            }
        }

        // ── Integer ──────────────────────────────────────────────────────────
        try {
            return Integer.parseInt(v);
        } catch (NumberFormatException ignored) {
        }

        // ── Long (grands entiers) ────────────────────────────────────────────
        try {
            return Long.parseLong(v);
        } catch (NumberFormatException ignored) {
        }

        // ── Double (virgule → point pour les CSV français) ───────────────────
        try {
            return Double.parseDouble(v.replace(",", "."));
        } catch (NumberFormatException ignored) {
        }

        // ── Boolean ──────────────────────────────────────────────────────────
        if (v.equalsIgnoreCase("true") || v.equalsIgnoreCase("false")) {
            return Boolean.parseBoolean(v);
        }

        // ── Sinon → String ───────────────────────────────────────────────────
        return v;
    }

    /**
     * Lit un fichier CSV et retourne une instance de la classe héritée de
     * Dataframe choisie.
     *
     * @param <T>           le type héritant de Dataframe
     * @param cheminFichier le chemin vers le fichier CSV
     * @param type          la classe cible (ex: DfIndividu.class)
     * @return une instance de T construite à partir du fichier, ou null si
     *         le fichier est introuvable
     * @throws FileEmpty si le fichier est vide ou sans en-têtes
     */
    public <T extends DataframeComplet> T lireCSV(String cheminFichier, Class<T> type) throws FileEmpty {

        List<String[]> lignesBrutes = new ArrayList<>();

        try (Scanner scanner = new Scanner(new File(cheminFichier), this.charset)) {

            boolean premiereLigne = true;

            while (scanner.hasNextLine()) {
                String ligne = scanner.nextLine();
                if (ligne.trim().isEmpty())
                    continue;

                String[] valeurs = ligne.split(Pattern.quote(this.separateur), -1);

                if (premiereLigne) {
                    this.entetes = valeurs;
                    premiereLigne = false;
                } else {
                    lignesBrutes.add(valeurs);
                }
            }

        } catch (FileNotFoundException e) {
            System.out.println("Fichier introuvable : " + cheminFichier);
            return null;
        }

        if (this.entetes == null) {
            throw new FileEmpty(cheminFichier);
        }

        this.nbLignes = lignesBrutes.size();
        this.nbCol = this.entetes.length;

        Object[][] tableau = new Object[this.nbLignes][this.nbCol];
        for (int i = 0; i < this.nbLignes; i++) {
            for (int j = 0; j < this.nbCol; j++) {
                String valeur = (j < lignesBrutes.get(i).length)
                        ? lignesBrutes.get(i)[j].trim()
                        : "";
                tableau[i][j] = convertirType(valeur);
            }
        }

        // Instanciation de la classe cible via réflexion
        try {
            return type.getConstructor(int.class, String[].class, Object[][].class)
                    .newInstance(this.nbLignes, this.entetes, tableau);
        } catch (Exception e) {
            System.out.println("Impossible d'instancier "
                    + type.getSimpleName() + " : " + e.getMessage());
            return null;
        }
    }

    /**
     * Affiche les entêtes avec leur numéro de colonne.
     *
     * @throws NoFileLoaded si aucun fichier n'a été chargé
     */
    public void afficherEntetes() throws NoFileLoaded {
        if (this.entetes == null) {
            throw new NoFileLoaded();
        }
        for (int i = 0; i < this.entetes.length; i++) {
            System.out.println("[" + i + "] " + this.entetes[i]);
        }
    }

    public static void main(String[] args) {
        int reussis = 0, total = 0;

        // ── Helper : lit silencieusement (supprime les prints internes) ───────
        // Les EmptyStringException et ArithmeticException viennent de
        // construireIndividus() sur des lignes sans espèce — c'est du bruit.
        java.io.PrintStream stdout = System.out;
        java.io.PrintStream devNull = new java.io.PrintStream(
                new java.io.OutputStream() { public void write(int b) {} });

        // ────────────────────────────────────────────────────────────────────
        // CSV 1 — Format standard (virgule, entêtes ligne 1)
        // Contenu : espece,longueur,poids,nbvers
        // Merlan,30.5,200,5
        // Hareng,25.0,150,0
        // Thon,80.0,5000,12
        // ,35.0,,3 ← ligne avec nulls
        // ────────────────────────────────────────────────────────────────────
        System.out.println("── CSV 1 : format standard ──────────────────────────");

        // Test 1 : lecture du fichier mackerel (séparateur ;) — vérification sans exception
        total++;
        try {
            LectureCSV lecteurMackerel = new LectureCSV(";");
            DfIndividu df = lecteurMackerel.lireCSV("data/mackerel.97442.csv", DfIndividu.class);
            if (df != null) {
                System.out.println("PASS Test 1 : lecture sans exception — "
                        + df.getNbLignes() + " lignes, " + df.getNbCol() + " colonnes");
                lecteurMackerel.afficherEntetes();
                df.afficherPremieresFignes(5);
                df.afficherStatistiques();
                reussis++;
            } else {
                System.out.println("FAIL Test 1 : df est null");
            }
        } catch (Exception e) {
            System.out.println("FAIL Test 1 : " + e);
        }


        // ── Tests 2–7 : test_simple.csv (séparateur virgule) ─────────────────
        // Instance fraîche à chaque lecture pour éviter la pollution d'état
        System.out.println("\n── CSV simple (virgule) ─────────────────────────────");
        DfIndividu dfSimple = null;
        try {
            System.setOut(devNull);
            dfSimple = new LectureCSV(",").lireCSV("data/Test/test_simple.csv", DfIndividu.class);
        } catch (FileEmpty e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally { System.setOut(stdout); }

        // Test 2 : dimensions correctes (4 lignes, 4 colonnes)
        total++;
        try {
            int[] size = dfSimple.getSize();
            if (size[0] == 4 && size[1] == 4) {
                System.out.println("PASS Test 2 : dimensions 4x4");
                reussis++;
            } else {
                System.out.println("FAIL Test 2 : dimensions attendues [4,4], obtenues ["
                        + size[0] + "," + size[1] + "]");
            }
        } catch (Exception e) { System.out.println("FAIL Test 2 : " + e); }

        // Test 3 : entêtes correctes
        total++;
        try {
            String[] noms = dfSimple.getNomColonnes();
            if ("espece".equals(noms[0]) && "longueur".equals(noms[1])
                    && "poids".equals(noms[2]) && "nbvers".equals(noms[3])) {
                System.out.println("PASS Test 3 : entêtes correctes");
                reussis++;
            } else {
                System.out.println("FAIL Test 3 : entêtes incorrectes : "
                        + java.util.Arrays.toString(noms));
            }
        } catch (Exception e) { System.out.println("FAIL Test 3 : " + e); }

        // Test 4 : types détectés — longueur=Double, nbvers=Integer
        total++;
        try {
            Object longueur = dfSimple.getCase(0, 1);
            Object nbvers   = dfSimple.getCase(0, 3);
            if (longueur instanceof Double && nbvers instanceof Integer) {
                System.out.println("PASS Test 4 : types détectés (Double, Integer)");
                reussis++;
            } else {
                System.out.println("FAIL Test 4 : types incorrects — longueur="
                        + longueur.getClass().getSimpleName()
                        + ", nbvers=" + nbvers.getClass().getSimpleName());
            }
        } catch (Exception e) { System.out.println("FAIL Test 4 : " + e); }

        // Test 5 : valeur numérique correcte — longueur[0] = 30.5
        total++;
        try {
            double longueur = (Double) dfSimple.getCase(0, 1);
            if (Math.abs(longueur - 30.5) < 1e-9) {
                System.out.println("PASS Test 5 : valeur 30.5 correcte");
                reussis++;
            } else {
                System.out.println("FAIL Test 5 : longueur attendue 30.5, obtenue " + longueur);
            }
        } catch (Exception e) { System.out.println("FAIL Test 5 : " + e); }

        // Test 6 : cellules vides → null (ligne 4 : espece vide, poids vide)
        total++;
        try {
            Object espece = dfSimple.getCase(3, 0);
            Object poids  = dfSimple.getCase(3, 2);
            if (espece == null && poids == null) {
                System.out.println("PASS Test 6 : cellules vides → null");
                reussis++;
            } else {
                System.out.println("FAIL Test 6 : espece=" + espece + ", poids=" + poids);
            }
        } catch (Exception e) { System.out.println("FAIL Test 6 : " + e); }

        // Test 7 : valeur String correcte — espece[1] = "Hareng"
        total++;
        try {
            Object espece = dfSimple.getCase(1, 0);
            if ("Hareng".equals(espece)) {
                System.out.println("PASS Test 7 : valeur String 'Hareng' correcte");
                reussis++;
            } else {
                System.out.println("FAIL Test 7 : espece attendue 'Hareng', obtenue " + espece);
            }
        } catch (Exception e) { System.out.println("FAIL Test 7 : " + e); }

        // ── Tests 8–12 : test_special.csv (valeurs spéciales ±, <LOQ, min-max) ─
        System.out.println("\n── CSV 2 : valeurs spéciales ────────────────────────");
        DfIndividu dfSpecial = null;
        try {
            System.setOut(devNull);
            dfSpecial = new LectureCSV(",").lireCSV("data/Test/test_special.csv", DfIndividu.class);
        } catch (FileEmpty e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally { System.setOut(stdout); }

        // Test 8 : "34.83 ± 2.32" → Double 34.83
        total++;
        try {
            Object val = dfSpecial.getCase(0, 1);
            if (val instanceof Double && Math.abs((Double) val - 34.83) < 1e-9) {
                System.out.println("PASS Test 8 : '34.83 ± 2.32' → 34.83");
                reussis++;
            } else {
                System.out.println("FAIL Test 8 : '34.83 ± 2.32' → " + val);
            }
        } catch (Exception e) { System.out.println("FAIL Test 8 : " + e); }

        // Test 9 : "<LOQ" → 0.0
        total++;
        try {
            Object val = dfSpecial.getCase(1, 1);
            if (Double.valueOf(0.0).equals(val)) {
                System.out.println("PASS Test 9 : '<LOQ' → 0.0");
                reussis++;
            } else {
                System.out.println("FAIL Test 9 : '<LOQ' → " + val
                        + " (" + (val != null ? val.getClass().getSimpleName() : "null") + ")");
            }
        } catch (Exception e) { System.out.println("FAIL Test 9 : " + e); }

        // Test 10 : "<12.5" → 6.25
        total++;
        try {
            Object val = dfSpecial.getCase(2, 2);
            if (val instanceof Double && Math.abs((Double) val - 6.25) < 1e-9) {
                System.out.println("PASS Test 10 : '<12.5' → 6.25");
                reussis++;
            } else {
                System.out.println("FAIL Test 10 : '<12.5' → " + val);
            }
        } catch (Exception e) { System.out.println("FAIL Test 10 : " + e); }

        // Test 11 : "55.47-74.05" → 64.76 (centre intervalle)
        total++;
        try {
            Object val = dfSpecial.getCase(0, 4);
            if (val instanceof Double && Math.abs((Double) val - 64.76) < 1e-9) {
                System.out.println("PASS Test 11 : '55.47-74.05' → 64.76");
                reussis++;
            } else {
                System.out.println("FAIL Test 11 : '55.47-74.05' → " + val);
            }
        } catch (Exception e) { System.out.println("FAIL Test 11 : " + e); }

        // Test 12 : cellule vide → null (longueur Thon)
        total++;
        try {
            Object val = dfSpecial.getCase(2, 1);
            if (val == null) {
                System.out.println("PASS Test 12 : cellule vide → null");
                reussis++;
            } else {
                System.out.println("FAIL Test 12 : cellule vide → " + val);
            }
        } catch (Exception e) { System.out.println("FAIL Test 12 : " + e); }

        // ── Tests 13–14 : test_sep.csv (séparateur ; + décimale ,) ───────────
        System.out.println("\n── CSV 3 : séparateur ';' et décimale ',' ───────────");
        DfIndividu dfSep = null;
        try {
            System.setOut(devNull);
            dfSep = new LectureCSV(";").lireCSV("data/Test/test_sep.csv", DfIndividu.class);
        } catch (FileEmpty e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally { System.setOut(stdout); }

        // Test 13 : dimensions 2 lignes, 3 colonnes
        total++;
        try {
            if (dfSep != null && dfSep.getNbLignes() == 2 && dfSep.getNbCol() == 3) {
                System.out.println("PASS Test 13 : lecture ; → 2 lignes, 3 colonnes");
                dfSep.afficherPremieresFignes(2);
                reussis++;
            } else {
                System.out.println("FAIL Test 13 : df=" + dfSep);
            }
        } catch (Exception e) { System.out.println("FAIL Test 13 : " + e); }

        // Test 14 : "30,5" → Double 30.5
        total++;
        try {
            Object val = dfSep.getCase(0, 1);
            if (val instanceof Double && Math.abs((Double) val - 30.5) < 1e-9) {
                System.out.println("PASS Test 14 : '30,5' → Double 30.5");
                reussis++;
            } else {
                System.out.println("FAIL Test 14 : '30,5' → " + val
                        + " (" + (val != null ? val.getClass().getSimpleName() : "null") + ")");
            }
        } catch (Exception e) { System.out.println("FAIL Test 14 : " + e); }

        // ── Test 15 : fichier inexistant ──────────────────────────────────────
        System.out.println("\n── CSV 4 : cas d'erreur ─────────────────────────────");
        total++;
        try {
            DfIndividu df = new LectureCSV(",").lireCSV("fichier_inexistant.csv", DfIndividu.class);
            if (df == null) {
                System.out.println("PASS Test 15 : fichier inexistant → null");
                reussis++;
            } else {
                System.out.println("FAIL Test 15 : df devrait être null");
            }
        } catch (Exception e) {
            System.out.println("FAIL Test 15 : exception inattendue " + e);
        }


        System.out.println("\n═══════════════════════════════════════════════════");
        System.out.println("=== LectureCSV : " + reussis + "/" + total + " tests réussis ===");
        System.out.println("═══════════════════════════════════════════════════");
    }
}
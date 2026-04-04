package fish.acquisition.lecture;

import fish.acquisition.DataframeComplet;
import fish.acquisition.DfIndividu;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

import fish.exceptions.*;

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

    /**
     * Constructeur avec séparateur personnalisé
     *
     * @param separateur le séparateur du fichier CSV
     */
    public LectureCSV(String separateur) {
        this.separateur = separateur;
    }

    /** Constructeur par défaut avec virgule comme séparateur */
    public LectureCSV() {
        this(",");
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

        try (Scanner scanner = new Scanner(new File(cheminFichier))) {

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

        // ────────────────────────────────────────────────────────────────────
        // CSV 1 — Format standard (virgule, entêtes ligne 1)
        // Contenu : espece,longueur,poids,nbvers
        // Merlan,30.5,200,5
        // Hareng,25.0,150,0
        // Thon,80.0,5000,12
        // ,35.0,,3 ← ligne avec nulls
        // ────────────────────────────────────────────────────────────────────
        System.out.println("── CSV 1 : format standard ──────────────────────────");
        LectureCSV lecteur = new LectureCSV(",");

        // Test 1 : lecture sans exception
        total++;
        try {
            DfIndividu df = lecteur.lireCSV("src/data/Test/test_simple.csv", DfIndividu.class);
            if (df != null) {
                System.out.println("PASS Test 1 : lecture sans exception");
                reussis++;
            } else {
                System.out.println("FAIL Test 1 : df est null");
            }
        } catch (Exception e) {
            System.out.println("FAIL Test 1 : " + e);
        }

        // Test 2 : dimensions correctes (4 lignes, 4 colonnes)
        total++;
        try {
            DfIndividu df = lecteur.lireCSV("src/data/Test/test_simple.csv", DfIndividu.class);
            int[] size = df.getSize();
            if (size[0] == 4 && size[1] == 4) {
                System.out.println("PASS Test 2 : dimensions 4x4");
                reussis++;
            } else {
                System.out.println("FAIL Test 2 : dimensions attendues [4,4], obtenues ["
                        + size[0] + "," + size[1] + "]");
            }
        } catch (Exception e) {
            System.out.println("FAIL Test 2 : " + e);
        }

        // Test 3 : entêtes correctes
        total++;
        try {
            DfIndividu df = lecteur.lireCSV("src/data/Test/test_simple.csv", DfIndividu.class);
            String[] noms = df.getNomColonnes();
            if ("espece".equals(noms[0]) && "longueur".equals(noms[1])
                    && "poids".equals(noms[2]) && "nbvers".equals(noms[3])) {
                System.out.println("PASS Test 3 : entêtes correctes");
                reussis++;
            } else {
                System.out.println("FAIL Test 3 : entêtes incorrectes : "
                        + java.util.Arrays.toString(noms));
            }
        } catch (Exception e) {
            System.out.println("FAIL Test 3 : " + e);
        }

        // Test 4 : types détectés — longueur=Double, nbvers=Integer
        total++;
        try {
            DfIndividu df = lecteur.lireCSV("src/data/Test/test_simple.csv", DfIndividu.class);
            Object longueur = df.getCase(0, 1); // 30.5
            Object nbvers = df.getCase(0, 3); // 5
            if (longueur instanceof Double && nbvers instanceof Integer) {
                System.out.println("PASS Test 4 : types détectés (Double, Integer)");
                reussis++;
            } else {
                System.out.println("FAIL Test 4 : types incorrects — longueur="
                        + longueur.getClass().getSimpleName()
                        + ", nbvers=" + nbvers.getClass().getSimpleName());
            }
        } catch (Exception e) {
            System.out.println("FAIL Test 4 : " + e);
        }

        // Test 5 : valeur numérique correcte
        total++;
        try {
            DfIndividu df = lecteur.lireCSV("src/data/Test/test_simple.csv", DfIndividu.class);
            double longueur = (Double) df.getCase(0, 1); // 30.5
            if (Math.abs(longueur - 30.5) < 1e-9) {
                System.out.println("PASS Test 5 : valeur 30.5 correcte");
                reussis++;
            } else {
                System.out.println("FAIL Test 5 : longueur attendue 30.5, obtenue " + longueur);
            }
        } catch (Exception e) {
            System.out.println("FAIL Test 5 : " + e);
        }

        // Test 6 : cellule vide → null
        total++;
        try {
            DfIndividu df = lecteur.lireCSV("src/data/Test/test_simple.csv", DfIndividu.class);
            Object espece = df.getCase(3, 0); // ligne 4, col espece → vide
            Object poids = df.getCase(3, 2); // ligne 4, col poids → vide
            if (espece == null && poids == null) {
                System.out.println("PASS Test 6 : cellules vides → null");
                reussis++;
            } else {
                System.out.println("FAIL Test 6 : espece=" + espece + ", poids=" + poids);
            }
        } catch (Exception e) {
            System.out.println("FAIL Test 6 : " + e);
        }

        // Test 7 : valeur String correcte
        total++;
        try {
            DfIndividu df = lecteur.lireCSV("src/data/Test/test_simple.csv", DfIndividu.class);
            Object espece = df.getCase(1, 0); // "Hareng"
            if ("Hareng".equals(espece)) {
                System.out.println("PASS Test 7 : valeur String 'Hareng' correcte");
                reussis++;
            } else {
                System.out.println("FAIL Test 7 : espece attendue 'Hareng', obtenue " + espece);
            }
        } catch (Exception e) {
            System.out.println("FAIL Test 7 : " + e);
        }

        // ────────────────────────────────────────────────────────────────────
        // CSV 2 — Valeurs spéciales (±, <LOQ, min-max, null)
        // Contenu : espece,longueur,poids,prevalence,ic95
        // Merlan,34.83 ± 2.32,325.78 ± 54.71,64.76,55.47-74.05
        // Hareng,<LOQ,459.17 ± 95.28,77.65,68.61-86.69
        // Thon,,<12.5,22.67,12.97-32.36
        // ────────────────────────────────────────────────────────────────────
        System.out.println("\n── CSV 2 : valeurs spéciales ────────────────────────");
        LectureCSV lecteurSpecial = new LectureCSV(",");

        // Test 8 : "34.83 ± 2.32" → Double 34.83
        total++;
        try {
            DfIndividu df = lecteurSpecial.lireCSV("src/data/Test/test_special.csv", DfIndividu.class);
            Object val = df.getCase(0, 1); // longueur Merlan
            if (val instanceof Double && Math.abs((Double) val - 34.83) < 1e-9) {
                System.out.println("PASS Test 8 : '34.83 ± 2.32' → 34.83");
                reussis++;
            } else {
                System.out.println("FAIL Test 8 : '34.83 ± 2.32' → " + val);
            }
        } catch (Exception e) {
            System.out.println("FAIL Test 8 : " + e);
        }

        // Test 9 : "<LOQ" → 0.0
        total++;
        try {
            DfIndividu df = lecteurSpecial.lireCSV("src/data/Test/test_special.csv", DfIndividu.class);
            Object val = df.getCase(1, 1); // longueur Hareng = <LOQ
            if (Double.valueOf(0.0).equals(val)) {
                System.out.println("PASS Test 9 : '<LOQ' → 0.0");
                reussis++;
            } else {
                System.out.println("FAIL Test 9 : '<LOQ' → " + val
                        + " (" + (val != null ? val.getClass().getSimpleName() : "null") + ")");
            }
        } catch (Exception e) {
            System.out.println("FAIL Test 9 : " + e);
        }

        // Test 10 : "<12.5" → 6.25 (moitié)
        total++;
        try {
            DfIndividu df = lecteurSpecial.lireCSV("src/data/Test/test_special.csv", DfIndividu.class);
            Object val = df.getCase(2, 2); // poids Thon = <12.5
            if (val instanceof Double && Math.abs((Double) val - 6.25) < 1e-9) {
                System.out.println("PASS Test 10 : '<12.5' → 6.25");
                reussis++;
            } else {
                System.out.println("FAIL Test 10 : '<12.5' → " + val);
            }
        } catch (Exception e) {
            System.out.println("FAIL Test 10 : " + e);
        }

        // Test 11 : "55.47-74.05" → 64.76 (centre intervalle)
        total++;
        try {
            DfIndividu df = lecteurSpecial.lireCSV("src/data/Test/test_special.csv", DfIndividu.class);
            Object val = df.getCase(0, 4); // ic95 Merlan = "55.47-74.05"
            if (val instanceof Double && Math.abs((Double) val - 64.76) < 1e-9) {
                System.out.println("PASS Test 11 : '55.47-74.05' → 64.76");
                reussis++;
            } else {
                System.out.println("FAIL Test 11 : '55.47-74.05' → " + val);
            }
        } catch (Exception e) {
            System.out.println("FAIL Test 11 : " + e);
        }

        // Test 12 : cellule vide (longueur Thon) → null
        total++;
        try {
            DfIndividu df = lecteurSpecial.lireCSV("src/data/Test/test_special.csv", DfIndividu.class);
            Object val = df.getCase(2, 1); // longueur Thon → vide
            if (val == null) {
                System.out.println("PASS Test 12 : cellule vide → null");
                reussis++;
            } else {
                System.out.println("FAIL Test 12 : cellule vide → " + val);
            }
        } catch (Exception e) {
            System.out.println("FAIL Test 12 : " + e);
        }

        // ────────────────────────────────────────────────────────────────────
        // CSV 3 — Séparateur point-virgule + décimale virgule
        // Contenu : espece;longueur;poids
        // Merlan;30,5;200
        // Hareng;25,0;150
        // ────────────────────────────────────────────────────────────────────
        System.out.println("\n── CSV 3 : séparateur ';' et décimale ',' ───────────");
        LectureCSV lecteurFR = new LectureCSV(";");

        // Test 13 : lecture avec séparateur ;
        total++;
        try {
            DfIndividu df = lecteurFR.lireCSV("src/data/Test/test_sep.csv", DfIndividu.class);
            if (df != null && df.getNbLignes() == 2 && df.getNbCol() == 3) {
                System.out.println("PASS Test 13 : lecture ; → 2 lignes, 3 colonnes");
                reussis++;
            } else {
                System.out.println("FAIL Test 13 : df=" + df);
            }
        } catch (Exception e) {
            System.out.println("FAIL Test 13 : " + e);
        }

        // Test 14 : "30,5" → Double 30.5
        total++;
        try {
            DfIndividu df = lecteurFR.lireCSV("src/data/Test/test_sep.csv", DfIndividu.class);
            Object val = df.getCase(0, 1); // longueur Merlan = "30,5"
            if (val instanceof Double && Math.abs((Double) val - 30.5) < 1e-9) {
                System.out.println("PASS Test 14 : '30,5' → Double 30.5");
                reussis++;
            } else {
                System.out.println("FAIL Test 14 : '30,5' → " + val
                        + " (" + (val != null ? val.getClass().getSimpleName() : "null") + ")");
            }
        } catch (Exception e) {
            System.out.println("FAIL Test 14 : " + e);
        }

        // ────────────────────────────────────────────────────────────────────
        // CSV 4 — Fichier inexistant
        // ────────────────────────────────────────────────────────────────────
        System.out.println("\n── CSV 4 : cas d'erreur ─────────────────────────────");

        // Test 15 : fichier inexistant → retourne null (pas d'exception)
        total++;
        try {
            DfIndividu df = lecteur.lireCSV("fichier_inexistant.csv", DfIndividu.class);
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

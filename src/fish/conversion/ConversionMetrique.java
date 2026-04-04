package fish.conversion;

import fish.acquisition.*;
import fish.acquisition.DfIndividu;
import fish.exceptions.OutOfBoundException;
import fish.poisson.Individu;
import fish.poisson.Population;

/**
 * Classe utilitaire de conversion d'unités métriques.
 *
 * Conversions de longueur : mm ↔ cm ↔ m
 * Conversions de poids : g ↔ kg ↔ t
 *
 * Supporte trois cibles :
 * - Dataframe : conversion d'une colonne entière
 * - Individu : conversion des champs longueur / poids
 * - Population : conversion sur tous les individus du tableau
 *
 * @author Jules Grenesche
 * @version 0.1
 */
public class ConversionMetrique {

    ////////////////////////////// Enums ////////////////////

    /** Unités de longueur métriques */
    public enum UniteLongueur {
        MM, CM, M
    }

    /** Unités de poids métriques */
    public enum UnitePoids {
        MG, G, KG
    }

    ////////////////////////////// Facteurs ////////////////////

    // Longueur — tout passe par MM comme pivot
    private static final double MM_PAR_CM = 10.0;
    private static final double MM_PAR_M = 1000.0;

    // Poids — tout passe par MG comme pivot
    private static final double MG_PAR_G = 1000.0;
    private static final double MG_PAR_KG = 1_000_000.0;

    ////////////////////////////// Scalaires ////////////////////

    /**
     * Convertit une longueur d'une unité métrique vers une autre.
     *
     * @param valeur la valeur à convertir
     * @param de     l'unité source
     * @param vers   l'unité cible
     * @return la valeur convertie
     */
    public static double convertirLongueur(double valeur,
            UniteLongueur de, UniteLongueur vers) {
        if (de == vers)
            return valeur;

        // Normalisation en mm
        double enMM = switch (de) {
            case MM -> valeur;
            case CM -> valeur * MM_PAR_CM;
            case M -> valeur * MM_PAR_M;
        };

        // Conversion vers l'unité cible
        return switch (vers) {
            case MM -> enMM;
            case CM -> enMM / MM_PAR_CM;
            case M -> enMM / MM_PAR_M;
        };
    }

    /**
     * Convertit un poids d'une unité métrique vers une autre.
     *
     * @param valeur la valeur à convertir
     * @param de     l'unité source
     * @param vers   l'unité cible
     * @return la valeur convertie
     */
    public static double convertirPoids(double valeur, UnitePoids de, UnitePoids vers) {
        if (de == vers)
            return valeur;

        // Normalisation en milligrammes
        double enMG = switch (de) {
            case MG -> valeur;
            case G -> valeur * MG_PAR_G;
            case KG -> valeur * MG_PAR_KG;
        };

        // Conversion vers l'unité cible
        return switch (vers) {
            case MG -> enMG;
            case G -> enMG / MG_PAR_G;
            case KG -> enMG / MG_PAR_KG;
        };
    }

    ////////////////////////////// Dataframe ////////////////////

    /**
     * Convertit toutes les valeurs numériques d'une colonne de longueur.
     * Les valeurs null sont ignorées.
     *
     * @param df   le dataframe à modifier en place
     * @param col  l'index de la colonne
     * @param de   l'unité source
     * @param vers l'unité cible
     * @return le nombre de valeurs converties
     */
    public static int convertirColonneLongueur(DataframeComplet df, int col,
            UniteLongueur de, UniteLongueur vers) {
        int converties = 0;
        for (int i = 0; i < df.getNbLignes(); i++) {
            try {
                Object val = df.getCase(i, col);
                if (val instanceof Number) {
                    double converti = convertirLongueur(((Number) val).doubleValue(), de, vers);
                    df.setCase(i, col, converti);
                    converties++;
                }
            } catch (OutOfBoundException e) {
                /* ignoré */ }
        }
        System.out.println("Longueur colonne " + col + " convertie : "
                + de + " → " + vers + " (" + converties + " valeurs)");
        return converties;
    }

    /**
     * Convertit toutes les valeurs numériques d'une colonne de poids.
     * Les valeurs null sont ignorées.
     *
     * @param df   le dataframe à modifier en place
     * @param col  l'index de la colonne
     * @param de   l'unité source
     * @param vers l'unité cible
     * @return le nombre de valeurs converties
     */
    public static int convertirColonnePoids(DataframeComplet df, int col,
            UnitePoids de, UnitePoids vers) {
        int converties = 0;
        for (int i = 0; i < df.getNbLignes(); i++) {
            try {
                Object val = df.getCase(i, col);
                if (val instanceof Number) {
                    double converti = convertirPoids(((Number) val).doubleValue(), de, vers);
                    df.setCase(i, col, converti);
                    converties++;
                }
            } catch (OutOfBoundException e) {
                /* ignoré */ }
        }
        System.out.println("Poids colonne " + col + " convertie : "
                + de + " → " + vers + " (" + converties + " valeurs)");
        return converties;
    }

    /**
     * Convertit la colonne dont le nom contient le mot-clé.
     * Cherche automatiquement l'index.
     *
     * @param df     le dataframe
     * @param motCle le mot-clé (ex: "longueur", "size", "poids")
     * @param de     l'unité source
     * @param vers   l'unité cible
     * @return le nombre de valeurs converties, ou -1 si colonne non trouvée
     */
    public static int convertirColonneLongueurParNom(DataframeComplet df, String motCle,
            UniteLongueur de, UniteLongueur vers) {
        int col = getIndexColonne(df, motCle);
        if (col < 0) {
            System.out.println("Colonne '" + motCle + "' introuvable.");
            return -1;
        }
        return convertirColonneLongueur(df, col, de, vers);
    }

    /**
     * Convertit la colonne de poids dont le nom contient le mot-clé.
     *
     * @param df     le dataframe
     * @param motCle le mot-clé (ex: "poids", "weight", "masse")
     * @param de     l'unité source
     * @param vers   l'unité cible
     * @return le nombre de valeurs converties, ou -1 si colonne non trouvée
     */
    public static int convertirColonnePoidsParNom(DataframeComplet df, String motCle,
            UnitePoids de, UnitePoids vers) {
        int col = getIndexColonne(df, motCle);
        if (col < 0) {
            System.out.println("Colonne '" + motCle + "' introuvable.");
            return -1;
        }
        return convertirColonnePoids(df, col, de, vers);
    }

    ////////////////////////////// Individu ////////////////////

    /**
     * Convertit la longueur d'un Individu.
     * Retourne un nouvel Individu avec la longueur convertie.
     *
     * @param individu l'individu source
     * @param de       l'unité source
     * @param vers     l'unité cible
     * @return la longueur convertie (float)
     */
    public static float convertirLongueurIndividu(Individu individu,
            UniteLongueur de, UniteLongueur vers) {
        double convertie = convertirLongueur(individu.getLongueur(), de, vers);
        return (float) convertie;
    }

    /**
     * Convertit le poids d'un Individu.
     *
     * @param individu l'individu source
     * @param de       l'unité source
     * @param vers     l'unité cible
     * @return le poids converti (float)
     */
    public static float convertirPoidsIndividu(Individu individu,
            UnitePoids de, UnitePoids vers) {
        double converti = convertirPoids(individu.getPoids(), de, vers);
        return (float) converti;
    }

    ////////////////////////////// Population ////////////////////

    /**
     * Retourne les longueurs converties de tous les individus d'une population.
     * Ne modifie pas les objets Individu (immuabilité des setters privés).
     *
     * @param population la population source
     * @param de         l'unité source
     * @param vers       l'unité cible
     * @return tableau des longueurs converties, dans le même ordre que tabIndividu
     */
    public static float[] convertirLongueursPopulation(Population population,
            UniteLongueur de, UniteLongueur vers) {
        Individu[] individus = population.getTabIndividu();
        if (individus == null) {
            System.out.println("Population sans tableau d'individus.");
            return new float[0];
        }
        float[] resultats = new float[individus.length];
        for (int i = 0; i < individus.length; i++) {
            resultats[i] = convertirLongueurIndividu(individus[i], de, vers);
        }
        System.out.println("Longueurs population converties : "
                + de + " → " + vers + " (" + individus.length + " individus)");
        return resultats;
    }

    /**
     * Retourne les poids convertis de tous les individus d'une population.
     *
     * @param population la population source
     * @param de         l'unité source
     * @param vers       l'unité cible
     * @return tableau des poids convertis, dans le même ordre que tabIndividu
     */
    public static float[] convertirPoidsPopulation(Population population,
            UnitePoids de, UnitePoids vers) {
        Individu[] individus = population.getTabIndividu();
        if (individus == null) {
            System.out.println("Population sans tableau d'individus.");
            return new float[0];
        }
        float[] resultats = new float[individus.length];
        for (int i = 0; i < individus.length; i++) {
            resultats[i] = convertirPoidsIndividu(individus[i], de, vers);
        }
        System.out.println("Poids population convertis : "
                + de + " → " + vers + " (" + individus.length + " individus)");
        return resultats;
    }

    ////////////////////////////// Utilitaire ////////////////////

    private static int getIndexColonne(DataframeComplet df, String motCle) {
        String[] noms = df.getNomColonnes();
        for (int j = 0; j < noms.length; j++) {
            if (noms[j].toLowerCase().contains(motCle.toLowerCase()))
                return j;
        }
        return -1;
    }

    ////////////////////////////// Main — tests

    public static void main(String[] args) {
        int reussis = 0, total = 0;

        // ── Conversions scalaires : longueur ──────────────────────────────────
        System.out.println("── convertirLongueur (scalaire) ─────────────────────");

        // Test 1 : mm → cm
        total++;
        double res = convertirLongueur(425.0, UniteLongueur.MM, UniteLongueur.CM);
        if (Math.abs(res - 42.5) < 1e-9) {
            System.out.println("PASS Test 1 : 425 mm → 42.5 cm");
            reussis++;
        } else {
            System.out.println("FAIL Test 1 : 425 mm → " + res);
        }

        // Test 2 : cm → m
        total++;
        res = convertirLongueur(42.5, UniteLongueur.CM, UniteLongueur.M);
        if (Math.abs(res - 0.425) < 1e-9) {
            System.out.println("PASS Test 2 : 42.5 cm → 0.425 m");
            reussis++;
        } else {
            System.out.println("FAIL Test 2 : 42.5 cm → " + res);
        }

        // Test 3 : m → mm
        total++;
        res = convertirLongueur(1.0, UniteLongueur.M, UniteLongueur.MM);
        if (Math.abs(res - 1000.0) < 1e-9) {
            System.out.println("PASS Test 3 : 1 m → 1000 mm");
            reussis++;
        } else {
            System.out.println("FAIL Test 3 : 1 m → " + res);
        }

        // Test 4 : même unité → valeur inchangée
        total++;
        res = convertirLongueur(30.0, UniteLongueur.CM, UniteLongueur.CM);
        if (Math.abs(res - 30.0) < 1e-9) {
            System.out.println("PASS Test 4 : 30 cm → 30 cm (identique)");
            reussis++;
        } else {
            System.out.println("FAIL Test 4 : " + res);
        }

        // Test 5 : aller-retour mm → cm → mm
        total++;
        double original = 537.0;
        double retour = convertirLongueur(
                convertirLongueur(original, UniteLongueur.MM, UniteLongueur.CM),
                UniteLongueur.CM, UniteLongueur.MM);
        if (Math.abs(retour - original) < 1e-6) {
            System.out.println("PASS Test 5 : aller-retour mm → cm → mm");
            reussis++;
        } else {
            System.out.println("FAIL Test 5 : aller-retour → " + retour);
        }

        // ── Conversions scalaires : poids ─────────────────────────────────────
        System.out.println("\n── convertirPoids (scalaire) ────────────────────────");

        // Test 6 : mg → g
        total++;
        res = convertirPoids(1500.0, UnitePoids.MG, UnitePoids.G);
        if (Math.abs(res - 1.5) < 1e-9) {
            System.out.println("PASS Test 6 : 1500 mg → 1.5 g");
            reussis++;
        } else {
            System.out.println("FAIL Test 6 : 1500 mg → " + res);
        }

        // Test 7 : g → kg
        total++;
        res = convertirPoids(2500.0, UnitePoids.G, UnitePoids.KG);
        if (Math.abs(res - 2.5) < 1e-9) {
            System.out.println("PASS Test 7 : 2500 g → 2.5 kg");
            reussis++;
        } else {
            System.out.println("FAIL Test 7 : 2500 g → " + res);
        }

        // Test 8 : kg → mg
        total++;
        res = convertirPoids(0.001, UnitePoids.KG, UnitePoids.MG);
        if (Math.abs(res - 1000.0) < 1e-6) {
            System.out.println("PASS Test 8 : 0.001 kg → 1000 mg");
            reussis++;
        } else {
            System.out.println("FAIL Test 8 : 0.001 kg → " + res);
        }

        // Test 9 : aller-retour mg → kg → mg
        total++;
        double origPoids = 325.78;
        double retourP = convertirPoids(
                convertirPoids(origPoids, UnitePoids.MG, UnitePoids.KG),
                UnitePoids.KG, UnitePoids.MG);
        if (Math.abs(retourP - origPoids) < 1e-6) {
            System.out.println("PASS Test 9 : aller-retour mg → kg → mg");
            reussis++;
        } else {
            System.out.println("FAIL Test 9 : aller-retour → " + retourP);
        }

        // ── Conversion sur Dataframe (colonne) ────────────────────────────────
        System.out.println("\n── convertirColonneLongueur / convertirColonnePoids ─");

        DfIndividu dfCol = null;
        try {
            Object[][] data = {{100.0, 2000.0}, {200.0, null}, {300.0, 3000.0}};
            dfCol = new DfIndividu(3, new String[]{"longueur_mm", "poids_g"}, data);
        } catch (Exception e) { System.out.println("Setup échoué : " + e); }

        // Test 10 : convertirColonneLongueur mm→cm — 3 valeurs converties (null ignoré)
        total++;
        try {
            int n = convertirColonneLongueur(dfCol, 0, UniteLongueur.MM, UniteLongueur.CM);
            Object v0 = dfCol.getCase(0, 0);
            if (n == 3 && v0 instanceof Number && Math.abs(((Number) v0).doubleValue() - 10.0) < 1e-9) {
                System.out.println("PASS Test 10 : convertirColonneLongueur mm→cm → 3 valeurs, 100mm=10cm");
                reussis++;
            } else {
                System.out.println("FAIL Test 10 : n=" + n + ", v0=" + v0);
            }
        } catch (Exception e) { System.out.println("FAIL Test 10 : " + e); }

        // Test 11 : convertirColonnePoids g→kg — 2 valeurs (null ignoré)
        total++;
        try {
            int n = convertirColonnePoids(dfCol, 1, UnitePoids.G, UnitePoids.KG);
            Object v0 = dfCol.getCase(0, 1);
            if (n == 2 && v0 instanceof Number && Math.abs(((Number) v0).doubleValue() - 2.0) < 1e-9) {
                System.out.println("PASS Test 11 : convertirColonnePoids g→kg → 2 valeurs, 2000g=2.0kg");
                reussis++;
            } else {
                System.out.println("FAIL Test 11 : n=" + n + ", v0=" + v0);
            }
        } catch (Exception e) { System.out.println("FAIL Test 11 : " + e); }

        // ── Conversion par nom de colonne ─────────────────────────────────────
        System.out.println("\n── convertirColonneLongueurParNom / PoidsParNom ─────");

        DfIndividu dfNom = null;
        try {
            Object[][] data2 = {{500.0, 1000.0}, {750.0, 1500.0}};
            dfNom = new DfIndividu(2, new String[]{"longueur_mm", "poids_g"}, data2);
        } catch (Exception e) { System.out.println("Setup 2 échoué : " + e); }

        // Test 12 : convertirColonneLongueurParNom avec mot-clé "longueur"
        total++;
        try {
            int n = convertirColonneLongueurParNom(dfNom, "longueur", UniteLongueur.MM, UniteLongueur.CM);
            Object v = dfNom.getCase(0, 0);
            if (n == 2 && v instanceof Number && Math.abs(((Number) v).doubleValue() - 50.0) < 1e-9) {
                System.out.println("PASS Test 12 : convertirColonneLongueurParNom 'longueur' → 500mm=50cm");
                reussis++;
            } else {
                System.out.println("FAIL Test 12 : n=" + n + ", v=" + v);
            }
        } catch (Exception e) { System.out.println("FAIL Test 12 : " + e); }

        // Test 13 : mot-clé introuvable → -1
        total++;
        try {
            int n = convertirColonneLongueurParNom(dfNom, "absent", UniteLongueur.MM, UniteLongueur.CM);
            if (n == -1) {
                System.out.println("PASS Test 13 : mot-clé absent → -1");
                reussis++;
            } else {
                System.out.println("FAIL Test 13 : attendu -1, obtenu " + n);
            }
        } catch (Exception e) { System.out.println("FAIL Test 13 : " + e); }

        // Test 14 : convertirColonnePoidsParNom avec mot-clé "poids"
        total++;
        try {
            int n = convertirColonnePoidsParNom(dfNom, "poids", UnitePoids.G, UnitePoids.KG);
            Object v = dfNom.getCase(0, 1);
            if (n == 2 && v instanceof Number && Math.abs(((Number) v).doubleValue() - 1.0) < 1e-9) {
                System.out.println("PASS Test 14 : convertirColonnePoidsParNom 'poids' → 1000g=1.0kg");
                reussis++;
            } else {
                System.out.println("FAIL Test 14 : n=" + n + ", v=" + v);
            }
        } catch (Exception e) { System.out.println("FAIL Test 14 : " + e); }

        // ── Conversion sur Individu ───────────────────────────────────────────
        System.out.println("\n── convertirLongueurIndividu / convertirPoidsIndividu ─");

        fish.poisson.Individu ind = null;
        try {
            ind = new fish.poisson.Individu("Merlan", 250f, 500f, 3,
                    new java.util.ArrayList<>());
        } catch (Exception e) { System.out.println("Setup Individu échoué : " + e); }

        // Test 15 : convertirLongueurIndividu mm→cm
        total++;
        try {
            float lon = convertirLongueurIndividu(ind, UniteLongueur.MM, UniteLongueur.CM);
            if (Math.abs(lon - 25.0f) < 1e-4) {
                System.out.println("PASS Test 15 : individu 250mm → 25.0cm");
                reussis++;
            } else {
                System.out.println("FAIL Test 15 : attendu 25.0, obtenu " + lon);
            }
        } catch (Exception e) { System.out.println("FAIL Test 15 : " + e); }

        // Test 16 : convertirPoidsIndividu g→kg
        total++;
        try {
            float pds = convertirPoidsIndividu(ind, UnitePoids.G, UnitePoids.KG);
            if (Math.abs(pds - 0.5f) < 1e-4) {
                System.out.println("PASS Test 16 : individu 500g → 0.5kg");
                reussis++;
            } else {
                System.out.println("FAIL Test 16 : attendu 0.5, obtenu " + pds);
            }
        } catch (Exception e) { System.out.println("FAIL Test 16 : " + e); }

        // ── Conversion sur Population ─────────────────────────────────────────
        System.out.println("\n── convertirLongueursPopulation / convertirPoidsPopulation ─");

        fish.poisson.Population pop = null;
        try {
            fish.poisson.Individu i1 = new fish.poisson.Individu("Merlan", 200f, 300f, 2, new java.util.ArrayList<>());
            fish.poisson.Individu i2 = new fish.poisson.Individu("Merlan", 300f, 500f, 0, new java.util.ArrayList<>());
            pop = new fish.poisson.Population(2, "Merlan", new String[]{"foie"},
                    new fish.poisson.Individu[]{i1, i2});
        } catch (Exception e) { System.out.println("Setup Population échoué : " + e); }

        // Test 17 : convertirLongueursPopulation mm→cm — tableau de 2 valeurs
        total++;
        try {
            float[] lons = convertirLongueursPopulation(pop, UniteLongueur.MM, UniteLongueur.CM);
            if (lons.length == 2
                    && Math.abs(lons[0] - 20.0f) < 1e-4
                    && Math.abs(lons[1] - 30.0f) < 1e-4) {
                System.out.println("PASS Test 17 : population 200mm→20cm, 300mm→30cm");
                reussis++;
            } else {
                System.out.println("FAIL Test 17 : lons=" + java.util.Arrays.toString(lons));
            }
        } catch (Exception e) { System.out.println("FAIL Test 17 : " + e); }

        // Test 18 : convertirPoidsPopulation g→kg
        total++;
        try {
            float[] poids = convertirPoidsPopulation(pop, UnitePoids.G, UnitePoids.KG);
            if (poids.length == 2
                    && Math.abs(poids[0] - 0.3f) < 1e-4
                    && Math.abs(poids[1] - 0.5f) < 1e-4) {
                System.out.println("PASS Test 18 : population 300g→0.3kg, 500g→0.5kg");
                reussis++;
            } else {
                System.out.println("FAIL Test 18 : poids=" + java.util.Arrays.toString(poids));
            }
        } catch (Exception e) { System.out.println("FAIL Test 18 : " + e); }

        // Test 19 : population null (tabIndividu null) → tableau vide
        total++;
        try {
            fish.poisson.Population popVide = new fish.poisson.Population(
                    5, "Hareng", 2, new String[]{"estomac"}, 3.0, 1.5);
            float[] lons = convertirLongueursPopulation(popVide, UniteLongueur.MM, UniteLongueur.CM);
            if (lons.length == 0) {
                System.out.println("PASS Test 19 : population sans tabIndividu → tableau vide");
                reussis++;
            } else {
                System.out.println("FAIL Test 19 : longueur tableau = " + lons.length);
            }
        } catch (Exception e) { System.out.println("FAIL Test 19 : " + e); }

        // ── Sur fichier réel mackerel ─────────────────────────────────────────
        System.out.println("\n── Conversion colonne sur mackerel.97442.csv ────────");

        // Test 20 : convertirColonneLongueurParNom sur mackerel
        total++;
        try {
            fish.acquisition.lecture.LectureCSV lect = new fish.acquisition.lecture.LectureCSV(";");
            DfIndividu dfMack = lect.lireCSV("data/mackerel.97442.csv", DfIndividu.class);
            if (dfMack != null) {
                System.out.println("── Avant conversion (mackerel, StandardLength) ──");
                dfMack.afficherPremieresFignes(5);
                // Colonne "StandardLength" en mm → cm
                int n = convertirColonneLongueurParNom(dfMack, "StandardLength",
                        UniteLongueur.MM, UniteLongueur.CM);
                if (n > 0) {
                    System.out.println("PASS Test 20 : StandardLength mm→cm → " + n + " valeur(s)");
                    System.out.println("── Après conversion (mackerel, StandardLength) ──");
                    dfMack.afficherPremieresFignes(5);
                    reussis++;
                } else {
                    System.out.println("FAIL Test 20 : n=" + n);
                }
            } else {
                System.out.println("SKIP Test 20 : mackerel non disponible");
                total--;
            }
        } catch (Exception e) { System.out.println("FAIL Test 20 : " + e); }

        System.out.println("\n═══════════════════════════════════════════════════");
        System.out.println("=== ConversionMetrique : " + reussis + "/" + total + " tests réussis ===");
        System.out.println("═══════════════════════════════════════════════════");
    }
}
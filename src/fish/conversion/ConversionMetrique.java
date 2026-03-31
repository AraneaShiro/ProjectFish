package fish.conversion;

import fish.acquisition.Dataframe;
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
    public static int convertirColonneLongueur(Dataframe df, int col,
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
    public static int convertirColonnePoids(Dataframe df, int col,
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
    public static int convertirColonneLongueurParNom(Dataframe df, String motCle,
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
    public static int convertirColonnePoidsParNom(Dataframe df, String motCle,
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

    private static int getIndexColonne(Dataframe df, String motCle) {
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

        // ── Longueur ──────────────────────────────────────────────────────
        System.out.println("── Longueur ─────────────────────────────");

        // Test 1 : mm → cm
        total++;
        double res = convertirLongueur(425.0, UniteLongueur.MM, UniteLongueur.CM);
        if (Math.abs(res - 42.5) < 1e-9) {
            System.out.println("PASS Test 1 : 425 mm → 42.5 cm");
            reussis++;
        } else
            System.out.println("FAIL Test 1 : 425 mm → " + res);

        // Test 2 : cm → m
        total++;
        res = convertirLongueur(42.5, UniteLongueur.CM, UniteLongueur.M);
        if (Math.abs(res - 0.425) < 1e-9) {
            System.out.println("PASS Test 2 : 42.5 cm → 0.425 m");
            reussis++;
        } else
            System.out.println("FAIL Test 2 : 42.5 cm → " + res);

        // Test 3 : m → mm
        total++;
        res = convertirLongueur(1.0, UniteLongueur.M, UniteLongueur.MM);
        if (Math.abs(res - 1000.0) < 1e-9) {
            System.out.println("PASS Test 3 : 1 m → 1000 mm");
            reussis++;
        } else
            System.out.println("FAIL Test 3 : 1 m → " + res);

        // Test 4 : même unité → valeur inchangée
        total++;
        res = convertirLongueur(30.0, UniteLongueur.CM, UniteLongueur.CM);
        if (Math.abs(res - 30.0) < 1e-9) {
            System.out.println("PASS Test 4 : 30 cm → 30 cm (identique)");
            reussis++;
        } else
            System.out.println("FAIL Test 4 : " + res);

        // Test 5 : aller-retour mm → cm → mm
        total++;
        double original = 537.0;
        double retour = convertirLongueur(
                convertirLongueur(original, UniteLongueur.MM, UniteLongueur.CM),
                UniteLongueur.CM, UniteLongueur.MM);
        if (Math.abs(retour - original) < 1e-6) {
            System.out.println("PASS Test 5 : aller-retour mm → cm → mm");
            reussis++;
        } else
            System.out.println("FAIL Test 5 : aller-retour → " + retour);

        // ── Poids ─────────────────────────────────────────────────────────
        System.out.println("── Poids ────────────────────────────────");

        // Test 6 : mg → g
        total++;
        res = convertirPoids(1500.0, UnitePoids.MG, UnitePoids.G);
        if (Math.abs(res - 1.5) < 1e-9) {
            System.out.println("PASS Test 6 : 1500 mg → 1.5 g");
            reussis++;
        } else
            System.out.println("FAIL Test 6 : 1500 mg → " + res);

        // Test 7 : g → kg
        total++;
        res = convertirPoids(2500.0, UnitePoids.G, UnitePoids.KG);
        if (Math.abs(res - 2.5) < 1e-9) {
            System.out.println("PASS Test 7 : 2500 g → 2.5 kg");
            reussis++;
        } else
            System.out.println("FAIL Test 7 : 2500 g → " + res);

        // Test 8 : kg → mg
        total++;
        res = convertirPoids(0.001, UnitePoids.KG, UnitePoids.MG);
        if (Math.abs(res - 1000.0) < 1e-6) {
            System.out.println("PASS Test 8 : 0.001 kg → 1000 mg");
            reussis++;
        } else
            System.out.println("FAIL Test 8 : 0.001 kg → " + res);

        // Test 9 : aller-retour mg → kg → mg
        total++;
        double origPoids = 325.78;
        double retourP = convertirPoids(
                convertirPoids(origPoids, UnitePoids.MG, UnitePoids.KG),
                UnitePoids.KG, UnitePoids.MG);
        if (Math.abs(retourP - origPoids) < 1e-6) {
            System.out.println("PASS Test 9 : aller-retour mg → kg → mg");
            reussis++;
        } else
            System.out.println("FAIL Test 9 : aller-retour → " + retourP);
    }
}
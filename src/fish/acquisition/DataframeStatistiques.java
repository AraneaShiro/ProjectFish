// ── Package ─────────────────
package fish.acquisition;

// ── Import ─────────────────
import fish.exceptions.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// ── TESTE : NON ─────────────────
/**
 * Statistiques numériques du dataframe.
 * Calcule moyenne, médiane, écart-type, variance, covariance et corrélation.
 *
 * @author Jules Grenesche
 * @version 0.4
 */
public abstract class DataframeStatistiques extends DataframeManipulation {

    // ── Constructeurs (délégation vers DataframeManipulation)
    // ─────────────────────

    /**
     * Constructeur sans tableau de données.
     *
     * @param nbLignes   le nombre de lignes
     * @param nomColonne les noms des colonnes
     */
    public DataframeStatistiques(int nbLignes, String[] nomColonne) {
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
    public DataframeStatistiques(int nbLignes, String[] nomColonne, Object[][] newtab)
            throws OutOfBoundException, NullParameterException {
        super(nbLignes, nomColonne, newtab);
    }

    // ── Méthode utilitaire interne ────────────────────────────────────────────

    /**
     * Récupère toutes les valeurs numériques d'une colonne (null ignorés).
     *
     * @param col l'index de la colonne
     * @return liste des valeurs numériques
     * @throws OutOfBoundException si l'index est invalide
     */
    private List<Double> getValeursNumeriquesColonne(int col) throws OutOfBoundException {
        if (col < 0 || col >= this.nbCol) {
            throw new OutOfBoundException(0, col, this.nbLignes, this.nbCol);
        }
        List<Double> valeurs = new ArrayList<>();
        for (int i = 0; i < this.nbLignes; i++) {
            Object val = this.tableau[i][col];
            if (val instanceof Number) {
                valeurs.add(((Number) val).doubleValue());
            }
        }
        return valeurs;
    }

    // ── Statistiques a une variable──────────────────────────────────────

    /**
     * Calcule la moyenne des valeurs numériques d'une colonne.
     *
     * @param col l'index de la colonne
     * @return la moyenne, ou 0.0 si aucune valeur numérique
     * @throws OutOfBoundException si l'index est invalide
     */
    @Override
    public double calculerMoyenne(int col) throws OutOfBoundException {
        List<Double> valeurs = getValeursNumeriquesColonne(col);
        if (valeurs.isEmpty())
            return 0.0;

        double somme = 0.0;
        for (double v : valeurs)
            somme += v;
        somme = somme / valeurs.size();

        this.statistiques.put("Moyenne :" + getNomCol(col), somme);
        return somme;
    }

    /**
     * Calcule la médiane des valeurs numériques d'une colonne.
     *
     * @param col l'index de la colonne
     * @return la médiane, ou 0.0 si aucune valeur numérique
     * @throws OutOfBoundException si l'index est invalide
     */
    @Override
    public double calculerMediane(int col) throws OutOfBoundException {
        List<Double> valeurs = getValeursNumeriquesColonne(col);
        if (valeurs.isEmpty())
            return 0.0;

        Collections.sort(valeurs);
        int milieu = valeurs.size() / 2;

        double mediane = (valeurs.size() % 2 == 0) // si pair ou non
                ? (valeurs.get(milieu - 1) + valeurs.get(milieu)) / 2.0
                : valeurs.get(milieu);

        this.statistiques.put("Mediane :" + getNomCol(col), mediane);
        return mediane;
    }

    /**
     * Calcule l'écart-type des valeurs numériques d'une colonne.
     *
     * @param col l'index de la colonne
     * @return l'écart-type, ou 0.0 si aucune valeur numérique
     * @throws OutOfBoundException si l'index est invalide
     */
    @Override
    public double calculerEcartType(int col) throws OutOfBoundException {
        List<Double> valeurs = getValeursNumeriquesColonne(col);
        if (valeurs.isEmpty())
            return 0.0;

        double moyenne = calculerMoyenne(col);
        double sommeCarre = 0.0;
        for (double v : valeurs) {
            sommeCarre += Math.pow(v - moyenne, 2);
        }
        double ecartType = Math.sqrt(sommeCarre / valeurs.size());
        this.statistiques.put("EcartType :" + getNomCol(col), ecartType);
        return ecartType;
    }

    /**
     * Calcule la variance des valeurs numériques d'une colonne.
     * Variance = moyenne des carrés des écarts à la moyenne.
     *
     * @param col l'index de la colonne
     * @return la variance, ou 0.0 si aucune valeur numérique
     * @throws OutOfBoundException si l'index est invalide
     */
    public double calculerVariance(int col) throws OutOfBoundException {
        List<Double> valeurs = getValeursNumeriquesColonne(col);
        if (valeurs.isEmpty())
            return 0.0;

        double moyenne = calculerMoyenne(col);
        double sommeCarre = 0.0;
        for (double v : valeurs) {
            sommeCarre += Math.pow(v - moyenne, 2);
        }
        double variance = sommeCarre / valeurs.size();
        this.statistiques.put("Variance :" + getNomCol(col), variance);
        return variance;
    }

    // ── Statistiques a 2 valeur ────────────────────────────────────────────────

    /**
     * Calcule la covariance entre deux colonnes numériques.
     * CoVariance = moyenne des produits des écarts à la moyenne de chaque colonne.
     *
     * @param col1 l'index de la première colonne
     * @param col2 l'index de la deuxième colonne
     * @return la covariance, ou 0.0 si aucune valeur numérique
     * @throws OutOfBoundException si un index est invalide
     */
    public double calculerCoVariance(int col1, int col2) throws OutOfBoundException {
        List<Double> valeurs1 = getValeursNumeriquesColonne(col1);
        List<Double> valeurs2 = getValeursNumeriquesColonne(col2);
        if (valeurs1.isEmpty() || valeurs2.isEmpty())
            return 0.0;

        int taille = Math.min(valeurs1.size(), valeurs2.size());
        double moyenne1 = calculerMoyenne(col1);
        double moyenne2 = calculerMoyenne(col2);

        double somme = 0.0;
        for (int i = 0; i < taille; i++) {
            somme += (valeurs1.get(i) - moyenne1) * (valeurs2.get(i) - moyenne2);
        }
        double covariance = somme / taille;
        this.statistiques.put("CoVariance :" + getNomCol(col1) + " " + getNomCol(col2), covariance);
        return covariance;
    }

    /**
     * Calcule la corrélation de Pearson entre deux colonnes numériques.
     * Corrélation = CoVariance(col1, col2) / (EcartType(col1) * EcartType(col2)).
     *
     * @param col1 l'index de la première colonne
     * @param col2 l'index de la deuxième colonne
     * @return le coefficient de corrélation entre -1 et 1, ou 0.0 si impossible
     * @throws OutOfBoundException si un index est invalide
     */
    @Override
    public double calculerCorrelation(int col1, int col2) throws OutOfBoundException {
        double ecartType1 = calculerEcartType(col1);
        double ecartType2 = calculerEcartType(col2);

        // Évite la division par zéro si une colonne est constante
        if (ecartType1 == 0.0 || ecartType2 == 0.0)
            return 0.0;

        double correlation = calculerCoVariance(col1, col2) / (ecartType1 * ecartType2);
        this.statistiques.put("Correlation :" + getNomCol(col1) + " " + getNomCol(col2), correlation);
        return correlation;
    }

     //Test
    public static void main(String[] args) {
        int ok = 0, tot = 0;
        System.out.println("=== Tests DataframeStatistiques ===");

        try {
            // Données : x = [10,20,30,40,50]
            Object[][] data = {{10.0, 1.0}, {20.0, 4.0}, {30.0, 9.0}, {40.0, 16.0}, {50.0, 25.0}};
            DfIndividu df = new DfIndividu(5, new String[]{"x", "y"}, data);

            // moyenne
            double moy = df.calculerMoyenne(0);
            tot++; if (Math.abs(moy - 30.0) < 1e-9) { ok++; System.out.println("PASS moyenne = 30.0"); } else System.out.println("FAIL moyenne = " + moy);

            // médiane nb valeurs impair
            double med = df.calculerMediane(0);
            tot++; if (Math.abs(med - 30.0) < 1e-9) { ok++; System.out.println("PASS médiane impaire = 30.0"); } else System.out.println("FAIL médiane = " + med);

            // médiane nb valeurs pair
            Object[][] dataPair = {{10.0}, {20.0}, {30.0}, {40.0}};
            DfIndividu dfPair = new DfIndividu(4, new String[]{"v"}, dataPair);
            double medPair = dfPair.calculerMediane(0);
            tot++; if (Math.abs(medPair - 25.0) < 1e-9) { ok++; System.out.println("PASS médiane paire = 25.0"); } else System.out.println("FAIL médiane paire = " + medPair);

            // écart-type
            double ecart = df.calculerEcartType(0);
            double expectedEcart = Math.sqrt(200.0);
            tot++; if (Math.abs(ecart - expectedEcart) < 1e-6) { ok++; System.out.println("PASS écart-type = √200 ≈ 14.14"); } else System.out.println("FAIL écart-type = " + ecart);

            // écart-type colonne constante = 0
            Object[][] dataConst = {{5.0}, {5.0}, {5.0}};
            DfIndividu dfConst = new DfIndividu(3, new String[]{"c"}, dataConst);
            double ecartConst = dfConst.calculerEcartType(0);
            tot++; if (Math.abs(ecartConst) < 1e-9) { ok++; System.out.println("PASS écart-type colonne constante = 0"); } else System.out.println("FAIL écart-type constante = " + ecartConst);

            // variance
            double var = df.calculerVariance(0);
            tot++; if (Math.abs(var - 200.0) < 1e-9) { ok++; System.out.println("PASS variance = 200.0"); } else System.out.println("FAIL variance = " + var);

            // covariance positive
            double cov = df.calculerCoVariance(0, 1);
            tot++; if (cov > 0) { ok++; System.out.println("PASS covariance > 0"); } else System.out.println("FAIL covariance = " + cov);

            // corrélation
            double corr = df.calculerCorrelation(0, 1);
            tot++; if (corr > 0.95 && corr <= 1.0) { ok++; System.out.println("PASS corrélation = " + String.format("%.4f", corr)); } else System.out.println("FAIL corrélation = " + corr);

            // corrélation colonne constante = 0
            Object[][] dataConst2 = {{10.0, 5.0}, {20.0, 5.0}, {30.0, 5.0}};
            DfIndividu dfConst2 = new DfIndividu(3, new String[]{"a", "b"}, dataConst2);
            double corrConst = dfConst2.calculerCorrelation(0, 1);
            tot++; if (Math.abs(corrConst) < 1e-9) { ok++; System.out.println("PASS corrélation colonne constante = 0"); } else System.out.println("FAIL corrélation constante = " + corrConst);

            // colonne String → moyenne = 0
            Object[][] dataString = {{"A"}, {"B"}, {"C"}};
            DfIndividu dfStr = new DfIndividu(3, new String[]{"s"}, dataString);
            double moyStr = dfStr.calculerMoyenne(0);
            tot++; if (Math.abs(moyStr) < 1e-9) { ok++; System.out.println("PASS moyenne colonne String = 0"); } else System.out.println("FAIL moyenne String = " + moyStr);

            // exceptions
            try {
                df.calculerMoyenne(99);
                System.out.println("FAIL calculerMoyenne index invalide");
            } catch (OutOfBoundException e) { tot++; ok++; System.out.println("PASS calculerMoyenne index invalide → exception"); }

            // statistiques stockées
            tot++; if (!df.getStatistique().isEmpty()) { ok++; System.out.println("PASS statistiques stockées dans HashMap"); } else System.out.println("FAIL statistiques stockées");

        } catch (Exception e) {
            System.out.println("FAIL général : " + e);
        }

        System.out.println("\n=== DataframeStatistiques : " + ok + "/" + tot + " ===");
    }
}

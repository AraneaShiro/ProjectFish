package fish.nettoyage;

import fish.acquisition.DfPopulation;
import fish.exceptions.NullParameterException;
import fish.exceptions.OutOfBoundException;

/**
 * Nettoyage spécifique à un DfPopulation.
 * La colonne Anisakis peut être "prévalence", "abondance" ou "intensité"
 * selon le format (standard ou multi-période).
 *
 * @author Jules Grenesche
 * @version 0.1
 */
public class NettoyageDfPopulation extends NettoyageDataframe {

    private DfPopulation dfPopulation;

    public NettoyageDfPopulation(DfPopulation df) {
        super(df);
        this.dfPopulation = df;
    }

    public NettoyageDfPopulation(DfPopulation df, double seuilNull) {
        super(df, seuilNull);
        this.dfPopulation = df;
    }

    @Override
    protected int getIndexColonneAnisakis() {
        // Format multi-période : prévalence totale
        if (dfPopulation.isFormatMultiPeriode()) {
            int col = getIndexColonne("prévalence");
            if (col < 0)
                col = getIndexColonne("prevalence");
            if (col < 0)
                col = getIndexColonne("abondance");
            return col;
        }
        // Format standard : infectés ou abondance
        int col = getIndexColonne("infect");
        if (col < 0)
            col = getIndexColonne("abondance");
        return col;
    }

    @Override
    protected void reconstruireDataframe(Object[][] nouveauTableau, int nbLignes) {
        try {
            this.dfPopulation = new DfPopulation(nbLignes, df.getNomColonnes(), nouveauTableau);
            this.df = this.dfPopulation;
        } catch (OutOfBoundException | NullParameterException e) {
            System.out.println("Erreur reconstruction DfPopulation : " + e.getMessage());
        }
    }

    @Override
    protected void reconstruireDataframeAvecNoms(Object[][] tab, String[] noms, int nbLignes) {
        try {
            this.dfPopulation = new DfPopulation(nbLignes, noms, tab);
            this.df = this.dfPopulation;
        } catch (OutOfBoundException | NullParameterException e) {
            System.out.println("Erreur reconstruction DfPopulation : " + e.getMessage());
        }
    }

    public DfPopulation getDfPopulation() {
        return dfPopulation;
    }
}
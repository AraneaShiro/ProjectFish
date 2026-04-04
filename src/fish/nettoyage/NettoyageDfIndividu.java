package fish.nettoyage;

import fish.acquisition.DfIndividu;
import fish.exceptions.NullParameterException;
import fish.exceptions.OutOfBoundException;

/**
 * Nettoyage spécifique à un DfIndividu.
 * La colonne Anisakis est "nbvers" ou "taux" selon ce qui est disponible.
 *
 * @author Jules Grenesche
 * @version 0.1
 */
public class NettoyageDfIndividu extends NettoyageDataframe {

    private DfIndividu dfIndividu;

    public NettoyageDfIndividu(DfIndividu df) {
        super(df);
        this.dfIndividu = df;
    }

    public NettoyageDfIndividu(DfIndividu df, double seuilNull) {
        super(df, seuilNull);
        this.dfIndividu = df;
    }

    @Override
    protected int getIndexColonneAnisakis() {
        // Priorité : nbvers → taux
        int col = getIndexColonne("nbvers");
        if (col < 0)
            col = getIndexColonne("taux");
        return col;
    }

    @Override
    protected void reconstruireDataframe(Object[][] nouveauTableau, int nbLignes) {
        try {
            this.dfIndividu = new DfIndividu(nbLignes, df.getNomColonnes(), nouveauTableau);
            this.df = this.dfIndividu;
        } catch (OutOfBoundException | NullParameterException e) {
            System.out.println("Erreur reconstruction DfIndividu : " + e.getMessage());
        }
    }

    @Override
    protected void reconstruireDataframeAvecNoms(Object[][] tab, String[] noms, int nbLignes) {
        try {
            this.dfIndividu = new DfIndividu(nbLignes, noms, tab);
            this.df = this.dfIndividu;
        } catch (OutOfBoundException | NullParameterException e) {
            System.out.println("Erreur reconstruction DfIndividu : " + e.getMessage());
        }
    }

    public DfIndividu getDfIndividu() {
        return dfIndividu;
    }
}
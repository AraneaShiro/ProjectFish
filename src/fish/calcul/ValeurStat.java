package fish.calcul;

/**
 * Classe représentant les différentes valeurs stastiques calculés
 *
 * @author Jules Grenesche
 * @version 1
 *          Obselete
 */
public class ValeurStat {

    ////////////////////////////// Attributs ////////////////////

    /** Le nom de la statistique exemple : moyenne poids */
    private String NomStat;

    /** La valeur de cette attribut */
    private float value;

    ////////////////////////////// Get/Setter ////////////////////

    /**
     * Donne le nom de la stat
     * 
     * @return String du nom
     */
    public String GetNom() {
        return this.NomStat;
    }

    /**
     * Donne la valeur de la stat
     * 
     * @return float de la valeur
     */
    public float GetValue() {
        return this.value;
    }

    /**
     * Met le nom de la stat
     * 
     * @param nom le nom de la stat
     */
    private void setNom(String nom) {
        this.NomStat = nom;
    }

    /**
     * Met la valeur de la stat
     * 
     * @param valeur la valeur
     */
    private void setValue(float valeur) {
        this.value = valeur;
    }

    public ValeurStat(String nom, float valeur) {
        setNom(nom);
        setValue(valeur);
    }

}

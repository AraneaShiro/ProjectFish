package fish.poisson;

/**
 * Classe representant les possibles contenus d'un poisson
 * (ex:Un organe) et le nombre de vers ou son taux d'infestation
 * 
 * @author Jules Grenesche
 * @version 0.1
 */
public class Contenu {

    //////////////////////////////Attribut////////////////////
    /**
     * Nom du contenu
     */
    private String type;

    /**
     * Nombre de vers
     */
    private int nbVers;

    /**
     * Taux d'infestation
     */
    private float tauxInfestation;

    //////////////////////////////Constructeur////////////////////
    /**
     * Constructeur avec nombre de vers
     * 
     * @param type   String du type du contenu
     * @param nbVers Int du nombre de vers dans le contenu
     */
    public Contenu(String type, int nbVers) {

        this.type = type;
        this.nbVers = nbVers;
    }

    /**
     * Constructeur avec le taux d'infestation
     * 
     * @param type String du type du contenu
     * @param taux Float du taux d'infestation dans le contenu
     */
    public Contenu(String type, float taux) {

        this.type = type;
        this.tauxInfestation = taux;
    }

    //////////////////////////////Getter////////////////////
    /**
     * Methode Get sur le type
     * 
     * @return Renvoie le type du contenu
     */
    public String getType() {
        return type;
    }

    /**
     * Methode Get sur le nombre de vers
     * 
     * @return Renvoie le nombre de vers du contenu
     */
    public int getnbVers() {
        return nbVers;
    }

    /**
     * Methode Get sur le taux d'infestation du contenu
     * 
     * @return Renvoie le taux d'infestation
     */
    public float getTaux() {
        return tauxInfestation;
    }

    //////////////////////////////Setter////////////////////
    /**
     * Methode pour renseigner le type du contenu
     * 
     * @param type String du type de contenu
     */
    private void setType(String type) {
        this.type = type;
    }

    /**
     * Methode pour renseigner le nombre de vers dans le contenu
     * 
     * @param nbVers Int du nombre de vers que l'on veut renseigner dans le contenu
     */
    private void setNbVers(int nbVers) {
        this.nbVers = nbVers;
    }

    /**
     * Methode pour renseigner le taux d'infestation dans le contenu
     * 
     * @param taux Float du pourcentage que l'on veut renseigner dans le contenu
     */
    private void setNbVers(float taux) {
        this.tauxInfestation = taux;
    }

    //////////////////////////////Setter////////////////////
}

package fish.acquisition;

/**
 * Interface de méthode utilitaire de lecture
 * 
 * @author Jules Grenesche
 * @version 1
 */
public interface Utilitaire {

    /**
     * Fonction utilitaire qui lis si c est possible en Float
     * et le renvoie ou une valeur par defaut
     *
     * @param ligne  la ligne de la case a lire
     * @param col    la colonne de la case a lire
     * @param defaut la valeur par defaut si illisible ou mauvaise coordonné
     * @return la valeur ou la valeur par defaut
     */
    public int lireInt(int ligne, int col, int defaut);

    /**
     * Fonction utilitaire qui lis si c est possible en int
     * et le renvoie ou une valeur par defaut
     *
     * @param ligne  la ligne de la case a lire
     * @param col    la colonne de la case a lire
     * @param defaut la valeur par defaut si illisible ou mauvaise coordonné
     * @return la valeur ou la valeur par defaut
     */
    public float lireFloat(int ligne, int col, float defaut);

    /**
     * Fonction utilitaire qui lis si c est possible en String
     * et le renvoie ou une valeur par defaut
     *
     * @param ligne la ligne de la case a lire
     * @param col   la colonne de la case a lire
     * @return la valeur ou un string vide
     */
    public String lireString(int ligne, int col);
}

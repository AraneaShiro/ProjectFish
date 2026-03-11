
package fish.poisson;

/**
 * Representation d'un poisson
 * @author Jules Grenesche
 * @version 0.1
 * A METTRE LE SEE !!!!!!!!
 * 
 */
public class Individu {
//////////////////////////////Attribut////////////////////
	/**
	 * Nom de l'espece
	 */
	 private Espece espece;
	/**
	 * Taille du poisson
	 */
	private float longueur;
	/**
	 * Poids du poisson
	 */
	private float poids;
	/**
	 * Represente si le poisson est infesté.
	 */
	private boolean infested;
	/**
	 * Nombre total de vers Anisakis dans le poisson
	 */
	private int nbTotalVers;
	/**
	 * Taux de l'infestation du poisson
	 */
	private float tauxInfestation;
	
	/**
	 * Collection des differents contenue du poisson étudié.
	 */
	private HashMap<Contenue> contenuePoisson = new HashMap<>();
	
	
//////////////////////////////Getter Setter////////////////////
	
	
	
//////////////////////////////Constructor////////////////////

//////////////////////////////Methodes////////////////////
	/**
	 * @param args
	 */
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}

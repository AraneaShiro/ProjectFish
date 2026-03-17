
package fish.poisson;

import java.lang.reflect.Array;
import java.util.ArrayList;

import fish.poisson.Contenu;

/**
 * Representation d'un poisson
 * 
 * @author Jules Grenesche
 * @version 0.1
 * @see Contenu
 * 
 */
public class Individu {
	//////////////////////////////Attribut////////////////////
	/**
	 * Nom de l'espece
	 */
	private String espece;
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
	 * Liste des differents contenus du poisson étudié.
	 */
	private ArrayList<Contenu> contenuPoisson = new ArrayList<Contenu>();

	////////////////////////////// Getter Setter/////////////////*
	/**
	 * Methode Get sur l'espece de l'individu
	 * 
	 * @return Renvoie l'espece de l'individu
	 */
	public String getEspece() {
		return espece;
	}

	/**
	 * Methode Get sur la longueur de l'individu
	 * 
	 * @return Renvoie la longueur de l'individu
	 */
	public float getLongueur() {
		return longueur;
	}

	/**
	 * Methode Get sur le poids de l'individu
	 * 
	 * @return Renvoie le poids de l'individu
	 */
	public float getPoids() {
		return poids;
	}

	/**
	 * Methode pour savoir si l'individu est infecté
	 * 
	 * @return Renvoie si l'individu est infecté
	 */
	public boolean isInfected() {
		return infested;
	}

	/**
	 * Methode Get sur le nombre de vers dans l'individu
	 * 
	 * @return Renvoie le nombre de vers dans l'individu
	 */
	public int getNbTotalVers() {
		return nbTotalVers;
	}

	/**
	 * Methode Get sur le taux d'infestation de l'individu
	 * 
	 * @return Renvoie le taux d'infestation
	 */
	public float getTauxInfestation() {
		return tauxInfestation;
	}

	/**
	 * Methode Get sur les contenus de l'individu
	 * 
	 * @return Renvoie un tableau des contenus
	 */
	public ArrayList<Contenu> getContenus() {
		return contenuPoisson;
	}

	/**
	 * Methode pour renseigner l'espece de l'individu
	 * 
	 * @param espece String de l'espece
	 */
	private void setEspece(String espece) {
		this.espece = espece;
	}

	/**
	 * Methode pour renseigner la longueur de l'individu
	 * 
	 * @param longueur Float de la longueur de l'individu
	 */
	private void setLongueur(float longueur) {
		this.longueur = longueur;
	}

	/**
	 * Methode pour renseigner le poids de l'individu
	 * 
	 * @param poids Float de le poids de l'individu
	 */
	private void setPoids(float poids) {
		this.poids = poids;
	}

	/**
	 * Methode pour renseigner le status d'infestation de l'individu
	 * 
	 * @param isInfested Boolean sur le status d'infestation voulu, si true = est
	 *                   infecté
	 */
	private void setInfested(Boolean isInfested) {
		this.infested = isInfested;
	}

	/**
	 * Methode pour renseigner le nombre de vers
	 * 
	 * @param nbVers int sur le nombre de vers total dans l'individu
	 */
	private void setNbTotalVers(int nbVers) {
		this.nbTotalVers = nbVers;
	}

	/**
	 * Methode pour renseigner le taux d'infestation de l'individu
	 * 
	 * @param taux float sur le taux d'infestation
	 */
	private void setTauxInfestation(float taux) {
		this.tauxInfestation = taux;
	}

	/**
	 * Methode pour renseigner les contenus de l'individu
	 * 
	 * @param contenus tableau de contenu
	 */
	private void setContenus(ArrayList<Contenu> contenus) {
		this.contenuPoisson = contenus;
	}

	//////////////////////////////Constructor////////////////////

	/**
	 * Constructeur avec le nombre de vers
	 * 
	 * @param espece   l'espece de l'individu
	 * @param taille   la taille de l'individu
	 * @param poids    le poids de l'individu
	 * @param nbVers   le nombre de vers dans l'individu
	 * @param contenus les contenus de l'individu
	 */
	public Individu(String espece, float taille, float poids, int nbVers, ArrayList<Contenu> contenus) {
		setEspece(espece);
		setContenus(contenus);
		setLongueur(taille);
		setPoids(poids);
		setNbTotalVers(nbVers);
		if (nbVers > 0) {
			setInfested(true);
		} else {
			setInfested(false);
		}
	}

	/**
	 * Constructeur avec le taux
	 * 
	 * @param espece   l'espece de l'individu
	 * @param taille   la taille de l'individu
	 * @param poids    le poids de l'individu
	 * @param taux     le taux d'infestation de l'individu
	 * @param contenus les contenus de l'individu
	 */
	public Individu(String espece, float taille, float poids, float taux, ArrayList<Contenu> contenus) {
		setEspece(espece);
		setContenus(contenus);
		setLongueur(taille);
		setPoids(poids);
		setTauxInfestation(taux);
		if (taux != 0) {
			setInfested(true);
		} else {
			setInfested(false);
		}
	}

	//////////////////////////////Methodes////////////////////
	/**
	 * @param args
	 */

	public static void main(String[] args) {

	}

}

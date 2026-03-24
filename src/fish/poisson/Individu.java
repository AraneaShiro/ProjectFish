
package fish.poisson;

import java.util.ArrayList;

import fish.exceptions.*;
import fish.poisson.Contenu;
import java.math.*;;

/**
 * Representation d'un poisson
 * 
 * @author Jules Grenesche / Arthur Bernard
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
	 * @return Renvoie le nom d'espece de l'individu
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
	 * @throws EmptyStringException si le string entré en paramètre est vide
	 */
	private void setEspece(String espece) throws EmptyStringException{
		if(espece == ""){
			throw new EmptyStringException();
		}
		this.espece = espece;
	}

	/**
	 * Methode pour renseigner la longueur de l'individu
	 * 
	 * @param longueur Float de la longueur de l'individu
	 * @throws NegativeValueException si la valeur entree en parametre est négative
	 */
	private void setLongueur(float longueur) throws NegativeValueException{
		if(longueur <0){
			throw new NegativeValueException();
		}else{
			this.longueur = longueur;
		}
	}

	/**
	 * Methode pour renseigner le poids de l'individu
	 * 
	 * @param poids Float de le poids de l'individu
	 * @throws NegativeValueException si la valeur entree en parametre est négative
	 */
	private void setPoids(float poids) throws NegativeValueException{
		if(poids <0){
			throw new NegativeValueException();
		} else{
			this.poids = poids;
		}
		
	}

	/**
	 * Methode pour renseigner le statut d'infestation de l'individu
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
	 * @throws NegativeValueException si la valeur entree en parametre est négative (-1 = nombre de vers non indiqué)
	 */
	private void setNbTotalVers(int nbVers) throws NegativeValueException{
		if(nbVers <-1){
			throw new NegativeValueException();
		}else{
			this.nbTotalVers = nbVers;
		}	
	}

	/**
	 * Methode pour renseigner le taux d'infestation de l'individu
	 * 
	 * @param taux float sur le taux d'infestation
	 * @throws NegativeValueException si la valeur entree en parametre est négative
	 */
	private void setTauxInfestation(float taux) throws NegativeValueException, TauxValueException{
		if(taux<0){
			throw new NegativeValueException();
		}else if (taux >1){
			throw new TauxValueException();
		}else{
			this.tauxInfestation = taux;
		}
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
	 * @throws NegativeValueException si au moins l'une des valeurs de taille, poids ou nbVers est négative
	 * @throws EmptyStringException si le string entré en paramètre est vide
	 */
	public Individu(String espece, float taille, float poids, int nbVers, ArrayList<Contenu> contenus) throws NegativeValueException, EmptyStringException{
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
	 * @throws NegativeValueException si au moins l'une des valeurs de taille, poids ou taux est négative
	 * @throws NoNbVersException si le nombre de vers des contenus est inconnu
	 * @throws IndiceException si l'indice d'un des contenus dans l'arraylist est out of range
	 * @throws EmptyStringException si le string entré en paramètre est vide
	 * @throws TauxValueException si le taux entré en paramètre est supérieur à 1
	 */
	public Individu(String espece, float taille, float poids, float taux, ArrayList<Contenu> contenus) throws NegativeValueException, NoNbVersException,IndiceException,TauxValueException, EmptyStringException{
		setEspece(espece);
		setContenus(contenus);
		setLongueur(taille);
		setPoids(poids);
		setTauxInfestation(taux);
		setNbTotalVers(-1);
		MajNBVersPoisson();
		if (taux != 0) {
			setInfested(true);
		} else {
			setInfested(false);
		}
	}

	//////////////////////////////Methodes////////////////////
	/**
	 * Methode qui renvoie le nombre de vers du contenu correspondant à l'indice dans l'arraylist
	 * @param indice int indice du contenu dans l'arraylist
	 * @return Renvoie le nombre de vers du contenu ou -1 si nombre non renseigné ou indice out of range
	 * @throws NoNbVersException quand le nombre de vers du contenu est non renseigné
	 * @throws IndiceException quand l'indice entré en paramètre est out of range
	 */
	public int getVersContenu(int indice) throws NoNbVersException, IndiceException{
		if(indice>this.contenuPoisson.size() || this.contenuPoisson.get(indice).getNbVers() == -1){//Si indice out of range ou 
			if(this.contenuPoisson.get(indice).getNbVers() == -1){									//NbVers non renseigné 
				throw new NoNbVersException(); 					   
			}
			else{
				throw new IndiceException();
			}
		}else{
			return this.contenuPoisson.get(indice).getNbVers();
		}
	}
	/**
	 * Methode qui renvoie le nombre de vers total des contenus étudiés
	 * @return Renvoie le nombre de vers total des contenus
	 * @throws NoNbVersException quand le nombre de vers du contenu est non renseigné
	 * @throws IndiceException quand l'indice entré en paramètre est out of range
	 */

	public int getVersContenuTotal() throws NoNbVersException, IndiceException{
		int somme = 0;
		for(int i=0;i<this.contenuPoisson.size();i++){
			somme += getVersContenu(i);
		}
		return somme;
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
	 * Methode qui met à jour le nombre total de vers du poisson avec celui total de l'ensemble des contenus
	 * @return true si le nombre a été mis à jour, false sinon
	 * @throws NoNbVersException si le nombre de vers des contenus est non renseigné
	 * @throws IndiceException si l'indice d'un contenu est out of range
	 */

	public boolean MajNBVersPoisson() throws NoNbVersException, IndiceException{
		if(getVersContenuTotal() !=this.nbTotalVers){
			this.nbTotalVers = getVersContenuTotal();
			return true;
		}else{
			return false;
		}
	}

	/**
	 * Méthode qui calcule et renvoie le taux d'infestation (calcul : NbVers/longueur de l'individu)
	 * @return taux un float correspondant au taux d'infestation calculé
	 * @throws NoNbVersException si le nombre de vers de l'individu est non renseigné
	 */

	public float calculTauxInfestation() throws NoNbVersException{
		float taux = 0;
		if(this.getNbTotalVers() == -1){
			throw new NoNbVersException();
		} else{
			taux =this.getNbTotalVers()/this.getLongueur();
		}
		return taux;
	}

	/**
	 * Méthode qui calcule et renvoie le nombre de vers (calcul : Taux d'infestation * longueur de l'individu)
	 * @return NbVers correspondant au nombre de vers calculé
	 */

	public int calculNbVers() {
		int NbVers =(int) Math.round(this.getTauxInfestation()*this.getLongueur());
		return NbVers;
	}
	///////////////////// main /////////////////
	public static void main(String[] args) {
		ArrayList<Contenu> content = new ArrayList<Contenu>();
		try{
			Individu i= new Individu("fish", 14.88f, 150.9f, 14, content);
			Contenu c1 = new Contenu("gonade", 14);
			Contenu c2 = new Contenu("head", 0.55f);
			i.contenuPoisson.add(c1);
			i.contenuPoisson.add(c2);
			System.out.println("Nombre total de vers des contenus");
			System.out.println(i.getNbTotalVers());
			System.out.println(i.calculTauxInfestation());
		} catch(Exception e){
			System.out.println(e);
		}
	}

}

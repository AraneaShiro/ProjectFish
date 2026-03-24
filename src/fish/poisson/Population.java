package fish.poisson;

import fish.exceptions.*;
import fish.poisson.*;

/**
 * Classe representant un ensemble de poisson
 * 
 * @author Jules Grenesche
 * @version 0.1
 * @see Individu
 */
public class Population {

	/**
	 * Nombre d'individu dans la population
	 */
	private int effectif;

	/**
	 * Espece de la population
	 */
	private String espece;
	/**
	 * Tableau des differents poissons si possible
	 */
	private Individu[] tabIndividu;
	/**
	 * Nombre de poissons infectés dans l'effectif
	 */
	private int nbInfectes;
	/**
	 * Partie du corps étudiée
	 */
	private String partieDuCorps;

	/**
	 * Abondance : Nombre de vers moyen par poisson de la population
	 */
	private float abondance;

	/**
	 * Intensité : Abondance uniquement sur les poissons infectés
	 */
	private float intensite;

	/**
	 * Taux d'infestation dans la population
	 */
	private float tauxInfestation;

	
	///////////// Getter Setter /////////////
	/**
	 * Setter de l'effectif de la population
	 * @param effectif l'effectif de la population
	 * @throws NegativeValueException si la valeur entrée en parametre est négative
	 */
	private void setEffectif(int effectif) throws NegativeValueException{
		if(effectif <0){
			throw new NegativeValueException();
		}else{
			this.effectif = effectif;
		}
	}

	/**
	 * Getter de l'effectif de la population
	 * @return l'effectif de la population
	 */
	public int getEffectif(){
		return this.effectif;
	}

	/**
	 * Setter nom espece de la population
	 * @param espece le nom de l'espece de la population
	 * @throws EmptyStringException si le string entré en parametre est vide
	 */
	private void setEspece(String espece) throws EmptyStringException{
		if(espece == ""){
			throw new EmptyStringException();
		}
		this.espece = espece;
	}

	/**
	 * Getter nom d'espece de la population
	 * @return le nom d'espece de la population
	 */
	public String getEspece(){
		return this.espece;
	}


	/**
	 * Setter tableau d'individus de la population
	 * @param tabIndividu le tableau d'individus à ajouter à la population
	 */
	private void setTabIndividu(Individu[] tabIndividu){
		this.tabIndividu = tabIndividu;
	}

	/**
	 * Getter tableau d'individus de la population
	 * @return le tableau d'individus de la population
	 */
	public Individu[] getTabIndividu(){
		return this.tabIndividu;
	}

	/**
	 * setter nombre d'infectes
	 * @param nbInfectes le nombre d'infectes de la population
	 * @throws NegativeValueException si le nombre entré en paramètre est négatif
	 */	
	private void setNbInfectes(int nbInfectes) throws NegativeValueException{
		if(nbInfectes <0){
			throw new NegativeValueException();
		}else{
			this.nbInfectes = nbInfectes;
		}
	}

	/**
	 * Getter nombre d'infectes de la population
	 * @return le nombre d'infectes de la population
	 */
	public int getNbInfectes(){
		return this.nbInfectes;
	}


	/**
	 * Setter abondance dans la population
	 * @param abondance le nombre de vers moyen par poisson de la population
	 * @throws NegativeValueException si l'abondance entrée en paramètre est négative
	 */
	private void setAbondance(float abondance) throws NegativeValueException{
		if(abondance <0){
			throw new NegativeValueException();
		}else{
			this.abondance = abondance;
		}
	}

	/**
	 * Getter abondance dans la population
	 * @return l'abondance dans la population
	 */
	public float getAbondance(){
		return this.abondance;
	}

	/**
	 * Setter intensité dans la population
	 * @param intensite
	 * @throws NegativeValueException si l'intensité entrée en paramètre est négative
	 */
	private void setIntensite(float intensite) throws NegativeValueException{
		if(intensite<0){
			throw new NegativeValueException();
		} else{
			this.intensite = intensite;
		}
	}

	/**
	 * Getter intensité dans la population
	 * @return l'intensité dans la population
	 */
	public float getIntensite(){
		return this.intensite;
	}

	/**
	 * setter partie du corps
	 * @param partieCorps la partie du corps étudiée dans la population
	 * @throws EmptyStringException si le string entré en paramètre est vide
	 */	
	private void setPartieCorps(String partieCorps) throws EmptyStringException{
		if(partieCorps == ""){
			throw new EmptyStringException();
		}
		this.partieDuCorps = partieCorps;
	}

	/**
	 * Getter partie du corps
	 * @return la partie du corps étudiée dans la population
	 */
	public String getPartieCorps(){
		return this.partieDuCorps;
	}

	/**
	 * Setter taux d'infestation
	 * @param taux le taux d'infestation dans la population
	 * @throws NegativeValueException si le taux entré en paramètre est négatif
	 * @throws TauxValueException si le taux entré en paramètre est supérieur à 1
	 */
	private void setTauxInfestation(float taux) throws NegativeValueException, TauxValueException{
		if(taux <0){
			throw new NegativeValueException();
		} else if(taux>1){
			throw new TauxValueException();
		} else{
			this.tauxInfestation = taux;
		}
	}

	/**
	 * Getter Taux d'infestation
	 * @return le taux d'infestation de la population
	 */
	public float getTauxInfestation(){
		return this.tauxInfestation;
	}

	///////////// Constructeur /////////////
	/**
	 * Constructeur sans tableau d'individus
	 * @param effectif l'effectif de la population
	 * @param espece le nom de l'espece de poisson
	 * @param nbInfectes le nombre d'infectés de la population
	 * @param partieCorps le nom de la partie du corps étudiée dans la population
	 * @param abondance l'abondance (nombre de vers moyens par poisson)dans la population
	 * @param intensite l'intensité (abondance uniquement sur les infectés) dans la population
	 * @throws NegativeValueException si l'une des valeurs entrées en paramètres est négative
	 * @throws EmptyStringException si l'un des strings passés en paramètres est vide
	 */
	public Population(int effectif, String espece, int nbInfectes, String partieCorps, float intensite, float abondance) throws NegativeValueException, EmptyStringException{
		setEffectif(effectif);
		setEspece(espece);
		setNbInfectes(nbInfectes);
		setPartieCorps(partieCorps);
		setAbondance(abondance);
		setIntensite(intensite);
		this.tabIndividu = null;
	}
	

	/**
	 * Constructeur avec tableau d'individus
	 * @param effectif l'effectif de la population
	 * @param espece le nom de l'espece de poisson
	 * @param partieCorps le nom de la partie du corps étudiée dans la population
	 * @param tabIndividu le tableau d'individus de la population
	 * @throws NegativeValueException si l'une des valeurs entrées en paramètres est négative
	 * @throws EmptyStringException si l'un des strings passés en paramètres est vide
	 * @throws TauxValueException si le taux calculé à partir du tableau est supérieur à 1
	 */
	public Population(int effectif, String espece, String partieCorps, Individu[] tabIndividu) throws NegativeValueException, EmptyStringException, TauxValueException{
		setTabIndividu(tabIndividu);
		setEffectif(effectif);
		setEspece(espece);
		setPartieCorps(partieCorps);
		setNbInfectes(calculNbInfectes(tabIndividu));
		setTauxInfestation(calculTauxInfestation(tabIndividu));
	}

	/////////////////// Méthodes ////////////////////////

	/**
	 * methode pour vérifier la présence ou non du tableau d'individus
	 * @return true si le tableau d'individus est renseigné, false sinon
	 */
	public boolean aTableau(){
		if(this.getTabIndividu() !=null){
			return true;
		} else{
			return false;
		}
	}


	/**
	 * Methode qui calcule et renvoie le nombre total d'individus infectés à partir d'un tableau d'individus
	 * @param tabIndividus
	 * @return le nombre total d'infectés dans la population
	 */
	public int calculNbInfectes(Individu[] tabIndividus){
		int somme = 0;
		for (int i=0;i < tabIndividus.length;i++){
			if(tabIndividus[i].isInfected()){
				somme +=tabIndividus[i].getNbTotalVers();
			}
		}
		return somme;
	}

	/**
	 * Methode pour le calcul du taux d'infestation sans tableau d'individus
	 * @return le taux d'infestation calculé dans la population
	 */
	public float calculTauxInfestation() throws TauxValueException{
		float taux = this.getNbInfectes()/this.getEffectif();
		if(taux >1){
			throw new TauxValueException();
		}else{
			return taux;
		}		
	}

	/**
	 * Methode pour le calcul du taux d'infestation avec un tableau d'individus
	 * @param tabIndividus le tableau d'individus de la population
	 * @return le taux d'infestation calculé à partir du tableau
	 */
	public float calculTauxInfestation(Individu[] tabIndividus){
		float taux;
		int NbInfectes = calculNbInfectes(tabIndividus);
		taux = NbInfectes/this.getEffectif();
		return taux;
	}
	/////////////////// Main //////////////
	public static void main(String[] args) {
		//

	}

}

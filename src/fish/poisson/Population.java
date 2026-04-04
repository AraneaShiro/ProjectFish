package fish.poisson;

import fish.exceptions.*;

/**
 * Classe representant un ensemble de poisson
 * 
 * @author Jules Grenesche et Arthur Bernard
 * @version 1
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
	 * Tableau des partie du corps étudiées
	 */
	private String[] partieDuCorps;

	/**
	 * Abondance : Nombre de vers moyen par poisson de la population
	 */
	private double abondance;

	/**
	 * Intensité : Abondance uniquement sur les poissons infectés
	 */
	private double intensite;

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
	private void setAbondance(double abondance) throws NegativeValueException{
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
	public double getAbondance(){
		return this.abondance;
	}

	/**
	 * Setter intensité dans la population
	 * @param intensite
	 * @throws NegativeValueException si l'intensité entrée en paramètre est négative
	 */
	private void setIntensite(double intensite) throws NegativeValueException{
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
	public double getIntensite(){
		return this.intensite;
	}

	/**
	 * setter partie du corps
	 * @param partieCorps la partie du corps étudiée dans la population
	 */	
	private void setPartieCorps(String[] partieCorps){
		this.partieDuCorps = partieCorps;
	}

	/**
	 * Getter du tableau de parties du corps
	 * @return le tableau de parties du corps étudiée dans la population
	 */
	public String[] getPartieCorps(){
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
	public Population(int effectif, String espece, int nbInfectes, String[] partieCorps, double intensite, double abondance) throws NegativeValueException, EmptyStringException{
		try{
			setEffectif(effectif);
			setEspece(espece);
			setNbInfectes(nbInfectes);
			setPartieCorps(partieCorps);
			setAbondance(abondance);
			setIntensite(intensite);
			this.tabIndividu = null;
		} catch(Exception e){
			System.out.println(e);
		}
		
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
	 * @throws NoTabException si le tableau d'individus de la population n'est pas renseigné ou est null
	 */
	public Population(int effectif, String espece, String[] partieCorps, Individu[] tabIndividu) throws NegativeValueException, EmptyStringException, TauxValueException, NoTabException{
		try{
			setTabIndividu(tabIndividu);
			setEffectif(effectif);
			setEspece(espece);
			setPartieCorps(partieCorps);
			setNbInfectes(calculNbInfectes(tabIndividu));
			setTauxInfestation(calculTauxInfestationTab(tabIndividu));
			setAbondance(calculAbondance());
			setIntensite(calculIntensite());
		} catch(Exception e){
			System.out.println(e);
		}
		

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
				somme += 1;
			}
		}
		return somme;
	}

	/**
	 * Methode pour le calcul du taux d'infestation sans tableau d'individus
	 * @return le taux d'infestation calculé dans la population
	 * @throws TauxValueException si le taux calculé est supérieur à 1
	 * @throws NegativeValueException si le taux calculé est négatif
	 */
	public float calculTauxInfestation() throws TauxValueException, NegativeValueException{
		float taux = (float) this.getNbInfectes()/ this.getEffectif();
		if(taux >1){
			throw new TauxValueException();
		} else if(taux <0){
			throw new NegativeValueException();
		} else{
			return taux;
		}		
	}

	/**
	 * Methode pour le calcul du taux d'infestation avec un tableau d'individus
	 * @param tabIndividus le tableau d'individus de la population
	 * @return le taux d'infestation calculé à partir du tableau
	 */
	public float calculTauxInfestationTab(Individu[] tabIndividus){
		float taux;
		int NbInfectes = calculNbInfectes(tabIndividus);
		taux = (float)NbInfectes/this.getEffectif();
		return taux;
	}


	/**
	 * Méthode calcul d'abondance (nombre de vers moyen dans la population)
	 * @return l'abondance (float) dans la population
	 * @throws NoTabException si le tableau d'individus de la population n'est pas renseigné
	 */
	public float calculAbondance() throws NoTabException{
		int sommeTotalVers=0;
		if(this.aTableau()){
			for(int i=0; i<this.tabIndividu.length;i++){
				sommeTotalVers += this.tabIndividu[i].getNbTotalVers();
			}
		} else{
			throw new NoTabException();
		}
		float abondance =  (float)sommeTotalVers / this.tabIndividu.length;
		return abondance;
	}


	/**
	 * Méthode de calcul d'intensité (abondance pour les poissons infectés)
	 * @return l'intensité calculée
	 * @throws NoTabException si le tableau d'individus de la population n'est pas renseigné
	 */
	public float calculIntensite() throws NoTabException{
		int sommeTotalVers = 0;
		if(this.aTableau()){
			for(int i=0; i<this.tabIndividu.length;i++){
				sommeTotalVers += this.tabIndividu[i].getNbTotalVers();
			}
		} else{
			throw new NoTabException();
		}
		float intensite = (float)sommeTotalVers / this.nbInfectes;
		return intensite;
	}

	/////////////////// Main //////////////
	public static void main(String[] args) {
try {


    System.out.println("===== CREATION DES CONTENUS =====");

    Contenu c1 = new Contenu("foie", 2);
    Contenu c2 = new Contenu("gonades", 1);

    java.util.ArrayList<Contenu> contenus1 = new java.util.ArrayList<>();
    contenus1.add(c1);
    contenus1.add(c2);

    java.util.ArrayList<Contenu> contenus2 = new java.util.ArrayList<>();
    contenus2.add(new Contenu("foie", 0));
    contenus2.add(new Contenu("gonades", 0));

    System.out.println("===== CREATION DES INDIVIDUS =====");

    Individu i1 = new Individu("maquereau", 10, 5, 0.3f, contenus1); // infecté
    Individu i2 = new Individu("maquereau", 12, 6, 0, contenus2);   // non infecté
    Individu i3 = new Individu("maquereau", 8, 4, 0.5f, contenus1); // infecté

    Individu[] tab = {i1, i2, i3};

    String[] parties = {"foie", "gonades"};

    System.out.println("\n===== TEST CONSTRUCTEUR SANS TABLEAU =====");

    Population p1 = new Population(12, "maquereau", 6, parties, 0.67, 0.5);

    System.out.println("Effectif : " + p1.getEffectif());
    System.out.println("Espèce : " + p1.getEspece());
    System.out.println("Nb infectés : " + p1.getNbInfectes());
    System.out.println("Abondance : " + p1.getAbondance());
    System.out.println("Intensité : " + p1.getIntensite());
    System.out.println("aTableau ? " + p1.aTableau());

    System.out.println("\n===== TEST calculTauxInfestation =====");
    try {
        System.out.println("Taux : " + p1.calculTauxInfestation());
    } catch (Exception e) {
        System.out.println("Erreur : " + e);
    }

    System.out.println("\n===== TEST CONSTRUCTEUR AVEC TABLEAU =====");

    Population p2 = new Population(3, "maquereau", parties, tab);

    System.out.println("aTableau ? " + p2.aTableau());
    System.out.println("Nb infectés : " + p2.getNbInfectes());
    System.out.println("Taux infestation : " + p2.getTauxInfestation());

    System.out.println("\n===== TEST calculNbInfectes =====");
    System.out.println("Nb infectés (manuel) : " + p2.calculNbInfectes(tab));

    System.out.println("\n===== TEST calculTauxInfestationTab =====");
    System.out.println("Taux (tab) : " + p2.calculTauxInfestationTab(tab));

    System.out.println("\n===== TEST calculAbondance =====");
    try {
        System.out.println("Abondance : " + p2.calculAbondance());
    } catch (Exception e) {
        System.out.println("Erreur : " + e);
    }

    System.out.println("\n===== TEST calculIntensite =====");
    try {
        System.out.println("Intensité : " + p2.calculIntensite());
    } catch (Exception e) {
        System.out.println("Erreur : " + e);
    }

    System.out.println("\n===== TEST CAS LIMITES =====");

    // Tableau null
    try {
        Population p3 = new Population(5, "thon", parties, null);
    } catch (Exception e) {
        System.out.println("Erreur tableau null : " + e);
    }

    // Effectif négatif
    try {
        Population p4 = new Population(-1, "thon", 2, parties, 0.2, 0.1);
    } catch (Exception e) {
        System.out.println("Erreur effectif négatif : " + e);
    }

    // String vide
    try {
        Population p5 = new Population(5, "", 2, parties, 0.2, 0.1);
    } catch (Exception e) {
        System.out.println("Erreur string vide : " + e);
    }

    // Division par 0 (intensité si 0 infecté)
    try {
        Individu i4 = new Individu("maquereau", 10, 5, 0, contenus2);
        Individu[] tab2 = {i4};
        Population p6 = new Population(1, "maquereau", parties, tab2);
        System.out.println("Intensité : " + p6.calculIntensite());
    } catch (Exception e) {
        System.out.println("Erreur intensité : " + e);
    }

} catch (Exception e) {
    System.out.println("Erreur générale : " + e);
}


}


}

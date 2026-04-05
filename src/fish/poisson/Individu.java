
package fish.poisson;

import java.util.ArrayList;

import fish.exceptions.*;
import fish.poisson.Contenu;

/**
 * Representation d'un poisson
 * 
 * @author Jules Grenesche / Arthur Bernard
 * @version 1
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
	private void setEspece(String espece) throws EmptyStringException {
		if (espece == "") {
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
	private void setLongueur(float longueur) throws NegativeValueException {
		if (longueur < 0) {
			throw new NegativeValueException();
		} else {
			this.longueur = longueur;
		}
	}

	/**
	 * Methode pour renseigner le poids de l'individu
	 * 
	 * @param poids Float de le poids de l'individu
	 * @throws NegativeValueException si la valeur entree en parametre est négative
	 */
	private void setPoids(float poids) throws NegativeValueException {
		if (poids < 0) {
			throw new NegativeValueException();
		} else {
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
	 * @throws NegativeValueException si la valeur entree en parametre est négative
	 *                                (-1 = nombre de vers non indiqué)
	 */
	private void setNbTotalVers(int nbVers) throws NegativeValueException {
		if (nbVers < -1) {
			throw new NegativeValueException();
		} else {
			this.nbTotalVers = nbVers;
		}
	}

	/**
	 * Methode pour renseigner le taux d'infestation de l'individu
	 * 
	 * @param taux float sur le taux d'infestation
	 * @throws NegativeValueException si la valeur entree en parametre est négative
	 */
	private void setTauxInfestation(float taux) throws NegativeValueException, TauxValueException {
		if (taux < 0) {
			throw new NegativeValueException();
		} else if (taux > 1) {
			throw new TauxValueException();
		} else {
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
	 * @throws NegativeValueException si au moins l'une des valeurs de taille, poids
	 *                                ou nbVers est négative
	 * @throws EmptyStringException   si le string entré en paramètre est vide
	 */
	public Individu(String espece, float taille, float poids, int nbVers, ArrayList<Contenu> contenus)
			throws NegativeValueException, EmptyStringException {
		try {
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
		} catch (Exception e) {
			System.out.println(e);
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
	 * @throws NegativeValueException si au moins l'une des valeurs de taille, poids
	 *                                ou taux est négative
	 * @throws NoNbVersException      si le nombre de vers des contenus est inconnu
	 * @throws IndiceException        si l'indice d'un des contenus dans l'arraylist
	 *                                est out of range
	 * @throws EmptyStringException   si le string entré en paramètre est vide
	 * @throws TauxValueException     si le taux entré en paramètre est supérieur à
	 *                                1
	 */
	public Individu(String espece, float taille, float poids, float taux, ArrayList<Contenu> contenus)
			throws NegativeValueException, NoNbVersException, IndiceException, TauxValueException,
			EmptyStringException {
		try {
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
		} catch (Exception e) {
			System.out.println(e);
		}

	}

	//////////////////////////////Methodes////////////////////
	/**
	 * Methode qui renvoie le nombre de vers du contenu correspondant à l'indice
	 * dans l'arraylist
	 * 
	 * @param indice int indice du contenu dans l'arraylist
	 * @return Renvoie le nombre de vers du contenu ou -1 si nombre non renseigné ou
	 *         indice out of range
	 * @throws NoNbVersException quand le nombre de vers du contenu est non
	 *                           renseigné
	 * @throws IndiceException   quand l'indice entré en paramètre est out of range
	 */
	public int getVersContenu(int indice) throws NoNbVersException, IndiceException {
		if (indice > this.contenuPoisson.size() || this.contenuPoisson.get(indice).getNbVers() == -1) {// Si indice out
																										// of range ou
			if (this.contenuPoisson.get(indice).getNbVers() == -1) { // NbVers non renseigné
				throw new NoNbVersException();
			} else {
				throw new IndiceException();
			}
		} else {
			return this.contenuPoisson.get(indice).getNbVers();
		}
	}

	/**
	 * Méthode renvoyant le nombre total de vers dans un type de contenu en
	 * particulier
	 * 
	 * @param type le type de contenu
	 * @return le nombre total de vers dans les contenus du type entré en paramètre
	 * @throws EmptyStringException si le string entré en paramètre est vide
	 */
	public int getVersTotalContenuType(String type) throws EmptyStringException {
		if (type == "") {
			throw new EmptyStringException();
		}

		int somme = 0;
		for (int i = 0; i < this.contenuPoisson.size(); i++) {
			if (this.contenuPoisson.get(i).getType() == type) {
				somme += this.contenuPoisson.get(i).getNbVers();
			}
		}
		return somme;
	}

	/**
	 * Méthode renvoyant le nombre de vers du premier contenu du type entré en
	 * paramètre
	 * 
	 * @param type le type du contenu d'intérêt
	 * @return le nombre de vers du premier contenu de ce type
	 * @throws EmptyStringException si le string entré en paramètre est vide
	 */
	public int getVersContenu(String type) throws EmptyStringException {
		if (type == "") {
			throw new EmptyStringException();
		}
		int nbVers = 0;
		for (int i = 0; i < this.contenuPoisson.size(); i++) {
			if (this.contenuPoisson.get(i).getType() == type) {
				nbVers = this.contenuPoisson.get(i).getNbVers();
				break;
			}
		}
		return nbVers;
	}

	/**
	 * Methode qui renvoie le nombre de vers total des contenus étudiés
	 * 
	 * @return Renvoie le nombre de vers total des contenus
	 * @throws NoNbVersException quand le nombre de vers du contenu est non
	 *                           renseigné
	 * @throws IndiceException   quand l'indice entré en paramètre est out of range
	 */

	public int getVersContenuTotal() throws NoNbVersException, IndiceException {
		int somme = 0;
		for (int i = 0; i < this.contenuPoisson.size(); i++) {
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
	 * Methode qui met à jour le nombre total de vers du poisson avec celui total de
	 * l'ensemble des contenus
	 * 
	 * @return true si le nombre a été mis à jour, false sinon
	 * @throws NoNbVersException si le nombre de vers des contenus est non renseigné
	 * @throws IndiceException   si l'indice d'un contenu est out of range
	 */

	public boolean MajNBVersPoisson() throws NoNbVersException, IndiceException {
		if (getVersContenuTotal() != this.nbTotalVers) {
			this.nbTotalVers = getVersContenuTotal();
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Méthode qui calcule et renvoie le taux d'infestation (calcul :
	 * NbVers/longueur de l'individu)
	 * 
	 * @return taux un float correspondant au taux d'infestation calculé
	 * @throws NoNbVersException si le nombre de vers de l'individu est non
	 *                           renseigné
	 */

	public float calculTauxInfestation() throws NoNbVersException {
		float taux = 0;
		if (this.getNbTotalVers() == -1) {
			throw new NoNbVersException();
		} else {
			taux = this.getNbTotalVers() / this.getLongueur();
		}
		return taux;
	}

	/**
	 * Méthode qui calcule et renvoie le nombre de vers (calcul : Taux d'infestation
	 * * longueur de l'individu)
	 * 
	 * @return NbVers correspondant au nombre de vers calculé
	 */

	public int calculNbVers() {
		int NbVers = (int) Math.round(this.getTauxInfestation() * this.getLongueur());
		return NbVers;
	}

	///////////////////// main /////////////////
	public static void main(String[] args) {


	try {


    // ===== Création des contenus =====
    Contenu contenuAvecVers1 = new Contenu("Coeur", 1);
    Contenu contenuAvecVers2 = new Contenu("Foie", 2);
    ArrayList<Contenu> contenuVers = new ArrayList<>();
    contenuVers.add(contenuAvecVers1);
    contenuVers.add(contenuAvecVers2);

    Contenu contenuSansVers1 = new Contenu("Coeur", 0);
    Contenu contenuSansVers2 = new Contenu("Foie", 0);
    ArrayList<Contenu> contenuSansVers = new ArrayList<>();
    contenuSansVers.add(contenuSansVers1);
    contenuSansVers.add(contenuSansVers2);

    ArrayList<Contenu> vide = new ArrayList<>();

    // ===== Constructeurs =====
    Individu fishVers = new Individu("Merlu", 10, 11, 0.3f, contenuVers);
    Individu fishSansVers = new Individu("Merlu", 10, 11, 5, contenuSansVers);

    System.out.println("===== TEST GETTERS =====");
    System.out.println("Espèce : " + fishVers.getEspece());
    System.out.println("Longueur : " + fishVers.getLongueur());
    System.out.println("Poids : " + fishVers.getPoids());
    System.out.println("Nb total vers : " + fishVers.getNbTotalVers());
    System.out.println("Taux infestation : " + fishVers.getTauxInfestation());

    System.out.println("\n===== TEST isInfected =====");
    System.out.println("fishVers infecté ? " + fishVers.isInfected());
    System.out.println("fishSansVers infecté ? " + fishSansVers.isInfected());

    System.out.println("\n===== TEST getVersContenu (indice) =====");
    System.out.println("Indice 1 : " + fishVers.getVersContenu(1));

    try {
        System.out.println("Indice invalide : " + fishVers.getVersContenu(10));
    } catch (Exception e) {
        System.out.println("Erreur attendue : " + e);
    }

    System.out.println("\n===== TEST getVersContenu (type) =====");
    System.out.println("Coeur : " + fishVers.getVersContenu("Coeur"));

    System.out.println("\n===== TEST getVersTotalContenuType =====");
    System.out.println("Total Coeur : " + fishVers.getVersTotalContenuType("Coeur"));

    System.out.println("\n===== TEST getVersContenuTotal =====");
    System.out.println("Total vers contenus : " + fishVers.getVersContenuTotal());

    System.out.println("\n===== TEST MajNBVersPoisson =====");
    System.out.println("Mise à jour ? " + fishVers.MajNBVersPoisson());
    System.out.println("Nb total vers après MAJ : " + fishVers.getNbTotalVers());

    System.out.println("\n===== TEST calculTauxInfestation =====");
    try {
        System.out.println("Taux calculé : " + fishSansVers.calculTauxInfestation());
    } catch (Exception e) {
        System.out.println("Erreur attendue : " + e);
    }

    System.out.println("\n===== TEST calculNbVers =====");
    System.out.println("Nb vers calculé : " + fishVers.calculNbVers());

    System.out.println("\n===== TEST cas limites =====");

    // String vide
    try {
        fishVers.getVersContenu("");
    } catch (Exception e) {
        System.out.println("Erreur string vide : " + e);
    }

    // Aucun contenu
    Individu fishVide = new Individu("Cabillaud", 5, 3, 0.0f, vide);
    try {
        System.out.println("Total vers (vide) : " + fishVide.getVersContenuTotal());
    } catch (Exception e) {
        System.out.println("Erreur attendue : " + e);
    }

    // Test taux > 1
    try {
        Individu erreurTaux = new Individu("Saumon", 10, 5, 2.0f, contenuVers);
    } catch (Exception e) {
        System.out.println("Erreur taux > 1 : " + e);
    }

    // Test valeurs négatives
    try {
        Individu erreurNeg = new Individu("Thon", -1, 5, 2, contenuVers);
    } catch (Exception e) {
        System.out.println("Erreur valeur négative : " + e);
    }

} catch (Exception e) {
    System.out.println("Erreur générale : " + e);
}

	}


}

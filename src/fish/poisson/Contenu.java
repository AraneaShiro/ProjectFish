package fish.poisson;

import fish.exceptions.*;


/**
 * Classe representant les possibles contenus d'un poisson
 * (ex:Un organe) et le nombre de vers ou son taux d'infestation
 * 
 * @author Jules Grenesche / Arthur Bernard
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

    /**
     * Booleen contenu infecté ou pas
     */
    private boolean isInfected;

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
     * @return Renvoie le nombre de vers du contenu ou -1 si nombre non renseigné
     * @throws NoNbVersException si le nombre de vers est non indiqué (=-1)
     */
    public int getNbVers() {
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

    /**
     * Methode Get sur le booleen sur l'infestation
     * 
     * @return Renvoie le booleen sur l'infestation du contenu
     */
    public boolean getInfected(){
        return this.isInfected;
    }

    //////////////////////////////Setter////////////////////
    /**
     * Methode pour renseigner le type du contenu
     * 
     * @param type String du type de contenu
     * @throws EmptyStringException si le string entré en paramètre est vide
     */
    private void setType(String type) throws EmptyStringException{
        if(type == ""){
            throw new EmptyStringException();
        }
        this.type = type;
    }

    /**
     * Methode pour renseigner le nombre de vers dans le contenu
     * 
     * @param nbVers Int du nombre de vers que l'on veut renseigner dans le contenu
     * @throws NegativeValueException si la valeur entrée en paramètre est négative et différente de -1 (non indiqué)
     */
    private void setNbVers(int nbVers) throws NegativeValueException{
        if(nbVers <-1){
            throw new NegativeValueException();
        }else{
            this.nbVers = nbVers;            
        }
    }

    /**
     * Methode pour renseigner le taux d'infestation dans le contenu
     * 
     * @param taux Float du pourcentage que l'on veut renseigner dans le contenu
     * @throws NegativeValueException si la valeur entrée en parametre est négative
     * @throws TauxValueException si la valeur du taux est supérieure à 1
     */
    private void setTauxInfestation(float taux) throws NegativeValueException, TauxValueException{
        if(taux <0){
            throw new NegativeValueException();
        }else if(taux >1){
            throw new TauxValueException();
        }else{
            this.tauxInfestation = taux;        
        }
    }
    

    //////////////////////////////Constructeur////////////////////
    /**
     * Constructeur avec nombre de vers
     * 
     * @param type   String du type du contenu
     * @param nbVers Int du nombre de vers dans le contenu
     * @throws EmptyStringException si le string entré en paramètre est vide
     * @throws NegativeValueException si le nombre de vers entré en paramère est <-1
     */
    public Contenu(String type, int nbVers) throws NegativeValueException, EmptyStringException{
        try{
           setType(type);
            setNbVers(nbVers);
            if(nbVers>0){
                this.isInfected = true;
            }else{
                this.isInfected = false;
            } 
        } catch(Exception e){
            System.out.println(e);
        }
        
    }

    /**
     * Constructeur avec le taux d'infestation
     * 
     * @param type String du type du contenu
     * @param taux Float du taux d'infestation dans le contenu
     * @throws EmptyStringException si le string entré en paramètre est vide
     * @throws NegativeValueException si le taux est négatif
     * @throws TauxValueException si le taux est supérieur à 1
     */
    public Contenu(String type, float taux) throws NegativeValueException, TauxValueException , EmptyStringException{
        try{
           setType(type);
            setTauxInfestation(taux);
            setNbVers(-1);
            if(taux >0){
                this.isInfected = true;
            }else{
                this.isInfected = false;
            } 
        } catch(Exception e){
            System.out.println(e);
        }
        
    }

    ///////////////////////////// Main ////////////////////
    public static void main(String[] args){
        try{
            Contenu content = new Contenu("body", 0.33f);
            Contenu c2 = new Contenu("foie", 0);
            System.out.println(content.getNbVers());
            System.out.println("Contenu infecté : "+content.isInfected);
            System.out.println(c2.getNbVers());
            System.out.println("Contenu 2 infecté : "+ c2.isInfected);

        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }
}

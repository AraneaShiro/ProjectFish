package acquisition.lecture;
import acquisition.Dataframe;

/**
 * LectureCSV est la classe qui permet de lire un csv et le convertir dans le dataframe
 * 
 * @author Arthur BERNARD
 * @version 0.1
 * Date : 17/03
 */

public class LectureCSV {
////////////////////////////// Attributs ////////////////////
    /**
     * Le Dataframe contenant les informations du CSV lu
     * @see Dataframe
     */
    protected Dataframe dataframe;

//////////////////////////////Getter / Setter////////////////////
	/**
     * Méthode get sur le dataframe
     * @return le dataframe associé au fichier CSV lu
     */
    public Dataframe getDataframe(){
        return this.dataframe;
    }
	
	
//////////////////////////////Constructor////////////////////
    
//////////////////////////////Methodes////////////////////

}

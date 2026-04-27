package fish.nettoyage;

import fish.acquisition.*;
import fish.exceptions.OutOfBoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Classe abstraite de nettoyage d'un Dataframe.
 * Contient les opérations communes à tous les types de dataframe.
 *
 * Opérations disponibles :
 * - suppressionInvalid() : supprime les lignes dont le taux de null
 * dépasse le seuil
 * - suppressionColonnesVides() : supprime les colonnes entièrement nulles
 * - triAnisakis() : trie par valeur Anisakis décroissante
 * - reconnaissanceAnisakis() : ajoute une colonne booléenne "Anisakis_Present"
 * - Suppression ligne() 
 *
 * @author Jules Grenesche
 * @version 0.5
 */
public abstract class NettoyageDataframe {

    /** Le dataframe à nettoyer */
    protected DataframeComplet df;

    /**
     * Seuil de tolérance null : proportion maximale de null autorisée par
     * ligne (0.5 = 50%)
     */
    protected double seuilNull;

    /**
     * Constructeur avec seuil personnalisé
     *
     * @param df        le dataframe à nettoyer
     * @param seuilNull proportion max de null autorisée par ligne (ex: 0.5)
     */
    public NettoyageDataframe(DataframeComplet df, double seuilNull) {
        this.df = df;
        this.seuilNull = seuilNull;
    }

    /**
     * Constructeur par défaut — seuil à 50%
     *
     * @param df le dataframe à nettoyer
     */
    public NettoyageDataframe(DataframeComplet df) {
        this(df, 0.5);
    }

    public DataframeComplet getDf() {
        return df;
    }

    // ── Suppression lignes invalides ─────────────────────────────────────────

    /**
     * Supprime les lignes dont le taux de valeurs null dépasse le seuil.
     * Reconstruit le tableau sans ces lignes.
     *
     * @return le nombre de lignes supprimées
     */
    public int suppressionInvalid() {
        int nbCol = df.getNbCol();
        int nbLignes = df.getNbLignes();
        List<Integer> lignesValides = new ArrayList<>();

        for (int i = 0; i < nbLignes; i++) {
            int nbNull = 0;
            for (int j = 0; j < nbCol; j++) {
                try {
                    if (df.getCase(i, j) == null)
                        nbNull++;
                } catch (OutOfBoundException e) {
                    /* ignoré */ }
            }
            double tauxNull = (double) nbNull / nbCol;
            if (tauxNull <= seuilNull) {
                lignesValides.add(i);
            }
        }

        int supprimees = nbLignes - lignesValides.size();
        if (supprimees == 0) {
            System.out.println("Aucune ligne invalide détectée.");
            return 0;
        }

        // Garde-fou : si toutes les lignes seraient supprimées, on annule
        if (lignesValides.isEmpty()) {
            System.out.println("Suppression annulée : toutes les lignes dépassent le seuil.");
            return 0;
        }

        Object[][] nouveauTableau = new Object[lignesValides.size()][nbCol];
        for (int i = 0; i < lignesValides.size(); i++) {
            int ligneSource = lignesValides.get(i);
            for (int j = 0; j < nbCol; j++) {
                try {
                    nouveauTableau[i][j] = df.getCase(ligneSource, j);
                } catch (OutOfBoundException e) {
                    /* ignoré */ }
            }
        }

        reconstruireDataframe(nouveauTableau, lignesValides.size());
        System.out.println(supprimees + " ligne(s) supprimée(s) (taux null > "
                + (int) (seuilNull * 100) + "%).");
        return supprimees;
    }

    // ── Suppression colonnes vides ───────────────────────────────────────────

    /**
     * Supprime les colonnes entièrement vides (toutes les valeurs null).
     * Utile pour les CSV avec séparateurs doubles (ex: ";;").
     *
     * @return le nombre de colonnes supprimées
     */
    public int suppressionColonnesVides() {
        int nbCol = df.getNbCol();
        int nbLignes = df.getNbLignes();
        List<Integer> colonnesValides = new ArrayList<>();

        for (int j = 0; j < nbCol; j++) {
            boolean toutNull = true;
            for (int i = 0; i < nbLignes; i++) {
                try {
                    if (df.getCase(i, j) != null) {
                        toutNull = false;
                        break;
                    }
                } catch (OutOfBoundException e) {
                    /* ignoré */ }
            }
            if (!toutNull)
                colonnesValides.add(j);
        }

        int supprimees = nbCol - colonnesValides.size();
        if (supprimees == 0) {
            System.out.println("Aucune colonne vide détectée.");
            return 0;
        }

        // Reconstruction sans les colonnes vides
        String[] anciens = df.getNomColonnes();
        String[] nouveauxNoms = new String[colonnesValides.size()];
        Object[][] nouveauTableau = new Object[nbLignes][colonnesValides.size()];

        for (int k = 0; k < colonnesValides.size(); k++) {
            nouveauxNoms[k] = anciens[colonnesValides.get(k)];
        }
        for (int i = 0; i < nbLignes; i++) {
            for (int k = 0; k < colonnesValides.size(); k++) {
                try {
                    nouveauTableau[i][k] = df.getCase(i, colonnesValides.get(k));
                } catch (OutOfBoundException e) {
                    /* ignoré */ }
            }
        }

        reconstruireDataframeAvecNoms(nouveauTableau, nouveauxNoms, nbLignes);
        System.out.println(supprimees + " colonne(s) vide(s) supprimée(s).");
        return supprimees;
    }

    // ── Tri Anisakis ─────────────────────────────────────────────────────────

    /**
     * Trie le tableau selon la colonne Anisakis par ordre décroissant
     * (les plus infestés en premier).
     *
     * @return le nombre de lignes avec une valeur Anisakis > 0, ou -1 si
     *         aucune colonne Anisakis n'est détectée
     */
    public int triAnisakis() {
        int colAnisakis = getIndexColonneAnisakis();
        if (colAnisakis < 0) {
            System.out.println("Aucune colonne Anisakis détectée.");
            return -1;
        }

        int nbLignes = df.getNbLignes();
        Object[][] tableau = copierTableau();

        // Tri bulles décroissant sur la colonne Anisakis
        for (int i = 0; i < nbLignes - 1; i++) {
            for (int j = 0; j < nbLignes - i - 1; j++) {
                double val1 = toDouble(tableau[j][colAnisakis]);
                double val2 = toDouble(tableau[j + 1][colAnisakis]);
                if (val1 < val2) {
                    Object[] tmp = tableau[j];
                    tableau[j] = tableau[j + 1];
                    tableau[j + 1] = tmp;
                }
            }
        }

        reconstruireDataframe(tableau, nbLignes);

        int nbInfestes = 0;
        for (int i = 0; i < nbLignes; i++) {
            if (toDouble(tableau[i][colAnisakis]) > 0)
                nbInfestes++;
        }
        System.out.println("Tri Anisakis effectué. "
                + nbInfestes + "/" + nbLignes + " ligne(s) infestée(s).");
        return nbInfestes;
    }

    // ── Reconnaissance Anisakis ──────────────────────────────────────────────

    /**
     * Reconnaît et marque la présence d'Anisakis dans chaque ligne.
     * Ajoute une colonne booléenne "Anisakis_Present" si elle n'existe pas.
     * Met à jour la colonne existante sinon.
     *
     * @return le nombre de lignes positives à Anisakis, ou -1 si aucune
     *         colonne Anisakis n'est détectée
     */
    public int reconnaissanceAnisakis() {
        int colAnisakis = getIndexColonneAnisakis();
        if (colAnisakis < 0) {
            System.out.println("Aucune colonne Anisakis détectée.");
            return -1;
        }

        String nomMarqueur = "Anisakis_Present";
        int iMarqueur = getIndexColonne(nomMarqueur);
        int nbLignes = df.getNbLignes();
        int nbCol = df.getNbCol();
        int nbPositifs = 0;

        if (iMarqueur < 0) {
            // Ajout d'une colonne Boolean à droite
            String[] nouveauxNoms = ajouterNomColonne(nomMarqueur);
            Object[][] nouveauTableau = new Object[nbLignes][nbCol + 1];

            for (int i = 0; i < nbLignes; i++) {
                for (int j = 0; j < nbCol; j++) {
                    try {
                        nouveauTableau[i][j] = df.getCase(i, j);
                    } catch (OutOfBoundException e) {
                        /* ignoré */ }
                }
                boolean positif = toDouble(nouveauTableau[i][colAnisakis]) > 0;
                nouveauTableau[i][nbCol] = positif;
                if (positif)
                    nbPositifs++;
            }
            reconstruireDataframeAvecNoms(nouveauTableau, nouveauxNoms, nbLignes);

        } else {
            // Mise à jour de la colonne existante
            for (int i = 0; i < nbLignes; i++) {
                try {
                    boolean positif = toDouble(df.getCase(i, colAnisakis)) > 0;
                    df.setCase(i, iMarqueur, positif);
                    if (positif)
                        nbPositifs++;
                } catch (OutOfBoundException e) {
                    /* ignoré */ }
            }
        }

        System.out.println("Reconnaissance Anisakis : "
                + nbPositifs + "/" + nbLignes + " positifs.");
        return nbPositifs;
    }

    // ── Méthodes abstraites ──────────────────────────────────────────────────

    /**
     * Retourne l'index de la colonne Anisakis pertinente pour ce type de
     * dataframe (nbVers, taux, prévalence selon le contexte).
     * 
     * @return l'index de la colonne trouve
     */
    protected abstract int getIndexColonneAnisakis();

    /**
     * Reconstruit le dataframe avec un nouveau tableau
     * (mêmes colonnes, nombre de lignes modifié).
     * @param tab le tableau de donnée
     * @param nbLignes Le nombre de ligne
     */
    protected abstract void reconstruireDataframe(Object[][] nouveauTableau, int nbLignes);

    /**
     * Reconstruit le dataframe avec un nouveau tableau ET de nouveaux noms
     * de colonnes.
     * 
     * @param tab le tableau de donnée
     * @param noms les entetes
     * @param nbLignes Le nombre de ligne
     * 
     */
    protected abstract void reconstruireDataframeAvecNoms(Object[][] tab,
            String[] noms, int nbLignes);

    // ── Utilitaires ──────────────────────────────────────────────────────────

    /**
     * Convertit un Object en double. Retourne 0.0 si non numérique ou null.
     * @param val l'objet que l'on veut convertir en double
     * @return Un double de la valeur si l'objet est numerique sinon 0
     */
    protected double toDouble(Object val) {
        if (val instanceof Number)
            return ((Number) val).doubleValue();
        return 0.0;
    }

    /**
     * Retourne l'index de la première colonne dont le nom contient motCle
     * (insensible à la casse), ou -1 si non trouvé.
     * 
     * @param motCle Le mot cle que l on recherche
     * @return l'index de la colonne trouvé ou -1 sinon
     */
    protected int getIndexColonne(String motCle) {
        String[] noms = df.getNomColonnes();
        for (int j = 0; j < noms.length; j++) {
            if (noms[j].toLowerCase().contains(motCle.toLowerCase()))
                return j;
        }
        return -1;
    }

    /** Copie le tableau courant du dataframe dans un nouveau tableau. 
     * 
     * @return le tableau d'objet copier
    */
    protected Object[][] copierTableau() {
        int nbLignes = df.getNbLignes();
        int nbCol = df.getNbCol();
        Object[][] copie = new Object[nbLignes][nbCol];
        for (int i = 0; i < nbLignes; i++) {
            for (int j = 0; j < nbCol; j++) {
                try {
                    copie[i][j] = df.getCase(i, j);
                } catch (OutOfBoundException e) {
                    /* ignoré */ }
            }
        }
        return copie;
    }

    /**
     * Retourne un nouveau tableau de noms de colonnes avec nomNouvelle ajouté
     * à la fin.
     * 
     * @param nomNouvelle Nouveau nom de la colonne
     * @return retourne le tableau des entetes avec la nouvelle colonne
     */
    protected String[] ajouterNomColonne(String nomNouvelle) {
        String[] anciens = df.getNomColonnes();
        String[] nouveaux = new String[anciens.length + 1];
        System.arraycopy(anciens, 0, nouveaux, 0, anciens.length);
        nouveaux[anciens.length] = nomNouvelle;
        return nouveaux;
    }

/**
 * Supprime les lignes dont la valeur d'une colonne choisie est négative.
 * Les lignes avec null dans cette colonne sont conservées.
 *
 * @param col l'index de la colonne à vérifier
 * @return le nombre de lignes supprimées, ou -1 si l'index est invalide
 */
public int suppressionLignesNegatives(int col) {
    if (col < 0 || col >= df.getNbCol()) {
        System.out.println("Index de colonne invalide : " + col);
        return -1;
    }

    int nbLignes = df.getNbLignes();
    int nbCol    = df.getNbCol();
    List<Integer> lignesValides = new ArrayList<>();

    for (int i = 0; i < nbLignes; i++) {
        try {
            Object val = df.getCase(i, col);
            // On garde la ligne si : null OU valeur >= 0
            if (val == null || !(val instanceof Number) || ((Number) val).doubleValue() >= 0) {
                lignesValides.add(i);
            }
        } catch (OutOfBoundException e) {
            lignesValides.add(i); // En cas d'erreur on conserve la ligne
        }
    }

    int supprimees = nbLignes - lignesValides.size();
    if (supprimees == 0) {
        System.out.println("Aucune valeur négative dans la colonne '"
                + df.getNomCol(col) + "'.");
        return 0;
    }


    
    // Reconstruction du tableau sans les lignes négatives
    Object[][] nouveauTableau = new Object[lignesValides.size()][nbCol];
    for (int i = 0; i < lignesValides.size(); i++) {
        int ligneSource = lignesValides.get(i);
        for (int j = 0; j < nbCol; j++) {
            try {
                nouveauTableau[i][j] = df.getCase(ligneSource, j);
            } catch (OutOfBoundException e) { /* ignoré */ }
        }
    }

    reconstruireDataframe(nouveauTableau, lignesValides.size());
    System.out.println(supprimees + " ligne(s) supprimée(s) (valeur négative colonne '"
            + df.getNomCol(col) + "').");
    return supprimees;
}

/**
 * Surcharge par nom de colonne.
 * Cherche la colonne par mot-clé et appelle suppressionLignesNegatives(int).
 *
 * @param nomColonne le mot-clé à chercher dans les noms de colonnes
 * @return le nombre de lignes supprimées, ou -1 si colonne non trouvée
 */
public int suppressionLignesNegatives(String nomColonne) {
    int col = getIndexColonne(nomColonne);
    if (col < 0) {
        System.out.println("Colonne '" + nomColonne + "' introuvable.");
        return -1;
    }
    return suppressionLignesNegatives(col);
}




 /**
 * Remplace les éléments négatifs de la colonne ciblé par null
 *
 * @param col l'index de la colonne à vérifier
 * @return le nombre de cases passé a null et -1 si on ne trouve pas la colonne
 */
public int remplaceNegativeByNull(int col) {
    if (col < 0 || col >= df.getNbCol()) { //Si en dehors des dimensions du tableau
        System.out.println("Index de colonne invalide : " + col);
        return -1;
    }

    int nbLignes = df.getNbLignes();
    int nbCol    = df.getNbCol();

    int supprimees=0;
    for (int i = 0; i < nbLignes; i++) {
        try {
            Object val = df.getCase(i, col);
            // On garde la ligne si : null OU valeur >= 0
            if (val!=null &&  ((Number) val).doubleValue() < 0) { //Si négatif on mais null
                df.setCase(i, col, null);
                supprimees++;
            }
        } catch (OutOfBoundException e) {
            /*On ne fait rien */
        }
    }

    System.out.println(supprimees + " Cases passé a null (valeur négative colonne '"
            + df.getNomCol(col) + "').");
    return supprimees;
}

/**
 * Surcharge par nom de colonne.
 * Cherche la colonne par mot-clé et appelle remplaceNegativeByNull(int).
 *
 * @param nomColonne le mot-clé à chercher dans les noms de colonnes
 * @return le nombre de cases passé a null et -1 si on ne trouve pas la colonne
 */
public int remplaceNegativeByNull(String nomColonne) {
    int col = getIndexColonne(nomColonne);
    if (col < 0) {
        System.out.println("Colonne '" + nomColonne + "' introuvable.");
        return -1;
    }
    return remplaceNegativeByNull(col);
}



 /**
 * Remplace les éléments négatifs ou zero de la colonne ciblé par null
 * C est pour les cas comme une masse =0
 *
 * @param col l'index de la colonne à vérifier
 * @return le nombre de cases passé a null et -1 si on ne trouve pas la colonne
 */
public int remplaceNegativeOrZeroByNull(int col) {
    if (col < 0 || col >= df.getNbCol()) { //Si en dehors des dimensions du tableau
        System.out.println("Index de colonne invalide : " + col);
        return -1;
    }

    int nbLignes = df.getNbLignes();
    int nbCol    = df.getNbCol();

    int supprimees=0;
    for (int i = 0; i < nbLignes; i++) {
        try {
            Object val = df.getCase(i, col);
            // On garde la ligne si : null OU valeur >= 0
            if (val!=null && ((Number) val).doubleValue() <= 0 ) { //Si négatif on mais null ou égale a 0
                df.setCase(i, col, null);
                supprimees++;
            }
        } catch (OutOfBoundException e) {
            /*On ne fait rien */
        }
    }

    System.out.println(supprimees + " Cases passé a null (valeur négative ou 0 colonne '"
            + df.getNomCol(col) + "').");
    return supprimees;
}


/**
 * Surcharge par nom de colonne.
 * Cherche la colonne par mot-clé et appelle remplaceNegativeByNull(int).
 *
 * @param nomColonne le mot-clé à chercher dans les noms de colonnes
 * @return le nombre de cases passé a null et -1 si on ne trouve pas la colonne
 */
public int remplaceNegativeOrZeroByNull(String nomColonne) {
    int col = getIndexColonne(nomColonne);
    if (col < 0) {
        System.out.println("Colonne '" + nomColonne + "' introuvable.");
        return -1;
    }
    return remplaceNegativeOrZeroByNull(col);
}

/**
 * Remplace les éléments vide ou null par l'Objet le plus présent de la colonne 90% sauf si null
 *
 * @param col l'index de la colonne à vérifier
 * @param seuil le float ex 0.9 pour 90% du seuil voulue
 * @return le nombre de cases changés ou -1 sinon
 */
public int remplaceByMostCommon(int col, float seuil) {
    try {
        HashMap<Object, Integer> unique = df.getUniqueColonneSomme(col);
        int nbChangé = 0;
        int nbElement = 0;

        // On trouve le nombre d'éléments non null
        for (Map.Entry<Object, Integer> entry : unique.entrySet()) {
            if (entry.getKey() != null) { // On ne compte pas les nulls
                nbElement += entry.getValue();
            }
        }

        for (Map.Entry<Object, Integer> entry : unique.entrySet()) {
            if (entry.getKey() != null) {
                // Si l'élément est plus de seuil% des stats
                if (((float) entry.getValue() / (float) nbElement) > seuil) {

                    int nbLignes = df.getNbLignes();
                    for (int i = 0; i < nbLignes; i++) {
                        Object val = df.getCase(i, col);

                        // On garde la ligne si : null OU valeur vide
                        if (val == null || ((String) val).isEmpty()) {
                            df.setCase(i, col, entry.getValue()); //On remplace par la valeur principale
                            nbChangé++;
                        }
                    }
                    return nbChangé;
                }
            }
        }

        return nbChangé;

        } catch (Exception e) {
            
            return -1;
        }
    }
}



    





//A faire
/*
Kmeans ?
pllusProcheVoisin
kmedoid median
k_nn






*/

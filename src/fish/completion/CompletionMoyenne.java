// ── Package ───────────────────────────────────────────────────
package fish.completion;

// ── Import ───────────────────────────────────────────────────
import fish.acquisition.DataframeComplet;
import fish.acquisition.DfIndividu;
import fish.exceptions.OutOfBoundException;
import java.util.ArrayList;
import java.util.List;
// ── TESTE : Oui ───────────────────────────────────────────────────
/**
 * Complète les valeurs null d'un Dataframe par la moyenne de leur colonne.
 * Seules les colonnes numériques sont complétées.
 * Les colonnes String/Boolean sont ignorées.
 *
 * @author Jules Grenesche
 * @version 1
 */
public class CompletionMoyenne {

    private final DataframeComplet df;

    public CompletionMoyenne(DataframeComplet df) {
        this.df = df;
    }

    /**
     * Complète TOUTES les colonnes numériques du dataframe.
     *
     * @return le nombre de cases complétées
     */
    public int completerTout() {
        int total = 0;
        for (int j = 0; j < df.getNbCol(); j++) {
            total += completerColonne(j);
        }
        System.out.println(total + " case(s) complétée(s) par la moyenne.");
        return total;
    }

    /**
     * Complète une colonne spécifique par sa moyenne.
     *
     * @param col l'index de la colonne
     * @return le nombre de cases complétées dans cette colonne
     */
    public int completerColonne(int col) {
        double moyenne = calculerMoyenneColonne(col);
        if (Double.isNaN(moyenne))
            return 0; // Colonne non numérique

        int completes = 0;
        for (int i = 0; i < df.getNbLignes(); i++) {
            try {
                if (df.getCase(i, col) == null) {
                    df.setCase(i, col, moyenne);
                    completes++;
                }
            } catch (OutOfBoundException e) {
                /* ignoré */ }
        }
        return completes;
    }

    /**
     * Complète uniquement les colonnes dont le nom contient le mot-clé.
     *
     * @param motCle le mot-clé à rechercher dans les noms de colonnes
     * @return le nombre de cases complétées
     */
    public int completerColonneParNom(String motCle) {
        int total = 0;
        String[] noms = df.getNomColonnes();
        for (int j = 0; j < noms.length; j++) {
            if (noms[j].toLowerCase().contains(motCle.toLowerCase())) {
                total += completerColonne(j);
            }
        }
        return total;
    }

    // ── Utilitaire ───────────────────────────────────────────────────────────

    /** Retourne la moyenne de la colonne
     * @param col l'indice de la colonne
     * @return la moyenne de la colonne
     */
    private double calculerMoyenneColonne(int col) {
        List<Double> valeurs = new ArrayList<>();
        for (int i = 0; i < df.getNbLignes(); i++) {
            try {
                Object val = df.getCase(i, col);
                if (val instanceof Number)
                    valeurs.add(((Number) val).doubleValue());
            } catch (OutOfBoundException e) {
                /* ignoré */ }
        }
        if (valeurs.isEmpty())
            return Double.NaN;
        return valeurs.stream().mapToDouble(Double::doubleValue).average().orElse(Double.NaN);
    }

        // TEST
    public static void main(String[] args) {
        int ok = 0, tot = 0;
        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║                    TESTS CompletionMoyenne                   ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
        System.out.println();

        // ──────────────────────────────────────────────────────────────────────────────
        // TEST 1 : completerColonne
        // ──────────────────────────────────────────────────────────────────────────────
        System.out.println("─── TEST 1 : completerColonne() ───────────────────────────────");
        try {
            Object[][] data = {
                {30.0, 200.0, "Merlan"},
                {null, 150.0, "Hareng"},
                {50.0, null,  "Thon"},
                {40.0, null,  null}
            };
            DfIndividu df = new DfIndividu(4, new String[]{"longueur", "poids", "espece"}, data);
            CompletionMoyenne cm = new CompletionMoyenne(df);

            System.out.println("\n AVANT complétion :");
            df.afficherPremieresFignes(4);

            int n0 = cm.completerColonne(0);
            tot++; 
            if (n0 == 1) { 
                ok++; 
                System.out.println("\n Test 1.1 : completerColonne(longueur) → " + n0 + " case complétée"); 
            } else { 
                System.out.println("\n Test 1.1 : completerColonne(longueur) → " + n0 + " (attendu 1)"); 
            }

            int n1 = cm.completerColonne(1);
            tot++; 
            if (n1 == 2) { 
                ok++; 
                System.out.println(" Test 1.2 : completerColonne(poids) → " + n1 + " cases complétées"); 
            } else { 
                System.out.println(" Test 1.2 : completerColonne(poids) → " + n1 + " (attendu 2)"); 
            }

            int n2 = cm.completerColonne(2);
            tot++; 
            if (n2 == 0) { 
                ok++; 
                System.out.println(" Test 1.3 : completerColonne(espece) → " + n2 + " (colonne String ignorée)"); 
            } else { 
                System.out.println(" Test 1.3 : completerColonne(espece) → " + n2 + " (attendu 0)"); 
            }

            System.out.println("\n APRÈS complétion :");
            df.afficherPremieresFignes(4);
            df.afficherStatistiques();

        } catch (Exception e) { 
            System.out.println(" Test 1 : Exception - " + e.getMessage()); 
        }
        System.out.println();

        // ──────────────────────────────────────────────────────────────────────────────
        // TEST 2 : completerColonneParNom
        // ──────────────────────────────────────────────────────────────────────────────
        System.out.println("─── TEST 2 : completerColonneParNom() ─────────────────────────");
        try {
            Object[][] data = {{30.0, 200.0}, {null, 150.0}, {50.0, null}};
            DfIndividu df = new DfIndividu(3, new String[]{"longueur_mm", "poids_g"}, data);
            CompletionMoyenne cm = new CompletionMoyenne(df);

            System.out.println("\n AVANT complétion :");
            df.afficherPremieresFignes(3);

            int n = cm.completerColonneParNom("long");
            tot++; 
            if (n == 1) { 
                ok++; 
                System.out.println("\n Test 2.1 : completerColonneParNom('long') → " + n + " case complétée"); 
            } else { 
                System.out.println("\n Test 2.1 : completerColonneParNom('long') → " + n + " (attendu 1)"); 
            }

            int nAbs = cm.completerColonneParNom("absent");
            tot++; 
            if (nAbs == 0) { 
                ok++; 
                System.out.println(" Test 2.2 : completerColonneParNom('absent') → " + nAbs + " (mot-clé absent)"); 
            } else { 
                System.out.println(" Test 2.2 : completerColonneParNom('absent') → " + nAbs + " (attendu 0)"); 
            }

            System.out.println("\n APRÈS complétion :");
            df.afficherPremieresFignes(3);

        } catch (Exception e) { 
            System.out.println(" Test 2 : Exception - " + e.getMessage()); 
        }
        System.out.println();

        // ──────────────────────────────────────────────────────────────────────────────
        // TEST 3 : completerTout
        // ──────────────────────────────────────────────────────────────────────────────
        System.out.println("─── TEST 3 : completerTout() ─────────────────────────────────");
        try {
            Object[][] data = {{10.0, 1.0}, {20.0, null}, {null, 3.0}, {null, null}};
            DfIndividu df = new DfIndividu(4, new String[]{"a", "b"}, data);
            CompletionMoyenne cm = new CompletionMoyenne(df);

            System.out.println("\n AVANT complétion :");
            df.afficherPremieresFignes(4);
            System.out.println("\n   → Moyenne a = (10+20)/2 = 15.0");
            System.out.println("   → Moyenne b = (1+3)/2 = 2.0");

            int totalCompletes = cm.completerTout();
            tot++; 
            if (totalCompletes == 4) { 
                ok++; 
                System.out.println("\n Test 3.1 : completerTout() → " + totalCompletes + " cases complétées"); 
            } else { 
                System.out.println("\n Test 3.1 : completerTout() → " + totalCompletes + " (attendu 4)"); 
            }

            System.out.println("\n APRÈS complétion :");
            df.afficherPremieresFignes(4);
            df.afficherStatistiques();

        } catch (Exception e) { 
            System.out.println(" Test 3 : Exception - " + e.getMessage()); 
        }
        System.out.println();

        // ──────────────────────────────────────────────────────────────────────────────
        // TEST 4 : Dataframe sans nulls
        // ──────────────────────────────────────────────────────────────────────────────
        System.out.println("─── TEST 4 : Dataframe sans valeurs null ─────────────────────");
        try {
            Object[][] data = {{10.0, 1.0}, {20.0, 2.0}};
            DfIndividu df = new DfIndividu(2, new String[]{"a", "b"}, data);
            CompletionMoyenne cm = new CompletionMoyenne(df);

            System.out.println("\n Données (aucune valeur null) :");
            df.afficherPremieresFignes(2);

            int n = cm.completerTout();
            tot++; 
            if (n == 0) { 
                ok++; 
                System.out.println("\n Test 4 : completerTout() → " + n + " case (rien à compléter)"); 
            } else { 
                System.out.println("\n Test 4 : completerTout() → " + n + " (attendu 0)"); 
            }

        } catch (Exception e) { 
            System.out.println(" Test 4 : Exception - " + e.getMessage()); 
        }
        System.out.println();

        // ──────────────────────────────────────────────────────────────────────────────
        // TEST 5 : Colonne 100% null
        // ──────────────────────────────────────────────────────────────────────────────
        System.out.println("─── TEST 5 : Colonne entièrement null ────────────────────────");
        try {
            Object[][] data = {{null}, {null}, {null}};
            DfIndividu df = new DfIndividu(3, new String[]{"x"}, data);
            CompletionMoyenne cm = new CompletionMoyenne(df);

            System.out.println("\n Données (100% null) :");
            df.afficherPremieresFignes(3);
            System.out.println("\n   → Moyenne impossible (pas de valeurs numériques)");

            int n = cm.completerColonne(0);
            tot++; 
            if (n == 0) { 
                ok++; 
                System.out.println("\n Test 5 : complétion impossible → " + n + " case complétée"); 
            } else { 
                System.out.println("\n Test 5 : complétion → " + n + " (attendu 0)"); 
            }

            System.out.println("\n APRÈS tentative de complétion (inchangé) :");
            df.afficherPremieresFignes(3);

        } catch (Exception e) { 
            System.out.println(" Test 5 : Exception - " + e.getMessage()); 
        }
        System.out.println();

        // ──────────────────────────────────────────────────────────────────────────────
        // TEST 6 : Fichier réel mackerel.97442.csv
        // ──────────────────────────────────────────────────────────────────────────────
        System.out.println("─── TEST 6 : Fichier réel mackerel.97442.csv ─────────────────");
        try {
            fish.acquisition.lecture.LectureCSV lecteur = new fish.acquisition.lecture.LectureCSV(";");
            DfIndividu dfMack = lecteur.lireCSV("data/mackerel.97442.csv", DfIndividu.class);

            if (dfMack != null) {
                System.out.println("\n Lecture réussie : " + dfMack.getNbLignes() + " lignes, " + dfMack.getNbCol() + " colonnes");

                System.out.println("\n AVANT complétion (3 premières lignes) :");
                dfMack.afficherPremieresFignes(3);

                System.out.println("\n Statistiques AVANT complétion :");
                dfMack.afficherStatistiques();

                CompletionMoyenne cm = new CompletionMoyenne(dfMack);
                int n = cm.completerTout();
                tot++; ok++; 
                System.out.println("\n Test 6 : completerTout() → " + n + " case(s) complétée(s)");

                System.out.println("\n APRÈS complétion (3 premières lignes) :");
                dfMack.afficherPremieresFignes(3);

                System.out.println("\n Statistiques APRÈS complétion :");
                dfMack.afficherStatistiques();

            } else {
                System.out.println(" Test 6 : Lecture du fichier échouée");
            }

        } catch (Exception e) { 
            System.out.println(" Test 6 : Exception - " + e.getMessage()); 
        }
        System.out.println();

        // ──────────────────────────────────────────────────────────────────────────────
        // RÉSULTAT FINAL
        // ──────────────────────────────────────────────────────────────────────────────
        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║                    RÉSULTAT DES TESTS                        ║");
        System.out.println("╠══════════════════════════════════════════════════════════════╣");
        System.out.printf("║  CompletionMoyenne : %d/%d tests réussis%34s║\n", ok, tot, "");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
    }
}
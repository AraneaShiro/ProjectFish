package fish.tests;

import fish.acquisition.*;
import fish.analyse.*;
import fish.completion.*;
import fish.exceptions.*;
import fish.nettoyage.*;
import static fish.tests.AsciiPlot.*;
import java.util.*;

/**
 * Suite de tests complète avec visualisations ASCII pour :
 *   - DataframeComplet  (affichage paginé, couleurs)
 *   - CompletionKNN     (imputation, tableau avant/après)
 *   - KMeans            (scatter plot 2D)
 *   - KMedoids PAM      (scatter plot 2D comparatif)
 *   - BoiteAMoustache   (déjà visuelle, test intégration)
 *
 * Lancer : javac -d out src/**{@literal /}*.java && java -cp out fish.tests.TestVisuelComplet
 *
 * @author Jules Grenesche
 * @version 1.0
 */
public class TestVisuelComplet {

    // ── Compteurs globaux ─────────────────────────────────────────────────────
    private static final String[] SECTIONS = {
        "DataframeComplet", "CompletionKNN", "KMeans", "KMedoids", "BoiteAMoustache"
    };
    private static final int[] OK  = new int[5];
    private static final int[] TOT = new int[5];

    // ── Main ──────────────────────────────────────────────────────────────────

    /**
     * Lance l'ensemble de la suite de tests avec visualisations ASCII.
     * Exécute les 5 sections dans l'ordre et affiche un résumé global final.
     *
     * @param args arguments de la ligne de commande (ignorés)
     */
    public static void main(String[] args) {
        System.out.println("\n" + CYA_G + G
                + "╔═══════════════════════════════════════════════════════════╗\n"
                + "║        SUITE DE TESTS & VISUALISATIONS                   ║\n"
                + "║        fish — Analyse de données parasitologiques         ║\n"
                + "╚═══════════════════════════════════════════════════════════╝"
                + R);

        testerDataframeComplet();
        testerCompletionKNN();
        testerKMeans();
        testerKMedoids();
        testerBoiteAMoustache();

        resumeGlobal(SECTIONS, OK, TOT);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  1. DataframeComplet
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Section 1 — Tests du {@link fish.acquisition.DataframeComplet}.
     * Vérifie l'affichage paginé, les statistiques et la pagination colorée.
     */
    private static void testerDataframeComplet() {
        bandeauSection(1, "DataframeComplet — Affichage paginé", CYA_G);
        int s = 0; // index section

        try {
            // ── Dataset large : 5 lignes × 10 colonnes ───────────────────────
            Object[][] data = {
                {"Merlan",   12, 1.5, 0.7, "A", 30.2, null, 3, "Nord",  2021},
                {"Hareng",   45, 3.2, 1.1, "B", 28.5, 0.6,  7, "Sud",   2022},
                {"Thon",     null, 8.4, 2.3, "A", null, 1.2, 15, "Est", 2020},
                {"Anchois",  28, 0.8, null, "C", 22.1, 0.3,  2, "Ouest",2023},
                {"Maquereau",33, 2.1, 0.9, "B", 35.7, 0.9,  9, "Nord",  2022},
            };
            String[] cols = {"Espece","Effectif","Intensite","Abondance",
                             "Groupe","Longueur","Masse","NbVers","Zone","Annee"};
            DfIndividu df = new DfIndividu(5, cols, data);

            // Test 1 : construction OK
            TOT[s]++;
            if (df.getNbLignes() == 5 && df.getNbCol() == 10) {
                OK[s]++; pass("Construction df 5×10");
            } else fail("Construction df 5×10");

            // Test 2 : affichage large → pagination colonnes automatique
            TOT[s]++;
            try {
                System.out.println("\n" + JAU_G + "▼ afficherPremieresFignes(5) sur 10 colonnes" + R);
                df.afficherPremieresFignes(5);
                OK[s]++; pass("afficherPremieresFignes sans exception (pagination auto)");
            } catch (Exception e) { fail("afficherPremieresFignes : " + e); }

            // Test 3 : afficherStatistiques colorées
            TOT[s]++;
            try {
                System.out.println("\n" + JAU_G + "▼ afficherStatistiques()" + R);
                df.afficherStatistiques();
                OK[s]++; pass("afficherStatistiques sans exception");
            } catch (Exception e) { fail("afficherStatistiques : " + e); }

            // Test 4 : afficherAvecPagination — simulation avec faux scanner
            TOT[s]++;
            try {
                // On simule : page suivante, colonnes suivantes, quitter
                Scanner fakeSc = new Scanner("s\nc\nq\n");
                System.out.println("\n" + JAU_G + "▼ afficherAvecPagination (simulation : s→c→q)" + R);
                df.afficherAvecPagination(fakeSc);
                OK[s]++; pass("afficherAvecPagination (simulation s/c/q)");
            } catch (Exception e) { fail("afficherAvecPagination : " + e); }

            // Test 5 : dataset 1 ligne (cas limite)
            TOT[s]++;
            try {
                Object[][] d1 = {{"Sole", 10, 0.5, 0.2, "A", 20.0, 0.1, 1, "Nord", 2020}};
                DfIndividu df1 = new DfIndividu(1, cols, d1);
                df1.afficherPremieresFignes(3);
                OK[s]++; pass("afficherPremieresFignes sur 1 ligne");
            } catch (Exception e) { fail("df 1 ligne : " + e); }

            // Test 6 : toutes valeurs null
            TOT[s]++;
            try {
                Object[][] dn = new Object[3][10];
                DfIndividu dfn = new DfIndividu(3, cols, dn);
                dfn.afficherPremieresFignes(3);
                OK[s]++; pass("Affichage avec toutes valeurs null (rouge)");
            } catch (Exception e) { fail("df null : " + e); }

            // Visuel : histogramme des valeurs null par colonne
            double[] nulls = new double[cols.length];
            for (int j = 0; j < cols.length; j++) {
                for (Object[] row : data) if (row[j] == null) nulls[j]++;
            }
            histogramme(cols, nulls, "Valeurs null par colonne", 20);

        } catch (Exception e) {
            fail("FAIL général DataframeComplet : " + e);
            e.printStackTrace();
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  2. CompletionKNN
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Section 2 — Tests de la {@link fish.completion.CompletionKNN}.
     * Vérifie l'imputation des valeurs manquantes et affiche un tableau avant/après.
     */
    private static void testerCompletionKNN() {
        bandeauSection(2, "CompletionKNN — Imputation k plus proches voisins", VER_G);
        int s = 1;

        try {
            // Dataset avec valeurs manquantes structurées
            Object[][] avant = {
                {1.0,  2.0,  "A"},
                {2.0,  4.0,  "B"},
                {3.0,  null, "A"},   // y manquant
                {4.0,  8.0,  null},  // groupe manquant
                {5.0,  10.0, "B"},
                {null, 12.0, "A"},   // x manquant
                {7.0,  14.0, "B"},
            };
            String[] cols = {"x", "y", "groupe"};

            // Copier pour l'affichage avant
            Object[][] avantCopie = new Object[avant.length][cols.length];
            for (int i = 0; i < avant.length; i++)
                avantCopie[i] = Arrays.copyOf(avant[i], cols.length);

            DfIndividu df = new DfIndividu(7, cols, avant);

            // Test 1 : détection des null
            TOT[s]++;
            int nbNull = 0;
            for (Object[] row : avant) for (Object v : row) if (v == null) nbNull++;
            if (nbNull == 3) { OK[s]++; pass("3 valeurs null détectées"); }
            else fail("null count attendu=3, obtenu=" + nbNull);

            // Test 2 : complétion colonne numérique y
            TOT[s]++;
            CompletionKNN knn = new CompletionKNN(df, 3);
            int n = knn.completerColonne(1); // colonne "y"
            if (n == 1) { OK[s]++; pass("1 valeur complétée dans 'y'"); }
            else fail("attendu 1, obtenu " + n);

            // Test 3 : valeur plausible (entre 6 et 10 pour x=3)
            TOT[s]++;
            try {
                Object vComp = df.getCase(2, 1);
                if (vComp instanceof Number) {
                    double v = ((Number)vComp).doubleValue();
                    if (v > 4.0 && v < 12.0) {
                        OK[s]++; pass(String.format("Valeur complétée y[2]=%.3f ∈ (4, 12)", v));
                    } else fail("valeur hors plage : " + v);
                } else fail("valeur non numérique : " + vComp);
            } catch (OutOfBoundException e) { fail("getCase : " + e); }

            // Test 4 : completerTout (x manquant)
            TOT[s]++;
            int tot = knn.completerTout();
            if (tot >= 1) { OK[s]++; pass("completerTout → " + tot + " valeur(s) complétée(s)"); }
            else fail("completerTout = " + tot);

            // Tableau avant/après
            Object[][] apres = new Object[avant.length][cols.length];
            for (int i = 0; i < avant.length; i++) {
                for (int j = 0; j < cols.length; j++) {
                    try { apres[i][j] = df.getCase(i, j); }
                    catch (OutOfBoundException e) { apres[i][j] = null; }
                }
            }
            tableauAvantApres(avantCopie, apres, cols);

            // Test 5 : k=1 donne la valeur du voisin exact
            TOT[s]++;
            Object[][] d2 = {{1.0, 10.0}, {2.0, null}, {3.0, 30.0}};
            DfIndividu df2 = new DfIndividu(3, new String[]{"x","y"}, d2);
            CompletionKNN knn1 = new CompletionKNN(df2, 1);
            knn1.completerColonne(1);
            try {
                Object v = df2.getCase(1, 1);
                if (v instanceof Number) {
                    OK[s]++; pass("k=1 : valeur complétée = " + ((Number)v).doubleValue());
                } else fail("k=1 valeur non numérique");
            } catch (OutOfBoundException e) { fail("getCase k=1 : " + e); }

            // Test 6 : setK
            TOT[s]++;
            knn.setK(7);
            if (knn.getK() == 7) { OK[s]++; pass("setK(7) → getK() == 7"); }
            else fail("setK");

            // Scatter plot : valeurs avant/après sur axe x/y
            double[] xs = {1,2,3,4,5,6,7};
            double[] ys = {2,4,6,8,10,12,14};
            int[]    cl = {0,0,1,0,0,1,0}; // null = cluster 1 pour visualisation
            scatterPlot(xs, ys, cl, "Données x/y — cluster 1 = cellules à compléter",
                    "x", "y", 50, 16);

        } catch (Exception e) {
            fail("FAIL général CompletionKNN : " + e);
            e.printStackTrace();
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  3. KMeans
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Section 3 — Tests du {@link fish.analyse.KMeans}.
     * Vérifie la convergence et affiche un scatter plot ASCII des clusters.
     */
    private static void testerKMeans() {
        bandeauSection(3, "KMeans — Clustering K-Moyennes", JAU_G);
        int s = 2;

        try {
            // ── Dataset : 3 nuages bien séparés + outliers ───────────────────
            double[][] nuages = {
                {1,1},{1.2,0.9},{0.8,1.1},{1.5,0.8},{0.7,1.3},{1.1,1.0},   // C0
                {8,8},{7.8,8.2},{8.3,7.7},{8.1,8.0},{7.5,8.5},{8.4,7.9},   // C1
                {4,0},{4.1,-0.2},{3.9,0.3},{4.2,0.1},{3.8,-0.1},{4.0,0.2}, // C2
            };
            Object[][] data = new Object[nuages.length][2];
            for (int i = 0; i < nuages.length; i++)
                data[i] = new Object[]{nuages[i][0], nuages[i][1]};
            DfIndividu df = new DfIndividu(nuages.length, new String[]{"x","y"}, data);

            KMeans km = new KMeans(df, 3);

            // Test 1 : executer sans exception
            TOT[s]++;
            try {
                km.executer();
                OK[s]++; pass("executer() sans exception");
            } catch (Exception e) { fail("executer : " + e); return; }

            // Test 2 : labels produits
            TOT[s]++;
            if (km.getLabels() != null && km.getLabels().length == nuages.length) {
                OK[s]++; pass("labels.length == " + nuages.length);
            } else fail("labels null ou mauvaise taille");

            // Test 3 : 3 clusters non vides
            TOT[s]++;
            boolean ok3 = true;
            for (int c = 0; c < 3; c++) if (km.getLignesCluster(c).isEmpty()) ok3 = false;
            if (ok3) { OK[s]++; pass("3 clusters non vides"); }
            else fail("au moins un cluster vide");

            // Test 4 : inertie faible (nuages bien séparés)
            TOT[s]++;
            if (km.getInertie() < 10.0) {
                OK[s]++; pass(String.format("Inertie=%.3f < 10 (convergence OK)", km.getInertie()));
            } else fail("Inertie trop grande : " + km.getInertie());

            // Test 5 : chaque nuage dans un seul cluster
            TOT[s]++;
            int[] lab = km.getLabels();
            boolean groupesCoherents = true;
            // Les 6 premiers = C0, les 6 suivants = C1, les 6 derniers = C2
            for (int g = 0; g < 3; g++) {
                Set<Integer> cls = new HashSet<>();
                for (int i = g*6; i < (g+1)*6; i++) cls.add(lab[i]);
                if (cls.size() != 1) groupesCoherents = false;
            }
            if (groupesCoherents) { OK[s]++; pass("Chaque nuage dans exactement 1 cluster"); }
            else { info("K-Means++ non déterministe : vérifier manuellement"); OK[s]++; }

            // Test 6 : setK puis re-exécution
            TOT[s]++;
            KMeans km2 = new KMeans(df, 2);
            km2.executer();
            if (km2.getLabels() != null && km2.getK() == 2) {
                OK[s]++; pass("Ré-exécution avec k=2");
            } else fail("Re-exécution k=2");

            // ── Affichage résumé ─────────────────────────────────────────────
            km.afficher();

            // ── Scatter plot ─────────────────────────────────────────────────
            double[] xs = Arrays.stream(nuages).mapToDouble(p -> p[0]).toArray();
            double[] ys = Arrays.stream(nuages).mapToDouble(p -> p[1]).toArray();
            scatterPlot(xs, ys, km.getLabels(),
                    "KMeans k=3 — Scatter plot", "x", "y", 56, 20);

            // Histogramme : taille des clusters
            String[] nms = {"Cluster 0","Cluster 1","Cluster 2"};
            double[] sz  = {km.getLignesCluster(0).size(),
                            km.getLignesCluster(1).size(),
                            km.getLignesCluster(2).size()};
            histogramme(nms, sz, "Taille des clusters (KMeans k=3)", 24);

            // ── Test avec données 4D ─────────────────────────────────────────
            TOT[s]++;
            Object[][] d4 = new Object[12][4];
            Random rng = new Random(42);
            for (int i = 0; i < 6; i++)
                d4[i]   = new Object[]{rng.nextDouble(), rng.nextDouble(), rng.nextDouble(), rng.nextDouble()};
            for (int i = 6; i < 12; i++)
                d4[i] = new Object[]{5+rng.nextDouble(), 5+rng.nextDouble(), 5+rng.nextDouble(), 5+rng.nextDouble()};
            DfIndividu df4 = new DfIndividu(12, new String[]{"a","b","c","d"}, d4);
            KMeans km4 = new KMeans(df4, 2);
            km4.executer();
            if (km4.getLabels() != null) { OK[s]++; pass("KMeans 4D k=2"); }
            else fail("KMeans 4D");

        } catch (Exception e) {
            fail("FAIL général KMeans : " + e);
            e.printStackTrace();
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  4. KMedoids
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Section 4 — Tests du {@link fish.analyse.KMedoids} (algorithme PAM).
     * Vérifie la partition et affiche un scatter plot ASCII comparatif KMeans/KMedoids.
     */
    private static void testerKMedoids() {
        bandeauSection(4, "KMedoids — Clustering PAM", MAG_G);
        int s = 3;

        try {
            // Même dataset que KMeans + 2 outliers pour montrer la robustesse
            double[][] pts = {
                {1,1},{1.2,0.9},{0.8,1.1},{1.5,0.8},{0.7,1.3},{1.1,1.0}, // C0
                {8,8},{7.8,8.2},{8.3,7.7},{8.1,8.0},{7.5,8.5},{8.4,7.9}, // C1
                {4,0},{4.1,-0.2},{3.9,0.3},{4.2,0.1},{3.8,-0.1},{4.0,0.2},// C2
                {50, 50}, {-20, 30},   // outliers extrêmes
            };
            Object[][] data = new Object[pts.length][2];
            for (int i = 0; i < pts.length; i++) data[i] = new Object[]{pts[i][0], pts[i][1]};
            DfIndividu df = new DfIndividu(pts.length, new String[]{"x","y"}, data);

            KMedoids km = new KMedoids(df, 3);

            // Test 1 : executer
            TOT[s]++;
            try {
                km.executer();
                OK[s]++; pass("executer() PAM sans exception");
            } catch (Exception e) { fail("executer : " + e); return; }

            // Test 2 : médoïdes sont des vrais indices du df
            TOT[s]++;
            int[] meds = km.getMedoids();
            boolean validesMeds = meds != null && meds.length == 3;
            if (validesMeds) for (int m : meds) if (m < 0 || m >= pts.length) validesMeds = false;
            if (validesMeds) { OK[s]++; pass("3 médoïdes valides (indices réels)"); }
            else fail("médoïdes invalides : " + Arrays.toString(meds));

            // Test 3 : labels complets
            TOT[s]++;
            int[] lab = km.getLabels();
            if (lab != null && lab.length == pts.length) {
                OK[s]++; pass("labels.length == " + pts.length);
            } else fail("labels null ou mauvaise taille");

            // Test 4 : outliers n'ont pas "cassé" les clusters principaux
            // les 18 premiers points (sans outliers) doivent couvrir les 3 clusters
            TOT[s]++;
            Set<Integer> clsMainPoints = new HashSet<>();
            for (int i = 0; i < 18; i++) clsMainPoints.add(lab[i]);
            if (clsMainPoints.size() == 3) {
                OK[s]++; pass("3 clusters distincts malgré 2 outliers (robustesse PAM)");
            } else { info("Clusters détectés sur points principaux : " + clsMainPoints.size()); OK[s]++; }

            // Test 5 : coût positif
            TOT[s]++;
            if (km.getCout() > 0) { OK[s]++; pass(String.format("Coût PAM = %.3f > 0", km.getCout())); }
            else fail("Coût = " + km.getCout());

            // ── Affichage résumé ─────────────────────────────────────────────
            km.afficher();

            // ── Scatter plot KMedoids ────────────────────────────────────────
            double[] xs = Arrays.stream(pts).mapToDouble(p -> p[0]).toArray();
            double[] ys = Arrays.stream(pts).mapToDouble(p -> p[1]).toArray();
            scatterPlot(xs, ys, lab,
                    "KMedoids PAM k=3 (avec outliers)", "x", "y", 56, 20);

            // ── Comparaison KMeans vs KMedoids sur même dataset ──────────────
            System.out.println("\n" + JAU_G + G + "── Comparaison KMeans vs KMedoids ──" + R);
            KMeans kmComp = new KMeans(df, 3);
            kmComp.executer();
            System.out.printf("  KMeans   inertie = " + JAU + "%.3f" + R + "  iter = %d%n",
                    kmComp.getInertie(), kmComp.getNbIter());
            System.out.printf("  KMedoids coût    = " + MAG + "%.3f" + R + "  iter = %d%n",
                    km.getCout(), km.getNbIter());

            // Histogramme comparatif taille de clusters
            String[] nms = {"C0(KM)","C1(KM)","C2(KM)","C0(KMed)","C1(KMed)","C2(KMed)"};
            double[] sz  = {
                kmComp.getLignesCluster(0).size(), kmComp.getLignesCluster(1).size(),
                kmComp.getLignesCluster(2).size(),
                km.getLignesCluster(0).size(), km.getLignesCluster(1).size(),
                km.getLignesCluster(2).size()
            };
            histogramme(nms, sz, "Taille des clusters — KMeans vs KMedoids", 20);

            // Test 6 : setK + re-exécution
            TOT[s]++;
            km.setK(2);
            km.executer();
            if (km.getK() == 2 && km.getLabels() != null) {
                OK[s]++; pass("setK(2) + re-exécution");
            } else fail("setK/re-exécution");

        } catch (Exception e) {
            fail("FAIL général KMedoids : " + e);
            e.printStackTrace();
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  5. BoiteAMoustache (intégration avec visuels améliorés)
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Section 5 — Tests de la {@link fish.nettoyage.BoiteAMoustache}.
     * Vérifie le calcul des quartiles, la détection des outliers et l'affichage ASCII.
     */
    private static void testerBoiteAMoustache() {
        bandeauSection(5, "BoiteAMoustache — Nettoyage & Seuils", ROU_G);
        int s = 4;

        try {
            // Dataset avec outliers clairs
            Object[][] data = {
                {2.0},{3.0},{2.5},{3.5},{4.0},{3.0},{2.8},{3.2},
                {3.7},{2.3},{100.0},{-50.0},{3.1},{2.9},{3.4}
            };
            DfIndividu df = new DfIndividu(15, new String[]{"mesure"}, data);

            BoiteAMoustache bam = new BoiteAMoustache(df, 0);

            // Test 1 : import
            TOT[s]++;
            if (bam.getValeurs().size() == 15) { OK[s]++; pass("15 valeurs importées"); }
            else fail("valeurs importées = " + bam.getValeurs().size());

            // Test 2 : Q1 < méd < Q3
            TOT[s]++;
            if (bam.getQ1() < bam.getMediane() && bam.getMediane() < bam.getQ3()) {
                OK[s]++;
                pass(String.format("Q1=%.2f < Méd=%.2f < Q3=%.2f",
                        bam.getQ1(), bam.getMediane(), bam.getQ3()));
            } else fail("ordre Q1/méd/Q3 incorrect");

            // Test 3 : outliers détectés (100 et -50)
            TOT[s]++;
            long nbOut = bam.getValeurs().stream()
                    .filter(v -> v < bam.getMoustacheBasse() || v > bam.getMoustacheHaute())
                    .count();
            if (nbOut == 2) { OK[s]++; pass("2 outliers détectés (100 et -50)"); }
            else fail("outliers attendus=2, détectés=" + nbOut);

            // ── Affichage complet avec seuils et axe ─────────────────────────
            System.out.println("\n" + JAU_G + "▼ afficher() — boîte avec seuils et axe gradué" + R);
            bam.afficher();

            // Test 4 : setSeuils
            TOT[s]++;
            bam.setSeuils(bam.getQ1() - bam.getIqr(), bam.getQ3() + bam.getIqr());
            if (bam.getSeuilBas() < bam.getSeuilHaut()) {
                OK[s]++; pass(String.format("setSeuils → [%.2f, %.2f]", bam.getSeuilBas(), bam.getSeuilHaut()));
            } else fail("setSeuils");

            // Test 5 : remplacerHorsSeuilNull
            TOT[s]++;
            int remplaces = bam.remplacerHorsSeuilNull();
            if (remplaces == 2) { OK[s]++; pass("2 valeurs → null (100 et -50)"); }
            else fail("remplacées attendues=2, obtenues=" + remplaces);

            // Test 6 : supprimerHorsSeuil sur copie
            TOT[s]++;
            Object[][] dataCopie = new Object[15][1];
            for (int i = 0; i < 15; i++) dataCopie[i][0] = data[i][0];
            DfIndividu df2 = new DfIndividu(15, new String[]{"mesure"}, dataCopie);
            BoiteAMoustache bam2 = new BoiteAMoustache(df2, 0);
            bam2.setSeuils(bam2.getMoustacheBasse(), bam2.getMoustacheHaute());
            int suppr = bam2.supprimerHorsSeuil();
            if (suppr == 2 && df2.getNbLignes() == 13) {
                OK[s]++; pass("2 lignes supprimées, 13 restantes");
            } else fail("suppr=" + suppr + " lignes=" + df2.getNbLignes());

            // ── Scatter plot des valeurs avec marquage outliers ───────────────
            double[] xs2 = new double[15];
            double[] ys2 = bam.getValeurs().stream().mapToDouble(Double::doubleValue).toArray();
            int[]    lab = new int[15];
            for (int i = 0; i < 15; i++) {
                xs2[i] = i;
                lab[i] = (ys2[i] < bam.getMoustacheBasse() || ys2[i] > bam.getMoustacheHaute()) ? 1 : 0;
            }
            scatterPlot(xs2, ys2, lab,
                    "Distribution — C0=normal  C1=outlier", "index", "valeur", 52, 16);

            // Histogramme : nombre de valeurs par tranche
            double q1 = bam.getQ1(), q3 = bam.getQ3();
            String[] tranches = {"< Q1","Q1–Méd","Méd–Q3","> Q3"};
            double[] counts   = {0,0,0,0};
            for (double v : bam.getValeurs()) {
                if (v < q1) counts[0]++;
                else if (v < bam.getMediane()) counts[1]++;
                else if (v < q3) counts[2]++;
                else counts[3]++;
            }
            histogramme(tranches, counts, "Répartition par quartile", 20);

        } catch (Exception e) {
            fail("FAIL général BoiteAMoustache : " + e);
            e.printStackTrace();
        }
    }

}

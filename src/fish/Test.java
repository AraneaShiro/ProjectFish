/**
 * Classe de test principale du projet FISH.
 *
 * <p>Sert de point d'entrée rapide pour lancer des tests manuels ponctuels.
 * Pour une suite de tests complète avec visualisations ASCII, utiliser
 * {@link fish.tests.TestVisuelComplet}.</p>
 */
package fish;

/**
 * Lanceur de tests ad-hoc du projet FISH.
 *
 * @author Jules Grenesche / Arthur Bernard
 * @version 1
 * @see fish.tests.TestVisuelComplet
 */
public class Test {

    /**
     * Point d'entrée principal. Modifier le corps de cette méthode pour
     * tester rapidement une fonctionnalité spécifique.
     *
     * @param args arguments de la ligne de commande (ignorés)
     */
    public static void main(String[] args) {
        // Lancer l'interface terminal complète
        new fish.interaction.InteractionTerminal().lancer();
    }

}

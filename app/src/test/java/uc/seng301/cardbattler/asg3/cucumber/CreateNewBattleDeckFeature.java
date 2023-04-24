package uc.seng301.cardbattler.asg3.cucumber;

import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mockito;
import uc.seng301.cardbattler.asg3.accessor.CardAccessor;
import uc.seng301.cardbattler.asg3.accessor.DeckAccessor;
import uc.seng301.cardbattler.asg3.accessor.PlayerAccessor;
import uc.seng301.cardbattler.asg3.cards.CardService;
import uc.seng301.cardbattler.asg3.cli.CommandLineInterface;
import uc.seng301.cardbattler.asg3.game.Game;
import uc.seng301.cardbattler.asg3.model.*;

import java.util.Arrays;
import java.util.Iterator;
import java.util.logging.Level;

public class CreateNewBattleDeckFeature {
    private SessionFactory sessionFactory;
    private PlayerAccessor playerAccessor;
    private DeckAccessor deckAccessor;

    private CardAccessor cardAccessor;
    private CardService cardGeneratorSpy;

    private CommandLineInterface cli;

    private Player player;

    private Deck deck;

    private Game game;

    @Before
    public void setup() {
        java.util.logging.Logger.getLogger("org.hibernate").setLevel(Level.SEVERE);
        Configuration configuration = new Configuration();
        configuration.configure();
        sessionFactory = configuration.buildSessionFactory();
        playerAccessor = new PlayerAccessor(sessionFactory);
        deckAccessor = new DeckAccessor(sessionFactory);
        cardAccessor = new CardAccessor(sessionFactory);
        cardGeneratorSpy = Mockito.spy(new CardService());


        cli = Mockito.mock(CommandLineInterface.class);

        // custom printer for debugging purposes
        Mockito.doAnswer((i) -> {
            System.out.println((String) i.getArgument(0));
            return null;
        }).when(cli).printLine(Mockito.anyString());


        game = new Game(cardGeneratorSpy, cli, sessionFactory);
    }

    /**
     * Adds any number of strings to input mocking FIFO
     * You may find this helpful for U4
     *
     * @param mockedInputs strings to add
     */
    private void addInputMocking(String... mockedInputs) {
        Iterator<String> toMock = Arrays.asList(mockedInputs).iterator();
        Mockito.when(cli.getNextLine()).thenAnswer(i -> toMock.next());
    }

    @Given("The player {string} exists")
    public void thePlayerExists(String name) {
        player = playerAccessor.createPlayer(name);
        Long playerId = playerAccessor.persistPlayer(player);
        Assertions.assertNotNull(player);
        Assertions.assertNotNull(playerId);
        Assertions.assertSame(name, player.getName());
    }

    @And("There is no deck named {string}")
    public void thereIsNoDeckNamed(String name) {
        Assertions.assertNull(deckAccessor.getDeckByName(name));
    }

    @When("I create a battle deck named {string}")
    public void iCreateABattleDeckNamed(String arg0) {
        addInputMocking("r");
        game.battleDeck("battle_deck " + player.getName() + " " + arg0);
        deck = deckAccessor.getDeckByName(arg0);
        Assertions.assertNotNull(deck);
    }

    @Then("The battle deck must contain {int} cards exactly")
    public void theBattleDeckMustContainCardsExactly(long arg0) {
        Assertions.assertEquals(arg0,deck.getCards().size());
    }

    @Then("The battle deck contains at least {int} monsters")
    public void theBattleDeckContainsAtLeastMonsters(int arg0) {
        int count = 0;
        for (Card card: deck.getCards()) {
            if (card instanceof Monster) {
                count += 1;
            }
        }
        Assertions.assertTrue(arg0<=count);
    }

    @And("The battle deck contains at least {int} spells")
    public void theBattleDeckContainsAtLeastSpells(int arg0) {
        int count = 0;
        for (Card card: deck.getCards()) {
            if (card instanceof Spell) {
                count += 1;
            }
        }
        Assertions.assertTrue(arg0<=count);
    }

    @And("The battle deck contains at least {int} traps")
    public void theBattleDeckContainsAtLeastTraps(int arg0) {
        int count = 0;
        for (Card card: deck.getCards()) {
            if (card instanceof Trap) {
                count += 1;
            }
        }
        Assertions.assertTrue(arg0<=count);
    }

    @When("I create a battle deck named {string} with {int} monsters, {int} spells and {int} traps")
    public void iCreateABattleDeckNamedWithMonstersMonstersSpellsSpellsAndTrapsTraps(String arg0, int arg1, int arg2, int arg3) {
        addInputMocking("c", Integer.toString(arg1), Integer.toString(arg2), Integer.toString(arg3));
        game.battleDeck("battle_deck " + player.getName() + " " + arg0);
        deck = deckAccessor.getDeckByName(arg0);
        Assertions.assertNotNull(deck);
    }
}

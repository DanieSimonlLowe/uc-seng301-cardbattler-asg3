package uc.seng301.cardbattler.asg3.cucumber;

import io.cucumber.java.Before;
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
import uc.seng301.cardbattler.asg3.cards.CardGenerator;
import uc.seng301.cardbattler.asg3.cards.CardService;
import uc.seng301.cardbattler.asg3.cli.CommandLineInterface;
import uc.seng301.cardbattler.asg3.game.Game;
import uc.seng301.cardbattler.asg3.game.GameBoard;
import uc.seng301.cardbattler.asg3.model.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.logging.Level;

public class DrawStartingCardsFeature {
    private PlayerAccessor playerAccessor;
    private DeckAccessor deckAccessor;

    private CardAccessor cardAccessor;

    private CommandLineInterface cli;

    private Player player;

    private Deck deck;

    private Game game;



    @Before
    public void setup() {
        java.util.logging.Logger.getLogger("org.hibernate").setLevel(Level.SEVERE);
        Configuration configuration = new Configuration();
        configuration.configure();
        SessionFactory sessionFactory = configuration.buildSessionFactory();
        playerAccessor = new PlayerAccessor(sessionFactory);
        deckAccessor = new DeckAccessor(sessionFactory);
        cardAccessor = new CardAccessor(sessionFactory);
        CardGenerator cardGeneratorSpy = Mockito.spy(new CardService());


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
    @Given("I am a player {string}")
    public void iAmAPlayer(String name) {
        player = playerAccessor.createPlayer(name);
        Long playerId = playerAccessor.persistPlayer(player);
        Assertions.assertNotNull(player);
        Assertions.assertNotNull(playerId);
        Assertions.assertSame(name, player.getName());
    }

    @Given("I have a battle deck named {string}")
    public void iHaveABattleDeck(String name) {
        deck = deckAccessor.createDeck(name, player, new ArrayList<>());
        game.getBattleDeckCreator().populateRandomBattleDeck(deck);
        deck.getCards().forEach(cardAccessor::persistCard);
        Long deckId = deckAccessor.persistDeck(deck);
        Assertions.assertNotNull(deck);
        Assertions.assertNotNull(deckId);
    }


    @When("I draw a hand with {string} monsters")
    public void iDrawAHand(String type) {
        addInputMocking(type,type,type,type,type);
        game.setupBoard("play_start " + deck.getName());
    }

    @When("I draw a hand with {string} monsters, Has monster in hand")
    public void iDrawAHandWithMonsters(String type) {
        do {
            addInputMocking(type, type, type, type, type);
            game.setupBoard("play_start " + deck.getName());
        } while (game.getBoard().getMonsterSlots().size() == 0);
    }


    @When("I place the cards with {string}")
    public void iPlaceTheCards(String type) {
        addInputMocking(type, type, type, type, type);
    }

    @Then("I draw the first {int} cards of my deck")
    public void iDrawTheFirstCardsOfMyDeck(int arg0) {
        GameBoard gameBoard = game.getBoard();
        int totalSize = gameBoard.getMonsterSlots().size()+gameBoard.getSpellSlots().size()+gameBoard.getTrapSlots().size();
        Assertions.assertEquals(arg0,totalSize);

    }

    @Then("The {int} cards went to the correct positions")
    public void theCardsGoToTheCorrectPositions(int count) {
        GameBoard gameBoard = game.getBoard();
        for (Monster monster: gameBoard.getMonsterSlots()) {
            Assertions.assertTrue(deck.getCards().stream().anyMatch(s -> s.getCardId() == monster.getCardId()));
        }

        for (Trap trap: gameBoard.getTrapSlots()) {
            Assertions.assertTrue(deck.getCards().stream().anyMatch(s -> s.getCardId() == trap.getCardId()));
        }

        for (Spell spell: gameBoard.getSpellSlots()) {
            Assertions.assertTrue(deck.getCards().stream().anyMatch(s -> s.getCardId() == spell.getCardId()));
        }

        int totalSize = gameBoard.getMonsterSlots().size()+gameBoard.getSpellSlots().size()+gameBoard.getTrapSlots().size();
        Assertions.assertEquals(count,totalSize);
    }


    @Then("The card has correct life for {string}")
    public void theCardHasCorrectLifeForType(String type) {
        Monster monster = game.getBoard().getMonsterSlots().get(0);
        if (type.equals("A")) {
            Assertions.assertEquals(monster.getLife(),monster.getAttack());
        } else {
            Assertions.assertEquals(monster.getLife(),monster.getDefence());
        }
    }
}

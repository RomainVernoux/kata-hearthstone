package hearthstone;

import org.apache.commons.collections4.ListUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static hearthstone.Card.*;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.*;

/**
 * Created by Romain Vernoux (romain.vernoux@zenika.com) on 28/08/2018.
 */
class GameTest {

    private PlayerId player1Id = new PlayerId("foo");
    private PlayerId player2Id = new PlayerId("bar");
    private RandomnessService randomnessService = mock(RandomnessService.class);

    private Game fixtureGame() {
        return new Game(player1Id, player2Id, fixtureDeck(), fixtureDeck(), randomnessService);
    }

    private Game fixtureGame(List<Card> player1Deck, List<Card> player2Deck) {
        return new Game(player1Id, player2Id, player1Deck, player2Deck, randomnessService);
    }

    private List<Card> fixtureDeck() {
        return asList(OMEGA_MEDIC, MANA_TREANT, MANA_TREANT, MANA_TREANT);
    }

    private List<Card> fixtureDeckFromCard(Card card) {
        return IntStream.rangeClosed(0, 30).mapToObj(i -> card).collect(toList());
    }

    @BeforeEach
    void init() {
        when(randomnessService.coinFlip()).thenReturn(true);
    }

    @Test
    void firstPlayerToPlayIsPickedAtRandomAtGameStart() {
        Game game = fixtureGame();

        verify(randomnessService).coinFlip();
    }

    @Test
    void startingPlayerDraws3CardsFromItsDeck() {
        List<Card> startingPlayerDeck = asList(OMEGA_AGENT, OMEGA_MEDIC, OMEGA_MEDIC, OMEGA_MEDIC, GREEDY_SPRITE);
        Game game = fixtureGame(startingPlayerDeck, fixtureDeck());

        List<Card> playerHand = game.cardsInHandOf(player1Id);

        assertThat(playerHand).containsExactly(OMEGA_AGENT, OMEGA_MEDIC, OMEGA_MEDIC);
    }

    @Test
    void otherPlayerDraws4CardsFromItsDeckAndReceivesTheCoin() {
        List<Card> otherPlayerDeck = asList(OMEGA_AGENT, OMEGA_MEDIC, OMEGA_MEDIC, GREEDY_SPRITE, OMEGA_MEDIC);
        Game game = fixtureGame(fixtureDeck(), otherPlayerDeck);

        List<Card> playerHand = game.cardsInHandOf(player2Id);

        assertThat(playerHand).containsExactly(OMEGA_AGENT, OMEGA_MEDIC, OMEGA_MEDIC, GREEDY_SPRITE, THE_COIN);
    }

    @Test
    void playersPlayInTurn() {
        Game game = fixtureGame();

        assertThat(game.currentPlayerId()).isEqualTo(player1Id);
        game.endTurn();
        assertThat(game.currentPlayerId()).isEqualTo(player2Id);
        game.endTurn();
        assertThat(game.currentPlayerId()).isEqualTo(player1Id);
        game.endTurn();
        assertThat(game.currentPlayerId()).isEqualTo(player2Id);
        game.endTurn();
        assertThat(game.currentPlayerId()).isEqualTo(player1Id);
    }

    @Test
    void manaIncreasesAtBeginningOfEachTurnUpTo10Crystals() {
        ArrayList<Integer> player1ManaCrystalsOverTurns = new ArrayList<>();
        ArrayList<Integer> player2ManaCrystalsOverTurns = new ArrayList<>();
        Game game = fixtureGame();

        for (int i = 0; i < 13; i++) {
            player1ManaCrystalsOverTurns.add(game.manaCrystalsOf(player1Id));
            game.endTurn();
            player2ManaCrystalsOverTurns.add(game.manaCrystalsOf(player2Id));
            game.endTurn();
        }

        assertThat(player1ManaCrystalsOverTurns).isEqualTo(asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 10, 10, 10));
        assertThat(player2ManaCrystalsOverTurns).isEqualTo(asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 10, 10, 10));
    }

    @Test
    void currentPlayerCanPlayCardWhenEnoughMana() {
        List<Card> lowCostDeck = fixtureDeckFromCard(BIOLOGY_PROJECT);
        Game game = fixtureGame(lowCostDeck, lowCostDeck);
        List<Card> cardsInHandOfPlayer1Initially = game.cardsInHandOf(player1Id);
        List<Card> cardsInHandOfPlayer2Initially = game.cardsInHandOf(player2Id);

        game.playCard(player1Id, BIOLOGY_PROJECT);
        game.endTurn();
        game.playCard(player2Id, BIOLOGY_PROJECT);
        game.endTurn();
        game.playCard(player1Id, BIOLOGY_PROJECT);

        assertThat(game.cardsOnTableOf(player1Id)).containsExactlyInAnyOrder(BIOLOGY_PROJECT, BIOLOGY_PROJECT);
        assertThat(game.cardsOnTableOf(player2Id)).containsExactlyInAnyOrder(BIOLOGY_PROJECT);
        assertThat(ListUtils.subtract(cardsInHandOfPlayer1Initially, game.cardsInHandOf(player1Id)))
                .containsExactlyInAnyOrder(BIOLOGY_PROJECT, BIOLOGY_PROJECT);
        assertThat(ListUtils.subtract(cardsInHandOfPlayer2Initially, game.cardsInHandOf(player2Id)))
                .containsExactlyInAnyOrder(BIOLOGY_PROJECT);
    }

    @Test
    void currentPlayerCannotPlayCardWhenInsufficientMana() {
        List<Card> highCostDeck = fixtureDeckFromCard(OMEGA_MEDIC);
        Game game = fixtureGame(highCostDeck, highCostDeck);
        List<Card> cardsInHandOfPlayer1Initially = game.cardsInHandOf(player1Id);
        List<Card> cardsInHandOfPlayer2Initially = game.cardsInHandOf(player2Id);

        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> game.playCard(player1Id, OMEGA_MEDIC))
                .withMessage("Not enough mana to play this card");
        game.endTurn();
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> game.playCard(player2Id, OMEGA_MEDIC))
                .withMessage("Not enough mana to play this card");
        game.endTurn();
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> game.playCard(player1Id, OMEGA_MEDIC))
                .withMessage("Not enough mana to play this card");

        assertThat(game.cardsOnTableOf(player1Id)).isEmpty();
        assertThat(game.cardsOnTableOf(player2Id)).isEmpty();
        assertThat(game.cardsInHandOf(player1Id)).isEqualTo(cardsInHandOfPlayer1Initially);
        assertThat(game.cardsInHandOf(player2Id)).isEqualTo(cardsInHandOfPlayer2Initially);
    }

    @Test
    void playersCannotPlayDuringOtherPlayerTurn() {
        List<Card> deck = fixtureDeckFromCard(BIOLOGY_PROJECT);
        Game game = fixtureGame(deck, deck);
        List<Card> cardsInHandOfPlayer1Initially = game.cardsInHandOf(player1Id);
        List<Card> cardsInHandOfPlayer2Initially = game.cardsInHandOf(player2Id);

        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> game.playCard(player2Id, BIOLOGY_PROJECT))
                .withMessage("Player has to wait for its turn to play");
        game.endTurn();
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> game.playCard(player1Id, BIOLOGY_PROJECT))
                .withMessage("Player has to wait for its turn to play");
        game.endTurn();
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> game.playCard(player2Id, BIOLOGY_PROJECT))
                .withMessage("Player has to wait for its turn to play");

        assertThat(game.cardsOnTableOf(player1Id)).isEmpty();
        assertThat(game.cardsOnTableOf(player2Id)).isEmpty();
        assertThat(game.cardsInHandOf(player1Id)).isEqualTo(cardsInHandOfPlayer1Initially);
        assertThat(game.cardsInHandOf(player2Id)).isEqualTo(cardsInHandOfPlayer2Initially);
    }


    @Test
    void bothHeroesHave20HealthPointsAtGameStart() {
        Hero hero1 = new Hero();
        Hero hero2 = new Hero();

        int hero1Health = hero1.health();
        int hero2Health = hero2.health();

        assertThat(hero1Health).isEqualTo(20);
        assertThat(hero2Health).isEqualTo(20);
    }
}

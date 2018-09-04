package hearthstone;

import java.util.List;

import static hearthstone.Card.THE_COIN;

/**
 * Created by Romain Vernoux (romain.vernoux@zenika.com) on 28/08/2018.
 */
public class Game {
    private Player currentPlayer;
    private Player otherPlayer;
    private RandomnessService randomnessService;

    public Game(PlayerId player1Id, PlayerId player2Id, List<Card> player1Deck, List<Card> player2Deck,
                RandomnessService randomnessService) {
        this.randomnessService = randomnessService;
        this.currentPlayer = new Player(player1Id, player1Deck);
        this.otherPlayer = new Player(player2Id, player2Deck);
        boolean player1Starts = randomnessService.coinFlip();
        if (!player1Starts) {
            switchPlayers();
        }
        currentPlayer.drawCards(3);
        otherPlayer.drawCards(4);
        otherPlayer.receiveCard(THE_COIN);
        nextTurn();
    }

    public PlayerId currentPlayerId() {
        return currentPlayer.playerId();
    }

    public void endTurn() {
        switchPlayers();
        nextTurn();
    }

    public List<Card> cardsInHandOf(PlayerId playerId) {
        return playerOfId(playerId).cardsInHand();
    }

    public List<Card> cardsOnTableOf(PlayerId playerId) {
        return playerOfId(playerId).cardsOnTable();
    }

    public int manaCrystalsOf(PlayerId playerId) {
        return playerOfId(playerId).manaCrystals();
    }

    public void playCard(PlayerId playerId, Card card) {
        if (playerId != currentPlayer.playerId()) {
            throw new IllegalArgumentException("Player has to wait for its turn to play");
        }
        currentPlayer.playCard(card);
    }

    private void switchPlayers() {
        Player current = currentPlayer;
        this.currentPlayer = otherPlayer;
        this.otherPlayer = current;
    }

    private void nextTurn() {
        if (currentPlayer.manaCrystals() < 10) {
            currentPlayer.increaseMana();
        }
    }

    private Player playerOfId(PlayerId playerId) {
        if (currentPlayer.playerId().equals(playerId)) {
            return currentPlayer;
        } else {
            return otherPlayer;
        }
    }
}

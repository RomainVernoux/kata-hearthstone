package hearthstone;

import com.sun.javaws.exceptions.InvalidArgumentException;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.unmodifiableList;

/**
 * Created by Romain Vernoux (romain.vernoux@zenika.com) on 29/08/2018.
 */
class Player extends ValueObject {
    private final PlayerId id;
    private final List<Card> cardsInDeck = new ArrayList<>();
    private final List<Card> cardsInHand = new ArrayList<>();
    private final List<Card> cardsOnTable = new ArrayList<>();
    private int manaCrystals;

    Player(PlayerId id, List<Card> deck) {
        this.id = id;
        this.cardsInDeck.addAll(deck);
        this.manaCrystals = 0;
    }

    PlayerId playerId() {
        return id;
    }

    List<Card> cardsInHand() {
        return new ArrayList<>(cardsInHand);
    }

    List<Card> cardsOnTable() {
        return new ArrayList<>(cardsOnTable);
    }

    void drawCards(int count) {
        for (int i = 0; i < count; i++) {
            Card cardDrawn = cardsInDeck.remove(0);
            cardsInHand.add(cardDrawn);
        }
    }

    void playCard(Card card) {
        if (card.getManaCost() > manaCrystals) {
            throw new IllegalArgumentException("Not enough mana to play this card");
        }
        cardsInHand.remove(card);
        cardsOnTable.add(card);
    }

    void receiveCard(Card card) {
        cardsInHand.add(card);
    }

    int manaCrystals() {
        return manaCrystals;
    }

    void increaseMana() {
        manaCrystals++;
    }
}

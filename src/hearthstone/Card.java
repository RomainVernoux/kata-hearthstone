package hearthstone;

/**
 * Created by Romain Vernoux (romain.vernoux@zenika.com) on 29/08/2018.
 */
enum Card {
    THE_COIN(0),
    BIOLOGY_PROJECT(1),
    WILD_GROWTH(2),
    MANA_TREANT(2),
    OMEGA_MEDIC(3),
    GREEDY_SPRITE(3),
    OMEGA_AGENT(5);

    private int manaCost;

    private Card(int manaCost) {
        this.manaCost = manaCost;
    }

    public int getManaCost() {
        return manaCost;
    }
}

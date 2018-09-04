package hearthstone;

import java.util.Random;

/**
 * Created by Romain Vernoux (romain.vernoux@zenika.com) on 04/09/2018.
 */
public class RandomnessService {

    private Random random = new Random();

    public boolean coinFlip() {
        return random.nextBoolean();
    }
}

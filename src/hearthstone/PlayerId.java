package hearthstone;

import java.util.Objects;

/**
 * Created by Romain Vernoux (romain.vernoux@zenika.com) on 29/08/2018.
 */
public class PlayerId {
    private final String id;

    public PlayerId(String id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlayerId playerId = (PlayerId) o;
        return Objects.equals(id, playerId.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

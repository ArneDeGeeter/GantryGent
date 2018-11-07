package be.kul.gantry.domain;

import java.util.Objects;

/**
 * Created by Wim on 12/05/2015.
 */
public class Item {

    private final int id;

    public Item(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return "Item{" +
                "id=" + id +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Item)) return false;
        Item item = (Item) o;
        return id == item.id;
    }

    @Override
    public int hashCode() {

        return Objects.hash(id);
    }
}

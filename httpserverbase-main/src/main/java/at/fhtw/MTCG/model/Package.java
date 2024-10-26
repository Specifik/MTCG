package at.fhtw.MTCG.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import java.util.List;

public class Package {
    @JsonAlias({"cards"})
    private List<Card> cards;

    // Jackson needs the default constructor
    public Package() {}

    public Package(List<Card> cards) {
        this.cards = cards;
    }

    public List<Card> getCards() {
        return cards;
    }

    public void setCards(List<Card> cards) {
        this.cards = cards;
    }
}
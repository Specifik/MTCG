package at.fhtw.MTCG.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class Package {
    private List<Card> cards;

    // Jackson benötigt einen leeren Konstruktor
    public Package() {}

    @JsonCreator
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

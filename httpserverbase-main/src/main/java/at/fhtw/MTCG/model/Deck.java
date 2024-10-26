package at.fhtw.MTCG.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import java.util.List;

public class Deck {
    @JsonAlias({"userDeck"})
    private List<Card> userDeck;

    // Jackson needs the default constructor
    public Deck() {}

    public Deck(List<Card> userDeck) {
        this.userDeck = userDeck;
    }

    public List<Card> getUserDeck() {
        return userDeck;
    }

    public void setUserDeck(List<Card> userDeck) {
        this.userDeck = userDeck;
    }
}
package at.fhtw.MTCG.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import java.util.List;

public class Package {
    @JsonAlias({"cards"})
    private List<Card> packageCards;

    // Jackson needs the default constructor
    public Package() {}

    public Package(List<Card> packageCards) {
        this.packageCards = packageCards;
    }

    public List<Card> getCards() {
        return packageCards;
    }

    public void setCards(List<Card> packageCards) {
        this.packageCards = packageCards;
    }
}
package at.fhtw.MTCG.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import java.util.List;
import java.util.UUID;

public class Deck {
    @JsonAlias({"userId"})
    private int userId;
    @JsonAlias({"cardIds"})
    private List<UUID> cardIds;

    public Deck() {}

    public Deck(int userId, List<UUID> cardIds) {
        this.userId = userId;
        this.cardIds = cardIds;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public List<UUID> getCardIds() {
        return cardIds;
    }

    public void setCardIds(List<UUID> cardIds) {
        this.cardIds = cardIds;
    }
}

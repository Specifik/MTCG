package at.fhtw.MTCG.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import java.util.UUID;

public class Trading {
    @JsonAlias({"id"})
    private UUID id;
    @JsonAlias({"userId"})
    private int userId;
    @JsonAlias({"cardId"})
    private UUID cardId;
    @JsonAlias({"type"})
    private String type; // monster oder spell
    @JsonAlias({"minDamage"})
    private double minDamage;

    public Trading() {}

    public Trading(UUID id, int userId, UUID cardId, String type, double minDamage) {
        this.id = id;
        this.userId = userId;
        this.cardId = cardId;
        this.type = type;
        this.minDamage = minDamage;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public UUID getCardId() { return cardId; }
    public void setCardId(UUID cardId) { this.cardId = cardId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public double getMinDamage() { return minDamage; }
    public void setMinDamage(double minDamage) { this.minDamage = minDamage; }
}

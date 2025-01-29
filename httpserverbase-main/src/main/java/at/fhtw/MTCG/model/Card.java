package at.fhtw.MTCG.model;

import com.fasterxml.jackson.annotation.JsonAlias;

import java.util.UUID;

public class Card {
    @JsonAlias({"id"})
    private UUID id;
    @JsonAlias({"name"})
    private String name;
    @JsonAlias({"damage"})
    private double damage;
    @JsonAlias({"elementType"})
    private String elementType;
    @JsonAlias({"packageId"})
    private UUID packageId;
    @JsonAlias({"userId"})
    private UUID userId;

    // Jackson needs the default constructor
    public Card() {}

    public Card(UUID id, String name, double damage, String elementType, UUID packageId) {
        this.id = id;
        this.name = name;
        this.damage = damage;
        this.elementType = elementType;
        this.packageId = packageId;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getDamage() {
        return damage;
    }

    public void setDamage(double damage) {
        this.damage = damage;
    }

    public String getElementType() {
        return elementType;
    }

    public void setElementType(String elementType) {
        this.elementType = elementType;
    }

    public UUID getPackageId() {
        return packageId;
    }

    public void setPackageId(UUID packageId) {
        this.packageId = packageId;
    }

    public boolean isSpellCard() {
        return true; // TODO
    }

    public boolean isMonsterCard() {
        return true; // TODO
    }
}

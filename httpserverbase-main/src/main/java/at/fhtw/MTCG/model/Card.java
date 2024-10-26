package at.fhtw.MTCG.model;

import com.fasterxml.jackson.annotation.JsonAlias;

public class Card {
    @JsonAlias({"name"})
    private String name;
    @JsonAlias({"damage"})
    private final int damage;
    @JsonAlias({"elementType"})
    private String elementType;

    // Jackson needs the default constructor
    public Card() {
        this.damage = 0;
    }

    public Card(String name, int damage, String elementType) {
        this.name = name;
        this.damage = damage;
        this.elementType = elementType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getDamage() {
        return damage;
    }

    public String getElementType() {
        return elementType;
    }

    public void setElementType(String elementType) {
        this.elementType = elementType;
    }

    public boolean isSpellCard() {
        return true; // TODO
    }

    public boolean isMonsterCard() {
        return true; // TODO
    }
}
package at.fhtw.MTCG.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import java.util.List;

public class Stack {
    @JsonAlias({"userStack"})
    private List<Card> userStack;

    // Jackson needs the default constructor
    public Stack() {}

    public Stack(List<Card> userStack) {
        this.userStack = userStack;
    }

    public List<Card> getUserStack() {
        return userStack;
    }

    public void setUserStack(List<Card> userStack) {
        this.userStack = userStack;
    }
}
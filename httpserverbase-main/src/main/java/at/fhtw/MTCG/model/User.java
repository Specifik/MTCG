package at.fhtw.MTCG.model;

import com.fasterxml.jackson.annotation.JsonAlias;

public class User {
    @JsonAlias({"id"})
    private Integer id;
    @JsonAlias({"username", "Username"})
    private String username;
    @JsonAlias({"password", "Password"})
    private String password;
    @JsonAlias({"coins"})
    private int coins;
    private boolean loggedIn;
    @JsonAlias({"token", "Token"})
    private String token;

    // Jackson needs the default constructor
    public User() {}

    public User(Integer id, String username, String password, int coins, String token, boolean loggedIn) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.coins = coins;
        this.token = token;
        this.loggedIn = loggedIn;
    }
    public User(Integer id, String username, String password, int coins) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.coins = coins;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getCoins() {
        return coins;
    }

    public void setCoins(int coins) {
        this.coins = coins;
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public void setLoggedIn(boolean loggedIn) {
        this.loggedIn = loggedIn;
    }

    public void setToken(String token){
        this.token = token;
    }
    public String getToken() {return token; }
}

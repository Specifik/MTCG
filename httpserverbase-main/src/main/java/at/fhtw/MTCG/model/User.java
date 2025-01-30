package at.fhtw.MTCG.model;

import com.fasterxml.jackson.annotation.JsonAlias;

public class User {
    @JsonAlias({"id"})
    private Integer id;
    @JsonAlias({"username", "Username"})
    private String username;
    @JsonAlias({"password", "Password"})
    private String password;
    @JsonAlias({"name", "Name"})
    private String name;
    @JsonAlias({"coins"})
    private int coins;
    @JsonAlias({"bio", "Bio"})
    private String bio;
    @JsonAlias({"image", "Image"})
    private String image;
    @JsonAlias({"token", "Token"})
    private String token;
    @JsonAlias({"loggedIn"})
    private boolean loggedIn;
    @JsonAlias({"elo"})
    private int elo;
    @JsonAlias({"gamesPlayed"})
    private int gamesPlayed;
    @JsonAlias({"wins"})
    private int wins;
    @JsonAlias({"losses"})
    private int losses;

    // Jackson needs a default constructor
    public User() {}

    // Neuer vollständiger Konstruktor mit allen Attributen
    public User(Integer id, String username, String password, String name, int coins, String bio, String image,
                String token, boolean loggedIn, int elo, int gamesPlayed, int wins, int losses) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.name = name;
        this.coins = coins;
        this.bio = bio;
        this.image = image;
        this.token = token;
        this.loggedIn = loggedIn;
        this.elo = elo;
        this.gamesPlayed = gamesPlayed;
        this.wins = wins;
        this.losses = losses;
    }

    // Getter und Setter
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getCoins() { return coins; }
    public void setCoins(int coins) { this.coins = coins; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public boolean isLoggedIn() { return loggedIn; }
    public void setLoggedIn(boolean loggedIn) { this.loggedIn = loggedIn; }

    public int getElo() { return elo; }
    public void setElo(int elo) { this.elo = elo; }

    public int getGamesPlayed() { return gamesPlayed; }
    public void setGamesPlayed(int gamesPlayed) { this.gamesPlayed = gamesPlayed; }

    public int getWins() { return wins; }
    public void setWins(int wins) { this.wins = wins; }

    public int getLosses() { return losses; }
    public void setLosses(int losses) { this.losses = losses; }
}

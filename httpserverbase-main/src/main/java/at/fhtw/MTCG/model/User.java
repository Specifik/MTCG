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
    @JsonAlias({"name", "Name"})
    private String name;
    @JsonAlias({"bio", "Bio"})
    private String bio;
    @JsonAlias({"image", "Image"})
    private String image;

    // Default constructor for Jackson
    public User() {}

    // Full constructor
    public User(Integer id, String username, String password, String name, int coins, String bio, String image, String token, boolean loggedIn) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.name = name;
        this.coins = coins;
        this.bio = bio;
        this.image = image;
        this.token = token;
        this.loggedIn = loggedIn;
    }

    // Constructor without password
    public User(Integer id, String username, String name, int coins, String bio, String image, String token, boolean loggedIn) {
        this(id, username, null, name, coins, bio, image, token, loggedIn);
    }

    public Integer getId() { return id; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getName() { return name; }
    public int getCoins() { return coins; }
    public String getBio() { return bio; }
    public String getImage() { return image; }
    public String getToken() { return token; }
    public boolean isLoggedIn() { return loggedIn; }

    public void setId(Integer id) { this.id = id; }
    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
    public void setName(String name) { this.name = name; }
    public void setCoins(int coins) { this.coins = coins; }
    public void setBio(String bio) { this.bio = bio; }
    public void setImage(String image) { this.image = image; }
    public void setToken(String token) { this.token = token; }
    public void setLoggedIn(boolean loggedIn) { this.loggedIn = loggedIn; }
}

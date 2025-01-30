package at.fhtw.MTCG.model;

import com.fasterxml.jackson.annotation.JsonAlias;

import java.util.List;

public class Battle {
    @JsonAlias({"user1Id"})
    private int user1Id;
    @JsonAlias({"user2Id"})
    private int user2Id;
    @JsonAlias({"winnerId"})
    private Integer winnerId;
    @JsonAlias({"battleLog"})
    private List<String> battleLog;

    public Battle(int user1Id, int user2Id, Integer winnerId, List<String> battleLog) {
        this.user1Id = user1Id;
        this.user2Id = user2Id;
        this.winnerId = winnerId;
        this.battleLog = battleLog;
    }

    public int getUser1Id() { return user1Id; }

    public int getUser2Id() { return user2Id; }

    public Integer getWinnerId() { return winnerId; }

    public List<String> getBattleLog() { return battleLog; }
}

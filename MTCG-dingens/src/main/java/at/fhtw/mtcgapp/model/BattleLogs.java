package at.fhtw.mtcgapp.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class BattleLogs {
    @JsonAlias({"Battle_Log-Id"})
    Integer battle_logId;
    @JsonIgnore
    Integer playerAId;
    @JsonIgnore
    Integer playerBId;
    @JsonAlias({"first_Player"})
    String first_Player;
    @JsonAlias({"second_Player"})
    String second_Player;
    @JsonIgnore
    String log;

    public BattleLogs(Integer battle_logId, Integer playerAId, Integer playerBId, String log) {
        this.battle_logId = battle_logId;
        this.playerAId = playerAId;
        this.playerBId = playerBId;
        this.log = log;
    }

    public Integer getBattle_logId() {
        return battle_logId;
    }

    public void setBattle_logId(Integer battle_logId) {
        this.battle_logId = battle_logId;
    }

    public Integer getPlayerAId() {
        return playerAId;
    }

    public void setPlayerAId(Integer playerAId) {
        this.playerAId = playerAId;
    }

    public Integer getPlayerBId() {
        return playerBId;
    }

    public void setPlayerBId(Integer playerBId) {
        this.playerBId = playerBId;
    }

    public String getFirst_Player() {
        return first_Player;
    }

    public void setFirst_Player(String first_Player) {
        this.first_Player = first_Player;
    }

    public String getSecond_Player() {
        return second_Player;
    }

    public void setSecond_Player(String second_Player) {
        this.second_Player = second_Player;
    }

    public String getLog() {
        return log;
    }

    public void setLog(String log) {
        this.log = log;
    }
}

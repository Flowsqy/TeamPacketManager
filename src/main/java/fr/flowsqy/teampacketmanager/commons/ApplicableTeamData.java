package fr.flowsqy.teampacketmanager.commons;

import org.bukkit.entity.Player;

import java.util.Objects;

public class ApplicableTeamData {

    private final Player player;
    private final TeamData teamData;

    public ApplicableTeamData(Player player, TeamData teamData) {
        Objects.requireNonNull(player);
        Objects.requireNonNull(teamData);
        this.player = player;
        this.teamData = teamData;
    }

    public Player getPlayer() {
        return player;
    }

    public TeamData getTeamData() {
        return teamData;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ApplicableTeamData that = (ApplicableTeamData) o;
        return Objects.equals(player, that.player) && Objects.equals(teamData, that.teamData);
    }

    @Override
    public int hashCode() {
        return Objects.hash(player, teamData);
    }

    @Override
    public String toString() {
        return "ApplicableTeamData{" +
                "player=" + player +
                ", teamData=" + teamData +
                '}';
    }
}

package fr.flowsqy.teampacketmanager;

import org.bukkit.plugin.java.JavaPlugin;

public class TeamPacketManagerPlugin extends JavaPlugin {

    private TeamPacketManager teamPacketManager;

    @Override
    public void onEnable() {
        teamPacketManager = new TeamPacketManager(this);
    }

    public TeamPacketManager getTeamPacketManager() {
        return teamPacketManager;
    }

}

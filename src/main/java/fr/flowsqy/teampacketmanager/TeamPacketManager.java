package fr.flowsqy.teampacketmanager;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;

public class TeamPacketManager implements Listener {

    private final Map<String, TeamData> data;

    public TeamPacketManager(Plugin plugin) {
        data = new HashMap<>();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private void onQuit(PlayerQuitEvent event){
        data.remove(event.getPlayer().getName());
    }

}

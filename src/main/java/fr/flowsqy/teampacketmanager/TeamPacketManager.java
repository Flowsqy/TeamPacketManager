package fr.flowsqy.teampacketmanager;

import fr.flowsqy.teampacketmanager.commons.TeamData;
import fr.flowsqy.teampacketmanager.task.TeamPacketTaskManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TeamPacketManager implements Listener {

    private final Map<String, TeamData> data;
    private final TeamPacketTaskManager taskManager;
    private boolean locked;

    public TeamPacketManager(TeamPacketManagerPlugin plugin, TeamPacketTaskManager taskManager) {
        data = new HashMap<>();
        this.taskManager = taskManager;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        if (this.locked == locked)
            return;
        this.locked = locked;
        final List<Object> packets;
        if (locked) {
            packets = data.values().stream()
                    .map(teamData -> {
                        try {
                            return TeamPacketSender.getPacket(
                                    new TeamData(teamData.getId()),
                                    Collections.emptyList(),
                                    TeamPacketSender.Method.REMOVE
                            );
                        } catch (ReflectiveOperationException exception) {
                            throw new RuntimeException(exception);
                        }
                    })
                    .collect(Collectors.toList());
        } else {
            packets = data.entrySet().stream()
                    .map(entry -> {
                        try {
                            return TeamPacketSender.getPacket(
                                    entry.getValue(),
                                    Collections.singletonList(entry.getKey()),
                                    TeamPacketSender.Method.CREATE
                            );
                        } catch (ReflectiveOperationException exception) {
                            throw new RuntimeException(exception);
                        }
                    })
                    .collect(Collectors.toList());
        }
        taskManager.subscribeAll(packets);
    }

    public TeamData applyTeamData(Player player, TeamData teamData) {
        if (locked)
            return null;
        if (player == null)
            return null;
        final String playerName = player.getName();
        final TeamData previousData = data.get(playerName);
        if (teamData == null)
            return previousData;
        if (previousData == null) {
            data.put(playerName, teamData);
            final Object packet;
            try {
                packet = TeamPacketSender.getPacket(
                        teamData,
                        Collections.singletonList(playerName),
                        TeamPacketSender.Method.CREATE
                );
            } catch (ReflectiveOperationException exception) {
                throw new RuntimeException(exception);
            }
            taskManager.subscribeAll(packet);
            return null;
        }
        final TeamData conflictData = previousData.merge(teamData);
        final boolean changeName = !conflictData.getId().equals(TeamData.DEFAULT_TEAM_ID);
        if (changeName) {
            removeTeam(conflictData.getId());
        }
        final Object packet;
        try {
            packet = TeamPacketSender.getPacket(
                    previousData,
                    Collections.singletonList(playerName),
                    changeName ? TeamPacketSender.Method.CREATE : TeamPacketSender.Method.UPDATE_INFO
            );
        } catch (ReflectiveOperationException exception) {
            throw new RuntimeException(exception);
        }
        taskManager.subscribeAll(packet);
        return conflictData;
    }

    public TeamData removeTeamData(String player) {
        final TeamData teamData = data.remove(player);
        if (locked)
            return teamData;
        if (teamData != null && !teamData.getId().equals(TeamData.DEFAULT_TEAM_ID)) {
            removeTeam(teamData.getId());
        }
        return teamData;
    }

    private void removeTeam(String id) {
        final Object packet;
        try {
            packet = TeamPacketSender.getPacket(
                    new TeamData(id),
                    Collections.emptyList(),
                    TeamPacketSender.Method.REMOVE
            );
        } catch (ReflectiveOperationException exception) {
            throw new RuntimeException(exception);
        }
        taskManager.subscribeAll(packet);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private void onQuit(PlayerQuitEvent event) {
        removeTeamData(event.getPlayer().getName());
        taskManager.remove(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private void onJoin(PlayerJoinEvent event) {
        if (locked)
            return;
        if (data.isEmpty())
            return;
        final List<Object> packets = data.entrySet().stream()
                .map(entry -> {
                    try {
                        return TeamPacketSender.getPacket(
                                entry.getValue(),
                                Collections.singletonList(entry.getKey()),
                                TeamPacketSender.Method.CREATE
                        );
                    } catch (ReflectiveOperationException exception) {
                        throw new RuntimeException(exception);
                    }
                })
                .collect(Collectors.toList());
        taskManager.subscribe(event.getPlayer(), packets);
    }

}

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

import java.util.*;
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

    public TeamData get(Player player) {
        return applyTeamData(player, null);
    }

    public TeamData applyTeamData(Player player, TeamData teamData) {
        Objects.requireNonNull(player);
        final String playerName = player.getName();
        final TeamData previousData = data.get(playerName);
        if (teamData == null || teamData.isNull())
            return previousData;
        if (!teamData.canSend())
            throw new IllegalArgumentException(teamData + " can not be sent");
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
        if(previousData.equals(teamData)){
            return previousData;
        }
        final TeamData conflictData = previousData.merge(teamData);
        final boolean changeName = conflictData.canSend();
        if (changeName && !locked) {
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
        if (!locked && teamData != null && teamData.canSend()) {
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
        if (locked || data.isEmpty())
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

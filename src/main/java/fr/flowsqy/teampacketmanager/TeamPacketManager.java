package fr.flowsqy.teampacketmanager;

import fr.flowsqy.teampacketmanager.commons.TeamData;
import fr.flowsqy.teampacketmanager.exception.TeamIdException;
import fr.flowsqy.teampacketmanager.io.Messages;
import fr.flowsqy.teampacketmanager.task.TeamPacketTaskManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class TeamPacketManager implements Listener {

    private final Logger logger;
    private final Messages messages;
    private final Map<String, TeamData> data;
    private final Map<String, String> idPlayer;
    private final TeamPacketTaskManager taskManager;
    private boolean locked;

    public TeamPacketManager(TeamPacketManagerPlugin plugin, TeamPacketTaskManager taskManager) {
        logger = plugin.getLogger();
        messages = plugin.getMessages();
        data = new HashMap<>();
        idPlayer = new HashMap<>();
        this.taskManager = taskManager;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Lock state
     * @return true if packets are locked, false otherwise
     */
    public boolean isLocked() {
        return locked;
    }

    /**
     * Lock or unlock packets
     * @param locked true to lock packets, false to unlock
     */
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

    /**
     * Get the current TeamData
     * @param player The player who is linked to the team data
     * @return The team data
     */
    public TeamData get(Player player) {
        return applyTeamData(player, null);
    }

    /**
     * Apply a custom team data to a player
     * @param player The targeted player
     * @param teamData The data to send
     * @return The old TeamData, can be null if player was not registered
     * @throws NullPointerException if the player is null
     * @throws IllegalArgumentException if the team data can not be sent
     * @throws TeamIdException if the id is already taken by another player
     */
    public TeamData applyTeamData(Player player, TeamData teamData) {
        Objects.requireNonNull(player);
        final String playerName = player.getName();
        final TeamData previousData = data.get(playerName);
        // Try to send Null Data
        if (teamData == null || teamData.isNull())
            return previousData;
        // Try to send a data with null id
        if (!teamData.canSend())
            throw new IllegalArgumentException(teamData + " can not be sent");
        // Try to send a team that is already assigned to another player
        final String linkedPlayer = idPlayer.get(teamData.getId());
        if (linkedPlayer != null && !playerName.equals(linkedPlayer)) {
            throw new TeamIdException("The id '" + teamData.getId() + "' is already taken by the player '" + linkedPlayer + "'");
        }
        // The previous data does not exist -> Create a new one
        if (previousData == null) {
            data.put(playerName, teamData);
            // Register the new id
            idPlayer.put(teamData.getId(), playerName);
            // If locked, does not send the packet
            if (!locked) {
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
            }
            return null;
        }
        // The new data is exactly the same as the previous one -> Nothing to do
        if (previousData.equals(teamData)) {
            return previousData;
        }
        // Merge to actualize data
        final TeamData conflictData = previousData.merge(teamData);
        final boolean changeName = conflictData.canSend();
        // Team change id -> Remove the old one
        if (changeName) {
            final String previousId = conflictData.getId();
            final String removedPlayer = idPlayer.remove(previousId);
            // Check if we remove the good player from id map for synchronisation warning
            if (!playerName.equals(removedPlayer)) {
                final String awkwardMessage = messages.getMessage("manager.awkward-remove",
                        "%teamplayer%", "%idplayer%", "%teamid%",
                        playerName, String.valueOf(removedPlayer), previousId
                );
                if (awkwardMessage != null)
                    logger.log(Level.WARNING, awkwardMessage);
            }
            // Register the new id
            idPlayer.put(teamData.getId(), playerName);
            // Does not send the packet if the tab is locked
            if (!locked) {
                removeTeam(conflictData.getId());
            }
        }
        // Does not send the packet if the tab is locked
        if (locked)
            return conflictData;
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

    /**
     * Remove the TeamData of a player
     * @param player The targeted player
     * @return The data who is removed
     */
    public TeamData removeTeamData(String player) {
        Objects.requireNonNull(player);
        final TeamData teamData = data.remove(player);
        if (teamData != null && teamData.canSend()) {
            final String id = teamData.getId();
            if (!locked) {
                removeTeam(id);
            }
            final String removedPlayer = idPlayer.remove(id);
            final String awkwardMessage = messages.getMessage("manager.awkward-remove",
                    "%teamplayer%", "%idplayer%", "%teamid%",
                    player, String.valueOf(removedPlayer), id
            );
            if (awkwardMessage != null && !player.equals(removedPlayer)) {
                logger.log(Level.WARNING, awkwardMessage);
            }
        }
        return teamData;
    }

    /**
     * Send a remove packet
     * @param id the id of the team to remove
     */
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

    /**
     * Quit event handle method to remove the data of player who leave the server
     * @param event The PlayerQuitEvent
     */
    @EventHandler(priority = EventPriority.MONITOR)
    private void onQuit(PlayerQuitEvent event) {
        removeTeamData(event.getPlayer().getName());
        taskManager.remove(event.getPlayer());
    }

    /**
     * Join event handle method to send data of players who are already connected
     * @param event The PlayerJoinEvent
     */
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

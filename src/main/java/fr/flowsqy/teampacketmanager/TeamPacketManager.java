package fr.flowsqy.teampacketmanager;

import fr.flowsqy.teampacketmanager.commons.ApplicableTeamData;
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
     *
     * @return true if packets are locked, false otherwise
     */
    public boolean isLocked() {
        return locked;
    }

    /**
     * Lock or unlock packets
     *
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
     *
     * @param player The player who is linked to the team data
     * @return The team data
     */
    public TeamData get(Player player) {
        return applyTeamData(player, null);
    }

    /**
     * Apply multiple TeamData as the same time. This allow you to change permute id
     *
     * @param applicableList The list of all Team that will be sent
     */
    public synchronized void applyTeamData(List<ApplicableTeamData> applicableList) {
        final Set<String> players = new HashSet<>(applicableList.size());
        final Set<String> ids = new HashSet<>(applicableList.size());
        // Find same values and register all player names and ids
        for (ApplicableTeamData applicable : applicableList) {
            if (applicable == null)
                continue;
            if (!applicable.getTeamData().canSend())
                continue;
            if (!players.add(applicable.getPlayer().getName()))
                throw new RuntimeException(
                        "Attempting to change the team data for player '"
                                + applicable.getPlayer().getName()
                                + "' two times in same instruction. Can not select the good id and pass id check");
            if (!ids.add(applicable.getTeamData().getId()))
                throw new TeamIdException("Attempting to register same id for different players : " + applicable.getTeamData().getId());
        }
        final List<Object> packets = new ArrayList<>(ids.size());
        final Set<String> removed = new HashSet<>();
        final Set<String> updated = new HashSet<>();
        for (ApplicableTeamData applicable : applicableList) {
            final TeamData newData = applicable.getTeamData();
            final String id = newData.getId();
            final String previousPlayerName = idPlayer.get(id);
            final boolean update = previousPlayerName != null;
            // Id is already set but will not be updated -> Same id for two teams (registered and to register)
            if (update && !players.contains(previousPlayerName)) {
                throw new TeamIdException("The id '" + id + "' is already taken by the player '" + previousPlayerName + "'");
            }

            final String playerName = applicable.getPlayer().getName();
            final TeamData currentData = data.get(playerName);

            // Player has not previous team data -> register it server side
            if (currentData == null) {
                if (!locked) {
                    try {
                        // The team already exist client side -> update it
                        if (update) {
                            updateTeam(packets, previousPlayerName, playerName, newData, id);
                            // If the team will be removed, remove it from remove list, add it as updated otherwise
                            if (!removed.remove(id)) {
                                updated.add(id);
                            }
                        }
                        // The team does not exist client side -> create it
                        else {
                            packets.add(TeamPacketSender.getPacket(
                                    newData,
                                    Collections.singletonList(playerName),
                                    TeamPacketSender.Method.CREATE
                                    )
                            );
                        }
                    } catch (ReflectiveOperationException exception) {
                        throw new RuntimeException(exception);
                    }
                }
                data.put(playerName, newData);
                idPlayer.put(id, playerName);
                continue;
            }

            // Player has a previous team data

            // The new data is same as the old one
            if (currentData.equals(newData)) {
                continue;
            }

            // Update the data
            final TeamData conflictData = currentData.merge(newData);

            if (!locked) {
                try {
                    // Exist client side -> Update it client side
                    if (update) {
                        updateTeam(packets, previousPlayerName, playerName, currentData, id);
                        // If the team will be removed, remove it from remove list, add it as updated otherwise
                        if (!removed.remove(id)) {
                            updated.add(id);
                        }
                    }
                    // Or Create a new team client side but the player has an old team data (both side)
                    else {
                        // Remove from updated, add to remove list if has not been updated yet
                        if (!updated.remove(conflictData.getId())) {
                            // Add to remove list old id that is not updated yet
                            removed.add(conflictData.getId());
                        }
                        // Add the new team client side
                        packets.add(TeamPacketSender.getPacket(
                                currentData,
                                Collections.singletonList(playerName),
                                TeamPacketSender.Method.CREATE
                                )
                        );
                    }
                } catch (ReflectiveOperationException exception) {
                    throw new RuntimeException(exception);
                }
            }

            // Register the new id
            idPlayer.put(id, playerName);
        }
        // Remove all unused teams
        for (String removeId : removed) {
            if (!locked) {
                try {
                    packets.add(TeamPacketSender.getPacket(
                            new TeamData(removeId),
                            Collections.emptyList(),
                            TeamPacketSender.Method.REMOVE
                            )
                    );
                } catch (ReflectiveOperationException exception) {
                    throw new RuntimeException(exception);
                }
            }
            idPlayer.remove(removeId);
        }

        // Send packets
        taskManager.subscribeAll(packets);
    }

    /**
     * Add the three packets to update a team client side
     *
     * @param packets            The packet list
     * @param previousPlayerName The name of previous player who owned the team
     * @param playerName         The name of the new player who own the team
     * @param teamData           The data of the new team
     * @param id                 The id of the data
     * @throws ReflectiveOperationException if a packet creation fail
     */
    private void updateTeam(
            List<Object> packets,
            String previousPlayerName,
            String playerName,
            TeamData teamData,
            String id)
            throws ReflectiveOperationException {
        // Remove old player
        packets.add(TeamPacketSender.getPacket(
                id,
                null,
                null,
                null,
                null,
                null,
                null,
                Collections.singletonList(previousPlayerName),
                TeamPacketSender.Method.REMOVE_PLAYERS.getMethod(),
                0
                )
        );
        // Update info
        packets.add(TeamPacketSender.getPacket(
                teamData,
                Collections.emptyList(),
                TeamPacketSender.Method.UPDATE_INFO
                )
        );
        // Add new player
        packets.add(TeamPacketSender.getPacket(
                id,
                null,
                null,
                null,
                null,
                null,
                null,
                Collections.singletonList(playerName),
                TeamPacketSender.Method.ADD_PLAYERS.getMethod(),
                0
                )
        );
    }

    /**
     * Apply a custom team data to a player
     *
     * @param player   The targeted player
     * @param teamData The data to send
     * @return The old TeamData, can be null if player was not registered
     * @throws NullPointerException     if the player is null
     * @throws IllegalArgumentException if the team data can not be sent
     * @throws TeamIdException          if the id is already taken by another player
     */
    public synchronized TeamData applyTeamData(Player player, TeamData teamData) {
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
     *
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
     *
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
     *
     * @param event The PlayerQuitEvent
     */
    @EventHandler(priority = EventPriority.MONITOR)
    private void onQuit(PlayerQuitEvent event) {
        removeTeamData(event.getPlayer().getName());
        taskManager.remove(event.getPlayer());
    }

    /**
     * Join event handle method to send data of players who are already connected
     *
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

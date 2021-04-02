package fr.flowsqy.teampacketmanager;

import fr.flowsqy.teampacketmanager.commons.TeamData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class TeamPacketManager implements Listener {

    private final int PACKET_BY_TICKS;
    private final int INTERVAL_PACKET_SENT;

    private final Plugin plugin;
    private final Map<String, TeamData> data;

    private boolean locked;

    public TeamPacketManager(TeamPacketManagerPlugin plugin) {
        PACKET_BY_TICKS = clamp(plugin.getConfiguration().getInt("packets-by-ticks", 10), Integer.MAX_VALUE, 1);
        INTERVAL_PACKET_SENT = clamp(plugin.getConfiguration().getInt("interval-packet-sent", 4), 20, 1);
        this.plugin = plugin;
        data = new HashMap<>();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    private static int clamp(int value, int max, int min) {
        if (max > value)
            return max;
        if (min < value)
            return min;
        return value;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        if (this.locked == locked)
            return;
        this.locked = locked;
        if (locked) {
            new BukkitRunnable() {

                private final Iterator<Map.Entry<String, TeamData>> entryIterator;

                {
                    entryIterator = data.entrySet().iterator();
                }

                @Override
                public void run() {
                    int packetCount = 1;
                    while (entryIterator.hasNext()) {
                        if (packetCount > PACKET_BY_TICKS)
                            return;
                        final Map.Entry<String, TeamData> entry = entryIterator.next();
                        TeamPacketSender.sendTeamData(
                                Bukkit.getOnlinePlayers(),
                                new TeamData.Builder().id(entry.getValue().getId()).create(),
                                Collections.emptyList(),
                                TeamPacketSender.Method.REMOVE
                        );
                        packetCount++;
                    }
                    cancel();
                }
            }.runTaskTimer(plugin, 0L, INTERVAL_PACKET_SENT);
        } else {
            new BukkitRunnable() {

                private final Iterator<Map.Entry<String, TeamData>> entryIterator;

                {
                    entryIterator = data.entrySet().iterator();
                }

                @Override
                public void run() {
                    int packetCount = 1;
                    while (entryIterator.hasNext()) {
                        if (packetCount > PACKET_BY_TICKS)
                            return;
                        final Map.Entry<String, TeamData> entry = entryIterator.next();
                        TeamPacketSender.sendTeamData(
                                Bukkit.getOnlinePlayers(),
                                entry.getValue(),
                                new ArrayList<>(Collections.singletonList(entry.getKey())),
                                TeamPacketSender.Method.CREATE
                        );
                        packetCount++;
                    }
                    cancel();
                }
            }.runTaskTimer(plugin, 0L, INTERVAL_PACKET_SENT);
        }
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
            TeamPacketSender.sendTeamData(
                    Bukkit.getOnlinePlayers(),
                    teamData,
                    new ArrayList<>(Collections.singletonList(playerName)),
                    TeamPacketSender.Method.CREATE
            );
            return null;
        }
        final TeamData conflictData = previousData.merge(teamData);
        final boolean changeName = !conflictData.getId().equals(TeamData.DEFAULT_TEAM_ID);
        if (changeName) {
            removeTeam(conflictData.getId());
        }
        TeamPacketSender.sendTeamData(
                Bukkit.getOnlinePlayers(),
                previousData,
                new ArrayList<>(Collections.singletonList(playerName)),
                changeName ? TeamPacketSender.Method.CREATE : TeamPacketSender.Method.UPDATE_INFO
        );
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
        TeamPacketSender.sendTeamData(
                Bukkit.getOnlinePlayers(),
                new TeamData(id),
                new ArrayList<>(),
                TeamPacketSender.Method.REMOVE
        );
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private void onQuit(PlayerQuitEvent event) {
        removeTeamData(event.getPlayer().getName());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private void onJoin(PlayerJoinEvent event) throws ReflectiveOperationException {
        if (locked)
            return;
        new BukkitRunnable() {

            private final Object playerConnection;
            private final Iterator<Map.Entry<String, TeamData>> entryIterator;

            {
                playerConnection = TeamPacketSender.getPlayerConnection(event.getPlayer());
                entryIterator = data.entrySet().iterator();
            }

            @Override
            public void run() {
                int packetCount = 1;
                while (entryIterator.hasNext()) {
                    if (packetCount > PACKET_BY_TICKS)
                        return;
                    final Map.Entry<String, TeamData> entry = entryIterator.next();
                    try {
                        TeamPacketSender.sendPacket(playerConnection,
                                TeamPacketSender.getPacket(
                                        entry.getValue(),
                                        new ArrayList<>(Collections.singletonList(entry.getKey())),
                                        TeamPacketSender.Method.CREATE
                                )
                        );
                    } catch (ReflectiveOperationException e) {
                        e.printStackTrace();
                    }
                    packetCount++;
                }
                cancel();
            }
        }.runTaskTimer(plugin, 0L, INTERVAL_PACKET_SENT);
    }

}

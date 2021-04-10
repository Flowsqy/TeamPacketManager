package fr.flowsqy.teampacketmanager.task;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TeamPacketTaskManager {

    private final Plugin plugin;
    private final Map<Player, TeamPacketSendTask> playerTask;
    private TeamPacketSendAllTask allTask;

    public TeamPacketTaskManager(Plugin plugin) {
        this.plugin = plugin;
        this.playerTask = new HashMap<>();
    }

    public void subscribeAll(Object packet) {
        subscribeAll(Collections.singletonList(packet));
    }

    public void subscribeAll(List<Object> packets) {
        if (allTask == null) {
            allTask = new TeamPacketSendAllTask(() -> allTask = null, playerTask);
            allTask.add(packets);
            allTask.start(plugin);
        } else
            allTask.add(packets);
    }

    public void subscribe(Player player, Object packet) {
        subscribe(player, Collections.singletonList(packet));
    }

    public void subscribe(Player player, List<Object> packets) {
        TeamPacketSendTask task = playerTask.get(player);
        if (task == null) {
            task = new TeamPacketSendTask(() -> playerTask.remove(player), player);
            playerTask.put(player, task);
            task.add(packets);
            task.start(plugin);
        } else
            task.add(packets);
    }

    public void remove(Player player) {
        final TeamPacketSendTask task = playerTask.remove(player);
        cancelTask(task);
    }

    public void cancelAll() {
        cancelTask(allTask);
        playerTask.values().forEach(this::cancelTask);
    }

    private void cancelTask(AbstractTeamPacketSendTask task) {
        if (task != null && !task.isCancelled())
            task.cancel();
    }

}

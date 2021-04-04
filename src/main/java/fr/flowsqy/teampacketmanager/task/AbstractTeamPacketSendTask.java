package fr.flowsqy.teampacketmanager.task;

import fr.flowsqy.teampacketmanager.TeamPacketManagerPlugin;
import org.bukkit.configuration.Configuration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

public abstract class AbstractTeamPacketSendTask extends BukkitRunnable {

    private static final int PACKET_BY_TICKS;
    private static final int INTERVAL_PACKET_SENT;
    private static final int TASK_PERSISTENT;

    static {
        final Configuration configuration = JavaPlugin.getPlugin(TeamPacketManagerPlugin.class).getConfiguration();
        PACKET_BY_TICKS = Math.max(configuration.getInt("packets-by-ticks", 10), 1);
        INTERVAL_PACKET_SENT = clamp(configuration.getInt("interval-packet-sent", 4), 20, 1);
        TASK_PERSISTENT = Math.max(configuration.getInt("task-persistent", 20), 0);
    }

    private final Queue<Object> queue;
    private final Runnable closeHandler;
    private int persistent;
    private int runtimeErrors;

    public AbstractTeamPacketSendTask(Runnable closeHandler) {
        this.queue = new ArrayDeque<>();
        this.closeHandler = closeHandler;
        runtimeErrors = 0;
    }

    private static int clamp(int value, int max, int min) {
        if (max < value)
            return max;
        if (min > value)
            return min;
        return value;
    }

    @Override
    public void run() {
        int packetCount = 1;
        Object packet;
        while ((packet = queue.poll()) != null) {
            persistent = 0;
            try {
                send(packet);
            } catch (ReflectiveOperationException exception) {
                exception.printStackTrace();
                runtimeErrors++;
                if (runtimeErrors > 5) {
                    cancel();
                }
            }
            packetCount++;
            if (packetCount > PACKET_BY_TICKS)
                return;
        }
        persistent++;
        if (persistent > TASK_PERSISTENT) {
            cancel();
        }
    }

    public abstract void send(Object packet) throws ReflectiveOperationException;

    public void start(Plugin plugin) {
        this.runTaskTimer(plugin, 0L, INTERVAL_PACKET_SENT);
    }

    public void add(List<Object> packets) {
        queue.addAll(packets);
    }

    @Override
    public synchronized void cancel() throws IllegalStateException {
        super.cancel();
        closeHandler.run();
    }

}

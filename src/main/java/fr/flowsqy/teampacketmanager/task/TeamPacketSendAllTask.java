package fr.flowsqy.teampacketmanager.task;

import fr.flowsqy.teampacketmanager.TeamPacketSender;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.Map;

public class TeamPacketSendAllTask extends AbstractTeamPacketSendTask {

    private final Map<Player, TeamPacketSendTask> playerTask;

    public TeamPacketSendAllTask(Runnable closeHandler, Map<Player, TeamPacketSendTask> playerTask) {
        super(closeHandler);
        this.playerTask = playerTask;
    }

    @Override
    public void send(Object packet) throws ReflectiveOperationException {
        for (Player player : Bukkit.getOnlinePlayers()) {
            final TeamPacketSendTask task = playerTask.get(player);
            if (task == null)
                TeamPacketSender.sendPacket(TeamPacketSender.getPlayerConnection(player), packet);
            else
                task.add(Collections.singletonList(packet));
        }
    }

    @Override
    public String toString() {
        return "TeamPacketSendAllTask{}";
    }
}

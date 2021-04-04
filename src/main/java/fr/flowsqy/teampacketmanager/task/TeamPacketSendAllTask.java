package fr.flowsqy.teampacketmanager.task;

import fr.flowsqy.teampacketmanager.TeamPacketSender;
import org.bukkit.Bukkit;

public class TeamPacketSendAllTask extends AbstractTeamPacketSendTask {
    public TeamPacketSendAllTask(Runnable closeHandler) {
        super(closeHandler);
    }

    @Override
    public void send(Object packet) throws ReflectiveOperationException {
        TeamPacketSender.sendPacketTo(Bukkit.getOnlinePlayers(), packet);
    }

    @Override
    public String toString() {
        return "TeamPacketSendAllTask{}";
    }
}

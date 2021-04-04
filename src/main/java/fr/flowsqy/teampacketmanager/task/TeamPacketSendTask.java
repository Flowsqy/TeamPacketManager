package fr.flowsqy.teampacketmanager.task;

import fr.flowsqy.teampacketmanager.TeamPacketSender;
import org.bukkit.entity.Player;

public class TeamPacketSendTask extends AbstractTeamPacketSendTask {

    private final Object playerConnection;

    public TeamPacketSendTask(Runnable closeHandler, Player player) {
        super(closeHandler);
        try {
            this.playerConnection = TeamPacketSender.getPlayerConnection(player);
        } catch (ReflectiveOperationException exception) {
            throw new RuntimeException(exception);
        }
    }

    @Override
    public void send(Object packet) throws ReflectiveOperationException {
        TeamPacketSender.sendPacket(playerConnection, packet);
    }

    @Override
    public String toString() {
        return "TeamPacketSendTask{" +
                "playerConnection=" + playerConnection +
                '}';
    }
}

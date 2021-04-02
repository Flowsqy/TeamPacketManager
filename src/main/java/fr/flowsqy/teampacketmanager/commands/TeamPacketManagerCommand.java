package fr.flowsqy.teampacketmanager.commands;

import fr.flowsqy.teampacketmanager.TeamPacketManagerPlugin;
import fr.flowsqy.teampacketmanager.io.Messages;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class TeamPacketManagerCommand implements TabExecutor {

    private final static String TRUE_ARG = "true";
    private final static String FALSE_ARG = "false";

    private final TeamPacketManagerPlugin plugin;
    private final Messages messages;

    public TeamPacketManagerCommand(TeamPacketManagerPlugin plugin) {
        this.plugin = plugin;
        this.messages = plugin.getMessages();
        final PluginCommand command = plugin.getCommand("lockteampacket");
        assert command != null;
        command.setExecutor(this);
        command.setTabCompleter(this);
        final String permMessage = messages.getMessage("util.noperm");
        if (permMessage != null)
            command.setPermissionMessage(permMessage);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length != 1)
            return messages.sendMessage(sender, "command.help");
        final String arg = args[0].toLowerCase(Locale.ROOT);
        if (arg.equals(TRUE_ARG)) {
            if (plugin.getTeamPacketManager().isLocked())
                return messages.sendMessage(sender, "command.alreadylocked");
            plugin.getTeamPacketManager().setLocked(true);
            return messages.sendMessage(sender, "command.lock");
        }
        if (arg.equals(FALSE_ARG)) {
            if (!plugin.getTeamPacketManager().isLocked())
                return messages.sendMessage(sender, "command.notlocked");
            plugin.getTeamPacketManager().setLocked(false);
            return messages.sendMessage(sender, "command.unlock");
        }
        return messages.sendMessage(sender, "command.help");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length != 1)
            return Collections.emptyList();
        final String arg = args[0].toLowerCase(Locale.ROOT);
        if (arg.isEmpty())
            return Arrays.asList(TRUE_ARG, FALSE_ARG);
        if (TRUE_ARG.startsWith(arg))
            return Collections.singletonList(TRUE_ARG);
        if (FALSE_ARG.startsWith(arg))
            return Collections.singletonList(FALSE_ARG);
        return Collections.emptyList();
    }
}

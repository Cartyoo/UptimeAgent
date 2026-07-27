package xyz.herberto.uptimeAgent.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import org.bukkit.command.CommandSender;
import xyz.herberto.uptimeAgent.UptimeAgent;

@CommandAlias("uptimeagent")
@Description("Commands for Layeredy Uptime Agent")
public class UptimeAgentCommand extends BaseCommand {

    @HelpCommand
    @Default
    public void help(CommandSender sender, CommandHelp help) {
        help.showHelp();
    }

    @Subcommand("reload")
    @CommandPermission("uptimeagent.command.reload")
    public void reload(CommandSender sender) {
        UptimeAgent.getInstance().reloadConfig();
        UptimeAgent.setUptimeToken(UptimeAgent.getInstance().getConfig().getString("layeredy-uptime-token"));

        sender.sendMessage("§a[Layeredy Uptime Agent] Config reloaded successfully!");
    }

}

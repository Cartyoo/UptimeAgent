package xyz.herberto.uptimeAgent;

import co.aikar.commands.PaperCommandManager;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.herberto.uptimeAgent.commands.UptimeAgentCommand;
import xyz.herberto.uptimeAgent.task.UptimeTask;

import java.time.Instant;

public final class UptimeAgent extends JavaPlugin {
    @Getter public static UptimeAgent instance;
    @Getter @Setter public static String uptimeToken;
    @Getter public static String agentVersion;

    @Override
    public void onEnable() {
        instance = this;

        PaperCommandManager commandManager = new PaperCommandManager(this);
        commandManager.enableUnstableAPI("help");

        commandManager.registerCommand(new UptimeAgentCommand());

        saveDefaultConfig();

        uptimeToken = getConfig().getString("layeredy-uptime-token");
        if(uptimeToken == null) {
            getLogger().severe("[" + Instant.now() + "] ERROR 403: invalid token. Check 'layeredy-uptime-token' in your config.yml. Exiting. (lyrdy.co/server-agent-error)");
        }

        agentVersion = getPluginMeta().getVersion();

        new UptimeTask().runTaskTimerAsynchronously(this, 0L, 20L * getConfig().getLong("interval-seconds"));

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}

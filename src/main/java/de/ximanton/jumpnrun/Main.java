package de.ximanton.jumpnrun;

import de.ximanton.jumpnrun.command.JNRCommand;
import de.ximanton.jumpnrun.jnr.JNRManager;
import de.ximanton.jumpnrun.jnr.JumpNRun;
import de.ximanton.jumpnrun.jnr.JumpingPlayer;
import de.ximanton.jumpnrun.data.MessageManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

public final class Main extends JavaPlugin {

    private static Main instance;

    public static Main getInstance() {
        return instance;
    }

    public Main() {
        instance = this;
    }

    private String databasePath;
    private MessageManager messages;

    @Override
    public void onEnable() {
        reload();

        Bukkit.getPluginManager().registerEvents(JNRManager.getInstance(), this);
        JNRCommand.register();
    }

    public void reload() {
        onDisable();

        try {
            getConfig().load(new File(getDataFolder(), "config.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
        }
        saveDefaultConfig();

        messages = new MessageManager(getConfig().getConfigurationSection("messages"));
        databasePath = getConfig().getString("database");

        JNRManager.getInstance().init(getConfig());
    }

    @Override
    public void onDisable() {
        for (JumpNRun jnr : JNRManager.getInstance().getJumpNRuns()) {
            for (JumpingPlayer player : jnr.getPlayers()) {
                player.clearSlots();
            }
            jnr.removeHolograms();
        }
        JNRManager.getInstance().getJumpNRuns().clear();
        if (JNRManager.getInstance().getDatabase() != null) {
            try {
                JNRManager.getInstance().getDatabase().getConnection().close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
    }

    public String getDatabasePath() {
        return databasePath;
    }

    public MessageManager getMessages() {
        return messages;
    }
}

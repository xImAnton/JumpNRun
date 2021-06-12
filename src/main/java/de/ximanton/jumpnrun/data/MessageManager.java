package de.ximanton.jumpnrun.data;

import de.ximanton.jumpnrun.util.Util;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class MessageManager {

    private final String fly;
    private final String startHolo;
    private final String endHolo;
    private final String start;
    private final String reset;
    private final String goToStart;
    private final String finished;
    private final String pb;
    private final String highscore;
    private final String checkpoint;
    private final String backToCheckpoint;
    private final String backToStart;
    private final String quit;

    public MessageManager(ConfigurationSection config) {
        fly = config.getString("fly", "Jump'n'Run challenge ended because you flew!");
        startHolo = config.getString("startHolo", "§aStart");
        endHolo = config.getString("endHolo", "§aEnd");
        start = config.getString("start", "Jump'n'Run started!");
        reset = config.getString("reset", "Your time has been reset");
        goToStart = config.getString("gotostart", "Go to the §eStart Plate §7to start the Jump'n'Run");
        finished = config.getString("finished", "You finished the Jump'n'Run in §6%time%§7!");
        pb = config.getString("pb", "§a§lThat's a new personal record! Your previous time was §6%time%§7!");
        highscore = config.getString("highscore", "§e§l%player%§r§a set a new Highscore of §6§l%time%§r§a!");
        checkpoint = config.getString("checkpoint", "Checkpoint §a#%checkpoint%§7 reached after §6%time%§7!");
        backToCheckpoint = config.getString("backToCheckpoint", "§7Back to §aCheckpoint");
        backToStart = config.getString("backToStart", "§7Back to §aStart");
        quit = config.getString("quit", "§cQuit Jump'n'Run");
    }

    public String getQuit() {
        return quit;
    }

    public String getBackToStart() {
        return backToStart;
    }

    public String getBackToCheckpoint() {
        return backToCheckpoint;
    }

    public String formatCheckpointMessage(int checkpointI, String time) {
        return checkpoint
                .replace("%checkpoint%", String.valueOf(checkpointI))
                .replace("%time%", time);
    }

    public String formatHighscoreMessage(Player p, long time) {
        return highscore
                .replace("%player%", p.getDisplayName())
                .replace("%time%", Util.formatTime(time));
    }

    public String formatPBMessage(long time) {
        return pb.replace("%time%", Util.formatTime(time));
    }

    public String formatFinishedMessage(String time) {
        return finished.replace("%time%", time);
    }

    public String getGoToStartMessage() {
        return goToStart;
    }

    public String getResetMessage() {
        return reset;
    }

    public String getStartMessage() {
        return start;
    }

    public String getFlyMessage() {
        return fly;
    }

    public String getStartHolo() {
        return startHolo;
    }

    public String getEndHolo() {
        return endHolo;
    }
}

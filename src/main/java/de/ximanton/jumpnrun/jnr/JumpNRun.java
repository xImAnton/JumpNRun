package de.ximanton.jumpnrun.jnr;

import de.ximanton.jumpnrun.data.DatabaseConnector;
import de.ximanton.jumpnrun.Main;
import de.ximanton.jumpnrun.util.Hologram;
import de.ximanton.jumpnrun.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class JumpNRun {

    private final String id;
    private final String name;
    private final Location startPlate;
    private final Location endPlate;
    private final ArrayList<Location> checkPoints = new ArrayList<>();
    private final World world;
    private final boolean cancelInLava;
    private Block highscoreSign;

    private final ArrayList<Hologram> holograms = new ArrayList<>();

    private final ArrayList<JumpingPlayer> players = new ArrayList<>();

    public String getId() {
        return id;
    }

    public JumpNRun(ConfigurationSection config, String id) {
        this.id = id;
        this.world = Bukkit.getWorld(config.getString("world", "world"));

        this.name = config.getString("name");
        this.cancelInLava = config.getBoolean("cancelInLava", true);
        this.startPlate = Util.locationFromString(config.getString("start"), world);
        this.endPlate = Util.locationFromString(config.getString("end"), world);

        if (!Util.isPressurePlate(world.getBlockAt(startPlate), world.getBlockAt(endPlate))) {
            throw new IllegalArgumentException("start or end plate is not a pressure plate!");
        }

        int cpIndex = 1;
        for (String cpCoords : config.getStringList("checkpoints")) {
            Location cp = Util.locationFromString(cpCoords, world);

            if (!Util.isPressurePlate(world.getBlockAt(cp))) {
                throw new IllegalArgumentException("specified block at " + cp + " is not a pressure plate!");
            }

            Hologram cpHolo = new Hologram(cp.clone().add(0, 0.4, 0));
            cpHolo.addLine(ChatColor.GRAY + "Checkpoint " + ChatColor.GREEN + "#" + cpIndex);
            holograms.add(cpHolo);

            checkPoints.add(cp);
            cpIndex++;
        }

        Hologram startHologram = new Hologram(startPlate.clone().add(0, 1.1, 0));
        startHologram.addLine(name);
        startHologram.addLine(Main.getInstance().getMessages().getStartHolo());
        holograms.add(startHologram);

        Hologram endHologram = new Hologram(endPlate.clone().add(0, 1.1, 0));
        endHologram.addLine(name);
        endHologram.addLine(Main.getInstance().getMessages().getEndHolo());
        holograms.add(endHologram);

        highscoreSign = null;

        if (config.getBoolean("highscoreSign.enabled")) {
             highscoreSign = world.getBlockAt(Util.locationFromString(config.getString("highscoreSign.coords")));

             if (!(highscoreSign.getState() instanceof Sign)) {
                 throw new IllegalArgumentException("specified block is not a sign");
             }
        }

        updateSign(JNRManager.getInstance().getDatabase().getHighscore(id));
    }

    public void removeHolograms() {
        holograms.forEach(Hologram::destroy);
    }

    public ArrayList<JumpingPlayer> getPlayers() {
        return players;
    }

    public Location getStartPlate() {
        return startPlate;
    }

    public Location getEndPlate() {
        return endPlate;
    }

    public ArrayList<Location> getCheckPoints() {
        return checkPoints;
    }

    public String getPrefix() {
        return ChatColor.DARK_GRAY + "[" + ChatColor.WHITE + name + ChatColor.DARK_GRAY + "]" + ChatColor.GRAY + " ";
    }

    public void updateSign(DatabaseConnector.PlayerRecord rec) {
        if (highscoreSign == null) return;
        Sign signState = (Sign) highscoreSign.getState();

        if (rec == null) {
            signState.setLine(0, "");
            signState.setLine(1, name);
            signState.setLine(2, ChatColor.YELLOW + "No Highscore yet");
            signState.setLine(3, "");
        } else {
            signState.setLine(0, name);
            signState.setLine(1, ChatColor.YELLOW + rec.getPlayer().getName());
            signState.setLine(2, ChatColor.BOLD + ChatColor.GOLD.toString() + Util.formatTime(rec.time()));
            signState.setLine(3, ChatColor.GREEN + Util.formatDate(rec.getEndTime()));
        }

        signState.update();
    }

    private void onStartPlate(Player p) {
        JumpingPlayer player = getJumpingPlayer(p);
        if (p.isFlying()) {
            p.sendMessage(getPrefix() + Main.getInstance().getMessages().getFlyMessage());
            return;
        }
        if (player == null) {
            p.sendMessage(getPrefix() + Main.getInstance().getMessages().getStartMessage());
            player = new JumpingPlayer(p, this);
            player.restart();
            players.add(player);
            return;
        }
        p.sendMessage(getPrefix() + Main.getInstance().getMessages().getResetMessage());
        player.restart();
    }

    public Block getHighscoreSign() {
        return highscoreSign;
    }

    public JumpingPlayer getJumpingPlayer(Player p) {
        for (JumpingPlayer jp : players) {
            if (jp.getPlayer().equals(p)) {
                return jp;
            }
        }
        return null;
    }

    public boolean removeHighScore() {
        boolean result = JNRManager.getInstance().getDatabase().removeHighscore(id);
        updateSign(JNRManager.getInstance().getDatabase().getHighscore(id));

        return result;
    }

    public boolean isPlayerPlaying(Player p) {
        return getJumpingPlayer(p) != null;
    }

    private void onEndPlate(Player p) {
        JumpingPlayer player = getJumpingPlayer(p);
        if (player == null) {
            p.sendMessage(getPrefix() + Main.getInstance().getMessages().getGoToStartMessage());
            return;
        }

        p.sendMessage(getPrefix() + Main.getInstance().getMessages().formatFinishedMessage(player.getCurrentTime()));

        DatabaseConnector.PlayerRecord rec = new DatabaseConnector.PlayerRecord(id, player.getPlayer().getUniqueId(), player.getStartTime(), System.currentTimeMillis());
        DatabaseConnector.PlayerRecord highscore = JNRManager.getInstance().getDatabase().getHighscore(id);

        if (JNRManager.getInstance().getDatabase().updateScore(rec)) {
            p.sendMessage(Main.getInstance().getMessages().formatPBMessage(rec.time()));
        }

        removePlayer(player);

        if (highscore == null) {
            newHighscore(rec);
            return;
        }
        if (rec.better(highscore)) {
            newHighscore(rec);
        }
    }

    public void newHighscore(DatabaseConnector.PlayerRecord rec) {
        Bukkit.broadcastMessage(getPrefix() + Main.getInstance().getMessages().formatHighscoreMessage(rec.getPlayer(), rec.time()));
        updateSign(rec);
    }

    public void removePlayer(JumpingPlayer p) {
        p.end();
        players.remove(p);
    }

    public int getCheckpointIndex(Block block) {
        for (int i = 0; i < checkPoints.size(); i++) {
            if (checkPoints.get(i).getBlock().getLocation().equals(block.getLocation())) {
                return i;
            }
        }
        return -1;
    }

    public void onCheckpoint(Player p, Block b) {
        JumpingPlayer player = getJumpingPlayer(p);
        if (player == null) {
            p.sendMessage(getPrefix() + Main.getInstance().getMessages().getGoToStartMessage());
            return;
        }

        int cpIndex = getCheckpointIndex(b);

        if (cpIndex <= player.getCheckPoint()) return;

        player.setCheckPoint(cpIndex);
        p.sendMessage(getPrefix() + Main.getInstance().getMessages().formatCheckpointMessage(cpIndex + 1, player.getCurrentTime()));
    }

    public void onPlate(Player p, JNRManager.PressurePlateType plateType, Block block) {
        switch (plateType) {
            case START:
                onStartPlate(p);
                break;
            case END:
                onEndPlate(p);
                break;
            case CHECKPOINT:
                onCheckpoint(p, block);
                break;
        }
    }

    public boolean isCancelInLava() {
        return cancelInLava;
    }

    public void reset() {
        JNRManager.getInstance().getDatabase().resetJNR(id);
        updateSign(null);
    }
}

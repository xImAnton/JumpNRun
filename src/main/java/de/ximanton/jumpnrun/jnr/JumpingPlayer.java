package de.ximanton.jumpnrun.jnr;

import de.ximanton.jumpnrun.util.Util;
import org.bukkit.entity.Player;

public class JumpingPlayer {

    private final Player player;
    private long startTime;
    private int checkPoint = -1;
    private final JumpNRun jnr;

    public JumpingPlayer(Player player, JumpNRun jnr) {
        this.player = player;
        this.jnr = jnr;
        restart();
        player.getInventory().setItem(2, Util.getBackToCheckpointItem());
        player.getInventory().setItem(3, Util.getResetItem());
        player.getInventory().setItem(5, Util.getQuitItem());
    }

    public void restart(boolean teleport) {
        checkPoint = -1;
        startTime = System.currentTimeMillis();
        if (teleport) {
            player.teleport(jnr.getStartPlate());
        }
    }

    public void restart() {
        restart(false);
    }

    public void setCheckPoint(int checkPoint) {
        this.checkPoint = checkPoint;
    }

    public void backToCheckPoint() {
        if (checkPoint < 0) {
            player.teleport(jnr.getStartPlate());
            return;
        }
        player.teleport(jnr.getCheckPoints().get(checkPoint));
    }

    public Player getPlayer() {
        return player;
    }

    public void clearSlots() {
        JNRManager.USED_SLOTS.forEach((i) -> player.getInventory().setItem(i, null));
    }

    public void end() {
        clearSlots();
    }

    public String getCurrentTime() {
        long delta = System.currentTimeMillis() - startTime;
        return Util.formatTime(delta);
    }

    public int getCheckPoint() {
        return checkPoint;
    }

    public long getStartTime() {
        return startTime;
    }
}

package de.ximanton.jumpnrun.jnr;

import de.ximanton.jumpnrun.data.DatabaseConnector;
import de.ximanton.jumpnrun.Main;
import de.ximanton.jumpnrun.util.Util;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class JNRManager implements Listener {

    private final static JNRManager INSTANCE = new JNRManager();
    public final static List<Integer> USED_SLOTS = Arrays.asList(2, 3, 5);

    public static JNRManager getInstance() {
        return INSTANCE;
    }
    private final HashMap<Player, Long> lastPPuses = new HashMap<>();

    private DatabaseConnector database;

    public DatabaseConnector getDatabase() {
        return database;
    }

    public void init(FileConfiguration config) {
        lastPPuses.clear();

        database = new DatabaseConnector();
        for (String jnrID : config.getConfigurationSection("jnrs").getKeys(false)) {
            if (!config.getBoolean("jnrs." + jnrID + ".enabled")) continue;
            JumpNRun jnr = new JumpNRun(config.getConfigurationSection("jnrs." + jnrID), jnrID);
            System.out.println("Loaded Jump n Run: " + jnrID);

            database.addJNR(jnrID);
            jnrs.add(jnr);
        }
    }

    private final ArrayList<JumpNRun> jnrs = new ArrayList<>();

    public ArrayList<JumpNRun> getJumpNRuns() {
        return jnrs;
    }

    public JumpNRun getJNRForPlayer(Player p) {
        for (JumpNRun jnr : jnrs) {
            if (jnr.isPlayerPlaying(p)) {
                return jnr;
            }
        }
        return null;
    }

    @EventHandler
    public void onPressurePlate(PlayerInteractEvent e) {
        if (!e.getAction().equals(Action.PHYSICAL)) return;
        if (!Util.isPressurePlate(e.getClickedBlock())) return;

        long time = System.currentTimeMillis();
        if (time - lastPPuses.getOrDefault(e.getPlayer(), 0L) < 1000) return;
        lastPPuses.put(e.getPlayer(), time);

        GetJNRResult jnrPlate = getJNRPlate(e.getClickedBlock());
        if (jnrPlate.getType().equals(PressurePlateType.NONE) | jnrPlate.getJnr() == null) return;
        jnrPlate.getJnr().onPlate(e.getPlayer(), jnrPlate.getType(), jnrPlate.getBlock());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent e) {
        JumpNRun jnr = getJNRForPlayer(e.getPlayer());
        if (jnr == null) return;
        if (USED_SLOTS.contains(e.getPlayer().getInventory().getHeldItemSlot())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        JumpNRun jnr = getJNRForPlayer((Player) e.getWhoClicked());
        if (jnr == null) return;
        if (!(e.getInventory() instanceof PlayerInventory)) return;
        if (USED_SLOTS.contains(e.getSlot())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (!e.getCause().equals(EntityDamageEvent.DamageCause.LAVA)) return;
        if (!(e.getEntity() instanceof Player)) return;
        Player p = (Player) e.getEntity();
        JumpNRun jnr = getJNRForPlayer(p);
        if (jnr == null) return;
        if (!jnr.isCancelInLava()) return;

        JumpingPlayer player = jnr.getJumpingPlayer(p);
        player.backToCheckPoint();
        p.setHealth(20);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInteract(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK && e.getAction() != Action.RIGHT_CLICK_AIR) return;
        JumpNRun jnr = getJNRForPlayer(e.getPlayer());
        if (jnr == null) return;
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK) e.setCancelled(true);
        int currentSlot = e.getPlayer().getInventory().getHeldItemSlot();
        if (USED_SLOTS.contains(currentSlot)) {
            e.setCancelled(true);
            JumpingPlayer p = jnr.getJumpingPlayer(e.getPlayer());
            if (currentSlot == 2) {
                p.backToCheckPoint();
                return;
            }
            if (currentSlot == 3) {
                p.restart(true);
                return;
            }
            if (currentSlot == 5) {
                jnr.removePlayer(p);
            }
        }
    }

    @EventHandler
    public void onFly(PlayerToggleFlightEvent e) {
        JumpNRun jnr = getJNRForPlayer(e.getPlayer());
        if (jnr == null) return;
        e.getPlayer().sendMessage(jnr.getPrefix() + Main.getInstance().getMessages().getFlyMessage());
        jnr.removePlayer(jnr.getJumpingPlayer(e.getPlayer()));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        JumpNRun jnr = getJNRForPlayer(e.getPlayer());
        if (jnr == null) return;
        jnr.removePlayer(jnr.getJumpingPlayer(e.getPlayer()));
    }

    public boolean containsLocation(List<Location> it, Block b) {
        for (Location l : it) {
            if (l.getBlock().getLocation().equals(b.getLocation())) {
                return true;
            }
        }
        return false;
    }

    public GetJNRResult getJNRPlate(Block b) {
        for (JumpNRun jnr : jnrs) {
            if (jnr.getEndPlate().getBlock().equals(b)) {
                return new GetJNRResult(jnr, PressurePlateType.END, b);
            }
            if (jnr.getStartPlate().getBlock().equals(b)) {
                return new GetJNRResult(jnr, PressurePlateType.START, b);
            }
            if (containsLocation(jnr.getCheckPoints(), b)) {
                return new GetJNRResult(jnr, PressurePlateType.CHECKPOINT, b);
            }
        }
        return new GetJNRResult(null, PressurePlateType.NONE, null);
    }

    private static class GetJNRResult {

        private final JumpNRun jnr;
        private final PressurePlateType type;
        private final Block block;

        public GetJNRResult(JumpNRun jnr, PressurePlateType type, Block block) {
            this.jnr = jnr;
            this.type = type;
            this.block = block;
        }

        public Block getBlock() {
            return block;
        }

        public JumpNRun getJnr() {
            return jnr;
        }

        public PressurePlateType getType() {
            return type;
        }
    }

    public enum PressurePlateType {
        START,
        END,
        CHECKPOINT,
        NONE
    }

    public List<String> getJNRNames() {
        return jnrs.stream().map(JumpNRun::getId).collect(Collectors.toList());
    }

    public JumpNRun getById(String id) {
        for (JumpNRun jnr : jnrs) {
            if (jnr.getId().equals(id)) {
                return jnr;
            }
        }
        return null;
    }
}

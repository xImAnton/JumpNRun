package de.ximanton.jumpnrun.util;

import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;

public class Hologram {

    private final ArrayList<Entity> armorStands = new ArrayList<>();
    private final Location location;

    public Hologram(Location location) {
        this.location = location;
    }

    public void addLine(String text) {
        Location newLoc = new Location(location.getWorld(), location.getX(), location.getY() - (armorStands.size() * 0.25d), location.getZ());
        ArmorStand armorStand = (ArmorStand) location.getWorld().spawnEntity(newLoc, EntityType.ARMOR_STAND);
        armorStand.setInvisible(true);
        armorStand.setInvulnerable(true);
        armorStand.setCustomNameVisible(true);
        armorStand.setGravity(false);
        armorStand.setCustomName(text);
        armorStand.setAI(false);
        armorStand.setCollidable(false);
        armorStand.setSmall(true);
        armorStand.setBasePlate(false);
        armorStand.setVisible(false);
        armorStand.addScoreboardTag("hologram");
        armorStands.add(armorStand);
    }

    public void setLine(int index, String text) {
        armorStands.get(index).setCustomName(text);
    }

    public void destroy() {
        for (Entity entity : armorStands) {
            entity.remove();
        }
    }

}

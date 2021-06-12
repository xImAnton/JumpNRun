package de.ximanton.jumpnrun.util;

import de.ximanton.jumpnrun.Main;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.TimeZone;

public abstract class Util {

    private static List<Material> pressurePlates = Arrays.asList(
            Material.ACACIA_PRESSURE_PLATE,
            Material.BIRCH_PRESSURE_PLATE,
            Material.CRIMSON_PRESSURE_PLATE,
            Material.OAK_PRESSURE_PLATE,
            Material.DARK_OAK_PRESSURE_PLATE,
            Material.HEAVY_WEIGHTED_PRESSURE_PLATE,
            Material.DARK_OAK_PRESSURE_PLATE,
            Material.JUNGLE_PRESSURE_PLATE,
            Material.LIGHT_WEIGHTED_PRESSURE_PLATE,
            Material.WARPED_PRESSURE_PLATE,
            Material.STONE_PRESSURE_PLATE,
            Material.SPRUCE_PRESSURE_PLATE,
            Material.POLISHED_BLACKSTONE_PRESSURE_PLATE
    );

    public static Location locationFromString(String s, World world) {
        String[] coords = s.split(" ");
        double x = Double.parseDouble(coords[0]), y = Double.parseDouble(coords[1]), z = Double.parseDouble(coords[2]);
        if (coords.length == 3) {
            return new Location(world, x, y, z);
        }
        return new Location(world, x, y, z, Float.parseFloat(coords[3]), 0);
    }

    public static Location locationFromString(String s) {
        return locationFromString(s, Bukkit.getWorld("world"));
    }

    public static boolean isPressurePlate(Block ...blocks) {
        for (Block b : blocks) {
            if (!pressurePlates.contains(b.getType())) return false;
        }
        return true;
    }

    public static ItemStack getBackToCheckpointItem() {
        ItemStack i = new ItemStack(Material.HEAVY_WEIGHTED_PRESSURE_PLATE);
        ItemMeta meta = i.getItemMeta();
        meta.setDisplayName(Main.getInstance().getMessages().getBackToCheckpoint());
        i.setItemMeta(meta);
        return i;
    }

    public static ItemStack getResetItem() {
        ItemStack i = new ItemStack(Material.RED_CARPET);
        ItemMeta meta = i.getItemMeta();
        meta.setDisplayName(Main.getInstance().getMessages().getBackToStart());
        i.setItemMeta(meta);
        return i;
    }

    public static ItemStack getQuitItem() {
        ItemStack i = new ItemStack(Material.BARRIER);
        ItemMeta meta = i.getItemMeta();
        meta.setDisplayName(Main.getInstance().getMessages().getQuit());
        i.setItemMeta(meta);
        return i;
    }

    public static String formatTime(long time) {
        long minutes = Math.floorDiv(time, 60000L);
        long seconds = Math.floorDiv(time, 1000L) % 60;
        long millis = time % 1000L;
        return String.format("%02d:%02d:%03d", minutes, seconds, millis);
    }

    public static String formatDate(long time) {
        LocalDateTime date = LocalDateTime.ofInstant(Instant.ofEpochMilli(time), TimeZone.getDefault().toZoneId());
        return String.format("%02d. %02d. %d", date.getDayOfMonth(), date.getMonth().getValue(), date.getYear());
    }

}

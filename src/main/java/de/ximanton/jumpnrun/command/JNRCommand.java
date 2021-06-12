package de.ximanton.jumpnrun.command;

import de.ximanton.jumpnrun.Main;
import de.ximanton.jumpnrun.jnr.JNRManager;
import de.ximanton.jumpnrun.jnr.JumpNRun;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.permissions.Permissible;

import java.util.Collections;
import java.util.List;

public class JNRCommand implements CommandExecutor, TabCompleter {

    public static void register() {
        JNRCommand jnr = new JNRCommand();
        PluginCommand jnrCmd = Main.getInstance().getCommand("jumpnrun");
        jnrCmd.setExecutor(jnr);
        jnrCmd.setTabCompleter(jnr);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Please specify the action you want to dispatch!");
            return false;
        }
        List<String> allowed = getAllowedArgs(sender);
        if (!allowed.contains(args[0])) {
            sender.sendMessage(ChatColor.RED + "You don't have the permission to dispatch this action!");
            return false;
        }
        JumpNRun jnr;
        switch (args[0]) {
            case "reset":
                if (!requiresJNRName(sender, args)) return false;
                jnr = JNRManager.getInstance().getById(args[1]);
                jnr.reset();
                sender.sendMessage(jnr.getPrefix() + "Reset Scores!");
                break;
            case "reload":
                Main.getInstance().reload();
                sender.sendMessage(ChatColor.GREEN + "Reloaded Plugin!");
                break;
            case "resethighscore":
                if (!requiresJNRName(sender, args)) return false;
                jnr = JNRManager.getInstance().getById(args[1]);
                if (jnr.removeHighScore()) {
                    sender.sendMessage(ChatColor.GREEN + "Removed Highscore!");
                } else {
                    sender.sendMessage(ChatColor.RED + "Nothing to Delete!");
                }
            default:
                return false;
        }
        return true;
    }

    private boolean requiresJNRName(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Please specify a JNR you want to modify");
            return false;
        }
        if (!JNRManager.getInstance().getJNRNames().contains(args[1])) {
            sender.sendMessage(ChatColor.RED + "There is no JNR with that name!");
            return false;
        }
        return true;
    }

    public List<String> getAllowedArgs(Permissible p) {
        List<String> args = new java.util.ArrayList<>();
        if (p.hasPermission("jumpnrun.command.reset")) {
            args.add("reset");
            args.add("resethighscore");
        }
        if (p.hasPermission("jumpnrun.command.reload")) args.add("reload");
        return args;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> allowed = getAllowedArgs(sender);

        if (args.length < 2) {
            return allowed;
        }

        if (allowed.contains(args[0]) && args.length == 2) {
            return JNRManager.getInstance().getJNRNames();
        }

        return Collections.emptyList();
    }
}

package ir.syphix.home.utils;

import ir.syphix.home.Home;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class HomeTabCompleter implements TabCompleter {

    FileConfiguration config = Home.getInstance().getConfig();
    List<String> arguments = new ArrayList<>();

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player player) {
            if (arguments.isEmpty()) {
                if (player.hasPermission("home.delete")) {
                    arguments.add("delete");
                }
                if (player.hasPermission("home.go")) {
                    arguments.add("go");
                }
                if (player.hasPermission("home.list")) {
                    arguments.add("list");
                }
                if (player.hasPermission("home.set")) {
                    arguments.add("set");
                }
                if (player.hasPermission("home.info")) {
                    arguments.add("info");
                }
            }
            if (args.length == 1) {
                return arguments.stream().filter(argument -> argument.startsWith(args[0].toLowerCase())).collect(Collectors.toList());
            }
            if (args.length == 2 && (args[0].equals("go") || args[0].equals("info") || args[0].equals("delete"))) {
                if (config.getConfigurationSection("players_home." + player.getUniqueId()) == null) return null;
                if (!player.hasPermission("home." + args[0])) {
                    return null;
                }

                List<String> homes = new ArrayList<>(config.getConfigurationSection("players_home." + player.getUniqueId() + ".homes").getKeys(false));
                if (args[1].isEmpty()) {
                    return homes;
                } else {
                    return homes.stream().filter(home -> home.toLowerCase().startsWith(args[1])).collect(Collectors.toList());
                }
            }
        }
        return null;
    }
}

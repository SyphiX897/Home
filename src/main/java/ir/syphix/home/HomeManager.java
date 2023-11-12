package ir.syphix.home;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class HomeManager {

    public static List<UUID> IS_IN_TELEPORT = new ArrayList<>();
    private static final FileConfiguration config = Home.getInstance().getConfig();
    private static final String rootSectionName = "players_home";
    public static String getHomeName(Player player, String homeName) {
        ConfigurationSection homeSection = getSection(player, rootSectionName, player.getUniqueId().toString(), "homes");
        for (String home : homeSection.getKeys(false)) {
            if (home.equals(homeName)) {
                return home;
            }
        }
        return null;
    }
    public static String getWorldName(Player player, String homeName) {
        ConfigurationSection homeSection = getSection(player, rootSectionName, player.getUniqueId().toString(), "homes", homeName);
        return homeSection.getString("world");
    }
    public static double getX(Player player, String homeName) {
        ConfigurationSection homeSection = getSection(player, rootSectionName, player.getUniqueId().toString(), "homes", homeName);
        return homeSection.getDouble("x");
    }
    public static double getY(Player player, String homeName) {
        ConfigurationSection homeSection = getSection(player, rootSectionName, player.getUniqueId().toString(), "homes", homeName);
        return homeSection.getDouble("y");
    }
    public static double getZ(Player player, String homeName) {
        ConfigurationSection homeSection = getSection(player, rootSectionName, player.getUniqueId().toString(), "homes", homeName);
        return homeSection.getDouble("z");
    }
    public static double getYaw(Player player, String homeName) {
        ConfigurationSection homeSection = getSection(player, rootSectionName, player.getUniqueId().toString(), "homes", homeName);
        return homeSection.getDouble("yaw");
    }
    public static double getPitch(Player player, String homeName) {
        ConfigurationSection homeSection = getSection(player, rootSectionName, player.getUniqueId().toString(), "homes", homeName);
        return homeSection.getDouble("pitch");
    }


    public static ConfigurationSection getSection(Player player, String... sections) {
        String joinedSections = String.join(".", sections);
        ConfigurationSection section = config.getConfigurationSection(joinedSections);
        if (section == null) {
            player.sendMessage(MiniMessage.miniMessage().deserialize("<gradient:dark_red:red>This section doesn't exist!"));
            return null;
        }
        return section;
    }
}

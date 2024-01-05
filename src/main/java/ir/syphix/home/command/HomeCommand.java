package ir.syphix.home.command;

import ir.syphix.home.Home;
import ir.syphix.home.HomeManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class HomeCommand implements CommandExecutor {
    FileConfiguration config = Home.getInstance().getConfig();
    String rootSectionName = "players_home";

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player player) {
            if (args.length == 0) {
                player.sendMessage(toComponent("<gradient:dark_red:red>Too few argument"));
                return true;
            }

            switch (args[0]) {
                case "set" -> {
                    if (hasPermission(player, "set")) {
                        return true;
                    }

                    if (missingArgument(args, player)) return true;

                    int homeLimit = config.getInt("home_limit");
                    if (getSection(rootSectionName, player.getUniqueId().toString(), "homes") != null && getSection(rootSectionName, player.getUniqueId().toString(), "homes").getKeys(false).size() >= homeLimit) {
                        player.sendMessage(toComponent(String.format("<gradient:dark_red:red>The number of permitted houses is %d houses!", homeLimit)));
                        return true;
                    }

                    setLocation(player, args[1]);
                }
                case "go" -> {
                    if (hasPermission(player, "go")) {
                        return true;
                    }

                    if (missingArgument(args, player)) return true;
                    teleportToHomeLocation(player, args[1]);
                }
                case "delete" -> {
                    if (hasPermission(player, "delete")) {
                        return true;
                    }

                    if (missingArgument(args, player)) return true;
                    deleteHome(player, args[1]);
                }
                case "list" -> {
                    if (hasPermission(player, "list")) {
                        return true;
                    }

                    homeList(player);
                }
                case "info" -> {
                    if (hasPermission(player, "info")) {
                        return true;
                    }
                    homesInfo(player, args[1]);
                }
            }
        }
        return true;
    }

    public void setLocation(Player player, String homeName) {
        Location playerLocation = player.getLocation();
        UUID playerUUID = player.getUniqueId();

        if (player.getLocation().getWorld().getBlockAt((int) playerLocation.getX(), (int) playerLocation.getY() - 1, (int) playerLocation.getZ()).getType().equals(Material.AIR)) {
            player.sendMessage(toComponent("<gradient:dark_red:red>You can't create your home on air!"));
            return;
        }

        ConfigurationSection rootSection = getSection(rootSectionName);

        if (rootSection == null) {
            player.sendMessage(toComponent("<gradient:dark_red:red>Can't find rootSection in config.yml file, please report it to server admins!"));
            return;
        }

        if (rootSection.getConfigurationSection(playerUUID.toString()) == null) {
            rootSection.createSection(playerUUID.toString());
        }
        if (rootSection.getConfigurationSection(playerUUID.toString() + ".homes") == null) {
            rootSection.getConfigurationSection(playerUUID.toString()).createSection("homes");
        }

        if (rootSection.getConfigurationSection(playerUUID.toString()).getString("player_name") == null) {
            rootSection.getConfigurationSection(playerUUID.toString()).set("player_name", player.getName());
        }

        if (rootSection.getConfigurationSection(playerUUID + ".homes." + homeName) != null) {
            player.sendMessage(toComponent("<gradient:dark_red:red>This home is already exist!"));
            return;
        }

        ConfigurationSection homeSection = getSection(rootSectionName, playerUUID.toString(), "homes", homeName);
        if (homeSection == null) {
            getSection(rootSectionName, playerUUID.toString(), "homes");
            homeSection = config.getConfigurationSection("players_home." + playerUUID + ".homes").createSection(homeName);
        }

        World playerWorld = player.getWorld();
        float playerPitch = player.getEyeLocation().getPitch();
        float playerYaw = player.getEyeLocation().getYaw();
        double x = playerLocation.getX();
        double y = playerLocation.getY();
        double z = playerLocation.getZ();


        homeSection.set("world", playerWorld.getName());
        homeSection.set("x", x);
        homeSection.set("y", y);
        homeSection.set("z", z);
        homeSection.set("yaw", playerYaw);
        homeSection.set("pitch", playerPitch);

        Home.getInstance().saveConfig();

        player.sendMessage(toComponent("<gradient:dark_green:green>Home location has been successfully set."));
    }

    public void teleportToHomeLocation(Player player, String homeName) {
        UUID playerUUID = player.getUniqueId();

        ConfigurationSection homeSection = getSection(rootSectionName, playerUUID.toString(), "homes", homeName);

        if (sectionIsNotExist(homeSection, player, "<gradient:dark_red:red>This home doesn't exist!")) return;

        World world = Bukkit.getWorld(homeSection.getString("world", "world"));
        float yaw = (float) homeSection.getDouble("yaw");
        float pitch = (float) homeSection.getDouble("pitch");
        double x = homeSection.getDouble("x");
        double y = homeSection.getDouble("y");
        double z = homeSection.getDouble("z");

        Location homeLocation = new Location(world, x, y, z, yaw, pitch);

        int cooldown = config.getInt("home_cooldown");

        Location playerLocation = player.getLocation().clone();

        player.sendMessage(toComponent(String.format("<gradient:dark_green:green>You will be teleported in <yellow>%d</yellow> seconds, <yellow>please don't move!", cooldown)));
        HomeManager.IS_IN_TELEPORT.add(playerUUID);

        if (config.getBoolean("cancel_teleport_on_move")) {
            Bukkit.getScheduler().runTaskTimer(Home.getInstance(), task -> {
                if (player.getLocation().distance(playerLocation) >= 1 && HomeManager.IS_IN_TELEPORT.contains(playerUUID)) {
                    task.cancel();
                    HomeManager.IS_IN_TELEPORT.remove(playerUUID);
                    player.sendMessage(toComponent("<gradient:dark_red:red>You moved, Teleportation process canceled!"));
                }
            }, 1, 1);
        }

        Bukkit.getScheduler().runTaskLater(Home.getInstance(), task -> {
            if (HomeManager.IS_IN_TELEPORT.contains(playerUUID)) {
                player.teleport(homeLocation);
                HomeManager.IS_IN_TELEPORT.remove(playerUUID);
            }
        }, (cooldown * 20L));
    }
    public void deleteHome(Player player, String homeName) {
        UUID playerUUID = player.getUniqueId();

        ConfigurationSection homesSection = getSection(rootSectionName, playerUUID.toString(), "homes");

        if (homesSection == null) {
            player.sendMessage(toComponent("<gradient:dark_red:red>This home doesn't exist!"));
            return;
        }

        if (sectionIsNotExist(homesSection.getConfigurationSection(homeName), player, "<gradient:dark_red:red>This home doesn't exist!")) return;

        if (sectionIsNotExist(homesSection, player, "<gradient:dark_red:red>This player doesn't have any home!")) return;

        homesSection.set(homeName, null);
        Home.getInstance().saveConfig();
        player.sendMessage(toComponent("<gradient:dark_green:green>home has been successfully deleted."));
    }
    public void homeList(Player player) {
        UUID playerUUID = player.getUniqueId();

        ConfigurationSection homesSection = getSection(rootSectionName, playerUUID.toString(), "homes");

        if (sectionIsNotExist(homesSection, player, "<gradient:dark_red:red>This player doesn't have any home!")) return;

        if (homesSection.getKeys(false).isEmpty()) {
            player.sendMessage(toComponent("<gradient:dark_red:red>You dont have any home!"));
            return;
        }

        player.sendMessage(toComponent("<gradient:dark_green:green>Homes: <yellow>" + String.join(" <green>|</green><yellow> ", homesSection.getKeys(false))));
    }
    public void homesInfo(Player player, String homeName) {
        ConfigurationSection homeSection = getSection(rootSectionName, player.getUniqueId().toString(), "homes", homeName);
        if (sectionIsNotExist(homeSection, player, "<gradient:dark_red:red>This home doesn't exist!")) return;

        String home = HomeManager.getHomeName(player, homeName);
        String world = HomeManager.getWorldName(player, homeName);
        int x = (int) HomeManager.getX(player, homeName);
        int y = (int) HomeManager.getY(player, homeName);
        int z = (int) HomeManager.getZ(player, homeName);
        int yaw = (int) HomeManager.getYaw(player, homeName);
        int pitch = (int) HomeManager.getPitch(player, homeName);

        player.sendMessage(toComponent("<#a3ff05>]=------------- Home info -------------=[</#a3ff05>"));
        player.sendMessage(toComponent("<gradient:dark_green:green>Name: <yellow>" + home));
        player.sendMessage(toComponent("<gradient:dark_green:green>World: <yellow>" + world));
        player.sendMessage(toComponent("<gradient:dark_green:green>X: <yellow>" + x + " <gradient:dark_green:green>Y: <yellow>" + y + " <gradient:dark_green:green>Z: <yellow>" + z));
        player.sendMessage(toComponent("<gradient:dark_green:green>Yaw: <yellow>" + yaw + " <gradient:dark_green:green>Pitch: <yellow>" + pitch));
        player.sendMessage(toComponent("                      <click:run_command:/home go " + homeName + "><green><underlined>Teleport</click>"));
        player.sendMessage(toComponent("<#a3ff05>]=-----------------------------------=[</#a3ff05>"));
    }

    public boolean missingArgument(String[] args, Player player) {
        if (args.length < 2) {
            player.sendMessage(toComponent("<gradient:dark_red:red>Missing home name"));
            return true;
        }
        return false;
    }
    public boolean sectionIsNotExist(ConfigurationSection section, Player player, String message) {
        if (section == null) {
            player.sendMessage(toComponent(message));
            return true;
        }
        return false;
    }

    public ConfigurationSection getSection(String... sections) {
        String joinedSections = String.join(".", sections);
        return config.getConfigurationSection(joinedSections);
    }
    public boolean hasPermission(Player player, String permission) {
        if (!player.hasPermission("home." + permission)) {
            player.sendMessage(toComponent("<gradient:dark_red:red>You dont have permission to use this command!"));
            return true;
        }
        return false;
    }

    public Component toComponent(String content) {
        return MiniMessage.miniMessage().deserialize(content);
    }
}

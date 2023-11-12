package ir.syphix.home;

import ir.syphix.home.command.HomeCommand;
import ir.syphix.home.listener.PlayerMoveListener;
import ir.syphix.home.utils.HomeTabCompleter;
import org.bukkit.plugin.java.JavaPlugin;

public final class Home extends JavaPlugin {

    public static Home instance;
    public static Home getInstance() {
        return instance;
    }


    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        getCommand("Home").setExecutor(new HomeCommand());
        getCommand("Home").setTabCompleter(new HomeTabCompleter());
        if (getConfig().getBoolean("cancel_teleport_on_move")) {
            getServer().getPluginManager().registerEvents(new PlayerMoveListener(), this);
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}

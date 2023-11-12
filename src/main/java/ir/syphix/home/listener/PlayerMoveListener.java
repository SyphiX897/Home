package ir.syphix.home.listener;

import ir.syphix.home.HomeManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerMoveListener implements Listener {

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        HomeManager.IS_IN_TELEPORT.remove(event.getPlayer().getUniqueId());
    }
}

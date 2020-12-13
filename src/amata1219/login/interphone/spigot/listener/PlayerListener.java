package amata1219.login.interphone.spigot.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {

    @EventHandler
    public void on(PlayerJoinEvent event) {
        event.setJoinMessage("");
    }

    @EventHandler
    public void on(PlayerQuitEvent event) {
        event.setQuitMessage("");
    }

}

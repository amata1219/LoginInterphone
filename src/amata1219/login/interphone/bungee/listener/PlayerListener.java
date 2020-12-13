package amata1219.login.interphone.bungee.listener;

import amata1219.login.interphone.Channels;
import amata1219.login.interphone.bungee.Main;
import amata1219.login.interphone.bungee.setting.EventActionSetting.EventType;
import amata1219.redis.plugin.messages.common.RedisPluginMessagesAPI;
import amata1219.redis.plugin.messages.common.io.ByteIO;
import com.google.common.io.ByteArrayDataOutput;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class PlayerListener implements Listener {

    private final Main plugin = Main.instance();
    private final RedisPluginMessagesAPI redis;

    public PlayerListener(RedisPluginMessagesAPI redis) {
        this.redis = redis;
    }

    @EventHandler
    public void onJoin(ServerSwitchEvent event){
        ProxiedPlayer player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();

        HashMap<UUID, String> currentServers = plugin.playersToCurrentServers;

        if(currentServers.containsKey(playerUUID)) return;

        schedule(250, TimeUnit.MILLISECONDS, () -> {
            ServerInfo server = player.getServer().getInfo();
            currentServers.put(playerUUID, server.getName());

            ByteArrayDataOutput out = ByteIO.newDataOutput();
            out.writeUTF(playerUUID.toString());

            redis.publisher().sendRedisMessage(Channels.CHECK, out);
        });
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onSwitch(ServerSwitchEvent e){
        ProxiedPlayer player = e.getPlayer();
        UUID playerUUID = player.getUniqueId();

        HashMap<UUID, String> currentServers = plugin.playersToCurrentServers;

        if(!currentServers.containsKey(playerUUID)) return;

        schedule(1, TimeUnit.SECONDS, () -> {
            String serverName = player.getServer().getInfo().getName();
            plugin.textSender().sendMessage(EventType.SWITCH, player.getName(), serverName, currentServers.get(playerUUID));
            plugin.soundPlayer().playSound(EventType.SWITCH);

            currentServers.put(playerUUID, serverName);
        });
    }

    @EventHandler
    public void onQuit(PlayerDisconnectEvent e){
        ProxiedPlayer player = e.getPlayer();
        UUID playerUUID = player.getUniqueId();

        HashMap<UUID, String> currentServers = plugin.playersToCurrentServers;

        plugin.textSender().sendMessage(EventType.QUIT, player.getName(), currentServers.get(playerUUID), null);
        plugin.soundPlayer().playSound(EventType.QUIT);

        currentServers.remove(playerUUID);
        plugin.quitters.add(playerUUID);

        schedule(plugin.rejoinTicks(), TimeUnit.SECONDS, () -> plugin.quitters.remove(playerUUID));
    }

    private void schedule(int delay, TimeUnit unit, Runnable action){
        plugin.getProxy().getScheduler().schedule(plugin, action, delay, unit);
    }

}

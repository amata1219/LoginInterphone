package amata1219.login.interphone.bungee.subscriber;

import amata1219.login.interphone.bungee.Main;
import amata1219.login.interphone.bungee.setting.EventActionSetting.EventType;
import amata1219.redis.plugin.messages.common.RedisSubscriber;
import com.google.common.io.ByteArrayDataInput;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.UUID;

public class ResultSubscriber implements RedisSubscriber {

    private final Main plugin = Main.instance();

    @Override
    public void onRedisMessageReceived(String sourceServerName, ByteArrayDataInput in) {
        UUID uuid = UUID.fromString(in.readUTF());
        ProxiedPlayer player = plugin.getProxy().getPlayer(uuid);

        if(!player.isConnected()) return;

        String serverName = plugin.playersToCurrentServers.get(uuid);
        if (!serverName.equals(sourceServerName)) return;

        boolean hasPlayedBefore = in.readBoolean();
        EventType type;
        if (hasPlayedBefore) {
            if (plugin.quitters.contains(uuid)) type = EventType.REJOIN;
            else type = EventType.JOIN;
        } else {
            type = EventType.FIRST_JOIN;
        }

        boolean isBanned = in.readBoolean();
        if(isBanned) return;

        plugin.soundPlayer().playSound(type);
        plugin.textSender().sendMessage(type, player.getName(), serverName, null);
    }

}

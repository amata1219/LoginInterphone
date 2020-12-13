package amata1219.login.interphone.spigot.subscriber;

import amata1219.login.interphone.Channels;
import amata1219.login.interphone.spigot.MCBansBridge;
import amata1219.redis.plugin.messages.common.RedisPluginMessagesAPI;
import amata1219.redis.plugin.messages.common.RedisSubscriber;
import amata1219.redis.plugin.messages.common.io.ByteIO;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.UUID;

public class CheckSubscriber implements RedisSubscriber {

    private final RedisPluginMessagesAPI redis;
    private final MCBansBridge mcBans;

    public CheckSubscriber(RedisPluginMessagesAPI redis, MCBansBridge mcBans) {
        this.redis = redis;
        this.mcBans = mcBans;
    }

    @Override
    public void onRedisMessageReceived(String sourceServerName, ByteArrayDataInput in) {
        String playerUUID = in.readUTF();
        OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.fromString(playerUUID));

        boolean isBanned = mcBans != null && mcBans.isBanned(player.getName());

        ByteArrayDataOutput out = ByteIO.newDataOutput();
        out.writeUTF(playerUUID);
        out.writeBoolean(player.hasPlayedBefore());
        out.writeBoolean(isBanned);

        redis.publisher().sendRedisMessage(Channels.RESULT, out);
    }

}

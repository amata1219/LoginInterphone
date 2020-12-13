package amata1219.login.interphone.bungee.messenger;

import amata1219.login.interphone.Channels;
import amata1219.login.interphone.bungee.Main;
import amata1219.login.interphone.bungee.setting.EventActionSetting.EventType;
import amata1219.login.interphone.bungee.setting.ServerSetting;
import amata1219.login.interphone.bungee.setting.SoundPlaySetting;
import amata1219.redis.plugin.messages.common.RedisPluginMessagesAPI;
import amata1219.redis.plugin.messages.common.io.ByteIO;
import com.google.common.io.ByteArrayDataOutput;
import net.md_5.bungee.api.config.ServerInfo;

import java.util.HashMap;

public class SoundPlayer {

    private final Main plugin = Main.instance();
    private final RedisPluginMessagesAPI redis;

    public SoundPlayer(RedisPluginMessagesAPI redis) {
        this.redis = redis;
    }

    public void playSound(EventType event){
        HashMap<String, ServerSetting> settings = plugin.settings;
        for(ServerInfo server : plugin.getProxy().getServers().values()){
            String serverName = server.getName();
            if(!settings.containsKey(serverName) || server.getPlayers().isEmpty()) continue;

            SoundPlaySetting sps = settings.get(serverName).eass.get(event).sps;
            if(!sps.playable) continue;

            ByteArrayDataOutput out = ByteIO.newDataOutput();

            out.writeUTF(sps.name);
            out.writeInt(sps.repetitions);
            out.writeInt(sps.interval);
            out.writeFloat(sps.volume);
            out.writeFloat(sps.pitch);

            redis.publisher().sendRedisMessage(Channels.PLAY, out);
        }
    }

}

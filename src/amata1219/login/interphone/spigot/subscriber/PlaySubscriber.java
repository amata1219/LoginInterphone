package amata1219.login.interphone.spigot.subscriber;

import amata1219.login.interphone.spigot.Main;
import amata1219.redis.plugin.messages.common.RedisSubscriber;
import com.google.common.io.ByteArrayDataInput;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class PlaySubscriber implements RedisSubscriber {

    @Override
    public void onRedisMessageReceived(String sourceServerName, ByteArrayDataInput in) {
        Sound sound = Sound.valueOf(in.readUTF());

        int repetition = in.readInt();
        int interval = in.readInt();
        float volume = in.readFloat();
        float pitch = in.readFloat();

        int delay = 0;
        for(int i = 0; i < repetition; i++){
            Bukkit.getScheduler().runTaskLater(Main.instance(), () -> {
                for(Player player : Bukkit.getOnlinePlayers()) player.playSound(player.getLocation(), sound, volume, pitch);
            }, delay);
            delay += interval;
        }
    }

}

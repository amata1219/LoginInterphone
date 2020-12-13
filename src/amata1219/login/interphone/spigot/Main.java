package amata1219.login.interphone.spigot;

import amata1219.login.interphone.Channels;
import amata1219.login.interphone.spigot.listener.PlayerListener;
import amata1219.login.interphone.spigot.subscriber.CheckSubscriber;
import amata1219.login.interphone.spigot.subscriber.PlaySubscriber;
import amata1219.redis.plugin.messages.common.RedisPluginMessagesAPI;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public class Main extends JavaPlugin {

	private static Main instance;

	private final RedisPluginMessagesAPI redis = (RedisPluginMessagesAPI) getServer().getPluginManager().getPlugin("RedisPluginMessages");
	private MCBansBridge mcBansBridge;

	@Override
	public void onEnable(){
		instance = this;

		Plugin mcBans = getServer().getPluginManager().getPlugin("MCBans");
		if(mcBans != null) mcBansBridge = MCBansBridge.load(mcBans);

		registerEventListeners(
				new PlayerListener()
		);

		redis.registerIncomingChannels(Channels.CHECK, Channels.PLAY);
		redis.registerSubscriber(Channels.CHECK, new CheckSubscriber(redis, mcBansBridge));
		redis.registerSubscriber(Channels.PLAY, new PlaySubscriber());

		redis.registerOutgoingChannels(Channels.RESULT);
	}

	private void registerEventListeners(Listener... listeners) {
		for (Listener listener : listeners) getServer().getPluginManager().registerEvents(listener, this);
	}

	@Override
	public void onDisable(){
		HandlerList.unregisterAll(this);
	}

	public static Main instance(){
		return instance;
	}

}

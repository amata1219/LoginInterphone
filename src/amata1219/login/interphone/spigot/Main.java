package amata1219.login.interphone.spigot;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.Messenger;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.scheduler.BukkitRunnable;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import amata1219.login.interphone.bungee.Channel;

public class Main extends JavaPlugin implements Listener, PluginMessageListener {

	private static Main plugin;

	private MCBansBridge mcbans;

	@Override
	public void onEnable(){
		plugin = this;
		
		PluginManager pm = getServer().getPluginManager();

		Plugin maybeMCBans = pm.getPlugin("MCBans");
		if(maybeMCBans != null) mcbans = MCBansBridge.load(maybeMCBans);

		pm.registerEvents(this, this);

		Messenger messenger = getServer().getMessenger();
		messenger.registerOutgoingPluginChannel(this, "BungeeCord");
		messenger.registerIncomingPluginChannel(this, "BungeeCord", this);
	}

	@Override
	public void onDisable(){
		HandlerList.unregisterAll((JavaPlugin) this);

		Messenger messenger = getServer().getMessenger();
		messenger.unregisterOutgoingPluginChannel(this, "BungeeCord");
		messenger.unregisterIncomingPluginChannel(this, "BungeeCord", this);
	}

	public static Main getPlugin(){
		return plugin;
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent e){
		e.setJoinMessage("");
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent e){
		e.setQuitMessage("");
	}

	@Override
	public void onPluginMessageReceived(String tag, Player repeater, byte[] data) {
		if(!tag.equalsIgnoreCase("BungeeCord") && !tag.equalsIgnoreCase("bungeecord:main")) return;

		ByteArrayDataInput in = ByteStreams.newDataInput(data);
		Channel channel = Channel.newInstance(in);

		if(!channel.read().equalsIgnoreCase(Channel.PACKET_ID)) return;

		if(channel.read().equalsIgnoreCase("CHECK")){
			ByteArrayDataOutput out = ByteStreams.newDataOutput();

			out.writeUTF(Channel.PACKET_ID);
			out.writeUTF("RESULT");
			out.writeUTF(channel.read());

			UUID uuid = UUID.fromString(channel.current());
			OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
			
			out.writeBoolean(player.hasPlayedBefore());
			
			boolean isBanned = mcbans == null ? false : mcbans.isBanned(player.getName());
			out.writeBoolean(isBanned);

			repeater.sendPluginMessage(this, "BungeeCord", out.toByteArray());
		}else if(channel.current().equalsIgnoreCase("PLAY")){
			Sound sound = Sound.valueOf(channel.read());

			int repeatitons = in.readInt(), interval = in.readInt();
			float volume = in.readFloat(), pitch = in.readFloat();

			int n = 0;
			for(int i = repeatitons; i > 0; i--){
				new BukkitRunnable(){
					@Override
					public void run(){
						for(Player player : Bukkit.getOnlinePlayers()) player.playSound(player.getLocation(), sound, volume, pitch);
					}
				}.runTaskLater(this, n);
				n += interval;
			}
		}
	}

}

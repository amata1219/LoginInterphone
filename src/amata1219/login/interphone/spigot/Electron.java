package amata1219.login.interphone.spigot;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.scheduler.BukkitRunnable;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import amata1219.login.interphone.bungee.Channel;

public class Electron extends JavaPlugin implements Listener, PluginMessageListener {

	private static Electron plugin;

	@Override
	public void onEnable(){
		plugin = this;

		getServer().getPluginManager().registerEvents(this, this);

		getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
		getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", this);
	}

	@Override
	public void onDisable(){
		HandlerList.unregisterAll((JavaPlugin) this);

		getServer().getMessenger().unregisterOutgoingPluginChannel(this, "BungeeCord");
		getServer().getMessenger().unregisterIncomingPluginChannel(this, "BungeeCord", this);
	}

	public static Electron getPlugin(){
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
	public void onPluginMessageReceived(String tag, Player player, byte[] data) {
		if(!tag.equalsIgnoreCase("BungeeCord") && !tag.equalsIgnoreCase("bungeecord:main"))
			return;

		Channel channel = Channel.newInstance(data);

		channel.read();
		if(!channel.get().equalsIgnoreCase(Channel.PACKET_ID))
			return;

		channel.read();
		if(channel.get().equalsIgnoreCase("CHECK")){
			ByteArrayDataOutput out = ByteStreams.newDataOutput();

			out.writeUTF(Channel.PACKET_ID);
			out.writeUTF("RESULT");

			channel.read();
			out.writeUTF(channel.get());
			out.writeBoolean(Bukkit.getOfflinePlayer(UUID.fromString(channel.get())).hasPlayedBefore());

			player.sendPluginMessage(this, "BungeeCord", out.toByteArray());
		}else if(channel.get().equalsIgnoreCase("PLAY")){
			channel.read();
			Sound sound = Sound.valueOf(channel.get());

			int repeat = channel.getByteArrayDataInput().readInt();

			int interval = channel.getByteArrayDataInput().readInt();

			float volume = channel.getByteArrayDataInput().readFloat();

			float pitch = channel.getByteArrayDataInput().readFloat();

			int n = 0;
			for(int i = repeat; i > 0; i--){
				new BukkitRunnable(){
					@Override
					public void run(){
						for(Player p : Bukkit.getOnlinePlayers())
							p.playSound(p.getLocation(), sound, volume, pitch);
					}
				}.runTaskLater(this, n);
				n += interval;
			}
		}
	}

}

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
		System.out.println(tag);

		if(!tag.equals("BungeeCord") && !tag.equals("BungeeCord"))
			return;

		Channel channel = Channel.newInstance(data);

		channel.read();
		if(!channel.get().equals(Channel.PACKET_ID))
			return;

		if(channel.get().equals("CHECK_JOIN_TYPE")){
			ByteArrayDataOutput out = ByteStreams.newDataOutput();

			out.writeUTF(Channel.PACKET_ID);
			out.writeUTF("RESULT_OF_JOIN_TYPE");

			channel.read();
			out.writeUTF(channel.get());
			out.writeUTF(String.valueOf(Bukkit.getOfflinePlayer(UUID.fromString(channel.get())).hasPlayedBefore()));

			player.sendPluginMessage(this, "BungeeCord", out.toByteArray());
		}else if(channel.get().equals("PLAY_SOUND")){
			channel.read();
			Sound sound = Sound.valueOf(channel.get());

			channel.read();
			float volume = Float.valueOf(channel.get());

			channel.read();
			float pitch = Float.valueOf(channel.get());

			for(Player p : Bukkit.getOnlinePlayers())
				p.playSound(p.getLocation(), sound, volume, pitch);
		}
	}

}

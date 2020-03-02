package amata1219.login.interphone.bungee;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import amata1219.login.interphone.bungee.setting.EventActionSetting;
import amata1219.login.interphone.bungee.setting.EventActionSetting.EventType;
import amata1219.login.interphone.bungee.setting.MessageDisplaySetting;
import amata1219.login.interphone.bungee.setting.MessageDisplaySetting.DisplayType;
import amata1219.login.interphone.bungee.setting.ServerSetting;
import amata1219.login.interphone.bungee.setting.ServerSettingLoading;
import amata1219.login.interphone.bungee.setting.SoundPlaySetting;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.Title;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

public class Main extends Plugin implements Listener {

	private static Main plugin;
	private static final TextComponent EMPTY_COMPONENT = new TextComponent(" ");

	private final ProxyServer proxy = getProxy();
	private final HashMap<String, ServerSetting> settings = new HashMap<>();
	
	private int rejoin = 60;

	@Override
	public void onEnable(){
		plugin = this;

		saveDefaultConfig(folder() + File.separator + "template.yml");

		loadServerSettings();

		proxy.registerChannel("bungeecord:main");

		PluginManager pm = proxy.getPluginManager();
		pm.registerCommand(this, new LoginInterphoneCommand());
		pm.registerListener(this, this);

		loadConfig();
	}

	@Override
	public void onDisable(){
		proxy.getPluginManager().unregisterListener(this);
		proxy.unregisterChannel("bungeecord:main");
	}

	public static Main getPlugin(){
		return plugin;
	}
	
	public void loadConfig(){
		File file = new File(folder(), "config.yml");
		
		if(!file.exists()){
			try{
				folder().mkdirs();
				file.createNewFile();
			}catch(IOException e){

			}

			try(
				FileOutputStream output = new FileOutputStream(file);
				InputStream input = getResourceAsStream("config.yml")
			){
				ByteStreams.copy(input, output);
			}catch(IOException e){
				e.printStackTrace();
			}
		}

		try{
			Configuration config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
			rejoin = config.getInt("Time to be rejoined");
		}catch(IOException e){
			e.printStackTrace();
		}
	}

	public Configuration saveDefaultConfig(String path){
		File file = new File(path);

		if(!file.exists()){
			try{
				folder().mkdirs();
				file.createNewFile();
			}catch(IOException e){

			}

			try(
				FileOutputStream output = new FileOutputStream(file);
				InputStream input = getResourceAsStream("template.yml")
			){
				ByteStreams.copy(input, output);
			}catch(IOException e){
				e.printStackTrace();
				return null;
			}
		}

		try{
           return ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
		}catch(IOException e){
			e.printStackTrace();
		}

		return null;
	}
	
	public void loadServerSettings(){
		settings.clear();
		
		String path = getDataFolder() + File.separator + "Servers";
		File directory = new File(path);
		if(!directory.exists()) directory.mkdir();
		
		for(File file : directory.listFiles()){
			String name = file.getName();
			if(!name.endsWith(".yml")) continue;
			
			Configuration config = saveDefaultConfig(path + File.separator + name);
			
			ServerSetting setting = ServerSettingLoading.load(config);
			settings.put(name.substring(0, name.length() - 4), setting);
		}
	}

	private final HashMap<UUID, String> currentServer = new HashMap<>();
	private final Set<UUID> playersWhoHasJustQuitted = new HashSet<>();

	@EventHandler
	public void onJoin(ServerSwitchEvent event){
		ProxiedPlayer player = event.getPlayer();
		UUID uuid = player.getUniqueId();
		
		if(currentServer.containsKey(uuid)) return;

		schedule(250, TimeUnit.MILLISECONDS, () -> {
			ServerInfo server = player.getServer().getInfo();
			currentServer.put(uuid, server.getName());

			ByteArrayDataOutput out = ByteStreams.newDataOutput();

			out.writeUTF(Channel.PACKET_ID);
			out.writeUTF("CHECK");
			out.writeUTF(uuid.toString());

			server.sendData("BungeeCord", out.toByteArray());
		});
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onSwitch(ServerSwitchEvent e){
		ProxiedPlayer player = e.getPlayer();
		UUID uuid = player.getUniqueId();
		
		if(!currentServer.containsKey(uuid)) return;

		schedule(1000, TimeUnit.MILLISECONDS, () -> {
			String serverName = player.getServer().getInfo().getName();

			displayMessage(EventType.SWITCH, player.getName(), serverName, currentServer.get(uuid));
			playSound(EventType.SWITCH);

			currentServer.put(uuid, serverName);
		});
	}
	

	@EventHandler
	public void onQuit(PlayerDisconnectEvent e){
		ProxiedPlayer player = e.getPlayer();
		UUID uuid = player.getUniqueId();

		displayMessage(EventType.QUIT, player.getName(), currentServer.get(uuid), null);
		playSound(EventType.QUIT);

		currentServer.remove(uuid);
		playersWhoHasJustQuitted.add(uuid);

		schedule(rejoin, TimeUnit.SECONDS, () -> playersWhoHasJustQuitted.remove(uuid));
	}
	

	@EventHandler
	public void onReceive(PluginMessageEvent e){
		String tag = e.getTag();
		
		if(!tag.equalsIgnoreCase("BungeeCord") && !tag.equalsIgnoreCase("bungeecord:main")) return;

		ByteArrayDataInput in = ByteStreams.newDataInput(e.getData());
		Channel channel = Channel.newInstance(in);

		if(!channel.read().equalsIgnoreCase(Channel.PACKET_ID)) return;
		
		if(!channel.read().equalsIgnoreCase("RESULT")) return;

		UUID uuid = UUID.fromString(channel.read());
		ProxiedPlayer player = getProxy().getPlayer(uuid);
		
		if(!player.isConnected()) return;

		boolean isFirstJoining = in.readBoolean();
		EventType type = isFirstJoining ? EventType.FIRST_JOIN : playersWhoHasJustQuitted.contains(uuid) ? EventType.REJOIN : EventType.JOIN;
		if(type == EventType.REJOIN) playersWhoHasJustQuitted.remove(uuid);

		boolean isBanned = in.readBoolean();
		if(isBanned) return;

		displayMessage(type, player.getName(), currentServer.get(uuid), null);
		playSound(type);
	}
	
	private void displayMessage(EventType event, String playerName, String currentServerName, String previousServerName){
		for(ServerInfo server : proxy.getServers().values()){
			String serverName = server.getName();
			if(!settings.containsKey(serverName) || server.getPlayers().isEmpty()) continue;
			
			EventActionSetting eas = settings.get(serverName).eass.get(event);
			String text = eas.text.replace("[player]", playerName);
			
			for(Entry<DisplayType, MessageDisplaySetting> entry : eas.mdss.entrySet()){
				MessageDisplaySetting mds = entry.getValue();
				if(!mds.displayable) continue;
				
				ServerSetting currentServerSetting = settings.get(currentServerName);
				if(event == EventType.SWITCH){
					ServerSetting previousServerSetting = settings.get(previousServerName);
					text = text.replace("[from_server]", previousServerSetting != null ? previousServerSetting.alias : "missing")
							.replace("[to_server]", currentServerSetting != null ? currentServerSetting.alias : "missing");
				}else{
					text = text.replace("[server]", currentServerSetting != null ? currentServerSetting.alias : "missing");
				}
				
				TextComponent component = new TextComponent(text);
				
				switch(entry.getKey()){
				case CHAT:{
					for(ProxiedPlayer player : server.getPlayers()) player.sendMessage(component);
					continue;
				}case ACTION_BAR:{
					AtomicInteger count = new AtomicInteger();
					
					TaskHolder holder = new TaskHolder();
					
					ScheduledTask task = schedule(0, 1, TimeUnit.SECONDS, () -> {
						for(ProxiedPlayer player : server.getPlayers()) player.sendMessage(ChatMessageType.ACTION_BAR, component);

						if(count.incrementAndGet() >= mds.duration){
							if(mds.duration % 2 == 0){
								holder.cancelTask();
								return;
							}

							schedule(1, TimeUnit.SECONDS, () -> {
								for(ProxiedPlayer player : server.getPlayers()) player.sendMessage(ChatMessageType.ACTION_BAR, EMPTY_COMPONENT);
								holder.cancelTask();
							});
						}
					});
					
					holder.setTask(task);
					continue;
				}case TITLE:{
					Title title = proxy.createTitle();
					title.title(component);
					title.stay(mds.duration);
					for(ProxiedPlayer player : server.getPlayers()) player.sendTitle(title);
					continue;
				}default:
					continue;
				}
			}
		}
	}

	private void playSound(EventType event){
		for(ServerInfo server : getProxy().getServers().values()){
			String serverName = server.getName();
			if(!settings.containsKey(serverName) || server.getPlayers().isEmpty()) continue;

			SoundPlaySetting sps = settings.get(serverName).eass.get(event).sps;
			if(!sps.playable) continue;
			
			ByteArrayDataOutput out = ByteStreams.newDataOutput();

			out.writeUTF(Channel.PACKET_ID);
			out.writeUTF("PLAY");
			out.writeUTF(sps.name);
			out.writeInt(sps.repetitions);
			out.writeInt(sps.interval);
			out.writeFloat(sps.volume);
			out.writeFloat(sps.pitch);

			server.sendData("BungeeCord", out.toByteArray());
		}
	}
	
	
	private ScheduledTask schedule(int delay, TimeUnit unit, Runnable action){
		return proxy.getScheduler().schedule(this, action, delay, unit);
	}
	
	private ScheduledTask schedule(int delay, int interval, TimeUnit unit, Runnable action){
		return proxy.getScheduler().schedule(this, action, delay, interval, unit);
	}
	
	private File folder(){
		return getDataFolder();
	}

	private static class TaskHolder {

		private ScheduledTask task;

		public void setTask(ScheduledTask task){
			this.task = task;
		}

		public void cancelTask(){
			if(task != null) task.cancel();
		}

	}

}

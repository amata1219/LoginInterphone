package amata1219.login.interphone.bungee;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import amata1219.login.interphone.Util;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.event.ServerDisconnectEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

public class Nucleus extends Plugin implements Listener {

	private static Nucleus plugin;

	public HashMap<String, ElectronData> electrons = new HashMap<>();

	private int rejoin = 15;

	@Override
	public void onEnable(){
		plugin = this;

		saveDefaultConfig(getDataFolder() + File.separator + "template.yml");

		load();

		getProxy().registerChannel("BungeeCord");

		getProxy().getPluginManager().registerListener(this, this);

		getProxy().getPluginManager().registerCommand(this, new Command("logininterphone", "login.interphone.logininterphone"){

			@Override
			public void execute(CommandSender sender, String[] args) {
				if(args.length == 0){
					sender.sendMessage(new TextComponent(ChatColor.AQUA + "LoginInterphone v" + Nucleus.getPlugin().getDescription().getVersion()));
				}else if(args[0].equalsIgnoreCase("reload")){
					load();

					sender.sendMessage(new TextComponent(ChatColor.AQUA + "コンフィグをリロードしました。"));
				}
			}

		});

		File file = new File(getDataFolder() + File.separator + "config.yml");

		if(!file.exists()){
			try{
				getDataFolder().mkdirs();

				file.createNewFile();
			}catch(IOException e){

			}

			try(FileOutputStream output = new FileOutputStream(file);
					InputStream input = getResourceAsStream("config.yml")){
					ByteStreams.copy(input, output);
			}catch(IOException e){
				e.printStackTrace();
			}
		}

		try{
           rejoin = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file).getInt("ReJoin");
		}catch(IOException e){
			e.printStackTrace();
		}
	}

	@Override
	public void onDisable(){
		getProxy().getPluginManager().unregisterListener(this);

		getProxy().unregisterChannel("BungeeCord");
	}

	public static Nucleus getPlugin(){
		return plugin;
	}

	public Configuration saveDefaultConfig(String path){
		File file = new File(path);

		if(!file.exists()){
			try{
				getDataFolder().mkdirs();

				file.createNewFile();
			}catch(IOException e){

			}

			try(FileOutputStream output = new FileOutputStream(file);
					InputStream input = getResourceAsStream("template.yml")){
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

	public void load(){
		electrons.clear();

		File directory = new File(getDataFolder() + File.separator + "Servers");
		if(!directory.exists())
			directory.mkdir();

		for(File file : directory.listFiles()){
			String name = file.getName();
			if(!name.endsWith(".yml"))
				continue;

			electrons.put(name.substring(0, name.length() - 4), new ElectronData(saveDefaultConfig(getDataFolder() + File.separator + "Servers" + File.separator + name)));
		}
	}

	public String get(String[] args, int index){
		if(args.length <= index)
			return "";
		else
			return args[index];
	}

	private HashMap<UUID, String> locs = new HashMap<>();
	private Set<UUID> cache = new HashSet<>();

	@EventHandler
	public void onJoin(ServerSwitchEvent e){
		ProxiedPlayer player = e.getPlayer();
		UUID uuid = player.getUniqueId();
		if(locs.containsKey(uuid))
			return;

		getProxy().getScheduler().schedule(this, new Runnable(){

			@Override
			public void run() {
				ServerInfo server = player.getServer().getInfo();
				locs.put(uuid, server.getName());

				ByteArrayDataOutput out = ByteStreams.newDataOutput();

				out.writeUTF(Channel.PACKET_ID);
				out.writeUTF("CHECK_JOIN_TYPE");
				out.writeUTF(uuid.toString());

				player.sendData("BungeeCord", out.toByteArray());

				//server.sendData("BungeeCord", out.toByteArray());
			}

		}, 250, TimeUnit.MILLISECONDS);
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onSwitch(ServerSwitchEvent e){
		ProxiedPlayer player = e.getPlayer();
		UUID uuid = player.getUniqueId();
		if(!locs.containsKey(uuid))
			return;

		getProxy().getScheduler().schedule(this, new Runnable(){

			@Override
			public void run() {
				String serverName = player.getServer().getInfo().getName();

				sendMessage(Type.SWITCH, player.getName(), locs.get(uuid), serverName);
				playSound(Type.SWITCH);

				locs.put(uuid, serverName);
			}

		}, 1000, TimeUnit.MILLISECONDS);
	}

	@EventHandler
	public void onQuit(ServerDisconnectEvent e){
		ProxiedPlayer player = e.getPlayer();
		UUID uuid = player.getUniqueId();

		sendMessage(Type.QUIT, player.getName(),locs.get(uuid));
		playSound(Type.QUIT);

		locs.remove(uuid);
		cache.add(uuid);

		getProxy().getScheduler().schedule(this, new Runnable(){

			@Override
			public void run() {
				cache.remove(uuid);
			}

		}, rejoin, TimeUnit.SECONDS);

	}

	@EventHandler
	public void onReceive(PluginMessageEvent e){
		System.out.println(e.getTag());

		if(!e.getTag().equals("BungeeCord") && !e.getTag().equals("BungeeCord"))
			return;

		Channel channel = Channel.newInstance(e.getData());

		channel.read();
		if(!channel.get().equals(Channel.PACKET_ID))
			return;

		channel.read();
		if(!channel.get().equals("RESULT_OF_JOIN_TYPE"))
			return;

		channel.read();
		UUID uuid = UUID.fromString(channel.get());
		ProxiedPlayer player = getProxy().getPlayer(UUID.fromString(channel.get()));
		if(!player.isConnected())
			return;

		channel.read();

		Type type = Type.NORMAL_JOIN;

		if(channel.get().equals("false")){
			type = Type.FIRST_JOIN;
		}else if(channel.get().equals("true")){
			if(cache.contains(uuid)){
				type = Type.RE_JOIN;
				cache.remove(uuid);
			}
		}

		sendMessage(type, player.getName(), locs.get(uuid));
		playSound(type);
	}

	public void sendMessage(Type type, String playerName, String... serverNames){
		for(ServerInfo server : getProxy().getServers().values()){
			if(!electrons.containsKey(server.getName()))
				return;

			Settings settings = electrons.get(server.getName()).settings.get(type);
			if(!settings.isDisplay())
				continue;

			String text = Util.replaceAll(settings.getText(), "[player]", playerName);
			if(type == Type.SWITCH)
				text = Util.replaceAll(Util.replaceAll(text, "[from_server]", electrons.get(serverNames[0]).getAliases()), "[to_server]", electrons.get(serverNames[1]).getAliases());
			else
				text = Util.replaceAll(text, "[server]", electrons.get(serverNames[0]).getAliases());

			TextComponent component = new TextComponent(text);

			for(ProxiedPlayer player : server.getPlayers())
				player.sendMessage(component);
		}
	}

	public void playSound(Type type){
		for(ServerInfo server : getProxy().getServers().values()){
			if(!electrons.containsKey(server.getName()))
				return;

			Settings settings = electrons.get(server.getName()).settings.get(type);
			if(!settings.isPlay())
				return;

			ByteArrayDataOutput out = ByteStreams.newDataOutput();

			out.writeUTF(Channel.PACKET_ID);
			out.writeUTF("PLAY_SOUND");
			out.writeUTF(settings.getSound());
			out.writeUTF(String.valueOf(settings.getVolume()));
			out.writeUTF(String.valueOf(settings.getPitch()));

			byte[] array = out.toByteArray();

			int n = 0;
			for(int i = settings.getRepeat(); i > 0; i--){
				getProxy().getScheduler().schedule(this, new Runnable(){

					@Override
					public void run() {
						server.sendData("BungeeCord", array);
					}

				}, n, TimeUnit.MILLISECONDS);
				n += settings.getInterval();
			}
		}
	}

}

package amata1219.login.interphone.bungee;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.UUID;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;

import net.md_5.bungee.api.CommandSender;
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

	@Override
	public void onEnable(){
		plugin = this;

		saveDefaultConfig(getDataFolder() + File.separator + "template.yml");

		load();

		getProxy().getPluginManager().registerCommand(this, new Command("logininterphone", "login.interphone.logininterphone"){

			@Override
			public void execute(CommandSender sender, String[] args) {
			}

		});
	}

	@Override
	public void onDisable(){

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

	@EventHandler
	public void onJoin(ServerSwitchEvent e){
		ProxiedPlayer player = e.getPlayer();
		if(locs.containsKey(player.getUniqueId()))
			return;
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onSwitch(ServerSwitchEvent e){
		ProxiedPlayer player = e.getPlayer();
		if(!locs.containsKey(player.getUniqueId()))
			return;
	}

	@EventHandler
	public void onQuit(ServerDisconnectEvent e){

	}

	@EventHandler
	public void onReceive(PluginMessageEvent e){
		if(!e.getTag().equals("BungeeCord") && !e.getTag().equals("bungeecord:main"))
			return;

		Channel channel = Channel.newInstance(e.getData());

		channel.read();
		if(!channel.get().equals(Channel.PACKET_ID))
			return;

		channel.read();
		if(!channel.get().equals("CHECK_JOIN_TYPE"))
			return;

		channel.read();
		if(channel.get().equals("IS_FIRST_JOIN")){

		}else if(channel.get().equals("IS_NORMAL_JOIN")){

		}


	}

}

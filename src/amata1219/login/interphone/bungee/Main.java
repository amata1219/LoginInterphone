package amata1219.login.interphone.bungee;

import amata1219.login.interphone.Channels;
import amata1219.login.interphone.bungee.listener.PlayerListener;
import amata1219.login.interphone.bungee.messenger.SoundPlayer;
import amata1219.login.interphone.bungee.messenger.TextSender;
import amata1219.login.interphone.bungee.setting.ServerSetting;
import amata1219.login.interphone.bungee.setting.ServerSettingLoading;
import amata1219.login.interphone.bungee.subscriber.ResultSubscriber;
import amata1219.redis.plugin.messages.common.RedisPluginMessagesAPI;
import com.google.common.io.ByteStreams;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Main extends Plugin implements Listener {

	private static Main plugin;

	private final RedisPluginMessagesAPI redis = (RedisPluginMessagesAPI) getProxy().getPluginManager().getPlugin("RedisPluginMessages");

	private TextSender textSender;
	private SoundPlayer soundPlayer;

	public final HashMap<String, ServerSetting> settings = new HashMap<>();

	private int rejoin = 60;

	public final HashMap<UUID, String> playersToCurrentServers = new HashMap<>();
	public final Set<UUID> quitters = new HashSet<>();

	@Override
	public void onEnable(){
		plugin = this;

		redis.registerIncomingChannels(Channels.RESULT);
		redis.registerSubscriber(Channels.RESULT, new ResultSubscriber());

		redis.registerOutgoingChannels(Channels.CHECK, Channels.PLAY);

		textSender = new TextSender();
		soundPlayer = new SoundPlayer(redis);

		saveDefaultConfig(folder() + File.separator + "template.yml");

		loadServerSettings();

		getProxy().getPluginManager().registerCommand(this, new LoginInterphoneCommand());

		registerEventListeners(
				new PlayerListener(redis)
		);

		loadConfig();
	}

	private void registerEventListeners(Listener... listeners) {
		for (Listener listener : listeners) getProxy().getPluginManager().registerListener(this, listener);
	}

	@Override
	public void onDisable(){
		getProxy().getPluginManager().unregisterListener(this);
		getProxy().unregisterChannel("bungeecord:main");
	}

	public static Main instance(){
		return plugin;
	}

	public TextSender textSender() {
		return textSender;
	}

	public SoundPlayer soundPlayer() {
		return soundPlayer;
	}

	public int rejoinTicks() {
		return rejoin;
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

	private File folder(){
		return getDataFolder();
	}

}

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
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import net.md_5.bungee.api.scheduler.TaskScheduler;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

public class Main extends Plugin implements Listener {

	private static Main plugin;
	private static final TextComponent EMPTY_COMPONENT = new TextComponent(" ");

	public HashMap<String, ServerData> electrons = new HashMap<>();

	private int rejoin = 15;
	private boolean actionBarMessageEnable = false;
	private int actionBarMessageDuration = 5;

	@Override
	public void onEnable(){
		plugin = this;

		saveDefaultConfig(getDataFolder() + File.separator + "template.yml");

		load();

		getProxy().registerChannel("bungeecord:main");

		getProxy().getPluginManager().registerListener(this, this);

		getProxy().getPluginManager().registerCommand(this, new Command("logininterphone", "login.interphone.logininterphone"){

			@Override
			public void execute(CommandSender sender, String[] args) {
				if(args.length == 0){
					sender.sendMessage(new TextComponent(ChatColor.AQUA + "LoginInterphone v" + Main.getPlugin().getDescription().getVersion()));
				}else if(args[0].equalsIgnoreCase("reload")){
					load();

					loadConfig();

					sender.sendMessage(new TextComponent(ChatColor.AQUA + "設定を再読み込みしました。"));
				}
			}

		});

		loadConfig();

	}

	@Override
	public void onDisable(){
		getProxy().getPluginManager().unregisterListener(this);

		getProxy().unregisterChannel("bungeecord:main");
	}

	public static Main getPlugin(){
		return plugin;
	}

	public void loadConfig(){
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
			Configuration config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
           rejoin = config.getInt("ReJoin");
           actionBarMessageEnable = config.getBoolean("ActionBarMessage.Enable");
           actionBarMessageDuration = config.getInt("ActionBarMessage.Duration");
		}catch(IOException e){
			e.printStackTrace();
		}
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

			electrons.put(name.substring(0, name.length() - 4), new ServerData(saveDefaultConfig(getDataFolder() + File.separator + "Servers" + File.separator + name)));
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
				out.writeUTF("CHECK");
				out.writeUTF(uuid.toString());

				server.sendData("BungeeCord", out.toByteArray());
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
	public void onQuit(PlayerDisconnectEvent e){
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
		if(!e.getTag().equalsIgnoreCase("BungeeCord") && !e.getTag().equalsIgnoreCase("bungeecord:main"))
			return;

		Channel channel = Channel.newInstance(e.getData());

		channel.read();
		if(!channel.get().equalsIgnoreCase(Channel.PACKET_ID))
			return;

		channel.read();
		if(!channel.get().equalsIgnoreCase("RESULT"))
			return;

		channel.read();
		UUID uuid = UUID.fromString(channel.get());
		ProxiedPlayer player = getProxy().getPlayer(UUID.fromString(channel.get()));
		if(!player.isConnected())
			return;

		Type type = Type.NORMAL_JOIN;
		if(channel.getByteArrayDataInput().readBoolean()){
			if(cache.contains(uuid)){
				type = Type.RE_JOIN;
				cache.remove(uuid);
			}
		}else{
			type = Type.FIRST_JOIN;
		}

		boolean isban = channel.getByteArrayDataInput().readBoolean();
		if(isban)
			return;

		sendMessage(type, player.getName(), locs.get(uuid));
		playSound(type);
	}

	public void sendMessage(Type type, String playerName, String... serverNames){
		for(ServerInfo server : getProxy().getServers().values()){
			if(!electrons.containsKey(server.getName()))
				continue;

			if(server.getPlayers().isEmpty())
				continue;

			Settings settings = electrons.get(server.getName()).settings.get(type);
			if(!settings.isDisplay())
				continue;

			String text = settings.getText().replace("[player]", playerName);
			//Util.replaceAll~
			ServerData s1 = electrons.get(serverNames[0]);
			if(type == Type.SWITCH){
				ServerData s2 = electrons.get(serverNames[1]);
				if(s1 != null && s2 != null)
					text = text.replace("[from_server]", electrons.get(serverNames[0]).getAliases()).replace("[to_server]", electrons.get(serverNames[1]).getAliases());
					//text = Util.replaceAll(Util.replaceAll(text, "[from_server]", electrons.get(serverNames[0]).getAliases()), "[to_server]", electrons.get(serverNames[1]).getAliases());
			}else{
				if(s1 != null) text = text.replace("[server]", electrons.get(serverNames[0]).getAliases());
					//text = Util.replaceAll(text, "[server]", electrons.get(serverNames[0]).getAliases());
			}

			TextComponent component = new TextComponent(text);

			//メッセージをアクションバーに表示する場合
			if(actionBarMessageEnable){
				TaskScheduler scheduler = getProxy().getScheduler();

				//何回メッセージを表示したか
				AtomicInteger count = new AtomicInteger();

				TaskHolder holder = new TaskHolder();

				//遅延無し、1秒毎に繰り返す
				holder.setTask(scheduler.schedule(this, new Runnable(){

					@Override
					public void run() {
						for(ProxiedPlayer player : server.getPlayers()) player.sendMessage(ChatMessageType.ACTION_BAR, component);

						//指定秒数だけ表示した場合
						if(count.incrementAndGet() >= actionBarMessageDuration){

							//秒数が偶数であればキャンセルする
							if(actionBarMessageDuration % 2 == 0){
								holder.cancelTask();
								return;
							}

							//秒数が奇数であれば1秒後に空のメッセージを送信した上でキャンセルする
							scheduler.schedule(Main.plugin, new Runnable(){

								@Override
								public void run() {
									for(ProxiedPlayer player : server.getPlayers()) player.sendMessage(ChatMessageType.ACTION_BAR, EMPTY_COMPONENT);
									holder.cancelTask();
								}

							}, 1, TimeUnit.SECONDS);
						}
					}

				}, 0, 1, TimeUnit.SECONDS));

			}else{
				for(ProxiedPlayer player : server.getPlayers()) player.sendMessage(component);
			}
		}
	}

	public void playSound(Type type){
		for(ServerInfo server : getProxy().getServers().values()){
			if(!electrons.containsKey(server.getName()))
				continue;

			if(server.getPlayers().isEmpty())
				continue;

			Settings settings = electrons.get(server.getName()).settings.get(type);
			if(!settings.isPlay())
				return;

			ByteArrayDataOutput out = ByteStreams.newDataOutput();

			out.writeUTF(Channel.PACKET_ID);
			out.writeUTF("PLAY");
			out.writeUTF(settings.getSound());
			out.writeInt(settings.getRepeat());
			out.writeInt(settings.getInterval());
			out.writeFloat(settings.getVolume());
			out.writeFloat(settings.getPitch());

			server.sendData("BungeeCord", out.toByteArray());
		}
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

package amata1219.login.interphone.spigot;

import org.bukkit.plugin.java.JavaPlugin;

public class Electron extends JavaPlugin {

	private static Electron plugin;

	@Override
	public void onEnable(){
		plugin = this;
	}

	@Override
	public void onDisable(){

	}

	public static Electron getPlugin(){
		return plugin;
	}

}

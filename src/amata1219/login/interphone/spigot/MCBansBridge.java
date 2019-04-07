package amata1219.login.interphone.spigot;

import org.bukkit.plugin.Plugin;

import com.mcbans.firestar.mcbans.MCBans;
import com.mcbans.firestar.mcbans.bukkitListeners.PlayerListener;

public class MCBansBridge {

	public static MCBansBridge load(Plugin plugin){
		if(!(plugin instanceof MCBans))
			return null;

		return new MCBansBridge();
	}

	private MCBansBridge(){

	}

	public boolean isExist(String name){
		return PlayerListener.cache.getIfPresent(name.toLowerCase()) != null;
	}

}

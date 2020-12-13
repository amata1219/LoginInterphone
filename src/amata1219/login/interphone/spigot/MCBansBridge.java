package amata1219.login.interphone.spigot;

import org.bukkit.plugin.Plugin;

import com.mcbans.firestar.mcbans.MCBans;
import com.mcbans.firestar.mcbans.bukkitListeners.PlayerListener;

public class MCBansBridge {

	public static MCBansBridge load(Plugin plugin){
		return plugin instanceof MCBans ? new MCBansBridge() : null;
	}

	private MCBansBridge(){

	}
	
	public boolean isBanned(String name){
		if(MCBans.getInstance().apiServer == null) return false;
		
		String ex = PlayerListener.cache.getIfPresent(name.toLowerCase());
		if(ex == null) return false;
		
		String[] s = ex.split(";");
		return s.length >= 5 && "lgtis".contains(s[0]);
	}

}

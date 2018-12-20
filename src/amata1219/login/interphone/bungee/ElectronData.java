package amata1219.login.interphone.bungee;

import java.util.HashMap;

import net.md_5.bungee.config.Configuration;

public class ElectronData {

	public HashMap<Type, Settings> settings = new HashMap<>();

	public ElectronData(Configuration config){
		settings.put(Type.FIRST_JOIN, new Settings(Type.FIRST_JOIN, config));
		settings.put(Type.NORMAL_JOIN, new Settings(Type.NORMAL_JOIN, config));
		settings.put(Type.RE_JOIN, new Settings(Type.RE_JOIN, config));
		settings.put(Type.SWITCH, new Settings(Type.SWITCH, config));
		settings.put(Type.QUIT, new Settings(Type.QUIT, config));
	}

}

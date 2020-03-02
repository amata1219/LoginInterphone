package amata1219.login.interphone.bungee.setting;

import java.util.HashMap;
import java.util.Map;

import amata1219.login.interphone.bungee.setting.EventActionSetting.EventType;
import amata1219.login.interphone.bungee.setting.MessageDisplaySetting.DisplayType;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.config.Configuration;

public class ServerSettingLoading {
	
	public static ServerSetting load(Configuration config){
		Map<EventType, EventActionSetting> eass = new HashMap<>();
		
		for(EventType event : EventType.values()){
			
			String text = color(config.getString(event.name + ".Message.Text"));
			Map<DisplayType, MessageDisplaySetting> mdss = new HashMap<>();
			
			for(DisplayType display : DisplayType.values()){
				
				Configuration section = config.getSection(event.name + ".Message." + display.name);
				
				MessageDisplaySetting mds = new MessageDisplaySetting(
					section.getBoolean("Displayable"),
					section.getInt("Duration")
				);
				
				mdss.put(display, mds);
			}
			
			Configuration section = config.getSection(event.name + ".Sound");
			
			SoundPlaySetting sps = new SoundPlaySetting(
				section.getString("Name"),
				section.getBoolean("Playable"),
				section.getFloat("Volume"),
				section.getFloat("Pitch"),
				section.getInt("Repetitions"),
				section.getInt("Interval")
			);
			
			EventActionSetting eas = new EventActionSetting(text, mdss, sps);
			
			eass.put(event, eas);
		}
		
		return new ServerSetting(color(config.getString("Alias")), eass);
	}
	
	private static String color(String text){
		return ChatColor.translateAlternateColorCodes('&', text);
	}

}

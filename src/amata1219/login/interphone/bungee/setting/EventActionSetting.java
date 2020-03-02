package amata1219.login.interphone.bungee.setting;

import java.util.Map;

import amata1219.login.interphone.bungee.setting.MessageDisplaySetting.DisplayType;

public class EventActionSetting {
	
	public final String text;
	public final Map<DisplayType, MessageDisplaySetting> mdss;
	public final SoundPlaySetting sps;
	
	public EventActionSetting(String text, Map<DisplayType, MessageDisplaySetting> mdss, SoundPlaySetting sps){
		this.text = text;
		this.mdss = mdss;
		this.sps = sps;
	}
	
	public static enum EventType {
		
		FIRST_JOIN("FirstJoin"),
		JOIN("Join"),
		REJOIN("Rejoin"),
		SWITCH("Switch"),
		QUIT("Quit");
		
		public final String name;
		
		private EventType(String name){
			this.name = name;
		}
		
	}

}

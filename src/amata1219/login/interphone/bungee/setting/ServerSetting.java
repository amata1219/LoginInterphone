package amata1219.login.interphone.bungee.setting;

import java.util.Map;

import amata1219.login.interphone.bungee.setting.EventActionSetting.EventType;

public class ServerSetting {
	
	public final String alias;
	public final Map<EventType, EventActionSetting> eass;
	
	public ServerSetting(String alias, Map<EventType, EventActionSetting> eass){
		this.alias = alias;
		this.eass = eass;
	}

}

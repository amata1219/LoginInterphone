package amata1219.login.interphone.bungee.setting;

public class MessageDisplaySetting {
	
	public final boolean displayable;
	public final int duration;
	
	public MessageDisplaySetting(boolean displayable, int duration){
		this.displayable = displayable;
		this.duration = duration;
	}
	
	public static enum DisplayType {
		
		CHAT,
		ACTION_BAR,
		TITLE;
		
	}

}

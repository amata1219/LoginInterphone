package amata1219.login.interphone.bungee;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.config.Configuration;

public class Settings {

	private boolean display;
	private String text;
	private boolean play;
	private String sound;
	private float volume;
	private float pitch;
	private int repeat;
	private int interval;

	public Settings(Type type, Configuration config){
		display = config.getBoolean(type.getString() + ".Message.Display");
		text = ChatColor.translateAlternateColorCodes('&', config.getString(type.getString() + ".Message.Text"));
		play = config.getBoolean(type.getString() + ".Sound.Play");
		sound = config.getString(type.getString() + ".Sound.Type");
		volume = config.getFloat(type.getString() + ".Sound.Volume");
		pitch = config.getFloat(type.getString() + ".Sound.Pitch");
		repeat = config.getInt(type.getString() + ".Sound.Repeat");
		interval = Float.valueOf(config.getFloat(type.getString() + ".Sound.Interval") * 1000).intValue();
	}

	public boolean isDisplay() {
		return display;
	}

	public String getText() {
		return text;
	}

	public boolean isPlay() {
		return play;
	}

	public String getSound() {
		return sound;
	}

	public float getVolume() {
		return volume;
	}

	public float getPitch() {
		return pitch;
	}

	public int getRepeat() {
		return repeat;
	}

	public int getInterval() {
		return interval;
	}

}

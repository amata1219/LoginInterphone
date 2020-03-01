package amata1219.login.interphone.bungee.setting;

public class SoundPlaySetting {
	
	public final String name;
	public final boolean playable;
	public final float volume, pitch;
	public final int repetitions, interval;
	
	public SoundPlaySetting(String name, boolean playable, float volume, float pitch, int reptitions, int interval){
		this.name = name;
		this.playable = playable;
		this.volume = volume;
		this.pitch = pitch;
		this.repetitions = reptitions;
		this.interval = interval;
	}

}

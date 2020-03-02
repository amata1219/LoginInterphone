package amata1219.login.interphone.bungee;

import com.google.common.io.ByteArrayDataInput;

public class Channel {

	public static final String PACKET_ID = "amata1219.login.interphone";

	public static Channel newInstance(ByteArrayDataInput in){
		return new Channel(in);
	}
	
	private ByteArrayDataInput in;
	private String message;

	private Channel(ByteArrayDataInput in){
		this.in = in;
	}

	public String read(){
		return message = in.readUTF();
	}

	public String current(){
		return message;
	}

}

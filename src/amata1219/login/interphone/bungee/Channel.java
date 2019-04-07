package amata1219.login.interphone.bungee;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;

public class Channel {

	public static final String PACKET_ID = "amata1219.login.interphone";

	private ByteArrayDataInput in;

	private String message;

	private Channel(){

	}

	public static Channel newInstance(byte[] data){
		Channel channel = new Channel();

		channel.in = ByteStreams.newDataInput(data);

		return channel;
	}

	public ByteArrayDataInput getByteArrayDataInput(){
		return in;
	}

	public void read(){
		message = in.readUTF();
	}

	public String get(){
		return message;
	}

}

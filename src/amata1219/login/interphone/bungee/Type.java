package amata1219.login.interphone.bungee;

public enum Type {

	FIRST_JOIN("FirstJoin"),
	NORMAL_JOIN("NormalJoin"),
	RE_JOIN("ReJoin"),
	SWITCH("Switch"),
	QUIT("Quit");

	private final String type;

	private Type(String type){
		this.type = type;
	}

	public String getString(){
		return type;
	}

}

package amata1219.login.interphone.bungee;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;

public class LoginInterphoneCommand extends Command {
	
	private final Main plugin = Main.getPlugin();
	
	public LoginInterphoneCommand(){
		super("logininterphone", "login.interphone.logininterphone");
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if(args.length == 0){
			String version = plugin.getDescription().getVersion();
			TextComponent message = new TextComponent(ChatColor.AQUA + "LoginInterphone v" + version);
			sender.sendMessage(message);
		}else if(args[0].equalsIgnoreCase("reload")){
			plugin.loadConfig();
			plugin.loadServerSettings();
			TextComponent message = new TextComponent(ChatColor.AQUA + "設定を再読み込みしました。");
			sender.sendMessage(message);
		}
	}

}

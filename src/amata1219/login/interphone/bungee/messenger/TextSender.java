package amata1219.login.interphone.bungee.messenger;

import amata1219.login.interphone.bungee.Constants;
import amata1219.login.interphone.bungee.Main;
import amata1219.login.interphone.bungee.setting.EventActionSetting;
import amata1219.login.interphone.bungee.setting.EventActionSetting.EventType;
import amata1219.login.interphone.bungee.setting.MessageDisplaySetting;
import amata1219.login.interphone.bungee.setting.ServerSetting;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.Title;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.scheduler.ScheduledTask;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class TextSender {

    private final Main plugin = Main.instance();

    public void sendMessage(EventType event, String playerName, String currentServerName, String previousServerName){
        HashMap<String, ServerSetting> settings = plugin.settings;
        for(ServerInfo server : plugin.getProxy().getServers().values()){
            String serverName = server.getName();
            if(!settings.containsKey(serverName) || server.getPlayers().isEmpty()) continue;

            EventActionSetting eas = settings.get(serverName).eass.get(event);
            String text = eas.text.replace("[player]", playerName);

            for(Map.Entry<MessageDisplaySetting.DisplayType, MessageDisplaySetting> entry : eas.mdss.entrySet()){
                MessageDisplaySetting mds = entry.getValue();
                if(!mds.displayable) continue;

                ServerSetting currentServerSetting = settings.get(currentServerName);
                if(event == EventActionSetting.EventType.SWITCH){
                    ServerSetting previousServerSetting = settings.get(previousServerName);
                    text = text.replace("[from_server]", previousServerSetting != null ? previousServerSetting.alias : "missing")
                            .replace("[to_server]", currentServerSetting != null ? currentServerSetting.alias : "missing");
                }else{
                    text = text.replace("[server]", currentServerSetting != null ? currentServerSetting.alias : "missing");
                }

                TextComponent component = new TextComponent(text);

                switch(entry.getKey()){
                    case CHAT:{
                        for(ProxiedPlayer player : server.getPlayers()) player.sendMessage(component);
                        continue;
                    } case ACTION_BAR: {
                        AtomicInteger count = new AtomicInteger();

                        TaskHolder holder = new TaskHolder();

                        ScheduledTask task = schedule(0, 1, TimeUnit.SECONDS, () -> {
                            for(ProxiedPlayer player : server.getPlayers()) player.sendMessage(ChatMessageType.ACTION_BAR, component);

                            if(count.incrementAndGet() >= mds.duration){
                                if(mds.duration % 2 == 0){
                                    holder.cancelTask();
                                    return;
                                }

                                schedule(1, TimeUnit.SECONDS, () -> {
                                    for(ProxiedPlayer player : server.getPlayers()) player.sendMessage(ChatMessageType.ACTION_BAR, Constants.EMPTY_TEXT_COMPONENT);
                                    holder.cancelTask();
                                });
                            }
                        });

                        holder.setTask(task);
                        continue;
                    } case TITLE: {
                        Title title = plugin.getProxy().createTitle()
                                .title(Constants.EMPTY_TEXT_COMPONENT)
                                .subTitle(component)
                                .stay(mds.duration * 20);
                        for(ProxiedPlayer player : server.getPlayers()) title.send(player);
                        continue;
                    } default: {

                    }
                }
            }
        }
    }

    private ScheduledTask schedule(int delay, TimeUnit unit, Runnable action){
        return plugin.getProxy().getScheduler().schedule(plugin, action, delay, unit);
    }

    private ScheduledTask schedule(int delay, int interval, TimeUnit unit, Runnable action){
        return plugin.getProxy().getScheduler().schedule(plugin, action, delay, interval, unit);
    }

    private static class TaskHolder {

        private ScheduledTask task;

        public void setTask(ScheduledTask task){
            this.task = task;
        }

        public void cancelTask(){
            if(task != null) task.cancel();
        }

    }

}

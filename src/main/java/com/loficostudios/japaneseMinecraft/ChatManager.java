package com.loficostudios.japaneseMinecraft;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/// TODO ADD SUPPORT FOR MULTI-RECEIVERS

public class ChatManager implements Listener {
    private final Map<UUID, UUID> dm = new HashMap<>();

    private static final String CHAT_FORMAT = "§8[<local>§8] §f<player>: §r<message>";
    private static final String CHAT_FORMAT_DM_SELF = "§f[§6DM§f] -> <receiver> §8[<local>§8] §f(you): §r<message>";
    private static final String CHAT_FORMAT_DM_OTHER = "§f[§6DM§f] -> §8[<local>§8] §f<sender>: §r<message>";

    public void startDM(Player sender, Player receiver) {
        dm.put(sender.getUniqueId(), receiver.getUniqueId());
    }

    @EventHandler
    private void onChat(AsyncChatEvent e) {
        e.setCancelled(true);

        var sender = e.getPlayer();
        var message = e.message();

        var isJapanese = JapaneseMinecraft.isPlayerLanguageJapanese(sender);

        var receiverUUID = dm.get(sender.getUniqueId());
        var global = receiverUUID == null;

        if (global) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.sendMessage(Component.text(CHAT_FORMAT)
                        .replaceText("<player>", sender.displayName())
                        .replaceText("<message>", message)
                        .replaceText("<local>", Component.text(isJapanese ? "§5JP" : "§aEN")));
            }
        } else {
            var receiver = Bukkit.getPlayer(receiverUUID);
            if (receiver != null && receiver.isOnline()) {
                sender.sendMessage(Component.text(CHAT_FORMAT_DM_SELF)
                        .replaceText("<receiver>", receiver.displayName())
                        .replaceText("<message>", message)
                        .replaceText("<local>", Component.text(isJapanese ? "§5JP" : "§aEN")));
                receiver.sendMessage(Component.text(CHAT_FORMAT_DM_OTHER)
                        .replaceText("<sender>", sender.displayName())
                        .replaceText("<message>", message)
                        .replaceText("<local>", Component.text(isJapanese ? "§5JP" : "§aEN")));
            } else {
                var eng = "This player is no longer online. '/dm' to exit.";
                var jp = "このプレイヤーはもうオンラインではありません。'/dm'で終了します。";
                sender.sendMessage(JapaneseMinecraft.isPlayerLanguageJapanese(sender) ? jp : eng);
            }
        }
    }

    public boolean stopDM(Player sender) {
        return dm.remove(sender.getUniqueId()) != null;
    }
}

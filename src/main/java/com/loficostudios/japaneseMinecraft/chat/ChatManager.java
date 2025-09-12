package com.loficostudios.japaneseMinecraft.chat;

import com.loficostudios.japaneseMinecraft.JapaneseMinecraft;
import com.loficostudios.japaneseMinecraft.Language;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/// TODO ADD SUPPORT FOR MULTI-RECEIVERS

public class ChatManager implements Listener {
    private final Map<UUID, DM> dm = new HashMap<>();

    private static final String CHAT_FORMAT = "§8[<local>§8] §f<player>: §r<message>";
    private static final String CHAT_FORMAT_DM_SELF = "§f[§6DM§f] -> <receiver> §8[<local>§8] §f(you): §r<message>";
    private static final String CHAT_FORMAT_DM_OTHER = "§f[§6DM§f] -> §8[<local>§8] §f<sender>: §r<message>";

    private final ChatLog log;

    public ChatManager(JapaneseMinecraft plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
        log = new ChatLog(plugin);
    }

    public void startDM(Player sender, Player receiver) {
        dm.put(sender.getUniqueId(), new DM(receiver.getUniqueId()));
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
            Bukkit.getOnlinePlayers().forEach(p -> p.sendMessage(formatMessage(CHAT_FORMAT, sender, p, message, isJapanese ? Language.JAPANESE : Language.ENGLISH)));
        } else {
            var receiver = Bukkit.getPlayer(receiverUUID.with());
            if (receiver != null && receiver.isOnline()) {
                sender.sendMessage(formatMessage(CHAT_FORMAT_DM_SELF, sender, receiver, message, isJapanese ? Language.JAPANESE : Language.ENGLISH));
                receiver.sendMessage(formatMessage(CHAT_FORMAT_DM_OTHER, sender, receiver, message, isJapanese ? Language.JAPANESE : Language.ENGLISH));
            } else {
                var eng = "This player is no longer online. '/dm' to exit.";
                var jp = "このプレイヤーはもうオンラインではありません。'/dm'で終了します。";
                sender.sendMessage(isJapanese ? jp : eng);
            }
        }

        log.log(sender, LegacyComponentSerializer.legacyAmpersand().serialize(message));
    }

    private Component formatMessage(String template, @NotNull Player sender, Player receiver, Component message, Language language) {
        var comp = Component.text(template)
                .replaceText("<message>", message)
                .replaceText("<local>", Component.text(language.equals(Language.JAPANESE) ? "§5JP" : "§aEN"))
                .replaceText("<sender>", sender.displayName())
                .replaceText("<player>", sender.displayName());
        if (receiver != null)
            comp = comp.replaceText("<receiver>", receiver.displayName());
        return comp;
    }

    public boolean stopDM(Player sender) {
        return dm.remove(sender.getUniqueId()) != null;
    }

    public @Nullable String getDM(Player sender) {
        var dm = this.dm.get(sender.getUniqueId());
        if (dm == null) return null;
        var receiver = Bukkit.getPlayer(dm.with());
        if (receiver == null || !receiver.isOnline())
            return null;
        return receiver.getName();
    }
}

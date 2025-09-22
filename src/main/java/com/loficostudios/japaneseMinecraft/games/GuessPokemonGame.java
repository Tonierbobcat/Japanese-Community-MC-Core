package com.loficostudios.japaneseMinecraft.games;

import com.github.jikyo.romaji.Transliterator;
import com.loficostudios.japaneseMinecraft.Common;
import com.loficostudios.japaneseMinecraft.Items;
import com.loficostudios.japaneseMinecraft.JapaneseMinecraft;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class GuessPokemonGame implements Game, Listener {
    private boolean active;

//    private static final String PREFIX = Common.createMessagePrefix("Guess That Pokemon!", "§a");
    private static final String PREFIX = Common.createMessagePrefix("GTP!", "§a");

    private static final Map<String, PokemonGuessEntry> englishEntries = new HashMap<>();
    private static final Map<String, PokemonGuessEntry> japaneseEntries = new HashMap<>();

    private @Nullable PokemonGuessEntry current = null;

    @Override
    public int getLengthMinutes() {
        return 1;
    }

    @Override
    public void reset() {
        current = null;
    }

    @Override
    public void end() {
        active = false;
    }

    @EventHandler
    private void onChat(AsyncChatEvent e) {
        if (!isActive() || current == null)
            return;
        var player = e.getPlayer();
        var isJapanese = JapaneseMinecraft.isPlayerLanguageJapanese(player);

        var message = PlainTextComponentSerializer.plainText().serialize(e.message());

        var eng = "{player} guessed the correct pokemon first: {pokemon}"
                .replace("{player}", player.getName())
                .replace("{pokemon}", current.englishName);
        var jp = "{player}が正しいポケモンを当てました: {pokemon}"
                .replace("{player}", player.getName())
                .replace("{pokemon}", current.japaneseNameKana);

        if (isJapanese) {
            var romaji = Transliterator.transliterate(message.toLowerCase()).getFirst();
            var entry = japaneseEntries.get(romaji);
            if (entry != null) {
                awardWinner(player);
                sendMessageToAllPlayers(eng, jp);
                end();
            }
        } else {
            var entry = englishEntries.get(message.toLowerCase());
            if (entry != null) {
                awardWinner(player);
                sendMessageToAllPlayers(eng, jp);
                end();
            }
        }
    }

    private void awardWinner(Player winner) {
        var item = Items.ITEMS.createItemStack(Items.POKEBALL, 5);
        var money = 50;

        var eng = Component.text(PREFIX + "You have been awarded with {amount}x {item} & ${money}");

        var jp = Component.text(PREFIX + "{item}を{amount}個と${money}を獲得しました");

        JapaneseMinecraft.getEconomyProvider().depositPlayer(JapaneseMinecraft.getPlayerProfile(winner), money);

        var isFull = winner.getInventory().firstEmpty() == -1;
        if (isFull) {
            winner.getWorld().dropItem(winner.getLocation(), item);
        } else {
            winner.getInventory().addItem(item);
        }

        var isJapanese = JapaneseMinecraft.isPlayerLanguageJapanese(winner);

        winner.sendMessage((isJapanese ? jp : eng)
                .replaceText("{item}", item.displayName())
                .replaceText("{money}", Component.text("" + money))
                .replaceText("{amount}", Component.text("" + item.getAmount())));
    }

    private void sendMessageToAllPlayers(String eng, String jp) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            var isJapanese = JapaneseMinecraft.isPlayerLanguageJapanese(player);
            player.sendMessage(PREFIX + (isJapanese ? jp : eng));
        }
    }

    @Override
    public void start() {
        active = true;

        var index = ThreadLocalRandom.current().nextInt(englishEntries.size());
        /// this is nonnull
        current = englishEntries.values().stream().toList().get(index);
        assert current != null;

        for (Player player : Bukkit.getOnlinePlayers()) {
            var isJapanese = JapaneseMinecraft.isPlayerLanguageJapanese(player);

            var eng = "Guess the pokemon: {hint}"
                    .replace("{hint}", current.englishHint);
            var jp = "ポケモンを当ててみよう: {hint}"
                    .replace("{hint}", current.japaneseHint);

            if (isJapanese) {
                player.sendMessage(PREFIX + jp);
            } else {
                player.sendMessage(PREFIX + eng);
            }
        }
    }

    @Override
    public int getMinPlayers() {
//        return 2;
        return 1;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public String getId() {
        return "pokemon";
    }

    @Override
    public String getPrefix() {
        return PREFIX;
    }

    private record PokemonGuessEntry(String englishName, String englishHint, String japaneseNameKana, String japaneseHint) {
    }

    static {
        List<PokemonGuessEntry> entries = new ArrayList<>();

        entries.add(new PokemonGuessEntry("Pikachu", "_i_chu", "ピカチュウ", "ピカ〇〇ウ"));
        entries.add(new PokemonGuessEntry("Bulbasaur", "bul__saur", "フシギダネ", "フシ〇〇ネ"));
        entries.add(new PokemonGuessEntry("Charmander", "Cha__nder", "ヒトカゲ", "ヒト〇ゲ"));
        entries.add(new PokemonGuessEntry("Squirtle", "Squi__le", "ゼニガメ", "ゼ〇ガメ"));
        entries.add(new PokemonGuessEntry("Jigglypuff", "__ggly__ff", "プリン", "プ〇ン"));
        entries.add(new PokemonGuessEntry("Meowth", "Meo_t_", "ニャース", "ニャー〇"));
        entries.add(new PokemonGuessEntry("Psyduck", "Ps__uck", "コダック", "コ〇ック"));
        entries.add(new PokemonGuessEntry("Snorlax", "Sno_la_", "カビゴン", "カビ〇ン"));
        entries.add(new PokemonGuessEntry("Eevee", "E_ve_", "イーブイ", "イー〇〇"));
        entries.add(new PokemonGuessEntry("Mewtwo", "M_w_wo", "ミュウツー", "ミュウ〇ー"));

        for (PokemonGuessEntry entry : entries) {
            englishEntries.put(entry.englishName.toLowerCase(), entry);

            var romaji = Transliterator.transliterate(entry.japaneseNameKana).getFirst();

            japaneseEntries.put(romaji, entry);
        }
    }
}

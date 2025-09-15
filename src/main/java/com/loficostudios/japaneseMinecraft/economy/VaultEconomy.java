package com.loficostudios.japaneseMinecraft.economy;

import com.loficostudios.japaneseMinecraft.JapaneseMinecraft;
import com.loficostudios.japaneseMinecraft.profile.PlayerProfile;
import com.loficostudios.japaneseMinecraft.shop.EconomyProvider;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.List;
import java.util.function.BiConsumer;

public class VaultEconomy implements Economy, EconomyProvider {
    private final JapaneseMinecraft plugin;

    public VaultEconomy(JapaneseMinecraft plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean isEnabled() {
        return plugin.isEnabled();
    }

    @Override
    public String getName() {
        return "JapaneseMC";
    }

    @Override
    public boolean hasBankSupport() {
        return false;
    }

    @Override
    public int fractionalDigits() {
        return -1;
    }

    @Override
    public String format(double v) {
        return "" + v;
    }

    @Override
    public String currencyNamePlural() {
        return currencyNameSingular();
    }

    @Override
    public String currencyNameSingular() {
        return "$";
    }

    @Override
    public boolean hasAccount(String playerName) {
        return hasAccount(Bukkit.getOfflinePlayer(playerName));
    }

    @Override
    public boolean hasAccount(OfflinePlayer offlinePlayer) {
        return plugin.getProfileManager().getProfile(offlinePlayer.getUniqueId()) != null;
    }

    @Override
    public boolean hasAccount(String playerName, String world) {
        return hasAccount(playerName);
    }

    @Override
    public boolean hasAccount(OfflinePlayer offlinePlayer, String world) {
        return hasAccount(offlinePlayer);
    }

    @Override
    public double getBalance(String playerName) {
        return getBalance(Bukkit.getOfflinePlayer(playerName));
    }

    @Override
    public double getBalance(OfflinePlayer offlinePlayer) {
        var profile = plugin.getProfileManager().getOrCreateOfflineProfile(offlinePlayer);
        return profile != null ? profile.getMoney() : 0.0;
    }

    @Override
    public double getBalance(String playerName, String world) {
        return getBalance(playerName);
    }

    @Override
    public double getBalance(OfflinePlayer offlinePlayer, String world) {
        return getBalance(offlinePlayer);
    }

    @Override
    public boolean has(String playerName, double amount) {
        return has(Bukkit.getOfflinePlayer(playerName), amount);
    }

    @Override
    public boolean has(OfflinePlayer offlinePlayer, double amount) {
        var profile = plugin.getProfileManager().getOrCreateOfflineProfile(offlinePlayer);
        return profile != null && has(profile, amount);
    }

    @Override
    public boolean has(String playerName, String world, double v) {
        return has(playerName, v);
    }

    @Override
    public boolean has(OfflinePlayer offlinePlayer, String world, double v) {
        return has(offlinePlayer, v);
    }

    @Override
    public EconomyResponse withdrawPlayer(String playerName, double v) {
        return withdrawPlayer(Bukkit.getOfflinePlayer(playerName), v);
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer offlinePlayer, double v) {
        return compute(offlinePlayer, v, this::withdrawPlayer);
    }

    @Override
    public EconomyResponse withdrawPlayer(String playerName, String world, double v) {
        return withdrawPlayer(playerName, v);
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer offlinePlayer, String world, double v) {
        return withdrawPlayer(offlinePlayer, v);
    }

    @Override
    public EconomyResponse depositPlayer(String playerName, double v) {
        return depositPlayer(Bukkit.getOfflinePlayer(playerName), v);
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer offlinePlayer, double v) {
        return compute(offlinePlayer, v, this::depositPlayer);
    }

    @Override
    public EconomyResponse depositPlayer(String playerName, String world, double v) {
        return depositPlayer(playerName, v);
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer offlinePlayer, String world, double v) {
        return depositPlayer(offlinePlayer, v);
    }

    @Override
    public boolean createPlayerAccount(String playerName) {
        return createPlayerAccount(Bukkit.getOfflinePlayer(playerName));
    }

    public EconomyResponse compute(OfflinePlayer offlinePlayer, double amount, BiConsumer<PlayerProfile, Double> runnable) {
        try {
            PlayerProfile profile = plugin.getProfileManager().getOrCreateOfflineProfile(offlinePlayer);
            runnable.accept(profile, amount);
            return new EconomyResponse(amount, getBalance(offlinePlayer), EconomyResponse.ResponseType.SUCCESS, null);
        } catch (Exception e) {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Could not create profile for player '{uuid}'".replace("{uuid}", "" + offlinePlayer.getUniqueId()));
        }
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer offlinePlayer) {
        var profile = plugin.getProfileManager().getOrCreateOfflineProfile(offlinePlayer);
        return profile != null;
    }

    @Override
    public boolean createPlayerAccount(String playerName, String world) {
        return createPlayerAccount(playerName);
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer offlinePlayer, String world) {
        return createPlayerAccount(offlinePlayer);
    }

    @Override
    public EconomyResponse createBank(String s, String s1) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "JapaneseMC does not support bank accounts!");
    }

    @Override
    public EconomyResponse createBank(String s, OfflinePlayer offlinePlayer) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "JapaneseMC does not support bank accounts!");
    }

    @Override
    public EconomyResponse deleteBank(String s) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "JapaneseMC does not support bank accounts!");
    }

    @Override
    public EconomyResponse bankBalance(String s) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "JapaneseMC does not support bank accounts!");

    }

    @Override
    public EconomyResponse bankHas(String s, double v) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "JapaneseMC does not support bank accounts!");

    }

    @Override
    public EconomyResponse bankWithdraw(String s, double v) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "JapaneseMC does not support bank accounts!");

    }

    @Override
    public EconomyResponse bankDeposit(String s, double v) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "JapaneseMC does not support bank accounts!");

    }

    @Override
    public EconomyResponse isBankOwner(String s, String s1) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "JapaneseMC does not support bank accounts!");

    }

    @Override
    public EconomyResponse isBankOwner(String s, OfflinePlayer offlinePlayer) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "JapaneseMC does not support bank accounts!");

    }

    @Override
    public EconomyResponse isBankMember(String s, String s1) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "JapaneseMC does not support bank accounts!");

    }

    @Override
    public EconomyResponse isBankMember(String s, OfflinePlayer offlinePlayer) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "JapaneseMC does not support bank accounts!");

    }

    @Override
    public List<String> getBanks() {
        return List.of();
    }

    @Override
    public boolean has(PlayerProfile player, double amount) {
        return player.hasMoney(amount);
    }

    @Override
    public boolean withdrawPlayer(PlayerProfile player, double amount) {
        player.subtractMoney(amount);
        return true;
    }

    @Override
    public boolean depositPlayer(PlayerProfile player, double amount) {
        player.addMoney(amount);
        return true;
    }

    @Override
    public double getBalance(PlayerProfile player) {
        return player.getMoney();
    }
}

package me.kaotich00.easyranking;

import me.kaotich00.easyranking.api.service.TaskService;
import me.kaotich00.easyranking.commandrework.CommandManager;
import me.kaotich00.easyranking.listener.board.*;
import me.kaotich00.easyranking.listener.gui.reward.GUIRewardListener;
import me.kaotich00.easyranking.listener.gui.reward.TitleRewardListener;
import me.kaotich00.easyranking.listener.player.PlayerJoinListener;
import me.kaotich00.easyranking.reward.types.title.TitleExpansion;
import me.kaotich00.easyranking.service.ERBoardService;
import me.kaotich00.easyranking.service.ERRewardService;
import me.kaotich00.easyranking.service.ERTaskService;
import me.kaotich00.easyranking.storage.Storage;
import me.kaotich00.easyranking.storage.StorageFactory;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class Easyranking extends JavaPlugin {

    public static FileConfiguration defaultConfig;
    public static Economy economyService;

    @Override
    public void onEnable() {
        Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "[EasyRanking]" + ChatColor.RESET + " Loading configuration...");
        loadConfiguration();

        Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "[EasyRanking]" + ChatColor.RESET + " Initializing database...");
        initStorage();

        Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "[EasyRanking]" + ChatColor.RESET + " Registering commands...");
        registerCommands();

        Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "[EasyRanking]" + ChatColor.RESET + " Registering listeners...");
        registerListeners();

        Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "[EasyRanking]" + ChatColor.RESET + " Registering services...");
        registerServices();

        Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "[EasyRanking]" + ChatColor.RESET + " Registering economy...");
        if (!setupEconomy()) {
            this.getLogger().severe("This plugin needs Vault and an Economy plugin in order to function!");
            Bukkit.getPluginManager().disablePlugin(this);
        }

    }

    @Override
    public void onDisable() {
        shutdownStorage();
    }

    private void loadConfiguration() {
        getConfig().options().copyDefaults(true);
        saveDefaultConfig();
        defaultConfig = getConfig();
    }

    public void registerCommands() {
        Objects.requireNonNull(getCommand("er")).setExecutor(new CommandManager());
    }

    public void registerServices() {
        ERRewardService.getInstance();
        ERBoardService.getInstance();
        ERTaskService.getInstance();
    }

    public void initStorage() {
        Storage storage = StorageFactory.getInstance();
        storage.init();
    }

    public void shutdownStorage() {
        Storage storage = StorageFactory.getInstance();
        storage.shutdown();
    }

    public void registerListeners(){
        getServer().getPluginManager().registerEvents(new GUIRewardListener(),this);
        getServer().getPluginManager().registerEvents(new KilledMobsListener(),this);
        getServer().getPluginManager().registerEvents(new PlayerDeathListener(),this);
        getServer().getPluginManager().registerEvents(new OresMinedListener(),this);
        getServer().getPluginManager().registerEvents(new WoodCutListener(),this);
        getServer().getPluginManager().registerEvents(new FarmingListener(),this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(),this);
        getServer().getPluginManager().registerEvents(new FishingListener(),this);


        // If PlaceholderAPI is enable, easyranking will use it, otherwise
        // it will use a custom made chat listener
        if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null){
            Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "[EasyRanking]" + ChatColor.RESET + " Hooking to PlaceholderAPI...");
            new TitleExpansion(this).register();
        } else {
            Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "[EasyRanking]" + ChatColor.RESET + " PlaceholderAPI not found, using default title handler.");
            getServer().getPluginManager().registerEvents(new TitleRewardListener(), this);
        }

        // If Bounties is enable, easyranking will hook into it
        if(Bukkit.getPluginManager().getPlugin("Bounties") != null) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "[EasyRanking]" + ChatColor.RESET + " Hooking to Bounties...");
            getServer().getPluginManager().registerEvents(new BountiesListener(),this);
        }
    }

    @SuppressWarnings("ConstantConditions")
    public boolean setupEconomy() {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null)
            return false;

        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);

        if (rsp == null)
            return false;

        economyService = rsp.getProvider();

        return economyService != null;
    }

    public static FileConfiguration getDefaultConfig() {
        return defaultConfig;
    }

    public static Economy getEconomy() {
        return economyService;
    }

    public void reloadDefaultConfig() {
        reloadConfig();
        defaultConfig = getConfig();
        TaskService taskService = ERTaskService.getInstance();
        // Re-schedule syn tasks
        taskService.stopEconomyTask();
        taskService.stopDatabaseSyncTask();
        taskService.scheduleEconomyTask();
        taskService.scheduleDatabaseSyncTask();
    }

}
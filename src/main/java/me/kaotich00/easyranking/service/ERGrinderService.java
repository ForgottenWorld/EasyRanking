package me.kaotich00.easyranking.service;

import me.kaotich00.easyranking.Easyranking;
import me.kaotich00.easyranking.api.service.GrinderService;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.UUID;

public class ERGrinderService implements GrinderService {

    private static ERGrinderService instance;

    private final HashMap<UUID, Long> uuidDeathTimeMap;
    private int grinderTaskID = -1;
    private boolean isEnabled;

    private ERGrinderService() {
        if (instance != null)
            throw new RuntimeException("Use getInstance() method to get the single instance of this class.");

        this.uuidDeathTimeMap = new HashMap<>();
        scheduleGrinderTask();
    }

    public static ERGrinderService getInstance() {
        if(instance == null)
            instance = new ERGrinderService();
        return instance;
    }



    @Override
    public void scheduleGrinderTask() {
        FileConfiguration defaultConfig = Easyranking.getDefaultConfig();
        this.isEnabled = defaultConfig.getBoolean("playerKilled.anti_grinder.enabled",false);

        if (!this.isEnabled) {
            this.grinderTaskID = -1;
            return;
        }

        long check_frequency = defaultConfig.getLong("playerKilled.anti_grinder.check_frequency") * 1200;
        long clear_period = defaultConfig.getLong("playerKilled.anti_grinder.clear_period") * 1200;

        this.grinderTaskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(Easyranking.getPlugin(Easyranking.class),
                () -> {
            long checkTime = System.currentTimeMillis() - clear_period;
            this.uuidDeathTimeMap.entrySet().removeIf(entry -> entry.getValue() < checkTime);
        },check_frequency,check_frequency);

    }

    @Override
    public void stopGrinderTask() {
        if (grinderTaskID != -1) {
            Bukkit.getServer().getScheduler().cancelTask(grinderTaskID);
            grinderTaskID = -1;
        }
    }

    @Override
    public boolean addGrinderUUID(UUID uuid, Long mills) {
        return !this.isEnabled || this.uuidDeathTimeMap.putIfAbsent(uuid,mills) == null;
    }

}

package me.kaotich00.easyranking.listener.board.mospawn;

import me.kaotich00.easyranking.Easyranking;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.SpawnerSpawnEvent;
import org.bukkit.metadata.FixedMetadataValue;

public class MobSpawnListener implements Listener {

    @EventHandler
    public void onMobSpawnFromSpawner(SpawnerSpawnEvent event) {
        event.getEntity()
                .setMetadata("SPAWNER", new FixedMetadataValue(Easyranking.getPlugin(Easyranking.class), true));
    }

}

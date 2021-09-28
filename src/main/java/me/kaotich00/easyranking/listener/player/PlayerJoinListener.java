package me.kaotich00.easyranking.listener.player;

import me.kaotich00.easyranking.api.service.RewardService;
import me.kaotich00.easyranking.service.ERRewardService;
import me.kaotich00.easyranking.storage.StorageFactory;
import me.kaotich00.easyranking.storage.StorageMethod;
import me.kaotich00.easyranking.utils.ChatFormatter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class PlayerJoinListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {

        Player player = event.getPlayer();

        RewardService rewardService = ERRewardService.getInstance();
        List<ItemStack> rewardsForPlayer = rewardService.getUncollectedRewardForUser(event.getPlayer().getUniqueId());

        if (rewardsForPlayer == null)
            return;

        player.sendMessage(ChatFormatter.formatSuccessMessage("You had uncollected rewards, please check your"
                + " inventory or drops if you have a full inventory."));

        for( ItemStack reward : rewardsForPlayer ) {
            if (player.getInventory().addItem(reward).size() != 0)
                player.getWorld().dropItem(player.getLocation(), reward);
        }

        rewardService.removeUncollectedItemsForPlayer(player.getUniqueId());
        CompletableFuture.runAsync(() -> {
            StorageMethod storage = StorageFactory.getInstance().getStorageMethod();
            storage.clearUncollectedRewards(player.getUniqueId());
        });

    }

}

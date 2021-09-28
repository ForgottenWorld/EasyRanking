package me.kaotich00.easyranking.listener.board;

import me.kaotich00.easyranking.Easyranking;
import me.kaotich00.easyranking.api.board.Board;
import me.kaotich00.easyranking.api.service.BoardService;
import me.kaotich00.easyranking.service.ERBoardService;
import me.kaotich00.easyranking.utils.BoardUtil;
import org.bukkit.GameMode;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;

import java.util.Objects;
import java.util.Optional;

public class FishingListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFishing(PlayerFishEvent event) {
        if (event.getPlayer().getGameMode().equals(GameMode.CREATIVE)
                || !event.getState().equals(PlayerFishEvent.State.CAUGHT_FISH)) return;

        BoardService boardService = ERBoardService.getInstance();
        Player player = event.getPlayer();

        if (boardService.isUserExempted(player.getUniqueId())) return;

        Item item = (Item) event.getCaught();

        if (Objects.isNull(item)) return;

        String itemName = item.getItemStack().getType().name();

        FileConfiguration defaultConfig = Easyranking.getDefaultConfig();
        ConfigurationSection fishingSection = defaultConfig.getConfigurationSection("fishing.values");

        if (fishingSection == null
                || !fishingSection.contains(itemName))
            return;

        Optional<Board> optionalBoard = boardService.getBoardById(BoardUtil.FISHING_BOARD_ID);

        if (!optionalBoard.isPresent()) return;

        Board board = optionalBoard.get();

        int score = defaultConfig.getInt("fishing.values." + itemName);
        boardService.addScoreToPlayer(board, player.getUniqueId(), (float) score);

    }

}

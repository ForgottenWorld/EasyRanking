package me.kaotich00.easyranking.listener.board;

import me.kaotich00.easyranking.Easyranking;
import me.kaotich00.easyranking.api.board.Board;
import me.kaotich00.easyranking.api.service.BoardService;
import me.kaotich00.easyranking.service.ERBoardService;
import me.kaotich00.easyranking.utils.BoardUtil;
import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.Optional;

public class WoodCutListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onWoodCut(BlockBreakEvent event) {
        if (event.getPlayer().getGameMode().equals(GameMode.CREATIVE))
            return;

        if (!event.getBlock().getMetadata("PLACED").isEmpty())
            return;

        FileConfiguration defaultConfig = Easyranking.getDefaultConfig();
        ConfigurationSection oreSection = defaultConfig.getConfigurationSection("wood.values");

        if(oreSection == null || !oreSection.contains(event.getBlock().getType().name()))
            return;

        BoardService boardService = ERBoardService.getInstance();
        Player player = event.getPlayer();

        if (boardService.isUserExempted(player.getUniqueId()))
            return;

        Optional<Board> optionalBoard = boardService.getBoardById(BoardUtil.CARPENTER_BOARD_ID);

        if (!optionalBoard.isPresent())
            return;

        Board board = optionalBoard.get();
        String minedWood = event.getBlock().getType().name();

        int score = defaultConfig.getInt("wood.values." + minedWood);
        boardService.addScoreToPlayer(board, player.getUniqueId(), (float) score);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onWoodPlaced(BlockPlaceEvent event) {
        FileConfiguration defaultConfig = Easyranking.getDefaultConfig();
        ConfigurationSection woodSection = defaultConfig.getConfigurationSection("wood.values");

        if (woodSection == null || !woodSection.contains(event.getBlock().getType().name()))
            return;

        Block block = event.getBlock();
        block.setMetadata("PLACED", new FixedMetadataValue(Easyranking.getPlugin(Easyranking.class), true));
    }

}

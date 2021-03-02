package me.kaotich00.easyranking.listener.board;

import me.kaotich00.easyranking.Easyranking;
import me.kaotich00.easyranking.api.board.Board;
import me.kaotich00.easyranking.api.service.BoardService;
import me.kaotich00.easyranking.service.ERBoardService;
import me.kaotich00.easyranking.utils.BoardUtil;
import org.bukkit.GameMode;
import org.bukkit.block.data.Ageable;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.Optional;

public class FarmingListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFarming(BlockBreakEvent event) {
        if( event.getPlayer().getGameMode().equals(GameMode.CREATIVE) ) {
            return;
        }

        if( !event.getBlock().getMetadata("PLACED").isEmpty() ) {
            return;
        }

        FileConfiguration defaultConfig = Easyranking.getDefaultConfig();
        ConfigurationSection oreSection = defaultConfig.getConfigurationSection("farming.values");
        if( !oreSection.contains(event.getBlock().getType().name()) ) {
            return;
        }

        BoardService boardService = ERBoardService.getInstance();
        Player player = event.getPlayer();

        if(boardService.isUserExempted(player.getUniqueId())) {
            return;
        }

        Optional<Board> optionalBoard = boardService.getBoardById(BoardUtil.FARMER_BOARD_ID);

        if( !optionalBoard.isPresent() ) {
            return;
        }

        Board board = optionalBoard.get();
        String brokenCultivation = event.getBlock().getType().name();

        if(!(event.getBlock().getBlockData() instanceof Ageable)) {
            return;
        }

        Ageable age = (Ageable) event.getBlock().getBlockData();

        if(!(age.getAge() == age.getMaximumAge())) {
            return;
        }

        Integer score = defaultConfig.getInt("farming.values." + brokenCultivation);
        boardService.addScoreToPlayer(board, player.getUniqueId(), score.floatValue());
    }

}

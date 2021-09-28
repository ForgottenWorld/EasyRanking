package me.kaotich00.easyranking.listener.board;

import me.kaotich00.easyranking.Easyranking;
import me.kaotich00.easyranking.api.board.Board;
import me.kaotich00.easyranking.api.service.BoardService;
import me.kaotich00.easyranking.service.ERBoardService;
import me.kaotich00.easyranking.utils.BoardUtil;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.Optional;

public class KilledMobsListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMobKilled(EntityDeathEvent event) {
        if (event.getEntity().getKiller() == null)
            return;

        if (!(event.getEntity() instanceof Monster)
                && !(event.getEntity() instanceof Flying)
                && !(event.getEntity() instanceof Slime)
                && !(event.getEntity() instanceof Animals)) {
            return;
        }

        BoardService boardService = ERBoardService.getInstance();
        Player player = event.getEntity().getKiller();

        if (boardService.isUserExempted(player.getUniqueId()))
            return;

        Optional<Board> optionalBoard = boardService.getBoardById(BoardUtil.MOB_KILLED_BOARD_ID);

        if (!optionalBoard.isPresent())
            return;

        Board board = optionalBoard.get();

        FileConfiguration defaultConfig = Easyranking.getDefaultConfig();
        ConfigurationSection mobSection = defaultConfig.getConfigurationSection("mobKilled.values");

        String killedMob = event.getEntityType().name();

        if (mobSection == null || !mobSection.contains(killedMob))
            return;

        int score = defaultConfig.getInt("mobKilled.values." + killedMob);
        boardService.addScoreToPlayer(board, player.getUniqueId(), (float) score);

    }

}

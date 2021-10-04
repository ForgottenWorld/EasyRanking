package me.kaotich00.easyranking.listener.board;

import me.kaotich00.easyranking.api.service.BoardService;
import me.kaotich00.easyranking.service.ERBoardService;
import me.kaotich00.easyranking.service.ERGrinderService;
import me.kaotich00.easyranking.utils.BoardUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.*;

public class PlayerDeathListener implements Listener {
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        UUID playerDeathUUID = event.getEntity().getUniqueId();

        BoardService boardService = ERBoardService.getInstance();

        if (boardService.isUserExempted(playerDeathUUID))
            return;

        boardService.getBoardById(BoardUtil.DEATH_BOARD_ID)
                .ifPresent(board -> boardService.addScoreToPlayer(board,playerDeathUUID, 1f));

        if (event.getEntity().getKiller() == null)
            return;

        UUID playerKillerUUID = event.getEntity().getKiller().getUniqueId();

        if (boardService.isUserExempted(playerKillerUUID))
            return;

        if (!ERGrinderService.getInstance().addGrinderUUID(playerDeathUUID,System.currentTimeMillis()))
            return;

        boardService.getBoardById(BoardUtil.PLAYER_KILLED_BOARD_ID)
                .ifPresent(board -> boardService.addScoreToPlayer(board, playerKillerUUID, 1f));


    }

}
